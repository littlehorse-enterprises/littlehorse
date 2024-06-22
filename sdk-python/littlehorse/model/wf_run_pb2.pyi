from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ThreadType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    ENTRYPOINT: _ClassVar[ThreadType]
    CHILD: _ClassVar[ThreadType]
    INTERRUPT: _ClassVar[ThreadType]
    FAILURE_HANDLER: _ClassVar[ThreadType]
ENTRYPOINT: ThreadType
CHILD: ThreadType
INTERRUPT: ThreadType
FAILURE_HANDLER: ThreadType

class WfRun(_message.Message):
    __slots__ = ["id", "wf_spec_id", "old_wf_spec_versions", "status", "greatest_threadrun_number", "start_time", "end_time", "thread_runs", "pending_interrupts", "pending_failures"]
    ID_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    OLD_WF_SPEC_VERSIONS_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    GREATEST_THREADRUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    START_TIME_FIELD_NUMBER: _ClassVar[int]
    END_TIME_FIELD_NUMBER: _ClassVar[int]
    THREAD_RUNS_FIELD_NUMBER: _ClassVar[int]
    PENDING_INTERRUPTS_FIELD_NUMBER: _ClassVar[int]
    PENDING_FAILURES_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.WfRunId
    wf_spec_id: _object_id_pb2.WfSpecId
    old_wf_spec_versions: _containers.RepeatedCompositeFieldContainer[_object_id_pb2.WfSpecId]
    status: _common_enums_pb2.LHStatus
    greatest_threadrun_number: int
    start_time: _timestamp_pb2.Timestamp
    end_time: _timestamp_pb2.Timestamp
    thread_runs: _containers.RepeatedCompositeFieldContainer[ThreadRun]
    pending_interrupts: _containers.RepeatedCompositeFieldContainer[PendingInterrupt]
    pending_failures: _containers.RepeatedCompositeFieldContainer[PendingFailureHandler]
    def __init__(self, id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ..., wf_spec_id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., old_wf_spec_versions: _Optional[_Iterable[_Union[_object_id_pb2.WfSpecId, _Mapping]]] = ..., status: _Optional[_Union[_common_enums_pb2.LHStatus, str]] = ..., greatest_threadrun_number: _Optional[int] = ..., start_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., end_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., thread_runs: _Optional[_Iterable[_Union[ThreadRun, _Mapping]]] = ..., pending_interrupts: _Optional[_Iterable[_Union[PendingInterrupt, _Mapping]]] = ..., pending_failures: _Optional[_Iterable[_Union[PendingFailureHandler, _Mapping]]] = ...) -> None: ...

class ThreadRun(_message.Message):
    __slots__ = ["wf_spec_id", "number", "status", "thread_spec_name", "start_time", "end_time", "error_message", "child_thread_ids", "parent_thread_id", "halt_reasons", "interrupt_trigger_id", "failure_being_handled", "current_node_position", "handled_failed_children", "type"]
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    NUMBER_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    THREAD_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    START_TIME_FIELD_NUMBER: _ClassVar[int]
    END_TIME_FIELD_NUMBER: _ClassVar[int]
    ERROR_MESSAGE_FIELD_NUMBER: _ClassVar[int]
    CHILD_THREAD_IDS_FIELD_NUMBER: _ClassVar[int]
    PARENT_THREAD_ID_FIELD_NUMBER: _ClassVar[int]
    HALT_REASONS_FIELD_NUMBER: _ClassVar[int]
    INTERRUPT_TRIGGER_ID_FIELD_NUMBER: _ClassVar[int]
    FAILURE_BEING_HANDLED_FIELD_NUMBER: _ClassVar[int]
    CURRENT_NODE_POSITION_FIELD_NUMBER: _ClassVar[int]
    HANDLED_FAILED_CHILDREN_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    wf_spec_id: _object_id_pb2.WfSpecId
    number: int
    status: _common_enums_pb2.LHStatus
    thread_spec_name: str
    start_time: _timestamp_pb2.Timestamp
    end_time: _timestamp_pb2.Timestamp
    error_message: str
    child_thread_ids: _containers.RepeatedScalarFieldContainer[int]
    parent_thread_id: int
    halt_reasons: _containers.RepeatedCompositeFieldContainer[ThreadHaltReason]
    interrupt_trigger_id: _object_id_pb2.ExternalEventId
    failure_being_handled: FailureBeingHandled
    current_node_position: int
    handled_failed_children: _containers.RepeatedScalarFieldContainer[int]
    type: ThreadType
    def __init__(self, wf_spec_id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., number: _Optional[int] = ..., status: _Optional[_Union[_common_enums_pb2.LHStatus, str]] = ..., thread_spec_name: _Optional[str] = ..., start_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., end_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., error_message: _Optional[str] = ..., child_thread_ids: _Optional[_Iterable[int]] = ..., parent_thread_id: _Optional[int] = ..., halt_reasons: _Optional[_Iterable[_Union[ThreadHaltReason, _Mapping]]] = ..., interrupt_trigger_id: _Optional[_Union[_object_id_pb2.ExternalEventId, _Mapping]] = ..., failure_being_handled: _Optional[_Union[FailureBeingHandled, _Mapping]] = ..., current_node_position: _Optional[int] = ..., handled_failed_children: _Optional[_Iterable[int]] = ..., type: _Optional[_Union[ThreadType, str]] = ...) -> None: ...

