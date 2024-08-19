from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.common_wfspec_pb2 as _common_wfspec_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class TaskDef(_message.Message):
    __slots__ = ["id", "input_vars", "created_at", "schema_output"]
    ID_FIELD_NUMBER: _ClassVar[int]
    INPUT_VARS_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    SCHEMA_OUTPUT_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.TaskDefId
    input_vars: _containers.RepeatedCompositeFieldContainer[_common_wfspec_pb2.VariableDef]
    created_at: _timestamp_pb2.Timestamp
    schema_output: TaskDefOutputSchema
    def __init__(self, id: _Optional[_Union[_object_id_pb2.TaskDefId, _Mapping]] = ..., input_vars: _Optional[_Iterable[_Union[_common_wfspec_pb2.VariableDef, _Mapping]]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., schema_output: _Optional[_Union[TaskDefOutputSchema, _Mapping]] = ...) -> None: ...

class TaskDefOutputSchema(_message.Message):
    __slots__ = ["value_def"]
    VALUE_DEF_FIELD_NUMBER: _ClassVar[int]
    value_def: _common_wfspec_pb2.VariableDef
    def __init__(self, value_def: _Optional[_Union[_common_wfspec_pb2.VariableDef, _Mapping]] = ...) -> None: ...
