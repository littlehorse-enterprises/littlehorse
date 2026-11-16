import inspect
from enum import Enum
import asyncio
from datetime import datetime
import functools
from types import FrameType
from inspect import Parameter, signature, iscoroutinefunction
import logging
import math
import signal
import time
import traceback
from google.protobuf.json_format import MessageToJson
from typing import (
    Any,
    AsyncIterator,
    Callable,
    Optional,
    Annotated,
    get_args,
    get_origin,
    get_type_hints,
)
from littlehorse.config import LHConfig
from littlehorse.exceptions import (
    TaskSchemaMismatchException,
    TaskResultReportingException,
    LHTaskException as LHTaskPythonException,
)
from littlehorse.model import (
    LHErrorType,
    TaskStatus,
    VariableDef,
    NodeRunId,
    TaskDefId,
    TaskRunId,
    WfRunId,
    PollTaskRequest,
    PutTaskDefRequest,
    RegisterTaskWorkerRequest,
    RegisterTaskWorkerResponse,
    ReportTaskRun,
    ReturnType,
    ScheduledTask,
    TaskDef,
    TypeDefinition,
    StructDefId,
    LHTaskError,
    LHTaskException,
    VariableValue,
    CheckpointId,
    Checkpoint,
    PutCheckpointRequest,
    PutCheckpointResponse,
    LittleHorseStub,
)
from google.protobuf.timestamp_pb2 import Timestamp
from littlehorse.utils import extract_value, to_variable_type, to_variable_value
from littlehorse.utils import to_type

REPORT_TASK_RETRIES_INTERVAL_SECONDS = 2
REPORT_TASK_FAIL_RETRIES = 15
HEARTBEAT_INTERVAL_SECONDS = 60
INITIAL_REGISTRATION_RETRY_SECONDS = 1
POLL_TASK_INTERVAL_SECONDS = 5
GRPC_UNARY_CALL_TIMEOUT_SECONDS = 30

logger = logging.getLogger(__name__)


def _resolved_signature(func: Callable[..., Any]) -> inspect.Signature:
    """Return the signature of *func* with string annotations resolved.

    When ``from __future__ import annotations`` is used, all annotations
    are stored as strings.  ``get_type_hints`` evaluates them back into
    real types so that downstream code can do ``isinstance`` checks.

    Only *string* annotations are replaced so that already-resolved type
    objects keep their identity (important for ``is WorkerContext`` checks
    that vary across Python versions).
    """
    sig = signature(func)
    try:
        hints = get_type_hints(func, include_extras=True)
    except Exception:
        return sig
    new_params = []
    for name, param in sig.parameters.items():
        if name in hints and isinstance(param.annotation, str):
            new_params.append(param.replace(annotation=hints[name]))
        else:
            new_params.append(param)
    return_annotation = sig.return_annotation
    if isinstance(sig.return_annotation, str) and "return" in hints:
        return_annotation = hints["return"]
    return sig.replace(parameters=new_params, return_annotation=return_annotation)


