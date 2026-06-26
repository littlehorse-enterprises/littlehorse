import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.common_enums_pb2 as _common_enums_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class BulkJobStatus(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    BULK_JOB_RUNNING: _ClassVar[BulkJobStatus]
    BULK_JOB_COMPLETED: _ClassVar[BulkJobStatus]
    BULK_JOB_FAILED: _ClassVar[BulkJobStatus]
BULK_JOB_RUNNING: BulkJobStatus
BULK_JOB_COMPLETED: BulkJobStatus
BULK_JOB_FAILED: BulkJobStatus

class BulkJob(_message.Message):
    __slots__ = ("id", "created_at", "status", "bulk_delete_wf_run", "subprocesses", "total_subprocesses")
    class Subprocess(_message.Message):
        __slots__ = ("id", "status")
        ID_FIELD_NUMBER: _ClassVar[int]
        STATUS_FIELD_NUMBER: _ClassVar[int]
        id: int
        status: BulkJobStatus
        def __init__(self, id: _Optional[int] = ..., status: _Optional[_Union[BulkJobStatus, str]] = ...) -> None: ...
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    BULK_DELETE_WF_RUN_FIELD_NUMBER: _ClassVar[int]
    SUBPROCESSES_FIELD_NUMBER: _ClassVar[int]
    TOTAL_SUBPROCESSES_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.BulkJobId
    created_at: _timestamp_pb2.Timestamp
    status: BulkJobStatus
    bulk_delete_wf_run: BulkDeleteWfRun
    subprocesses: _containers.RepeatedCompositeFieldContainer[BulkJob.Subprocess]
    total_subprocesses: int
    def __init__(self, id: _Optional[_Union[_object_id_pb2.BulkJobId, _Mapping]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., status: _Optional[_Union[BulkJobStatus, str]] = ..., bulk_delete_wf_run: _Optional[_Union[BulkDeleteWfRun, _Mapping]] = ..., subprocesses: _Optional[_Iterable[_Union[BulkJob.Subprocess, _Mapping]]] = ..., total_subprocesses: _Optional[int] = ...) -> None: ...

class BulkDeleteWfRun(_message.Message):
    __slots__ = ("wf_spec_name", "earliest_start", "latest_start", "wf_run_status")
    WF_SPEC_NAME_FIELD_NUMBER: _ClassVar[int]
    EARLIEST_START_FIELD_NUMBER: _ClassVar[int]
    LATEST_START_FIELD_NUMBER: _ClassVar[int]
    WF_RUN_STATUS_FIELD_NUMBER: _ClassVar[int]
    wf_spec_name: str
    earliest_start: _timestamp_pb2.Timestamp
    latest_start: _timestamp_pb2.Timestamp
    wf_run_status: _common_enums_pb2.LHStatus
    def __init__(self, wf_spec_name: _Optional[str] = ..., earliest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., latest_start: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., wf_run_status: _Optional[_Union[_common_enums_pb2.LHStatus, str]] = ...) -> None: ...

class CreateBulkJobRequest(_message.Message):
    __slots__ = ("id", "bulk_delete_wf_run")
    ID_FIELD_NUMBER: _ClassVar[int]
    BULK_DELETE_WF_RUN_FIELD_NUMBER: _ClassVar[int]
    id: str
    bulk_delete_wf_run: BulkDeleteWfRun
    def __init__(self, id: _Optional[str] = ..., bulk_delete_wf_run: _Optional[_Union[BulkDeleteWfRun, _Mapping]] = ...) -> None: ...

class GetBulkJobRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.BulkJobId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.BulkJobId, _Mapping]] = ...) -> None: ...
