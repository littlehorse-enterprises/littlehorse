import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class VariableValue(_message.Message):
    __slots__ = ("json_obj", "json_arr", "double", "bool", "str", "int", "bytes", "wf_run_id", "utc_timestamp", "struct")
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
    def __init__(self, json_obj: _Optional[str] = ..., json_arr: _Optional[str] = ..., double: _Optional[float] = ..., bool: _Optional[bool] = ..., str: _Optional[str] = ..., int: _Optional[int] = ..., bytes: _Optional[bytes] = ..., wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ..., utc_timestamp: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., struct: _Optional[_Union[Struct, _Mapping]] = ...) -> None: ...

class Variable(_message.Message):
    __slots__ = ("id", "value", "created_at", "wf_spec_id", "masked")
    ID_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    MASKED_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.VariableId
    value: VariableValue
    created_at: _timestamp_pb2.Timestamp
    wf_spec_id: _object_id_pb2.WfSpecId
    masked: bool
    def __init__(self, id: _Optional[_Union[_object_id_pb2.VariableId, _Mapping]] = ..., value: _Optional[_Union[VariableValue, _Mapping]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., wf_spec_id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., masked: _Optional[bool] = ...) -> None: ...

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
    __slots__ = ("value",)
    VALUE_FIELD_NUMBER: _ClassVar[int]
    value: VariableValue
    def __init__(self, value: _Optional[_Union[VariableValue, _Mapping]] = ...) -> None: ...
