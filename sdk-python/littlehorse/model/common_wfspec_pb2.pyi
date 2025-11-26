import littlehorse.model.common_enums_pb2 as _common_enums_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class VariableMutationType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
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
    __slots__ = ()
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
    __slots__ = ("json_path", "lh_path", "variable_name", "literal_value", "format_string", "node_output", "expression", "target_type")
    class FormatString(_message.Message):
        __slots__ = ("format", "args")
        FORMAT_FIELD_NUMBER: _ClassVar[int]
        ARGS_FIELD_NUMBER: _ClassVar[int]
        format: VariableAssignment
        args: _containers.RepeatedCompositeFieldContainer[VariableAssignment]
        def __init__(self, format: _Optional[_Union[VariableAssignment, _Mapping]] = ..., args: _Optional[_Iterable[_Union[VariableAssignment, _Mapping]]] = ...) -> None: ...
    class NodeOutputReference(_message.Message):
        __slots__ = ("node_name",)
        NODE_NAME_FIELD_NUMBER: _ClassVar[int]
        node_name: str
        def __init__(self, node_name: _Optional[str] = ...) -> None: ...
    class Expression(_message.Message):
        __slots__ = ("lhs", "operation", "rhs")
        LHS_FIELD_NUMBER: _ClassVar[int]
        OPERATION_FIELD_NUMBER: _ClassVar[int]
        RHS_FIELD_NUMBER: _ClassVar[int]
        lhs: VariableAssignment
        operation: VariableMutationType
        rhs: VariableAssignment
        def __init__(self, lhs: _Optional[_Union[VariableAssignment, _Mapping]] = ..., operation: _Optional[_Union[VariableMutationType, str]] = ..., rhs: _Optional[_Union[VariableAssignment, _Mapping]] = ...) -> None: ...
    JSON_PATH_FIELD_NUMBER: _ClassVar[int]
    LH_PATH_FIELD_NUMBER: _ClassVar[int]
    VARIABLE_NAME_FIELD_NUMBER: _ClassVar[int]
    LITERAL_VALUE_FIELD_NUMBER: _ClassVar[int]
    FORMAT_STRING_FIELD_NUMBER: _ClassVar[int]
    NODE_OUTPUT_FIELD_NUMBER: _ClassVar[int]
    EXPRESSION_FIELD_NUMBER: _ClassVar[int]
    TARGET_TYPE_FIELD_NUMBER: _ClassVar[int]
    json_path: str
    lh_path: LHPath
    variable_name: str
    literal_value: _variable_pb2.VariableValue
    format_string: VariableAssignment.FormatString
    node_output: VariableAssignment.NodeOutputReference
    expression: VariableAssignment.Expression
    target_type: TypeDefinition
    def __init__(self, json_path: _Optional[str] = ..., lh_path: _Optional[_Union[LHPath, _Mapping]] = ..., variable_name: _Optional[str] = ..., literal_value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., format_string: _Optional[_Union[VariableAssignment.FormatString, _Mapping]] = ..., node_output: _Optional[_Union[VariableAssignment.NodeOutputReference, _Mapping]] = ..., expression: _Optional[_Union[VariableAssignment.Expression, _Mapping]] = ..., target_type: _Optional[_Union[TypeDefinition, _Mapping]] = ...) -> None: ...

class VariableMutation(_message.Message):
    __slots__ = ("lhs_name", "lhs_json_path", "operation", "rhs_assignment", "literal_value", "node_output")
    class NodeOutputSource(_message.Message):
        __slots__ = ("jsonpath", "lh_path")
        JSONPATH_FIELD_NUMBER: _ClassVar[int]
        LH_PATH_FIELD_NUMBER: _ClassVar[int]
        jsonpath: str
        lh_path: LHPath
        def __init__(self, jsonpath: _Optional[str] = ..., lh_path: _Optional[_Union[LHPath, _Mapping]] = ...) -> None: ...
    LHS_NAME_FIELD_NUMBER: _ClassVar[int]
    LHS_JSON_PATH_FIELD_NUMBER: _ClassVar[int]
    OPERATION_FIELD_NUMBER: _ClassVar[int]
    RHS_ASSIGNMENT_FIELD_NUMBER: _ClassVar[int]
    LITERAL_VALUE_FIELD_NUMBER: _ClassVar[int]
    NODE_OUTPUT_FIELD_NUMBER: _ClassVar[int]
    lhs_name: str
    lhs_json_path: str
    operation: VariableMutationType
    rhs_assignment: VariableAssignment
    literal_value: _variable_pb2.VariableValue
    node_output: VariableMutation.NodeOutputSource
    def __init__(self, lhs_name: _Optional[str] = ..., lhs_json_path: _Optional[str] = ..., operation: _Optional[_Union[VariableMutationType, str]] = ..., rhs_assignment: _Optional[_Union[VariableAssignment, _Mapping]] = ..., literal_value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., node_output: _Optional[_Union[VariableMutation.NodeOutputSource, _Mapping]] = ...) -> None: ...

