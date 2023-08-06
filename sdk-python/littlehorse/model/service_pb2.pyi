from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class LHResponseCodePb(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    OK: _ClassVar[LHResponseCodePb]
    CONNECTION_ERROR: _ClassVar[LHResponseCodePb]
    NOT_FOUND_ERROR: _ClassVar[LHResponseCodePb]
    BAD_REQUEST_ERROR: _ClassVar[LHResponseCodePb]
    VALIDATION_ERROR: _ClassVar[LHResponseCodePb]
    ALREADY_EXISTS_ERROR: _ClassVar[LHResponseCodePb]
    REPORTED_BUT_NOT_PROCESSED: _ClassVar[LHResponseCodePb]

class LHStatusPb(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    STARTING: _ClassVar[LHStatusPb]
    RUNNING: _ClassVar[LHStatusPb]
    COMPLETED: _ClassVar[LHStatusPb]
    HALTING: _ClassVar[LHStatusPb]
    HALTED: _ClassVar[LHStatusPb]
    ERROR: _ClassVar[LHStatusPb]

class TaskStatusPb(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    TASK_SCHEDULED: _ClassVar[TaskStatusPb]
    TASK_RUNNING: _ClassVar[TaskStatusPb]
    TASK_SUCCESS: _ClassVar[TaskStatusPb]
    TASK_FAILED: _ClassVar[TaskStatusPb]
    TASK_TIMEOUT: _ClassVar[TaskStatusPb]
    TASK_OUTPUT_SERIALIZING_ERROR: _ClassVar[TaskStatusPb]
    TASK_INPUT_VAR_SUB_ERROR: _ClassVar[TaskStatusPb]

class LHHealthResultPb(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    LH_HEALTH_RUNNING: _ClassVar[LHHealthResultPb]
    LH_HEALTH_REBALANCING: _ClassVar[LHHealthResultPb]
    LH_HEALTH_ERROR: _ClassVar[LHHealthResultPb]

class VariableTypePb(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    JSON_OBJ: _ClassVar[VariableTypePb]
    JSON_ARR: _ClassVar[VariableTypePb]
    DOUBLE: _ClassVar[VariableTypePb]
    BOOL: _ClassVar[VariableTypePb]
    STR: _ClassVar[VariableTypePb]
    INT: _ClassVar[VariableTypePb]
    BYTES: _ClassVar[VariableTypePb]
    NULL: _ClassVar[VariableTypePb]

class ThreadTypePb(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    ENTRYPOINT: _ClassVar[ThreadTypePb]
    CHILD: _ClassVar[ThreadTypePb]
    INTERRUPT: _ClassVar[ThreadTypePb]
    FAILURE_HANDLER: _ClassVar[ThreadTypePb]

class ComparatorPb(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    LESS_THAN: _ClassVar[ComparatorPb]
    GREATER_THAN: _ClassVar[ComparatorPb]
    LESS_THAN_EQ: _ClassVar[ComparatorPb]
    GREATER_THAN_EQ: _ClassVar[ComparatorPb]
    EQUALS: _ClassVar[ComparatorPb]
    NOT_EQUALS: _ClassVar[ComparatorPb]
    IN: _ClassVar[ComparatorPb]
    NOT_IN: _ClassVar[ComparatorPb]

class VariableMutationTypePb(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    ASSIGN: _ClassVar[VariableMutationTypePb]
    ADD: _ClassVar[VariableMutationTypePb]
    EXTEND: _ClassVar[VariableMutationTypePb]
    SUBTRACT: _ClassVar[VariableMutationTypePb]
    MULTIPLY: _ClassVar[VariableMutationTypePb]
    DIVIDE: _ClassVar[VariableMutationTypePb]
    REMOVE_IF_PRESENT: _ClassVar[VariableMutationTypePb]
    REMOVE_INDEX: _ClassVar[VariableMutationTypePb]
    REMOVE_KEY: _ClassVar[VariableMutationTypePb]

class UserTaskRunStatusPb(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    UNASSIGNED: _ClassVar[UserTaskRunStatusPb]
    ASSIGNED_NOT_CLAIMED: _ClassVar[UserTaskRunStatusPb]
    CLAIMED: _ClassVar[UserTaskRunStatusPb]
    DONE: _ClassVar[UserTaskRunStatusPb]
    CANCELLED: _ClassVar[UserTaskRunStatusPb]

class IndexTypePb(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    LOCAL_INDEX: _ClassVar[IndexTypePb]
    REMOTE_INDEX: _ClassVar[IndexTypePb]

class MetricsWindowLengthPb(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    MINUTES_5: _ClassVar[MetricsWindowLengthPb]
    HOURS_2: _ClassVar[MetricsWindowLengthPb]
    DAYS_1: _ClassVar[MetricsWindowLengthPb]
OK: LHResponseCodePb
CONNECTION_ERROR: LHResponseCodePb
NOT_FOUND_ERROR: LHResponseCodePb
BAD_REQUEST_ERROR: LHResponseCodePb
VALIDATION_ERROR: LHResponseCodePb
ALREADY_EXISTS_ERROR: LHResponseCodePb
REPORTED_BUT_NOT_PROCESSED: LHResponseCodePb
STARTING: LHStatusPb
RUNNING: LHStatusPb
COMPLETED: LHStatusPb
HALTING: LHStatusPb
HALTED: LHStatusPb
ERROR: LHStatusPb
TASK_SCHEDULED: TaskStatusPb
TASK_RUNNING: TaskStatusPb
TASK_SUCCESS: TaskStatusPb
TASK_FAILED: TaskStatusPb
TASK_TIMEOUT: TaskStatusPb
TASK_OUTPUT_SERIALIZING_ERROR: TaskStatusPb
TASK_INPUT_VAR_SUB_ERROR: TaskStatusPb
LH_HEALTH_RUNNING: LHHealthResultPb
LH_HEALTH_REBALANCING: LHHealthResultPb
LH_HEALTH_ERROR: LHHealthResultPb
JSON_OBJ: VariableTypePb
JSON_ARR: VariableTypePb
DOUBLE: VariableTypePb
BOOL: VariableTypePb
STR: VariableTypePb
INT: VariableTypePb
BYTES: VariableTypePb
NULL: VariableTypePb
ENTRYPOINT: ThreadTypePb
CHILD: ThreadTypePb
INTERRUPT: ThreadTypePb
FAILURE_HANDLER: ThreadTypePb
LESS_THAN: ComparatorPb
GREATER_THAN: ComparatorPb
LESS_THAN_EQ: ComparatorPb
GREATER_THAN_EQ: ComparatorPb
EQUALS: ComparatorPb
NOT_EQUALS: ComparatorPb
IN: ComparatorPb
NOT_IN: ComparatorPb
ASSIGN: VariableMutationTypePb
ADD: VariableMutationTypePb
EXTEND: VariableMutationTypePb
SUBTRACT: VariableMutationTypePb
MULTIPLY: VariableMutationTypePb
DIVIDE: VariableMutationTypePb
REMOVE_IF_PRESENT: VariableMutationTypePb
REMOVE_INDEX: VariableMutationTypePb
REMOVE_KEY: VariableMutationTypePb
UNASSIGNED: UserTaskRunStatusPb
ASSIGNED_NOT_CLAIMED: UserTaskRunStatusPb
CLAIMED: UserTaskRunStatusPb
DONE: UserTaskRunStatusPb
CANCELLED: UserTaskRunStatusPb
LOCAL_INDEX: IndexTypePb
REMOTE_INDEX: IndexTypePb
MINUTES_5: MetricsWindowLengthPb
HOURS_2: MetricsWindowLengthPb
DAYS_1: MetricsWindowLengthPb

class WfSpecIdPb(_message.Message):
    __slots__ = ["name", "version"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    VERSION_FIELD_NUMBER: _ClassVar[int]
    name: str
    version: int
    def __init__(self, name: _Optional[str] = ..., version: _Optional[int] = ...) -> None: ...

class TaskDefIdPb(_message.Message):
    __slots__ = ["name"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    def __init__(self, name: _Optional[str] = ...) -> None: ...

class ExternalEventDefIdPb(_message.Message):
    __slots__ = ["name"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    def __init__(self, name: _Optional[str] = ...) -> None: ...

class GetLatestWfSpecPb(_message.Message):
    __slots__ = ["name"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    def __init__(self, name: _Optional[str] = ...) -> None: ...

class GetLatestUserTaskDefPb(_message.Message):
    __slots__ = ["name"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    def __init__(self, name: _Optional[str] = ...) -> None: ...

class GetWfSpecReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: WfSpecPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[WfSpecPb, _Mapping]] = ...) -> None: ...

class PutWfSpecPb(_message.Message):
    __slots__ = ["name", "thread_specs", "entrypoint_thread_name", "retention_hours"]
    class ThreadSpecsEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: ThreadSpecPb
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[ThreadSpecPb, _Mapping]] = ...) -> None: ...
    NAME_FIELD_NUMBER: _ClassVar[int]
    THREAD_SPECS_FIELD_NUMBER: _ClassVar[int]
    ENTRYPOINT_THREAD_NAME_FIELD_NUMBER: _ClassVar[int]
    RETENTION_HOURS_FIELD_NUMBER: _ClassVar[int]
    name: str
    thread_specs: _containers.MessageMap[str, ThreadSpecPb]
    entrypoint_thread_name: str
    retention_hours: int
    def __init__(self, name: _Optional[str] = ..., thread_specs: _Optional[_Mapping[str, ThreadSpecPb]] = ..., entrypoint_thread_name: _Optional[str] = ..., retention_hours: _Optional[int] = ...) -> None: ...

class PutWfSpecReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: WfSpecPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[WfSpecPb, _Mapping]] = ...) -> None: ...

class GetTaskDefReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: TaskDefPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[TaskDefPb, _Mapping]] = ...) -> None: ...

class PutTaskDefPb(_message.Message):
    __slots__ = ["name", "input_vars"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    INPUT_VARS_FIELD_NUMBER: _ClassVar[int]
    name: str
    input_vars: _containers.RepeatedCompositeFieldContainer[VariableDefPb]
    def __init__(self, name: _Optional[str] = ..., input_vars: _Optional[_Iterable[_Union[VariableDefPb, _Mapping]]] = ...) -> None: ...

class PutTaskDefReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: TaskDefPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[TaskDefPb, _Mapping]] = ...) -> None: ...

class PutUserTaskDefPb(_message.Message):
    __slots__ = ["name", "fields", "description"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    name: str
    fields: _containers.RepeatedCompositeFieldContainer[UserTaskFieldPb]
    description: str
    def __init__(self, name: _Optional[str] = ..., fields: _Optional[_Iterable[_Union[UserTaskFieldPb, _Mapping]]] = ..., description: _Optional[str] = ...) -> None: ...

class PutUserTaskDefReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: UserTaskDefPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[UserTaskDefPb, _Mapping]] = ...) -> None: ...

class GetUserTaskDefReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: UserTaskDefPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[UserTaskDefPb, _Mapping]] = ...) -> None: ...

class PutExternalEventDefPb(_message.Message):
    __slots__ = ["name", "retention_hours"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    RETENTION_HOURS_FIELD_NUMBER: _ClassVar[int]
    name: str
    retention_hours: int
    def __init__(self, name: _Optional[str] = ..., retention_hours: _Optional[int] = ...) -> None: ...

class PutExternalEventDefReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: ExternalEventDefPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[ExternalEventDefPb, _Mapping]] = ...) -> None: ...

class GetExternalEventDefReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: ExternalEventDefPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[ExternalEventDefPb, _Mapping]] = ...) -> None: ...

class GetWfRunReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: WfRunPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[WfRunPb, _Mapping]] = ...) -> None: ...

class GetNodeRunReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: NodeRunPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[NodeRunPb, _Mapping]] = ...) -> None: ...

class GetTaskRunReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: TaskRunPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[TaskRunPb, _Mapping]] = ...) -> None: ...

class GetUserTaskRunReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: UserTaskRunPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[UserTaskRunPb, _Mapping]] = ...) -> None: ...

class GetVariableReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: VariablePb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[VariablePb, _Mapping]] = ...) -> None: ...

class GetExternalEventReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: ExternalEventPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[ExternalEventPb, _Mapping]] = ...) -> None: ...

class PutExternalEventPb(_message.Message):
    __slots__ = ["wf_run_id", "external_event_def_name", "guid", "content", "thread_run_number", "node_run_position"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    GUID_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    NODE_RUN_POSITION_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    external_event_def_name: str
    guid: str
    content: VariableValuePb
    thread_run_number: int
    node_run_position: int
    def __init__(self, wf_run_id: _Optional[str] = ..., external_event_def_name: _Optional[str] = ..., guid: _Optional[str] = ..., content: _Optional[_Union[VariableValuePb, _Mapping]] = ..., thread_run_number: _Optional[int] = ..., node_run_position: _Optional[int] = ...) -> None: ...

class DeleteExternalEventPb(_message.Message):
    __slots__ = ["wf_run_id", "external_event_def_name", "guid"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    GUID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    external_event_def_name: str
    guid: str
    def __init__(self, wf_run_id: _Optional[str] = ..., external_event_def_name: _Optional[str] = ..., guid: _Optional[str] = ...) -> None: ...

class PutExternalEventReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: ExternalEventPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[ExternalEventPb, _Mapping]] = ...) -> None: ...

class RunWfPb(_message.Message):
    __slots__ = ["wf_spec_name", "wf_spec_version", "variables", "id"]
    class VariablesEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: VariableValuePb
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[VariableValuePb, _Mapping]] = ...) -> None: ...
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_VERSION_FIELD_NUMBER: _ClassVar[int]
    VARIABLES_FIELD_NUMBER: _ClassVar[int]
    ID_FIELD_NUMBER: _ClassVar[int]
    wf_spec_name: str
    wf_spec_version: int
    variables: _containers.MessageMap[str, VariableValuePb]
    id: str
    def __init__(self, wf_spec_name: _Optional[str] = ..., wf_spec_version: _Optional[int] = ..., variables: _Optional[_Mapping[str, VariableValuePb]] = ..., id: _Optional[str] = ...) -> None: ...

class RunWfReplyPb(_message.Message):
    __slots__ = ["code", "message", "wf_spec_version", "wf_run_id"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_VERSION_FIELD_NUMBER: _ClassVar[int]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    wf_spec_version: int
    wf_run_id: str
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., wf_spec_version: _Optional[int] = ..., wf_run_id: _Optional[str] = ...) -> None: ...

