import asyncio
from datetime import datetime
from inspect import Parameter, signature, iscoroutinefunction
import logging
from typing import Any, AsyncIterator, Callable
from littlehorse.config import LHConfig
from littlehorse.exceptions import (
    TaskSchemaMismatchException,
)
from littlehorse.model.common_enums_pb2 import TaskStatus
from littlehorse.model.object_id_pb2 import NodeRunId, TaskDefId, TaskRunId
from littlehorse.model.service_pb2 import (
    PollTaskRequest,
    RegisterTaskWorkerRequest,
    RegisterTaskWorkerResponse,
    ReportTaskRun,
    ScheduledTask,
)
from littlehorse.model.task_def_pb2 import TaskDef
from littlehorse.utils import parse_value, parse_type, extract_value, timestamp_now

REPORT_TASK_DEFAULT_RETRIES = 5
HEARTBEAT_DEFAULT_INTERVAL = 5


class LHWorkerContext:
    def __init__(self, scheduled_task: ScheduledTask) -> None:
        self._scheduled_task = scheduled_task
        self._log_entries: list[str] = []

    @property
    def scheduled_time(self) -> datetime:
        """Returns the time at which the task was scheduled
        by the processor. May be useful in certain customer
        edge cases, eg. to determine whether it's too
        late to actually perform an action, when
        (datetime.now() - ctx.scheduled_time()) is
        above some threshold, etc.

        Returns:
            datetime: The time at which the current NodeRun was scheduled.
        """
        return datetime.fromtimestamp(float(self._scheduled_task.created_at.seconds))

    @property
    def task_run_id(self) -> TaskRunId:
        """Task Run Id.

        Returns:
            TaskRunId: the ID of the associated TaskRun.
        """
        return self._scheduled_task.task_run_id

    @property
    def wf_run_id(self) -> str:
        """Get the associated workflow run id.

        Returns:
            str: Workflow run id.
        """
        return self._scheduled_task.task_run_id.wf_run_id

    @property
    def attempt_number(self) -> int:
        """Returns the attemptNumber of the NodeRun
        that's being executed. If this is the
        first attempt, returns zero. If this is the
        first retry, returns 1, and so on.

        Returns:
            int: The attempt number of the NodeRun that's being executed.
        """
        return self._scheduled_task.attempt_number

    @property
    def idempotency_key(self) -> str:
        """Returns an idempotency key that can be used to make calls t
        o upstream api's idempotent across TaskRun Retries.

        Returns:
            str: An idempotency key.
        """
        return self.task_run_id.task_guid

    @property
    def task_def_name(self) -> str:
        """Name of this task.

        Returns:
            str: Name.
        """
        return self._scheduled_task.task_def_id.name

    @property
    def node_run_id(self) -> NodeRunId:
        """Returns the NodeRun ID for the Task that was just scheduled.

        Returns:
            NodeRunId: A NodeRunId object.
        """
        source = self._scheduled_task.source
        return (
            source.task_node.node_run_id
            if source.WhichOneof("task_run_source") == "task_node"
            else source.user_task_trigger.node_run_id
        )

    def log(self, entry: Any) -> None:
        """Provides a way to push data into the log output. Any object may be passed in;
        its String representation will be appended to the logOutput of this NodeRun.

           Args:
               entry (str): Message to log to the NodeRun's logOutput.
        """
        self._log_entries.append(f"[{datetime.now()}] {entry}")

    @property
    def log_output(self) -> str:
        """Returns the current log output.

        Returns:
            str: Log output.
        """
        return "\n".join(self._log_entries)

    def __str__(self) -> str:
        return str(
            {
                "wf_run_id": self.wf_run_id,
                "idempotency_key": self.idempotency_key,
                "task_def_name": self.task_def_name,
                "scheduled_time": str(self.scheduled_time),
                "attempt_number": self.attempt_number,
            }
        )


class LHTask:
    def __init__(self, callable: Callable[..., Any], task_def: TaskDef) -> None:
        self.task_def = task_def

        self._callable = callable
        self._signature = signature(callable)

        self._validate_callable()
        self._validate_match()

    def _validate_match(self) -> None:
        task_def_vars = [parse_type(var.type) for var in self.task_def.input_vars]

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

    @property
    def task_name(self) -> str:
        return self.task_def.name

    def has_context(self) -> bool:
        last_parameter = list(self._signature.parameters.values())[-1]
        return last_parameter.annotation is LHWorkerContext


