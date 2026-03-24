import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class MetricWindowId(_message.Message):
    __slots__ = ("wf_spec_id", "task_def_id", "user_task_def_id", "tenant_id", "window_start")
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    TENANT_ID_FIELD_NUMBER: _ClassVar[int]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    wf_spec_id: _object_id_pb2.WfSpecId
    task_def_id: _object_id_pb2.TaskDefId
    user_task_def_id: _object_id_pb2.UserTaskDefId
    tenant_id: _object_id_pb2.TenantId
    window_start: _timestamp_pb2.Timestamp
    def __init__(self, wf_spec_id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., task_def_id: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ..., user_task_def_id: _Optional[_Union[_object_id_pb2.UserTaskDefId, _Mapping]] = ..., tenant_id: _Optional[_Union[_object_id_pb2.TenantId, _Mapping]] = ..., window_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

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
    id: MetricWindowId
    workflow: WfMetrics
    task: TaskMetrics
    def __init__(self, id: _Optional[_Union[MetricWindowId, _Mapping]] = ..., workflow: _Optional[_Union[WfMetrics, _Mapping]] = ..., task: _Optional[_Union[TaskMetrics, _Mapping]] = ...) -> None: ...

class WfMetrics(_message.Message):
    __slots__ = ("started", "running_to_completed", "running_to_error", "running_to_exception", "running_to_halting", "halting_to_halted", "halted_to_running", "running_to_halted")
    STARTED_FIELD_NUMBER: _ClassVar[int]
    RUNNING_TO_COMPLETED_FIELD_NUMBER: _ClassVar[int]
    RUNNING_TO_ERROR_FIELD_NUMBER: _ClassVar[int]
    RUNNING_TO_EXCEPTION_FIELD_NUMBER: _ClassVar[int]
    RUNNING_TO_HALTING_FIELD_NUMBER: _ClassVar[int]
    HALTING_TO_HALTED_FIELD_NUMBER: _ClassVar[int]
    HALTED_TO_RUNNING_FIELD_NUMBER: _ClassVar[int]
    RUNNING_TO_HALTED_FIELD_NUMBER: _ClassVar[int]
    started: CountAndTiming
    running_to_completed: CountAndTiming
    running_to_error: CountAndTiming
    running_to_exception: CountAndTiming
    running_to_halting: CountAndTiming
    halting_to_halted: CountAndTiming
    halted_to_running: CountAndTiming
    running_to_halted: CountAndTiming
    def __init__(self, started: _Optional[_Union[CountAndTiming, _Mapping]] = ..., running_to_completed: _Optional[_Union[CountAndTiming, _Mapping]] = ..., running_to_error: _Optional[_Union[CountAndTiming, _Mapping]] = ..., running_to_exception: _Optional[_Union[CountAndTiming, _Mapping]] = ..., running_to_halting: _Optional[_Union[CountAndTiming, _Mapping]] = ..., halting_to_halted: _Optional[_Union[CountAndTiming, _Mapping]] = ..., halted_to_running: _Optional[_Union[CountAndTiming, _Mapping]] = ..., running_to_halted: _Optional[_Union[CountAndTiming, _Mapping]] = ...) -> None: ...

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
