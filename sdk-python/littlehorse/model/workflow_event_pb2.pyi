import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.common_wfspec_pb2 as _common_wfspec_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class WorkflowEvent(_message.Message):
    __slots__ = ("id", "content", "created_at", "node_run_id")
    ID_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    NODE_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.WorkflowEventId
    content: _variable_pb2.VariableValue
    created_at: _timestamp_pb2.Timestamp
    node_run_id: _object_id_pb2.NodeRunId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.WorkflowEventId, _Mapping]] = ..., content: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., node_run_id: _Optional[_Union[_object_id_pb2.NodeRunId, _Mapping]] = ...) -> None: ...

class WorkflowEventDef(_message.Message):
    __slots__ = ("id", "created_at", "content_type")
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    CONTENT_TYPE_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.WorkflowEventDefId
    created_at: _timestamp_pb2.Timestamp
    content_type: _common_wfspec_pb2.ReturnType
    def __init__(self, id: _Optional[_Union[_object_id_pb2.WorkflowEventDefId, _Mapping]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., content_type: _Optional[_Union[_common_wfspec_pb2.ReturnType, _Mapping]] = ...) -> None: ...