class SearchWfRunPb(_message.Message):
    __slots__ = ["bookmark", "limit", "status_and_spec", "name", "status_and_name"]
    class StatusAndSpecPb(_message.Message):
        __slots__ = ["wf_spec_name", "status", "wf_spec_version", "earliest_start", "latest_start"]
        WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
        STATUS_FIELD_NUMBER: _ClassVar[int]
        WF_SPEC_VERSION_FIELD_NUMBER: _ClassVar[int]
        EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
        LATEST_START_FIELD_NUMBER: _ClassVar[int]
        wf_spec_name: str
        status: LHStatusPb
        wf_spec_version: int
        earliest_start: _timestamp_pb2.Timestamp
        latest_start: _timestamp_pb2.Timestamp
        def __init__(self, wf_spec_name: _Optional[str] = ..., status: _Optional[_Union[LHStatusPb, str]] = ..., wf_spec_version: _Optional[int] = ..., earliest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...
    class NamePb(_message.Message):
        __slots__ = ["wf_spec_name", "earliest_start", "latest_start"]
        WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
        EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
        LATEST_START_FIELD_NUMBER: _ClassVar[int]
        wf_spec_name: str
        earliest_start: _timestamp_pb2.Timestamp
        latest_start: _timestamp_pb2.Timestamp
        def __init__(self, wf_spec_name: _Optional[str] = ..., earliest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...
    class StatusAndNamePb(_message.Message):
        __slots__ = ["wf_spec_name", "status", "earliest_start", "latest_start"]
        WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
        STATUS_FIELD_NUMBER: _ClassVar[int]
        EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
        LATEST_START_FIELD_NUMBER: _ClassVar[int]
        wf_spec_name: str
        status: LHStatusPb
        earliest_start: _timestamp_pb2.Timestamp
        latest_start: _timestamp_pb2.Timestamp
        def __init__(self, wf_spec_name: _Optional[str] = ..., status: _Optional[_Union[LHStatusPb, str]] = ..., earliest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    STATUS_AND_SPEC_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    STATUS_AND_NAME_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    status_and_spec: SearchWfRunPb.StatusAndSpecPb
    name: SearchWfRunPb.NamePb
    status_and_name: SearchWfRunPb.StatusAndNamePb
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., status_and_spec: _Optional[_Union[SearchWfRunPb.StatusAndSpecPb, _Mapping]] = ..., name: _Optional[_Union[SearchWfRunPb.NamePb, _Mapping]] = ..., status_and_name: _Optional[_Union[SearchWfRunPb.StatusAndNamePb, _Mapping]] = ...) -> None: ...

class SearchWfRunReplyPb(_message.Message):
    __slots__ = ["code", "message", "results", "bookmark"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[WfRunIdPb]
    bookmark: bytes
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[WfRunIdPb, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchTaskRunPb(_message.Message):
    __slots__ = ["bookmark", "limit", "status_and_task_def", "task_def"]
    class StatusAndTaskDefPb(_message.Message):
        __slots__ = ["status", "task_def_name", "earliest_start", "latest_start"]
        STATUS_FIELD_NUMBER: _ClassVar[int]
        TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
        EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
        LATEST_START_FIELD_NUMBER: _ClassVar[int]
        status: TaskStatusPb
        task_def_name: str
        earliest_start: _timestamp_pb2.Timestamp
        latest_start: _timestamp_pb2.Timestamp
        def __init__(self, status: _Optional[_Union[TaskStatusPb, str]] = ..., task_def_name: _Optional[str] = ..., earliest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...
    class ByTaskDefPb(_message.Message):
        __slots__ = ["task_def_name", "earliest_start", "latest_start"]
        TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
        EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
        LATEST_START_FIELD_NUMBER: _ClassVar[int]
        task_def_name: str
        earliest_start: _timestamp_pb2.Timestamp
        latest_start: _timestamp_pb2.Timestamp
        def __init__(self, task_def_name: _Optional[str] = ..., earliest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    STATUS_AND_TASK_DEF_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    status_and_task_def: SearchTaskRunPb.StatusAndTaskDefPb
    task_def: SearchTaskRunPb.ByTaskDefPb
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., status_and_task_def: _Optional[_Union[SearchTaskRunPb.StatusAndTaskDefPb, _Mapping]] = ..., task_def: _Optional[_Union[SearchTaskRunPb.ByTaskDefPb, _Mapping]] = ...) -> None: ...

class SearchTaskRunReplyPb(_message.Message):
    __slots__ = ["code", "message", "results", "bookmark"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[TaskRunIdPb]
    bookmark: bytes
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[TaskRunIdPb, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchNodeRunPb(_message.Message):
    __slots__ = ["bookmark", "limit", "wf_run_id"]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    wf_run_id: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., wf_run_id: _Optional[str] = ...) -> None: ...

class SearchNodeRunReplyPb(_message.Message):
    __slots__ = ["code", "message", "results", "bookmark"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[NodeRunIdPb]
    bookmark: bytes
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[NodeRunIdPb, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchUserTaskRunPb(_message.Message):
    __slots__ = ["bookmark", "limit", "status", "user_task_def_name", "user_id", "user_group", "earliest_start", "latest_start"]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    USER_GROUP_FIELD_NUMBER: _ClassVar[int]
    EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
    LATEST_START_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    status: UserTaskRunStatusPb
    user_task_def_name: str
    user_id: str
    user_group: str
    earliest_start: _timestamp_pb2.Timestamp
    latest_start: _timestamp_pb2.Timestamp
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., status: _Optional[_Union[UserTaskRunStatusPb, str]] = ..., user_task_def_name: _Optional[str] = ..., user_id: _Optional[str] = ..., user_group: _Optional[str] = ..., earliest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class SearchUserTaskRunReplyPb(_message.Message):
    __slots__ = ["code", "message", "results", "bookmark"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[UserTaskRunIdPb]
    bookmark: bytes
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[UserTaskRunIdPb, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchVariablePb(_message.Message):
    __slots__ = ["bookmark", "limit", "wf_run_id", "value"]
    class NameAndValuePb(_message.Message):
        __slots__ = ["value", "wf_spec_version", "var_name", "wf_spec_name"]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        WF_SPEC_VERSION_FIELD_NUMBER: _ClassVar[int]
        VAR_NAME_FIELD_NUMBER: _ClassVar[int]
        WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
        value: VariableValuePb
        wf_spec_version: int
        var_name: str
        wf_spec_name: str
        def __init__(self, value: _Optional[_Union[VariableValuePb, _Mapping]] = ..., wf_spec_version: _Optional[int] = ..., var_name: _Optional[str] = ..., wf_spec_name: _Optional[str] = ...) -> None: ...
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    wf_run_id: str
    value: SearchVariablePb.NameAndValuePb
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., wf_run_id: _Optional[str] = ..., value: _Optional[_Union[SearchVariablePb.NameAndValuePb, _Mapping]] = ...) -> None: ...

class SearchVariableReplyPb(_message.Message):
    __slots__ = ["code", "message", "results", "bookmark"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[VariableIdPb]
    bookmark: bytes
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[VariableIdPb, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchTaskDefPb(_message.Message):
    __slots__ = ["bookmark", "limit", "prefix"]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    PREFIX_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    prefix: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., prefix: _Optional[str] = ...) -> None: ...

class SearchTaskDefReplyPb(_message.Message):
    __slots__ = ["code", "message", "results", "bookmark"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[TaskDefIdPb]
    bookmark: bytes
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[TaskDefIdPb, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchUserTaskDefPb(_message.Message):
    __slots__ = ["bookmark", "limit", "prefix", "name"]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    PREFIX_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    prefix: str
    name: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., prefix: _Optional[str] = ..., name: _Optional[str] = ...) -> None: ...

class SearchUserTaskDefReplyPb(_message.Message):
    __slots__ = ["code", "message", "results", "bookmark"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[UserTaskDefIdPb]
    bookmark: bytes
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[UserTaskDefIdPb, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchWfSpecPb(_message.Message):
    __slots__ = ["bookmark", "limit", "name", "prefix", "task_def_name"]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    PREFIX_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    name: str
    prefix: str
    task_def_name: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., name: _Optional[str] = ..., prefix: _Optional[str] = ..., task_def_name: _Optional[str] = ...) -> None: ...

class SearchWfSpecReplyPb(_message.Message):
    __slots__ = ["code", "message", "results", "bookmark"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[WfSpecIdPb]
    bookmark: bytes
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[WfSpecIdPb, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchExternalEventDefPb(_message.Message):
    __slots__ = ["bookmark", "limit", "prefix"]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    PREFIX_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    prefix: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., prefix: _Optional[str] = ...) -> None: ...

class SearchExternalEventDefReplyPb(_message.Message):
    __slots__ = ["code", "message", "results", "bookmark"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[ExternalEventDefIdPb]
    bookmark: bytes
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[ExternalEventDefIdPb, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchExternalEventPb(_message.Message):
    __slots__ = ["bookmark", "limit", "wf_run_id", "external_event_def_name_and_status"]
    class ByExtEvtDefNameAndStatusPb(_message.Message):
        __slots__ = ["external_event_def_name", "is_claimed"]
        EXTERNAL_EVENT_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
        IS_CLAIMED_FIELD_NUMBER: _ClassVar[int]
        external_event_def_name: str
        is_claimed: bool
        def __init__(self, external_event_def_name: _Optional[str] = ..., is_claimed: bool = ...) -> None: ...
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_NAME_AND_STATUS_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    wf_run_id: str
    external_event_def_name_and_status: SearchExternalEventPb.ByExtEvtDefNameAndStatusPb
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., wf_run_id: _Optional[str] = ..., external_event_def_name_and_status: _Optional[_Union[SearchExternalEventPb.ByExtEvtDefNameAndStatusPb, _Mapping]] = ...) -> None: ...

class SearchExternalEventReplyPb(_message.Message):
    __slots__ = ["code", "message", "results", "bookmark"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[ExternalEventIdPb]
    bookmark: bytes
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[ExternalEventIdPb, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class ListNodeRunsPb(_message.Message):
    __slots__ = ["wf_run_id"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    def __init__(self, wf_run_id: _Optional[str] = ...) -> None: ...

class ListNodeRunsReplyPb(_message.Message):
    __slots__ = ["code", "message", "results"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[NodeRunPb]
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[NodeRunPb, _Mapping]]] = ...) -> None: ...

class ListVariablesPb(_message.Message):
    __slots__ = ["wf_run_id"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    def __init__(self, wf_run_id: _Optional[str] = ...) -> None: ...

class ListVariablesReplyPb(_message.Message):
    __slots__ = ["code", "message", "results"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[VariablePb]
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[VariablePb, _Mapping]]] = ...) -> None: ...

class ListExternalEventsPb(_message.Message):
    __slots__ = ["wf_run_id"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    def __init__(self, wf_run_id: _Optional[str] = ...) -> None: ...

class ListExternalEventsReplyPb(_message.Message):
    __slots__ = ["code", "message", "results"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[ExternalEventPb]
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[ExternalEventPb, _Mapping]]] = ...) -> None: ...

class RegisterTaskWorkerPb(_message.Message):
    __slots__ = ["client_id", "task_def_name", "listener_name"]
    CLIENT_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    LISTENER_NAME_FIELD_NUMBER: _ClassVar[int]
    client_id: str
    task_def_name: str
    listener_name: str
    def __init__(self, client_id: _Optional[str] = ..., task_def_name: _Optional[str] = ..., listener_name: _Optional[str] = ...) -> None: ...

class TaskWorkerHeartBeatPb(_message.Message):
    __slots__ = ["client_id", "task_def_name", "listener_name"]
    CLIENT_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    LISTENER_NAME_FIELD_NUMBER: _ClassVar[int]
    client_id: str
    task_def_name: str
    listener_name: str
    def __init__(self, client_id: _Optional[str] = ..., task_def_name: _Optional[str] = ..., listener_name: _Optional[str] = ...) -> None: ...

class RegisterTaskWorkerReplyPb(_message.Message):
    __slots__ = ["code", "message", "your_hosts"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    YOUR_HOSTS_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    your_hosts: _containers.RepeatedCompositeFieldContainer[HostInfoPb]
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., your_hosts: _Optional[_Iterable[_Union[HostInfoPb, _Mapping]]] = ...) -> None: ...

class HostInfoPb(_message.Message):
    __slots__ = ["host", "port"]
    HOST_FIELD_NUMBER: _ClassVar[int]
    PORT_FIELD_NUMBER: _ClassVar[int]
    host: str
    port: int
    def __init__(self, host: _Optional[str] = ..., port: _Optional[int] = ...) -> None: ...

class TaskWorkerMetadataPb(_message.Message):
    __slots__ = ["client_id", "latest_heartbeat", "hosts"]
    CLIENT_ID_FIELD_NUMBER: _ClassVar[int]
    LATEST_HEARTBEAT_FIELD_NUMBER: _ClassVar[int]
    HOSTS_FIELD_NUMBER: _ClassVar[int]
    client_id: str
    latest_heartbeat: _timestamp_pb2.Timestamp
    hosts: _containers.RepeatedCompositeFieldContainer[HostInfoPb]
    def __init__(self, client_id: _Optional[str] = ..., latest_heartbeat: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., hosts: _Optional[_Iterable[_Union[HostInfoPb, _Mapping]]] = ...) -> None: ...

class TaskWorkerGroupPb(_message.Message):
    __slots__ = ["task_def_name", "created_at", "task_workers"]
    class TaskWorkersEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: TaskWorkerMetadataPb
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[TaskWorkerMetadataPb, _Mapping]] = ...) -> None: ...
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    TASK_WORKERS_FIELD_NUMBER: _ClassVar[int]
    task_def_name: str
    created_at: _timestamp_pb2.Timestamp
    task_workers: _containers.MessageMap[str, TaskWorkerMetadataPb]
    def __init__(self, task_def_name: _Optional[str] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., task_workers: _Optional[_Mapping[str, TaskWorkerMetadataPb]] = ...) -> None: ...

class TaskWorkerGroupIdPb(_message.Message):
    __slots__ = ["task_def_name"]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    task_def_name: str
    def __init__(self, task_def_name: _Optional[str] = ...) -> None: ...

class PollTaskPb(_message.Message):
    __slots__ = ["task_def_name", "client_id", "task_worker_version"]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    CLIENT_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_WORKER_VERSION_FIELD_NUMBER: _ClassVar[int]
    task_def_name: str
    client_id: str
    task_worker_version: str
    def __init__(self, task_def_name: _Optional[str] = ..., client_id: _Optional[str] = ..., task_worker_version: _Optional[str] = ...) -> None: ...

class ScheduledTaskPb(_message.Message):
    __slots__ = ["task_run_id", "task_def_id", "attempt_number", "variables", "created_at", "source"]
    TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    ATTEMPT_NUMBER_FIELD_NUMBER: _ClassVar[int]
    VARIABLES_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    SOURCE_FIELD_NUMBER: _ClassVar[int]
    task_run_id: TaskRunIdPb
    task_def_id: TaskDefIdPb
    attempt_number: int
    variables: _containers.RepeatedCompositeFieldContainer[VarNameAndValPb]
    created_at: _timestamp_pb2.Timestamp
    source: TaskRunSourcePb
    def __init__(self, task_run_id: _Optional[_Union[TaskRunIdPb, _Mapping]] = ..., task_def_id: _Optional[_Union[TaskDefIdPb, _Mapping]] = ..., attempt_number: _Optional[int] = ..., variables: _Optional[_Iterable[_Union[VarNameAndValPb, _Mapping]]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., source: _Optional[_Union[TaskRunSourcePb, _Mapping]] = ...) -> None: ...

class TaskRunSourcePb(_message.Message):
    __slots__ = ["task_node", "user_task_trigger"]
    TASK_NODE_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_TRIGGER_FIELD_NUMBER: _ClassVar[int]
    task_node: TaskNodeReferencePb
    user_task_trigger: UserTaskTriggerReferencePb
    def __init__(self, task_node: _Optional[_Union[TaskNodeReferencePb, _Mapping]] = ..., user_task_trigger: _Optional[_Union[UserTaskTriggerReferencePb, _Mapping]] = ...) -> None: ...

class PollTaskReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: ScheduledTaskPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[ScheduledTaskPb, _Mapping]] = ...) -> None: ...

class ReportTaskRunPb(_message.Message):
    __slots__ = ["task_run_id", "time", "status", "output", "log_output", "attempt_number"]
    TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    TIME_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_FIELD_NUMBER: _ClassVar[int]
    LOG_OUTPUT_FIELD_NUMBER: _ClassVar[int]
    ATTEMPT_NUMBER_FIELD_NUMBER: _ClassVar[int]
    task_run_id: TaskRunIdPb
    time: _timestamp_pb2.Timestamp
    status: TaskStatusPb
    output: VariableValuePb
    log_output: VariableValuePb
    attempt_number: int
    def __init__(self, task_run_id: _Optional[_Union[TaskRunIdPb, _Mapping]] = ..., time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., status: _Optional[_Union[TaskStatusPb, str]] = ..., output: _Optional[_Union[VariableValuePb, _Mapping]] = ..., log_output: _Optional[_Union[VariableValuePb, _Mapping]] = ..., attempt_number: _Optional[int] = ...) -> None: ...

class ReportTaskReplyPb(_message.Message):
    __slots__ = ["code", "message"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ...) -> None: ...

class StopWfRunPb(_message.Message):
    __slots__ = ["wf_run_id", "thread_run_number"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    thread_run_number: int
    def __init__(self, wf_run_id: _Optional[str] = ..., thread_run_number: _Optional[int] = ...) -> None: ...

class StopWfRunReplyPb(_message.Message):
    __slots__ = ["code", "message"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ...) -> None: ...

class ResumeWfRunPb(_message.Message):
    __slots__ = ["wf_run_id", "thread_run_number"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    thread_run_number: int
    def __init__(self, wf_run_id: _Optional[str] = ..., thread_run_number: _Optional[int] = ...) -> None: ...

class ResumeWfRunReplyPb(_message.Message):
    __slots__ = ["code", "message"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ...) -> None: ...

class DeleteWfRunPb(_message.Message):
    __slots__ = ["wf_run_id"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    def __init__(self, wf_run_id: _Optional[str] = ...) -> None: ...

class DeleteTaskDefPb(_message.Message):
    __slots__ = ["name"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    def __init__(self, name: _Optional[str] = ...) -> None: ...

class DeleteUserTaskDefPb(_message.Message):
    __slots__ = ["name", "version"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    VERSION_FIELD_NUMBER: _ClassVar[int]
    name: str
    version: int
    def __init__(self, name: _Optional[str] = ..., version: _Optional[int] = ...) -> None: ...

class DeleteWfSpecPb(_message.Message):
    __slots__ = ["name", "version"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    VERSION_FIELD_NUMBER: _ClassVar[int]
    name: str
    version: int
    def __init__(self, name: _Optional[str] = ..., version: _Optional[int] = ...) -> None: ...

class DeleteExternalEventDefPb(_message.Message):
    __slots__ = ["name"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    def __init__(self, name: _Optional[str] = ...) -> None: ...

class DeleteObjectReplyPb(_message.Message):
    __slots__ = ["code", "message"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ...) -> None: ...

class HealthCheckPb(_message.Message):
    __slots__ = []
    def __init__(self) -> None: ...

class HealthCheckReplyPb(_message.Message):
    __slots__ = ["core_state", "timer_state"]
    CORE_STATE_FIELD_NUMBER: _ClassVar[int]
    TIMER_STATE_FIELD_NUMBER: _ClassVar[int]
    core_state: LHHealthResultPb
    timer_state: LHHealthResultPb
    def __init__(self, core_state: _Optional[_Union[LHHealthResultPb, str]] = ..., timer_state: _Optional[_Union[LHHealthResultPb, str]] = ...) -> None: ...

class TaskDefMetricsQueryPb(_message.Message):
    __slots__ = ["window_start", "window_type", "task_def_name"]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_TYPE_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    window_start: _timestamp_pb2.Timestamp
    window_type: MetricsWindowLengthPb
    task_def_name: str
    def __init__(self, window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., window_type: _Optional[_Union[MetricsWindowLengthPb, str]] = ..., task_def_name: _Optional[str] = ...) -> None: ...

class TaskDefMetricsReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: TaskDefMetricsPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[TaskDefMetricsPb, _Mapping]] = ...) -> None: ...

class ListTaskMetricsPb(_message.Message):
    __slots__ = ["last_window_start", "num_windows", "task_def_name", "window_length"]
    LAST_WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    NUM_WINDOWS_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    WINDOW_LENGTH_FIELD_NUMBER: _ClassVar[int]
    last_window_start: _timestamp_pb2.Timestamp
    num_windows: int
    task_def_name: str
    window_length: MetricsWindowLengthPb
    def __init__(self, last_window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., num_windows: _Optional[int] = ..., task_def_name: _Optional[str] = ..., window_length: _Optional[_Union[MetricsWindowLengthPb, str]] = ...) -> None: ...

class ListTaskMetricsReplyPb(_message.Message):
    __slots__ = ["code", "message", "results"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[TaskDefMetricsPb]
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[TaskDefMetricsPb, _Mapping]]] = ...) -> None: ...

class WfSpecMetricsQueryPb(_message.Message):
    __slots__ = ["window_start", "window_type", "wf_spec_name", "wf_spec_version"]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_TYPE_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_VERSION_FIELD_NUMBER: _ClassVar[int]
    window_start: _timestamp_pb2.Timestamp
    window_type: MetricsWindowLengthPb
    wf_spec_name: str
    wf_spec_version: int
    def __init__(self, window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., window_type: _Optional[_Union[MetricsWindowLengthPb, str]] = ..., wf_spec_name: _Optional[str] = ..., wf_spec_version: _Optional[int] = ...) -> None: ...

class WfSpecMetricsReplyPb(_message.Message):
    __slots__ = ["code", "message", "result"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    result: WfSpecMetricsPb
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., result: _Optional[_Union[WfSpecMetricsPb, _Mapping]] = ...) -> None: ...

class ListWfMetricsPb(_message.Message):
    __slots__ = ["last_window_start", "num_windows", "wf_spec_name", "wf_spec_version", "window_length"]
    LAST_WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    NUM_WINDOWS_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_VERSION_FIELD_NUMBER: _ClassVar[int]
    WINDOW_LENGTH_FIELD_NUMBER: _ClassVar[int]
    last_window_start: _timestamp_pb2.Timestamp
    num_windows: int
    wf_spec_name: str
    wf_spec_version: int
    window_length: MetricsWindowLengthPb
    def __init__(self, last_window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., num_windows: _Optional[int] = ..., wf_spec_name: _Optional[str] = ..., wf_spec_version: _Optional[int] = ..., window_length: _Optional[_Union[MetricsWindowLengthPb, str]] = ...) -> None: ...

class ListWfMetricsReplyPb(_message.Message):
    __slots__ = ["code", "message", "results"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    results: _containers.RepeatedCompositeFieldContainer[WfSpecMetricsPb]
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ..., results: _Optional[_Iterable[_Union[WfSpecMetricsPb, _Mapping]]] = ...) -> None: ...

class VariableValuePb(_message.Message):
    __slots__ = ["type", "json_obj", "json_arr", "double", "bool", "str", "int", "bytes"]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    JSON_OBJ_FIELD_NUMBER: _ClassVar[int]
    JSON_ARR_FIELD_NUMBER: _ClassVar[int]
    DOUBLE_FIELD_NUMBER: _ClassVar[int]
    BOOL_FIELD_NUMBER: _ClassVar[int]
    STR_FIELD_NUMBER: _ClassVar[int]
    INT_FIELD_NUMBER: _ClassVar[int]
    BYTES_FIELD_NUMBER: _ClassVar[int]
    type: VariableTypePb
    json_obj: str
    json_arr: str
    double: float
    bool: bool
    str: str
    int: int
    bytes: bytes
    def __init__(self, type: _Optional[_Union[VariableTypePb, str]] = ..., json_obj: _Optional[str] = ..., json_arr: _Optional[str] = ..., double: _Optional[float] = ..., bool: bool = ..., str: _Optional[str] = ..., int: _Optional[int] = ..., bytes: _Optional[bytes] = ...) -> None: ...

class VariableIdPb(_message.Message):
    __slots__ = ["wf_run_id", "thread_run_number", "name"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    thread_run_number: int
    name: str
    def __init__(self, wf_run_id: _Optional[str] = ..., thread_run_number: _Optional[int] = ..., name: _Optional[str] = ...) -> None: ...

class VariablePb(_message.Message):
    __slots__ = ["value", "wf_run_id", "thread_run_number", "name", "date"]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    DATE_FIELD_NUMBER: _ClassVar[int]
    value: VariableValuePb
    wf_run_id: str
    thread_run_number: int
    name: str
    date: _timestamp_pb2.Timestamp
    def __init__(self, value: _Optional[_Union[VariableValuePb, _Mapping]] = ..., wf_run_id: _Optional[str] = ..., thread_run_number: _Optional[int] = ..., name: _Optional[str] = ..., date: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class VarNameAndValPb(_message.Message):
    __slots__ = ["var_name", "value"]
    VAR_NAME_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    var_name: str
    value: VariableValuePb
    def __init__(self, var_name: _Optional[str] = ..., value: _Optional[_Union[VariableValuePb, _Mapping]] = ...) -> None: ...

class ExternalEventIdPb(_message.Message):
    __slots__ = ["wf_run_id", "external_event_def_name", "guid"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    GUID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    external_event_def_name: str
    guid: str
    def __init__(self, wf_run_id: _Optional[str] = ..., external_event_def_name: _Optional[str] = ..., guid: _Optional[str] = ...) -> None: ...

class ExternalEventPb(_message.Message):
    __slots__ = ["wf_run_id", "external_event_def_name", "guid", "created_at", "content", "thread_run_number", "node_run_position", "claimed"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    GUID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    NODE_RUN_POSITION_FIELD_NUMBER: _ClassVar[int]
    CLAIMED_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    external_event_def_name: str
    guid: str
    created_at: _timestamp_pb2.Timestamp
    content: VariableValuePb
    thread_run_number: int
    node_run_position: int
    claimed: bool
    def __init__(self, wf_run_id: _Optional[str] = ..., external_event_def_name: _Optional[str] = ..., guid: _Optional[str] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., content: _Optional[_Union[VariableValuePb, _Mapping]] = ..., thread_run_number: _Optional[int] = ..., node_run_position: _Optional[int] = ..., claimed: bool = ...) -> None: ...

class WfRunIdPb(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...

class WfRunPb(_message.Message):
    __slots__ = ["id", "wf_spec_name", "wf_spec_version", "status", "start_time", "end_time", "thread_runs", "pending_interrupts", "pending_failures"]
    ID_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_VERSION_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    START_TIME_FIELD_NUMBER: _ClassVar[int]
    END_TIME_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUNS_FIELD_NUMBER: _ClassVar[int]
    PENDING_INTERRUPTS_FIELD_NUMBER: _ClassVar[int]
    PENDING_FAILURES_FIELD_NUMBER: _ClassVar[int]
    id: str
    wf_spec_name: str
    wf_spec_version: int
    status: LHStatusPb
    start_time: _timestamp_pb2.Timestamp
    end_time: _timestamp_pb2.Timestamp
    thread_runs: _containers.RepeatedCompositeFieldContainer[ThreadRunPb]
    pending_interrupts: _containers.RepeatedCompositeFieldContainer[PendingInterruptPb]
    pending_failures: _containers.RepeatedCompositeFieldContainer[PendingFailureHandlerPb]
    def __init__(self, id: _Optional[str] = ..., wf_spec_name: _Optional[str] = ..., wf_spec_version: _Optional[int] = ..., status: _Optional[_Union[LHStatusPb, str]] = ..., start_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., end_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., thread_runs: _Optional[_Iterable[_Union[ThreadRunPb, _Mapping]]] = ..., pending_interrupts: _Optional[_Iterable[_Union[PendingInterruptPb, _Mapping]]] = ..., pending_failures: _Optional[_Iterable[_Union[PendingFailureHandlerPb, _Mapping]]] = ...) -> None: ...

class ThreadRunPb(_message.Message):
    __slots__ = ["wf_run_id", "number", "status", "wf_spec_name", "wf_spec_version", "thread_spec_name", "start_time", "end_time", "error_message", "child_thread_ids", "parent_thread_id", "halt_reasons", "interrupt_trigger_id", "failure_being_handled", "current_node_position", "handled_failed_children", "type"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    NUMBER_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_VERSION_FIELD_NUMBER: _ClassVar[int]
    THREAD_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    START_TIME_FIELD_NUMBER: _ClassVar[int]
    END_TIME_FIELD_NUMBER: _ClassVar[int]
    ERROR_MESSAGE_FIELD_NUMBER: _ClassVar[int]
    CHILD_THREAD_IDS_FIELD_NUMBER: _ClassVar[int]
    PARENT_THREAD_ID_FIELD_NUMBER: _ClassVar[int]
    HALT_REASONS_FIELD_NUMBER: _ClassVar[int]
    INTERRUPT_TRIGGER_ID_FIELD_NUMBER: _ClassVar[int]
    FAILURE_BEING_HANDLED_FIELD_NUMBER: _ClassVar[int]
    CURRENT_NODE_POSITION_FIELD_NUMBER: _ClassVar[int]
    HANDLED_FAILED_CHILDREN_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    number: int
    status: LHStatusPb
    wf_spec_name: str
    wf_spec_version: int
    thread_spec_name: str
    start_time: _timestamp_pb2.Timestamp
    end_time: _timestamp_pb2.Timestamp
    error_message: str
    child_thread_ids: _containers.RepeatedScalarFieldContainer[int]
    parent_thread_id: int
    halt_reasons: _containers.RepeatedCompositeFieldContainer[ThreadHaltReasonPb]
    interrupt_trigger_id: ExternalEventIdPb
    failure_being_handled: FailureBeingHandledPb
    current_node_position: int
    handled_failed_children: _containers.RepeatedScalarFieldContainer[int]
    type: ThreadTypePb
    def __init__(self, wf_run_id: _Optional[str] = ..., number: _Optional[int] = ..., status: _Optional[_Union[LHStatusPb, str]] = ..., wf_spec_name: _Optional[str] = ..., wf_spec_version: _Optional[int] = ..., thread_spec_name: _Optional[str] = ..., start_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., end_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., error_message: _Optional[str] = ..., child_thread_ids: _Optional[_Iterable[int]] = ..., parent_thread_id: _Optional[int] = ..., halt_reasons: _Optional[_Iterable[_Union[ThreadHaltReasonPb, _Mapping]]] = ..., interrupt_trigger_id: _Optional[_Union[ExternalEventIdPb, _Mapping]] = ..., failure_being_handled: _Optional[_Union[FailureBeingHandledPb, _Mapping]] = ..., current_node_position: _Optional[int] = ..., handled_failed_children: _Optional[_Iterable[int]] = ..., type: _Optional[_Union[ThreadTypePb, str]] = ...) -> None: ...

class FailureBeingHandledPb(_message.Message):
    __slots__ = ["thread_run_number", "node_run_position", "failure_number"]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    NODE_RUN_POSITION_FIELD_NUMBER: _ClassVar[int]
    FAILURE_NUMBER_FIELD_NUMBER: _ClassVar[int]
    thread_run_number: int
    node_run_position: int
    failure_number: int
    def __init__(self, thread_run_number: _Optional[int] = ..., node_run_position: _Optional[int] = ..., failure_number: _Optional[int] = ...) -> None: ...

class PendingInterruptPb(_message.Message):
    __slots__ = ["external_event_id", "handler_spec_name", "interrupted_thread_id"]
    EXTERNAL_EVENT_ID_FIELD_NUMBER: _ClassVar[int]
    HANDLER_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    INTERRUPTED_THREAD_ID_FIELD_NUMBER: _ClassVar[int]
    external_event_id: ExternalEventIdPb
    handler_spec_name: str
    interrupted_thread_id: int
    def __init__(self, external_event_id: _Optional[_Union[ExternalEventIdPb, _Mapping]] = ..., handler_spec_name: _Optional[str] = ..., interrupted_thread_id: _Optional[int] = ...) -> None: ...

class PendingFailureHandlerPb(_message.Message):
    __slots__ = ["failed_thread_run", "handler_spec_name"]
    FAILED_THREAD_RUN_FIELD_NUMBER: _ClassVar[int]
    HANDLER_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    failed_thread_run: int
    handler_spec_name: str
    def __init__(self, failed_thread_run: _Optional[int] = ..., handler_spec_name: _Optional[str] = ...) -> None: ...

class PendingInterruptHaltReasonPb(_message.Message):
    __slots__ = ["external_event_id"]
    EXTERNAL_EVENT_ID_FIELD_NUMBER: _ClassVar[int]
    external_event_id: ExternalEventIdPb
    def __init__(self, external_event_id: _Optional[_Union[ExternalEventIdPb, _Mapping]] = ...) -> None: ...

class PendingFailureHandlerHaltReasonPb(_message.Message):
    __slots__ = ["node_run_position"]
    NODE_RUN_POSITION_FIELD_NUMBER: _ClassVar[int]
    node_run_position: int
    def __init__(self, node_run_position: _Optional[int] = ...) -> None: ...

class HandlingFailureHaltReasonPb(_message.Message):
    __slots__ = ["handler_thread_id"]
    HANDLER_THREAD_ID_FIELD_NUMBER: _ClassVar[int]
    handler_thread_id: int
    def __init__(self, handler_thread_id: _Optional[int] = ...) -> None: ...

class ParentHaltedPb(_message.Message):
    __slots__ = ["parent_thread_id"]
    PARENT_THREAD_ID_FIELD_NUMBER: _ClassVar[int]
    parent_thread_id: int
    def __init__(self, parent_thread_id: _Optional[int] = ...) -> None: ...

class InterruptedPb(_message.Message):
    __slots__ = ["interrupt_thread_id"]
    INTERRUPT_THREAD_ID_FIELD_NUMBER: _ClassVar[int]
    interrupt_thread_id: int
    def __init__(self, interrupt_thread_id: _Optional[int] = ...) -> None: ...

class ManualHaltPb(_message.Message):
    __slots__ = ["meaning_of_life"]
    MEANING_OF_LIFE_FIELD_NUMBER: _ClassVar[int]
    meaning_of_life: bool
    def __init__(self, meaning_of_life: bool = ...) -> None: ...

class ThreadHaltReasonPb(_message.Message):
    __slots__ = ["parent_halted", "interrupted", "pending_interrupt", "pending_failure", "handling_failure", "manual_halt"]
    PARENT_HALTED_FIELD_NUMBER: _ClassVar[int]
    INTERRUPTED_FIELD_NUMBER: _ClassVar[int]
    PENDING_INTERRUPT_FIELD_NUMBER: _ClassVar[int]
    PENDING_FAILURE_FIELD_NUMBER: _ClassVar[int]
    HANDLING_FAILURE_FIELD_NUMBER: _ClassVar[int]
    MANUAL_HALT_FIELD_NUMBER: _ClassVar[int]
    parent_halted: ParentHaltedPb
    interrupted: InterruptedPb
    pending_interrupt: PendingInterruptHaltReasonPb
    pending_failure: PendingFailureHandlerHaltReasonPb
    handling_failure: HandlingFailureHaltReasonPb
    manual_halt: ManualHaltPb
    def __init__(self, parent_halted: _Optional[_Union[ParentHaltedPb, _Mapping]] = ..., interrupted: _Optional[_Union[InterruptedPb, _Mapping]] = ..., pending_interrupt: _Optional[_Union[PendingInterruptHaltReasonPb, _Mapping]] = ..., pending_failure: _Optional[_Union[PendingFailureHandlerHaltReasonPb, _Mapping]] = ..., handling_failure: _Optional[_Union[HandlingFailureHaltReasonPb, _Mapping]] = ..., manual_halt: _Optional[_Union[ManualHaltPb, _Mapping]] = ...) -> None: ...

class NodeRunIdPb(_message.Message):
    __slots__ = ["wf_run_id", "thread_run_number", "position"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    POSITION_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    thread_run_number: int
    position: int
    def __init__(self, wf_run_id: _Optional[str] = ..., thread_run_number: _Optional[int] = ..., position: _Optional[int] = ...) -> None: ...

class TaskNodeRunPb(_message.Message):
    __slots__ = ["task_run_id"]
    TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    task_run_id: TaskRunIdPb
    def __init__(self, task_run_id: _Optional[_Union[TaskRunIdPb, _Mapping]] = ...) -> None: ...

class UserTaskNodeRunPb(_message.Message):
    __slots__ = ["user_task_run_id"]
    USER_TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    user_task_run_id: UserTaskRunIdPb
    def __init__(self, user_task_run_id: _Optional[_Union[UserTaskRunIdPb, _Mapping]] = ...) -> None: ...

class NodeRunPb(_message.Message):
    __slots__ = ["wf_run_id", "thread_run_number", "position", "status", "arrival_time", "end_time", "wf_spec_id", "thread_spec_name", "node_name", "error_message", "failures", "task", "external_event", "entrypoint", "exit", "start_thread", "wait_threads", "sleep", "user_task", "failure_handler_ids"]
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
    FAILURE_HANDLER_IDS_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    thread_run_number: int
    position: int
    status: LHStatusPb
    arrival_time: _timestamp_pb2.Timestamp
    end_time: _timestamp_pb2.Timestamp
    wf_spec_id: WfSpecIdPb
    thread_spec_name: str
    node_name: str
    error_message: str
    failures: _containers.RepeatedCompositeFieldContainer[FailurePb]
    task: TaskNodeRunPb
    external_event: ExternalEventRunPb
    entrypoint: EntrypointRunPb
    exit: ExitRunPb
    start_thread: StartThreadRunPb
    wait_threads: WaitForThreadsRunPb
    sleep: SleepNodeRunPb
    user_task: UserTaskNodeRunPb
    failure_handler_ids: _containers.RepeatedScalarFieldContainer[int]
    def __init__(self, wf_run_id: _Optional[str] = ..., thread_run_number: _Optional[int] = ..., position: _Optional[int] = ..., status: _Optional[_Union[LHStatusPb, str]] = ..., arrival_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., end_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., wf_spec_id: _Optional[_Union[WfSpecIdPb, _Mapping]] = ..., thread_spec_name: _Optional[str] = ..., node_name: _Optional[str] = ..., error_message: _Optional[str] = ..., failures: _Optional[_Iterable[_Union[FailurePb, _Mapping]]] = ..., task: _Optional[_Union[TaskNodeRunPb, _Mapping]] = ..., external_event: _Optional[_Union[ExternalEventRunPb, _Mapping]] = ..., entrypoint: _Optional[_Union[EntrypointRunPb, _Mapping]] = ..., exit: _Optional[_Union[ExitRunPb, _Mapping]] = ..., start_thread: _Optional[_Union[StartThreadRunPb, _Mapping]] = ..., wait_threads: _Optional[_Union[WaitForThreadsRunPb, _Mapping]] = ..., sleep: _Optional[_Union[SleepNodeRunPb, _Mapping]] = ..., user_task: _Optional[_Union[UserTaskNodeRunPb, _Mapping]] = ..., failure_handler_ids: _Optional[_Iterable[int]] = ...) -> None: ...

class FailurePb(_message.Message):
    __slots__ = ["failure_name", "message", "content"]
    FAILURE_NAME_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    failure_name: str
    message: str
    content: VariableValuePb
    def __init__(self, failure_name: _Optional[str] = ..., message: _Optional[str] = ..., content: _Optional[_Union[VariableValuePb, _Mapping]] = ...) -> None: ...

class TaskAttemptPb(_message.Message):
    __slots__ = ["output", "log_output", "schedule_time", "start_time", "end_time", "task_worker_id", "task_worker_version", "status"]
    OUTPUT_FIELD_NUMBER: _ClassVar[int]
    LOG_OUTPUT_FIELD_NUMBER: _ClassVar[int]
    SCHEDULE_TIME_FIELD_NUMBER: _ClassVar[int]
    START_TIME_FIELD_NUMBER: _ClassVar[int]
    END_TIME_FIELD_NUMBER: _ClassVar[int]
    TASK_WORKER_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_WORKER_VERSION_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    output: VariableValuePb
    log_output: VariableValuePb
    schedule_time: _timestamp_pb2.Timestamp
    start_time: _timestamp_pb2.Timestamp
    end_time: _timestamp_pb2.Timestamp
    task_worker_id: str
    task_worker_version: str
    status: TaskStatusPb
    def __init__(self, output: _Optional[_Union[VariableValuePb, _Mapping]] = ..., log_output: _Optional[_Union[VariableValuePb, _Mapping]] = ..., schedule_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., start_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., end_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., task_worker_id: _Optional[str] = ..., task_worker_version: _Optional[str] = ..., status: _Optional[_Union[TaskStatusPb, str]] = ...) -> None: ...

class TaskRunIdPb(_message.Message):
    __slots__ = ["wf_run_id", "task_guid"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_GUID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    task_guid: str
    def __init__(self, wf_run_id: _Optional[str] = ..., task_guid: _Optional[str] = ...) -> None: ...

class TaskRunPb(_message.Message):
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
    id: TaskRunIdPb
    attempts: _containers.RepeatedCompositeFieldContainer[TaskAttemptPb]
    max_attempts: int
    task_def_name: str
    input_variables: _containers.RepeatedCompositeFieldContainer[VarNameAndValPb]
    source: TaskRunSourcePb
    scheduled_at: _timestamp_pb2.Timestamp
    status: TaskStatusPb
    timeout_seconds: int
    def __init__(self, id: _Optional[_Union[TaskRunIdPb, _Mapping]] = ..., attempts: _Optional[_Iterable[_Union[TaskAttemptPb, _Mapping]]] = ..., max_attempts: _Optional[int] = ..., task_def_name: _Optional[str] = ..., input_variables: _Optional[_Iterable[_Union[VarNameAndValPb, _Mapping]]] = ..., source: _Optional[_Union[TaskRunSourcePb, _Mapping]] = ..., scheduled_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., status: _Optional[_Union[TaskStatusPb, str]] = ..., timeout_seconds: _Optional[int] = ...) -> None: ...

class TaskNodeReferencePb(_message.Message):
    __slots__ = ["node_run_id", "wf_spec_id"]
    NODE_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    node_run_id: NodeRunIdPb
    wf_spec_id: WfSpecIdPb
    def __init__(self, node_run_id: _Optional[_Union[NodeRunIdPb, _Mapping]] = ..., wf_spec_id: _Optional[_Union[WfSpecIdPb, _Mapping]] = ...) -> None: ...

class UserTaskTriggerReferencePb(_message.Message):
    __slots__ = ["node_run_id", "user_task_event_number", "wf_spec_id"]
    NODE_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_EVENT_NUMBER_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    node_run_id: NodeRunIdPb
    user_task_event_number: int
    wf_spec_id: WfSpecIdPb
    def __init__(self, node_run_id: _Optional[_Union[NodeRunIdPb, _Mapping]] = ..., user_task_event_number: _Optional[int] = ..., wf_spec_id: _Optional[_Union[WfSpecIdPb, _Mapping]] = ...) -> None: ...

class EntrypointRunPb(_message.Message):
    __slots__ = []
    def __init__(self) -> None: ...

class ExitRunPb(_message.Message):
    __slots__ = []
    def __init__(self) -> None: ...

class StartThreadRunPb(_message.Message):
    __slots__ = ["child_thread_id", "thread_spec_name"]
    CHILD_THREAD_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    child_thread_id: int
    thread_spec_name: str
    def __init__(self, child_thread_id: _Optional[int] = ..., thread_spec_name: _Optional[str] = ...) -> None: ...

class WaitForThreadsRunPb(_message.Message):
    __slots__ = ["threads"]
    class WaitForThreadPb(_message.Message):
        __slots__ = ["thread_end_time", "thread_status", "thread_run_number"]
        THREAD_END_TIME_FIELD_NUMBER: _ClassVar[int]
        THREAD_STATUS_FIELD_NUMBER: _ClassVar[int]
        THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
        thread_end_time: _timestamp_pb2.Timestamp
        thread_status: LHStatusPb
        thread_run_number: int
        def __init__(self, thread_end_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., thread_status: _Optional[_Union[LHStatusPb, str]] = ..., thread_run_number: _Optional[int] = ...) -> None: ...
    THREADS_FIELD_NUMBER: _ClassVar[int]
    threads: _containers.RepeatedCompositeFieldContainer[WaitForThreadsRunPb.WaitForThreadPb]
    def __init__(self, threads: _Optional[_Iterable[_Union[WaitForThreadsRunPb.WaitForThreadPb, _Mapping]]] = ...) -> None: ...

class ExternalEventRunPb(_message.Message):
    __slots__ = ["external_event_def_name", "event_time", "external_event_id"]
    EXTERNAL_EVENT_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    EVENT_TIME_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_ID_FIELD_NUMBER: _ClassVar[int]
    external_event_def_name: str
    event_time: _timestamp_pb2.Timestamp
    external_event_id: ExternalEventIdPb
    def __init__(self, external_event_def_name: _Optional[str] = ..., event_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., external_event_id: _Optional[_Union[ExternalEventIdPb, _Mapping]] = ...) -> None: ...

class SleepNodeRunPb(_message.Message):
    __slots__ = ["maturation_time"]
    MATURATION_TIME_FIELD_NUMBER: _ClassVar[int]
    maturation_time: _timestamp_pb2.Timestamp
    def __init__(self, maturation_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class EdgeConditionPb(_message.Message):
    __slots__ = ["comparator", "left", "right"]
    COMPARATOR_FIELD_NUMBER: _ClassVar[int]
    LEFT_FIELD_NUMBER: _ClassVar[int]
    RIGHT_FIELD_NUMBER: _ClassVar[int]
    comparator: ComparatorPb
    left: VariableAssignmentPb
    right: VariableAssignmentPb
    def __init__(self, comparator: _Optional[_Union[ComparatorPb, str]] = ..., left: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ..., right: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ...) -> None: ...

class EdgePb(_message.Message):
    __slots__ = ["sink_node_name", "condition"]
    SINK_NODE_NAME_FIELD_NUMBER: _ClassVar[int]
    CONDITION_FIELD_NUMBER: _ClassVar[int]
    sink_node_name: str
    condition: EdgeConditionPb
    def __init__(self, sink_node_name: _Optional[str] = ..., condition: _Optional[_Union[EdgeConditionPb, _Mapping]] = ...) -> None: ...

class VariableAssignmentPb(_message.Message):
    __slots__ = ["json_path", "variable_name", "literal_value", "format_string"]
    class FormatStringPb(_message.Message):
        __slots__ = ["format", "args"]
        FORMAT_FIELD_NUMBER: _ClassVar[int]
        ARGS_FIELD_NUMBER: _ClassVar[int]
        format: VariableAssignmentPb
        args: _containers.RepeatedCompositeFieldContainer[VariableAssignmentPb]
        def __init__(self, format: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ..., args: _Optional[_Iterable[_Union[VariableAssignmentPb, _Mapping]]] = ...) -> None: ...
    JSON_PATH_FIELD_NUMBER: _ClassVar[int]
    VARIABLE_NAME_FIELD_NUMBER: _ClassVar[int]
    LITERAL_VALUE_FIELD_NUMBER: _ClassVar[int]
    FORMAT_STRING_FIELD_NUMBER: _ClassVar[int]
    json_path: str
    variable_name: str
    literal_value: VariableValuePb
    format_string: VariableAssignmentPb.FormatStringPb
    def __init__(self, json_path: _Optional[str] = ..., variable_name: _Optional[str] = ..., literal_value: _Optional[_Union[VariableValuePb, _Mapping]] = ..., format_string: _Optional[_Union[VariableAssignmentPb.FormatStringPb, _Mapping]] = ...) -> None: ...

class VariableMutationPb(_message.Message):
    __slots__ = ["lhs_name", "lhs_json_path", "operation", "source_variable", "literal_value", "node_output"]
    class NodeOutputSourcePb(_message.Message):
        __slots__ = ["jsonpath"]
        JSONPATH_FIELD_NUMBER: _ClassVar[int]
        jsonpath: str
        def __init__(self, jsonpath: _Optional[str] = ...) -> None: ...
    LHS_NAME_FIELD_NUMBER: _ClassVar[int]
    LHS_JSON_PATH_FIELD_NUMBER: _ClassVar[int]
    OPERATION_FIELD_NUMBER: _ClassVar[int]
    SOURCE_VARIABLE_FIELD_NUMBER: _ClassVar[int]
    LITERAL_VALUE_FIELD_NUMBER: _ClassVar[int]
    NODE_OUTPUT_FIELD_NUMBER: _ClassVar[int]
    lhs_name: str
    lhs_json_path: str
    operation: VariableMutationTypePb
    source_variable: VariableAssignmentPb
    literal_value: VariableValuePb
    node_output: VariableMutationPb.NodeOutputSourcePb
    def __init__(self, lhs_name: _Optional[str] = ..., lhs_json_path: _Optional[str] = ..., operation: _Optional[_Union[VariableMutationTypePb, str]] = ..., source_variable: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ..., literal_value: _Optional[_Union[VariableValuePb, _Mapping]] = ..., node_output: _Optional[_Union[VariableMutationPb.NodeOutputSourcePb, _Mapping]] = ...) -> None: ...

class NopNodePb(_message.Message):
    __slots__ = []
    def __init__(self) -> None: ...

class SleepNodePb(_message.Message):
    __slots__ = ["raw_seconds", "timestamp", "iso_date"]
    RAW_SECONDS_FIELD_NUMBER: _ClassVar[int]
    TIMESTAMP_FIELD_NUMBER: _ClassVar[int]
    ISO_DATE_FIELD_NUMBER: _ClassVar[int]
    raw_seconds: VariableAssignmentPb
    timestamp: VariableAssignmentPb
    iso_date: VariableAssignmentPb
    def __init__(self, raw_seconds: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ..., timestamp: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ..., iso_date: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ...) -> None: ...

class NodePb(_message.Message):
    __slots__ = ["outgoing_edges", "variable_mutations", "failure_handlers", "entrypoint", "exit", "task", "external_event", "start_thread", "wait_for_threads", "nop", "sleep", "user_task"]
    OUTGOING_EDGES_FIELD_NUMBER: _ClassVar[int]
    VARIABLE_MUTATIONS_FIELD_NUMBER: _ClassVar[int]
    FAILURE_HANDLERS_FIELD_NUMBER: _ClassVar[int]
    ENTRYPOINT_FIELD_NUMBER: _ClassVar[int]
    EXIT_FIELD_NUMBER: _ClassVar[int]
    TASK_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_FIELD_NUMBER: _ClassVar[int]
    START_THREAD_FIELD_NUMBER: _ClassVar[int]
    WAIT_FOR_THREADS_FIELD_NUMBER: _ClassVar[int]
    NOP_FIELD_NUMBER: _ClassVar[int]
    SLEEP_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_FIELD_NUMBER: _ClassVar[int]
    outgoing_edges: _containers.RepeatedCompositeFieldContainer[EdgePb]
    variable_mutations: _containers.RepeatedCompositeFieldContainer[VariableMutationPb]
    failure_handlers: _containers.RepeatedCompositeFieldContainer[FailureHandlerDefPb]
    entrypoint: EntrypointNodePb
    exit: ExitNodePb
    task: TaskNodePb
    external_event: ExternalEventNodePb
    start_thread: StartThreadNodePb
    wait_for_threads: WaitForThreadsNodePb
    nop: NopNodePb
    sleep: SleepNodePb
    user_task: UserTaskNodePb
    def __init__(self, outgoing_edges: _Optional[_Iterable[_Union[EdgePb, _Mapping]]] = ..., variable_mutations: _Optional[_Iterable[_Union[VariableMutationPb, _Mapping]]] = ..., failure_handlers: _Optional[_Iterable[_Union[FailureHandlerDefPb, _Mapping]]] = ..., entrypoint: _Optional[_Union[EntrypointNodePb, _Mapping]] = ..., exit: _Optional[_Union[ExitNodePb, _Mapping]] = ..., task: _Optional[_Union[TaskNodePb, _Mapping]] = ..., external_event: _Optional[_Union[ExternalEventNodePb, _Mapping]] = ..., start_thread: _Optional[_Union[StartThreadNodePb, _Mapping]] = ..., wait_for_threads: _Optional[_Union[WaitForThreadsNodePb, _Mapping]] = ..., nop: _Optional[_Union[NopNodePb, _Mapping]] = ..., sleep: _Optional[_Union[SleepNodePb, _Mapping]] = ..., user_task: _Optional[_Union[UserTaskNodePb, _Mapping]] = ...) -> None: ...

class UserTaskFieldPb(_message.Message):
    __slots__ = ["name", "type", "description", "display_name"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    DISPLAY_NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    type: VariableTypePb
    description: str
    display_name: str
    def __init__(self, name: _Optional[str] = ..., type: _Optional[_Union[VariableTypePb, str]] = ..., description: _Optional[str] = ..., display_name: _Optional[str] = ...) -> None: ...

class UserTaskDefIdPb(_message.Message):
    __slots__ = ["name", "version"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    VERSION_FIELD_NUMBER: _ClassVar[int]
    name: str
    version: int
    def __init__(self, name: _Optional[str] = ..., version: _Optional[int] = ...) -> None: ...

class UserTaskDefPb(_message.Message):
    __slots__ = ["name", "version", "description", "fields", "created_at"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    VERSION_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    name: str
    version: int
    description: str
    fields: _containers.RepeatedCompositeFieldContainer[UserTaskFieldPb]
    created_at: _timestamp_pb2.Timestamp
    def __init__(self, name: _Optional[str] = ..., version: _Optional[int] = ..., description: _Optional[str] = ..., fields: _Optional[_Iterable[_Union[UserTaskFieldPb, _Mapping]]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class UTActionTriggerPb(_message.Message):
    __slots__ = ["task", "cancel", "reassign", "delay_seconds", "hook"]
    class UTHook(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
        __slots__ = []
        ON_ARRIVAL: _ClassVar[UTActionTriggerPb.UTHook]
        ON_TASK_ASSIGNED: _ClassVar[UTActionTriggerPb.UTHook]
    ON_ARRIVAL: UTActionTriggerPb.UTHook
    ON_TASK_ASSIGNED: UTActionTriggerPb.UTHook
    class UTACancelPb(_message.Message):
        __slots__ = []
        def __init__(self) -> None: ...
    class UTATaskPb(_message.Message):
        __slots__ = ["task", "mutations"]
        TASK_FIELD_NUMBER: _ClassVar[int]
        MUTATIONS_FIELD_NUMBER: _ClassVar[int]
        task: TaskNodePb
        mutations: _containers.RepeatedCompositeFieldContainer[VariableMutationPb]
        def __init__(self, task: _Optional[_Union[TaskNodePb, _Mapping]] = ..., mutations: _Optional[_Iterable[_Union[VariableMutationPb, _Mapping]]] = ...) -> None: ...
    class UTAReassignPb(_message.Message):
        __slots__ = ["user_id", "user_group"]
        USER_ID_FIELD_NUMBER: _ClassVar[int]
        USER_GROUP_FIELD_NUMBER: _ClassVar[int]
        user_id: VariableAssignmentPb
        user_group: VariableAssignmentPb
        def __init__(self, user_id: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ..., user_group: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ...) -> None: ...
    TASK_FIELD_NUMBER: _ClassVar[int]
    CANCEL_FIELD_NUMBER: _ClassVar[int]
    REASSIGN_FIELD_NUMBER: _ClassVar[int]
    DELAY_SECONDS_FIELD_NUMBER: _ClassVar[int]
    HOOK_FIELD_NUMBER: _ClassVar[int]
    task: UTActionTriggerPb.UTATaskPb
    cancel: UTActionTriggerPb.UTACancelPb
    reassign: UTActionTriggerPb.UTAReassignPb
    delay_seconds: VariableAssignmentPb
    hook: UTActionTriggerPb.UTHook
    def __init__(self, task: _Optional[_Union[UTActionTriggerPb.UTATaskPb, _Mapping]] = ..., cancel: _Optional[_Union[UTActionTriggerPb.UTACancelPb, _Mapping]] = ..., reassign: _Optional[_Union[UTActionTriggerPb.UTAReassignPb, _Mapping]] = ..., delay_seconds: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ..., hook: _Optional[_Union[UTActionTriggerPb.UTHook, str]] = ...) -> None: ...

class UserTaskNodePb(_message.Message):
    __slots__ = ["user_task_def_name", "user_group", "user_id", "actions", "user_task_def_version", "notes"]
    USER_TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    USER_GROUP_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    ACTIONS_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_DEF_VERSION_FIELD_NUMBER: _ClassVar[int]
    NOTES_FIELD_NUMBER: _ClassVar[int]
    user_task_def_name: str
    user_group: VariableAssignmentPb
    user_id: VariableAssignmentPb
    actions: _containers.RepeatedCompositeFieldContainer[UTActionTriggerPb]
    user_task_def_version: int
    notes: VariableAssignmentPb
    def __init__(self, user_task_def_name: _Optional[str] = ..., user_group: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ..., user_id: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ..., actions: _Optional[_Iterable[_Union[UTActionTriggerPb, _Mapping]]] = ..., user_task_def_version: _Optional[int] = ..., notes: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ...) -> None: ...

class UserTaskEventPb(_message.Message):
    __slots__ = ["time", "task_executed", "reassigned", "cancelled"]
    class UTECancelledPb(_message.Message):
        __slots__ = []
        def __init__(self) -> None: ...
    class UTETaskExecutedPb(_message.Message):
        __slots__ = ["task_run"]
        TASK_RUN_FIELD_NUMBER: _ClassVar[int]
        task_run: TaskRunIdPb
        def __init__(self, task_run: _Optional[_Union[TaskRunIdPb, _Mapping]] = ...) -> None: ...
    class UTEReassignedPb(_message.Message):
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
    REASSIGNED_FIELD_NUMBER: _ClassVar[int]
    CANCELLED_FIELD_NUMBER: _ClassVar[int]
    time: _timestamp_pb2.Timestamp
    task_executed: UserTaskEventPb.UTETaskExecutedPb
    reassigned: UserTaskEventPb.UTEReassignedPb
    cancelled: UserTaskEventPb.UTECancelledPb
    def __init__(self, time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., task_executed: _Optional[_Union[UserTaskEventPb.UTETaskExecutedPb, _Mapping]] = ..., reassigned: _Optional[_Union[UserTaskEventPb.UTEReassignedPb, _Mapping]] = ..., cancelled: _Optional[_Union[UserTaskEventPb.UTECancelledPb, _Mapping]] = ...) -> None: ...

class UserTaskRunIdPb(_message.Message):
    __slots__ = ["wf_run_id", "user_task_guid"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_GUID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    user_task_guid: str
    def __init__(self, wf_run_id: _Optional[str] = ..., user_task_guid: _Optional[str] = ...) -> None: ...

class UserTaskRunPb(_message.Message):
    __slots__ = ["id", "user_task_def_id", "specific_user_id", "user_group", "claimed_by_user_id", "results", "status", "events", "notes", "scheduled_time", "node_run_id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    SPECIFIC_USER_ID_FIELD_NUMBER: _ClassVar[int]
    USER_GROUP_FIELD_NUMBER: _ClassVar[int]
    CLAIMED_BY_USER_ID_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    EVENTS_FIELD_NUMBER: _ClassVar[int]
    NOTES_FIELD_NUMBER: _ClassVar[int]
    SCHEDULED_TIME_FIELD_NUMBER: _ClassVar[int]
    NODE_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    id: UserTaskRunIdPb
    user_task_def_id: UserTaskDefIdPb
    specific_user_id: str
    user_group: str
    claimed_by_user_id: str
    results: _containers.RepeatedCompositeFieldContainer[UserTaskFieldResultPb]
    status: UserTaskRunStatusPb
    events: _containers.RepeatedCompositeFieldContainer[UserTaskEventPb]
    notes: str
    scheduled_time: _timestamp_pb2.Timestamp
    node_run_id: NodeRunIdPb
    def __init__(self, id: _Optional[_Union[UserTaskRunIdPb, _Mapping]] = ..., user_task_def_id: _Optional[_Union[UserTaskDefIdPb, _Mapping]] = ..., specific_user_id: _Optional[str] = ..., user_group: _Optional[str] = ..., claimed_by_user_id: _Optional[str] = ..., results: _Optional[_Iterable[_Union[UserTaskFieldResultPb, _Mapping]]] = ..., status: _Optional[_Union[UserTaskRunStatusPb, str]] = ..., events: _Optional[_Iterable[_Union[UserTaskEventPb, _Mapping]]] = ..., notes: _Optional[str] = ..., scheduled_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., node_run_id: _Optional[_Union[NodeRunIdPb, _Mapping]] = ...) -> None: ...

class AssignUserTaskRunPb(_message.Message):
    __slots__ = ["user_task_run_id", "override_claim", "user_id", "user_group"]
    USER_TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    OVERRIDE_CLAIM_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    USER_GROUP_FIELD_NUMBER: _ClassVar[int]
    user_task_run_id: UserTaskRunIdPb
    override_claim: bool
    user_id: str
    user_group: str
    def __init__(self, user_task_run_id: _Optional[_Union[UserTaskRunIdPb, _Mapping]] = ..., override_claim: bool = ..., user_id: _Optional[str] = ..., user_group: _Optional[str] = ...) -> None: ...

class AssignUserTaskRunReplyPb(_message.Message):
    __slots__ = ["code", "message"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ...) -> None: ...

class CompleteUserTaskRunReplyPb(_message.Message):
    __slots__ = ["code", "message"]
    CODE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    code: LHResponseCodePb
    message: str
    def __init__(self, code: _Optional[_Union[LHResponseCodePb, str]] = ..., message: _Optional[str] = ...) -> None: ...

class UserTaskFieldResultPb(_message.Message):
    __slots__ = ["name", "value"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    name: str
    value: VariableValuePb
    def __init__(self, name: _Optional[str] = ..., value: _Optional[_Union[VariableValuePb, _Mapping]] = ...) -> None: ...

class UserTaskResultPb(_message.Message):
    __slots__ = ["fields"]
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    fields: _containers.RepeatedCompositeFieldContainer[UserTaskFieldResultPb]
    def __init__(self, fields: _Optional[_Iterable[_Union[UserTaskFieldResultPb, _Mapping]]] = ...) -> None: ...

class SaveUserTaskRunPb(_message.Message):
    __slots__ = ["result", "user_id", "results"]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    result: UserTaskResultPb
    user_id: str
    results: _containers.RepeatedCompositeFieldContainer[UserTaskFieldResultPb]
    def __init__(self, result: _Optional[_Union[UserTaskResultPb, _Mapping]] = ..., user_id: _Optional[str] = ..., results: _Optional[_Iterable[_Union[UserTaskFieldResultPb, _Mapping]]] = ...) -> None: ...

class CompleteUserTaskRunPb(_message.Message):
    __slots__ = ["user_task_run_id", "result", "user_id"]
    USER_TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    user_task_run_id: UserTaskRunIdPb
    result: UserTaskResultPb
    user_id: str
    def __init__(self, user_task_run_id: _Optional[_Union[UserTaskRunIdPb, _Mapping]] = ..., result: _Optional[_Union[UserTaskResultPb, _Mapping]] = ..., user_id: _Optional[str] = ...) -> None: ...

class StartThreadNodePb(_message.Message):
    __slots__ = ["thread_spec_name", "variables"]
    class VariablesEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: VariableAssignmentPb
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ...) -> None: ...
    THREAD_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    VARIABLES_FIELD_NUMBER: _ClassVar[int]
    thread_spec_name: str
    variables: _containers.MessageMap[str, VariableAssignmentPb]
    def __init__(self, thread_spec_name: _Optional[str] = ..., variables: _Optional[_Mapping[str, VariableAssignmentPb]] = ...) -> None: ...

class FailureHandlerDefPb(_message.Message):
    __slots__ = ["specific_failure", "handler_spec_name"]
    SPECIFIC_FAILURE_FIELD_NUMBER: _ClassVar[int]
    HANDLER_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    specific_failure: str
    handler_spec_name: str
    def __init__(self, specific_failure: _Optional[str] = ..., handler_spec_name: _Optional[str] = ...) -> None: ...

class WaitForThreadsNodePb(_message.Message):
    __slots__ = ["threads"]
    class ThreadToWaitForPb(_message.Message):
        __slots__ = ["thread_run_number"]
        THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
        thread_run_number: VariableAssignmentPb
        def __init__(self, thread_run_number: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ...) -> None: ...
    THREADS_FIELD_NUMBER: _ClassVar[int]
    threads: _containers.RepeatedCompositeFieldContainer[WaitForThreadsNodePb.ThreadToWaitForPb]
    def __init__(self, threads: _Optional[_Iterable[_Union[WaitForThreadsNodePb.ThreadToWaitForPb, _Mapping]]] = ...) -> None: ...

class TaskNodePb(_message.Message):
    __slots__ = ["task_def_name", "timeout_seconds", "retries", "variables"]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    TIMEOUT_SECONDS_FIELD_NUMBER: _ClassVar[int]
    RETRIES_FIELD_NUMBER: _ClassVar[int]
    VARIABLES_FIELD_NUMBER: _ClassVar[int]
    task_def_name: str
    timeout_seconds: int
    retries: int
    variables: _containers.RepeatedCompositeFieldContainer[VariableAssignmentPb]
    def __init__(self, task_def_name: _Optional[str] = ..., timeout_seconds: _Optional[int] = ..., retries: _Optional[int] = ..., variables: _Optional[_Iterable[_Union[VariableAssignmentPb, _Mapping]]] = ...) -> None: ...

class ExternalEventNodePb(_message.Message):
    __slots__ = ["external_event_def_name", "timeout_seconds"]
    EXTERNAL_EVENT_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    TIMEOUT_SECONDS_FIELD_NUMBER: _ClassVar[int]
    external_event_def_name: str
    timeout_seconds: VariableAssignmentPb
    def __init__(self, external_event_def_name: _Optional[str] = ..., timeout_seconds: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ...) -> None: ...

class EntrypointNodePb(_message.Message):
    __slots__ = []
    def __init__(self) -> None: ...

class ExitNodePb(_message.Message):
    __slots__ = ["failure_def"]
    FAILURE_DEF_FIELD_NUMBER: _ClassVar[int]
    failure_def: FailureDefPb
    def __init__(self, failure_def: _Optional[_Union[FailureDefPb, _Mapping]] = ...) -> None: ...

class FailureDefPb(_message.Message):
    __slots__ = ["failure_name", "message", "content"]
    FAILURE_NAME_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    failure_name: str
    message: str
    content: VariableAssignmentPb
    def __init__(self, failure_name: _Optional[str] = ..., message: _Optional[str] = ..., content: _Optional[_Union[VariableAssignmentPb, _Mapping]] = ...) -> None: ...

class VariableDefPb(_message.Message):
    __slots__ = ["type", "name", "index_type", "json_indexes", "default_value"]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    INDEX_TYPE_FIELD_NUMBER: _ClassVar[int]
    JSON_INDEXES_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_VALUE_FIELD_NUMBER: _ClassVar[int]
    type: VariableTypePb
    name: str
    index_type: IndexTypePb
    json_indexes: _containers.RepeatedCompositeFieldContainer[JsonIndexPb]
    default_value: VariableValuePb
    def __init__(self, type: _Optional[_Union[VariableTypePb, str]] = ..., name: _Optional[str] = ..., index_type: _Optional[_Union[IndexTypePb, str]] = ..., json_indexes: _Optional[_Iterable[_Union[JsonIndexPb, _Mapping]]] = ..., default_value: _Optional[_Union[VariableValuePb, _Mapping]] = ...) -> None: ...

class JsonIndexPb(_message.Message):
    __slots__ = ["path", "index_type"]
    PATH_FIELD_NUMBER: _ClassVar[int]
    INDEX_TYPE_FIELD_NUMBER: _ClassVar[int]
    path: str
    index_type: IndexTypePb
    def __init__(self, path: _Optional[str] = ..., index_type: _Optional[_Union[IndexTypePb, str]] = ...) -> None: ...

class InterruptDefPb(_message.Message):
    __slots__ = ["external_event_def_name", "handler_spec_name"]
    EXTERNAL_EVENT_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    HANDLER_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    external_event_def_name: str
    handler_spec_name: str
    def __init__(self, external_event_def_name: _Optional[str] = ..., handler_spec_name: _Optional[str] = ...) -> None: ...

class ThreadSpecPb(_message.Message):
    __slots__ = ["nodes", "variable_defs", "interrupt_defs"]
    class NodesEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: NodePb
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[NodePb, _Mapping]] = ...) -> None: ...
    NODES_FIELD_NUMBER: _ClassVar[int]
    VARIABLE_DEFS_FIELD_NUMBER: _ClassVar[int]
    INTERRUPT_DEFS_FIELD_NUMBER: _ClassVar[int]
    nodes: _containers.MessageMap[str, NodePb]
    variable_defs: _containers.RepeatedCompositeFieldContainer[VariableDefPb]
    interrupt_defs: _containers.RepeatedCompositeFieldContainer[InterruptDefPb]
    def __init__(self, nodes: _Optional[_Mapping[str, NodePb]] = ..., variable_defs: _Optional[_Iterable[_Union[VariableDefPb, _Mapping]]] = ..., interrupt_defs: _Optional[_Iterable[_Union[InterruptDefPb, _Mapping]]] = ...) -> None: ...

class WfSpecPb(_message.Message):
    __slots__ = ["name", "version", "created_at", "status", "thread_specs", "entrypoint_thread_name", "retention_hours"]
    class ThreadSpecsEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: ThreadSpecPb
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[ThreadSpecPb, _Mapping]] = ...) -> None: ...
    NAME_FIELD_NUMBER: _ClassVar[int]
    VERSION_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    THREAD_SPECS_FIELD_NUMBER: _ClassVar[int]
    ENTRYPOINT_THREAD_NAME_FIELD_NUMBER: _ClassVar[int]
    RETENTION_HOURS_FIELD_NUMBER: _ClassVar[int]
    name: str
    version: int
    created_at: _timestamp_pb2.Timestamp
    status: LHStatusPb
    thread_specs: _containers.MessageMap[str, ThreadSpecPb]
    entrypoint_thread_name: str
    retention_hours: int
    def __init__(self, name: _Optional[str] = ..., version: _Optional[int] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., status: _Optional[_Union[LHStatusPb, str]] = ..., thread_specs: _Optional[_Mapping[str, ThreadSpecPb]] = ..., entrypoint_thread_name: _Optional[str] = ..., retention_hours: _Optional[int] = ...) -> None: ...

class OutputSchemaPb(_message.Message):
    __slots__ = ["output_type"]
    OUTPUT_TYPE_FIELD_NUMBER: _ClassVar[int]
    output_type: VariableTypePb
    def __init__(self, output_type: _Optional[_Union[VariableTypePb, str]] = ...) -> None: ...

class TaskDefPb(_message.Message):
    __slots__ = ["name", "input_vars", "created_at"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    INPUT_VARS_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    name: str
    input_vars: _containers.RepeatedCompositeFieldContainer[VariableDefPb]
    created_at: _timestamp_pb2.Timestamp
    def __init__(self, name: _Optional[str] = ..., input_vars: _Optional[_Iterable[_Union[VariableDefPb, _Mapping]]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class ExternalEventDefPb(_message.Message):
    __slots__ = ["name", "created_at", "retention_hours"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    RETENTION_HOURS_FIELD_NUMBER: _ClassVar[int]
    name: str
    created_at: _timestamp_pb2.Timestamp
    retention_hours: int
    def __init__(self, name: _Optional[str] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., retention_hours: _Optional[int] = ...) -> None: ...

class TaskDefMetricsIdPb(_message.Message):
    __slots__ = ["window_start", "window_type", "task_def_name"]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_TYPE_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    window_start: _timestamp_pb2.Timestamp
    window_type: MetricsWindowLengthPb
    task_def_name: str
    def __init__(self, window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., window_type: _Optional[_Union[MetricsWindowLengthPb, str]] = ..., task_def_name: _Optional[str] = ...) -> None: ...

class TaskDefMetricsPb(_message.Message):
    __slots__ = ["window_start", "type", "taskDefName", "schedule_to_start_max", "schedule_to_start_avg", "start_to_complete_max", "start_to_complete_avg", "total_completed", "total_errored", "total_started", "total_scheduled"]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    TASKDEFNAME_FIELD_NUMBER: _ClassVar[int]
    SCHEDULE_TO_START_MAX_FIELD_NUMBER: _ClassVar[int]
    SCHEDULE_TO_START_AVG_FIELD_NUMBER: _ClassVar[int]
    START_TO_COMPLETE_MAX_FIELD_NUMBER: _ClassVar[int]
    START_TO_COMPLETE_AVG_FIELD_NUMBER: _ClassVar[int]
    TOTAL_COMPLETED_FIELD_NUMBER: _ClassVar[int]
    TOTAL_ERRORED_FIELD_NUMBER: _ClassVar[int]
    TOTAL_STARTED_FIELD_NUMBER: _ClassVar[int]
    TOTAL_SCHEDULED_FIELD_NUMBER: _ClassVar[int]
    window_start: _timestamp_pb2.Timestamp
    type: MetricsWindowLengthPb
    taskDefName: str
    schedule_to_start_max: int
    schedule_to_start_avg: int
    start_to_complete_max: int
    start_to_complete_avg: int
    total_completed: int
    total_errored: int
    total_started: int
    total_scheduled: int
    def __init__(self, window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., type: _Optional[_Union[MetricsWindowLengthPb, str]] = ..., taskDefName: _Optional[str] = ..., schedule_to_start_max: _Optional[int] = ..., schedule_to_start_avg: _Optional[int] = ..., start_to_complete_max: _Optional[int] = ..., start_to_complete_avg: _Optional[int] = ..., total_completed: _Optional[int] = ..., total_errored: _Optional[int] = ..., total_started: _Optional[int] = ..., total_scheduled: _Optional[int] = ...) -> None: ...

class WfSpecMetricsIdPb(_message.Message):
    __slots__ = ["window_start", "window_type", "wf_spec_name", "wf_spec_version"]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_TYPE_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_VERSION_FIELD_NUMBER: _ClassVar[int]
    window_start: _timestamp_pb2.Timestamp
    window_type: MetricsWindowLengthPb
    wf_spec_name: str
    wf_spec_version: int
    def __init__(self, window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., window_type: _Optional[_Union[MetricsWindowLengthPb, str]] = ..., wf_spec_name: _Optional[str] = ..., wf_spec_version: _Optional[int] = ...) -> None: ...

class WfSpecMetricsPb(_message.Message):
    __slots__ = ["window_start", "type", "wfSpecName", "wfSpecVersion", "total_started", "total_completed", "total_errored", "start_to_complete_max", "start_to_complete_avg"]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    WFSPECNAME_FIELD_NUMBER: _ClassVar[int]
    WFSPECVERSION_FIELD_NUMBER: _ClassVar[int]
    TOTAL_STARTED_FIELD_NUMBER: _ClassVar[int]
    TOTAL_COMPLETED_FIELD_NUMBER: _ClassVar[int]
    TOTAL_ERRORED_FIELD_NUMBER: _ClassVar[int]
    START_TO_COMPLETE_MAX_FIELD_NUMBER: _ClassVar[int]
    START_TO_COMPLETE_AVG_FIELD_NUMBER: _ClassVar[int]
    window_start: _timestamp_pb2.Timestamp
    type: MetricsWindowLengthPb
    wfSpecName: str
    wfSpecVersion: int
    total_started: int
    total_completed: int
    total_errored: int
    start_to_complete_max: int
    start_to_complete_avg: int
    def __init__(self, window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., type: _Optional[_Union[MetricsWindowLengthPb, str]] = ..., wfSpecName: _Optional[str] = ..., wfSpecVersion: _Optional[int] = ..., total_started: _Optional[int] = ..., total_completed: _Optional[int] = ..., total_errored: _Optional[int] = ..., start_to_complete_max: _Optional[int] = ..., start_to_complete_avg: _Optional[int] = ...) -> None: ...
