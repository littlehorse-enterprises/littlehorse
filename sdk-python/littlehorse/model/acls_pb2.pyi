from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ACLResource(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    ACL_WF_SPEC: _ClassVar[ACLResource]
    ACL_TASK_DEF: _ClassVar[ACLResource]
    ACL_EXTERNAL_EVENT_DEF: _ClassVar[ACLResource]
    ACL_USER_TASK_DEF: _ClassVar[ACLResource]
    ACL_PRINCIPAL: _ClassVar[ACLResource]
    ACL_TENANT: _ClassVar[ACLResource]
    ACL_ALL_RESOURCE_TYPES: _ClassVar[ACLResource]

class ACLAction(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    READ: _ClassVar[ACLAction]
    EXECUTE: _ClassVar[ACLAction]
    WRITE_METADATA: _ClassVar[ACLAction]
    ALL_ACTIONS: _ClassVar[ACLAction]
ACL_WF_SPEC: ACLResource
ACL_TASK_DEF: ACLResource
ACL_EXTERNAL_EVENT_DEF: ACLResource
ACL_USER_TASK_DEF: ACLResource
ACL_PRINCIPAL: ACLResource
ACL_TENANT: ACLResource
ACL_ALL_RESOURCE_TYPES: ACLResource
READ: ACLAction
EXECUTE: ACLAction
WRITE_METADATA: ACLAction
ALL_ACTIONS: ACLAction

class Principal(_message.Message):
    __slots__ = ["id", "acls", "tenant_id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    ACLS_FIELD_NUMBER: _ClassVar[int]
    TENANT_ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    acls: _containers.RepeatedCompositeFieldContainer[ServerACL]
    tenant_id: str
    def __init__(self, id: _Optional[str] = ..., acls: _Optional[_Iterable[_Union[ServerACL, _Mapping]]] = ..., tenant_id: _Optional[str] = ...) -> None: ...

class Tenant(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...

class ServerACL(_message.Message):
    __slots__ = ["resources", "allowed_actions", "name", "prefix"]
    RESOURCES_FIELD_NUMBER: _ClassVar[int]
    ALLOWED_ACTIONS_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    PREFIX_FIELD_NUMBER: _ClassVar[int]
    resources: _containers.RepeatedScalarFieldContainer[ACLResource]
    allowed_actions: _containers.RepeatedScalarFieldContainer[ACLAction]
    name: str
    prefix: str
    def __init__(self, resources: _Optional[_Iterable[_Union[ACLResource, str]]] = ..., allowed_actions: _Optional[_Iterable[_Union[ACLAction, str]]] = ..., name: _Optional[str] = ..., prefix: _Optional[str] = ...) -> None: ...

class PutPrincipalRequest(_message.Message):
    __slots__ = ["id", "acls", "tenant_id", "overwrite"]
    ID_FIELD_NUMBER: _ClassVar[int]
    ACLS_FIELD_NUMBER: _ClassVar[int]
    TENANT_ID_FIELD_NUMBER: _ClassVar[int]
    OVERWRITE_FIELD_NUMBER: _ClassVar[int]
    id: str
    acls: _containers.RepeatedCompositeFieldContainer[ServerACL]
    tenant_id: str
    overwrite: bool
    def __init__(self, id: _Optional[str] = ..., acls: _Optional[_Iterable[_Union[ServerACL, _Mapping]]] = ..., tenant_id: _Optional[str] = ..., overwrite: bool = ...) -> None: ...

class DeletePrincipalRequest(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...

class PutTenantRequest(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...

class PutTenantResponse(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...
