import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.common_wfspec_pb2 as _common_wfspec_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ExternalEvent(_message.Message):
    __slots__ = ("id", "created_at", "content", "thread_run_number", "node_run_position", "claimed")
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
    def __init__(self, id: _Optional[_Union[_object_id_pb2.ExternalEventId, _Mapping]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., content: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., thread_run_number: _Optional[int] = ..., node_run_position: _Optional[int] = ..., claimed: _Optional[bool] = ...) -> None: ...

class ExternalEventDef(_message.Message):
    __slots__ = ("id", "created_at", "retention_policy", "type_information", "correlated_event_config")
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    RETENTION_POLICY_FIELD_NUMBER: _ClassVar[int]
    TYPE_INFORMATION_FIELD_NUMBER: _ClassVar[int]
    CORRELATED_EVENT_CONFIG_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.ExternalEventDefId
    created_at: _timestamp_pb2.Timestamp
    retention_policy: ExternalEventRetentionPolicy
    type_information: _common_wfspec_pb2.ReturnType
    correlated_event_config: CorrelatedEventConfig
    def __init__(self, id: _Optional[_Union[_object_id_pb2.ExternalEventDefId, _Mapping]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., retention_policy: _Optional[_Union[ExternalEventRetentionPolicy, _Mapping]] = ..., type_information: _Optional[_Union[_common_wfspec_pb2.ReturnType, _Mapping]] = ..., correlated_event_config: _Optional[_Union[CorrelatedEventConfig, _Mapping]] = ...) -> None: ...

class CorrelatedEventConfig(_message.Message):
    __slots__ = ("ttl_seconds", "delete_after_first_correlation")
    TTL_SECONDS_FIELD_NUMBER: _ClassVar[int]
    DELETE_AFTER_FIRST_CORRELATION_FIELD_NUMBER: _ClassVar[int]
    ttl_seconds: int
    delete_after_first_correlation: bool
    def __init__(self, ttl_seconds: _Optional[int] = ..., delete_after_first_correlation: _Optional[bool] = ...) -> None: ...

class CorrelatedEvent(_message.Message):
    __slots__ = ("id", "created_at", "content", "external_events")
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENTS_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.CorrelatedEventId
    created_at: _timestamp_pb2.Timestamp
    content: _variable_pb2.VariableValue
    external_events: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.ExternalEventId]
    def __init__(self, id: _Optional[_Union[_object_id_pb2.CorrelatedEventId, _Mapping]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., content: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ..., external_events: _Optional[_Iterable[_Union[_object_id_pb2.ExternalEventId, _Mapping]]] = ...) -> None: ...

class ExternalEventRetentionPolicy(_message.Message):
    __slots__ = ("seconds_after_put",)
    SECONDS_AFTER_PUT_FIELD_NUMBER: _ClassVar[int]
    seconds_after_put: int
    def __init__(self, seconds_after_put: _Optional[int] = ...) -> None: ...
