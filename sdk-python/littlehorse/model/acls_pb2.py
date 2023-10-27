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




DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\nacls.proto\x12\x0blittlehorse\"P\n\tPrincipal\x12\n\n\x02id\x18\x01 \x01(\t\x12$\n\x04\x61\x63ls\x18\x02 \x03(\x0b\x32\x16.littlehorse.ServerACL\x12\x11\n\ttenant_id\x18\x03 \x01(\t\"\x14\n\x06Tenant\x12\n\n\x02id\x18\x01 \x01(\t\"\x9e\x01\n\tServerACL\x12+\n\tresources\x18\x01 \x03(\x0e\x32\x18.littlehorse.ACLResource\x12/\n\x0f\x61llowed_actions\x18\x02 \x03(\x0e\x32\x16.littlehorse.ACLAction\x12\x0e\n\x04name\x18\x03 \x01(\tH\x00\x12\x10\n\x06prefix\x18\x04 \x01(\tH\x00\x42\x11\n\x0fresource_filter\"\x80\x01\n\x13PutPrincipalRequest\x12\n\n\x02id\x18\x01 \x01(\t\x12$\n\x04\x61\x63ls\x18\x02 \x03(\x0b\x32\x16.littlehorse.ServerACL\x12\x16\n\ttenant_id\x18\x03 \x01(\tH\x00\x88\x01\x01\x12\x11\n\toverwrite\x18\x05 \x01(\x08\x42\x0c\n\n_tenant_id\"$\n\x16\x44\x65letePrincipalRequest\x12\n\n\x02id\x18\x01 \x01(\t\"\x1e\n\x10PutTenantRequest\x12\n\n\x02id\x18\x01 \x01(\t*\xa2\x01\n\x0b\x41\x43LResource\x12\x0f\n\x0b\x41\x43L_WF_SPEC\x10\x00\x12\x10\n\x0c\x41\x43L_TASK_DEF\x10\x01\x12\x1a\n\x16\x41\x43L_EXTERNAL_EVENT_DEF\x10\x02\x12\x15\n\x11\x41\x43L_USER_TASK_DEF\x10\x03\x12\x11\n\rACL_PRINCIPAL\x10\x04\x12\x0e\n\nACL_TENANT\x10\x05\x12\x1a\n\x16\x41\x43L_ALL_RESOURCE_TYPES\x10\x06*G\n\tACLAction\x12\x08\n\x04READ\x10\x00\x12\x0b\n\x07\x45XECUTE\x10\x01\x12\x12\n\x0eWRITE_METADATA\x10\x02\x12\x0f\n\x0b\x41LL_ACTIONS\x10\x03\x42(\n\x1bio.littlehorse.common.protoP\x01Z\x07.;modelb\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'acls_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\033io.littlehorse.common.protoP\001Z\007.;model'
  _globals['_ACLRESOURCE']._serialized_start=494
  _globals['_ACLRESOURCE']._serialized_end=656
  _globals['_ACLACTION']._serialized_start=658
  _globals['_ACLACTION']._serialized_end=729
  _globals['_PRINCIPAL']._serialized_start=27
  _globals['_PRINCIPAL']._serialized_end=107
  _globals['_TENANT']._serialized_start=109
  _globals['_TENANT']._serialized_end=129
  _globals['_SERVERACL']._serialized_start=132
  _globals['_SERVERACL']._serialized_end=290
  _globals['_PUTPRINCIPALREQUEST']._serialized_start=293
  _globals['_PUTPRINCIPALREQUEST']._serialized_end=421
  _globals['_DELETEPRINCIPALREQUEST']._serialized_start=423
  _globals['_DELETEPRINCIPALREQUEST']._serialized_end=459
  _globals['_PUTTENANTREQUEST']._serialized_start=461
  _globals['_PUTTENANTREQUEST']._serialized_end=491
# @@protoc_insertion_point(module_scope)
