import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class WorkflowMetricId(_message.Message):
    __slots__ = ("wf_spec",)
    WF_SPEC_FIELD_NUMBER: _ClassVar[int]
    wf_spec: _object_id_pb2.WfSpecId
    def __init__(self, wf_spec: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ...) -> None: ...

class TaskMetricId(_message.Message):
    __slots__ = ("task_def",)
    TASK_DEF_FIELD_NUMBER: _ClassVar[int]
    task_def: _object_id_pb2.TaskDefId
    def __init__(self, task_def: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ...) -> None: ...

class NodeMetricId(_message.Message):
    __slots__ = ("wf_spec", "node_name", "node_position")
    WF_SPEC_FIELD_NUMBER: _ClassVar[int]
    NODE_NAME_FIELD_NUMBER: _ClassVar[int]
    NODE_POSITION_FIELD_NUMBER: _ClassVar[int]
    wf_spec: _object_id_pb2.WfSpecId
    node_name: str
    node_position: int
    def __init__(self, wf_spec: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., node_name: _Optional[str] = ..., node_position: _Optional[int] = ...) -> None: ...

class MetricWindowId(_message.Message):
    __slots__ = ("workflow", "task", "node", "window_start")
    WORKFLOW_FIELD_NUMBER: _ClassVar[int]
    TASK_FIELD_NUMBER: _ClassVar[int]
    NODE_FIELD_NUMBER: _ClassVar[int]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    workflow: WorkflowMetricId
    task: TaskMetricId
    node: NodeMetricId
    window_start: _timestamp_pb2.Timestamp
    def __init__(self, workflow: _Optional[_Union[WorkflowMetricId, _Mapping]] = ..., task: _Optional[_Union[TaskMetricId, _Mapping]] = ..., node: _Optional[_Union[NodeMetricId, _Mapping]] = ..., window_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class CountAndTiming(_message.Message):
    __slots__ = ("count", "min_latency_ms", "max_latency_ms", "total_latency_ms")
    COUNT_FIELD_NUMBER: _ClassVar[int]
    MIN_LATENCY_MS_FIELD_NUMBER: _ClassVar[int]
    MAX_LATENCY_MS_FIELD_NUMBER: _ClassVar[int]
    TOTAL_LATENCY_MS_FIELD_NUMBER: _ClassVar[int]
    count: int
    min_latency_ms: int
    max_latency_ms: int
    total_latency_ms: int
    def __init__(self, count: _Optional[int] = ..., min_latency_ms: _Optional[int] = ..., max_latency_ms: _Optional[int] = ..., total_latency_ms: _Optional[int] = ...) -> None: ...

class MetricWindow(_message.Message):
    __slots__ = ("id", "metrics")
    class MetricsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: CountAndTiming
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[CountAndTiming, _Mapping]] = ...) -> None: ...
    ID_FIELD_NUMBER: _ClassVar[int]
    METRICS_FIELD_NUMBER: _ClassVar[int]
    id: MetricWindowId
    metrics: _containers.MessageMap[str, CountAndTiming]
    def __init__(self, id: _Optional[_Union[MetricWindowId, _Mapping]] = ..., metrics: _Optional[_Mapping[str, CountAndTiming]] = ...) -> None: ...

class ListWfMetricsRequest(_message.Message):
    __slots__ = ("wf_spec", "window_start", "window_end")
    WF_SPEC_FIELD_NUMBER: _ClassVar[int]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_END_FIELD_NUMBER: _ClassVar[int]
    wf_spec: _object_id_pb2.WfSpecId
    window_start: _timestamp_pb2.Timestamp
    window_end: _timestamp_pb2.Timestamp
    def __init__(self, wf_spec: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., window_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., window_end: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class MetricsList(_message.Message):
    __slots__ = ("windows",)
    WINDOWS_FIELD_NUMBER: _ClassVar[int]
    windows: _containers.RepeatedCompositeFieldContainer[MetricWindow]
    def __init__(self, windows: _Optional[_Iterable[_Union[MetricWindow, _Mapping]]] = ...) -> None: ...
