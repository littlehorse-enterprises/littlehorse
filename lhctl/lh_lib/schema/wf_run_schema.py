from datetime import datetime
from enum import Enum

from typing import TYPE_CHECKING, Any, List, Mapping, Optional, Union
from lh_sdk.utils import LHBaseModel
from lh_lib.schema.wf_spec_schema import ACCEPTABLE_TYPES, EdgeSchema


VALID_VARIABLE_TYPE = Union[float, int, str, bool, list, dict]


class LHFailureReasonEnum(Enum):
    TASK_FAILURE = "TASK_FAILURE"
    VARIABLE_LOOKUP_ERROR = "VARIABLE_LOOKUP_ERROR"
    INVALID_WF_SPEC_ERROR = "INVALID_WF_SPEC_ERROR"
    TIMEOUT = "TIMEOUT"
    INTERNAL_LITTLEHORSE_ERROR = "INTERNAL_LITTLEHORSE_ERROR"


class NodeTypeEnum(Enum):
    TASK = "TASK"
    EXTERNAL_EVENT = "EXTERNAL_EVENT"
    SPAWN_THREAD = "SPAWN_THREAD"
    WAIT_FOR_THREAD = "WAIT_FOR_THREAD"
    SLEEP = "SLEEP"
    NOP = "NOP"
    THROW_EXCEPTION = "THROW_EXCEPTION"


class UpNextPair(LHBaseModel):
    edge: EdgeSchema
    attempt_number: int = 0


class LHExecutionStatusEnum(Enum):
    SCHEDULED = "SCHEDULED"
    RUNNING = "RUNNING"
    HALTING = "HALTING"
    HALTED = "HALTED"
    COMPLETED = "COMPLETED"


class TaskRunSchema(LHBaseModel):
    position: int
    number: int
    wf_spec_id: str
    wf_spec_name: str
    thread_id: int
    attempt_number: int = 0

    task_def_version_number: Optional[int] = None

    node_name: str

    worker_id: Optional[str] = None
    stdin: Optional[str] = None
    stdout: Optional[ACCEPTABLE_TYPES] = None
    stderr: Optional[ACCEPTABLE_TYPES] = None
    returncode: Optional[int] = None

    schedule_time: Optional[datetime] = None
    start_time: Optional[datetime] = None
    end_time: Optional[datetime] = None

    failure_reason: Optional[LHFailureReasonEnum] = None
    failure_message: Optional[str] = None

    node_type: NodeTypeEnum
    status: LHExecutionStatusEnum


class WFHaltReasonEnum(Enum):
    PARENT_STOPPED = "PARENT_STOPPED"
    PARENT_INTERRUPTED = "PARENT_INTERRUPTED"
    FAILED = "FAILED"
    INTERRUPT = "INTERRUPT"
    MANUAL_STOP = "MANUAL_STOP"


class ThreadRunSchema(LHBaseModel):
    thread_spec_name: str
    up_next: list[UpNextPair]
    status: LHExecutionStatusEnum

    variables: Optional[Mapping[str, Optional[VALID_VARIABLE_TYPE]]] = None
    id: int
    parent_thread_id: Optional[int] = None
    child_thread_ids: List[int]
    active_interrupt_thread_ids: List[int]
    handled_interrupt_thread_ids: List[int]

    task_runs: List[TaskRunSchema]
 
    error_message: Optional[str] = None
    is_interrupt_thread: bool = False

    variable_locks: Mapping[str, int]
    halt_reasons: Optional[List[WFHaltReasonEnum]] = None # TODO


class WFEventIdSchema(LHBaseModel):
    topic: str
    partition: int
    offset: int


class ExternalEventPayloadSchema(LHBaseModel):
    external_event_def_id: str
    external_event_def_name: str
    content: Any
    timestamp: Optional[datetime] = None


class ExternalEventCorrelSchema(LHBaseModel):
    event: ExternalEventPayloadSchema
    assigned_task_run_execution_number: Optional[int] = None
    assigned_node_name: Optional[str] = None
    assigned_thread_id: Optional[str] = None
    arrival_time: Optional[datetime] = None


class ThreadRunMetaSchema(LHBaseModel):
    thread_id: int
    parent_thread_id: Optional[int] = None
    thread_spec_name: str
    source_node_name: Optional[str] = None
    source_node_id: Optional[str] = None


class WFRunSchema(LHBaseModel):
    created: Optional[datetime] = None
    object_id: str
    wf_spec_name: str
    wf_spec_digest: str
    thread_runs: List[ThreadRunSchema]

    status: LHExecutionStatusEnum
    start_time: Optional[datetime] = None
    end_time: Optional[datetime] = None

    error_code: Optional[LHFailureReasonEnum] = None
    error_message: Optional[str] = None

    correlated_events: Optional[Mapping[str, List[ExternalEventCorrelSchema]]] = None
    pending_interrupts: Optional[List[str]] = None
