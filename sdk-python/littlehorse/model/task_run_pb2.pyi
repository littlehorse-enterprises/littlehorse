from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.user_tasks_pb2 as _user_tasks_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class TaskRun(_message.Message):
    __slots__ = ["id", "attempts", "max_attempts", "task_def_name", "input_variables", "source", "scheduled_at", "status", "timeout_seconds"]
    ID_FIELD_NUMBER: _ClassVar[int]
    ATTEMPTS_FIELD_NUMBER: _ClassVar[int]
    MAX_ATTEMPTS_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    INPUT_VARIABLES_FIELD_NUMBER: _ClassVar[int]
    SOURCE_FIELD_NUMBER: _ClassVar[int]
    SCHEDULED_AT_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    TIMEOUT_SECONDS_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.TaskRunId
    attempts: _containers.RepeatedCompositeFieldContainer[TaskAttempt]
    max_attempts: int
    task_def_name: str
    input_variables: _containers.RepeatedCompositeFieldContainer[VarNameAndVal]
    source: TaskRunSource
    scheduled_at: _timestamp_pb2.Timestamp
    status: _common_enums_pb2.TaskStatus
    timeout_seconds: int
    def __init__(self, id: _Optional[_Union[_object_id_pb2.TaskRunId, _Mapping]] = ..., attempts: _Optional[_Iterable[_Union[TaskAttempt, _Mapping]]] = ..., max_attempts: _Optional[int] = ..., task_def_name: _Optional[str] = ..., input_variables: _Optional[_Iterable[_Union[VarNameAndVal, _Mapping]]] = ..., source: _Optional[_Union[TaskRunSource, _Mapping]] = ..., scheduled_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., status: _Optional[_Union[_common_enums_pb2.TaskStatus, str]] = ..., timeout_seconds: _Optional[int] = ...) -> None: ...

class VarNameAndVal(_message.Message):
    __slots__ = ["var_name", "value"]
    VAR_NAME_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    var_name: str
    value: _variable_pb2.VariableValue
    def __init__(self, var_name: _Optional[str] = ..., value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ...) -> None: ...

class TaskAttempt(_message.Message):
    __slots__ = ["output", "log_output", "schedule_time", "start_time", "end_time", "task_worker_id", "task_worker_version", "status"]
    OUTPUT_FIELD_NUMBER: _ClassVar[int]
    LOG_OUTPUT_FIELD_NUMBER: _ClassVar[int]
    SCHEDULE_TIME_FIELD_NUMBER: _ClassVar[int]
    START_TIME_FIELD_NUMBER: _ClassVar[int]
    END_TIME_FIELD_NUMBER: _ClassVar[int]
    TASK_WORKER_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_WORKER_VERSION_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    output: _variable_pb2.VariableValue
    log_output: _variable_pb2.VariableValue
    schedule_time: _timestamp_pb2.Timestamp
    start_time: _timestamp_pb2.Timestamp
    end_time: _timestamp_pb2.Timestamp
    task_worker_id: str
    task_worker_version: str
    status: _common_enums_pb2.TaskStatus
    def __init__(self, output: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., log_output: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., schedule_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., start_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., end_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., task_worker_id: _Optional[str] = ..., task_worker_version: _Optional[str] = ..., status: _Optional[_Union[_common_enums_pb2.TaskStatus, str]] = ...) -> None: ...

class TaskRunSource(_message.Message):
    __slots__ = ["task_node", "user_task_trigger"]
    TASK_NODE_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_TRIGGER_FIELD_NUMBER: _ClassVar[int]
    task_node: TaskNodeReference
    user_task_trigger: _user_tasks_pb2.UserTaskTriggerReference
    def __init__(self, task_node: _Optional[_Union[TaskNodeReference, _Mapping]] = ..., user_task_trigger: _Optional[_Union[_user_tasks_pb2.UserTaskTriggerReference, _Mapping]] = ...) -> None: ...

class TaskNodeReference(_message.Message):
    __slots__ = ["node_run_id", "wf_spec_id"]
    NODE_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    node_run_id: _object_id_pb2.NodeRunId
    wf_spec_id: _object_id_pb2.WfSpecId
    def __init__(self, node_run_id: _Optional[_Union[_object_id_pb2.NodeRunId, _Mapping]] = ..., wf_spec_id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ...) -> None: ...
