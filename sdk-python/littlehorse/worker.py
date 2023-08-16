import asyncio
from datetime import datetime
from inspect import Parameter, signature
import logging
from typing import Any, Callable, Optional
from littlehorse.config import LHConfig
from littlehorse.exceptions import (
    InvalidTaskDefNameException,
    TaskSchemaMismatchException,
)
from littlehorse.model.service_pb2 import (
    GetTaskDefReplyPb,
    LHResponseCodePb,
    RegisterTaskWorkerPb,
    TaskDefIdPb,
    TaskDefPb,
)
from littlehorse.model.service_pb2_grpc import LHPublicApiStub


class LHWorkerContext:
    pass


class LHTask:
    def __init__(
        self, callable_task: Callable[..., Any], task_def: TaskDefPb
    ) -> None:
        self.task_def = task_def
        self._callable = callable_task

        # create the signature and validate it is a callable
        self._signature = signature(callable_task)

        # validate callable
        self._validate_callable()

        # validate match
        self._validate_match()

    def _validate_match(self) -> None:
        self.task_def.input_vars

    def _validate_callable(self) -> None:
        def filter(filer: Callable[[Parameter], bool]) -> list[Parameter]:
            return [
                param for param in self._signature.parameters.values() if filer(param)
            ]

        def map(parameters: list[Parameter]) -> list[str]:
            return [param.name for param in parameters]

        # validate *args
        filtered_params = filter(lambda param: param.kind is Parameter.VAR_POSITIONAL)
        if len(filtered_params) > 0:
            raise TaskSchemaMismatchException(
                f"Positional parameters (*args) not allowed: {map(filtered_params)}"
            )

        # validate *kwargs
        filtered_params = filter(lambda param: param.kind is Parameter.VAR_KEYWORD)
        if len(filtered_params) > 0:
            raise TaskSchemaMismatchException(
                f"Keyword parameters (*kwargs) not allowed: {map(filtered_params)}"
            )

        # validate not annotated
        filtered_params = filter(lambda param: param.annotation is Parameter.empty)
        if len(filtered_params) > 0:
            raise TaskSchemaMismatchException(
                f"Not annotated parameters found: {map(filtered_params)}"
            )

        # validate any
        filtered_params = filter(lambda param: param.annotation is Any)
        if len(filtered_params) > 0:
            raise TaskSchemaMismatchException(
                f"Any is not allowed: {map(filtered_params)}"
            )

        # validate context
        filtered_params = filter(lambda param: param.annotation is LHWorkerContext)
        if len(filtered_params) > 1:
            raise TaskSchemaMismatchException(
                f"Too many context arguments (expected 1): {map(filtered_params)}"
            )

    def name(self) -> str:
        return self.task_def.name

    def has_context(self) -> bool:
        ctx_parameters = [
            param
            for param in self._signature.parameters.values()
            if param.annotation is LHWorkerContext
        ]

        return bool(ctx_parameters)


class LHTaskWorker:
    _log = logging.getLogger("LHTaskWorker")

    def __init__(
        self, callable: Callable[..., Any], task_def_name: str, config: LHConfig
    ) -> None:
        self.callable = callable
        self.task_def_name = task_def_name
        self.config = config
        self.running = False

        self._heartbeat_task: Optional[asyncio.Task[None]] = None

        self._is_valid()

    async def _heartbeat(self) -> None:
        async with self.config.establish_channel(async_channel=True) as channel:
            stub = LHPublicApiStub(channel)
            while self.running:
                self._log.debug("Sending heart beat at %s", datetime.now())

                request = RegisterTaskWorkerPb(
                    client_id=self.config.client_id(),
                    listener_name=self.config.server_listener(),
                    task_def_name=self.task_def_name,
                )
                await stub.RegisterTaskWorker(request)
                # HERE: CHECK THE OPENED CONNECTIONS,
                # DEPENDING ON "YOUR HOSTS" KEEP THEM OR KILL THEM
                await asyncio.sleep(5)

    async def start(self) -> None:
        self.running = True
        self._heartbeat_task = asyncio.create_task(self._heartbeat())

    def stop(self) -> None:
        self._log.debug("Stopping worker")

        if self._heartbeat_task is None or not self.running:
            return

        self._log.debug("Cancelling heartbeat task")
        self._heartbeat_task.cancel()
        self.running = False

    def _is_valid(self) -> None:
        with self.config.establish_channel() as channel:
            stub = LHPublicApiStub(channel)
            reply: GetTaskDefReplyPb = stub.GetTaskDef(
                TaskDefIdPb(name=self.task_def_name)
            )

        if reply.code is not LHResponseCodePb.OK:
            raise InvalidTaskDefNameException(
                f"Couldn't find TaskDef: {self.task_def_name}"
            )

        task_def = reply.result
        print(len(task_def.input_vars))


if __name__ == "__main__":
    from pathlib import Path

    logging.basicConfig(level=logging.DEBUG)

    config_path = Path.home().joinpath(".config", "littlehorse.config")

    def greeting(name: str, ctx2: LHWorkerContext) -> str:
        greeting = f"Hello {name}!"
        print(greeting)
        return greeting

    config = LHConfig()
    config.load(config_path)
    worker = LHTaskWorker(greeting, "greet", config)

    loop = asyncio.get_event_loop()

    async def wait_until_stop() -> None:  # this is not needed in production
        await asyncio.sleep(10)
        worker.stop()
        loop.stop()

    try:
        loop.create_task(worker.start())
        loop.create_task(wait_until_stop())  # this is not needed in production
        loop.run_forever()
    except KeyboardInterrupt:
        pass
