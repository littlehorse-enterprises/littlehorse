# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: metrics.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import timestamp_pb2 as google_dot_protobuf_dot_timestamp__pb2
from google.protobuf import duration_pb2 as google_dot_protobuf_dot_duration__pb2
import littlehorse.model.object_id_pb2 as object__id__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\rmetrics.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x1egoogle/protobuf/duration.proto\x1a\x0fobject_id.proto\"\x8d\x01\n\x06Metric\x12!\n\x02id\x18\x01 \x01(\x0b\x32\x15.littlehorse.MetricId\x12.\n\ncreated_at\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x30\n\rwindow_length\x18\x03 \x01(\x0b\x32\x19.google.protobuf.Duration\"\xdd\x01\n\x0fPartitionMetric\x12*\n\x02id\x18\x01 \x01(\x0b\x32\x1e.littlehorse.PartitionMetricId\x12.\n\ncreated_at\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12<\n\x0e\x61\x63tive_windows\x18\x03 \x03(\x0b\x32$.littlehorse.PartitionWindowedMetric\x12\x30\n\rwindow_length\x18\x04 \x01(\x0b\x32\x19.google.protobuf.Duration\"Z\n\x17PartitionWindowedMetric\x12\r\n\x05value\x18\x01 \x01(\x01\x12\x30\n\x0cwindow_start\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\"`\n\x11PartitionMetricId\x12!\n\x02id\x18\x01 \x01(\x0b\x32\x15.littlehorse.MetricId\x12(\n\ttenant_id\x18\x02 \x01(\x0b\x32\x15.littlehorse.TenantId\"p\n\tMetricRun\x12$\n\x02id\x18\x01 \x01(\x0b\x32\x18.littlehorse.MetricRunId\x12\r\n\x05value\x18\x02 \x01(\x01\x12.\n\ncreated_at\x18\x04 \x01(\x0b\x32\x1a.google.protobuf.TimestampBM\n\x1fio.littlehorse.sdk.common.protoP\x01Z\t.;lhproto\xaa\x02\x1cLittleHorse.Sdk.Common.Protob\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'metrics_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\t.;lhproto\252\002\034LittleHorse.Sdk.Common.Proto'
  _globals['_METRIC']._serialized_start=113
  _globals['_METRIC']._serialized_end=254
  _globals['_PARTITIONMETRIC']._serialized_start=257
  _globals['_PARTITIONMETRIC']._serialized_end=478
  _globals['_PARTITIONWINDOWEDMETRIC']._serialized_start=480
  _globals['_PARTITIONWINDOWEDMETRIC']._serialized_end=570
  _globals['_PARTITIONMETRICID']._serialized_start=572
  _globals['_PARTITIONMETRICID']._serialized_end=668
  _globals['_METRICRUN']._serialized_start=670
  _globals['_METRICRUN']._serialized_end=782
# @@protoc_insertion_point(module_scope)