class FailureBeingHandled(_message.Message):
    __slots__ = ["thread_run_number", "node_run_position", "failure_number"]
    THREAD_RUN_NUMBER_FIELD_NUMBER: _ClassVar[int]
    NODE_RUN_POSITION_FIELD_NUMBER: _ClassVar[int]
    FAILURE_NUMBER_FIELD_NUMBER: _ClassVar[int]
    thread_run_number: int
    node_run_position: int
    failure_number: int
    def __init__(self, thread_run_number: _Optional[int] = ..., node_run_position: _Optional[int] = ..., failure_number: _Optional[int] = ...) -> None: ...

class PendingInterrupt(_message.Message):
    __slots__ = ["external_event_id", "handler_spec_name", "interrupted_thread_id"]
    EXTERNAL_EVENT_ID_FIELD_NUMBER: _ClassVar[int]
    HANDLER_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    INTERRUPTED_THREAD_ID_FIELD_NUMBER: _ClassVar[int]
    external_event_id: _object_id_pb2.ExternalEventId
    handler_spec_name: str
    interrupted_thread_id: int
    def __init__(self, external_event_id: _Optional[_Union[_object_id_pb2.ExternalEventId, _Mapping]] = ..., handler_spec_name: _Optional[str] = ..., interrupted_thread_id: _Optional[int] = ...) -> None: ...

class PendingFailureHandler(_message.Message):
    __slots__ = ["failed_thread_run", "handler_spec_name"]
    FAILED_THREAD_RUN_FIELD_NUMBER: _ClassVar[int]
    HANDLER_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    failed_thread_run: int
    handler_spec_name: str
    def __init__(self, failed_thread_run: _Optional[int] = ..., handler_spec_name: _Optional[str] = ...) -> None: ...

class PendingInterruptHaltReason(_message.Message):
    __slots__ = ["external_event_id"]
    EXTERNAL_EVENT_ID_FIELD_NUMBER: _ClassVar[int]
    external_event_id: _object_id_pb2.ExternalEventId
    def __init__(self, external_event_id: _Optional[_Union[_object_id_pb2.ExternalEventId, _Mapping]] = ...) -> None: ...

class PendingFailureHandlerHaltReason(_message.Message):
    __slots__ = ["node_run_position"]
    NODE_RUN_POSITION_FIELD_NUMBER: _ClassVar[int]
    node_run_position: int
    def __init__(self, node_run_position: _Optional[int] = ...) -> None: ...

class HandlingFailureHaltReason(_message.Message):
    __slots__ = ["handler_thread_id"]
    HANDLER_THREAD_ID_FIELD_NUMBER: _ClassVar[int]
    handler_thread_id: int
    def __init__(self, handler_thread_id: _Optional[int] = ...) -> None: ...

class ParentHalted(_message.Message):
    __slots__ = ["parent_thread_id"]
    PARENT_THREAD_ID_FIELD_NUMBER: _ClassVar[int]
    parent_thread_id: int
    def __init__(self, parent_thread_id: _Optional[int] = ...) -> None: ...

class Interrupted(_message.Message):
    __slots__ = ["interrupt_thread_id"]
    INTERRUPT_THREAD_ID_FIELD_NUMBER: _ClassVar[int]
    interrupt_thread_id: int
    def __init__(self, interrupt_thread_id: _Optional[int] = ...) -> None: ...

class ManualHalt(_message.Message):
    __slots__ = ["meaning_of_life"]
    MEANING_OF_LIFE_FIELD_NUMBER: _ClassVar[int]
    meaning_of_life: bool
    def __init__(self, meaning_of_life: bool = ...) -> None: ...

class ThreadHaltReason(_message.Message):
    __slots__ = ["parent_halted", "interrupted", "pending_interrupt", "pending_failure", "handling_failure", "manual_halt"]
    PARENT_HALTED_FIELD_NUMBER: _ClassVar[int]
    INTERRUPTED_FIELD_NUMBER: _ClassVar[int]
    PENDING_INTERRUPT_FIELD_NUMBER: _ClassVar[int]
    PENDING_FAILURE_FIELD_NUMBER: _ClassVar[int]
    HANDLING_FAILURE_FIELD_NUMBER: _ClassVar[int]
    MANUAL_HALT_FIELD_NUMBER: _ClassVar[int]
    parent_halted: ParentHalted
    interrupted: Interrupted
    pending_interrupt: PendingInterruptHaltReason
    pending_failure: PendingFailureHandlerHaltReason
    handling_failure: HandlingFailureHaltReason
    manual_halt: ManualHalt
    def __init__(self, parent_halted: _Optional[_Union[ParentHalted, _Mapping]] = ..., interrupted: _Optional[_Union[Interrupted, _Mapping]] = ..., pending_interrupt: _Optional[_Union[PendingInterruptHaltReason, _Mapping]] = ..., pending_failure: _Optional[_Union[PendingFailureHandlerHaltReason, _Mapping]] = ..., handling_failure: _Optional[_Union[HandlingFailureHaltReason, _Mapping]] = ..., manual_halt: _Optional[_Union[ManualHalt, _Mapping]] = ...) -> None: ...
