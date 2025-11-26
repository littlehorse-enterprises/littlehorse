import datetime

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
import littlehorse.model.struct_def_pb2 as _struct_def_pb2
import littlehorse.model.acls_pb2 as _acls_pb2
import littlehorse.model.workflow_event_pb2 as _workflow_event_pb2
import littlehorse.model.scheduled_wf_run_pb2 as _scheduled_wf_run_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class AllowedUpdateType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    ALL_UPDATES: _ClassVar[AllowedUpdateType]
    MINOR_REVISION_UPDATES: _ClassVar[AllowedUpdateType]
    NO_UPDATES: _ClassVar[AllowedUpdateType]

class StructDefCompatibilityType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    NO_SCHEMA_UPDATES: _ClassVar[StructDefCompatibilityType]
    FULLY_COMPATIBLE_SCHEMA_UPDATES: _ClassVar[StructDefCompatibilityType]
ALL_UPDATES: AllowedUpdateType
MINOR_REVISION_UPDATES: AllowedUpdateType
NO_UPDATES: AllowedUpdateType
NO_SCHEMA_UPDATES: StructDefCompatibilityType
FULLY_COMPATIBLE_SCHEMA_UPDATES: StructDefCompatibilityType

class GetLatestUserTaskDefRequest(_message.Message):
    __slots__ = ("name",)
    NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    def __init__(self, name: _Optional[str] = ...) -> None: ...

class PutWfSpecRequest(_message.Message):
    __slots__ = ("name", "thread_specs", "entrypoint_thread_name", "retention_policy", "parent_wf_spec", "allowed_updates")
    class ThreadSpecsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _wf_spec_pb2.ThreadSpec
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_wf_spec_pb2.ThreadSpec, _Mapping]] = ...) -> None: ...
    NAME_FIELD_NUMBER: _ClassVar[int]
    THREAD_SPECS_FIELD_NUMBER: _ClassVar[int]
    ENTRYPOINT_THREAD_NAME_FIELD_NUMBER: _ClassVar[int]
    RETENTION_POLICY_FIELD_NUMBER: _ClassVar[int]
    PARENT_WF_SPEC_FIELD_NUMBER: _ClassVar[int]
    ALLOWED_UPDATES_FIELD_NUMBER: _ClassVar[int]
    name: str
    thread_specs: _containers.MessageMap[str, _wf_spec_pb2.ThreadSpec]
    entrypoint_thread_name: str
    retention_policy: _wf_spec_pb2.WorkflowRetentionPolicy
    parent_wf_spec: _wf_spec_pb2.WfSpec.ParentWfSpecReference
    allowed_updates: AllowedUpdateType
    def __init__(self, name: _Optional[str] = ..., thread_specs: _Optional[_Mapping[str, _wf_spec_pb2.ThreadSpec]] = ..., entrypoint_thread_name: _Optional[str] = ..., retention_policy: _Optional[_Union[_wf_spec_pb2.WorkflowRetentionPolicy, _Mapping]] = ..., parent_wf_spec: _Optional[_Union[_wf_spec_pb2.WfSpec.ParentWfSpecReference, _Mapping]] = ..., allowed_updates: _Optional[_Union[AllowedUpdateType, str]] = ...) -> None: ...

class PutTaskDefRequest(_message.Message):
    __slots__ = ("name", "input_vars", "return_type")
    NAME_FIELD_NUMBER: _ClassVar[int]
    INPUT_VARS_FIELD_NUMBER: _ClassVar[int]
    RETURN_TYPE_FIELD_NUMBER: _ClassVar[int]
    name: str
    input_vars: _containers.RepeatedCompositeFieldContainer[_common_wfspec_pb2.VariableDef]
    return_type: _common_wfspec_pb2.ReturnType
    def __init__(self, name: _Optional[str] = ..., input_vars: _Optional[_Iterable[_Union[_common_wfspec_pb2.VariableDef, _Mapping]]] = ..., return_type: _Optional[_Union[_common_wfspec_pb2.ReturnType, _Mapping]] = ...) -> None: ...

class PutStructDefRequest(_message.Message):
    __slots__ = ("name", "description", "struct_def", "allowed_updates")
    NAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    STRUCT_DEF_FIELD_NUMBER: _ClassVar[int]
    ALLOWED_UPDATES_FIELD_NUMBER: _ClassVar[int]
    name: str
    description: str
    struct_def: _common_wfspec_pb2.InlineStructDef
    allowed_updates: StructDefCompatibilityType
    def __init__(self, name: _Optional[str] = ..., description: _Optional[str] = ..., struct_def: _Optional[_Union[_common_wfspec_pb2.InlineStructDef, _Mapping]] = ..., allowed_updates: _Optional[_Union[StructDefCompatibilityType, str]] = ...) -> None: ...

class ValidateStructDefEvolutionRequest(_message.Message):
    __slots__ = ("struct_def_id", "struct_def", "compatibility_type")
    STRUCT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    STRUCT_DEF_FIELD_NUMBER: _ClassVar[int]
    COMPATIBILITY_TYPE_FIELD_NUMBER: _ClassVar[int]
    struct_def_id: _object_id_pb2.StructDefId
    struct_def: _common_wfspec_pb2.InlineStructDef
    compatibility_type: StructDefCompatibilityType
    def __init__(self, struct_def_id: _Optional[_Union[_object_id_pb2.StructDefId, _Mapping]] = ..., struct_def: _Optional[_Union[_common_wfspec_pb2.InlineStructDef, _Mapping]] = ..., compatibility_type: _Optional[_Union[StructDefCompatibilityType, str]] = ...) -> None: ...

