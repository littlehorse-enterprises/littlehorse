from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class UserTaskRunStatus(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    UNASSIGNED: _ClassVar[UserTaskRunStatus]
    ASSIGNED: _ClassVar[UserTaskRunStatus]
    DONE: _ClassVar[UserTaskRunStatus]
    CANCELLED: _ClassVar[UserTaskRunStatus]
UNASSIGNED: UserTaskRunStatus
ASSIGNED: UserTaskRunStatus
DONE: UserTaskRunStatus
CANCELLED: UserTaskRunStatus

class UserTaskDef(_message.Message):
    __slots__ = ["name", "version", "description", "fields", "created_at"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    VERSION_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    name: str
    version: int
    description: str
    fields: _containers.RepeatedCompositeFieldContainer[UserTaskField]
    created_at: _timestamp_pb2.Timestamp
    def __init__(self, name: _Optional[str] = ..., version: _Optional[int] = ..., description: _Optional[str] = ..., fields: _Optional[_Iterable[_Union[UserTaskField, _Mapping]]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class UserTaskField(_message.Message):
    __slots__ = ["name", "type", "description", "display_name", "required"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    DISPLAY_NAME_FIELD_NUMBER: _ClassVar[int]
    REQUIRED_FIELD_NUMBER: _ClassVar[int]
    name: str
    type: _common_enums_pb2.VariableType
    description: str
    display_name: str
    required: bool
    def __init__(self, name: _Optional[str] = ..., type: _Optional[_Union[_common_enums_pb2.VariableType, str]] = ..., description: _Optional[str] = ..., display_name: _Optional[str] = ..., required: bool = ...) -> None: ...

class UserTaskRun(_message.Message):
    __slots__ = ["id", "user_task_def_id", "user_group", "user_id", "results", "status", "events", "notes", "scheduled_time", "node_run_id", "epoch"]
    class ResultsEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _variable_pb2.VariableValue
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ...) -> None: ...
    ID_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    USER_GROUP_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    EVENTS_FIELD_NUMBER: _ClassVar[int]
    NOTES_FIELD_NUMBER: _ClassVar[int]
    SCHEDULED_TIME_FIELD_NUMBER: _ClassVar[int]
    NODE_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    EPOCH_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.UserTaskRunId
    user_task_def_id: _object_id_pb2.UserTaskDefId
    user_group: str
    user_id: str
    results: _containers.MessageMap[str, _variable_pb2.VariableValue]
    status: UserTaskRunStatus
    events: _containers.RepeatedCompositeFieldContainer[UserTaskEvent]
    notes: str
    scheduled_time: _timestamp_pb2.Timestamp
    node_run_id: _object_id_pb2.NodeRunId
    epoch: int
    def __init__(self, id: _Optional[_Union[_object_id_pb2.UserTaskRunId, _Mapping]] = ..., user_task_def_id: _Optional[_Union[_object_id_pb2.UserTaskDefId, _Mapping]] = ..., user_group: _Optional[str] = ..., user_id: _Optional[str] = ..., results: _Optional[_Mapping[str, _variable_pb2.VariableValue]] = ..., status: _Optional[_Union[UserTaskRunStatus, str]] = ..., events: _Optional[_Iterable[_Union[UserTaskEvent, _Mapping]]] = ..., notes: _Optional[str] = ..., scheduled_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., node_run_id: _Optional[_Union[_object_id_pb2.NodeRunId, _Mapping]] = ..., epoch: _Optional[int] = ...) -> None: ...

class AssignUserTaskRunRequest(_message.Message):
    __slots__ = ["user_task_run_id", "override_claim", "user_group", "user_id"]
    USER_TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    OVERRIDE_CLAIM_FIELD_NUMBER: _ClassVar[int]
    USER_GROUP_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    user_task_run_id: _object_id_pb2.UserTaskRunId
    override_claim: bool
    user_group: str
    user_id: str
    def __init__(self, user_task_run_id: _Optional[_Union[_object_id_pb2.UserTaskRunId, _Mapping]] = ..., override_claim: bool = ..., user_group: _Optional[str] = ..., user_id: _Optional[str] = ...) -> None: ...

class CompleteUserTaskRunRequest(_message.Message):
    __slots__ = ["user_task_run_id", "results", "user_id"]
    class ResultsEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _variable_pb2.VariableValue
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ...) -> None: ...
    USER_TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    user_task_run_id: _object_id_pb2.UserTaskRunId
    results: _containers.MessageMap[str, _variable_pb2.VariableValue]
    user_id: str
    def __init__(self, user_task_run_id: _Optional[_Union[_object_id_pb2.UserTaskRunId, _Mapping]] = ..., results: _Optional[_Mapping[str, _variable_pb2.VariableValue]] = ..., user_id: _Optional[str] = ...) -> None: ...

class SaveUserTaskRunProgressRequest(_message.Message):
    __slots__ = ["user_task_run_id", "results", "user_id", "policy"]
    class SaveUserTaskRunAssignmentPolicy(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
        __slots__ = []
        FAIL_IF_CLAIMED_BY_OTHER: _ClassVar[SaveUserTaskRunProgressRequest.SaveUserTaskRunAssignmentPolicy]
        IGNORE_CLAIM: _ClassVar[SaveUserTaskRunProgressRequest.SaveUserTaskRunAssignmentPolicy]
    FAIL_IF_CLAIMED_BY_OTHER: SaveUserTaskRunProgressRequest.SaveUserTaskRunAssignmentPolicy
    IGNORE_CLAIM: SaveUserTaskRunProgressRequest.SaveUserTaskRunAssignmentPolicy
    class ResultsEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _variable_pb2.VariableValue
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ...) -> None: ...
    USER_TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    POLICY_FIELD_NUMBER: _ClassVar[int]
    user_task_run_id: _object_id_pb2.UserTaskRunId
    results: _containers.MessageMap[str, _variable_pb2.VariableValue]
    user_id: str
    policy: SaveUserTaskRunProgressRequest.SaveUserTaskRunAssignmentPolicy
    def __init__(self, user_task_run_id: _Optional[_Union[_object_id_pb2.UserTaskRunId, _Mapping]] = ..., results: _Optional[_Mapping[str, _variable_pb2.VariableValue]] = ..., user_id: _Optional[str] = ..., policy: _Optional[_Union[SaveUserTaskRunProgressRequest.SaveUserTaskRunAssignmentPolicy, str]] = ...) -> None: ...

class CancelUserTaskRunRequest(_message.Message):
    __slots__ = ["user_task_run_id"]
    USER_TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    user_task_run_id: _object_id_pb2.UserTaskRunId
    def __init__(self, user_task_run_id: _Optional[_Union[_object_id_pb2.UserTaskRunId, _Mapping]] = ...) -> None: ...

class UserTaskTriggerReference(_message.Message):
    __slots__ = ["node_run_id", "user_task_event_number", "user_id", "user_group"]
    NODE_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_EVENT_NUMBER_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    USER_GROUP_FIELD_NUMBER: _ClassVar[int]
    node_run_id: _object_id_pb2.NodeRunId
    user_task_event_number: int
    user_id: str
    user_group: str
    def __init__(self, node_run_id: _Optional[_Union[_object_id_pb2.NodeRunId, _Mapping]] = ..., user_task_event_number: _Optional[int] = ..., user_id: _Optional[str] = ..., user_group: _Optional[str] = ...) -> None: ...

class UserTaskEvent(_message.Message):
    __slots__ = ["time", "task_executed", "assigned", "cancelled", "saved"]
    class UTECancelled(_message.Message):
        __slots__ = ["message"]
        MESSAGE_FIELD_NUMBER: _ClassVar[int]
        message: str
        def __init__(self, message: _Optional[str] = ...) -> None: ...
    class UTETaskExecuted(_message.Message):
        __slots__ = ["task_run"]
        TASK_RUN_FIELD_NUMBER: _ClassVar[int]
        task_run: _object_id_pb2.TaskRunId
        def __init__(self, task_run: _Optional[_Union[_object_id_pb2.TaskRunId, _Mapping]] = ...) -> None: ...
    class UTESaved(_message.Message):
        __slots__ = ["user_id", "results"]
        class ResultsEntry(_message.Message):
            __slots__ = ["key", "value"]
            KEY_FIELD_NUMBER: _ClassVar[int]
            VALUE_FIELD_NUMBER: _ClassVar[int]
            key: str
            value: _variable_pb2.VariableValue
            def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ...) -> None: ...
        USER_ID_FIELD_NUMBER: _ClassVar[int]
        RESULTS_FIELD_NUMBER: _ClassVar[int]
        user_id: str
        results: _containers.MessageMap[str, _variable_pb2.VariableValue]
        def __init__(self, user_id: _Optional[str] = ..., results: _Optional[_Mapping[str, _variable_pb2.VariableValue]] = ...) -> None: ...
    class UTEAssigned(_message.Message):
        __slots__ = ["old_user_id", "old_user_group", "new_user_id", "new_user_group"]
        OLD_USER_ID_FIELD_NUMBER: _ClassVar[int]
        OLD_USER_GROUP_FIELD_NUMBER: _ClassVar[int]
        NEW_USER_ID_FIELD_NUMBER: _ClassVar[int]
        NEW_USER_GROUP_FIELD_NUMBER: _ClassVar[int]
        old_user_id: str
        old_user_group: str
        new_user_id: str
        new_user_group: str
        def __init__(self, old_user_id: _Optional[str] = ..., old_user_group: _Optional[str] = ..., new_user_id: _Optional[str] = ..., new_user_group: _Optional[str] = ...) -> None: ...
    TIME_FIELD_NUMBER: _ClassVar[int]
    TASK_EXECUTED_FIELD_NUMBER: _ClassVar[int]
    ASSIGNED_FIELD_NUMBER: _ClassVar[int]
    CANCELLED_FIELD_NUMBER: _ClassVar[int]
    SAVED_FIELD_NUMBER: _ClassVar[int]
    time: _timestamp_pb2.Timestamp
    task_executed: UserTaskEvent.UTETaskExecuted
    assigned: UserTaskEvent.UTEAssigned
    cancelled: UserTaskEvent.UTECancelled
    saved: UserTaskEvent.UTESaved
    def __init__(self, time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., task_executed: _Optional[_Union[UserTaskEvent.UTETaskExecuted, _Mapping]] = ..., assigned: _Optional[_Union[UserTaskEvent.UTEAssigned, _Mapping]] = ..., cancelled: _Optional[_Union[UserTaskEvent.UTECancelled, _Mapping]] = ..., saved: _Optional[_Union[UserTaskEvent.UTESaved, _Mapping]] = ...) -> None: ...
