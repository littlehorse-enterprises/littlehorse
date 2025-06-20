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


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\nacls.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x0fobject_id.proto\"\xa2\x02\n\tPrincipal\x12$\n\x02id\x18\x01 \x01(\x0b\x32\x18.littlehorse.PrincipalId\x12.\n\ncreated_at\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x42\n\x0fper_tenant_acls\x18\x03 \x03(\x0b\x32).littlehorse.Principal.PerTenantAclsEntry\x12,\n\x0bglobal_acls\x18\x04 \x01(\x0b\x32\x17.littlehorse.ServerACLs\x1aM\n\x12PerTenantAclsEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12&\n\x05value\x18\x02 \x01(\x0b\x32\x17.littlehorse.ServerACLs:\x02\x38\x01\"\xb5\x01\n\x06Tenant\x12!\n\x02id\x18\x01 \x01(\x0b\x32\x15.littlehorse.TenantId\x12.\n\ncreated_at\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12@\n\x13output_topic_config\x18\x03 \x01(\x0b\x32\x1e.littlehorse.OutputTopicConfigH\x00\x88\x01\x01\x42\x16\n\x14_output_topic_config\"2\n\nServerACLs\x12$\n\x04\x61\x63ls\x18\x01 \x03(\x0b\x32\x16.littlehorse.ServerACL\"\x9e\x01\n\tServerACL\x12+\n\tresources\x18\x01 \x03(\x0e\x32\x18.littlehorse.ACLResource\x12/\n\x0f\x61llowed_actions\x18\x02 \x03(\x0e\x32\x16.littlehorse.ACLAction\x12\x0e\n\x04name\x18\x03 \x01(\tH\x00\x12\x10\n\x06prefix\x18\x04 \x01(\tH\x00\x42\x11\n\x0fresource_filter\"\xff\x01\n\x13PutPrincipalRequest\x12\n\n\x02id\x18\x01 \x01(\t\x12L\n\x0fper_tenant_acls\x18\x02 \x03(\x0b\x32\x33.littlehorse.PutPrincipalRequest.PerTenantAclsEntry\x12,\n\x0bglobal_acls\x18\x03 \x01(\x0b\x32\x17.littlehorse.ServerACLs\x12\x11\n\toverwrite\x18\x05 \x01(\x08\x1aM\n\x12PerTenantAclsEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12&\n\x05value\x18\x02 \x01(\x0b\x32\x17.littlehorse.ServerACLs:\x02\x38\x01\">\n\x16\x44\x65letePrincipalRequest\x12$\n\x02id\x18\x01 \x01(\x0b\x32\x18.littlehorse.PrincipalId\"\xb8\x01\n\x11OutputTopicConfig\x12Y\n\x17\x64\x65\x66\x61ult_recording_level\x18\x01 \x01(\x0e\x32\x38.littlehorse.OutputTopicConfig.OutputTopicRecordingLevel\"H\n\x19OutputTopicRecordingLevel\x12\x15\n\x11\x41LL_ENTITY_EVENTS\x10\x00\x12\x14\n\x10NO_ENTITY_EVENTS\x10\x01\"x\n\x10PutTenantRequest\x12\n\n\x02id\x18\x01 \x01(\t\x12@\n\x13output_topic_config\x18\x02 \x01(\x0b\x32\x1e.littlehorse.OutputTopicConfigH\x00\x88\x01\x01\x42\x16\n\x14_output_topic_config*\xd5\x01\n\x0b\x41\x43LResource\x12\x10\n\x0c\x41\x43L_WORKFLOW\x10\x00\x12\x0c\n\x08\x41\x43L_TASK\x10\x01\x12\x16\n\x12\x41\x43L_EXTERNAL_EVENT\x10\x02\x12\x11\n\rACL_USER_TASK\x10\x03\x12\x11\n\rACL_PRINCIPAL\x10\x04\x12\x0e\n\nACL_TENANT\x10\x05\x12\x15\n\x11\x41\x43L_ALL_RESOURCES\x10\x06\x12\x19\n\x15\x41\x43L_TASK_WORKER_GROUP\x10\x07\x12\x16\n\x12\x41\x43L_WORKFLOW_EVENT\x10\x08\x12\x0e\n\nACL_STRUCT\x10\t*C\n\tACLAction\x12\x08\n\x04READ\x10\x00\x12\x07\n\x03RUN\x10\x01\x12\x12\n\x0eWRITE_METADATA\x10\x02\x12\x0f\n\x0b\x41LL_ACTIONS\x10\x03\x42M\n\x1fio.littlehorse.sdk.common.protoP\x01Z\t.;lhproto\xaa\x02\x1cLittleHorse.Sdk.Common.Protob\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'acls_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\t.;lhproto\252\002\034LittleHorse.Sdk.Common.Proto'
  _PRINCIPAL_PERTENANTACLSENTRY._options = None
  _PRINCIPAL_PERTENANTACLSENTRY._serialized_options = b'8\001'
  _PUTPRINCIPALREQUEST_PERTENANTACLSENTRY._options = None
  _PUTPRINCIPALREQUEST_PERTENANTACLSENTRY._serialized_options = b'8\001'
  _globals['_ACLRESOURCE']._serialized_start=1399
  _globals['_ACLRESOURCE']._serialized_end=1612
  _globals['_ACLACTION']._serialized_start=1614
  _globals['_ACLACTION']._serialized_end=1681
  _globals['_PRINCIPAL']._serialized_start=78
  _globals['_PRINCIPAL']._serialized_end=368
  _globals['_PRINCIPAL_PERTENANTACLSENTRY']._serialized_start=291
  _globals['_PRINCIPAL_PERTENANTACLSENTRY']._serialized_end=368
  _globals['_TENANT']._serialized_start=371
  _globals['_TENANT']._serialized_end=552
  _globals['_SERVERACLS']._serialized_start=554
  _globals['_SERVERACLS']._serialized_end=604
  _globals['_SERVERACL']._serialized_start=607
  _globals['_SERVERACL']._serialized_end=765
  _globals['_PUTPRINCIPALREQUEST']._serialized_start=768
  _globals['_PUTPRINCIPALREQUEST']._serialized_end=1023
  _globals['_PUTPRINCIPALREQUEST_PERTENANTACLSENTRY']._serialized_start=291
  _globals['_PUTPRINCIPALREQUEST_PERTENANTACLSENTRY']._serialized_end=368
  _globals['_DELETEPRINCIPALREQUEST']._serialized_start=1025
  _globals['_DELETEPRINCIPALREQUEST']._serialized_end=1087
  _globals['_OUTPUTTOPICCONFIG']._serialized_start=1090
  _globals['_OUTPUTTOPICCONFIG']._serialized_end=1274
  _globals['_OUTPUTTOPICCONFIG_OUTPUTTOPICRECORDINGLEVEL']._serialized_start=1202
  _globals['_OUTPUTTOPICCONFIG_OUTPUTTOPICRECORDINGLEVEL']._serialized_end=1274
  _globals['_PUTTENANTREQUEST']._serialized_start=1276
  _globals['_PUTTENANTREQUEST']._serialized_end=1396
# @@protoc_insertion_point(module_scope)