class ValidateStructDefEvolutionResponse(_message.Message):
    __slots__ = ("is_valid",)
    IS_VALID_FIELD_NUMBER: _ClassVar[int]
    is_valid: bool
    def __init__(self, is_valid: _Optional[bool] = ...) -> None: ...

class PutWorkflowEventDefRequest(_message.Message):
    __slots__ = ("name", "content_type")
    NAME_FIELD_NUMBER: _ClassVar[int]
    CONTENT_TYPE_FIELD_NUMBER: _ClassVar[int]
    name: str
    content_type: _common_wfspec_pb2.ReturnType
    def __init__(self, name: _Optional[str] = ..., content_type: _Optional[_Union[_common_wfspec_pb2.ReturnType, _Mapping]] = ...) -> None: ...

class PutUserTaskDefRequest(_message.Message):
    __slots__ = ("name", "fields", "description")
    NAME_FIELD_NUMBER: _ClassVar[int]
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    name: str
    fields: _containers.RepeatedCompositeFieldContainer[_user_tasks_pb2.UserTaskField]
    description: str
    def __init__(self, name: _Optional[str] = ..., fields: _Optional[_Iterable[_Union[_user_tasks_pb2.UserTaskField, _Mapping]]] = ..., description: _Optional[str] = ...) -> None: ...

class PutExternalEventDefRequest(_message.Message):
    __slots__ = ("name", "retention_policy", "content_type", "correlated_event_config")
    NAME_FIELD_NUMBER: _ClassVar[int]
    RETENTION_POLICY_FIELD_NUMBER: _ClassVar[int]
    CONTENT_TYPE_FIELD_NUMBER: _ClassVar[int]
    CORRELATED_EVENT_CONFIG_FIELD_NUMBER: _ClassVar[int]
    name: str
    retention_policy: _external_event_pb2.ExternalEventRetentionPolicy
    content_type: _common_wfspec_pb2.ReturnType
    correlated_event_config: _external_event_pb2.CorrelatedEventConfig
    def __init__(self, name: _Optional[str] = ..., retention_policy: _Optional[_Union[_external_event_pb2.ExternalEventRetentionPolicy, _Mapping]] = ..., content_type: _Optional[_Union[_common_wfspec_pb2.ReturnType, _Mapping]] = ..., correlated_event_config: _Optional[_Union[_external_event_pb2.CorrelatedEventConfig, _Mapping]] = ...) -> None: ...

class PutExternalEventRequest(_message.Message):
    __slots__ = ("wf_run_id", "external_event_def_id", "guid", "content", "thread_run_number", "node_run_position")
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    GUID_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    NODE_RUN_POSITION_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: _object_id_pb2.WfRunId
    external_event_def_id: _object_id_pb2.ExternalEventDefId
    guid: str
    content: _variable_pb2.VariableValue
    thread_run_number: int
    node_run_position: int
    def __init__(self, wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ..., external_event_def_id: _Optional[_Union[_object_id_pb2.ExternalEventDefId, _Mapping]] = ..., guid: _Optional[str] = ..., content: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., thread_run_number: _Optional[int] = ..., node_run_position: _Optional[int] = ...) -> None: ...

class PutCorrelatedEventRequest(_message.Message):
    __slots__ = ("key", "external_event_def_id", "content")
    KEY_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    key: str
    external_event_def_id: _object_id_pb2.ExternalEventDefId
    content: _variable_pb2.VariableValue
    def __init__(self, key: _Optional[str] = ..., external_event_def_id: _Optional[_Union[_object_id_pb2.ExternalEventDefId, _Mapping]] = ..., content: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ...) -> None: ...

class DeleteExternalEventRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.ExternalEventId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.ExternalEventId, _Mapping]] = ...) -> None: ...

class DeleteScheduledWfRunRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.ScheduledWfRunId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.ScheduledWfRunId, _Mapping]] = ...) -> None: ...

class DeleteWfRunRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.WfRunId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ...) -> None: ...

class DeleteCorrelatedEventRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.CorrelatedEventId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.CorrelatedEventId, _Mapping]] = ...) -> None: ...

class DeleteTaskDefRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.TaskDefId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ...) -> None: ...

class DeleteStructDefRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.StructDefId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.StructDefId, _Mapping]] = ...) -> None: ...

class DeleteUserTaskDefRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.UserTaskDefId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.UserTaskDefId, _Mapping]] = ...) -> None: ...

class DeleteWfSpecRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.WfSpecId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ...) -> None: ...

class DeleteExternalEventDefRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.ExternalEventDefId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.ExternalEventDefId, _Mapping]] = ...) -> None: ...

class DeleteWorkflowEventDefRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.WorkflowEventDefId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.WorkflowEventDefId, _Mapping]] = ...) -> None: ...

