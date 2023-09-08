from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from typing import ClassVar as _ClassVar

DESCRIPTOR: _descriptor.FileDescriptor

class LHStatus(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    STARTING: _ClassVar[LHStatus]
    RUNNING: _ClassVar[LHStatus]
    COMPLETED: _ClassVar[LHStatus]
    HALTING: _ClassVar[LHStatus]
    HALTED: _ClassVar[LHStatus]
    ERROR: _ClassVar[LHStatus]
    EXCEPTION: _ClassVar[LHStatus]

class TaskStatus(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    TASK_SCHEDULED: _ClassVar[TaskStatus]
    TASK_RUNNING: _ClassVar[TaskStatus]
    TASK_SUCCESS: _ClassVar[TaskStatus]
    TASK_FAILED: _ClassVar[TaskStatus]
    TASK_TIMEOUT: _ClassVar[TaskStatus]
    TASK_OUTPUT_SERIALIZING_ERROR: _ClassVar[TaskStatus]
    TASK_INPUT_VAR_SUB_ERROR: _ClassVar[TaskStatus]
    TASK_CANCELLED: _ClassVar[TaskStatus]

class MetricsWindowLength(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    MINUTES_5: _ClassVar[MetricsWindowLength]
    HOURS_2: _ClassVar[MetricsWindowLength]
    DAYS_1: _ClassVar[MetricsWindowLength]

class VariableType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    JSON_OBJ: _ClassVar[VariableType]
    JSON_ARR: _ClassVar[VariableType]
    DOUBLE: _ClassVar[VariableType]
    BOOL: _ClassVar[VariableType]
    STR: _ClassVar[VariableType]
    INT: _ClassVar[VariableType]
    BYTES: _ClassVar[VariableType]
    NULL: _ClassVar[VariableType]

class WaitForThreadsPolicy(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    STOP_ON_FAILURE: _ClassVar[WaitForThreadsPolicy]
STARTING: LHStatus
RUNNING: LHStatus
COMPLETED: LHStatus
HALTING: LHStatus
HALTED: LHStatus
ERROR: LHStatus
EXCEPTION: LHStatus
TASK_SCHEDULED: TaskStatus
TASK_RUNNING: TaskStatus
TASK_SUCCESS: TaskStatus
TASK_FAILED: TaskStatus
TASK_TIMEOUT: TaskStatus
TASK_OUTPUT_SERIALIZING_ERROR: TaskStatus
TASK_INPUT_VAR_SUB_ERROR: TaskStatus
TASK_CANCELLED: TaskStatus
MINUTES_5: MetricsWindowLength
HOURS_2: MetricsWindowLength
DAYS_1: MetricsWindowLength
JSON_OBJ: VariableType
JSON_ARR: VariableType
DOUBLE: VariableType
BOOL: VariableType
STR: VariableType
INT: VariableType
BYTES: VariableType
NULL: VariableType
STOP_ON_FAILURE: WaitForThreadsPolicy
