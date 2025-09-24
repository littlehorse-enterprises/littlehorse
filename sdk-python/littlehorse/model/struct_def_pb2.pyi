import datetime

import littlehorse.model.common_wfspec_pb2 as _common_wfspec_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class StructDef(_message.Message):
    __slots__ = ("id", "description", "created_at", "struct_def")
    ID_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    STRUCT_DEF_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.StructDefId
    description: str
    created_at: _timestamp_pb2.Timestamp
    struct_def: _common_wfspec_pb2.InlineStructDef
    def __init__(self, id: _Optional[_Union[_object_id_pb2.StructDefId, _Mapping]] = ..., description: _Optional[str] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., struct_def: _Optional[_Union[_common_wfspec_pb2.InlineStructDef, _Mapping]] = ...) -> None: ...
