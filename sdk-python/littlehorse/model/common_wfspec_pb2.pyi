import littlehorse.model.common_enums_pb2 as _common_enums_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class VariableMutationType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    ASSIGN: _ClassVar[VariableMutationType]
    ADD: _ClassVar[VariableMutationType]
    EXTEND: _ClassVar[VariableMutationType]
    SUBTRACT: _ClassVar[VariableMutationType]
    MULTIPLY: _ClassVar[VariableMutationType]
    DIVIDE: _ClassVar[VariableMutationType]
    REMOVE_IF_PRESENT: _ClassVar[VariableMutationType]
    REMOVE_INDEX: _ClassVar[VariableMutationType]
    REMOVE_KEY: _ClassVar[VariableMutationType]

class Comparator(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    LESS_THAN: _ClassVar[Comparator]
    GREATER_THAN: _ClassVar[Comparator]
    LESS_THAN_EQ: _ClassVar[Comparator]
    GREATER_THAN_EQ: _ClassVar[Comparator]
    EQUALS: _ClassVar[Comparator]
    NOT_EQUALS: _ClassVar[Comparator]
    IN: _ClassVar[Comparator]
    NOT_IN: _ClassVar[Comparator]
ASSIGN: VariableMutationType
ADD: VariableMutationType
EXTEND: VariableMutationType
SUBTRACT: VariableMutationType
MULTIPLY: VariableMutationType
DIVIDE: VariableMutationType
REMOVE_IF_PRESENT: VariableMutationType
REMOVE_INDEX: VariableMutationType
REMOVE_KEY: VariableMutationType
LESS_THAN: Comparator
GREATER_THAN: Comparator
LESS_THAN_EQ: Comparator
GREATER_THAN_EQ: Comparator
EQUALS: Comparator
NOT_EQUALS: Comparator
IN: Comparator
NOT_IN: Comparator

class VariableAssignment(_message.Message):
    __slots__ = ["json_path", "variable_name", "literal_value", "format_string"]
    class FormatString(_message.Message):
        __slots__ = ["format", "args"]
        FORMAT_FIELD_NUMBER: _ClassVar[int]
        ARGS_FIELD_NUMBER: _ClassVar[int]
        format: VariableAssignment
        args: _containers.RepeatedCompositeFieldContainer[VariableAssignment]
        def __init__(self, format: _Optional[_Union[VariableAssignment, _Mapping]] = ..., args: _Optional[_Iterable[_Union[VariableAssignment, _Mapping]]] = ...) -> None: ...
    JSON_PATH_FIELD_NUMBER: _ClassVar[int]
    VARIABLE_NAME_FIELD_NUMBER: _ClassVar[int]
    LITERAL_VALUE_FIELD_NUMBER: _ClassVar[int]
    FORMAT_STRING_FIELD_NUMBER: _ClassVar[int]
    json_path: str
    variable_name: str
    literal_value: _variable_pb2.VariableValue
    format_string: VariableAssignment.FormatString
    def __init__(self, json_path: _Optional[str] = ..., variable_name: _Optional[str] = ..., literal_value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., format_string: _Optional[_Union[VariableAssignment.FormatString, _Mapping]] = ...) -> None: ...

class VariableMutation(_message.Message):
    __slots__ = ["lhs_name", "lhs_json_path", "operation", "source_variable", "literal_value", "node_output"]
    class NodeOutputSource(_message.Message):
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
    operation: VariableMutationType
    source_variable: VariableAssignment
    literal_value: _variable_pb2.VariableValue
    node_output: VariableMutation.NodeOutputSource
    def __init__(self, lhs_name: _Optional[str] = ..., lhs_json_path: _Optional[str] = ..., operation: _Optional[_Union[VariableMutationType, str]] = ..., source_variable: _Optional[_Union[VariableAssignment, _Mapping]] = ..., literal_value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., node_output: _Optional[_Union[VariableMutation.NodeOutputSource, _Mapping]] = ...) -> None: ...

class VariableDef(_message.Message):
    __slots__ = ["type", "name", "default_value", "masked_value"]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_VALUE_FIELD_NUMBER: _ClassVar[int]
    MASKED_VALUE_FIELD_NUMBER: _ClassVar[int]
    type: _common_enums_pb2.VariableType
    name: str
    default_value: _variable_pb2.VariableValue
    masked_value: bool
    def __init__(self, type: _Optional[_Union[_common_enums_pb2.VariableType, str]] = ..., name: _Optional[str] = ..., default_value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., masked_value: bool = ...) -> None: ...

class UTActionTrigger(_message.Message):
    __slots__ = ["task", "cancel", "reassign", "delay_seconds", "hook"]
    class UTHook(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
        __slots__ = []
        ON_ARRIVAL: _ClassVar[UTActionTrigger.UTHook]
        ON_TASK_ASSIGNED: _ClassVar[UTActionTrigger.UTHook]
    ON_ARRIVAL: UTActionTrigger.UTHook
    ON_TASK_ASSIGNED: UTActionTrigger.UTHook
    class UTACancel(_message.Message):
        __slots__ = []
        def __init__(self) -> None: ...
    class UTATask(_message.Message):
        __slots__ = ["task", "mutations"]
        TASK_FIELD_NUMBER: _ClassVar[int]
        MUTATIONS_FIELD_NUMBER: _ClassVar[int]
        task: TaskNode
        mutations: _containers.RepeatedCompositeFieldContainer[VariableMutation]
        def __init__(self, task: _Optional[_Union[TaskNode, _Mapping]] = ..., mutations: _Optional[_Iterable[_Union[VariableMutation, _Mapping]]] = ...) -> None: ...
    class UTAReassign(_message.Message):
        __slots__ = ["user_id", "user_group"]
        USER_ID_FIELD_NUMBER: _ClassVar[int]
        USER_GROUP_FIELD_NUMBER: _ClassVar[int]
        user_id: VariableAssignment
        user_group: VariableAssignment
        def __init__(self, user_id: _Optional[_Union[VariableAssignment, _Mapping]] = ..., user_group: _Optional[_Union[VariableAssignment, _Mapping]] = ...) -> None: ...
    TASK_FIELD_NUMBER: _ClassVar[int]
    CANCEL_FIELD_NUMBER: _ClassVar[int]
    REASSIGN_FIELD_NUMBER: _ClassVar[int]
    DELAY_SECONDS_FIELD_NUMBER: _ClassVar[int]
    HOOK_FIELD_NUMBER: _ClassVar[int]
    task: UTActionTrigger.UTATask
    cancel: UTActionTrigger.UTACancel
    reassign: UTActionTrigger.UTAReassign
    delay_seconds: VariableAssignment
    hook: UTActionTrigger.UTHook
    def __init__(self, task: _Optional[_Union[UTActionTrigger.UTATask, _Mapping]] = ..., cancel: _Optional[_Union[UTActionTrigger.UTACancel, _Mapping]] = ..., reassign: _Optional[_Union[UTActionTrigger.UTAReassign, _Mapping]] = ..., delay_seconds: _Optional[_Union[VariableAssignment, _Mapping]] = ..., hook: _Optional[_Union[UTActionTrigger.UTHook, str]] = ...) -> None: ...

class ExponentialBackoffRetryPolicy(_message.Message):
    __slots__ = ["base_interval_ms", "max_delay_ms", "multiplier"]
    BASE_INTERVAL_MS_FIELD_NUMBER: _ClassVar[int]
    MAX_DELAY_MS_FIELD_NUMBER: _ClassVar[int]
    MULTIPLIER_FIELD_NUMBER: _ClassVar[int]
    base_interval_ms: int
    max_delay_ms: int
    multiplier: float
    def __init__(self, base_interval_ms: _Optional[int] = ..., max_delay_ms: _Optional[int] = ..., multiplier: _Optional[float] = ...) -> None: ...

class TaskNode(_message.Message):
    __slots__ = ["task_def_id", "dynamic_task", "timeout_seconds", "retries", "exponential_backoff", "variables"]
    TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    DYNAMIC_TASK_FIELD_NUMBER: _ClassVar[int]
    TIMEOUT_SECONDS_FIELD_NUMBER: _ClassVar[int]
    RETRIES_FIELD_NUMBER: _ClassVar[int]
    EXPONENTIAL_BACKOFF_FIELD_NUMBER: _ClassVar[int]
    VARIABLES_FIELD_NUMBER: _ClassVar[int]
    task_def_id: _object_id_pb2.TaskDefId
    dynamic_task: VariableAssignment
    timeout_seconds: int
    retries: int
    exponential_backoff: ExponentialBackoffRetryPolicy
    variables: _containers.RepeatedCompositeFieldContainer[VariableAssignment]
    def __init__(self, task_def_id: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ..., dynamic_task: _Optional[_Union[VariableAssignment, _Mapping]] = ..., timeout_seconds: _Optional[int] = ..., retries: _Optional[int] = ..., exponential_backoff: _Optional[_Union[ExponentialBackoffRetryPolicy, _Mapping]] = ..., variables: _Optional[_Iterable[_Union[VariableAssignment, _Mapping]]] = ...) -> None: ...
