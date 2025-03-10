from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf import duration_pb2 as _duration_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class MetricSpec(_message.Message):
    __slots__ = ["id", "created_at", "window_lengths", "aggregate_as", "lh_status_ranges", "task_status_ranges", "user_task_status_ranges"]
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    WINDOW_LENGTHS_FIELD_NUMBER: _ClassVar[int]
    AGGREGATE_AS_FIELD_NUMBER: _ClassVar[int]
    LH_STATUS_RANGES_FIELD_NUMBER: _ClassVar[int]
    TASK_STATUS_RANGES_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_STATUS_RANGES_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.MetricSpecId
    created_at: _timestamp_pb2.Timestamp
    window_lengths: _containers.RepeatedCompositeFieldContainer[_duration_pb2.Duration]
    aggregate_as: _containers.RepeatedScalarFieldContainer[_common_enums_pb2.AggregationType]
    lh_status_ranges: _containers.RepeatedCompositeFieldContainer[LHStatusRange]
    task_status_ranges: _containers.RepeatedCompositeFieldContainer[LHStatusRange]
    user_task_status_ranges: _containers.RepeatedCompositeFieldContainer[UserTaskRunStatusRange]
    def __init__(self, id: _Optional[_Union[_object_id_pb2.MetricSpecId, _Mapping]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., window_lengths: _Optional[_Iterable[_Union[_duration_pb2.Duration, _Mapping]]] = ..., aggregate_as: _Optional[_Iterable[_Union[_common_enums_pb2.AggregationType, str]]] = ..., lh_status_ranges: _Optional[_Iterable[_Union[LHStatusRange, _Mapping]]] = ..., task_status_ranges: _Optional[_Iterable[_Union[LHStatusRange, _Mapping]]] = ..., user_task_status_ranges: _Optional[_Iterable[_Union[UserTaskRunStatusRange, _Mapping]]] = ...) -> None: ...

class LHStatusRange(_message.Message):
    __slots__ = ["starts", "ends"]
    STARTS_FIELD_NUMBER: _ClassVar[int]
    ENDS_FIELD_NUMBER: _ClassVar[int]
    starts: _common_enums_pb2.LHStatus
    ends: _common_enums_pb2.LHStatus
    def __init__(self, starts: _Optional[_Union[_common_enums_pb2.LHStatus, str]] = ..., ends: _Optional[_Union[_common_enums_pb2.LHStatus, str]] = ...) -> None: ...

class TaskRunStatusRange(_message.Message):
    __slots__ = ["starts", "ends"]
    STARTS_FIELD_NUMBER: _ClassVar[int]
    ENDS_FIELD_NUMBER: _ClassVar[int]
    starts: _common_enums_pb2.TaskStatus
    ends: _common_enums_pb2.TaskStatus
    def __init__(self, starts: _Optional[_Union[_common_enums_pb2.TaskStatus, str]] = ..., ends: _Optional[_Union[_common_enums_pb2.TaskStatus, str]] = ...) -> None: ...

class UserTaskRunStatusRange(_message.Message):
    __slots__ = ["starts", "ends"]
    STARTS_FIELD_NUMBER: _ClassVar[int]
    ENDS_FIELD_NUMBER: _ClassVar[int]
    starts: _common_enums_pb2.UserTaskRunStatus
    ends: _common_enums_pb2.UserTaskRunStatus
    def __init__(self, starts: _Optional[_Union[_common_enums_pb2.UserTaskRunStatus, str]] = ..., ends: _Optional[_Union[_common_enums_pb2.UserTaskRunStatus, str]] = ...) -> None: ...

class PartitionMetric(_message.Message):
    __slots__ = ["id", "created_at", "active_windows", "window_length", "aggregation_type"]
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    ACTIVE_WINDOWS_FIELD_NUMBER: _ClassVar[int]
    WINDOW_LENGTH_FIELD_NUMBER: _ClassVar[int]
    AGGREGATION_TYPE_FIELD_NUMBER: _ClassVar[int]
    id: PartitionMetricId
    created_at: _timestamp_pb2.Timestamp
    active_windows: _containers.RepeatedCompositeFieldContainer[PartitionWindowedMetric]
    window_length: _duration_pb2.Duration
    aggregation_type: _common_enums_pb2.AggregationType
    def __init__(self, id: _Optional[_Union[PartitionMetricId, _Mapping]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., active_windows: _Optional[_Iterable[_Union[PartitionWindowedMetric, _Mapping]]] = ..., window_length: _Optional[_Union[_duration_pb2.Duration, _Mapping]] = ..., aggregation_type: _Optional[_Union[_common_enums_pb2.AggregationType, str]] = ...) -> None: ...

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
    __slots__ = ["id", "tenant_id", "aggregation_type"]
    ID_FIELD_NUMBER: _ClassVar[int]
    TENANT_ID_FIELD_NUMBER: _ClassVar[int]
    AGGREGATION_TYPE_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.MetricSpecId
    tenant_id: _object_id_pb2.TenantId
    aggregation_type: _common_enums_pb2.AggregationType
    def __init__(self, id: _Optional[_Union[_object_id_pb2.MetricSpecId, _Mapping]] = ..., tenant_id: _Optional[_Union[_object_id_pb2.TenantId, _Mapping]] = ..., aggregation_type: _Optional[_Union[_common_enums_pb2.AggregationType, str]] = ...) -> None: ...

class Metric(_message.Message):
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
    id: _object_id_pb2.MetricId
    count: int
    latency_avg: int
    created_at: _timestamp_pb2.Timestamp
    value_per_partition: _containers.ScalarMap[int, float]
    def __init__(self, id: _Optional[_Union[_object_id_pb2.MetricId, _Mapping]] = ..., count: _Optional[int] = ..., latency_avg: _Optional[int] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., value_per_partition: _Optional[_Mapping[int, float]] = ...) -> None: ...
