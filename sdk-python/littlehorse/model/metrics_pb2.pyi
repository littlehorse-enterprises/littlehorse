import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

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
    __slots__ = ("id", "workflow", "task")
    ID_FIELD_NUMBER: _ClassVar[int]
    WORKFLOW_FIELD_NUMBER: _ClassVar[int]
    TASK_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.MetricWindowId
    workflow: WfMetrics
    task: TaskMetrics
    def __init__(self, id: _Optional[_Union[_object_id_pb2.MetricWindowId, _Mapping]] = ..., workflow: _Optional[_Union[WfMetrics, _Mapping]] = ..., task: _Optional[_Union[TaskMetrics, _Mapping]] = ...) -> None: ...

class WfMetrics(_message.Message):
    __slots__ = ("started", "running_to_completed", "running_to_error", "running_to_exception", "running_to_halting", "running_to_halted", "halting_to_halted", "halted_to_running")
    STARTED_FIELD_NUMBER: _ClassVar[int]
    RUNNING_TO_COMPLETED_FIELD_NUMBER: _ClassVar[int]
    RUNNING_TO_ERROR_FIELD_NUMBER: _ClassVar[int]
    RUNNING_TO_EXCEPTION_FIELD_NUMBER: _ClassVar[int]
    RUNNING_TO_HALTING_FIELD_NUMBER: _ClassVar[int]
    RUNNING_TO_HALTED_FIELD_NUMBER: _ClassVar[int]
    HALTING_TO_HALTED_FIELD_NUMBER: _ClassVar[int]
    HALTED_TO_RUNNING_FIELD_NUMBER: _ClassVar[int]
    started: CountAndTiming
    running_to_completed: CountAndTiming
    running_to_error: CountAndTiming
    running_to_exception: CountAndTiming
    running_to_halting: CountAndTiming
    running_to_halted: CountAndTiming
    halting_to_halted: CountAndTiming
    halted_to_running: CountAndTiming
    def __init__(self, started: _Optional[_Union[CountAndTiming, _Mapping]] = ..., running_to_completed: _Optional[_Union[CountAndTiming, _Mapping]] = ..., running_to_error: _Optional[_Union[CountAndTiming, _Mapping]] = ..., running_to_exception: _Optional[_Union[CountAndTiming, _Mapping]] = ..., running_to_halting: _Optional[_Union[CountAndTiming, _Mapping]] = ..., running_to_halted: _Optional[_Union[CountAndTiming, _Mapping]] = ..., halting_to_halted: _Optional[_Union[CountAndTiming, _Mapping]] = ..., halted_to_running: _Optional[_Union[CountAndTiming, _Mapping]] = ...) -> None: ...

class TaskMetrics(_message.Message):
    __slots__ = ("taskrun_created_to_completed", "taskrun_created_to_error", "taskrun_created_to_exception", "taskattempt_pending_to_scheduled", "taskattempt_scheduled_to_running", "taskattempt_running_to_error", "taskattempt_running_to_success", "taskattempt_running_to_exception")
    TASKRUN_CREATED_TO_COMPLETED_FIELD_NUMBER: _ClassVar[int]
    TASKRUN_CREATED_TO_ERROR_FIELD_NUMBER: _ClassVar[int]
    TASKRUN_CREATED_TO_EXCEPTION_FIELD_NUMBER: _ClassVar[int]
    TASKATTEMPT_PENDING_TO_SCHEDULED_FIELD_NUMBER: _ClassVar[int]
    TASKATTEMPT_SCHEDULED_TO_RUNNING_FIELD_NUMBER: _ClassVar[int]
    TASKATTEMPT_RUNNING_TO_ERROR_FIELD_NUMBER: _ClassVar[int]
    TASKATTEMPT_RUNNING_TO_SUCCESS_FIELD_NUMBER: _ClassVar[int]
    TASKATTEMPT_RUNNING_TO_EXCEPTION_FIELD_NUMBER: _ClassVar[int]
    taskrun_created_to_completed: CountAndTiming
    taskrun_created_to_error: CountAndTiming
    taskrun_created_to_exception: CountAndTiming
    taskattempt_pending_to_scheduled: CountAndTiming
    taskattempt_scheduled_to_running: CountAndTiming
    taskattempt_running_to_error: CountAndTiming
    taskattempt_running_to_success: CountAndTiming
    taskattempt_running_to_exception: CountAndTiming
    def __init__(self, taskrun_created_to_completed: _Optional[_Union[CountAndTiming, _Mapping]] = ..., taskrun_created_to_error: _Optional[_Union[CountAndTiming, _Mapping]] = ..., taskrun_created_to_exception: _Optional[_Union[CountAndTiming, _Mapping]] = ..., taskattempt_pending_to_scheduled: _Optional[_Union[CountAndTiming, _Mapping]] = ..., taskattempt_scheduled_to_running: _Optional[_Union[CountAndTiming, _Mapping]] = ..., taskattempt_running_to_error: _Optional[_Union[CountAndTiming, _Mapping]] = ..., taskattempt_running_to_success: _Optional[_Union[CountAndTiming, _Mapping]] = ..., taskattempt_running_to_exception: _Optional[_Union[CountAndTiming, _Mapping]] = ...) -> None: ...

