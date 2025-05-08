import littlehorse.model.common_wfspec_pb2 as _common_wfspec_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class StructDef(_message.Message):
    __slots__ = ["id", "description", "struct_def"]
    ID_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    STRUCT_DEF_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.StructDefId
    description: str
    struct_def: InlineStructDef
    def __init__(self, id: _Optional[_Union[_object_id_pb2.StructDefId, _Mapping]] = ..., description: _Optional[str] = ..., struct_def: _Optional[_Union[InlineStructDef, _Mapping]] = ...) -> None: ...

class InlineStructDef(_message.Message):
    __slots__ = ["fields"]
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    fields: _containers.RepeatedCompositeFieldContainer[StructFieldDef]
    def __init__(self, fields: _Optional[_Iterable[_Union[StructFieldDef, _Mapping]]] = ...) -> None: ...

class StructFieldDef(_message.Message):
    __slots__ = ["name", "optional", "field_type"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    OPTIONAL_FIELD_NUMBER: _ClassVar[int]
    FIELD_TYPE_FIELD_NUMBER: _ClassVar[int]
    name: str
    optional: bool
    field_type: _common_wfspec_pb2.TypeDefinition
    def __init__(self, name: _Optional[str] = ..., optional: bool = ..., field_type: _Optional[_Union[_common_wfspec_pb2.TypeDefinition, _Mapping]] = ...) -> None: ...
