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
    __slots__ = ["id", "user_task_def_id", "results", "status", "events", "notes", "scheduled_time", "node_run_id", "user_group", "user"]
    ID_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    EVENTS_FIELD_NUMBER: _ClassVar[int]
    NOTES_FIELD_NUMBER: _ClassVar[int]
    SCHEDULED_TIME_FIELD_NUMBER: _ClassVar[int]
    NODE_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_GROUP_FIELD_NUMBER: _ClassVar[int]
    USER_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.UserTaskRunId
    user_task_def_id: _object_id_pb2.UserTaskDefId
    results: _containers.RepeatedCompositeFieldContainer[UserTaskFieldResult]
    status: UserTaskRunStatus
    events: _containers.RepeatedCompositeFieldContainer[UserTaskEvent]
    notes: str
    scheduled_time: _timestamp_pb2.Timestamp
    node_run_id: _object_id_pb2.NodeRunId
    user_group: UserGroup
    user: User
    def __init__(self, id: _Optional[_Union[_object_id_pb2.UserTaskRunId, _Mapping]] = ..., user_task_def_id: _Optional[_Union[_object_id_pb2.UserTaskDefId, _Mapping]] = ..., results: _Optional[_Iterable[_Union[UserTaskFieldResult, _Mapping]]] = ..., status: _Optional[_Union[UserTaskRunStatus, str]] = ..., events: _Optional[_Iterable[_Union[UserTaskEvent, _Mapping]]] = ..., notes: _Optional[str] = ..., scheduled_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., node_run_id: _Optional[_Union[_object_id_pb2.NodeRunId, _Mapping]] = ..., user_group: _Optional[_Union[UserGroup, _Mapping]] = ..., user: _Optional[_Union[User, _Mapping]] = ...) -> None: ...

class AssignUserTaskRunRequest(_message.Message):
    __slots__ = ["user_task_run_id", "override_claim", "user", "user_group"]
    USER_TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    OVERRIDE_CLAIM_FIELD_NUMBER: _ClassVar[int]
    USER_FIELD_NUMBER: _ClassVar[int]
    USER_GROUP_FIELD_NUMBER: _ClassVar[int]
    user_task_run_id: _object_id_pb2.UserTaskRunId
    override_claim: bool
    user: User
    user_group: UserGroup
    def __init__(self, user_task_run_id: _Optional[_Union[_object_id_pb2.UserTaskRunId, _Mapping]] = ..., override_claim: bool = ..., user: _Optional[_Union[User, _Mapping]] = ..., user_group: _Optional[_Union[UserGroup, _Mapping]] = ...) -> None: ...

