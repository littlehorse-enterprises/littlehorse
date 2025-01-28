from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Mapping as _Mapping, Optional as _Optional, Union as _Union

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
    __slots__ = ["id", "created_at"]
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    id: MetricId
    created_at: _timestamp_pb2.Timestamp
    def __init__(self, id: _Optional[_Union[MetricId, _Mapping]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class MetricId(_message.Message):
    __slots__ = ["measurable", "type"]
    MEASURABLE_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    measurable: MeasurableObject
    type: MetricType
    def __init__(self, measurable: _Optional[_Union[MeasurableObject, str]] = ..., type: _Optional[_Union[MetricType, str]] = ...) -> None: ...

class PartitionMetric(_message.Message):
    __slots__ = ["id", "value", "created_at", "window_start"]
    ID_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    id: PartitionMetricId
    value: float
    created_at: _timestamp_pb2.Timestamp
    window_start: _timestamp_pb2.Timestamp
    def __init__(self, id: _Optional[_Union[PartitionMetricId, _Mapping]] = ..., value: _Optional[float] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class PartitionMetricId(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: MetricId
    def __init__(self, id: _Optional[_Union[MetricId, _Mapping]] = ...) -> None: ...
