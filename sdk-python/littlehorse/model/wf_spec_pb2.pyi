from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
import littlehorse.model.common_wfspec_pb2 as _common_wfspec_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class WfRunVariableAccessLevel(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    PUBLIC_VAR: _ClassVar[WfRunVariableAccessLevel]
    PRIVATE_VAR: _ClassVar[WfRunVariableAccessLevel]
    INHERITED_VAR: _ClassVar[WfRunVariableAccessLevel]
PUBLIC_VAR: WfRunVariableAccessLevel
PRIVATE_VAR: WfRunVariableAccessLevel
INHERITED_VAR: WfRunVariableAccessLevel

class WfSpec(_message.Message):
    __slots__ = ["id", "created_at", "frozen_variables", "status", "thread_specs", "entrypoint_thread_name", "retention_policy", "migration", "parent_wf_spec"]
    class ThreadSpecsEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: ThreadSpec
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[ThreadSpec, _Mapping]] = ...) -> None: ...
    class ParentWfSpecReference(_message.Message):
        __slots__ = ["wf_spec_name", "wf_spec_major_version"]
        WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
        WF_SPEC_MAJOR_VERSION_FIELD_NUMBER: _ClassVar[int]
        wf_spec_name: str
        wf_spec_major_version: int
        def __init__(self, wf_spec_name: _Optional[str] = ..., wf_spec_major_version: _Optional[int] = ...) -> None: ...
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    FROZEN_VARIABLES_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    THREAD_SPECS_FIELD_NUMBER: _ClassVar[int]
    ENTRYPOINT_THREAD_NAME_FIELD_NUMBER: _ClassVar[int]
    RETENTION_POLICY_FIELD_NUMBER: _ClassVar[int]
    MIGRATION_FIELD_NUMBER: _ClassVar[int]
    PARENT_WF_SPEC_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.WfSpecId
    created_at: _timestamp_pb2.Timestamp
    frozen_variables: _containers.RepeatedCompositeFieldContainer[ThreadVarDef]
    status: _common_enums_pb2.MetadataStatus
    thread_specs: _containers.MessageMap[str, ThreadSpec]
    entrypoint_thread_name: str
    retention_policy: WorkflowRetentionPolicy
    migration: WfSpecVersionMigration
    parent_wf_spec: WfSpec.ParentWfSpecReference
    def __init__(self, id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., frozen_variables: _Optional[_Iterable[_Union[ThreadVarDef, _Mapping]]] = ..., status: _Optional[_Union[_common_enums_pb2.MetadataStatus, str]] = ..., thread_specs: _Optional[_Mapping[str, ThreadSpec]] = ..., entrypoint_thread_name: _Optional[str] = ..., retention_policy: _Optional[_Union[WorkflowRetentionPolicy, _Mapping]] = ..., migration: _Optional[_Union[WfSpecVersionMigration, _Mapping]] = ..., parent_wf_spec: _Optional[_Union[WfSpec.ParentWfSpecReference, _Mapping]] = ...) -> None: ...

class WorkflowRetentionPolicy(_message.Message):
    __slots__ = ["seconds_after_wf_termination"]
    SECONDS_AFTER_WF_TERMINATION_FIELD_NUMBER: _ClassVar[int]
    seconds_after_wf_termination: int
    def __init__(self, seconds_after_wf_termination: _Optional[int] = ...) -> None: ...

class JsonIndex(_message.Message):
    __slots__ = ["field_path", "field_type"]
    FIELD_PATH_FIELD_NUMBER: _ClassVar[int]
    FIELD_TYPE_FIELD_NUMBER: _ClassVar[int]
    field_path: str
    field_type: _common_enums_pb2.VariableType
    def __init__(self, field_path: _Optional[str] = ..., field_type: _Optional[_Union[_common_enums_pb2.VariableType, str]] = ...) -> None: ...

