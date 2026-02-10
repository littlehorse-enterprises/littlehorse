import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class WfRunMigrationPlan(_message.Message):
    __slots__ = ("id", "created_at", "thread_migration", "new_wfSpec")
    class ThreadMigrationEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: ThreadMigrationPlan
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[ThreadMigrationPlan, _Mapping]] = ...) -> None: ...
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    THREAD_MIGRATION_FIELD_NUMBER: _ClassVar[int]
    NEW_WFSPEC_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.MigrationPlanId
    created_at: _timestamp_pb2.Timestamp
    thread_migration: _containers.MessageMap[str, ThreadMigrationPlan]
    new_wfSpec: _object_id_pb2.WfSpecId
    def __init__(self, id: _Optional[_Union[_object_id_pb2.MigrationPlanId, _Mapping]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., thread_migration: _Optional[_Mapping[str, ThreadMigrationPlan]] = ..., new_wfSpec: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ...) -> None: ...

class ThreadMigrationPlan(_message.Message):
    __slots__ = ("new_thread_name", "node_migrations", "required_migration_vars")
    class NodeMigrationsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: NodeMigrationPlan
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[NodeMigrationPlan, _Mapping]] = ...) -> None: ...
    NEW_THREAD_NAME_FIELD_NUMBER: _ClassVar[int]
    NODE_MIGRATIONS_FIELD_NUMBER: _ClassVar[int]
    REQUIRED_MIGRATION_VARS_FIELD_NUMBER: _ClassVar[int]
    new_thread_name: str
    node_migrations: _containers.MessageMap[str, NodeMigrationPlan]
    required_migration_vars: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, new_thread_name: _Optional[str] = ..., node_migrations: _Optional[_Mapping[str, NodeMigrationPlan]] = ..., required_migration_vars: _Optional[_Iterable[str]] = ...) -> None: ...

class NodeMigrationPlan(_message.Message):
    __slots__ = ("new_node",)
    NEW_NODE_FIELD_NUMBER: _ClassVar[int]
    new_node: str
    def __init__(self, new_node: _Optional[str] = ...) -> None: ...
