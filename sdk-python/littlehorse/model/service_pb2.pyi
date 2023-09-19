from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf import empty_pb2 as _empty_pb2
import littlehorse.model.common_wfspec_pb2 as _common_wfspec_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
import littlehorse.model.external_event_pb2 as _external_event_pb2
import littlehorse.model.wf_run_pb2 as _wf_run_pb2
import littlehorse.model.node_run_pb2 as _node_run_pb2
import littlehorse.model.task_run_pb2 as _task_run_pb2
import littlehorse.model.user_tasks_pb2 as _user_tasks_pb2
import littlehorse.model.wf_spec_pb2 as _wf_spec_pb2
import littlehorse.model.task_def_pb2 as _task_def_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class LHHealthResult(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    LH_HEALTH_RUNNING: _ClassVar[LHHealthResult]
    LH_HEALTH_REBALANCING: _ClassVar[LHHealthResult]
    LH_HEALTH_ERROR: _ClassVar[LHHealthResult]
LH_HEALTH_RUNNING: LHHealthResult
LH_HEALTH_REBALANCING: LHHealthResult
LH_HEALTH_ERROR: LHHealthResult

class GetLatestUserTaskDefRequest(_message.Message):
    __slots__ = ["name"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    def __init__(self, name: _Optional[str] = ...) -> None: ...

class PutWfSpecRequest(_message.Message):
    __slots__ = ["name", "thread_specs", "entrypoint_thread_name", "retention_hours"]
    class ThreadSpecsEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _wf_spec_pb2.ThreadSpec
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_wf_spec_pb2.ThreadSpec, _Mapping]] = ...) -> None: ...
    NAME_FIELD_NUMBER: _ClassVar[int]
    THREAD_SPECS_FIELD_NUMBER: _ClassVar[int]
    ENTRYPOINT_THREAD_NAME_FIELD_NUMBER: _ClassVar[int]
    RETENTION_HOURS_FIELD_NUMBER: _ClassVar[int]
    name: str
    thread_specs: _containers.MessageMap[str, _wf_spec_pb2.ThreadSpec]
    entrypoint_thread_name: str
    retention_hours: int
    def __init__(self, name: _Optional[str] = ..., thread_specs: _Optional[_Mapping[str, _wf_spec_pb2.ThreadSpec]] = ..., entrypoint_thread_name: _Optional[str] = ..., retention_hours: _Optional[int] = ...) -> None: ...

class PutTaskDefRequest(_message.Message):
    __slots__ = ["name", "input_vars"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    INPUT_VARS_FIELD_NUMBER: _ClassVar[int]
    name: str
    input_vars: _containers.RepeatedCompositeFieldContainer[_common_wfspec_pb2.VariableDef]
    def __init__(self, name: _Optional[str] = ..., input_vars: _Optional[_Iterable[_Union[_common_wfspec_pb2.VariableDef, _Mapping]]] = ...) -> None: ...

class PutUserTaskDefRequest(_message.Message):
    __slots__ = ["name", "fields", "description"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    name: str
    fields: _containers.RepeatedCompositeFieldContainer[_user_tasks_pb2.UserTaskField]
    description: str
    def __init__(self, name: _Optional[str] = ..., fields: _Optional[_Iterable[_Union[_user_tasks_pb2.UserTaskField, _Mapping]]] = ..., description: _Optional[str] = ...) -> None: ...

class PutExternalEventDefRequest(_message.Message):
    __slots__ = ["name", "retention_hours"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    RETENTION_HOURS_FIELD_NUMBER: _ClassVar[int]
    name: str
    retention_hours: int
    def __init__(self, name: _Optional[str] = ..., retention_hours: _Optional[int] = ...) -> None: ...

class PutExternalEventRequest(_message.Message):
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
    content: _variable_pb2.VariableValue
    thread_run_number: int
    node_run_position: int
    def __init__(self, wf_run_id: _Optional[str] = ..., external_event_def_name: _Optional[str] = ..., guid: _Optional[str] = ..., content: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., thread_run_number: _Optional[int] = ..., node_run_position: _Optional[int] = ...) -> None: ...

class DeleteExternalEventRequest(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.ExternalEventId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.ExternalEventId, _Mapping]] = ...) -> None: ...

class DeleteWfRunRequest(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.WfRunId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ...) -> None: ...

class DeleteTaskDefRequest(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.TaskDefId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ...) -> None: ...

class DeleteUserTaskDefRequest(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.UserTaskDefId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.UserTaskDefId, _Mapping]] = ...) -> None: ...

