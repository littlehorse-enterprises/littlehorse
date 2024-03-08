from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class WorkflowEvent(_message.Message):
    __slots__ = ["id", "content", "created_at"]
    ID_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.WorkflowEventId
    content: _variable_pb2.VariableValue
    created_at: _timestamp_pb2.Timestamp
    def __init__(self, id: _Optional[_Union[_object_id_pb2.WorkflowEventId, _Mapping]] = ..., content: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class WorkflowEventDef(_message.Message):
    __slots__ = ["id", "created_at", "type"]
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.WorkflowEventDefId
    created_at: _timestamp_pb2.Timestamp
    type: _common_enums_pb2.VariableType
    def __init__(self, id: _Optional[_Union[_object_id_pb2.WorkflowEventDefId, _Mapping]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., type: _Optional[_Union[_common_enums_pb2.VariableType, str]] = ...) -> None: ...
