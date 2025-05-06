from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.common_wfspec_pb2 as _common_wfspec_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.workflow_event_pb2 as _workflow_event_pb2
import littlehorse.model.user_tasks_pb2 as _user_tasks_pb2
import littlehorse.model.task_run_pb2 as _task_run_pb2
import littlehorse.model.task_def_pb2 as _task_def_pb2
import littlehorse.model.wf_spec_pb2 as _wf_spec_pb2
import littlehorse.model.wf_run_pb2 as _wf_run_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
import littlehorse.model.external_event_pb2 as _external_event_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class OutputTopicRecord(_message.Message):
    __slots__ = ["id", "timestamp", "task_run_executed", "workflow_event", "wf_run", "user_task_run", "variable_update"]
    ID_FIELD_NUMBER: _ClassVar[int]
    TIMESTAMP_FIELD_NUMBER: _ClassVar[int]
    TASK_RUN_EXECUTED_FIELD_NUMBER: _ClassVar[int]
    WORKFLOW_EVENT_FIELD_NUMBER: _ClassVar[int]
    WF_RUN_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_RUN_FIELD_NUMBER: _ClassVar[int]
    VARIABLE_UPDATE_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.WfRunId
    timestamp: _timestamp_pb2.Timestamp
    task_run_executed: TaskRunExecutedRecord
    workflow_event: WorkflowEventRecord
    wf_run: WfRunUpdateRecord
    user_task_run: UserTaskRunUpdateRecord
    variable_update: VariableUpdateRecord
    def __init__(self, id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ..., timestamp: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., task_run_executed: _Optional[_Union[TaskRunExecutedRecord, _Mapping]] = ..., workflow_event: _Optional[_Union[WorkflowEventRecord, _Mapping]] = ..., wf_run: _Optional[_Union[WfRunUpdateRecord, _Mapping]] = ..., user_task_run: _Optional[_Union[UserTaskRunUpdateRecord, _Mapping]] = ..., variable_update: _Optional[_Union[VariableUpdateRecord, _Mapping]] = ...) -> None: ...

class TaskRunExecutedRecord(_message.Message):
    __slots__ = ["task_run"]
    TASK_RUN_FIELD_NUMBER: _ClassVar[int]
    task_run: _task_run_pb2.TaskRun
    def __init__(self, task_run: _Optional[_Union[_task_run_pb2.TaskRun, _Mapping]] = ...) -> None: ...

class WorkflowEventRecord(_message.Message):
    __slots__ = ["workflow_event", "wf_spec_id"]
    WORKFLOW_EVENT_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    workflow_event: _workflow_event_pb2.WorkflowEvent
    wf_spec_id: _object_id_pb2.WfSpecId
    def __init__(self, workflow_event: _Optional[_Union[_workflow_event_pb2.WorkflowEvent, _Mapping]] = ..., wf_spec_id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ...) -> None: ...

class WfRunUpdateRecord(_message.Message):
    __slots__ = ["wf_run"]
    WF_RUN_FIELD_NUMBER: _ClassVar[int]
    wf_run: _wf_run_pb2.WfRun
    def __init__(self, wf_run: _Optional[_Union[_wf_run_pb2.WfRun, _Mapping]] = ...) -> None: ...

class UserTaskRunUpdateRecord(_message.Message):
    __slots__ = ["user_task_run"]
    USER_TASK_RUN_FIELD_NUMBER: _ClassVar[int]
    user_task_run: _user_tasks_pb2.UserTaskRun
    def __init__(self, user_task_run: _Optional[_Union[_user_tasks_pb2.UserTaskRun, _Mapping]] = ...) -> None: ...

class VariableUpdateRecord(_message.Message):
    __slots__ = ["current_variable"]
    CURRENT_VARIABLE_FIELD_NUMBER: _ClassVar[int]
    current_variable: _variable_pb2.Variable
    def __init__(self, current_variable: _Optional[_Union[_variable_pb2.Variable, _Mapping]] = ...) -> None: ...

class MetadataOutputTopicRecord(_message.Message):
    __slots__ = ["wf_spec", "task_def", "external_event_def", "workflow_event_def", "user_task_def"]
    WF_SPEC_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_FIELD_NUMBER: _ClassVar[int]
    WORKFLOW_EVENT_DEF_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_DEF_FIELD_NUMBER: _ClassVar[int]
    wf_spec: _wf_spec_pb2.WfSpec
    task_def: _task_def_pb2.TaskDef
    external_event_def: _external_event_pb2.ExternalEventDef
    workflow_event_def: _workflow_event_pb2.WorkflowEventDef
    user_task_def: _user_tasks_pb2.UserTaskDef
    def __init__(self, wf_spec: _Optional[_Union[_wf_spec_pb2.WfSpec, _Mapping]] = ..., task_def: _Optional[_Union[_task_def_pb2.TaskDef, _Mapping]] = ..., external_event_def: _Optional[_Union[_external_event_pb2.ExternalEventDef, _Mapping]] = ..., workflow_event_def: _Optional[_Union[_workflow_event_pb2.WorkflowEventDef, _Mapping]] = ..., user_task_def: _Optional[_Union[_user_tasks_pb2.UserTaskDef, _Mapping]] = ...) -> None: ...
