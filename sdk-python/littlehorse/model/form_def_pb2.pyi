import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.common_wfspec_pb2 as _common_wfspec_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.type_definition_pb2 as _type_definition_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class FormDef(_message.Message):
    __slots__ = ("id", "result_struct_def_id", "fields", "created_at", "metadata")
    class MetadataEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: FormMetadataValue
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[FormMetadataValue, _Mapping]] = ...) -> None: ...
    ID_FIELD_NUMBER: _ClassVar[int]
    RESULT_STRUCT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    METADATA_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.FormDefId
    result_struct_def_id: _object_id_pb2.StructDefId
    fields: _containers.RepeatedCompositeFieldContainer[FormFieldDef]
    created_at: _timestamp_pb2.Timestamp
    metadata: _containers.MessageMap[str, FormMetadataValue]
    def __init__(self, id: _Optional[_Union[_object_id_pb2.FormDefId, _Mapping]] = ..., result_struct_def_id: _Optional[_Union[_object_id_pb2.StructDefId, _Mapping]] = ..., fields: _Optional[_Iterable[_Union[FormFieldDef, _Mapping]]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., metadata: _Optional[_Mapping[str, FormMetadataValue]] = ...) -> None: ...

class FormFieldDef(_message.Message):
    __slots__ = ("field_path", "metadata")
    class MetadataEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: FormMetadataValue
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[FormMetadataValue, _Mapping]] = ...) -> None: ...
    FIELD_PATH_FIELD_NUMBER: _ClassVar[int]
    METADATA_FIELD_NUMBER: _ClassVar[int]
    field_path: _common_wfspec_pb2.LHPath
    metadata: _containers.MessageMap[str, FormMetadataValue]
    def __init__(self, field_path: _Optional[_Union[_common_wfspec_pb2.LHPath, _Mapping]] = ..., metadata: _Optional[_Mapping[str, FormMetadataValue]] = ...) -> None: ...

class FormMetadataValue(_message.Message):
    __slots__ = ("literal", "placeholder")
    LITERAL_FIELD_NUMBER: _ClassVar[int]
    PLACEHOLDER_FIELD_NUMBER: _ClassVar[int]
    literal: _type_definition_pb2.VariableValue
    placeholder: str
    def __init__(self, literal: _Optional[_Union[_type_definition_pb2.VariableValue, _Mapping]] = ..., placeholder: _Optional[str] = ...) -> None: ...

class FormDefReference(_message.Message):
    __slots__ = ("name", "version")
    NAME_FIELD_NUMBER: _ClassVar[int]
    VERSION_FIELD_NUMBER: _ClassVar[int]
    name: str
    version: int
    def __init__(self, name: _Optional[str] = ..., version: _Optional[int] = ...) -> None: ...

class Form(_message.Message):
    __slots__ = ("form_def_id", "fields", "metadata")
    class MetadataEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _type_definition_pb2.VariableValue
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_type_definition_pb2.VariableValue, _Mapping]] = ...) -> None: ...
    FORM_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    METADATA_FIELD_NUMBER: _ClassVar[int]
    form_def_id: _object_id_pb2.FormDefId
    fields: _containers.RepeatedCompositeFieldContainer[FormField]
    metadata: _containers.MessageMap[str, _type_definition_pb2.VariableValue]
    def __init__(self, form_def_id: _Optional[_Union[_object_id_pb2.FormDefId, _Mapping]] = ..., fields: _Optional[_Iterable[_Union[FormField, _Mapping]]] = ..., metadata: _Optional[_Mapping[str, _type_definition_pb2.VariableValue]] = ...) -> None: ...

class FormField(_message.Message):
    __slots__ = ("field_path", "metadata")
    class MetadataEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _type_definition_pb2.VariableValue
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_type_definition_pb2.VariableValue, _Mapping]] = ...) -> None: ...
    FIELD_PATH_FIELD_NUMBER: _ClassVar[int]
    METADATA_FIELD_NUMBER: _ClassVar[int]
    field_path: _common_wfspec_pb2.LHPath
    metadata: _containers.MessageMap[str, _type_definition_pb2.VariableValue]
    def __init__(self, field_path: _Optional[_Union[_common_wfspec_pb2.LHPath, _Mapping]] = ..., metadata: _Optional[_Mapping[str, _type_definition_pb2.VariableValue]] = ...) -> None: ...
