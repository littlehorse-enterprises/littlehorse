from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class VariableValue(_message.Message):
    __slots__ = ["type", "json_obj", "json_arr", "double", "bool", "str", "int", "bytes"]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    JSON_OBJ_FIELD_NUMBER: _ClassVar[int]
    JSON_ARR_FIELD_NUMBER: _ClassVar[int]
    DOUBLE_FIELD_NUMBER: _ClassVar[int]
    BOOL_FIELD_NUMBER: _ClassVar[int]
    STR_FIELD_NUMBER: _ClassVar[int]
    INT_FIELD_NUMBER: _ClassVar[int]
    BYTES_FIELD_NUMBER: _ClassVar[int]
    type: _common_enums_pb2.VariableType
    json_obj: str
    json_arr: str
    double: float
    bool: bool
    str: str
    int: int
    bytes: bytes
    def __init__(self, type: _Optional[_Union[_common_enums_pb2.VariableType, str]] = ..., json_obj: _Optional[str] = ..., json_arr: _Optional[str] = ..., double: _Optional[float] = ..., bool: bool = ..., str: _Optional[str] = ..., int: _Optional[int] = ..., bytes: _Optional[bytes] = ...) -> None: ...

class Variable(_message.Message):
    __slots__ = ["id", "value", "created_at", "wf_spec_id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.VariableId
    value: VariableValue
    created_at: _timestamp_pb2.Timestamp
    wf_spec_id: _object_id_pb2.WfSpecId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.VariableId, _Mapping]] = ..., value: _Optional[_Union[VariableValue, _Mapping]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., wf_spec_id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ...) -> None: ...