class ThreadVarDef(_message.Message):
    __slots__ = ["var_def", "required", "searchable", "json_indexes", "access_level"]
    VAR_DEF_FIELD_NUMBER: _ClassVar[int]
    REQUIRED_FIELD_NUMBER: _ClassVar[int]
    SEARCHABLE_FIELD_NUMBER: _ClassVar[int]
    JSON_INDEXES_FIELD_NUMBER: _ClassVar[int]
    ACCESS_LEVEL_FIELD_NUMBER: _ClassVar[int]
    var_def: _common_wfspec_pb2.VariableDef
    required: bool
    searchable: bool
    json_indexes: _containers.RepeatedCompositeFieldContainer[JsonIndex]
    access_level: WfRunVariableAccessLevel
    def __init__(self, var_def: _Optional[_Union[_common_wfspec_pb2.VariableDef, _Mapping]] = ..., required: bool = ..., searchable: bool = ..., json_indexes: _Optional[_Iterable[_Union[JsonIndex, _Mapping]]] = ..., access_level: _Optional[_Union[WfRunVariableAccessLevel, str]] = ...) -> None: ...

class ThreadSpec(_message.Message):
    __slots__ = ["nodes", "variable_defs", "interrupt_defs", "retention_policy"]
    class NodesEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: Node
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[Node, _Mapping]] = ...) -> None: ...
    NODES_FIELD_NUMBER: _ClassVar[int]
    VARIABLE_DEFS_FIELD_NUMBER: _ClassVar[int]
    INTERRUPT_DEFS_FIELD_NUMBER: _ClassVar[int]
    RETENTION_POLICY_FIELD_NUMBER: _ClassVar[int]
    nodes: _containers.MessageMap[str, Node]
    variable_defs: _containers.RepeatedCompositeFieldContainer[ThreadVarDef]
    interrupt_defs: _containers.RepeatedCompositeFieldContainer[InterruptDef]
    retention_policy: ThreadRetentionPolicy
    def __init__(self, nodes: _Optional[_Mapping[str, Node]] = ..., variable_defs: _Optional[_Iterable[_Union[ThreadVarDef, _Mapping]]] = ..., interrupt_defs: _Optional[_Iterable[_Union[InterruptDef, _Mapping]]] = ..., retention_policy: _Optional[_Union[ThreadRetentionPolicy, _Mapping]] = ...) -> None: ...

class ThreadRetentionPolicy(_message.Message):
    __slots__ = ["seconds_after_thread_termination"]
    SECONDS_AFTER_THREAD_TERMINATION_FIELD_NUMBER: _ClassVar[int]
    seconds_after_thread_termination: int
    def __init__(self, seconds_after_thread_termination: _Optional[int] = ...) -> None: ...

class InterruptDef(_message.Message):
    __slots__ = ["external_event_def_id", "handler_spec_name"]
    EXTERNAL_EVENT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    HANDLER_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    external_event_def_id: _object_id_pb2.ExternalEventDefId
    handler_spec_name: str
    def __init__(self, external_event_def_id: _Optional[_Union[_object_id_pb2.ExternalEventDefId, _Mapping]] = ..., handler_spec_name: _Optional[str] = ...) -> None: ...

class StartThreadNode(_message.Message):
    __slots__ = ["thread_spec_name", "variables"]
    class VariablesEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _common_wfspec_pb2.VariableAssignment
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ...) -> None: ...
    THREAD_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    VARIABLES_FIELD_NUMBER: _ClassVar[int]
    thread_spec_name: str
    variables: _containers.MessageMap[str, _common_wfspec_pb2.VariableAssignment]
    def __init__(self, thread_spec_name: _Optional[str] = ..., variables: _Optional[_Mapping[str, _common_wfspec_pb2.VariableAssignment]] = ...) -> None: ...

class StartMultipleThreadsNode(_message.Message):
    __slots__ = ["thread_spec_name", "variables", "iterable"]
    class VariablesEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _common_wfspec_pb2.VariableAssignment
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ...) -> None: ...
    THREAD_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    VARIABLES_FIELD_NUMBER: _ClassVar[int]
    ITERABLE_FIELD_NUMBER: _ClassVar[int]
    thread_spec_name: str
    variables: _containers.MessageMap[str, _common_wfspec_pb2.VariableAssignment]
    iterable: _common_wfspec_pb2.VariableAssignment
    def __init__(self, thread_spec_name: _Optional[str] = ..., variables: _Optional[_Mapping[str, _common_wfspec_pb2.VariableAssignment]] = ..., iterable: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ...) -> None: ...

