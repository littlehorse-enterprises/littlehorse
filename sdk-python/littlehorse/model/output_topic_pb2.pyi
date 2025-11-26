import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.workflow_event_pb2 as _workflow_event_pb2
import littlehorse.model.user_tasks_pb2 as _user_tasks_pb2
import littlehorse.model.task_run_pb2 as _task_run_pb2
import littlehorse.model.task_def_pb2 as _task_def_pb2
import littlehorse.model.wf_spec_pb2 as _wf_spec_pb2
import littlehorse.model.wf_run_pb2 as _wf_run_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
import littlehorse.model.struct_def_pb2 as _struct_def_pb2
import littlehorse.model.external_event_pb2 as _external_event_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class OutputTopicRecord(_message.Message):
    __slots__ = ("timestamp", "task_run", "workflow_event", "wf_run", "user_task_run", "variable", "external_event", "correlated_event", "task_checkpoint")
    TIMESTAMP_FIELD_NUMBER: _ClassVar[int]
    TASK_RUN_FIELD_NUMBER: _ClassVar[int]
    WORKFLOW_EVENT_FIELD_NUMBER: _ClassVar[int]
    WF_RUN_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_RUN_FIELD_NUMBER: _ClassVar[int]
    VARIABLE_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_FIELD_NUMBER: _ClassVar[int]
    CORRELATED_EVENT_FIELD_NUMBER: _ClassVar[int]
    TASK_CHECKPOINT_FIELD_NUMBER: _ClassVar[int]
    timestamp: _timestamp_pb2.Timestamp
    task_run: _task_run_pb2.TaskRun
    workflow_event: _workflow_event_pb2.WorkflowEvent
    wf_run: _wf_run_pb2.WfRun
    user_task_run: _user_tasks_pb2.UserTaskRun
    variable: _variable_pb2.Variable
    external_event: _external_event_pb2.ExternalEvent
    correlated_event: _external_event_pb2.CorrelatedEvent
    task_checkpoint: _task_run_pb2.Checkpoint
    def __init__(self, timestamp: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., task_run: _Optional[_Union[_task_run_pb2.TaskRun, _Mapping]] = ..., workflow_event: _Optional[_Union[_workflow_event_pb2.WorkflowEvent, _Mapping]] = ..., wf_run: _Optional[_Union[_wf_run_pb2.WfRun, _Mapping]] = ..., user_task_run: _Optional[_Union[_user_tasks_pb2.UserTaskRun, _Mapping]] = ..., variable: _Optional[_Union[_variable_pb2.Variable, _Mapping]] = ..., external_event: _Optional[_Union[_external_event_pb2.ExternalEvent, _Mapping]] = ..., correlated_event: _Optional[_Union[_external_event_pb2.CorrelatedEvent, _Mapping]] = ..., task_checkpoint: _Optional[_Union[_task_run_pb2.Checkpoint, _Mapping]] = ...) -> None: ...

class MetadataOutputTopicRecord(_message.Message):
    __slots__ = ("wf_spec", "task_def", "external_event_def", "workflow_event_def", "user_task_def", "struct_def")
    WF_SPEC_FIELD_NUMBER: _ClassVar[int]
    TASK_DEF_FIELD_NUMBER: _ClassVar[int]
    EXTERNAL_EVENT_DEF_FIELD_NUMBER: _ClassVar[int]
    WORKFLOW_EVENT_DEF_FIELD_NUMBER: _ClassVar[int]
    USER_TASK_DEF_FIELD_NUMBER: _ClassVar[int]
    STRUCT_DEF_FIELD_NUMBER: _ClassVar[int]
    wf_spec: _wf_spec_pb2.WfSpec
    task_def: _task_def_pb2.TaskDef
    external_event_def: _external_event_pb2.ExternalEventDef
    workflow_event_def: _workflow_event_pb2.WorkflowEventDef
    user_task_def: _user_tasks_pb2.UserTaskDef
    struct_def: _struct_def_pb2.StructDef
    def __init__(self, wf_spec: _Optional[_Union[_wf_spec_pb2.WfSpec, _Mapping]] = ..., task_def: _Optional[_Union[_task_def_pb2.TaskDef, _Mapping]] = ..., external_event_def: _Optional[_Union[_external_event_pb2.ExternalEventDef, _Mapping]] = ..., workflow_event_def: _Optional[_Union[_workflow_event_pb2.WorkflowEventDef, _Mapping]] = ..., user_task_def: _Optional[_Union[_user_tasks_pb2.UserTaskDef, _Mapping]] = ..., struct_def: _Optional[_Union[_struct_def_pb2.StructDef, _Mapping]] = ...) -> None: ...
