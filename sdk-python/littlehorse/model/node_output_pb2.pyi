import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class NodeOutput(_message.Message):
    __slots__ = ("id", "value", "created_at", "wf_spec_id", "node_run_position")
    ID_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    NODE_RUN_POSITION_FIELD_NUMBER: _ClassVar[int]
    id: NodeOutputId
    value: _variable_pb2.VariableValue
    created_at: _timestamp_pb2.Timestamp
    wf_spec_id: _object_id_pb2.WfSpecId
    node_run_position: int
    def __init__(self, id: _Optional[_Union[NodeOutputId, _Mapping]] = ..., value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., wf_spec_id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., node_run_position: _Optional[int] = ...) -> None: ...

class NodeOutputId(_message.Message):
    __slots__ = ("wf_run_id", "thread_run_number", "node_name")
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    NODE_NAME_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: _object_id_pb2.WfRunId
    thread_run_number: int
    node_name: str
    def __init__(self, wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ..., thread_run_number: _Optional[int] = ..., node_name: _Optional[str] = ...) -> None: ...