class FailureHandlerDef(_message.Message):
    __slots__ = ["handler_spec_name", "specific_failure", "any_failure_of_type"]
    class LHFailureType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
        __slots__ = []
        FAILURE_TYPE_ERROR: _ClassVar[FailureHandlerDef.LHFailureType]
        FAILURE_TYPE_EXCEPTION: _ClassVar[FailureHandlerDef.LHFailureType]
    FAILURE_TYPE_ERROR: FailureHandlerDef.LHFailureType
    FAILURE_TYPE_EXCEPTION: FailureHandlerDef.LHFailureType
    HANDLER_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    SPECIFIC_FAILURE_FIELD_NUMBER: _ClassVar[int]
    ANY_FAILURE_OF_TYPE_FIELD_NUMBER: _ClassVar[int]
    handler_spec_name: str
    specific_failure: str
    any_failure_of_type: FailureHandlerDef.LHFailureType
    def __init__(self, handler_spec_name: _Optional[str] = ..., specific_failure: _Optional[str] = ..., any_failure_of_type: _Optional[_Union[FailureHandlerDef.LHFailureType, str]] = ...) -> None: ...

class WaitForThreadsNode(_message.Message):
    __slots__ = ["threads", "thread_list", "per_thread_failure_handlers"]
    class ThreadToWaitFor(_message.Message):
        __slots__ = ["thread_run_number"]
        THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
        thread_run_number: _common_wfspec_pb2.VariableAssignment
        def __init__(self, thread_run_number: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ...) -> None: ...
    class ThreadsToWaitFor(_message.Message):
        __slots__ = ["threads"]
        THREADS_FIELD_NUMBER: _ClassVar[int]
        threads: _containers.RepeatedCompositeFieldContainer[WaitForThreadsNode.ThreadToWaitFor]
        def __init__(self, threads: _Optional[_Iterable[_Union[WaitForThreadsNode.ThreadToWaitFor, _Mapping]]] = ...) -> None: ...
    THREADS_FIELD_NUMBER: _ClassVar[int]
    THREAD_LIST_FIELD_NUMBER: _ClassVar[int]
    PER_THREAD_FAILURE_HANDLERS_FIELD_NUMBER: _ClassVar[int]
    threads: WaitForThreadsNode.ThreadsToWaitFor
    thread_list: _common_wfspec_pb2.VariableAssignment
    per_thread_failure_handlers: _containers.RepeatedCompositeFieldContainer[FailureHandlerDef]
    def __init__(self, threads: _Optional[_Union[WaitForThreadsNode.ThreadsToWaitFor, _Mapping]] = ..., thread_list: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ..., per_thread_failure_handlers: _Optional[_Iterable[_Union[FailureHandlerDef, _Mapping]]] = ...) -> None: ...

class ExternalEventNode(_message.Message):
    __slots__ = ["external_event_def_id", "timeout_seconds"]
    EXTERNAL_EVENT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    TIMEOUT_SECONDS_FIELD_NUMBER: _ClassVar[int]
    external_event_def_id: _object_id_pb2.ExternalEventDefId
    timeout_seconds: _common_wfspec_pb2.VariableAssignment
    def __init__(self, external_event_def_id: _Optional[_Union[_object_id_pb2.ExternalEventDefId, _Mapping]] = ..., timeout_seconds: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ...) -> None: ...

class EntrypointNode(_message.Message):
    __slots__ = []
    def __init__(self) -> None: ...

class ExitNode(_message.Message):
    __slots__ = ["failure_def"]
    FAILURE_DEF_FIELD_NUMBER: _ClassVar[int]
    failure_def: FailureDef
    def __init__(self, failure_def: _Optional[_Union[FailureDef, _Mapping]] = ...) -> None: ...

