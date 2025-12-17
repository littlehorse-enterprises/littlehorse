import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class WfRunMigrationPlan(_message.Message):
    __slots__ = ("id", "created_at", "migration_plan")
    ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    MIGRATION_PLAN_FIELD_NUMBER: _ClassVar[int]
    id: _object_id_pb2.MigrationPlanId
    created_at: _timestamp_pb2.Timestamp
    migration_plan: MigrationPlan
    def __init__(self, id: _Optional[_Union[_object_id_pb2.MigrationPlanId, _Mapping]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., migration_plan: _Optional[_Union[MigrationPlan, _Mapping]] = ...) -> None: ...

class MigrationPlan(_message.Message):
    __slots__ = ("thread_migrations",)
    class ThreadMigrationsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: ThreadMigrationPlan
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[ThreadMigrationPlan, _Mapping]] = ...) -> None: ...
    THREAD_MIGRATIONS_FIELD_NUMBER: _ClassVar[int]
    thread_migrations: _containers.MessageMap[str, ThreadMigrationPlan]
    def __init__(self, thread_migrations: _Optional[_Mapping[str, ThreadMigrationPlan]] = ...) -> None: ...

class ThreadMigrationPlan(_message.Message):
    __slots__ = ("new_thread_name", "node_migrations")
    class NodeMigrationsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: NodeMigrationPlan
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[NodeMigrationPlan, _Mapping]] = ...) -> None: ...
    NEW_THREAD_NAME_FIELD_NUMBER: _ClassVar[int]
    NODE_MIGRATIONS_FIELD_NUMBER: _ClassVar[int]
    new_thread_name: str
    node_migrations: _containers.MessageMap[str, NodeMigrationPlan]
    def __init__(self, new_thread_name: _Optional[str] = ..., node_migrations: _Optional[_Mapping[str, NodeMigrationPlan]] = ...) -> None: ...

class NodeMigrationPlan(_message.Message):
    __slots__ = ("new_node",)
    NEW_NODE_FIELD_NUMBER: _ClassVar[int]
    new_node: str
    def __init__(self, new_node: _Optional[str] = ...) -> None: ...
