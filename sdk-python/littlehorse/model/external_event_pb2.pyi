from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ExternalEvent(_message.Message):
    __slots__ = ["wf_run_id", "external_event_def_name", "guid", "created_at", "content", "thread_run_number", "node_run_position", "claimed"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    GUID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    NODE_RUN_POSITION_FIELD_NUMBER: _ClassVar[int]
    CLAIMED_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    external_event_def_name: str
    guid: str
    created_at: _timestamp_pb2.Timestamp
    content: _variable_pb2.VariableValue
    thread_run_number: int
    node_run_position: int
    claimed: bool
    def __init__(self, wf_run_id: _Optional[str] = ..., external_event_def_name: _Optional[str] = ..., guid: _Optional[str] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., content: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., thread_run_number: _Optional[int] = ..., node_run_position: _Optional[int] = ..., claimed: bool = ...) -> None: ...

class ExternalEventDef(_message.Message):
    __slots__ = ["name", "created_at", "retention_hours"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    RETENTION_HOURS_FIELD_NUMBER: _ClassVar[int]
    name: str
    created_at: _timestamp_pb2.Timestamp
    retention_hours: int
    def __init__(self, name: _Optional[str] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., retention_hours: _Optional[int] = ...) -> None: ...
