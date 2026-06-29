import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
import littlehorse.model.object_id_pb2 as _object_id_pb2
import littlehorse.model.common_wfspec_pb2 as _common_wfspec_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class WorkflowMigrationPlan(_message.Message):
    __slots__ = ("workflow_migration_plan_id", "created_at", "thread_migrations", "old_wfSpec", "major_version", "revision")
    class ThreadMigrationsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: ThreadMigrationPlan
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[ThreadMigrationPlan, _Mapping]] = ...) -> None: ...
    WORKFLOW_MIGRATION_PLAN_ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    THREAD_MIGRATIONS_FIELD_NUMBER: _ClassVar[int]
    OLD_WFSPEC_FIELD_NUMBER: _ClassVar[int]
    MAJOR_VERSION_FIELD_NUMBER: _ClassVar[int]
    REVISION_FIELD_NUMBER: _ClassVar[int]
    workflow_migration_plan_id: _object_id_pb2.WorkflowMigrationPlanId
    created_at: _timestamp_pb2.Timestamp
    thread_migrations: _containers.MessageMap[str, ThreadMigrationPlan]
    old_wfSpec: _object_id_pb2.WfSpecId
    major_version: int
    revision: int
    def __init__(self, workflow_migration_plan_id: _Optional[_Union[_object_id_pb2.WorkflowMigrationPlanId, _Mapping]] = ..., created_at: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., thread_migrations: _Optional[_Mapping[str, ThreadMigrationPlan]] = ..., old_wfSpec: _Optional[_Union[_object_id_pb2.WfSpecId, _Mapping]] = ..., major_version: _Optional[int] = ..., revision: _Optional[int] = ...) -> None: ...

class ThreadMigrationPlan(_message.Message):
    __slots__ = ("new_thread_name", "node_migrations", "thread_spec_dependencies")
    class NodeMigrationsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: NodeMigrationPlan
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[NodeMigrationPlan, _Mapping]] = ...) -> None: ...
    NEW_THREAD_NAME_FIELD_NUMBER: _ClassVar[int]
    NODE_MIGRATIONS_FIELD_NUMBER: _ClassVar[int]
    THREAD_SPEC_DEPENDENCIES_FIELD_NUMBER: _ClassVar[int]
    new_thread_name: str
    node_migrations: _containers.MessageMap[str, NodeMigrationPlan]
    thread_spec_dependencies: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, new_thread_name: _Optional[str] = ..., node_migrations: _Optional[_Mapping[str, NodeMigrationPlan]] = ..., thread_spec_dependencies: _Optional[_Iterable[str]] = ...) -> None: ...

class ThreadMigrationPlanRequest(_message.Message):
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
    __slots__ = ("new_node_name",)
    NEW_NODE_NAME_FIELD_NUMBER: _ClassVar[int]
    new_node_name: str
    def __init__(self, new_node_name: _Optional[str] = ...) -> None: ...

class MigrationVars(_message.Message):
    __slots__ = ("var_assignment_by_var_name",)
    class VarAssignmentByVarNameEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _common_wfspec_pb2.VariableAssignment
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_common_wfspec_pb2.VariableAssignment, _Mapping]] = ...) -> None: ...
    VAR_ASSIGNMENT_BY_VAR_NAME_FIELD_NUMBER: _ClassVar[int]
    var_assignment_by_var_name: _containers.MessageMap[str, _common_wfspec_pb2.VariableAssignment]
    def __init__(self, var_assignment_by_var_name: _Optional[_Mapping[str, _common_wfspec_pb2.VariableAssignment]] = ...) -> None: ...
