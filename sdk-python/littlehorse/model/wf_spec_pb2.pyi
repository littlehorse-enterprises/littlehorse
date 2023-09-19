from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
import littlehorse.model.common_wfspec_pb2 as _common_wfspec_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class WfSpec(_message.Message):
    __slots__ = ["name", "version", "created_at", "status", "thread_specs", "entrypoint_thread_name", "retention_hours"]
    class ThreadSpecsEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: ThreadSpec
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[ThreadSpec, _Mapping]] = ...) -> None: ...
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
    status: _common_enums_pb2.LHStatus
    thread_specs: _containers.MessageMap[str, ThreadSpec]
    entrypoint_thread_name: str
    retention_hours: int
    def __init__(self, name: _Optional[str] = ..., version: _Optional[int] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., status: _Optional[_Union[_common_enums_pb2.LHStatus, str]] = ..., thread_specs: _Optional[_Mapping[str, ThreadSpec]] = ..., entrypoint_thread_name: _Optional[str] = ..., retention_hours: _Optional[int] = ...) -> None: ...

class ThreadSpec(_message.Message):
    __slots__ = ["nodes", "variable_defs", "interrupt_defs"]
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
    nodes: _containers.MessageMap[str, Node]
    variable_defs: _containers.RepeatedCompositeFieldContainer[_common_wfspec_pb2.VariableDef]
    interrupt_defs: _containers.RepeatedCompositeFieldContainer[InterruptDef]
    def __init__(self, nodes: _Optional[_Mapping[str, Node]] = ..., variable_defs: _Optional[_Iterable[_Union[_common_wfspec_pb2.VariableDef, _Mapping]]] = ..., interrupt_defs: _Optional[_Iterable[_Union[InterruptDef, _Mapping]]] = ...) -> None: ...

class InterruptDef(_message.Message):
    __slots__ = ["external_event_def_name", "handler_spec_name"]
    EXTERNAL_EVENT_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    HANDLER_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    external_event_def_name: str
    handler_spec_name: str
    def __init__(self, external_event_def_name: _Optional[str] = ..., handler_spec_name: _Optional[str] = ...) -> None: ...

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
    __slots__ = ["threads", "thread_list", "policy"]
    class ThreadToWaitFor(_message.Message):
        __slots__ = ["thread_run_number"]
        THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
        thread_run_number: _common_wfspec_pb2.VariableAssignment
        def __init__(self, thread_run_number: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ...) -> None: ...
    THREADS_FIELD_NUMBER: _ClassVar[int]
    THREAD_LIST_FIELD_NUMBER: _ClassVar[int]
    POLICY_FIELD_NUMBER: _ClassVar[int]
    threads: _containers.RepeatedCompositeFieldContainer[WaitForThreadsNode.ThreadToWaitFor]
    thread_list: _common_wfspec_pb2.VariableAssignment
    policy: _common_enums_pb2.WaitForThreadsPolicy
    def __init__(self, threads: _Optional[_Iterable[_Union[WaitForThreadsNode.ThreadToWaitFor, _Mapping]]] = ..., thread_list: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ..., policy: _Optional[_Union[_common_enums_pb2.WaitForThreadsPolicy, str]] = ...) -> None: ...

class ExternalEventNode(_message.Message):
    __slots__ = ["external_event_def_name", "timeout_seconds"]
    EXTERNAL_EVENT_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    TIMEOUT_SECONDS_FIELD_NUMBER: _ClassVar[int]
    external_event_def_name: str
    timeout_seconds: _common_wfspec_pb2.VariableAssignment
    def __init__(self, external_event_def_name: _Optional[str] = ..., timeout_seconds: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ...) -> None: ...

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
    __slots__ = ["outgoing_edges", "variable_mutations", "failure_handlers", "entrypoint", "exit", "task", "external_event", "start_thread", "wait_for_threads", "nop", "sleep", "user_task", "start_multiple_threads"]
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
    START_MULTIPLE_THREADS_FIELD_NUMBER: _ClassVar[int]
    outgoing_edges: _containers.RepeatedCompositeFieldContainer[Edge]
    variable_mutations: _containers.RepeatedCompositeFieldContainer[_common_wfspec_pb2.VariableMutation]
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
    def __init__(self, outgoing_edges: _Optional[_Iterable[_Union[Edge, _Mapping]]] = ..., variable_mutations: _Optional[_Iterable[_Union[_common_wfspec_pb2.VariableMutation, _Mapping]]] = ..., failure_handlers: _Optional[_Iterable[_Union[FailureHandlerDef, _Mapping]]] = ..., entrypoint: _Optional[_Union[EntrypointNode, _Mapping]] = ..., exit: _Optional[_Union[ExitNode, _Mapping]] = ..., task: _Optional[_Union[_common_wfspec_pb2.TaskNode, _Mapping]] = ..., external_event: _Optional[_Union[ExternalEventNode, _Mapping]] = ..., start_thread: _Optional[_Union[StartThreadNode, _Mapping]] = ..., wait_for_threads: _Optional[_Union[WaitForThreadsNode, _Mapping]] = ..., nop: _Optional[_Union[NopNode, _Mapping]] = ..., sleep: _Optional[_Union[SleepNode, _Mapping]] = ..., user_task: _Optional[_Union[UserTaskNode, _Mapping]] = ..., start_multiple_threads: _Optional[_Union[StartMultipleThreadsNode, _Mapping]] = ...) -> None: ...

class UserTaskNode(_message.Message):
    __slots__ = ["user_task_def_name", "user_group", "user_id", "actions", "user_task_def_version", "notes"]
    USER_TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    USER_GROUP_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    ACTIONS_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_DEF_VERSION_FIELD_NUMBER: _ClassVar[int]
    NOTES_FIELD_NUMBER: _ClassVar[int]
    user_task_def_name: str
    user_group: _common_wfspec_pb2.VariableAssignment
    user_id: _common_wfspec_pb2.VariableAssignment
    actions: _containers.RepeatedCompositeFieldContainer[_common_wfspec_pb2.UTActionTrigger]
    user_task_def_version: int
    notes: _common_wfspec_pb2.VariableAssignment
    def __init__(self, user_task_def_name: _Optional[str] = ..., user_group: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ..., user_id: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ..., actions: _Optional[_Iterable[_Union[_common_wfspec_pb2.UTActionTrigger, _Mapping]]] = ..., user_task_def_version: _Optional[int] = ..., notes: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ...) -> None: ...

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
    __slots__ = ["sink_node_name", "condition"]
    SINK_NODE_NAME_FIELD_NUMBER: _ClassVar[int]
    CONDITION_FIELD_NUMBER: _ClassVar[int]
    sink_node_name: str
    condition: EdgeCondition
    def __init__(self, sink_node_name: _Optional[str] = ..., condition: _Optional[_Union[EdgeCondition, _Mapping]] = ...) -> None: ...

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