class DeleteWfSpecRequest(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.WfSpecId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ...) -> None: ...

class DeleteExternalEventDefRequest(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.ExternalEventDefId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.ExternalEventDefId, _Mapping]] = ...) -> None: ...

class RunWfRequest(_message.Message):
    __slots__ = ["wf_spec_name", "wf_spec_version", "variables", "id"]
    class VariablesEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _variable_pb2.VariableValue
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ...) -> None: ...
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_VERSION_FIELD_NUMBER: _ClassVar[int]
    VARIABLES_FIELD_NUMBER: _ClassVar[int]
    ID_FIELD_NUMBER: _ClassVar[int]
    wf_spec_name: str
    wf_spec_version: int
    variables: _containers.MessageMap[str, _variable_pb2.VariableValue]
    id: str
    def __init__(self, wf_spec_name: _Optional[str] = ..., wf_spec_version: _Optional[int] = ..., variables: _Optional[_Mapping[str, _variable_pb2.VariableValue]] = ..., id: _Optional[str] = ...) -> None: ...

class SearchWfRunRequest(_message.Message):
    __slots__ = ["bookmark", "limit", "status_and_spec", "name", "status_and_name"]
    class StatusAndSpecRequest(_message.Message):
        __slots__ = ["wf_spec_name", "status", "wf_spec_version", "earliest_start", "latest_start"]
        WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
        STATUS_FIELD_NUMBER: _ClassVar[int]
        WF_SPEC_VERSION_FIELD_NUMBER: _ClassVar[int]
        EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
        LATEST_START_FIELD_NUMBER: _ClassVar[int]
        wf_spec_name: str
        status: _common_enums_pb2.LHStatus
        wf_spec_version: int
        earliest_start: _timestamp_pb2.Timestamp
        latest_start: _timestamp_pb2.Timestamp
        def __init__(self, wf_spec_name: _Optional[str] = ..., status: _Optional[_Union[_common_enums_pb2.LHStatus, str]] = ..., wf_spec_version: _Optional[int] = ..., earliest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...
    class NameRequest(_message.Message):
        __slots__ = ["wf_spec_name", "earliest_start", "latest_start"]
        WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
        EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
        LATEST_START_FIELD_NUMBER: _ClassVar[int]
        wf_spec_name: str
        earliest_start: _timestamp_pb2.Timestamp
        latest_start: _timestamp_pb2.Timestamp
        def __init__(self, wf_spec_name: _Optional[str] = ..., earliest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...
    class StatusAndNameRequest(_message.Message):
        __slots__ = ["wf_spec_name", "status", "earliest_start", "latest_start"]
        WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
        STATUS_FIELD_NUMBER: _ClassVar[int]
        EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
        LATEST_START_FIELD_NUMBER: _ClassVar[int]
        wf_spec_name: str
        status: _common_enums_pb2.LHStatus
        earliest_start: _timestamp_pb2.Timestamp
        latest_start: _timestamp_pb2.Timestamp
        def __init__(self, wf_spec_name: _Optional[str] = ..., status: _Optional[_Union[_common_enums_pb2.LHStatus, str]] = ..., earliest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    STATUS_AND_SPEC_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    STATUS_AND_NAME_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    status_and_spec: SearchWfRunRequest.StatusAndSpecRequest
    name: SearchWfRunRequest.NameRequest
    status_and_name: SearchWfRunRequest.StatusAndNameRequest
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., status_and_spec: _Optional[_Union[SearchWfRunRequest.StatusAndSpecRequest, _Mapping]] = ..., name: _Optional[_Union[SearchWfRunRequest.NameRequest, _Mapping]] = ..., status_and_name: _Optional[_Union[SearchWfRunRequest.StatusAndNameRequest, _Mapping]] = ...) -> None: ...

class WfRunIdList(_message.Message):
    __slots__ = ["results", "bookmark"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.WfRunId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.WfRunId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchTaskRunRequest(_message.Message):
    __slots__ = ["bookmark", "limit", "status_and_task_def", "task_def"]
    class StatusAndTaskDefRequest(_message.Message):
        __slots__ = ["status", "task_def_name", "earliest_start", "latest_start"]
        STATUS_FIELD_NUMBER: _ClassVar[int]
        TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
        EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
        LATEST_START_FIELD_NUMBER: _ClassVar[int]
        status: _common_enums_pb2.TaskStatus
        task_def_name: str
        earliest_start: _timestamp_pb2.Timestamp
        latest_start: _timestamp_pb2.Timestamp
        def __init__(self, status: _Optional[_Union[_common_enums_pb2.TaskStatus, str]] = ..., task_def_name: _Optional[str] = ..., earliest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...
    class ByTaskDefRequest(_message.Message):
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
    status_and_task_def: SearchTaskRunRequest.StatusAndTaskDefRequest
    task_def: SearchTaskRunRequest.ByTaskDefRequest
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., status_and_task_def: _Optional[_Union[SearchTaskRunRequest.StatusAndTaskDefRequest, _Mapping]] = ..., task_def: _Optional[_Union[SearchTaskRunRequest.ByTaskDefRequest, _Mapping]] = ...) -> None: ...

class TaskRunIdList(_message.Message):
    __slots__ = ["results", "bookmark"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.TaskRunId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.TaskRunId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchNodeRunRequest(_message.Message):
    __slots__ = ["bookmark", "limit", "wf_run_id"]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    wf_run_id: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., wf_run_id: _Optional[str] = ...) -> None: ...

class NodeRunIdList(_message.Message):
    __slots__ = ["results", "bookmark"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.NodeRunId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.NodeRunId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchUserTaskRunRequest(_message.Message):
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
    status: _user_tasks_pb2.UserTaskRunStatus
    user_task_def_name: str
    user_id: str
    user_group: str
    earliest_start: _timestamp_pb2.Timestamp
    latest_start: _timestamp_pb2.Timestamp
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., status: _Optional[_Union[_user_tasks_pb2.UserTaskRunStatus, str]] = ..., user_task_def_name: _Optional[str] = ..., user_id: _Optional[str] = ..., user_group: _Optional[str] = ..., earliest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class UserTaskRunIdList(_message.Message):
    __slots__ = ["results", "bookmark"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.UserTaskRunId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.UserTaskRunId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchVariableRequest(_message.Message):
    __slots__ = ["bookmark", "limit", "wf_run_id", "value"]
    class NameAndValueRequest(_message.Message):
        __slots__ = ["value", "wf_spec_version", "var_name", "wf_spec_name"]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        WF_SPEC_VERSION_FIELD_NUMBER: _ClassVar[int]
        VAR_NAME_FIELD_NUMBER: _ClassVar[int]
        WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
        value: _variable_pb2.VariableValue
        wf_spec_version: int
        var_name: str
        wf_spec_name: str
        def __init__(self, value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., wf_spec_version: _Optional[int] = ..., var_name: _Optional[str] = ..., wf_spec_name: _Optional[str] = ...) -> None: ...
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    wf_run_id: str
    value: SearchVariableRequest.NameAndValueRequest
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., wf_run_id: _Optional[str] = ..., value: _Optional[_Union[SearchVariableRequest.NameAndValueRequest, _Mapping]] = ...) -> None: ...

class VariableIdList(_message.Message):
    __slots__ = ["results", "bookmark"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.VariableId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.VariableId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchTaskDefRequest(_message.Message):
    __slots__ = ["bookmark", "limit", "prefix"]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    PREFIX_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    prefix: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., prefix: _Optional[str] = ...) -> None: ...

class TaskDefIdList(_message.Message):
    __slots__ = ["results", "bookmark"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.TaskDefId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.TaskDefId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchUserTaskDefRequest(_message.Message):
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

class UserTaskDefIdList(_message.Message):
    __slots__ = ["results", "bookmark"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.UserTaskDefId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.UserTaskDefId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchWfSpecRequest(_message.Message):
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

class WfSpecIdList(_message.Message):
    __slots__ = ["results", "bookmark"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.WfSpecId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.WfSpecId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchExternalEventDefRequest(_message.Message):
    __slots__ = ["bookmark", "limit", "prefix"]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    PREFIX_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    prefix: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., prefix: _Optional[str] = ...) -> None: ...

class ExternalEventDefIdList(_message.Message):
    __slots__ = ["results", "bookmark"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.ExternalEventDefId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.ExternalEventDefId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchExternalEventRequest(_message.Message):
    __slots__ = ["bookmark", "limit", "wf_run_id", "external_event_def_name_and_status"]
    class ByExtEvtDefNameAndStatusRequest(_message.Message):
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
    external_event_def_name_and_status: SearchExternalEventRequest.ByExtEvtDefNameAndStatusRequest
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., wf_run_id: _Optional[str] = ..., external_event_def_name_and_status: _Optional[_Union[SearchExternalEventRequest.ByExtEvtDefNameAndStatusRequest, _Mapping]] = ...) -> None: ...

class ExternalEventIdList(_message.Message):
    __slots__ = ["results", "bookmark"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.ExternalEventId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.ExternalEventId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class ListNodeRunsRequest(_message.Message):
    __slots__ = ["wf_run_id"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    def __init__(self, wf_run_id: _Optional[str] = ...) -> None: ...

class NodeRunList(_message.Message):
    __slots__ = ["results"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_node_run_pb2.NodeRun]
    def __init__(self, results: _Optional[_Iterable[_Union[_node_run_pb2.NodeRun, _Mapping]]] = ...) -> None: ...

class ListVariablesRequest(_message.Message):
    __slots__ = ["wf_run_id"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    def __init__(self, wf_run_id: _Optional[str] = ...) -> None: ...

class VariableList(_message.Message):
    __slots__ = ["results"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_variable_pb2.Variable]
    def __init__(self, results: _Optional[_Iterable[_Union[_variable_pb2.Variable, _Mapping]]] = ...) -> None: ...

class ListExternalEventsRequest(_message.Message):
    __slots__ = ["wf_run_id"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    def __init__(self, wf_run_id: _Optional[str] = ...) -> None: ...

class ExternalEventList(_message.Message):
    __slots__ = ["results"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_external_event_pb2.ExternalEvent]
    def __init__(self, results: _Optional[_Iterable[_Union[_external_event_pb2.ExternalEvent, _Mapping]]] = ...) -> None: ...

class RegisterTaskWorkerRequest(_message.Message):
    __slots__ = ["client_id", "task_def_name", "listener_name"]
    CLIENT_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    LISTENER_NAME_FIELD_NUMBER: _ClassVar[int]
    client_id: str
    task_def_name: str
    listener_name: str
    def __init__(self, client_id: _Optional[str] = ..., task_def_name: _Optional[str] = ..., listener_name: _Optional[str] = ...) -> None: ...

class TaskWorkerHeartBeatRequest(_message.Message):
    __slots__ = ["client_id", "task_def_name", "listener_name"]
    CLIENT_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    LISTENER_NAME_FIELD_NUMBER: _ClassVar[int]
    client_id: str
    task_def_name: str
    listener_name: str
    def __init__(self, client_id: _Optional[str] = ..., task_def_name: _Optional[str] = ..., listener_name: _Optional[str] = ...) -> None: ...

class RegisterTaskWorkerResponse(_message.Message):
    __slots__ = ["your_hosts"]
    YOUR_HOSTS_FIELD_NUMBER: _ClassVar[int]
    your_hosts: _containers.RepeatedCompositeFieldContainer[LHHostInfo]
    def __init__(self, your_hosts: _Optional[_Iterable[_Union[LHHostInfo, _Mapping]]] = ...) -> None: ...

class LHHostInfo(_message.Message):
    __slots__ = ["host", "port"]
    HOST_FIELD_NUMBER: _ClassVar[int]
    PORT_FIELD_NUMBER: _ClassVar[int]
    host: str
    port: int
    def __init__(self, host: _Optional[str] = ..., port: _Optional[int] = ...) -> None: ...

class TaskWorkerMetadata(_message.Message):
    __slots__ = ["client_id", "latest_heartbeat", "hosts"]
    CLIENT_ID_FIELD_NUMBER: _ClassVar[int]
    LATEST_HEARTBEAT_FIELD_NUMBER: _ClassVar[int]
    HOSTS_FIELD_NUMBER: _ClassVar[int]
    client_id: str
    latest_heartbeat: _timestamp_pb2.Timestamp
    hosts: _containers.RepeatedCompositeFieldContainer[LHHostInfo]
    def __init__(self, client_id: _Optional[str] = ..., latest_heartbeat: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., hosts: _Optional[_Iterable[_Union[LHHostInfo, _Mapping]]] = ...) -> None: ...

class TaskWorkerGroup(_message.Message):
    __slots__ = ["task_def_name", "created_at", "task_workers"]
    class TaskWorkersEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: TaskWorkerMetadata
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[TaskWorkerMetadata, _Mapping]] = ...) -> None: ...
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    TASK_WORKERS_FIELD_NUMBER: _ClassVar[int]
    task_def_name: str
    created_at: _timestamp_pb2.Timestamp
    task_workers: _containers.MessageMap[str, TaskWorkerMetadata]
    def __init__(self, task_def_name: _Optional[str] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., task_workers: _Optional[_Mapping[str, TaskWorkerMetadata]] = ...) -> None: ...

class PollTaskRequest(_message.Message):
    __slots__ = ["task_def_name", "client_id", "task_worker_version"]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    CLIENT_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_WORKER_VERSION_FIELD_NUMBER: _ClassVar[int]
    task_def_name: str
    client_id: str
    task_worker_version: str
    def __init__(self, task_def_name: _Optional[str] = ..., client_id: _Optional[str] = ..., task_worker_version: _Optional[str] = ...) -> None: ...

class ScheduledTask(_message.Message):
    __slots__ = ["task_run_id", "task_def_id", "attempt_number", "variables", "created_at", "source"]
    TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    ATTEMPT_NUMBER_FIELD_NUMBER: _ClassVar[int]
    VARIABLES_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    SOURCE_FIELD_NUMBER: _ClassVar[int]
    task_run_id: _object_id_pb2.TaskRunId
    task_def_id: _object_id_pb2.TaskDefId
    attempt_number: int
    variables: _containers.RepeatedCompositeFieldContainer[_task_run_pb2.VarNameAndVal]
    created_at: _timestamp_pb2.Timestamp
    source: _task_run_pb2.TaskRunSource
    def __init__(self, task_run_id: _Optional[_Union[_object_id_pb2.TaskRunId, _Mapping]] = ..., task_def_id: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ..., attempt_number: _Optional[int] = ..., variables: _Optional[_Iterable[_Union[_task_run_pb2.VarNameAndVal, _Mapping]]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., source: _Optional[_Union[_task_run_pb2.TaskRunSource, _Mapping]] = ...) -> None: ...

class PollTaskResponse(_message.Message):
    __slots__ = ["result"]
    RESULT_FIELD_NUMBER: _ClassVar[int]
    result: ScheduledTask
    def __init__(self, result: _Optional[_Union[ScheduledTask, _Mapping]] = ...) -> None: ...

class ReportTaskRun(_message.Message):
    __slots__ = ["task_run_id", "time", "status", "output", "log_output", "attempt_number"]
    TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    TIME_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_FIELD_NUMBER: _ClassVar[int]
    LOG_OUTPUT_FIELD_NUMBER: _ClassVar[int]
    ATTEMPT_NUMBER_FIELD_NUMBER: _ClassVar[int]
    task_run_id: _object_id_pb2.TaskRunId
    time: _timestamp_pb2.Timestamp
    status: _common_enums_pb2.TaskStatus
    output: _variable_pb2.VariableValue
    log_output: _variable_pb2.VariableValue
    attempt_number: int
    def __init__(self, task_run_id: _Optional[_Union[_object_id_pb2.TaskRunId, _Mapping]] = ..., time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., status: _Optional[_Union[_common_enums_pb2.TaskStatus, str]] = ..., output: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., log_output: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., attempt_number: _Optional[int] = ...) -> None: ...

class StopWfRunRequest(_message.Message):
    __slots__ = ["wf_run_id", "thread_run_number"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    thread_run_number: int
    def __init__(self, wf_run_id: _Optional[str] = ..., thread_run_number: _Optional[int] = ...) -> None: ...

class ResumeWfRunRequest(_message.Message):
    __slots__ = ["wf_run_id", "thread_run_number"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    thread_run_number: int
    def __init__(self, wf_run_id: _Optional[str] = ..., thread_run_number: _Optional[int] = ...) -> None: ...

class TaskDefMetricsQueryRequest(_message.Message):
    __slots__ = ["window_start", "window_type", "task_def_name"]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_TYPE_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    window_start: _timestamp_pb2.Timestamp
    window_type: _common_enums_pb2.MetricsWindowLength
    task_def_name: str
    def __init__(self, window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., window_type: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ..., task_def_name: _Optional[str] = ...) -> None: ...

class ListTaskMetricsRequest(_message.Message):
    __slots__ = ["last_window_start", "num_windows", "task_def_name", "window_length"]
    LAST_WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    NUM_WINDOWS_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    WINDOW_LENGTH_FIELD_NUMBER: _ClassVar[int]
    last_window_start: _timestamp_pb2.Timestamp
    num_windows: int
    task_def_name: str
    window_length: _common_enums_pb2.MetricsWindowLength
    def __init__(self, last_window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., num_windows: _Optional[int] = ..., task_def_name: _Optional[str] = ..., window_length: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ...) -> None: ...

class ListTaskMetricsResponse(_message.Message):
    __slots__ = ["results"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[TaskDefMetrics]
    def __init__(self, results: _Optional[_Iterable[_Union[TaskDefMetrics, _Mapping]]] = ...) -> None: ...

class WfSpecMetricsQueryRequest(_message.Message):
    __slots__ = ["window_start", "window_type", "wf_spec_name", "wf_spec_version"]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_TYPE_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_VERSION_FIELD_NUMBER: _ClassVar[int]
    window_start: _timestamp_pb2.Timestamp
    window_type: _common_enums_pb2.MetricsWindowLength
    wf_spec_name: str
    wf_spec_version: int
    def __init__(self, window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., window_type: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ..., wf_spec_name: _Optional[str] = ..., wf_spec_version: _Optional[int] = ...) -> None: ...

class ListWfMetricsRequest(_message.Message):
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
    window_length: _common_enums_pb2.MetricsWindowLength
    def __init__(self, last_window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., num_windows: _Optional[int] = ..., wf_spec_name: _Optional[str] = ..., wf_spec_version: _Optional[int] = ..., window_length: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ...) -> None: ...

class ListWfMetricsResponse(_message.Message):
    __slots__ = ["results"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[WfSpecMetrics]
    def __init__(self, results: _Optional[_Iterable[_Union[WfSpecMetrics, _Mapping]]] = ...) -> None: ...

class TaskDefMetrics(_message.Message):
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
    type: _common_enums_pb2.MetricsWindowLength
    taskDefName: str
    schedule_to_start_max: int
    schedule_to_start_avg: int
    start_to_complete_max: int
    start_to_complete_avg: int
    total_completed: int
    total_errored: int
    total_started: int
    total_scheduled: int
    def __init__(self, window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., type: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ..., taskDefName: _Optional[str] = ..., schedule_to_start_max: _Optional[int] = ..., schedule_to_start_avg: _Optional[int] = ..., start_to_complete_max: _Optional[int] = ..., start_to_complete_avg: _Optional[int] = ..., total_completed: _Optional[int] = ..., total_errored: _Optional[int] = ..., total_started: _Optional[int] = ..., total_scheduled: _Optional[int] = ...) -> None: ...

class WfSpecMetrics(_message.Message):
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
    type: _common_enums_pb2.MetricsWindowLength
    wfSpecName: str
    wfSpecVersion: int
    total_started: int
    total_completed: int
    total_errored: int
    start_to_complete_max: int
    start_to_complete_avg: int
    def __init__(self, window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., type: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ..., wfSpecName: _Optional[str] = ..., wfSpecVersion: _Optional[int] = ..., total_started: _Optional[int] = ..., total_completed: _Optional[int] = ..., total_errored: _Optional[int] = ..., start_to_complete_max: _Optional[int] = ..., start_to_complete_avg: _Optional[int] = ...) -> None: ...

class ListUserTaskRunRequest(_message.Message):
    __slots__ = ["wf_run_id"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    def __init__(self, wf_run_id: _Optional[str] = ...) -> None: ...

class UserTaskRunList(_message.Message):
    __slots__ = ["results"]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_user_tasks_pb2.UserTaskRun]
    def __init__(self, results: _Optional[_Iterable[_Union[_user_tasks_pb2.UserTaskRun, _Mapping]]] = ...) -> None: ...
