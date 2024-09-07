from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class WfSpecId(_message.Message):
    __slots__ = ["name", "major_version", "revision"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    MAJOR_VERSION_FIELD_NUMBER: _ClassVar[int]
    REVISION_FIELD_NUMBER: _ClassVar[int]
    name: str
    major_version: int
    revision: int
    def __init__(self, name: _Optional[str] = ..., major_version: _Optional[int] = ..., revision: _Optional[int] = ...) -> None: ...

class TaskDefId(_message.Message):
    __slots__ = ["name"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    def __init__(self, name: _Optional[str] = ...) -> None: ...

class ExternalEventDefId(_message.Message):
    __slots__ = ["name"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    def __init__(self, name: _Optional[str] = ...) -> None: ...

class UserTaskDefId(_message.Message):
    __slots__ = ["name", "version"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    VERSION_FIELD_NUMBER: _ClassVar[int]
    name: str
    version: int
    def __init__(self, name: _Optional[str] = ..., version: _Optional[int] = ...) -> None: ...

class WorkflowEventDefId(_message.Message):
    __slots__ = ["name"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    def __init__(self, name: _Optional[str] = ...) -> None: ...

class TaskWorkerGroupId(_message.Message):
    __slots__ = ["task_def_id"]
    TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    task_def_id: TaskDefId
    def __init__(self, task_def_id: _Optional[_Union[TaskDefId, _Mapping]] = ...) -> None: ...

class VariableId(_message.Message):
    __slots__ = ["wf_run_id", "thread_run_number", "name"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: WfRunId
    thread_run_number: int
    name: str
    def __init__(self, wf_run_id: _Optional[_Union[WfRunId, _Mapping]] = ..., thread_run_number: _Optional[int] = ..., name: _Optional[str] = ...) -> None: ...

class ExternalEventId(_message.Message):
    __slots__ = ["wf_run_id", "external_event_def_id", "guid"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    GUID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: WfRunId
    external_event_def_id: ExternalEventDefId
    guid: str
    def __init__(self, wf_run_id: _Optional[_Union[WfRunId, _Mapping]] = ..., external_event_def_id: _Optional[_Union[ExternalEventDefId, _Mapping]] = ..., guid: _Optional[str] = ...) -> None: ...

class WfRunId(_message.Message):
    __slots__ = ["id", "parent_wf_run_id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    PARENT_WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    parent_wf_run_id: WfRunId
    def __init__(self, id: _Optional[str] = ..., parent_wf_run_id: _Optional[_Union[WfRunId, _Mapping]] = ...) -> None: ...

class NodeRunId(_message.Message):
    __slots__ = ["wf_run_id", "thread_run_number", "position"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    POSITION_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: WfRunId
    thread_run_number: int
    position: int
    def __init__(self, wf_run_id: _Optional[_Union[WfRunId, _Mapping]] = ..., thread_run_number: _Optional[int] = ..., position: _Optional[int] = ...) -> None: ...

class WorkflowEventId(_message.Message):
    __slots__ = ["wf_run_id", "workflow_event_def_id", "number"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    WORKFLOW_EVENT_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    NUMBER_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: WfRunId
    workflow_event_def_id: WorkflowEventDefId
    number: int
    def __init__(self, wf_run_id: _Optional[_Union[WfRunId, _Mapping]] = ..., workflow_event_def_id: _Optional[_Union[WorkflowEventDefId, _Mapping]] = ..., number: _Optional[int] = ...) -> None: ...

class TaskRunId(_message.Message):
    __slots__ = ["wf_run_id", "task_guid"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_GUID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: WfRunId
    task_guid: str
    def __init__(self, wf_run_id: _Optional[_Union[WfRunId, _Mapping]] = ..., task_guid: _Optional[str] = ...) -> None: ...

class UserTaskRunId(_message.Message):
    __slots__ = ["wf_run_id", "user_task_guid"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_GUID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: WfRunId
    user_task_guid: str
    def __init__(self, wf_run_id: _Optional[_Union[WfRunId, _Mapping]] = ..., user_task_guid: _Optional[str] = ...) -> None: ...

class TaskDefMetricsId(_message.Message):
    __slots__ = ["window_start", "window_type", "task_def_id"]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_TYPE_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_ID_FIELD_NUMBER: _ClassVar[int]
    window_start: _timestamp_pb2.Timestamp
    window_type: _common_enums_pb2.MetricsWindowLength
    task_def_id: TaskDefId
    def __init__(self, window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., window_type: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ..., task_def_id: _Optional[_Union[TaskDefId, _Mapping]] = ...) -> None: ...

class WfSpecMetricsId(_message.Message):
    __slots__ = ["window_start", "window_type", "wf_spec_id"]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_TYPE_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    window_start: _timestamp_pb2.Timestamp
    window_type: _common_enums_pb2.MetricsWindowLength
    wf_spec_id: WfSpecId
    def __init__(self, window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., window_type: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ..., wf_spec_id: _Optional[_Union[WfSpecId, _Mapping]] = ...) -> None: ...

class PrincipalId(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...

class TenantId(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...

class ScheduledWfRunId(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...
