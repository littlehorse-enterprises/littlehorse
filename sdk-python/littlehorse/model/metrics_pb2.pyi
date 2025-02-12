from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf import duration_pb2 as _duration_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class MeasurableObject(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    WORKFLOW: _ClassVar[MeasurableObject]
    TASK: _ClassVar[MeasurableObject]

class MetricType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    COUNT: _ClassVar[MetricType]
    AVG: _ClassVar[MetricType]
    RATIO: _ClassVar[MetricType]
WORKFLOW: MeasurableObject
TASK: MeasurableObject
COUNT: MetricType
AVG: MetricType
RATIO: MetricType

class Metric(_message.Message):
    __slots__ = ["id", "created_at", "window_length"]
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    WINDOW_LENGTH_FIELD_NUMBER: _ClassVar[int]
    id: MetricId
    created_at: _timestamp_pb2.Timestamp
    window_length: _duration_pb2.Duration
    def __init__(self, id: _Optional[_Union[MetricId, _Mapping]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., window_length: _Optional[_Union[_duration_pb2.Duration, _Mapping]] = ...) -> None: ...

class MetricId(_message.Message):
    __slots__ = ["measurable", "type"]
    MEASURABLE_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    measurable: MeasurableObject
    type: MetricType
    def __init__(self, measurable: _Optional[_Union[MeasurableObject, str]] = ..., type: _Optional[_Union[MetricType, str]] = ...) -> None: ...

class PartitionMetric(_message.Message):
    __slots__ = ["id", "created_at", "active_windows", "window_length"]
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    ACTIVE_WINDOWS_FIELD_NUMBER: _ClassVar[int]
    WINDOW_LENGTH_FIELD_NUMBER: _ClassVar[int]
    id: PartitionMetricId
    created_at: _timestamp_pb2.Timestamp
    active_windows: _containers.RepeatedCompositeFieldContainer[PartitionWindowedMetric]
    window_length: _duration_pb2.Duration
    def __init__(self, id: _Optional[_Union[PartitionMetricId, _Mapping]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., active_windows: _Optional[_Iterable[_Union[PartitionWindowedMetric, _Mapping]]] = ..., window_length: _Optional[_Union[_duration_pb2.Duration, _Mapping]] = ...) -> None: ...

class PartitionWindowedMetric(_message.Message):
    __slots__ = ["value", "window_start"]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    value: float
    window_start: _timestamp_pb2.Timestamp
    def __init__(self, value: _Optional[float] = ..., window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class PartitionMetricId(_message.Message):
    __slots__ = ["id", "tenant_id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    TENANT_ID_FIELD_NUMBER: _ClassVar[int]
    id: MetricId
    tenant_id: _object_id_pb2.TenantId
    def __init__(self, id: _Optional[_Union[MetricId, _Mapping]] = ..., tenant_id: _Optional[_Union[_object_id_pb2.TenantId, _Mapping]] = ...) -> None: ...
