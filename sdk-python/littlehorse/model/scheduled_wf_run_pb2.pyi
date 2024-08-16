from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.variable_pb2 as _variable_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ScheduledWfRun(_message.Message):
    __slots__ = ["id", "wf_spec_id", "variables", "parent_wf_run_id", "cron_expression", "created_at"]
    class VariablesEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _variable_pb2.VariableValue
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_variable_pb2.VariableValue, _Mapping]] = ...) -> None: ...
    ID_FIELD_NUMBER: _ClassVar[int]
    WF_SPEC_ID_FIELD_NUMBER: _ClassVar[int]
    VARIABLES_FIELD_NUMBER: _ClassVar[int]
    PARENT_WF_RUN_ID_FIELD_NUMBER: _ClassVar[int]
    CRON_EXPRESSION_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.ScheduledWfRunId
    wf_spec_id: _object_id_pb2.WfSpecId
    variables: _containers.MessageMap[str, _variable_pb2.VariableValue]
    parent_wf_run_id: _object_id_pb2.WfRunId
    cron_expression: str
    created_at: _timestamp_pb2.Timestamp
    def __init__(self, id: _Optional[_Union[_object_id_pb2.ScheduledWfRunId, _Mapping]] = ..., wf_spec_id: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., variables: _Optional[_Mapping[str, _variable_pb2.VariableValue]] = ..., parent_wf_run_id: _Optional[_Union[_object_id_pb2.WfRunId, _Mapping]] = ..., cron_expression: _Optional[str] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...