class VariableDef(_message.Message):
    __slots__ = ("type", "name", "default_value", "masked_value", "type_def")
    TYPE_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_VALUE_FIELD_NUMBER: _ClassVar[int]
    MASKED_VALUE_FIELD_NUMBER: _ClassVar[int]
    TYPE_DEF_FIELD_NUMBER: _ClassVar[int]
    type: _common_enums_pb2.VariableType
    name: str
    default_value: _variable_pb2.VariableValue
    masked_value: bool
    type_def: TypeDefinition
    def __init__(self, type: _Optional[_Union[_common_enums_pb2.VariableType, str]] = ..., name: _Optional[str] = ..., default_value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., masked_value: _Optional[bool] = ..., type_def: _Optional[_Union[TypeDefinition, _Mapping]] = ...) -> None: ...

class TypeDefinition(_message.Message):
    __slots__ = ("primitive_type", "struct_def_id", "masked")
    PRIMITIVE_TYPE_FIELD_NUMBER: _ClassVar[int]
    STRUCT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    MASKED_FIELD_NUMBER: _ClassVar[int]
    primitive_type: _common_enums_pb2.VariableType
    struct_def_id: _object_id_pb2.StructDefId
    masked: bool
    def __init__(self, primitive_type: _Optional[_Union[_common_enums_pb2.VariableType, str]] = ..., struct_def_id: _Optional[_Union[_object_id_pb2.StructDefId, _Mapping]] = ..., masked: _Optional[bool] = ...) -> None: ...

class ReturnType(_message.Message):
    __slots__ = ("return_type",)
    RETURN_TYPE_FIELD_NUMBER: _ClassVar[int]
    return_type: TypeDefinition
    def __init__(self, return_type: _Optional[_Union[TypeDefinition, _Mapping]] = ...) -> None: ...

class UTActionTrigger(_message.Message):
    __slots__ = ("task", "cancel", "reassign", "delay_seconds", "hook")
    class UTHook(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
        __slots__ = ()
        ON_ARRIVAL: _ClassVar[UTActionTrigger.UTHook]
        ON_TASK_ASSIGNED: _ClassVar[UTActionTrigger.UTHook]
    ON_ARRIVAL: UTActionTrigger.UTHook
    ON_TASK_ASSIGNED: UTActionTrigger.UTHook
    class UTACancel(_message.Message):
        __slots__ = ()
        def __init__(self) -> None: ...
    class UTATask(_message.Message):
        __slots__ = ("task", "mutations")
        TASK_FIELD_NUMBER: _ClassVar[int]
        MUTATIONS_FIELD_NUMBER: _ClassVar[int]
        task: TaskNode
        mutations: _containers.RepeatedCompositeFieldContainer[VariableMutation]
        def __init__(self, task: _Optional[_Union[TaskNode, _Mapping]] = ..., mutations: _Optional[_Iterable[_Union[VariableMutation, _Mapping]]] = ...) -> None: ...
    class UTAReassign(_message.Message):
        __slots__ = ("user_id", "user_group")
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
    __slots__ = ("base_interval_ms", "max_delay_ms", "multiplier")
    BASE_INTERVAL_MS_FIELD_NUMBER: _ClassVar[int]
    MAX_DELAY_MS_FIELD_NUMBER: _ClassVar[int]
    MULTIPLIER_FIELD_NUMBER: _ClassVar[int]
    base_interval_ms: int
    max_delay_ms: int
    multiplier: float
    def __init__(self, base_interval_ms: _Optional[int] = ..., max_delay_ms: _Optional[int] = ..., multiplier: _Optional[float] = ...) -> None: ...

class TaskNode(_message.Message):
    __slots__ = ("task_def_id", "dynamic_task", "timeout_seconds", "retries", "exponential_backoff", "variables")
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

class InlineStructDef(_message.Message):
    __slots__ = ("fields",)
    class FieldsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: StructFieldDef
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[StructFieldDef, _Mapping]] = ...) -> None: ...
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    fields: _containers.MessageMap[str, StructFieldDef]
    def __init__(self, fields: _Optional[_Mapping[str, StructFieldDef]] = ...) -> None: ...

class StructFieldDef(_message.Message):
    __slots__ = ("field_type", "default_value")
    FIELD_TYPE_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_VALUE_FIELD_NUMBER: _ClassVar[int]
    field_type: TypeDefinition
    default_value: _variable_pb2.VariableValue
    def __init__(self, field_type: _Optional[_Union[TypeDefinition, _Mapping]] = ..., default_value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ...) -> None: ...

class LHPath(_message.Message):
    __slots__ = ("path",)
    class Selector(_message.Message):
        __slots__ = ("key", "index")
        KEY_FIELD_NUMBER: _ClassVar[int]
        INDEX_FIELD_NUMBER: _ClassVar[int]
        key: str
        index: int
        def __init__(self, key: _Optional[str] = ..., index: _Optional[int] = ...) -> None: ...
    PATH_FIELD_NUMBER: _ClassVar[int]
    path: _containers.RepeatedCompositeFieldContainer[LHPath.Selector]
    def __init__(self, path: _Optional[_Iterable[_Union[LHPath.Selector, _Mapping]]] = ...) -> None: ...
