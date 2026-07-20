import datetime

import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

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
LESS_THAN: Comparator
GREATER_THAN: Comparator
LESS_THAN_EQ: Comparator
GREATER_THAN_EQ: Comparator
EQUALS: Comparator
NOT_EQUALS: Comparator
IN: Comparator
NOT_IN: Comparator

class TypeDefinition(_message.Message):
    __slots__ = ("primitive_type", "struct_def_id", "inline_array_def", "inline_struct_def", "inline_map_def", "masked")
    PRIMITIVE_TYPE_FIELD_NUMBER: _ClassVar[int]
    STRUCT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    INLINE_ARRAY_DEF_FIELD_NUMBER: _ClassVar[int]
    INLINE_STRUCT_DEF_FIELD_NUMBER: _ClassVar[int]
    INLINE_MAP_DEF_FIELD_NUMBER: _ClassVar[int]
    MASKED_FIELD_NUMBER: _ClassVar[int]
    primitive_type: _common_enums_pb2.VariableType
    struct_def_id: _object_id_pb2.StructDefId
    inline_array_def: InlineArrayDef
    inline_struct_def: InlineStructDef
    inline_map_def: InlineMapDef
    masked: bool
    def __init__(self, primitive_type: _Optional[_Union[_common_enums_pb2.VariableType, str]] = ..., struct_def_id: _Optional[_Union[_object_id_pb2.StructDefId, _Mapping]] = ..., inline_array_def: _Optional[_Union[InlineArrayDef, _Mapping]] = ..., inline_struct_def: _Optional[_Union[InlineStructDef, _Mapping]] = ..., inline_map_def: _Optional[_Union[InlineMapDef, _Mapping]] = ..., masked: _Optional[bool] = ...) -> None: ...

class InlineArrayDef(_message.Message):
    __slots__ = ("array_type",)
    ARRAY_TYPE_FIELD_NUMBER: _ClassVar[int]
    array_type: TypeDefinition
    def __init__(self, array_type: _Optional[_Union[TypeDefinition, _Mapping]] = ...) -> None: ...

class InlineMapDef(_message.Message):
    __slots__ = ("key_type", "value_type")
    KEY_TYPE_FIELD_NUMBER: _ClassVar[int]
    VALUE_TYPE_FIELD_NUMBER: _ClassVar[int]
    key_type: TypeDefinition
    value_type: TypeDefinition
    def __init__(self, key_type: _Optional[_Union[TypeDefinition, _Mapping]] = ..., value_type: _Optional[_Union[TypeDefinition, _Mapping]] = ...) -> None: ...

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
    __slots__ = ("field_type", "default_value", "is_nullable")
    FIELD_TYPE_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_VALUE_FIELD_NUMBER: _ClassVar[int]
    IS_NULLABLE_FIELD_NUMBER: _ClassVar[int]
    field_type: TypeDefinition
    default_value: VariableValue
    is_nullable: bool
    def __init__(self, field_type: _Optional[_Union[TypeDefinition, _Mapping]] = ..., default_value: _Optional[_Union[VariableValue, _Mapping]] = ..., is_nullable: _Optional[bool] = ...) -> None: ...

