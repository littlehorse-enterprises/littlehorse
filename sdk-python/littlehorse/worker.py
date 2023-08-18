import asyncio
from datetime import datetime
from inspect import Parameter, signature, iscoroutinefunction
import logging
import signal
from typing import Any, AsyncIterator, Callable
from littlehorse.config import LHConfig
from littlehorse.exceptions import (
    InvalidTaskDefNameException,
    TaskSchemaMismatchException,
    UnknownApiException,
)
from littlehorse.model.service_pb2 import (
    GetTaskDefReplyPb,
    LHResponseCodePb,
    PollTaskPb,
    RegisterTaskWorkerPb,
    RegisterTaskWorkerReplyPb,
    ScheduledTaskPb,
    TaskDefIdPb,
    TaskDefPb,
    VariableTypePb,
)
from littlehorse.model.service_pb2_grpc import LHPublicApiStub

VARIABLE_TYPES_MAP = {
    VariableTypePb.JSON_OBJ: dict[str, Any],
    VariableTypePb.JSON_ARR: list[Any],
    VariableTypePb.DOUBLE: float,
    VariableTypePb.BOOL: bool,
    VariableTypePb.STR: str,
    VariableTypePb.INT: int,
    VariableTypePb.BYTES: bytes,
}


class LHWorkerContext:
    def __init__(self, scheduled_task: ScheduledTaskPb) -> None:
        self._scheduled_task = scheduled_task

    def scheduled_time(self) -> datetime:
        return datetime.fromtimestamp(float(self._scheduled_task.created_at.seconds))

    def task_run_id(self) -> str:
        return self._scheduled_task.task_run_id.task_guid

    def wf_run_id(self) -> str:
        return self._scheduled_task.task_run_id.wf_run_id

    def __str__(self) -> str:
        return (
            f"WfRunId: {self.wf_run_id()}, TaskRunId: {self.task_run_id()}, "
            f"ScheduledTime: {self.scheduled_time()}"
        )


class LHTaskExecutor:
    _log = logging.getLogger("LHTaskExecutor")

    def __init__(
        self, callable: Callable[..., Any], task_def: TaskDefPb, size: int
    ) -> None:
        self.task_def = task_def

        self._callable = callable
        self._signature = signature(callable)
        self._ask_for_work_semaphore = asyncio.Semaphore(size)

        self._validate_callable()
        self._validate_match()

    def _validate_match(self) -> None:
        task_def_vars = [
            VARIABLE_TYPES_MAP[var.type] for var in self.task_def.input_vars
        ]

        callable_params = [
            param.annotation
            for param in self._signature.parameters.values()
            if param.annotation is not LHWorkerContext
        ]

        if len(task_def_vars) != len(callable_params):
            raise TaskSchemaMismatchException(
                f"Incorrect parameter list, expected: {task_def_vars}"
            )

        for task_def_var, callable_param in zip(task_def_vars, callable_params):
            if task_def_var != callable_param:
                raise TaskSchemaMismatchException(
                    f"Parameter types do not match, expected: {task_def_vars}"
                )

    def _validate_callable(self) -> None:
        def filter(filer: Callable[[Parameter], bool]) -> list[Parameter]:
            return [
                param for param in self._signature.parameters.values() if filer(param)
            ]

        def names(parameters: list[Parameter]) -> list[str]:
            return [param.name for param in parameters]

        # validate is coroutine
        if not iscoroutinefunction(self._callable):
            raise TaskSchemaMismatchException("Is not a coroutine function")

        # validate *args
        filtered_params = filter(lambda param: param.kind is Parameter.VAR_POSITIONAL)
        if len(filtered_params) > 0:
            raise TaskSchemaMismatchException(
                f"Positional parameters (*args) not allowed: {names(filtered_params)}"
            )

        # validate *kwargs
        filtered_params = filter(lambda param: param.kind is Parameter.VAR_KEYWORD)
        if len(filtered_params) > 0:
            raise TaskSchemaMismatchException(
                f"Keyword parameters (*kwargs) not allowed: {names(filtered_params)}"
            )

        # validate not annotated
        filtered_params = filter(lambda param: param.annotation is Parameter.empty)
        if len(filtered_params) > 0:
            raise TaskSchemaMismatchException(
                f"Not annotated parameters found: {names(filtered_params)}"
            )

        # validate any
        filtered_params = filter(lambda param: param.annotation is Any)
        if len(filtered_params) > 0:
            raise TaskSchemaMismatchException(
                f"Any is not allowed: {names(filtered_params)}"
            )

        # validate context is not repeated
        filtered_params = filter(lambda param: param.annotation is LHWorkerContext)
        if len(filtered_params) > 1:
            raise TaskSchemaMismatchException(
                f"Too many context arguments (expected 1): {names(filtered_params)}"
            )

        # validate context is the last one
        if len(filtered_params) > 0:
            last_parameter = list(self._signature.parameters.values())[-1]

            if last_parameter.annotation is not LHWorkerContext:
                raise TaskSchemaMismatchException(
                    "The WorkerContext should be the last parameter"
                )

    def task_name(self) -> str:
        return self.task_def.name

    def has_context(self) -> bool:
        last_parameter = list(self._signature.parameters.values())[-1]
        return last_parameter.annotation is LHWorkerContext

    async def schedule_task(self, task: ScheduledTaskPb) -> None:
        await self._ask_for_work_semaphore.acquire()
        asyncio.create_task(self._execute_task(task))

    async def _execute_task(self, task: ScheduledTaskPb) -> None:
        args: Any = [var.value.str for var in task.variables]
        if self.has_context():
            args.append(LHWorkerContext(task))
        await self._callable(*args)
        self._ask_for_work_semaphore.release()