class UserTaskFieldResult(_message.Message):
    __slots__ = ["name", "value"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    name: str
    value: _variable_pb2.VariableValue
    def __init__(self, name: _Optional[str] = ..., value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ...) -> None: ...

class UserTaskResult(_message.Message):
    __slots__ = ["fields"]
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    fields: _containers.RepeatedCompositeFieldContainer[UserTaskFieldResult]
    def __init__(self, fields: _Optional[_Iterable[_Union[UserTaskFieldResult, _Mapping]]] = ...) -> None: ...

class SaveUserTaskRun(_message.Message):
    __slots__ = ["result", "user_id", "results"]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    result: UserTaskResult
    user_id: str
    results: _containers.RepeatedCompositeFieldContainer[UserTaskFieldResult]
    def __init__(self, result: _Optional[_Union[UserTaskResult, _Mapping]] = ..., user_id: _Optional[str] = ..., results: _Optional[_Iterable[_Union[UserTaskFieldResult, _Mapping]]] = ...) -> None: ...

class CompleteUserTaskRunRequest(_message.Message):
    __slots__ = ["user_task_run_id", "result", "user_id"]
    USER_TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    user_task_run_id: _object_id_pb2.UserTaskRunId
    result: UserTaskResult
    user_id: str
    def __init__(self, user_task_run_id: _Optional[_Union[_object_id_pb2.UserTaskRunId, _Mapping]] = ..., result: _Optional[_Union[UserTaskResult, _Mapping]] = ..., user_id: _Optional[str] = ...) -> None: ...

class CancelUserTaskRunRequest(_message.Message):
    __slots__ = ["user_task_run_id"]
    USER_TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    user_task_run_id: _object_id_pb2.UserTaskRunId
    def __init__(self, user_task_run_id: _Optional[_Union[_object_id_pb2.UserTaskRunId, _Mapping]] = ...) -> None: ...

class UserTaskTriggerContext(_message.Message):
    __slots__ = ["user_group", "user"]
    USER_GROUP_FIELD_NUMBER: _ClassVar[int]
    USER_FIELD_NUMBER: _ClassVar[int]
    user_group: UserGroup
    user: User
    def __init__(self, user_group: _Optional[_Union[UserGroup, _Mapping]] = ..., user: _Optional[_Union[User, _Mapping]] = ...) -> None: ...

class UserTaskTriggerReference(_message.Message):
    __slots__ = ["node_run_id", "user_task_event_number", "wf_spec_id", "context"]
    NODE_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_EVENT_NUMBER_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    CONTEXT_FIELD_NUMBER: _ClassVar[int]
    node_run_id: _object_id_pb2.NodeRunId
    user_task_event_number: int
    wf_spec_id: _object_id_pb2.WfSpecId
    context: UserTaskTriggerContext
    def __init__(self, node_run_id: _Optional[_Union[_object_id_pb2.NodeRunId, _Mapping]] = ..., user_task_event_number: _Optional[int] = ..., wf_spec_id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., context: _Optional[_Union[UserTaskTriggerContext, _Mapping]] = ...) -> None: ...

class UserGroup(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...

class User(_message.Message):
    __slots__ = ["id", "user_group"]
    ID_FIELD_NUMBER: _ClassVar[int]
    USER_GROUP_FIELD_NUMBER: _ClassVar[int]
    id: str
    user_group: UserGroup
    def __init__(self, id: _Optional[str] = ..., user_group: _Optional[_Union[UserGroup, _Mapping]] = ...) -> None: ...

class UserTaskEvent(_message.Message):
    __slots__ = ["time", "task_executed", "reassigned", "cancelled"]
    class UTECancelled(_message.Message):
        __slots__ = []
        def __init__(self) -> None: ...
    class UTETaskExecuted(_message.Message):
        __slots__ = ["task_run"]
        TASK_RUN_FIELD_NUMBER: _ClassVar[int]
        task_run: _object_id_pb2.TaskRunId
        def __init__(self, task_run: _Optional[_Union[_object_id_pb2.TaskRunId, _Mapping]] = ...) -> None: ...
    class UTEReassigned(_message.Message):
        __slots__ = ["old_user", "old_user_group", "new_user", "new_user_group"]
        OLD_USER_FIELD_NUMBER: _ClassVar[int]
        OLD_USER_GROUP_FIELD_NUMBER: _ClassVar[int]
        NEW_USER_FIELD_NUMBER: _ClassVar[int]
        NEW_USER_GROUP_FIELD_NUMBER: _ClassVar[int]
        old_user: User
        old_user_group: UserGroup
        new_user: User
        new_user_group: UserGroup
        def __init__(self, old_user: _Optional[_Union[User, _Mapping]] = ..., old_user_group: _Optional[_Union[UserGroup, _Mapping]] = ..., new_user: _Optional[_Union[User, _Mapping]] = ..., new_user_group: _Optional[_Union[UserGroup, _Mapping]] = ...) -> None: ...
    TIME_FIELD_NUMBER: _ClassVar[int]
    TASK_EXECUTED_FIELD_NUMBER: _ClassVar[int]
    REASSIGNED_FIELD_NUMBER: _ClassVar[int]
    CANCELLED_FIELD_NUMBER: _ClassVar[int]
    time: _timestamp_pb2.Timestamp
    task_executed: UserTaskEvent.UTETaskExecuted
    reassigned: UserTaskEvent.UTEReassigned
    cancelled: UserTaskEvent.UTECancelled
    def __init__(self, time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., task_executed: _Optional[_Union[UserTaskEvent.UTETaskExecuted, _Mapping]] = ..., reassigned: _Optional[_Union[UserTaskEvent.UTEReassigned, _Mapping]] = ..., cancelled: _Optional[_Union[UserTaskEvent.UTECancelled, _Mapping]] = ...) -> None: ...
