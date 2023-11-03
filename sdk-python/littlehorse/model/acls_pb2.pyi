from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ACLResource(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    ACL_WORKFLOW: _ClassVar[ACLResource]
    ACL_TASK: _ClassVar[ACLResource]
    ACL_EXTERNAL_EVENT: _ClassVar[ACLResource]
    ACL_USER_TASK: _ClassVar[ACLResource]
    ACL_PRINCIPAL: _ClassVar[ACLResource]
    ACL_TENANT: _ClassVar[ACLResource]
    ALL: _ClassVar[ACLResource]

class ACLAction(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = []
    READ: _ClassVar[ACLAction]
    RUN: _ClassVar[ACLAction]
    WRITE_METADATA: _ClassVar[ACLAction]
    ALL_ACTIONS: _ClassVar[ACLAction]
ACL_WORKFLOW: ACLResource
ACL_TASK: ACLResource
ACL_EXTERNAL_EVENT: ACLResource
ACL_USER_TASK: ACLResource
ACL_PRINCIPAL: ACLResource
ACL_TENANT: ACLResource
ALL: ACLResource
READ: ACLAction
RUN: ACLAction
WRITE_METADATA: ACLAction
ALL_ACTIONS: ACLAction

class Principal(_message.Message):
    __slots__ = ["id", "tenant_acl_map", "global_acls"]
    class TenantAclMapEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: ServerACL
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[ServerACL, _Mapping]] = ...) -> None: ...
    ID_FIELD_NUMBER: _ClassVar[int]
    TENANT_ACL_MAP_FIELD_NUMBER: _ClassVar[int]
    GLOBAL_ACLS_FIELD_NUMBER: _ClassVar[int]
    id: str
    tenant_acl_map: _containers.MessageMap[str, ServerACL]
    global_acls: ServerACLs
    def __init__(self, id: _Optional[str] = ..., tenant_acl_map: _Optional[_Mapping[str, ServerACL]] = ..., global_acls: _Optional[_Union[ServerACLs, _Mapping]] = ...) -> None: ...

class Tenant(_message.Message):
    __slots__ = ["id"]
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...

class ServerACLs(_message.Message):
    __slots__ = ["acls"]
    ACLS_FIELD_NUMBER: _ClassVar[int]
    acls: _containers.RepeatedCompositeFieldContainer[ServerACL]
    def __init__(self, acls: _Optional[_Iterable[_Union[ServerACL, _Mapping]]] = ...) -> None: ...

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
    __slots__ = ["id", "tenant_acl_map", "global_acls", "overwrite"]
    class TenantAclMapEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: ServerACLs
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[ServerACLs, _Mapping]] = ...) -> None: ...
    ID_FIELD_NUMBER: _ClassVar[int]
    TENANT_ACL_MAP_FIELD_NUMBER: _ClassVar[int]
    GLOBAL_ACLS_FIELD_NUMBER: _ClassVar[int]
    OVERWRITE_FIELD_NUMBER: _ClassVar[int]
    id: str
    tenant_acl_map: _containers.MessageMap[str, ServerACLs]
    global_acls: ServerACLs
    overwrite: bool
    def __init__(self, id: _Optional[str] = ..., tenant_acl_map: _Optional[_Mapping[str, ServerACLs]] = ..., global_acls: _Optional[_Union[ServerACLs, _Mapping]] = ..., overwrite: bool = ...) -> None: ...

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