class RunWfRequest(_message.Message):
    __slots__ = ("wf_spec_name", "major_version", "revision", "variables", "id", "parent_wf_run_id")
    class VariablesEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _variable_pb2.VariableValue
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ...) -> None: ...
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    MAJOR_VERSION_FIELD_NUMBER: _ClassVar[int]
    REVISION_FIELD_NUMBER: _ClassVar[int]
    VARIABLES_FIELD_NUMBER: _ClassVar[int]
    ID_FIELD_NUMBER: _ClassVar[int]
    PARENT_WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    wf_spec_name: str
    major_version: int
    revision: int
    variables: _containers.MessageMap[str, _variable_pb2.VariableValue]
    id: str
    parent_wf_run_id: _object_id_pb2.WfRunId
    def __init__(self, wf_spec_name: _Optional[str] = ..., major_version: _Optional[int] = ..., revision: _Optional[int] = ..., variables: _Optional[_Mapping[str, _variable_pb2.VariableValue]] = ..., id: _Optional[str] = ..., parent_wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ...) -> None: ...

class ScheduleWfRequest(_message.Message):
    __slots__ = ("id", "wf_spec_name", "major_version", "revision", "variables", "parent_wf_run_id", "cron_expression")
    class VariablesEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _variable_pb2.VariableValue
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ...) -> None: ...
    ID_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    MAJOR_VERSION_FIELD_NUMBER: _ClassVar[int]
    REVISION_FIELD_NUMBER: _ClassVar[int]
    VARIABLES_FIELD_NUMBER: _ClassVar[int]
    PARENT_WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    CRON_EXPRESSION_FIELD_NUMBER: _ClassVar[int]
    id: str
    wf_spec_name: str
    major_version: int
    revision: int
    variables: _containers.MessageMap[str, _variable_pb2.VariableValue]
    parent_wf_run_id: _object_id_pb2.WfRunId
    cron_expression: str
    def __init__(self, id: _Optional[str] = ..., wf_spec_name: _Optional[str] = ..., major_version: _Optional[int] = ..., revision: _Optional[int] = ..., variables: _Optional[_Mapping[str, _variable_pb2.VariableValue]] = ..., parent_wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ..., cron_expression: _Optional[str] = ...) -> None: ...

class VariableMatch(_message.Message):
    __slots__ = ("var_name", "value")
    VAR_NAME_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    var_name: str
    value: _variable_pb2.VariableValue
    def __init__(self, var_name: _Optional[str] = ..., value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ...) -> None: ...

class AwaitWorkflowEventRequest(_message.Message):
    __slots__ = ("wf_run_id", "event_def_ids", "workflow_events_to_ignore")
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    EVENT_DEF_IDS_FIELD_NUMBER: _ClassVar[int]
    WORKFLOW_EVENTS_TO_IGNORE_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: _object_id_pb2.WfRunId
    event_def_ids: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.WorkflowEventDefId]
    workflow_events_to_ignore: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.WorkflowEventId]
    def __init__(self, wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ..., event_def_ids: _Optional[_Iterable[_Union[_object_id_pb2.WorkflowEventDefId, _Mapping]]] = ..., workflow_events_to_ignore: _Optional[_Iterable[_Union[_object_id_pb2.WorkflowEventId, _Mapping]]] = ...) -> None: ...

class SearchWfRunRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "wf_spec_name", "wf_spec_major_version", "wf_spec_revision", "status", "earliest_start", "latest_start", "variable_filters", "parent_wf_run_id", "show_full_tree")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_MAJOR_VERSION_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_REVISION_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
    LATEST_START_FIELD_NUMBER: _ClassVar[int]
    VARIABLE_FILTERS_FIELD_NUMBER: _ClassVar[int]
    PARENT_WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    SHOW_FULL_TREE_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    wf_spec_name: str
    wf_spec_major_version: int
    wf_spec_revision: int
    status: _common_enums_pb2.LHStatus
    earliest_start: _timestamp_pb2.Timestamp
    latest_start: _timestamp_pb2.Timestamp
    variable_filters: _containers.RepeatedCompositeFieldContainer[VariableMatch]
    parent_wf_run_id: _object_id_pb2.WfRunId
    show_full_tree: bool
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., wf_spec_name: _Optional[str] = ..., wf_spec_major_version: _Optional[int] = ..., wf_spec_revision: _Optional[int] = ..., status: _Optional[_Union[_common_enums_pb2.LHStatus, str]] = ..., earliest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., variable_filters: _Optional[_Iterable[_Union[VariableMatch, _Mapping]]] = ..., parent_wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ..., show_full_tree: _Optional[bool] = ...) -> None: ...

class WfRunIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.WfRunId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.WfRunId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchCorrelatedEventRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "earliest_start", "latest_start", "external_event_def_id", "has_external_events")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
    LATEST_START_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    HAS_EXTERNAL_EVENTS_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    earliest_start: _timestamp_pb2.Timestamp
    latest_start: _timestamp_pb2.Timestamp
    external_event_def_id: _object_id_pb2.ExternalEventDefId
    has_external_events: bool
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., earliest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., external_event_def_id: _Optional[_Union[_object_id_pb2.ExternalEventDefId, _Mapping]] = ..., has_external_events: _Optional[bool] = ...) -> None: ...

class CorrelatedEventIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.CorrelatedEventId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.CorrelatedEventId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchTaskRunRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "task_def_name", "status", "earliest_start", "latest_start")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
    LATEST_START_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    task_def_name: str
    status: _common_enums_pb2.TaskStatus
    earliest_start: _timestamp_pb2.Timestamp
    latest_start: _timestamp_pb2.Timestamp
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., task_def_name: _Optional[str] = ..., status: _Optional[_Union[_common_enums_pb2.TaskStatus, str]] = ..., earliest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class TaskRunIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.TaskRunId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.TaskRunId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchNodeRunRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "earliest_start", "latest_start", "status", "external_event_def")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
    LATEST_START_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    earliest_start: _timestamp_pb2.Timestamp
    latest_start: _timestamp_pb2.Timestamp
    status: _common_enums_pb2.LHStatus
    external_event_def: _object_id_pb2.ExternalEventDefId
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., earliest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., status: _Optional[_Union[_common_enums_pb2.LHStatus, str]] = ..., external_event_def: _Optional[_Union[_object_id_pb2.ExternalEventDefId, _Mapping]] = ...) -> None: ...

class NodeRunIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.NodeRunId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.NodeRunId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchUserTaskRunRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "status", "user_task_def_name", "user_id", "user_group", "earliest_start", "latest_start")
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
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., status: _Optional[_Union[_user_tasks_pb2.UserTaskRunStatus, str]] = ..., user_task_def_name: _Optional[str] = ..., user_id: _Optional[str] = ..., user_group: _Optional[str] = ..., earliest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class UserTaskRunIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.UserTaskRunId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.UserTaskRunId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchVariableRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "value", "wf_spec_major_version", "wf_spec_revision", "var_name", "wf_spec_name")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_MAJOR_VERSION_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_REVISION_FIELD_NUMBER: _ClassVar[int]
    VAR_NAME_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    value: _variable_pb2.VariableValue
    wf_spec_major_version: int
    wf_spec_revision: int
    var_name: str
    wf_spec_name: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., wf_spec_major_version: _Optional[int] = ..., wf_spec_revision: _Optional[int] = ..., var_name: _Optional[str] = ..., wf_spec_name: _Optional[str] = ...) -> None: ...

class VariableIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.VariableId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.VariableId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchTaskDefRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "prefix")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    PREFIX_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    prefix: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., prefix: _Optional[str] = ...) -> None: ...

class TaskDefIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.TaskDefId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.TaskDefId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchUserTaskDefRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "prefix", "name")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    PREFIX_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    prefix: str
    name: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., prefix: _Optional[str] = ..., name: _Optional[str] = ...) -> None: ...

class SearchStructDefRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "prefix", "name")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    PREFIX_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    prefix: str
    name: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., prefix: _Optional[str] = ..., name: _Optional[str] = ...) -> None: ...

class StructDefIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.StructDefId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.StructDefId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class UserTaskDefIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.UserTaskDefId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.UserTaskDefId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchWfSpecRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "name", "prefix", "task_def_name")
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
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.WfSpecId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.WfSpecId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchExternalEventDefRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "prefix")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    PREFIX_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    prefix: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., prefix: _Optional[str] = ...) -> None: ...

class ExternalEventDefIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.ExternalEventDefId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.ExternalEventDefId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchWorkflowEventDefRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "prefix")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    PREFIX_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    prefix: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., prefix: _Optional[str] = ...) -> None: ...

class WorkflowEventDefIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.WorkflowEventDefId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.WorkflowEventDefId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchTenantRequest(_message.Message):
    __slots__ = ("limit", "bookmark")
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    limit: int
    bookmark: bytes
    def __init__(self, limit: _Optional[int] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class TenantIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.TenantId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.TenantId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchPrincipalRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "earliest_start", "latest_start", "isAdmin", "tenantId")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
    LATEST_START_FIELD_NUMBER: _ClassVar[int]
    ISADMIN_FIELD_NUMBER: _ClassVar[int]
    TENANTID_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    earliest_start: _timestamp_pb2.Timestamp
    latest_start: _timestamp_pb2.Timestamp
    isAdmin: bool
    tenantId: str
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., earliest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., isAdmin: _Optional[bool] = ..., tenantId: _Optional[str] = ...) -> None: ...

class PrincipalIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.PrincipalId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.PrincipalId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchExternalEventRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "earliest_start", "latest_start", "external_event_def_id", "is_claimed")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
    LATEST_START_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    IS_CLAIMED_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    earliest_start: _timestamp_pb2.Timestamp
    latest_start: _timestamp_pb2.Timestamp
    external_event_def_id: _object_id_pb2.ExternalEventDefId
    is_claimed: bool
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., earliest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., external_event_def_id: _Optional[_Union[_object_id_pb2.ExternalEventDefId, _Mapping]] = ..., is_claimed: _Optional[bool] = ...) -> None: ...

class ExternalEventIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.ExternalEventId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.ExternalEventId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class SearchWorkflowEventRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "earliest_start", "latest_start", "workflow_event_def_id")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
    LATEST_START_FIELD_NUMBER: _ClassVar[int]
    WORKFLOW_EVENT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    earliest_start: _timestamp_pb2.Timestamp
    latest_start: _timestamp_pb2.Timestamp
    workflow_event_def_id: _object_id_pb2.WorkflowEventDefId
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., earliest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., workflow_event_def_id: _Optional[_Union[_object_id_pb2.WorkflowEventDefId, _Mapping]] = ...) -> None: ...