class LHConnection:
    _log = logging.getLogger("LHConnection")

    def __init__(
        self, server: str, config: LHConfig, task_executor: LHTaskExecutor
    ) -> None:
        self.server = server
        self.running = False
        self._task_executor = task_executor
        self._config = config
        self._ask_for_work_semaphore = asyncio.Semaphore()

    async def _ask_for_work(self) -> None:
        async with self._config.establish_channel(
            server=self.server, async_channel=True
        ) as channel:
            stub = LHPublicApiStub(channel)

            self._log.debug(
                "Connection: %s is asking for work for %s at %s",
                self.server,
                self._task_executor.task_name(),
                datetime.now(),
            )

            async def generator() -> AsyncIterator[PollTaskPb]:
                while self.running:
                    await self._ask_for_work_semaphore.acquire()
                    if self.running:
                        yield PollTaskPb(
                            client_id=self._config.client_id(),
                            task_worker_version=self._config.worker_version(),
                            task_def_name=self._task_executor.task_name(),
                        )

            async for reply in stub.PollTask(generator()):
                if reply.code != LHResponseCodePb.OK:
                    raise UnknownApiException(message=reply.message)
                await self._task_executor.schedule_task(reply.result)
                self._ask_for_work_semaphore.release()

    async def start(self) -> None:
        self._log.info(f"Starting server connection {self.server}")
        self.running = True
        await self._ask_for_work()

    async def stop(self) -> None:
        self._log.info(f"Stopping server connection {self.server}")
        self.running = False
        if self._ask_for_work_semaphore is not None:
            self._ask_for_work_semaphore.release()


class LHTaskWorker:
    """The LHTaskWorker talks to the LH Servers and executes a
    specified Task Method every time a Task is scheduled.
    """

    _log = logging.getLogger("LHTaskWorker")

    def __init__(
        self, callable: Callable[..., Any], task_def_name: str, config: LHConfig
    ) -> None:
        self._config = config
        self._connections: dict[str, LHConnection] = {}

        # get the task definition from the server
        stub = config.blocking_stub()
        reply: GetTaskDefReplyPb = stub.GetTaskDef(TaskDefIdPb(name=task_def_name))

        if reply.code is not LHResponseCodePb.OK:
            raise InvalidTaskDefNameException(f"Couldn't find TaskDef: {task_def_name}")

        # initialize internal task and parameters
        self._task_executor = LHTaskExecutor(
            callable, reply.result, config.num_worker_threads()
        )
        self.running = False

    async def _heartbeat(self) -> None:
        async with self._config.establish_channel(async_channel=True) as channel:
            stub = LHPublicApiStub(channel)
            while self.running:
                self._log.debug("Sending heart beat at %s", datetime.now())

                request = RegisterTaskWorkerPb(
                    client_id=self._config.client_id(),
                    listener_name=self._config.server_listener(),
                    task_def_name=self._task_executor.task_name(),
                )
                reply: RegisterTaskWorkerReplyPb = await stub.RegisterTaskWorker(
                    request
                )

                if reply.code != LHResponseCodePb.OK:
                    raise UnknownApiException(message=reply.message)

                hosts = [f"{host.host}:{host.port}" for host in reply.your_hosts]

                # add new connections
                hosts_to_be_added = [
                    host for host in hosts if host not in self._connections.keys()
                ]

                if hosts_to_be_added:
                    self._log.info("Connections to be added: %s", hosts_to_be_added)

                for host in hosts_to_be_added:
                    new_connection = LHConnection(
                        host, self._config, self._task_executor
                    )
                    self._connections[host] = new_connection
                    asyncio.create_task(new_connection.start())

                # remove invalid connections
                hosts_to_be_removed = {
                    host for host in self._connections.keys() if host not in hosts
                }

                if hosts_to_be_removed:
                    self._log.info("Connections to be removed: %s", hosts_to_be_removed)

                for host in hosts_to_be_removed:
                    connection_to_be_removed = self._connections.pop(host)
                    asyncio.create_task(connection_to_be_removed.stop())

                await asyncio.sleep(5)

    async def start(self) -> None:
        """Starts polling for and executing tasks."""
        self._log.info("Starting worker")
        self.running = True
        loop = asyncio.get_running_loop()

        for sig in (signal.SIGHUP, signal.SIGTERM, signal.SIGINT):
            loop.add_signal_handler(sig, lambda: asyncio.create_task(self.stop()))

        await self._heartbeat()

    async def stop(self) -> None:
        """Cleanly shuts down the Task Worker."""
        self._log.info("Stopping worker")
        self.running = False

        for connection in self._connections.values():
            await connection.stop()

        tasks = [
            task for task in asyncio.all_tasks() if task is not asyncio.current_task()
        ]
        await asyncio.gather(*tasks, return_exceptions=True)