class FailureDef(_message.Message):
    __slots__ = ["failure_name", "message", "content"]
    FAILURE_NAME_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    failure_name: str
    message: str
    content: _common_wfspec_pb2.VariableAssignment
    def __init__(self, failure_name: _Optional[str] = ..., message: _Optional[str] = ..., content: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ...) -> None: ...

class Node(_message.Message):
    __slots__ = ["outgoing_edges", "failure_handlers", "entrypoint", "exit", "task", "external_event", "start_thread", "wait_for_threads", "nop", "sleep", "user_task", "start_multiple_threads", "throw_event"]
    OUTGOING_EDGES_FIELD_NUMBER: _ClassVar[int]
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
    START_MULTIPLE_THREADS_FIELD_NUMBER: _ClassVar[int]
    THROW_EVENT_FIELD_NUMBER: _ClassVar[int]
    outgoing_edges: _containers.RepeatedCompositeFieldContainer[Edge]
    failure_handlers: _containers.RepeatedCompositeFieldContainer[FailureHandlerDef]
    entrypoint: EntrypointNode
    exit: ExitNode
    task: _common_wfspec_pb2.TaskNode
    external_event: ExternalEventNode
    start_thread: StartThreadNode
    wait_for_threads: WaitForThreadsNode
    nop: NopNode
    sleep: SleepNode
    user_task: UserTaskNode
    start_multiple_threads: StartMultipleThreadsNode
    throw_event: ThrowEventNode
    def __init__(self, outgoing_edges: _Optional[_Iterable[_Union[Edge, _Mapping]]] = ..., failure_handlers: _Optional[_Iterable[_Union[FailureHandlerDef, _Mapping]]] = ..., entrypoint: _Optional[_Union[EntrypointNode, _Mapping]] = ..., exit: _Optional[_Union[ExitNode, _Mapping]] = ..., task: _Optional[_Union[_common_wfspec_pb2.TaskNode, _Mapping]] = ..., external_event: _Optional[_Union[ExternalEventNode, _Mapping]] = ..., start_thread: _Optional[_Union[StartThreadNode, _Mapping]] = ..., wait_for_threads: _Optional[_Union[WaitForThreadsNode, _Mapping]] = ..., nop: _Optional[_Union[NopNode, _Mapping]] = ..., sleep: _Optional[_Union[SleepNode, _Mapping]] = ..., user_task: _Optional[_Union[UserTaskNode, _Mapping]] = ..., start_multiple_threads: _Optional[_Union[StartMultipleThreadsNode, _Mapping]] = ..., throw_event: _Optional[_Union[ThrowEventNode, _Mapping]] = ...) -> None: ...

class ThrowEventNode(_message.Message):
    __slots__ = ["event_def_id", "content"]
    EVENT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    event_def_id: _object_id_pb2.WorkflowEventDefId
    content: _common_wfspec_pb2.VariableAssignment
    def __init__(self, event_def_id: _Optional[_Union[_object_id_pb2.WorkflowEventDefId, _Mapping]] = ..., content: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ...) -> None: ...

class UserTaskNode(_message.Message):
    __slots__ = ["user_task_def_name", "user_group", "user_id", "actions", "user_task_def_version", "notes", "on_cancellation_exception_name"]
    USER_TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    USER_GROUP_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    ACTIONS_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_DEF_VERSION_FIELD_NUMBER: _ClassVar[int]
    NOTES_FIELD_NUMBER: _ClassVar[int]
    ON_CANCELLATION_EXCEPTION_NAME_FIELD_NUMBER: _ClassVar[int]
    user_task_def_name: str
    user_group: _common_wfspec_pb2.VariableAssignment
    user_id: _common_wfspec_pb2.VariableAssignment
    actions: _containers.RepeatedCompositeFieldContainer[_common_wfspec_pb2.UTActionTrigger]
    user_task_def_version: int
    notes: _common_wfspec_pb2.VariableAssignment
    on_cancellation_exception_name: _common_wfspec_pb2.VariableAssignment
    def __init__(self, user_task_def_name: _Optional[str] = ..., user_group: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ..., user_id: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ..., actions: _Optional[_Iterable[_Union[_common_wfspec_pb2.UTActionTrigger, _Mapping]]] = ..., user_task_def_version: _Optional[int] = ..., notes: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ..., on_cancellation_exception_name: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ...) -> None: ...