class WorkflowEventIdList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.WorkflowEventId]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.WorkflowEventId, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class ListNodeRunsRequest(_message.Message):
    __slots__ = ("wf_run_id", "thread_run_number", "bookmark", "limit")
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: _object_id_pb2.WfRunId
    thread_run_number: int
    bookmark: bytes
    limit: int
    def __init__(self, wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ..., thread_run_number: _Optional[int] = ..., bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ...) -> None: ...

class NodeRunList(_message.Message):
    __slots__ = ("results", "bookmark")
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_node_run_pb2.NodeRun]
    bookmark: bytes
    def __init__(self, results: _Optional[_Iterable[_Union[_node_run_pb2.NodeRun, _Mapping]]] = ..., bookmark: _Optional[bytes] = ...) -> None: ...

class ListVariablesRequest(_message.Message):
    __slots__ = ("wf_run_id",)
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: _object_id_pb2.WfRunId
    def __init__(self, wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ...) -> None: ...

class VariableList(_message.Message):
    __slots__ = ("results",)
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_variable_pb2.Variable]
    def __init__(self, results: _Optional[_Iterable[_Union[_variable_pb2.Variable, _Mapping]]] = ...) -> None: ...

class ListExternalEventsRequest(_message.Message):
    __slots__ = ("wf_run_id",)
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: _object_id_pb2.WfRunId
    def __init__(self, wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ...) -> None: ...

class ExternalEventList(_message.Message):
    __slots__ = ("results",)
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_external_event_pb2.ExternalEvent]
    def __init__(self, results: _Optional[_Iterable[_Union[_external_event_pb2.ExternalEvent, _Mapping]]] = ...) -> None: ...

class ListWorkflowEventsRequest(_message.Message):
    __slots__ = ("wf_run_id",)
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: _object_id_pb2.WfRunId
    def __init__(self, wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ...) -> None: ...

class WorkflowEventList(_message.Message):
    __slots__ = ("results",)
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_workflow_event_pb2.WorkflowEvent]
    def __init__(self, results: _Optional[_Iterable[_Union[_workflow_event_pb2.WorkflowEvent, _Mapping]]] = ...) -> None: ...

class RegisterTaskWorkerRequest(_message.Message):
    __slots__ = ("task_worker_id", "task_def_id")
    TASK_WORKER_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    task_worker_id: str
    task_def_id: _object_id_pb2.TaskDefId
    def __init__(self, task_worker_id: _Optional[str] = ..., task_def_id: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ...) -> None: ...

class TaskWorkerHeartBeatRequest(_message.Message):
    __slots__ = ("client_id", "task_def_id", "listener_name")
    CLIENT_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    LISTENER_NAME_FIELD_NUMBER: _ClassVar[int]
    client_id: str
    task_def_id: _object_id_pb2.TaskDefId
    listener_name: str
    def __init__(self, client_id: _Optional[str] = ..., task_def_id: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ..., listener_name: _Optional[str] = ...) -> None: ...

class RegisterTaskWorkerResponse(_message.Message):
    __slots__ = ("your_hosts", "is_cluster_healthy")
    YOUR_HOSTS_FIELD_NUMBER: _ClassVar[int]
    IS_CLUSTER_HEALTHY_FIELD_NUMBER: _ClassVar[int]
    your_hosts: _containers.RepeatedCompositeFieldContainer[LHHostInfo]
    is_cluster_healthy: bool
    def __init__(self, your_hosts: _Optional[_Iterable[_Union[LHHostInfo, _Mapping]]] = ..., is_cluster_healthy: _Optional[bool] = ...) -> None: ...

class LHHostInfo(_message.Message):
    __slots__ = ("host", "port")
    HOST_FIELD_NUMBER: _ClassVar[int]
    PORT_FIELD_NUMBER: _ClassVar[int]
    host: str
    port: int
    def __init__(self, host: _Optional[str] = ..., port: _Optional[int] = ...) -> None: ...

class PollTaskRequest(_message.Message):
    __slots__ = ("task_def_id", "client_id", "task_worker_version")
    TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    CLIENT_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_WORKER_VERSION_FIELD_NUMBER: _ClassVar[int]
    task_def_id: _object_id_pb2.TaskDefId
    client_id: str
    task_worker_version: str
    def __init__(self, task_def_id: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ..., client_id: _Optional[str] = ..., task_worker_version: _Optional[str] = ...) -> None: ...

class PutCheckpointRequest(_message.Message):
    __slots__ = ("task_run_id", "task_attempt", "value", "logs")
    TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_ATTEMPT_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    LOGS_FIELD_NUMBER: _ClassVar[int]
    task_run_id: _object_id_pb2.TaskRunId
    task_attempt: int
    value: _variable_pb2.VariableValue
    logs: str
    def __init__(self, task_run_id: _Optional[_Union[_object_id_pb2.TaskRunId, _Mapping]] = ..., task_attempt: _Optional[int] = ..., value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., logs: _Optional[str] = ...) -> None: ...

