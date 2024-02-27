# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: workflow_event.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import timestamp_pb2 as google_dot_protobuf_dot_timestamp__pb2
import littlehorse.model.variable_pb2 as variable__pb2
import littlehorse.model.object_id_pb2 as object__id__pb2
import littlehorse.model.common_enums_pb2 as common__enums__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x14workflow_event.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x0evariable.proto\x1a\x0fobject_id.proto\x1a\x12\x63ommon_enums.proto\"\x96\x01\n\rWorkflowEvent\x12(\n\x02id\x18\x01 \x01(\x0b\x32\x1c.littlehorse.WorkflowEventId\x12+\n\x07\x63ontent\x18\x02 \x01(\x0b\x32\x1a.littlehorse.VariableValue\x12.\n\ncreated_at\x18\x03 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\"\x98\x01\n\x10WorkflowEventDef\x12+\n\x02id\x18\x01 \x01(\x0b\x32\x1f.littlehorse.WorkflowEventDefId\x12.\n\ncreated_at\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\'\n\x04type\x18\x03 \x01(\x0e\x32\x19.littlehorse.VariableType\"r\n\x1aPutWorkflowEventDefRequest\x12+\n\x02id\x18\x01 \x01(\x0b\x32\x1f.littlehorse.WorkflowEventDefId\x12\'\n\x04type\x18\x02 \x01(\x0e\x32\x19.littlehorse.VariableTypeBG\n\x1fio.littlehorse.sdk.common.protoP\x01Z\x07.;model\xaa\x02\x18LittleHorse.Common.Protob\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'workflow_event_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\007.;model\252\002\030LittleHorse.Common.Proto'
  _globals['_WORKFLOWEVENT']._serialized_start=124
  _globals['_WORKFLOWEVENT']._serialized_end=274
  _globals['_WORKFLOWEVENTDEF']._serialized_start=277
  _globals['_WORKFLOWEVENTDEF']._serialized_end=429
  _globals['_PUTWORKFLOWEVENTDEFREQUEST']._serialized_start=431
  _globals['_PUTWORKFLOWEVENTDEFREQUEST']._serialized_end=545
# @@protoc_insertion_point(module_scope)
