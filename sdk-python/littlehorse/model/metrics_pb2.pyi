from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf import duration_pb2 as _duration_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class Metric(_message.Message):
    __slots__ = ["id", "created_at", "window_length"]
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    WINDOW_LENGTH_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.MetricId
    created_at: _timestamp_pb2.Timestamp
    window_length: _duration_pb2.Duration
    def __init__(self, id: _Optional[_Union[_object_id_pb2.MetricId, _Mapping]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., window_length: _Optional[_Union[_duration_pb2.Duration, _Mapping]] = ...) -> None: ...

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
    __slots__ = ["value", "window_start", "number_of_samples"]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    NUMBER_OF_SAMPLES_FIELD_NUMBER: _ClassVar[int]
    value: float
    window_start: _timestamp_pb2.Timestamp
    number_of_samples: int
    def __init__(self, value: _Optional[float] = ..., window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., number_of_samples: _Optional[int] = ...) -> None: ...

class PartitionMetricId(_message.Message):
    __slots__ = ["id", "tenant_id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    TENANT_ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.MetricId
    tenant_id: _object_id_pb2.TenantId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.MetricId, _Mapping]] = ..., tenant_id: _Optional[_Union[_object_id_pb2.TenantId, _Mapping]] = ...) -> None: ...

class MetricRun(_message.Message):
    __slots__ = ["id", "count", "latency_avg", "created_at", "value_per_partition"]
    class ValuePerPartitionEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: int
        value: float
        def __init__(self, key: _Optional[int] = ..., value: _Optional[float] = ...) -> None: ...
    ID_FIELD_NUMBER: _ClassVar[int]
    COUNT_FIELD_NUMBER: _ClassVar[int]
    LATENCY_AVG_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    VALUE_PER_PARTITION_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.MetricRunId
    count: int
    latency_avg: int
    created_at: _timestamp_pb2.Timestamp
    value_per_partition: _containers.ScalarMap[int, float]
    def __init__(self, id: _Optional[_Union[_object_id_pb2.MetricRunId, _Mapping]] = ..., count: _Optional[int] = ..., latency_avg: _Optional[int] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., value_per_partition: _Optional[_Mapping[int, float]] = ...) -> None: ...
