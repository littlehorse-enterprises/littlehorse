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

class MetadataStatus(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    ACTIVE: _ClassVar[MetadataStatus]
    ARCHIVED: _ClassVar[MetadataStatus]
    TERMINATING: _ClassVar[MetadataStatus]

class TaskStatus(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    TASK_SCHEDULED: _ClassVar[TaskStatus]
    TASK_RUNNING: _ClassVar[TaskStatus]
    TASK_SUCCESS: _ClassVar[TaskStatus]
    TASK_FAILED: _ClassVar[TaskStatus]
    TASK_TIMEOUT: _ClassVar[TaskStatus]
    TASK_OUTPUT_SERIALIZING_ERROR: _ClassVar[TaskStatus]
    TASK_INPUT_VAR_SUB_ERROR: _ClassVar[TaskStatus]
    TASK_EXCEPTION: _ClassVar[TaskStatus]
    TASK_PENDING: _ClassVar[TaskStatus]

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

class LHErrorType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    CHILD_FAILURE: _ClassVar[LHErrorType]
    VAR_SUB_ERROR: _ClassVar[LHErrorType]
    VAR_MUTATION_ERROR: _ClassVar[LHErrorType]
    USER_TASK_CANCELLED: _ClassVar[LHErrorType]
    TIMEOUT: _ClassVar[LHErrorType]
    TASK_FAILURE: _ClassVar[LHErrorType]
    VAR_ERROR: _ClassVar[LHErrorType]
    TASK_ERROR: _ClassVar[LHErrorType]
    INTERNAL_ERROR: _ClassVar[LHErrorType]
STARTING: LHStatus
RUNNING: LHStatus
COMPLETED: LHStatus
HALTING: LHStatus
HALTED: LHStatus
ERROR: LHStatus
EXCEPTION: LHStatus
ACTIVE: MetadataStatus
ARCHIVED: MetadataStatus
TERMINATING: MetadataStatus
TASK_SCHEDULED: TaskStatus
TASK_RUNNING: TaskStatus
TASK_SUCCESS: TaskStatus
TASK_FAILED: TaskStatus
TASK_TIMEOUT: TaskStatus
TASK_OUTPUT_SERIALIZING_ERROR: TaskStatus
TASK_INPUT_VAR_SUB_ERROR: TaskStatus
TASK_EXCEPTION: TaskStatus
TASK_PENDING: TaskStatus
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
CHILD_FAILURE: LHErrorType
VAR_SUB_ERROR: LHErrorType
VAR_MUTATION_ERROR: LHErrorType
USER_TASK_CANCELLED: LHErrorType
TIMEOUT: LHErrorType
TASK_FAILURE: LHErrorType
VAR_ERROR: LHErrorType
TASK_ERROR: LHErrorType
INTERNAL_ERROR: LHErrorType
