import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Mapping as _Mapping
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
    __slots__ = ("primitive_type", "struct_def_id", "inline_array_def", "masked")
    PRIMITIVE_TYPE_FIELD_NUMBER: _ClassVar[int]
    STRUCT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    INLINE_ARRAY_DEF_FIELD_NUMBER: _ClassVar[int]
    MASKED_FIELD_NUMBER: _ClassVar[int]
    primitive_type: _common_enums_pb2.VariableType
    struct_def_id: _object_id_pb2.StructDefId
    inline_array_def: InlineArrayDef
    masked: bool
    def __init__(self, primitive_type: _Optional[_Union[_common_enums_pb2.VariableType, str]] = ..., struct_def_id: _Optional[_Union[_object_id_pb2.StructDefId, _Mapping]] = ..., inline_array_def: _Optional[_Union[InlineArrayDef, _Mapping]] = ..., masked: _Optional[bool] = ...) -> None: ...

class InlineArrayDef(_message.Message):
    __slots__ = ("array_type",)
    ARRAY_TYPE_FIELD_NUMBER: _ClassVar[int]
    array_type: TypeDefinition
    def __init__(self, array_type: _Optional[_Union[TypeDefinition, _Mapping]] = ...) -> None: ...

class ReturnType(_message.Message):
    __slots__ = ("return_type",)
    RETURN_TYPE_FIELD_NUMBER: _ClassVar[int]
    return_type: TypeDefinition
    def __init__(self, return_type: _Optional[_Union[TypeDefinition, _Mapping]] = ...) -> None: ...
