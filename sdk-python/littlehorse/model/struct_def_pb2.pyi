import littlehorse.model.common_wfspec_pb2 as _common_wfspec_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class StructDef(_message.Message):
    __slots__ = ["id", "description", "created_at", "struct_def"]
    ID_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    STRUCT_DEF_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.StructDefId
    description: str
    created_at: _timestamp_pb2.Timestamp
    struct_def: InlineStructDef
    def __init__(self, id: _Optional[_Union[_object_id_pb2.StructDefId, _Mapping]] = ..., description: _Optional[str] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., struct_def: _Optional[_Union[InlineStructDef, _Mapping]] = ...) -> None: ...

class InlineStructDef(_message.Message):
    __slots__ = ["fields"]
    class FieldsEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: StructFieldDef
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[StructFieldDef, _Mapping]] = ...) -> None: ...
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    fields: _containers.MessageMap[str, StructFieldDef]
    def __init__(self, fields: _Optional[_Mapping[str, StructFieldDef]] = ...) -> None: ...

class StructFieldDef(_message.Message):
    __slots__ = ["field_type", "default_value"]
    FIELD_TYPE_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_VALUE_FIELD_NUMBER: _ClassVar[int]
    field_type: _common_wfspec_pb2.TypeDefinition
    default_value: _variable_pb2.VariableValue
    def __init__(self, field_type: _Optional[_Union[_common_wfspec_pb2.TypeDefinition, _Mapping]] = ..., default_value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ...) -> None: ...
