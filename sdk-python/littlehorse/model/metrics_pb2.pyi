import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf import duration_pb2 as _duration_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class MetricSpec(_message.Message):
    __slots__ = ("id", "created_at", "aggregators")
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    AGGREGATORS_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.MetricSpecId
    created_at: _timestamp_pb2.Timestamp
    aggregators: _containers.RepeatedCompositeFieldContainer[Aggregator]
    def __init__(self, id: _Optional[_Union[_object_id_pb2.MetricSpecId, _Mapping]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., aggregators: _Optional[_Iterable[_Union[Aggregator, _Mapping]]] = ...) -> None: ...

class Aggregator(_message.Message):
    __slots__ = ("window_length", "count", "ratio", "latency")
    class StatusRange(_message.Message):
        __slots__ = ("lh_status", "task_run", "user_task_run")
        LH_STATUS_FIELD_NUMBER: _ClassVar[int]
        TASK_RUN_FIELD_NUMBER: _ClassVar[int]
        USER_TASK_RUN_FIELD_NUMBER: _ClassVar[int]
        lh_status: LHStatusRange
        task_run: TaskRunStatusRange
        user_task_run: UserTaskRunStatusRange
        def __init__(self, lh_status: _Optional[_Union[LHStatusRange, _Mapping]] = ..., task_run: _Optional[_Union[TaskRunStatusRange, _Mapping]] = ..., user_task_run: _Optional[_Union[UserTaskRunStatusRange, _Mapping]] = ...) -> None: ...
    class Count(_message.Message):
        __slots__ = ("status_range",)
        STATUS_RANGE_FIELD_NUMBER: _ClassVar[int]
        status_range: Aggregator.StatusRange
        def __init__(self, status_range: _Optional[_Union[Aggregator.StatusRange, _Mapping]] = ...) -> None: ...
    class Ratio(_message.Message):
        __slots__ = ("status_range",)
        STATUS_RANGE_FIELD_NUMBER: _ClassVar[int]
        status_range: Aggregator.StatusRange
        def __init__(self, status_range: _Optional[_Union[Aggregator.StatusRange, _Mapping]] = ...) -> None: ...
    class Latency(_message.Message):
        __slots__ = ("status_range",)
        STATUS_RANGE_FIELD_NUMBER: _ClassVar[int]
        status_range: Aggregator.StatusRange
        def __init__(self, status_range: _Optional[_Union[Aggregator.StatusRange, _Mapping]] = ...) -> None: ...
    WINDOW_LENGTH_FIELD_NUMBER: _ClassVar[int]
    COUNT_FIELD_NUMBER: _ClassVar[int]
    RATIO_FIELD_NUMBER: _ClassVar[int]
    LATENCY_FIELD_NUMBER: _ClassVar[int]
    window_length: _duration_pb2.Duration
    count: Aggregator.Count
    ratio: Aggregator.Ratio
    latency: Aggregator.Latency
    def __init__(self, window_length: _Optional[_Union[datetime.timedelta, _duration_pb2.Duration, _Mapping]] = ..., count: _Optional[_Union[Aggregator.Count, _Mapping]] = ..., ratio: _Optional[_Union[Aggregator.Ratio, _Mapping]] = ..., latency: _Optional[_Union[Aggregator.Latency, _Mapping]] = ...) -> None: ...

class LHStatusRange(_message.Message):
    __slots__ = ("starts", "ends")
    STARTS_FIELD_NUMBER: _ClassVar[int]
    ENDS_FIELD_NUMBER: _ClassVar[int]
    starts: _common_enums_pb2.LHStatus
    ends: _common_enums_pb2.LHStatus
    def __init__(self, starts: _Optional[_Union[_common_enums_pb2.LHStatus, str]] = ..., ends: _Optional[_Union[_common_enums_pb2.LHStatus, str]] = ...) -> None: ...

class TaskRunStatusRange(_message.Message):
    __slots__ = ("starts", "ends")
    STARTS_FIELD_NUMBER: _ClassVar[int]
    ENDS_FIELD_NUMBER: _ClassVar[int]
    starts: _common_enums_pb2.TaskStatus
    ends: _common_enums_pb2.TaskStatus
    def __init__(self, starts: _Optional[_Union[_common_enums_pb2.TaskStatus, str]] = ..., ends: _Optional[_Union[_common_enums_pb2.TaskStatus, str]] = ...) -> None: ...

class UserTaskRunStatusRange(_message.Message):
    __slots__ = ("starts", "ends")
    STARTS_FIELD_NUMBER: _ClassVar[int]
    ENDS_FIELD_NUMBER: _ClassVar[int]
    starts: _common_enums_pb2.UserTaskRunStatus
    ends: _common_enums_pb2.UserTaskRunStatus
    def __init__(self, starts: _Optional[_Union[_common_enums_pb2.UserTaskRunStatus, str]] = ..., ends: _Optional[_Union[_common_enums_pb2.UserTaskRunStatus, str]] = ...) -> None: ...

class PartitionMetric(_message.Message):
    __slots__ = ("id", "created_at", "active_windows", "window_length")
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    ACTIVE_WINDOWS_FIELD_NUMBER: _ClassVar[int]
    WINDOW_LENGTH_FIELD_NUMBER: _ClassVar[int]
    id: PartitionMetricId
    created_at: _timestamp_pb2.Timestamp
    active_windows: _containers.RepeatedCompositeFieldContainer[PartitionWindowedMetric]
    window_length: _duration_pb2.Duration
    def __init__(self, id: _Optional[_Union[PartitionMetricId, _Mapping]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., active_windows: _Optional[_Iterable[_Union[PartitionWindowedMetric, _Mapping]]] = ..., window_length: _Optional[_Union[datetime.timedelta, _duration_pb2.Duration, _Mapping]] = ...) -> None: ...

class PartitionWindowedMetric(_message.Message):
    __slots__ = ("value", "window_start", "number_of_samples")
    VALUE_FIELD_NUMBER: _ClassVar[int]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    NUMBER_OF_SAMPLES_FIELD_NUMBER: _ClassVar[int]
    value: float
    window_start: _timestamp_pb2.Timestamp
    number_of_samples: int
    def __init__(self, value: _Optional[float] = ..., window_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., number_of_samples: _Optional[int] = ...) -> None: ...

class PartitionMetricId(_message.Message):
    __slots__ = ("id", "tenant_id", "aggregation_type")
    ID_FIELD_NUMBER: _ClassVar[int]
    TENANT_ID_FIELD_NUMBER: _ClassVar[int]
    AGGREGATION_TYPE_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.MetricSpecId
    tenant_id: _object_id_pb2.TenantId
    aggregation_type: _common_enums_pb2.AggregationType
    def __init__(self, id: _Optional[_Union[_object_id_pb2.MetricSpecId, _Mapping]] = ..., tenant_id: _Optional[_Union[_object_id_pb2.TenantId, _Mapping]] = ..., aggregation_type: _Optional[_Union[_common_enums_pb2.AggregationType, str]] = ...) -> None: ...

class Metric(_message.Message):
    __slots__ = ("id", "count", "latency_avg", "created_at", "value_per_partition")
    class ValuePerPartitionEntry(_message.Message):
        __slots__ = ("key", "value")
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
    def __init__(self, id: _Optional[_Union[_object_id_pb2.MetricId, _Mapping]] = ..., count: _Optional[int] = ..., latency_avg: _Optional[int] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., value_per_partition: _Optional[_Mapping[int, float]] = ...) -> None: ...