class WorkerContext:
    def __init__(self, scheduled_task: ScheduledTask, client: LittleHorseStub) -> None:
        self._scheduled_task = scheduled_task
        self._log_entries: list[str] = []
        self._checkpoints_so_far_in_this_run = 0
        self._client = client

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
    def wf_run_id(self) -> WfRunId:
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

    async def execute_and_checkpoint(self, callable: Callable[..., Any]) -> Any:
        """Execute a sync or async callback, or return its saved result on replay.

        The callback receives a CheckpointContext. Awaitable results are awaited
        before persisting the value and checkpoint logs. Exceptions and cancellation
        propagate without saving an incomplete result. Work may run again if it
        completes but the checkpoint is not persisted.
        """
        if (
            self._checkpoints_so_far_in_this_run
            < self._scheduled_task.total_observed_checkpoints
        ):
            output = await self._fetch_checkpoint(self._checkpoints_so_far_in_this_run)
            self._checkpoints_so_far_in_this_run += 1
            return output
        else:
            return await self._save_checkpoint(callable)

    async def _fetch_checkpoint(self, checkpoint_number: int) -> Any:
        id: CheckpointId = CheckpointId(
            task_run=self._scheduled_task.task_run_id,
            checkpoint_number=checkpoint_number,
        )

        checkpoint: Checkpoint = await self._client.GetCheckpoint(id)

        return extract_value(checkpoint.value)

    async def _save_checkpoint(self, callable: Callable[..., Any]) -> Any:
        checkpoint_context: CheckpointContext = CheckpointContext()
        result: Any = callable(checkpoint_context)
        if inspect.isawaitable(result):
            result = await result

        response: PutCheckpointResponse = await self._client.PutCheckpoint(
            PutCheckpointRequest(
                task_run_id=self._scheduled_task.task_run_id,
                task_attempt=self._scheduled_task.attempt_number,
                value=to_variable_value(result),
                logs=checkpoint_context.log_output,
            )
        )

        self._checkpoints_so_far_in_this_run += 1

        if (
            response.flow_control_continue_type
            != PutCheckpointResponse.FlowControlContinue.CONTINUE_TASK
        ):
            raise Exception("Halting execution because the server told us to.")

        return result

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


class CheckpointContext:
    def __init__(self) -> None:
        self._log_entries: list[str] = []

    def log(self, obj: Any) -> None:
        """Stores log data for the given Checkpoint.

        Args:
            obj (Any): The item to add to the Checkpoint's Log Output
        """
        self._log_entries.append(f"[{datetime.now()}] {obj}")

    @property
    def log_output(self) -> str:
        """Returns the current log output.

        Returns:
            str: Log output.
        """
        return "\n".join(self._log_entries)