class VariableValue(_message.Message):
    __slots__ = ("json_obj", "json_arr", "double", "bool", "str", "int", "bytes", "wf_run_id", "utc_timestamp", "struct", "array", "map")
    JSON_OBJ_FIELD_NUMBER: _ClassVar[int]
    JSON_ARR_FIELD_NUMBER: _ClassVar[int]
    DOUBLE_FIELD_NUMBER: _ClassVar[int]
    BOOL_FIELD_NUMBER: _ClassVar[int]
    STR_FIELD_NUMBER: _ClassVar[int]
    INT_FIELD_NUMBER: _ClassVar[int]
    BYTES_FIELD_NUMBER: _ClassVar[int]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    UTC_TIMESTAMP_FIELD_NUMBER: _ClassVar[int]
    STRUCT_FIELD_NUMBER: _ClassVar[int]
    ARRAY_FIELD_NUMBER: _ClassVar[int]
    MAP_FIELD_NUMBER: _ClassVar[int]
    json_obj: str
    json_arr: str
    double: float
    bool: bool
    str: str
    int: int
    bytes: bytes
    wf_run_id: _object_id_pb2.WfRunId
    utc_timestamp: _timestamp_pb2.Timestamp
    struct: Struct
    array: Array
    map: Map
    def __init__(self, json_obj: _Optional[str] = ..., json_arr: _Optional[str] = ..., double: _Optional[float] = ..., bool: _Optional[bool] = ..., str: _Optional[str] = ..., int: _Optional[int] = ..., bytes: _Optional[bytes] = ..., wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ..., utc_timestamp: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., struct: _Optional[_Union[Struct, _Mapping]] = ..., array: _Optional[_Union[Array, _Mapping]] = ..., map: _Optional[_Union[Map, _Mapping]] = ...) -> None: ...

class Map(_message.Message):
    __slots__ = ("entries", "map_type")
    class Entry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: VariableValue
        value: VariableValue
        def __init__(self, key: _Optional[_Union[VariableValue, _Mapping]] = ..., value: _Optional[_Union[VariableValue, _Mapping]] = ...) -> None: ...
    ENTRIES_FIELD_NUMBER: _ClassVar[int]
    MAP_TYPE_FIELD_NUMBER: _ClassVar[int]
    entries: _containers.RepeatedCompositeFieldContainer[Map.Entry]
    map_type: InlineMapDef
    def __init__(self, entries: _Optional[_Iterable[_Union[Map.Entry, _Mapping]]] = ..., map_type: _Optional[_Union[InlineMapDef, _Mapping]] = ...) -> None: ...

class Array(_message.Message):
    __slots__ = ("items", "element_type")
    ITEMS_FIELD_NUMBER: _ClassVar[int]
    ELEMENT_TYPE_FIELD_NUMBER: _ClassVar[int]
    items: _containers.RepeatedCompositeFieldContainer[VariableValue]
    element_type: TypeDefinition
    def __init__(self, items: _Optional[_Iterable[_Union[VariableValue, _Mapping]]] = ..., element_type: _Optional[_Union[TypeDefinition, _Mapping]] = ...) -> None: ...

class Struct(_message.Message):
    __slots__ = ("struct_def_id", "struct")
    STRUCT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    STRUCT_FIELD_NUMBER: _ClassVar[int]
    struct_def_id: _object_id_pb2.StructDefId
    struct: InlineStruct
    def __init__(self, struct_def_id: _Optional[_Union[_object_id_pb2.StructDefId, _Mapping]] = ..., struct: _Optional[_Union[InlineStruct, _Mapping]] = ...) -> None: ...

class InlineStruct(_message.Message):
    __slots__ = ("fields",)
    class FieldsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: StructField
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[StructField, _Mapping]] = ...) -> None: ...
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    fields: _containers.MessageMap[str, StructField]
    def __init__(self, fields: _Optional[_Mapping[str, StructField]] = ...) -> None: ...

class StructField(_message.Message):
    __slots__ = ("value", "masked")
    VALUE_FIELD_NUMBER: _ClassVar[int]
    MASKED_FIELD_NUMBER: _ClassVar[int]
    value: VariableValue
    masked: bool
    def __init__(self, value: _Optional[_Union[VariableValue, _Mapping]] = ..., masked: _Optional[bool] = ...) -> None: ...

class ReturnType(_message.Message):
    __slots__ = ("return_type",)
    RETURN_TYPE_FIELD_NUMBER: _ClassVar[int]
    return_type: TypeDefinition
    def __init__(self, return_type: _Optional[_Union[TypeDefinition, _Mapping]] = ...) -> None: ...
