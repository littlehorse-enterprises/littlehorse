# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# NO CHECKED-IN PROTOBUF GENCODE
# source: external_event.proto
# Protobuf Python Version: 5.29.3
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import runtime_version as _runtime_version
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
_runtime_version.ValidateProtobufRuntimeVersion(
    _runtime_version.Domain.PUBLIC,
    5,
    29,
    3,
    '',
    'external_event.proto'
)
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import timestamp_pb2 as google_dot_protobuf_dot_timestamp__pb2
import littlehorse.model.variable_pb2 as variable__pb2
import littlehorse.model.object_id_pb2 as object__id__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x14\x65xternal_event.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x0evariable.proto\x1a\x0fobject_id.proto\"\x93\x02\n\rExternalEvent\x12(\n\x02id\x18\x01 \x01(\x0b\x32\x1c.littlehorse.ExternalEventId\x12.\n\ncreated_at\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12+\n\x07\x63ontent\x18\x03 \x01(\x0b\x32\x1a.littlehorse.VariableValue\x12\x1e\n\x11thread_run_number\x18\x04 \x01(\x05H\x00\x88\x01\x01\x12\x1e\n\x11node_run_position\x18\x05 \x01(\x05H\x01\x88\x01\x01\x12\x0f\n\x07\x63laimed\x18\x06 \x01(\x08\x42\x14\n\x12_thread_run_numberB\x14\n\x12_node_run_position\"\xb4\x01\n\x10\x45xternalEventDef\x12+\n\x02id\x18\x01 \x01(\x0b\x32\x1f.littlehorse.ExternalEventDefId\x12.\n\ncreated_at\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x43\n\x10retention_policy\x18\x03 \x01(\x0b\x32).littlehorse.ExternalEventRetentionPolicy\"P\n\x1c\x45xternalEventRetentionPolicy\x12\x1b\n\x11seconds_after_put\x18\x01 \x01(\x03H\x00\x42\x13\n\x11\x65xt_evt_gc_policyBM\n\x1fio.littlehorse.sdk.common.protoP\x01Z\t.;lhproto\xaa\x02\x1cLittleHorse.Sdk.Common.Protob\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'external_event_pb2', _globals)
if not _descriptor._USE_C_DESCRIPTORS:
  _globals['DESCRIPTOR']._loaded_options = None
  _globals['DESCRIPTOR']._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\t.;lhproto\252\002\034LittleHorse.Sdk.Common.Proto'
  _globals['_EXTERNALEVENT']._serialized_start=104
  _globals['_EXTERNALEVENT']._serialized_end=379
  _globals['_EXTERNALEVENTDEF']._serialized_start=382
  _globals['_EXTERNALEVENTDEF']._serialized_end=562
  _globals['_EXTERNALEVENTRETENTIONPOLICY']._serialized_start=564
  _globals['_EXTERNALEVENTRETENTIONPOLICY']._serialized_end=644
# @@protoc_insertion_point(module_scope)
