from __future__ import annotations

from datetime import datetime
from enum import Enum
from typing import Mapping, Optional

from pydantic import Field

from lh_lib.schema.wf_run_schema import LHFailureReasonEnum
from lh_lib.schema.wf_spec_schema import ACCEPTABLE_TYPES
from lh_sdk.utils import LHBaseModel


class TaskRunStartedEvent(LHBaseModel):
    worker_id: str
    stdin: Optional[str] = None
    node_name: Optional[str] = None
    task_run_position: int
    thread_id: int


class TaskRunResultSchema(LHBaseModel):
    stdout: Optional[str] = None
    stderr: Optional[str] = None
    success: bool
    returncode: int


class TaskRunEndedEvent(LHBaseModel):
    result: TaskRunResultSchema
    thread_id: int
    task_run_position: int
    reason: Optional[LHFailureReasonEnum] = None
    message: Optional[str] = None


class TaskRunEventSchema(LHBaseModel):
    task_def_version_number: int
    thread_id: int
    task_run_position: int
    timestamp: datetime
    started_event: Optional[TaskRunStartedEvent] = None
    ended_event: Optional[TaskRunEndedEvent] = None


class TaskScheduleRequestSchema(LHBaseModel):
    task_def_id: str
    task_def_name: str
    wf_run_id: str
    wf_spec_id: str
    wf_spec_name: str
    thread_id: int
    task_run_position: int

    variable_substitutions: Optional[Mapping[str, ACCEPTABLE_TYPES]] = None

    kafka_topic: str


class WFEventTypeEnum(Enum):
    WF_RUN_STARTED = "WF_RUN_STARTED"
    TASK_EVENT = "TASK_EVENT"
    EXTERNAL_EVENT = "EXTERNAL_EVENT"
    TIMER_EVENT = "TIMER_EVENT"
    WF_RUN_STOP_REQUEST = "WF_RUN_STOP_REQUEST"
    WF_RUN_RESUME_REQUEST = "WF_RUN_RESUME_REQUEST"


class WFEventSchema(LHBaseModel):
    wf_spec_id: Optional[str] = None
    wf_spec_name: Optional[str] = None
    wf_run_id: str
    timestamp: datetime
    thread_id: int = Field(default=0)
    type: WFEventTypeEnum
    content: str