# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: acls.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import timestamp_pb2 as google_dot_protobuf_dot_timestamp__pb2
import littlehorse.model.object_id_pb2 as object__id__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\nacls.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x0fobject_id.proto\"\xa2\x02\n\tPrincipal\x12$\n\x02id\x18\x01 \x01(\x0b\x32\x18.littlehorse.PrincipalId\x12.\n\ncreated_at\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x42\n\x0fper_tenant_acls\x18\x03 \x03(\x0b\x32).littlehorse.Principal.PerTenantAclsEntry\x12,\n\x0bglobal_acls\x18\x04 \x01(\x0b\x32\x17.littlehorse.ServerACLs\x1aM\n\x12PerTenantAclsEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12&\n\x05value\x18\x02 \x01(\x0b\x32\x17.littlehorse.ServerACLs:\x02\x38\x01\"[\n\x06Tenant\x12!\n\x02id\x18\x01 \x01(\x0b\x32\x15.littlehorse.TenantId\x12.\n\ncreated_at\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\"2\n\nServerACLs\x12$\n\x04\x61\x63ls\x18\x01 \x03(\x0b\x32\x16.littlehorse.ServerACL\"\x9e\x01\n\tServerACL\x12+\n\tresources\x18\x01 \x03(\x0e\x32\x18.littlehorse.ACLResource\x12/\n\x0f\x61llowed_actions\x18\x02 \x03(\x0e\x32\x16.littlehorse.ACLAction\x12\x0e\n\x04name\x18\x03 \x01(\tH\x00\x12\x10\n\x06prefix\x18\x04 \x01(\tH\x00\x42\x11\n\x0fresource_filter\"\xff\x01\n\x13PutPrincipalRequest\x12\n\n\x02id\x18\x01 \x01(\t\x12L\n\x0fper_tenant_acls\x18\x02 \x03(\x0b\x32\x33.littlehorse.PutPrincipalRequest.PerTenantAclsEntry\x12,\n\x0bglobal_acls\x18\x03 \x01(\x0b\x32\x17.littlehorse.ServerACLs\x12\x11\n\toverwrite\x18\x05 \x01(\x08\x1aM\n\x12PerTenantAclsEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12&\n\x05value\x18\x02 \x01(\x0b\x32\x17.littlehorse.ServerACLs:\x02\x38\x01\"$\n\x16\x44\x65letePrincipalRequest\x12\n\n\x02id\x18\x01 \x01(\t\"\x1e\n\x10PutTenantRequest\x12\n\n\x02id\x18\x01 \x01(\t*\xad\x01\n\x0b\x41\x43LResource\x12\x10\n\x0c\x41\x43L_WORKFLOW\x10\x00\x12\x0c\n\x08\x41\x43L_TASK\x10\x01\x12\x16\n\x12\x41\x43L_EXTERNAL_EVENT\x10\x02\x12\x11\n\rACL_USER_TASK\x10\x03\x12\x11\n\rACL_PRINCIPAL\x10\x04\x12\x0e\n\nACL_TENANT\x10\x05\x12\x15\n\x11\x41\x43L_ALL_RESOURCES\x10\x06\x12\x19\n\x15\x41\x43L_TASK_WORKER_GROUP\x10\x07*C\n\tACLAction\x12\x08\n\x04READ\x10\x00\x12\x07\n\x03RUN\x10\x01\x12\x12\n\x0eWRITE_METADATA\x10\x02\x12\x0f\n\x0b\x41LL_ACTIONS\x10\x03\x42G\n\x1fio.littlehorse.sdk.common.protoP\x01Z\x07.;model\xaa\x02\x18LittleHorse.Common.Protob\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'acls_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\007.;model\252\002\030LittleHorse.Common.Proto'
  _PRINCIPAL_PERTENANTACLSENTRY._options = None
  _PRINCIPAL_PERTENANTACLSENTRY._serialized_options = b'8\001'
  _PUTPRINCIPALREQUEST_PERTENANTACLSENTRY._options = None
  _PUTPRINCIPALREQUEST_PERTENANTACLSENTRY._serialized_options = b'8\001'
  _globals['_ACLRESOURCE']._serialized_start=1005
  _globals['_ACLRESOURCE']._serialized_end=1178
  _globals['_ACLACTION']._serialized_start=1180
  _globals['_ACLACTION']._serialized_end=1247
  _globals['_PRINCIPAL']._serialized_start=78
  _globals['_PRINCIPAL']._serialized_end=368
  _globals['_PRINCIPAL_PERTENANTACLSENTRY']._serialized_start=291
  _globals['_PRINCIPAL_PERTENANTACLSENTRY']._serialized_end=368
  _globals['_TENANT']._serialized_start=370
  _globals['_TENANT']._serialized_end=461
  _globals['_SERVERACLS']._serialized_start=463
  _globals['_SERVERACLS']._serialized_end=513
  _globals['_SERVERACL']._serialized_start=516
  _globals['_SERVERACL']._serialized_end=674
  _globals['_PUTPRINCIPALREQUEST']._serialized_start=677
  _globals['_PUTPRINCIPALREQUEST']._serialized_end=932
  _globals['_PUTPRINCIPALREQUEST_PERTENANTACLSENTRY']._serialized_start=291
  _globals['_PUTPRINCIPALREQUEST_PERTENANTACLSENTRY']._serialized_end=368
  _globals['_DELETEPRINCIPALREQUEST']._serialized_start=934
  _globals['_DELETEPRINCIPALREQUEST']._serialized_end=970
  _globals['_PUTTENANTREQUEST']._serialized_start=972
  _globals['_PUTTENANTREQUEST']._serialized_end=1002
# @@protoc_insertion_point(module_scope)
