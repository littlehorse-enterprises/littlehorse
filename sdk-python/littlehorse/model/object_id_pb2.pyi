from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class WfSpecId(_message.Message):
    __slots__ = ["name", "version"]
    NAME_FIELD_NUMBER: _ClassVar[int]
    VERSION_FIELD_NUMBER: _ClassVar[int]
    name: str
    version: int
    def __init__(self, name: _Optional[str] = ..., version: _Optional[int] = ...) -> None: ...

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

class GetLatestWfSpecRequest(_message.Message):
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

class TaskWorkerGroupId(_message.Message):
    __slots__ = ["task_def_name"]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    task_def_name: str
    def __init__(self, task_def_name: _Optional[str] = ...) -> None: ...

class VariableId(_message.Message):
    __slots__ = ["wf_run_id", "thread_run_number", "name"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    thread_run_number: int
    name: str
    def __init__(self, wf_run_id: _Optional[str] = ..., thread_run_number: _Optional[int] = ..., name: _Optional[str] = ...) -> None: ...

class ExternalEventId(_message.Message):
    __slots__ = ["wf_run_id", "external_event_def_name", "guid"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    GUID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    external_event_def_name: str
    guid: str
    def __init__(self, wf_run_id: _Optional[str] = ..., external_event_def_name: _Optional[str] = ..., guid: _Optional[str] = ...) -> None: ...

class WfRunId(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...

class NodeRunId(_message.Message):
    __slots__ = ["wf_run_id", "thread_run_number", "position"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    POSITION_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    thread_run_number: int
    position: int
    def __init__(self, wf_run_id: _Optional[str] = ..., thread_run_number: _Optional[int] = ..., position: _Optional[int] = ...) -> None: ...

class TaskRunId(_message.Message):
    __slots__ = ["wf_run_id", "task_guid"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_GUID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    task_guid: str
    def __init__(self, wf_run_id: _Optional[str] = ..., task_guid: _Optional[str] = ...) -> None: ...

class UserTaskRunId(_message.Message):
    __slots__ = ["wf_run_id", "user_task_guid"]
    WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_GUID_FIELD_NUMBER: _ClassVar[int]
    wf_run_id: str
    user_task_guid: str
    def __init__(self, wf_run_id: _Optional[str] = ..., user_task_guid: _Optional[str] = ...) -> None: ...

class TaskDefMetricsId(_message.Message):
    __slots__ = ["window_start", "window_type", "task_def_name"]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_TYPE_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_NAME_FIELD_NUMBER: _ClassVar[int]
    window_start: _timestamp_pb2.Timestamp
    window_type: _common_enums_pb2.MetricsWindowLength
    task_def_name: str
    def __init__(self, window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., window_type: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ..., task_def_name: _Optional[str] = ...) -> None: ...

class WfSpecMetricsId(_message.Message):
    __slots__ = ["window_start", "window_type", "wf_spec_name", "wf_spec_version"]
    WINDOW_START_FIELD_NUMBER: _ClassVar[int]
    WINDOW_TYPE_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_VERSION_FIELD_NUMBER: _ClassVar[int]
    window_start: _timestamp_pb2.Timestamp
    window_type: _common_enums_pb2.MetricsWindowLength
    wf_spec_name: str
    wf_spec_version: int
    def __init__(self, window_start: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., window_type: _Optional[_Union[_common_enums_pb2.MetricsWindowLength, str]] = ..., wf_spec_name: _Optional[str] = ..., wf_spec_version: _Optional[int] = ...) -> None: ...