class EdgeCondition(_message.Message):
    __slots__ = ["comparator", "left", "right"]
    COMPARATOR_FIELD_NUMBER: _ClassVar[int]
    LEFT_FIELD_NUMBER: _ClassVar[int]
    RIGHT_FIELD_NUMBER: _ClassVar[int]
    comparator: _common_wfspec_pb2.Comparator
    left: _common_wfspec_pb2.VariableAssignment
    right: _common_wfspec_pb2.VariableAssignment
    def __init__(self, comparator: _Optional[_Union[_common_wfspec_pb2.Comparator, str]] = ..., left: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ..., right: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ...) -> None: ...

class Edge(_message.Message):
    __slots__ = ["sink_node_name", "condition", "variable_mutations"]
    SINK_NODE_NAME_FIELD_NUMBER: _ClassVar[int]
    CONDITION_FIELD_NUMBER: _ClassVar[int]
    VARIABLE_MUTATIONS_FIELD_NUMBER: _ClassVar[int]
    sink_node_name: str
    condition: EdgeCondition
    variable_mutations: _containers.RepeatedCompositeFieldContainer[_common_wfspec_pb2.VariableMutation]
    def __init__(self, sink_node_name: _Optional[str] = ..., condition: _Optional[_Union[EdgeCondition, _Mapping]] = ..., variable_mutations: _Optional[_Iterable[_Union[_common_wfspec_pb2.VariableMutation, _Mapping]]] = ...) -> None: ...

class NopNode(_message.Message):
    __slots__ = []
    def __init__(self) -> None: ...

class SleepNode(_message.Message):
    __slots__ = ["raw_seconds", "timestamp", "iso_date"]
    RAW_SECONDS_FIELD_NUMBER: _ClassVar[int]
    TIMESTAMP_FIELD_NUMBER: _ClassVar[int]
    ISO_DATE_FIELD_NUMBER: _ClassVar[int]
    raw_seconds: _common_wfspec_pb2.VariableAssignment
    timestamp: _common_wfspec_pb2.VariableAssignment
    iso_date: _common_wfspec_pb2.VariableAssignment
    def __init__(self, raw_seconds: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ..., timestamp: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ..., iso_date: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ...) -> None: ...

class WfSpecVersionMigration(_message.Message):
    __slots__ = ["new_major_version", "new_revision", "thread_spec_migrations"]
    class ThreadSpecMigrationsEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: ThreadSpecMigration
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[ThreadSpecMigration, _Mapping]] = ...) -> None: ...
    NEW_MAJOR_VERSION_FIELD_NUMBER: _ClassVar[int]
    NEW_REVISION_FIELD_NUMBER: _ClassVar[int]
    THREAD_SPEC_MIGRATIONS_FIELD_NUMBER: _ClassVar[int]
    new_major_version: int
    new_revision: int
    thread_spec_migrations: _containers.MessageMap[str, ThreadSpecMigration]
    def __init__(self, new_major_version: _Optional[int] = ..., new_revision: _Optional[int] = ..., thread_spec_migrations: _Optional[_Mapping[str, ThreadSpecMigration]] = ...) -> None: ...

class ThreadSpecMigration(_message.Message):
    __slots__ = ["new_thread_spec_name", "node_migrations"]
    class NodeMigrationsEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: NodeMigration
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[NodeMigration, _Mapping]] = ...) -> None: ...
    NEW_THREAD_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    NODE_MIGRATIONS_FIELD_NUMBER: _ClassVar[int]
    new_thread_spec_name: str
    node_migrations: _containers.MessageMap[str, NodeMigration]
    def __init__(self, new_thread_spec_name: _Optional[str] = ..., node_migrations: _Optional[_Mapping[str, NodeMigration]] = ...) -> None: ...

class NodeMigration(_message.Message):
    __slots__ = ["new_node_name"]
    NEW_NODE_NAME_FIELD_NUMBER: _ClassVar[int]
    new_node_name: str
    def __init__(self, new_node_name: _Optional[str] = ...) -> None: ...