class LHConnection:
    _log = logging.getLogger("LHConnection")

    def __init__(self, server: str, config: LHConfig, task: LHTask) -> None:
        self.server = server
        self.running = False
        self._task = task
        self._config = config
        self._ask_for_work_semaphore = asyncio.Semaphore()
        self._schedule_task_semaphore = asyncio.Semaphore(config.num_worker_threads)
        self._stub = self._config.stub(
            server=self.server, async_channel=True, name=self._task.task_name
        )

    async def _schedule_task(self, task: ScheduledTask) -> None:
        self._log.debug(
            "Scheduling task '%s' for WfRun '%s'",
            task.task_def_id.name,
            task.task_run_id.wf_run_id,
        )
        await self._schedule_task_semaphore.acquire()
        asyncio.create_task(self._execute_task(task))

    async def _execute_task(self, task: ScheduledTask) -> None:
        context = LHWorkerContext(task)
        args: Any = [extract_value(var.value) for var in task.variables]

        if self._task.has_context():
            args.append(context)

        try:
            output = parse_value(await self._task._callable(*args))
            status = TaskStatus.TASK_SUCCESS
        except TypeError as te:
            output = None
            context.log(te)
            status = TaskStatus.TASK_OUTPUT_SERIALIZING_ERROR
        except BaseException as be:
            output = None
            context.log(be)
            status = TaskStatus.TASK_FAILED

        self._schedule_task_semaphore.release()

        task_result = ReportTaskRun(
            task_run_id=task.task_run_id,
            time=timestamp_now(),
            attempt_number=task.attempt_number,
            status=status,
            output=output,
            log_output=parse_value(context.log_output) if context.log_output else None,
        )

        asyncio.create_task(self._report_task(task_result, REPORT_TASK_DEFAULT_RETRIES))

    async def _report_task(self, task_result: ReportTaskRun, retries_left: int) -> None:
        if retries_left <= 0:
            self._log.error(
                "Retries exhausted when reporting task: '%s'",
                task_result.task_run_id,
            )
            return

        self._log.debug(
            "Reporting task '%s'",
            self._task.task_name,
        )

        try:
            await self._stub.ReportTask(task_result)
            self._log.debug("Task '%s' successfully reported", self._task.task_name)
        except Exception as e:
            retries_left -= 1
            self._log.warning(
                "Error reporting task: '%s'. Retrying [%s]. %s",
                self._task.task_name,
                retries_left,
                e,
            )
            await asyncio.sleep(1.5)
            await self._report_task(task_result, retries_left)

    async def _ask_for_work(self) -> None:
        async def generator() -> AsyncIterator[PollTaskRequest]:
            while self.running:
                await self._ask_for_work_semaphore.acquire()
                if self.running:
                    yield PollTaskRequest(
                        client_id=self._config.client_id,
                        task_worker_version=self._config.worker_version,
                        task_def_name=self._task.task_name,
                    )
                    self._log.debug(
                        "Connection '%s' is asking for work '%s' '%s'",
                        self.server,
                        self._task.task_name,
                        datetime.now(),
                    )

        try:
            async for reply in self._stub.PollTask(generator()):
                if reply.HasField("result"):
                    await self._schedule_task(reply.result)
                else:
                    self._log.warning(
                        "Didn't successfully claim task,"
                        "likely due to server ('%s') restart.",
                        self.server,
                    )
                    await asyncio.sleep(HEARTBEAT_DEFAULT_INTERVAL)
                self._ask_for_work_semaphore.release()
        except Exception as e:
            self._log.error(
                "Api Connection Error at '%s', stopping: %s", self.server, e
            )
            self.stop()

    async def start(self) -> None:
        self._log.info(
            f"Starting server connection {self.server} "
            f"for task '{self._task.task_name}'"
        )
        self.running = True
        await self._ask_for_work()

    def stop(self) -> None:
        self._log.info(
            f"Stopping server connection {self.server} "
            f"for task '{self._task.task_name}'"
        )
        self.running = False
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
        self.running = False

        # get the task definition from the server
        stub = config.stub()
        reply: TaskDef = stub.GetTaskDef(TaskDefId(name=task_def_name))
        self._task = LHTask(callable, reply)

    async def _heartbeat(self) -> None:
        stub = self._config.stub(async_channel=True, name="heartbeat")

        while self.running:
            self._log.debug(
                "Sending heart beat (%s) at %s",
                self._task.task_name,
                datetime.now(),
            )

            request = RegisterTaskWorkerRequest(
                client_id=self._config.client_id,
                listener_name=self._config.server_listener,
                task_def_name=self._task.task_name,
            )
            try:
                reply: RegisterTaskWorkerResponse = await stub.RegisterTaskWorker(
                    request
                )
            except Exception as e:
                self._log.error(
                    "Error when registering task worker: %s. Closing. %s",
                    self._task.task_name,
                    e,
                )
                await asyncio.sleep(HEARTBEAT_DEFAULT_INTERVAL)
                continue

            hosts = [f"{host.host}:{host.port}" for host in reply.your_hosts]

            # remove invalid connections
            hosts_to_be_removed = {
                host for host in self._connections.keys() if host not in hosts
            }

            if hosts_to_be_removed:
                self._log.info("Connections to be removed: %s", hosts_to_be_removed)

            for host in hosts_to_be_removed:
                connection_to_be_removed = self._connections.pop(host)
                connection_to_be_removed.stop()

            # removing deads
            dead_connections = {
                host
                for host, connection in self._connections.items()
                if not connection.running
            }

            if dead_connections:
                self._log.info("Dead connections: %s", dead_connections)

            for host in dead_connections:
                connection_to_be_removed = self._connections.pop(host)
                connection_to_be_removed.stop()

            # add new connections
            hosts_to_be_added = [
                host for host in hosts if host not in self._connections.keys()
            ]

            if hosts_to_be_added:
                self._log.info("Connections to be added: %s", hosts_to_be_added)

            for host in hosts_to_be_added:
                new_connection = LHConnection(host, self._config, self._task)
                self._connections[host] = new_connection
                asyncio.create_task(new_connection.start())

            await asyncio.sleep(HEARTBEAT_DEFAULT_INTERVAL)

    async def start(self) -> None:
        """Starts polling for and executing tasks."""
        self._log.info(f"Starting worker '{self._task.task_name}'")
        self.running = True

        await self._heartbeat()

    def stop(self) -> None:
        """Cleanly shuts down the Task Worker."""
        self._log.info(f"Stopping worker '{self._task.task_name}'")
        self.running = False

        for connection in self._connections.values():
            connection.stop()