class PutCheckpointResponse(_message.Message):
    __slots__ = ("flow_control_continue_type", "created_checkpoint")
    class FlowControlContinue(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
        __slots__ = ()
        CONTINUE_TASK: _ClassVar[PutCheckpointResponse.FlowControlContinue]
        STOP_TASK: _ClassVar[PutCheckpointResponse.FlowControlContinue]
    CONTINUE_TASK: PutCheckpointResponse.FlowControlContinue
    STOP_TASK: PutCheckpointResponse.FlowControlContinue
    FLOW_CONTROL_CONTINUE_TYPE_FIELD_NUMBER: _ClassVar[int]
    CREATED_CHECKPOINT_FIELD_NUMBER: _ClassVar[int]
    flow_control_continue_type: PutCheckpointResponse.FlowControlContinue
    created_checkpoint: _task_run_pb2.Checkpoint
    def __init__(self, flow_control_continue_type: _Optional[_Union[PutCheckpointResponse.FlowControlContinue, str]] = ..., created_checkpoint: _Optional[_Union[_task_run_pb2.Checkpoint, _Mapping]] = ...) -> None: ...

class ScheduledTask(_message.Message):
    __slots__ = ("task_run_id", "task_def_id", "attempt_number", "variables", "created_at", "source", "total_observed_checkpoints")
    TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    ATTEMPT_NUMBER_FIELD_NUMBER: _ClassVar[int]
    VARIABLES_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    SOURCE_FIELD_NUMBER: _ClassVar[int]
    TOTAL_OBSERVED_CHECKPOINTS_FIELD_NUMBER: _ClassVar[int]
    task_run_id: _object_id_pb2.TaskRunId
    task_def_id: _object_id_pb2.TaskDefId
    attempt_number: int
    variables: _containers.RepeatedCompositeFieldContainer[_task_run_pb2.VarNameAndVal]
    created_at: _timestamp_pb2.Timestamp
    source: _task_run_pb2.TaskRunSource
    total_observed_checkpoints: int
    def __init__(self, task_run_id: _Optional[_Union[_object_id_pb2.TaskRunId, _Mapping]] = ..., task_def_id: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ..., attempt_number: _Optional[int] = ..., variables: _Optional[_Iterable[_Union[_task_run_pb2.VarNameAndVal, _Mapping]]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., source: _Optional[_Union[_task_run_pb2.TaskRunSource, _Mapping]] = ..., total_observed_checkpoints: _Optional[int] = ...) -> None: ...

class PollTaskResponse(_message.Message):
    __slots__ = ("result",)
    RESULT_FIELD_NUMBER: _ClassVar[int]
    result: ScheduledTask
    def __init__(self, result: _Optional[_Union[ScheduledTask, _Mapping]] = ...) -> None: ...

class ReportTaskRun(_message.Message):
    __slots__ = ("task_run_id", "time", "status", "log_output", "attempt_number", "output", "error", "exception", "total_checkpoints")
    TASK_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    TIME_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    LOG_OUTPUT_FIELD_NUMBER: _ClassVar[int]
    ATTEMPT_NUMBER_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_FIELD_NUMBER: _ClassVar[int]
    ERROR_FIELD_NUMBER: _ClassVar[int]
    EXCEPTION_FIELD_NUMBER: _ClassVar[int]
    TOTAL_CHECKPOINTS_FIELD_NUMBER: _ClassVar[int]
    task_run_id: _object_id_pb2.TaskRunId
    time: _timestamp_pb2.Timestamp
    status: _common_enums_pb2.TaskStatus
    log_output: _variable_pb2.VariableValue
    attempt_number: int
    output: _variable_pb2.VariableValue
    error: _task_run_pb2.LHTaskError
    exception: _task_run_pb2.LHTaskException
    total_checkpoints: int
    def __init__(self, task_run_id: _Optional[_Union[_object_id_pb2.TaskRunId, _Mapping]] = ..., time: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., status: _Optional[_Union[_common_enums_pb2.TaskStatus, str]] = ..., log_output: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., attempt_number: _Optional[int] = ..., output: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., error: _Optional[_Union[_task_run_pb2.LHTaskError, _Mapping]] = ..., exception: _Optional[_Union[_task_run_pb2.LHTaskException, _Mapping]] = ..., total_checkpoints: _Optional[int] = ...) -> None: ...

class StopWfRunRequest(_message.Message):
    __slots__ = ("wf_run_id", "thread_run_number")
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: _object_id_pb2.WfRunId
    thread_run_number: int
    def __init__(self, wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ..., thread_run_number: _Optional[int] = ...) -> None: ...

class ResumeWfRunRequest(_message.Message):
    __slots__ = ("wf_run_id", "thread_run_number")
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: _object_id_pb2.WfRunId
    thread_run_number: int
    def __init__(self, wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ..., thread_run_number: _Optional[int] = ...) -> None: ...

class RescueThreadRunRequest(_message.Message):
    __slots__ = ("wf_run_id", "thread_run_number", "skip_current_node")
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    SKIP_CURRENT_NODE_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: _object_id_pb2.WfRunId
    thread_run_number: int
    skip_current_node: bool
    def __init__(self, wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ..., thread_run_number: _Optional[int] = ..., skip_current_node: _Optional[bool] = ...) -> None: ...

class TaskDefMetricsQueryRequest(_message.Message):
    __slots__ = ("window_start", "window_type", "task_def_name")
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_TYPE_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    window_start: _timestamp_pb2.Timestamp
    window_type: _common_enums_pb2.MetricsWindowLength
    task_def_name: str
    def __init__(self, window_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., window_type: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ..., task_def_name: _Optional[str] = ...) -> None: ...

class ListTaskMetricsRequest(_message.Message):
    __slots__ = ("task_def_id", "last_window_start", "window_length", "num_windows")
    TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    LAST_WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_LENGTH_FIELD_NUMBER: _ClassVar[int]
    NUM_WINDOWS_FIELD_NUMBER: _ClassVar[int]
    task_def_id: _object_id_pb2.TaskDefId
    last_window_start: _timestamp_pb2.Timestamp
    window_length: _common_enums_pb2.MetricsWindowLength
    num_windows: int
    def __init__(self, task_def_id: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ..., last_window_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., window_length: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ..., num_windows: _Optional[int] = ...) -> None: ...

class ListTaskMetricsResponse(_message.Message):
    __slots__ = ("results",)
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[TaskDefMetrics]
    def __init__(self, results: _Optional[_Iterable[_Union[TaskDefMetrics, _Mapping]]] = ...) -> None: ...

class WfSpecMetricsQueryRequest(_message.Message):
    __slots__ = ("wf_spec_id", "window_start", "window_length")
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_LENGTH_FIELD_NUMBER: _ClassVar[int]
    wf_spec_id: _object_id_pb2.WfSpecId
    window_start: _timestamp_pb2.Timestamp
    window_length: _common_enums_pb2.MetricsWindowLength
    def __init__(self, wf_spec_id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., window_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., window_length: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ...) -> None: ...

class ListWfMetricsRequest(_message.Message):
    __slots__ = ("wf_spec_id", "last_window_start", "window_length", "num_windows")
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    LAST_WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_LENGTH_FIELD_NUMBER: _ClassVar[int]
    NUM_WINDOWS_FIELD_NUMBER: _ClassVar[int]
    wf_spec_id: _object_id_pb2.WfSpecId
    last_window_start: _timestamp_pb2.Timestamp
    window_length: _common_enums_pb2.MetricsWindowLength
    num_windows: int
    def __init__(self, wf_spec_id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., last_window_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., window_length: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ..., num_windows: _Optional[int] = ...) -> None: ...

class ListWfMetricsResponse(_message.Message):
    __slots__ = ("results",)
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[WfSpecMetrics]
    def __init__(self, results: _Optional[_Iterable[_Union[WfSpecMetrics, _Mapping]]] = ...) -> None: ...

class TaskDefMetrics(_message.Message):
    __slots__ = ("task_def_id", "window_start", "type", "schedule_to_start_max", "schedule_to_start_avg", "start_to_complete_max", "start_to_complete_avg", "total_completed", "total_errored", "total_started", "total_scheduled")
    TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    SCHEDULE_TO_START_MAX_FIELD_NUMBER: _ClassVar[int]
    SCHEDULE_TO_START_AVG_FIELD_NUMBER: _ClassVar[int]
    START_TO_COMPLETE_MAX_FIELD_NUMBER: _ClassVar[int]
    START_TO_COMPLETE_AVG_FIELD_NUMBER: _ClassVar[int]
    TOTAL_COMPLETED_FIELD_NUMBER: _ClassVar[int]
    TOTAL_ERRORED_FIELD_NUMBER: _ClassVar[int]
    TOTAL_STARTED_FIELD_NUMBER: _ClassVar[int]
    TOTAL_SCHEDULED_FIELD_NUMBER: _ClassVar[int]
    task_def_id: _object_id_pb2.TaskDefId
    window_start: _timestamp_pb2.Timestamp
    type: _common_enums_pb2.MetricsWindowLength
    schedule_to_start_max: int
    schedule_to_start_avg: int
    start_to_complete_max: int
    start_to_complete_avg: int
    total_completed: int
    total_errored: int
    total_started: int
    total_scheduled: int
    def __init__(self, task_def_id: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ..., window_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., type: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ..., schedule_to_start_max: _Optional[int] = ..., schedule_to_start_avg: _Optional[int] = ..., start_to_complete_max: _Optional[int] = ..., start_to_complete_avg: _Optional[int] = ..., total_completed: _Optional[int] = ..., total_errored: _Optional[int] = ..., total_started: _Optional[int] = ..., total_scheduled: _Optional[int] = ...) -> None: ...

class WfSpecMetrics(_message.Message):
    __slots__ = ("wf_spec_id", "window_start", "type", "total_started", "total_completed", "total_errored", "start_to_complete_max", "start_to_complete_avg")
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    TOTAL_STARTED_FIELD_NUMBER: _ClassVar[int]
    TOTAL_COMPLETED_FIELD_NUMBER: _ClassVar[int]
    TOTAL_ERRORED_FIELD_NUMBER: _ClassVar[int]
    START_TO_COMPLETE_MAX_FIELD_NUMBER: _ClassVar[int]
    START_TO_COMPLETE_AVG_FIELD_NUMBER: _ClassVar[int]
    wf_spec_id: _object_id_pb2.WfSpecId
    window_start: _timestamp_pb2.Timestamp
    type: _common_enums_pb2.MetricsWindowLength
    total_started: int
    total_completed: int
    total_errored: int
    start_to_complete_max: int
    start_to_complete_avg: int
    def __init__(self, wf_spec_id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., window_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., type: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ..., total_started: _Optional[int] = ..., total_completed: _Optional[int] = ..., total_errored: _Optional[int] = ..., start_to_complete_max: _Optional[int] = ..., start_to_complete_avg: _Optional[int] = ...) -> None: ...

class ListUserTaskRunRequest(_message.Message):
    __slots__ = ("wf_run_id",)
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: _object_id_pb2.WfRunId
    def __init__(self, wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ...) -> None: ...

class UserTaskRunList(_message.Message):
    __slots__ = ("results",)
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_user_tasks_pb2.UserTaskRun]
    def __init__(self, results: _Optional[_Iterable[_Union[_user_tasks_pb2.UserTaskRun, _Mapping]]] = ...) -> None: ...

class ScheduledWfRunIdList(_message.Message):
    __slots__ = ("results",)
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.ScheduledWfRunId]
    def __init__(self, results: _Optional[_Iterable[_Union[_object_id_pb2.ScheduledWfRunId, _Mapping]]] = ...) -> None: ...

