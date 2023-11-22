from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.common_wfspec_pb2 as _common_wfspec_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class TaskDef(_message.Message):
    __slots__ = ["id", "input_vars", "created_at"]
    ID_FIELD_NUMBER: _ClassVar[int]
    INPUT_VARS_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.TaskDefId
    input_vars: _containers.RepeatedCompositeFieldContainer[_common_wfspec_pb2.VariableDef]
    created_at: _timestamp_pb2.Timestamp
    def __init__(self, id: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ..., input_vars: _Optional[_Iterable[_Union[_common_wfspec_pb2.VariableDef, _Mapping]]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...