class ListWfMetricsRequest(_message.Message):
    __slots__ = ("wf_spec", "window_start", "window_end")
    WF_SPEC_FIELD_NUMBER: _ClassVar[int]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_END_FIELD_NUMBER: _ClassVar[int]
    wf_spec: _object_id_pb2.WfSpecId
    window_start: _timestamp_pb2.Timestamp
    window_end: _timestamp_pb2.Timestamp
    def __init__(self, wf_spec: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., window_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., window_end: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class ListTaskMetricsRequest(_message.Message):
    __slots__ = ("task_def", "window_start", "window_end")
    TASK_DEF_FIELD_NUMBER: _ClassVar[int]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_END_FIELD_NUMBER: _ClassVar[int]
    task_def: _object_id_pb2.TaskDefId
    window_start: _timestamp_pb2.Timestamp
    window_end: _timestamp_pb2.Timestamp
    def __init__(self, task_def: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ..., window_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., window_end: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class MetricsList(_message.Message):
    __slots__ = ("windows",)
    WINDOWS_FIELD_NUMBER: _ClassVar[int]
    windows: _containers.RepeatedCompositeFieldContainer[MetricWindow]
    def __init__(self, windows: _Optional[_Iterable[_Union[MetricWindow, _Mapping]]] = ...) -> None: ...

class GetLatestWfMetricWindowRequest(_message.Message):
    __slots__ = ("wf_spec_name", "major_version", "revision")
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    MAJOR_VERSION_FIELD_NUMBER: _ClassVar[int]
    REVISION_FIELD_NUMBER: _ClassVar[int]
    wf_spec_name: str
    major_version: int
    revision: int
    def __init__(self, wf_spec_name: _Optional[str] = ..., major_version: _Optional[int] = ..., revision: _Optional[int] = ...) -> None: ...

class GetLatestWfMetricWindowResponse(_message.Message):
    __slots__ = ("window",)
    WINDOW_FIELD_NUMBER: _ClassVar[int]
    window: MetricWindow
    def __init__(self, window: _Optional[_Union[MetricWindow, _Mapping]] = ...) -> None: ...

class GetLatestTaskMetricWindowRequest(_message.Message):
    __slots__ = ("task_def",)
    TASK_DEF_FIELD_NUMBER: _ClassVar[int]
    task_def: _object_id_pb2.TaskDefId
    def __init__(self, task_def: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ...) -> None: ...

class GetLatestTaskMetricWindowResponse(_message.Message):
    __slots__ = ("window",)
    WINDOW_FIELD_NUMBER: _ClassVar[int]
    window: MetricWindow
    def __init__(self, window: _Optional[_Union[MetricWindow, _Mapping]] = ...) -> None: ...

class MetricWindowIdList(_message.Message):
    __slots__ = ("bookmark", "results")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    RESULTS_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    results: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.MetricWindowId]
    def __init__(self, bookmark: _Optional[bytes] = ..., results: _Optional[_Iterable[_Union[_object_id_pb2.MetricWindowId, _Mapping]]] = ...) -> None: ...

class SearchWfMetricWindowRequest(_message.Message):
    __slots__ = ("bookmark", "limit", "wf_spec_name", "earliest_start", "latest_start", "latest_only")
    BOOKMARK_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
    LATEST_START_FIELD_NUMBER: _ClassVar[int]
    LATEST_ONLY_FIELD_NUMBER: _ClassVar[int]
    bookmark: bytes
    limit: int
    wf_spec_name: str
    earliest_start: _timestamp_pb2.Timestamp
    latest_start: _timestamp_pb2.Timestamp
    latest_only: bool
    def __init__(self, bookmark: _Optional[bytes] = ..., limit: _Optional[int] = ..., wf_spec_name: _Optional[str] = ..., earliest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., latest_only: _Optional[bool] = ...) -> None: ...
