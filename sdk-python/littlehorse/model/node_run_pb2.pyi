from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class NodeRun(_message.Message):
    __slots__ = ["wf_run_id", "thread_run_number", "position", "status", "arrival_time", "end_time", "wf_spec_id", "thread_spec_name", "node_name", "error_message", "failures", "task", "external_event", "entrypoint", "exit", "start_thread", "wait_threads", "sleep", "user_task", "start_multiple_threads", "failure_handler_ids"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    POSITION_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    ARRIVAL_TIME_FIELD_NUMBER: _ClassVar[int]
    END_TIME_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    NODE_NAME_FIELD_NUMBER: _ClassVar[int]
    ERROR_MESSAGE_FIELD_NUMBER: _ClassVar[int]
    FAILURES_FIELD_NUMBER: _ClassVar[int]
    TASK_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_FIELD_NUMBER: _ClassVar[int]
    ENTRYPOINT_FIELD_NUMBER: _ClassVar[int]
    EXIT_FIELD_NUMBER: _ClassVar[int]
    START_THREAD_FIELD_NUMBER: _ClassVar[int]
    WAIT_THREADS_FIELD_NUMBER: _ClassVar[int]
    SLEEP_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_FIELD_NUMBER: _ClassVar[int]
    START_MULTIPLE_THREADS_FIELD_NUMBER: _ClassVar[int]
    FAILURE_HANDLER_IDS_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    thread_run_number: int
    position: int
    status: _common_enums_pb2.LHStatus
    arrival_time: _timestamp_pb2.Timestamp
    end_time: _timestamp_pb2.Timestamp
    wf_spec_id: _object_id_pb2.WfSpecId
    thread_spec_name: str
    node_name: str
    error_message: str
    failures: _containers.RepeatedCompositeFieldContainer[Failure]
    task: TaskNodeRun
    external_event: ExternalEventRun
    entrypoint: EntrypointRun
    exit: ExitRun
    start_thread: StartThreadRun
    wait_threads: WaitForThreadsRun
    sleep: SleepNodeRun
    user_task: UserTaskNodeRun
    start_multiple_threads: StartMultipleThreadsRun
    failure_handler_ids: _containers.RepeatedScalarFieldContainer[int]
    def __init__(self, wf_run_id: _Optional[str] = ..., thread_run_number: _Optional[int] = ..., position: _Optional[int] = ..., status: _Optional[_Union[_common_enums_pb2.LHStatus, str]] = ..., arrival_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., end_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., wf_spec_id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., thread_spec_name: _Optional[str] = ..., node_name: _Optional[str] = ..., error_message: _Optional[str] = ..., failures: _Optional[_Iterable[_Union[Failure, _Mapping]]] = ..., task: _Optional[_Union[TaskNodeRun, _Mapping]] = ..., external_event: _Optional[_Union[ExternalEventRun, _Mapping]] = ..., entrypoint: _Optional[_Union[EntrypointRun, _Mapping]] = ..., exit: _Optional[_Union[ExitRun, _Mapping]] = ..., start_thread: _Optional[_Union[StartThreadRun, _Mapping]] = ..., wait_threads: _Optional[_Union[WaitForThreadsRun, _Mapping]] = ..., sleep: _Optional[_Union[SleepNodeRun, _Mapping]] = ..., user_task: _Optional[_Union[UserTaskNodeRun, _Mapping]] = ..., start_multiple_threads: _Optional[_Union[StartMultipleThreadsRun, _Mapping]] = ..., failure_handler_ids: _Optional[_Iterable[int]] = ...) -> None: ...

class TaskNodeRun(_message.Message):
    __slots__ = ["task_run_id"]
    TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    task_run_id: _object_id_pb2.TaskRunId
    def __init__(self, task_run_id: _Optional[_Union[_object_id_pb2.TaskRunId, _Mapping]] = ...) -> None: ...

class UserTaskNodeRun(_message.Message):
    __slots__ = ["user_task_run_id"]
    USER_TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    user_task_run_id: _object_id_pb2.UserTaskRunId
    def __init__(self, user_task_run_id: _Optional[_Union[_object_id_pb2.UserTaskRunId, _Mapping]] = ...) -> None: ...

class EntrypointRun(_message.Message):
    __slots__ = []
    def __init__(self) -> None: ...

class ExitRun(_message.Message):
    __slots__ = []
    def __init__(self) -> None: ...

class StartThreadRun(_message.Message):
    __slots__ = ["child_thread_id", "thread_spec_name"]
    CHILD_THREAD_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    child_thread_id: int
    thread_spec_name: str
    def __init__(self, child_thread_id: _Optional[int] = ..., thread_spec_name: _Optional[str] = ...) -> None: ...

class StartMultipleThreadsRun(_message.Message):
    __slots__ = ["thread_spec_name"]
    THREAD_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    thread_spec_name: str
    def __init__(self, thread_spec_name: _Optional[str] = ...) -> None: ...

class WaitForThreadsRun(_message.Message):
    __slots__ = ["threads", "policy"]
    class WaitForThread(_message.Message):
        __slots__ = ["thread_end_time", "thread_status", "thread_run_number", "already_handled"]
        THREAD_END_TIME_FIELD_NUMBER: _ClassVar[int]
        THREAD_STATUS_FIELD_NUMBER: _ClassVar[int]
        THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
        ALREADY_HANDLED_FIELD_NUMBER: _ClassVar[int]
        thread_end_time: _timestamp_pb2.Timestamp
        thread_status: _common_enums_pb2.LHStatus
        thread_run_number: int
        already_handled: bool
        def __init__(self, thread_end_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., thread_status: _Optional[_Union[_common_enums_pb2.LHStatus, str]] = ..., thread_run_number: _Optional[int] = ..., already_handled: bool = ...) -> None: ...
    THREADS_FIELD_NUMBER: _ClassVar[int]
    POLICY_FIELD_NUMBER: _ClassVar[int]
    threads: _containers.RepeatedCompositeFieldContainer[WaitForThreadsRun.WaitForThread]
    policy: _common_enums_pb2.WaitForThreadsPolicy
    def __init__(self, threads: _Optional[_Iterable[_Union[WaitForThreadsRun.WaitForThread, _Mapping]]] = ..., policy: _Optional[_Union[_common_enums_pb2.WaitForThreadsPolicy, str]] = ...) -> None: ...

class ExternalEventRun(_message.Message):
    __slots__ = ["external_event_def_name", "event_time", "external_event_id"]
    EXTERNAL_EVENT_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    EVENT_TIME_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_ID_FIELD_NUMBER: _ClassVar[int]
    external_event_def_name: str
    event_time: _timestamp_pb2.Timestamp
    external_event_id: _object_id_pb2.ExternalEventId
    def __init__(self, external_event_def_name: _Optional[str] = ..., event_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., external_event_id: _Optional[_Union[_object_id_pb2.ExternalEventId, _Mapping]] = ...) -> None: ...

class SleepNodeRun(_message.Message):
    __slots__ = ["maturation_time"]
    MATURATION_TIME_FIELD_NUMBER: _ClassVar[int]
    maturation_time: _timestamp_pb2.Timestamp
    def __init__(self, maturation_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class Failure(_message.Message):
    __slots__ = ["failure_name", "message", "content", "was_properly_handled"]
    FAILURE_NAME_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    WAS_PROPERLY_HANDLED_FIELD_NUMBER: _ClassVar[int]
    failure_name: str
    message: str
    content: _variable_pb2.VariableValue
    was_properly_handled: bool
    def __init__(self, failure_name: _Optional[str] = ..., message: _Optional[str] = ..., content: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., was_properly_handled: bool = ...) -> None: ...
