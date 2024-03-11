from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ExternalEvent(_message.Message):
    __slots__ = ["id", "created_at", "content", "thread_run_number", "node_run_position", "claimed"]
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    NODE_RUN_POSITION_FIELD_NUMBER: _ClassVar[int]
    CLAIMED_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.ExternalEventId
    created_at: _timestamp_pb2.Timestamp
    content: _variable_pb2.VariableValue
    thread_run_number: int
    node_run_position: int
    claimed: bool
    def __init__(self, id: _Optional[_Union[_object_id_pb2.ExternalEventId, _Mapping]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., content: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., thread_run_number: _Optional[int] = ..., node_run_position: _Optional[int] = ..., claimed: bool = ...) -> None: ...

class ExternalEventDef(_message.Message):
    __slots__ = ["id", "created_at", "retention_policy"]
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    RETENTION_POLICY_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.ExternalEventDefId
    created_at: _timestamp_pb2.Timestamp
    retention_policy: ExternalEventRetentionPolicy
    def __init__(self, id: _Optional[_Union[_object_id_pb2.ExternalEventDefId, _Mapping]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., retention_policy: _Optional[_Union[ExternalEventRetentionPolicy, _Mapping]] = ...) -> None: ...

class ExternalEventRetentionPolicy(_message.Message):
    __slots__ = ["seconds_after_put"]
    SECONDS_AFTER_PUT_FIELD_NUMBER: _ClassVar[int]
    seconds_after_put: int
    def __init__(self, seconds_after_put: _Optional[int] = ...) -> None: ...