class LHTask:
    def __init__(self, callable: Callable[..., Any], task_def: TaskDef) -> None:
        self.task_def = task_def

        self._callable = callable
        self._signature = _resolved_signature(callable)

        self._validate_callable()
        self._validate_match()

    def _validate_match(self) -> None:
        task_def_input_vars = self.task_def.input_vars

        callable_params = [
            param
            for param in self._signature.parameters.values()
            if param.annotation is not WorkerContext
        ]

        if len(task_def_input_vars) != len(callable_params):
            task_def_types = [var.type_def for var in task_def_input_vars]
            raise TaskSchemaMismatchException(
                f"Incorrect parameter list, expected: {task_def_types}"
            )

        from littlehorse.lh_struct import is_lh_struct

        for task_def_var, callable_param in zip(task_def_input_vars, callable_params):
            anno = callable_param.annotation
            if get_origin(anno) is Annotated:
                anno = get_args(anno)[0]

            # Struct parameter
            if task_def_var.type_def.HasField("struct_def_id"):
                if not (isinstance(anno, type) and is_lh_struct(anno)):
                    raise TaskSchemaMismatchException(
                        f"Parameter '{callable_param.name}' expected a @lh_struct_def class "
                        f"for struct '{task_def_var.type_def.struct_def_id.name}'"
                    )
                continue

            # Primitive parameter
            expected_type = to_type(task_def_var.type_def.primitive_type)
            if expected_type != anno:
                raise TaskSchemaMismatchException(
                    f"Parameter types do not match for '{callable_param.name}', "
                    f"expected: {expected_type} got: {anno}"
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
        filtered_params = filter(lambda param: param.annotation is WorkerContext)
        if len(filtered_params) > 1:
            raise TaskSchemaMismatchException(
                f"Too many context arguments (expected 1): {names(filtered_params)}"
            )

        # validate context is the last one
        if len(filtered_params) > 0:
            last_parameter = list(self._signature.parameters.values())[-1]

            if last_parameter.annotation is not WorkerContext:
                raise TaskSchemaMismatchException(
                    "The WorkerContext should be the last parameter"
                )

    @property
    def task_name(self) -> str:
        return self.task_def.id.name

    def has_context(self) -> bool:
        parameters = list(self._signature.parameters.values())
        if len(parameters) == 0:
            return False
        last_parameter = parameters[-1]
        return last_parameter.annotation is WorkerContext


class LHConnection:
    _log = logging.getLogger("LHConnection")

    def __init__(self, server: str, config: LHConfig, task: LHTask) -> None:
        self.server = server
        self.running = False
        self._task = task
        self._config = config
        self._ask_for_work_semaphore = asyncio.Semaphore()
        self._schedule_task_semaphore = asyncio.Semaphore(config.num_worker_threads)
        self._stopped = False
        self._poll_task: Optional[asyncio.Task[None]] = None
        self._tasks: set[asyncio.Task[None]] = set()
        self._failure: Optional[BaseException] = None
        self._stub = self._config.stub(
            server=self.server, async_channel=True, name=self._task.task_name
        )

    async def _schedule_task(self, task: ScheduledTask) -> None:
        self._log.debug(
            "Scheduling task '%s' for WfRun '%s'",
            task.task_def_id.name,
            task.task_run_id.wf_run_id.id,
        )
        started = asyncio.Event()
        execution = asyncio.create_task(self._execute_task(task, started))
        # Own the result before the first suspension, including capacity waits.
        self._tasks.add(execution)
        execution.add_done_callback(self._task_done)
        await started.wait()

    def _task_done(self, task: asyncio.Task[None]) -> None:
        self._tasks.discard(task)
        failure = (
            asyncio.CancelledError("Accepted TaskRun was cancelled")
            if task.cancelled()
            else task.exception()
        )
        if failure is not None:
            logger.error("Accepted TaskRun did not finish reporting", exc_info=failure)
            if self._failure is None:
                self._failure = failure

    async def _execute_task(self, task: ScheduledTask, started: asyncio.Event) -> None:
        async with self._schedule_task_semaphore:
            started.set()
            task_result = await self._run_task(task)
        await self._report_task(task_result, REPORT_TASK_FAIL_RETRIES)

    async def _run_task(self, task: ScheduledTask) -> ReportTaskRun:
        context = WorkerContext(task, self._stub)
        output: Optional[VariableValue] = None
        task_error: Optional[LHTaskError] = None
        task_exception: Optional[LHTaskException] = None
        status: TaskStatus

        try:
            # Input failures must also release capacity and produce a result.
            from littlehorse.lh_struct import is_lh_struct, deserialize_struct

            params = [
                p
                for p in self._task._signature.parameters.values()
                if p.annotation is not WorkerContext
            ]
            args: list[Any] = []
            for i, var in enumerate(task.variables):
                val = var.value
                if i < len(params):
                    anno = params[i].annotation
                    if get_origin(anno) is Annotated:
                        anno = get_args(anno)[0]
                    if (
                        val.WhichOneof("value") == "struct"
                        and isinstance(anno, type)
                        and is_lh_struct(anno)
                    ):
                        args.append(deserialize_struct(val.struct, anno))
                        continue
                args.append(extract_value(val))

            if self._task.has_context():
                args.append(context)

            raw_output = await self._task._callable(*args)
            try:
                output = to_variable_value(raw_output)
                status = TaskStatus.TASK_SUCCESS
            except TypeError:
                output = None
                stacktrace = traceback.format_exc()
                logging.error(stacktrace)
                status = TaskStatus.TASK_OUTPUT_SERDE_ERROR
                task_error = LHTaskError(
                    type=LHErrorType.VAR_SUB_ERROR,
                    message=f"Failed serializing output: {stacktrace}",
                )

        except LHTaskPythonException as exn:
            output = None
            stacktrace = traceback.format_exc()
            logging.error(stacktrace)
            status = TaskStatus.TASK_EXCEPTION
            task_exception = LHTaskException(
                name=exn.exception_name,
                message=exn.message,
                content=exn.content,
            )
        except asyncio.CancelledError:
            raise
        except BaseException:
            output = None
            stacktrace = traceback.format_exc()
            logging.error(stacktrace)
            status = TaskStatus.TASK_FAILED
            task_error = LHTaskError(
                type=LHErrorType.TASK_ERROR,
                message=stacktrace,
            )

        current_time = Timestamp()
        current_time.GetCurrentTime()

        return ReportTaskRun(
            task_run_id=task.task_run_id,
            time=current_time,
            attempt_number=task.attempt_number,
            status=status,
            output=output,
            error=task_error,
            exception=task_exception,
            log_output=(
                to_variable_value(context.log_output) if context.log_output else None
            ),
        )

    async def _report_task(self, task_result: ReportTaskRun, retries_left: int) -> None:
        if retries_left <= 0:
            self._log.error(
                "Retries exhausted when reporting task %s, and workflow %s",
                task_result.task_run_id.task_guid,
                task_result.task_run_id.wf_run_id.id,
            )
            raise TaskResultReportingException(
                f"Reporting retries exhausted for TaskRun "
                f"{task_result.task_run_id.task_guid} in workflow "
                f"{task_result.task_run_id.wf_run_id.id}"
            )

        self._log.debug(
            "Reporting task '%s'",
            self._task.task_name,
        )

        try:
            await self._stub.ReportTask(
                task_result, timeout=GRPC_UNARY_CALL_TIMEOUT_SECONDS
            )
            self._log.debug("Task '%s' successfully reported", self._task.task_name)
        except Exception as e:
            retries_left -= 1
            self._log.warning(
                "Error reporting task: '%s'. Retrying [%s]. %s",
                self._task.task_name,
                retries_left,
                e,
            )
            await asyncio.sleep(REPORT_TASK_RETRIES_INTERVAL_SECONDS)
            await self._report_task(task_result, retries_left)

    async def _ask_for_work(self) -> None:
        async def generator() -> AsyncIterator[PollTaskRequest]:
            while self.running:
                await self._ask_for_work_semaphore.acquire()
                if self.running:
                    yield PollTaskRequest(
                        client_id=self._config.task_worker_id,
                        task_worker_version=self._config.worker_version,
                        task_def_id=self._task.task_def.id,
                    )
                    self._log.debug(
                        "Connection '%s' is asking for work '%s' '%s'",
                        self.server,
                        self._task.task_name,
                        datetime.now(),
                    )

        try:
            async for reply in self._stub.PollTask(generator()):
                if not self.running:
                    break
                if reply.HasField("result"):
                    await self._schedule_task(reply.result)
                else:
                    self._log.warning(
                        "Didn't successfully claim task,"
                        "likely due to server ('%s') restart.",
                        self.server,
                    )
                    await asyncio.sleep(POLL_TASK_INTERVAL_SECONDS)
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
        if self._stopped:
            return
        self.running = True
        self._poll_task = asyncio.create_task(self._ask_for_work())
        try:
            poll_results = await asyncio.gather(self._poll_task, return_exceptions=True)
            if isinstance(poll_results[0], BaseException) and not self._stopped:
                raise poll_results[0]
            self.stop()
            if self._tasks:
                await asyncio.wait(self._tasks)
            if self._failure is not None:
                raise self._failure
        finally:
            self.stop()
            pending = [self._poll_task, *self._tasks]
            for task in pending:
                if not task.done():
                    task.cancel()
            await asyncio.gather(*pending, return_exceptions=True)

    def stop(self) -> None:
        if self._stopped:
            return
        self._log.info(
            f"Stopping server connection {self.server} "
            f"for task '{self._task.task_name}'"
        )
        self.running = False
        self._stopped = True
        self._ask_for_work_semaphore.release()
        if self._poll_task is not None and not self._poll_task.done():
            self._poll_task.cancel()


class TaskWorkerHealthReason(Enum):
    HEALTHY = "HEALTHY"
    UNHEALTHY = "UNHEALTHY"
    SERVER_REBALANCING = "SERVER_REBALANCING"


class LHTaskWorkerHealth:
    def __init__(self, healthy: bool, reason: TaskWorkerHealthReason) -> None:
        self.healthy = healthy
        self.reason = reason

    def __eq__(self, other: Any) -> bool:
        if isinstance(other, LHTaskWorkerHealth):
            return self.healthy == other.healthy and self.reason == other.reason
        return False


class LHLivenessController:
    def __init__(self) -> None:
        self._is_worker_healthy = True
        self._is_cluster_healthy = True
        self._running = True

    def notify_worker_failure(self) -> None:
        self._is_worker_healthy = False

    def notify_success_call(self, reply: RegisterTaskWorkerResponse) -> None:
        if reply.HasField("is_cluster_healthy"):
            self._is_cluster_healthy = reply.is_cluster_healthy
        else:
            self._is_cluster_healthy = True
        self._is_worker_healthy = True

    @property
    def is_worker_healthy(self) -> bool:
        return self._is_worker_healthy

    @property
    def is_cluster_healthy(self) -> bool:
        return self._is_cluster_healthy

    @property
    def keep_worker_running(self) -> bool:
        return self._running

    def stop(self) -> None:
        self._running = False

    def health(self) -> LHTaskWorkerHealth:
        if not self.is_cluster_healthy:
            return LHTaskWorkerHealth(False, TaskWorkerHealthReason.SERVER_REBALANCING)

        if not self.is_worker_healthy:
            return LHTaskWorkerHealth(False, TaskWorkerHealthReason.UNHEALTHY)

        return LHTaskWorkerHealth(True, TaskWorkerHealthReason.HEALTHY)


class LHTaskWorker:
    """The LHTaskWorker talks to the LH Servers and executes a
    specified Task Method every time a Task is scheduled.
    """

    _log = logging.getLogger("LHTaskWorker")

    def __init__(
        self,
        callable: Callable[..., Any],
        task_def_name: str,
        config: LHConfig,
        *,
        shutdown_timeout: float = 75.0,
    ) -> None:
        if config is None:
            raise ValueError("LHConfig cannot be None")

        if task_def_name is None:
            raise ValueError("TaskDefName cannot be None")

        if callable is None:
            raise ValueError("Callable cannot be None")

        if not math.isfinite(shutdown_timeout) or shutdown_timeout <= 0:
            raise ValueError("shutdown_timeout must be finite and greater than zero")

        self._config = config
        self._connections: dict[str, LHConnection] = {}
        self._connection_tasks: dict[asyncio.Task[None], LHConnection] = {}
        self._heartbeat_task: Optional[asyncio.Task[None]] = None
        self._failure: Optional[BaseException] = None
        self._shutdown_timeout = shutdown_timeout
        self._shutdown_deadline: Optional[float] = None
        self._liveness_controller = LHLivenessController()
        self._task_def_name = task_def_name
        self._callable = callable
        self._task: LHTask

    async def _heartbeat(self) -> None:
        stub = self._config.stub(async_channel=True, name="heartbeat")
        registered = False

        while self._liveness_controller.keep_worker_running:
            self._log.debug(
                "Sending heart beat for task %s at %s",
                self._task.task_name,
                datetime.now(),
            )

            request = RegisterTaskWorkerRequest(
                task_worker_id=self._config.task_worker_id,
                task_def_id=self._task.task_def.id,
            )
            try:
                reply: RegisterTaskWorkerResponse = await stub.RegisterTaskWorker(
                    request, timeout=GRPC_UNARY_CALL_TIMEOUT_SECONDS
                )
                self._log.debug(
                    "Heart beat received for task %s at %s",
                    self._task.task_name,
                    datetime.now(),
                )
            except Exception as e:
                self._log.error(
                    "Error when registering task worker: %s. %s",
                    self._task.task_name,
                    e,
                )
                self._liveness_controller.notify_worker_failure()
                await asyncio.sleep(
                    HEARTBEAT_INTERVAL_SECONDS
                    if registered
                    else INITIAL_REGISTRATION_RETRY_SECONDS
                )
                continue

            registered = True
            if not self.is_running:
                break
            self._liveness_controller.notify_success_call(reply)
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

            # removing dead connections
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
                connection_task = asyncio.create_task(new_connection.start())
                self._connection_tasks[connection_task] = new_connection
                connection_task.add_done_callback(self._connection_done)

            await asyncio.sleep(HEARTBEAT_INTERVAL_SECONDS)

    def health(self) -> LHTaskWorkerHealth:
        return self._liveness_controller.health()

    @property
    def is_running(self) -> bool:
        return self._liveness_controller.keep_worker_running

    async def start(self) -> None:
        """Run until stopped and all accepted executions and results are drained.

        Raises TimeoutError on drain expiry, TaskResultReportingException on
        exhausted reporting retries, and CancelledError if cancelled. Cancellation
        aborts owned work rather than completing a graceful shutdown.
        """
        self._log.info(f"Starting worker '{self._task_def_name}'")

        if not self.is_running:
            return
        try:
            stub = self._config.stub()
            reply: TaskDef = stub.GetTaskDef(
                TaskDefId(name=self._task_def_name),
                timeout=GRPC_UNARY_CALL_TIMEOUT_SECONDS,
            )
            self._task = LHTask(self._callable, reply)
            self._heartbeat_task = asyncio.create_task(self._heartbeat())
            heartbeat_results = await asyncio.gather(
                self._heartbeat_task, return_exceptions=True
            )
            heartbeat_failure = heartbeat_results[0]
            if isinstance(heartbeat_failure, BaseException) and (
                self.is_running
                or not isinstance(heartbeat_failure, asyncio.CancelledError)
            ):
                self._failure = heartbeat_failure
            self.stop()
            assert self._shutdown_deadline is not None
            try:
                await asyncio.wait_for(
                    self._drain_connections(),
                    max(0.0, self._shutdown_deadline - time.monotonic()),
                )
            except asyncio.TimeoutError as error:
                raise TimeoutError("Worker shutdown deadline expired") from error
            if time.monotonic() >= self._shutdown_deadline:
                raise TimeoutError("Worker shutdown deadline expired")
            if self._failure is not None:
                raise self._failure
        finally:
            self.stop()
            pending = list(self._connection_tasks)
            if self._heartbeat_task is not None:
                pending.append(self._heartbeat_task)
            for task in pending:
                if not task.done():
                    task.cancel()
            await asyncio.gather(*pending, return_exceptions=True)

    async def _drain_connections(self) -> None:
        if self._connection_tasks:
            await asyncio.wait(self._connection_tasks)

    def _connection_done(self, task: asyncio.Task[None]) -> None:
        connection = self._connection_tasks.pop(task)
        if self._connections.get(connection.server) is connection:
            self._connections.pop(connection.server)
        failure = (
            asyncio.CancelledError("Worker connection was cancelled")
            if task.cancelled()
            else task.exception()
        )
        if failure is not None:
            logger.error(
                "Worker connection did not drain successfully", exc_info=failure
            )
            if self._failure is None:
                self._failure = failure

    def stop(self) -> None:
        """Request shutdown synchronously; await start() for completion.

        The first request starts the shutdown_timeout deadline (default 75 seconds).
        Repeated requests do not restart the deadline or cancel accepted work.
        """
        if self._shutdown_deadline is not None:
            return
        self._shutdown_deadline = time.monotonic() + self._shutdown_timeout
        self._liveness_controller.stop()

        if self._heartbeat_task is not None and not self._heartbeat_task.done():
            self._heartbeat_task.cancel()
        for connection in self._connection_tasks.values():
            connection.stop()

        pending_tasks = sum(
            not task.done()
            for connection in self._connection_tasks.values()
            for task in connection._tasks
        )
        self._log.info(
            "Stopping worker '%s': waiting up to %s seconds for %d accepted tasks "
            "to complete execution and result reporting",
            self._task_def_name,
            self._shutdown_timeout,
            pending_tasks,
            extra={
                "task_def_name": self._task_def_name,
                "shutdown_timeout": self._shutdown_timeout,
                "pending_tasks": pending_tasks,
            },
        )

    def register_task_def(self) -> None:
        _create_task_def(self._callable, self._task_def_name, self._config)


class LHType:
    def __init__(
        self,
        name: str,
        masked: bool = False,
    ) -> None:
        self.name = name
        self.masked = masked


def _create_task_def(
    task: Callable[..., Any], name: str, config: LHConfig, timeout: Optional[int] = None
) -> None:
    stub = config.stub()
    task_signature = _resolved_signature(task)
    input_vars = [
        _to_variable_def(param)
        for param in task_signature.parameters.values()
        if param.annotation is not WorkerContext
    ]
    return_type = _return_to_lh_schema(task_signature.return_annotation)
    request = PutTaskDefRequest(
        name=name, input_vars=input_vars, return_type=return_type
    )
    stub.PutTaskDef(request, timeout=timeout)
    logging.info(f"TaskDef {name} was created:\n{MessageToJson(request)}")


def _to_variable_def(param: inspect.Parameter) -> VariableDef:
    from littlehorse.lh_struct import is_lh_struct, get_struct_def_name

    lh_type = _param_to_lh_type(param.annotation)
    if lh_type is None:
        lh_type = LHType(param.name)

    # Resolve the raw type (peel Annotated)
    raw_type = param.annotation
    if get_origin(raw_type) is Annotated:
        raw_type = get_args(raw_type)[0]

    # Check if it's a struct-decorated class
    if isinstance(raw_type, type) and is_lh_struct(raw_type):
        return VariableDef(
            name=lh_type.name,
            type_def=TypeDefinition(
                struct_def_id=StructDefId(
                    name=get_struct_def_name(raw_type),
                    version=-1,
                ),
                masked=lh_type.masked,
            ),
        )

    return VariableDef(
        name=lh_type.name,
        type_def=TypeDefinition(
            primitive_type=to_variable_type(param.annotation),
            masked=lh_type.masked,
        ),
    )


def _param_to_lh_type(annotated_type: type) -> Optional[LHType]:
    if get_origin(annotated_type) is Annotated:
        args = get_args(annotated_type)
        if len(args) > 1 and isinstance(args[1], LHType):
            return args[1]
    return None


def _return_to_lh_schema(return_type: type) -> Optional[ReturnType]:
    if return_type is None:
        return None

    from littlehorse.lh_struct import is_lh_struct, get_struct_def_name

    lh_type = _param_to_lh_type(return_type)

    # Resolve the raw type (peel Annotated)
    raw_return = return_type
    if get_origin(raw_return) is Annotated:
        raw_return = get_args(raw_return)[0]

    # Check if return type is a struct
    if isinstance(raw_return, type) and is_lh_struct(raw_return):
        return ReturnType(
            return_type=TypeDefinition(
                struct_def_id=StructDefId(
                    name=get_struct_def_name(raw_return),
                    version=-1,
                ),
                masked=lh_type.masked if lh_type else False,
            )
        )

    var = TypeDefinition(
        primitive_type=to_variable_type(return_type),
        masked=False,
    )
    if lh_type is not None:
        var = TypeDefinition(
            primitive_type=to_variable_type(return_type),
            masked=lh_type.masked,
        )
    return ReturnType(return_type=var)


def shutdown_hook(*workers: LHTaskWorker) -> None:
    """Add a shutdown hook for multiples workers"""

    def stop_workers(*workers: LHTaskWorker) -> None:
        for worker in workers:
            worker.stop()

    loop = asyncio.get_running_loop()

    for sig in (signal.SIGTERM, signal.SIGINT):
        stop_callback = functools.partial(stop_workers, *workers)
        try:
            loop.add_signal_handler(sig, stop_callback)
        except NotImplementedError:

            def _stop_handler(_sig: int, _frame: FrameType | None) -> None:
                stop_callback()

            try:
                signal.signal(sig, _stop_handler)
            except (ValueError, OSError):
                # Signal registration is unavailable in this runtime/thread.
                pass


async def start(*workers: LHTaskWorker, handle_signals: bool = True) -> None:
    """Run workers through shutdown, including acknowledged task results.

    By default SIGTERM/SIGINT request graceful shutdown. Set handle_signals=False
    to leave application handlers untouched and call worker.stop() yourself.
    """
    if handle_signals:
        shutdown_hook(*workers)
    tasks = [asyncio.create_task(worker.start()) for worker in workers]
    try:
        await asyncio.gather(*tasks)
    finally:
        for worker in workers:
            worker.stop()
        await asyncio.gather(*tasks, return_exceptions=True)
