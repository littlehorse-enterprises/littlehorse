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
    pass


class LHTask:
    def __init__(self, callable: Callable[..., Any], task_def: TaskDefPb) -> None:
        self.task_def = task_def
        self._callable = callable

        # create the signature and validate it is a callable
        self._signature = signature(callable)

        # validate callable
        self._validate_callable()

        # validate match
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

        # validate context
        filtered_params = filter(lambda param: param.annotation is LHWorkerContext)
        if len(filtered_params) > 1:
            raise TaskSchemaMismatchException(
                f"Too many context arguments (expected 1): {names(filtered_params)}"
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
        self._config = config
        self._heartbeat_task: Optional[asyncio.Task[None]] = None

        # get the task definition from the server
        with self._config.establish_channel() as channel:
            stub = LHPublicApiStub(channel)
            reply: GetTaskDefReplyPb = stub.GetTaskDef(
                TaskDefIdPb(name=task_def_name)
            )

        if reply.code is not LHResponseCodePb.OK:
            raise InvalidTaskDefNameException(
                f"Couldn't find TaskDef: {task_def_name}"
            )

        # initialize internal task and attributes
        self.task = LHTask(callable, reply.result)
        self.running = False

    async def _heartbeat(self) -> None:
        async with self._config.establish_channel(async_channel=True) as channel:
            stub = LHPublicApiStub(channel)
            while self.running:
                self._log.debug("Sending heart beat at %s", datetime.now())

                request = RegisterTaskWorkerPb(
                    client_id=self._config.client_id(),
                    listener_name=self._config.server_listener(),
                    task_def_name=self.task.name(),
                )
                await stub.RegisterTaskWorker(request)
                # HERE: CHECK THE OPENED CONNECTIONS,
                # DEPENDING ON "YOUR HOSTS" KEEP THEM OR KILL THEM
                await asyncio.sleep(5)

    async def start(self) -> None:
        self.running = True
        self._heartbeat_task = asyncio.create_task(self._heartbeat())

    def stop(self) -> None:
        self._log.info("Stopping worker")
        self.running = False

        if self._heartbeat_task is not None and not self._heartbeat_task.cancelled():
            self._heartbeat_task.cancel()