class SearchScheduledWfRunRequest(_message.Message):
    __slots__ = ("wf_spec_name", "major_version", "revision")
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    MAJOR_VERSION_FIELD_NUMBER: _ClassVar[int]
    REVISION_FIELD_NUMBER: _ClassVar[int]
    wf_spec_name: str
    major_version: int
    revision: int
    def __init__(self, wf_spec_name: _Optional[str] = ..., major_version: _Optional[int] = ..., revision: _Optional[int] = ...) -> None: ...

class TaskWorkerMetadata(_message.Message):
    __slots__ = ("task_worker_id", "latest_heartbeat", "hosts")
    TASK_WORKER_ID_FIELD_NUMBER: _ClassVar[int]
    LATEST_HEARTBEAT_FIELD_NUMBER: _ClassVar[int]
    HOSTS_FIELD_NUMBER: _ClassVar[int]
    task_worker_id: str
    latest_heartbeat: _timestamp_pb2.Timestamp
    hosts: _containers.RepeatedCompositeFieldContainer[LHHostInfo]
    def __init__(self, task_worker_id: _Optional[str] = ..., latest_heartbeat: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., hosts: _Optional[_Iterable[_Union[LHHostInfo, _Mapping]]] = ...) -> None: ...

class TaskWorkerGroup(_message.Message):
    __slots__ = ("id", "created_at", "task_workers")
    class TaskWorkersEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: TaskWorkerMetadata
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[TaskWorkerMetadata, _Mapping]] = ...) -> None: ...
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    TASK_WORKERS_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.TaskWorkerGroupId
    created_at: _timestamp_pb2.Timestamp
    task_workers: _containers.MessageMap[str, TaskWorkerMetadata]
    def __init__(self, id: _Optional[_Union[_object_id_pb2.TaskWorkerGroupId, _Mapping]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., task_workers: _Optional[_Mapping[str, TaskWorkerMetadata]] = ...) -> None: ...

class ListTaskRunsRequest(_message.Message):
    __slots__ = ("wf_run_id",)
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: _object_id_pb2.WfRunId
    def __init__(self, wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ...) -> None: ...

class TaskRunList(_message.Message):
    __slots__ = ("results",)
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    results: _containers.RepeatedCompositeFieldContainer[_task_run_pb2.TaskRun]
    def __init__(self, results: _Optional[_Iterable[_Union[_task_run_pb2.TaskRun, _Mapping]]] = ...) -> None: ...

class MigrateWfSpecRequest(_message.Message):
    __slots__ = ("old_wf_spec", "migration")
    OLD_WF_SPEC_FIELD_NUMBER: _ClassVar[int]
    MIGRATION_FIELD_NUMBER: _ClassVar[int]
    old_wf_spec: _object_id_pb2.WfSpecId
    migration: _wf_spec_pb2.WfSpecVersionMigration
    def __init__(self, old_wf_spec: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., migration: _Optional[_Union[_wf_spec_pb2.WfSpecVersionMigration, _Mapping]] = ...) -> None: ...

class GetLatestWfSpecRequest(_message.Message):
    __slots__ = ("name", "major_version")
    NAME_FIELD_NUMBER: _ClassVar[int]
    MAJOR_VERSION_FIELD_NUMBER: _ClassVar[int]
    name: str
    major_version: int
    def __init__(self, name: _Optional[str] = ..., major_version: _Optional[int] = ...) -> None: ...

class LittleHorseVersion(_message.Message):
    __slots__ = ("major_version", "minor_version", "patch_version", "pre_release_identifier")
    MAJOR_VERSION_FIELD_NUMBER: _ClassVar[int]
    MINOR_VERSION_FIELD_NUMBER: _ClassVar[int]
    PATCH_VERSION_FIELD_NUMBER: _ClassVar[int]
    PRE_RELEASE_IDENTIFIER_FIELD_NUMBER: _ClassVar[int]
    major_version: int
    minor_version: int
    patch_version: int
    pre_release_identifier: str
    def __init__(self, major_version: _Optional[int] = ..., minor_version: _Optional[int] = ..., patch_version: _Optional[int] = ..., pre_release_identifier: _Optional[str] = ...) -> None: ...
