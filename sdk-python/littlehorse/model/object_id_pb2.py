# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: object_id.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import timestamp_pb2 as google_dot_protobuf_dot_timestamp__pb2
import littlehorse.model.common_enums_pb2 as common__enums__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x0fobject_id.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x12\x63ommon_enums.proto\"A\n\x08WfSpecId\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x15\n\rmajor_version\x18\x02 \x01(\x05\x12\x10\n\x08revision\x18\x03 \x01(\x05\"\x19\n\tTaskDefId\x12\x0c\n\x04name\x18\x01 \x01(\t\"\"\n\x12\x45xternalEventDefId\x12\x0c\n\x04name\x18\x01 \x01(\t\".\n\rUserTaskDefId\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x0f\n\x07version\x18\x02 \x01(\x05\"@\n\x11TaskWorkerGroupId\x12+\n\x0btask_def_id\x18\x01 \x01(\x0b\x32\x16.littlehorse.TaskDefId\"^\n\nVariableId\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12\x19\n\x11thread_run_number\x18\x02 \x01(\x05\x12\x0c\n\x04name\x18\x03 \x01(\t\"\x88\x01\n\x0f\x45xternalEventId\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12>\n\x15\x65xternal_event_def_id\x18\x02 \x01(\x0b\x32\x1f.littlehorse.ExternalEventDefId\x12\x0c\n\x04guid\x18\x03 \x01(\t\"\x15\n\x07WfRunId\x12\n\n\x02id\x18\x01 \x01(\t\"a\n\tNodeRunId\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12\x19\n\x11thread_run_number\x18\x02 \x01(\x05\x12\x10\n\x08position\x18\x03 \x01(\x05\"G\n\tTaskRunId\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12\x11\n\ttask_guid\x18\x02 \x01(\t\"P\n\rUserTaskRunId\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12\x16\n\x0euser_task_guid\x18\x02 \x01(\t\"\xa8\x01\n\x10TaskDefMetricsId\x12\x30\n\x0cwindow_start\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x35\n\x0bwindow_type\x18\x02 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12+\n\x0btask_def_id\x18\x03 \x01(\x0b\x32\x16.littlehorse.TaskDefId\"\xa5\x01\n\x0fWfSpecMetricsId\x12\x30\n\x0cwindow_start\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x35\n\x0bwindow_type\x18\x02 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12)\n\nwf_spec_id\x18\x03 \x01(\x0b\x32\x15.littlehorse.WfSpecId\"\x19\n\x0bPrincipalId\x12\n\n\x02id\x18\x01 \x01(\t\"\x16\n\x08TenantId\x12\n\n\x02id\x18\x01 \x01(\t\"^\n\x12\x41ggregatedMetricId\x12+\n\nwf_spec_id\x18\x01 \x01(\x0b\x32\x15.littlehorse.WfSpecIdH\x00\x12\x15\n\x0bspecific_id\x18\x02 \x01(\tH\x00\x42\x04\n\x02idBG\n\x1fio.littlehorse.sdk.common.protoP\x01Z\x07.;model\xaa\x02\x18LittleHorse.Common.Protob\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'object_id_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\007.;model\252\002\030LittleHorse.Common.Proto'
  _globals['_WFSPECID']._serialized_start=85
  _globals['_WFSPECID']._serialized_end=150
  _globals['_TASKDEFID']._serialized_start=152
  _globals['_TASKDEFID']._serialized_end=177
  _globals['_EXTERNALEVENTDEFID']._serialized_start=179
  _globals['_EXTERNALEVENTDEFID']._serialized_end=213
  _globals['_USERTASKDEFID']._serialized_start=215
  _globals['_USERTASKDEFID']._serialized_end=261
  _globals['_TASKWORKERGROUPID']._serialized_start=263
  _globals['_TASKWORKERGROUPID']._serialized_end=327
  _globals['_VARIABLEID']._serialized_start=329
  _globals['_VARIABLEID']._serialized_end=423
  _globals['_EXTERNALEVENTID']._serialized_start=426
  _globals['_EXTERNALEVENTID']._serialized_end=562
  _globals['_WFRUNID']._serialized_start=564
  _globals['_WFRUNID']._serialized_end=585
  _globals['_NODERUNID']._serialized_start=587
  _globals['_NODERUNID']._serialized_end=684
  _globals['_TASKRUNID']._serialized_start=686
  _globals['_TASKRUNID']._serialized_end=757
  _globals['_USERTASKRUNID']._serialized_start=759
  _globals['_USERTASKRUNID']._serialized_end=839
  _globals['_TASKDEFMETRICSID']._serialized_start=842
  _globals['_TASKDEFMETRICSID']._serialized_end=1010
  _globals['_WFSPECMETRICSID']._serialized_start=1013
  _globals['_WFSPECMETRICSID']._serialized_end=1178
  _globals['_PRINCIPALID']._serialized_start=1180
  _globals['_PRINCIPALID']._serialized_end=1205
  _globals['_TENANTID']._serialized_start=1207
  _globals['_TENANTID']._serialized_end=1229
  _globals['_AGGREGATEDMETRICID']._serialized_start=1231
  _globals['_AGGREGATEDMETRICID']._serialized_end=1325
# @@protoc_insertion_point(module_scope)
