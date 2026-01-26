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

class StatusTransition(_message.Message):
    __slots__ = ("entity", "from_status", "to_status")
    ENTITY_FIELD_NUMBER: _ClassVar[int]
    FROM_STATUS_FIELD_NUMBER: _ClassVar[int]
    TO_STATUS_FIELD_NUMBER: _ClassVar[int]
    entity: _common_enums_pb2.MetricEntityType
    from_status: str
    to_status: str
    def __init__(self, entity: _Optional[_Union[_common_enums_pb2.MetricEntityType, str]] = ..., from_status: _Optional[str] = ..., to_status: _Optional[str] = ...) -> None: ...

class MetricScope(_message.Message):
    __slots__ = ("wf_spec", "task_def", "node")
    WF_SPEC_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_FIELD_NUMBER: _ClassVar[int]
    NODE_FIELD_NUMBER: _ClassVar[int]
    GLOBAL_FIELD_NUMBER: _ClassVar[int]
    wf_spec: _object_id_pb2.WfSpecId
    task_def: _object_id_pb2.TaskDefId
    node: NodeReference
    def __init__(self, wf_spec: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., task_def: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ..., node: _Optional[_Union[NodeReference, _Mapping]] = ..., **kwargs) -> None: ...

class NodeReference(_message.Message):
    __slots__ = ("wf_spec", "thread_name", "node_name", "task_def")
    WF_SPEC_FIELD_NUMBER: _ClassVar[int]
    THREAD_NAME_FIELD_NUMBER: _ClassVar[int]
    NODE_NAME_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_FIELD_NUMBER: _ClassVar[int]
    wf_spec: _object_id_pb2.WfSpecId
    thread_name: str
    node_name: str
    task_def: _object_id_pb2.TaskDefId
    def __init__(self, wf_spec: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., thread_name: _Optional[str] = ..., node_name: _Optional[str] = ..., task_def: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ...) -> None: ...

class MetricSpec(_message.Message):
    __slots__ = ("id", "created_at", "aggregation_type", "scope", "transition", "window_length")
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    AGGREGATION_TYPE_FIELD_NUMBER: _ClassVar[int]
    SCOPE_FIELD_NUMBER: _ClassVar[int]
    TRANSITION_FIELD_NUMBER: _ClassVar[int]
    WINDOW_LENGTH_FIELD_NUMBER: _ClassVar[int]
    id: str
    created_at: _timestamp_pb2.Timestamp
    aggregation_type: _common_enums_pb2.AggregationType
    scope: MetricScope
    transition: StatusTransition
    window_length: _duration_pb2.Duration
    def __init__(self, id: _Optional[str] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., aggregation_type: _Optional[_Union[_common_enums_pb2.AggregationType, str]] = ..., scope: _Optional[_Union[MetricScope, _Mapping]] = ..., transition: _Optional[_Union[StatusTransition, _Mapping]] = ..., window_length: _Optional[_Union[datetime.timedelta, _duration_pb2.Duration, _Mapping]] = ...) -> None: ...

class MetricsConfig(_message.Message):
    __slots__ = ("level", "retention_days")
    LEVEL_FIELD_NUMBER: _ClassVar[int]
    RETENTION_DAYS_FIELD_NUMBER: _ClassVar[int]
    level: _common_enums_pb2.MetricRecordingLevel
    retention_days: int
    def __init__(self, level: _Optional[_Union[_common_enums_pb2.MetricRecordingLevel, str]] = ..., retention_days: _Optional[int] = ...) -> None: ...

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

class ListMetricsRequest(_message.Message):
    __slots__ = ("id", "end_time")
    ID_FIELD_NUMBER: _ClassVar[int]
    END_TIME_FIELD_NUMBER: _ClassVar[int]
    id: MetricWindowId
    end_time: _timestamp_pb2.Timestamp
    def __init__(self, id: _Optional[_Union[MetricWindowId, _Mapping]] = ..., end_time: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class MetricList(_message.Message):
    __slots__ = ("windows",)
    WINDOWS_FIELD_NUMBER: _ClassVar[int]
    windows: _containers.RepeatedCompositeFieldContainer[MetricWindow]
    def __init__(self, windows: _Optional[_Iterable[_Union[MetricWindow, _Mapping]]] = ...) -> None: ...

class MetricLevelOverride(_message.Message):
    __slots__ = ("id", "new_level", "workflow")
    ID_FIELD_NUMBER: _ClassVar[int]
    NEW_LEVEL_FIELD_NUMBER: _ClassVar[int]
    WORKFLOW_FIELD_NUMBER: _ClassVar[int]
    id: str
    new_level: _common_enums_pb2.MetricRecordingLevel
    workflow: WorkflowMetricId
    def __init__(self, id: _Optional[str] = ..., new_level: _Optional[_Union[_common_enums_pb2.MetricRecordingLevel, str]] = ..., workflow: _Optional[_Union[WorkflowMetricId, _Mapping]] = ...) -> None: ...

class PutMetricLevelOverrideRequest(_message.Message):
    __slots__ = ("override",)
    OVERRIDE_FIELD_NUMBER: _ClassVar[int]
    override: MetricLevelOverride
    def __init__(self, override: _Optional[_Union[MetricLevelOverride, _Mapping]] = ...) -> None: ...

class DeleteMetricLevelOverrideRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...

class ListMetricLevelOverridesRequest(_message.Message):
    __slots__ = ("wf_spec_filter",)
    WF_SPEC_FILTER_FIELD_NUMBER: _ClassVar[int]
    wf_spec_filter: _object_id_pb2.WfSpecId
    def __init__(self, wf_spec_filter: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ...) -> None: ...

class MetricLevelOverridesList(_message.Message):
    __slots__ = ("overrides",)
    OVERRIDES_FIELD_NUMBER: _ClassVar[int]
    overrides: _containers.RepeatedCompositeFieldContainer[MetricLevelOverride]
    def __init__(self, overrides: _Optional[_Iterable[_Union[MetricLevelOverride, _Mapping]]] = ...) -> None: ...
