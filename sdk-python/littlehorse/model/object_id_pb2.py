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


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x0fobject_id.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x12\x63ommon_enums.proto\")\n\x08WfSpecId\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x0f\n\x07version\x18\x02 \x01(\x05\"\x19\n\tTaskDefId\x12\x0c\n\x04name\x18\x01 \x01(\t\"\"\n\x12\x45xternalEventDefId\x12\x0c\n\x04name\x18\x01 \x01(\t\"&\n\x16GetLatestWfSpecRequest\x12\x0c\n\x04name\x18\x01 \x01(\t\".\n\rUserTaskDefId\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x0f\n\x07version\x18\x02 \x01(\x05\"*\n\x11TaskWorkerGroupId\x12\x15\n\rtask_def_name\x18\x01 \x01(\t\"H\n\nVariableId\x12\x11\n\twf_run_id\x18\x01 \x01(\t\x12\x19\n\x11thread_run_number\x18\x02 \x01(\x05\x12\x0c\n\x04name\x18\x03 \x01(\t\"S\n\x0f\x45xternalEventId\x12\x11\n\twf_run_id\x18\x01 \x01(\t\x12\x1f\n\x17\x65xternal_event_def_name\x18\x02 \x01(\t\x12\x0c\n\x04guid\x18\x03 \x01(\t\"\x15\n\x07WfRunId\x12\n\n\x02id\x18\x01 \x01(\t\"K\n\tNodeRunId\x12\x11\n\twf_run_id\x18\x01 \x01(\t\x12\x19\n\x11thread_run_number\x18\x02 \x01(\x05\x12\x10\n\x08position\x18\x03 \x01(\x05\"1\n\tTaskRunId\x12\x11\n\twf_run_id\x18\x01 \x01(\t\x12\x11\n\ttask_guid\x18\x02 \x01(\t\":\n\rUserTaskRunId\x12\x11\n\twf_run_id\x18\x01 \x01(\t\x12\x16\n\x0euser_task_guid\x18\x02 \x01(\t\"\x92\x01\n\x10TaskDefMetricsId\x12\x30\n\x0cwindow_start\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x35\n\x0bwindow_type\x18\x02 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12\x15\n\rtask_def_name\x18\x03 \x01(\t\"\xa9\x01\n\x0fWfSpecMetricsId\x12\x30\n\x0cwindow_start\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x35\n\x0bwindow_type\x18\x02 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12\x14\n\x0cwf_spec_name\x18\x03 \x01(\t\x12\x17\n\x0fwf_spec_version\x18\x04 \x01(\x05\"\x19\n\x0bPrincipalId\x12\n\n\x02id\x18\x01 \x01(\tB,\n\x1fio.littlehorse.sdk.common.protoP\x01Z\x07.;modelb\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'object_id_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\007.;model'
  _globals['_WFSPECID']._serialized_start=85
  _globals['_WFSPECID']._serialized_end=126
  _globals['_TASKDEFID']._serialized_start=128
  _globals['_TASKDEFID']._serialized_end=153
  _globals['_EXTERNALEVENTDEFID']._serialized_start=155
  _globals['_EXTERNALEVENTDEFID']._serialized_end=189
  _globals['_GETLATESTWFSPECREQUEST']._serialized_start=191
  _globals['_GETLATESTWFSPECREQUEST']._serialized_end=229
  _globals['_USERTASKDEFID']._serialized_start=231
  _globals['_USERTASKDEFID']._serialized_end=277
  _globals['_TASKWORKERGROUPID']._serialized_start=279
  _globals['_TASKWORKERGROUPID']._serialized_end=321
  _globals['_VARIABLEID']._serialized_start=323
  _globals['_VARIABLEID']._serialized_end=395
  _globals['_EXTERNALEVENTID']._serialized_start=397
  _globals['_EXTERNALEVENTID']._serialized_end=480
  _globals['_WFRUNID']._serialized_start=482
  _globals['_WFRUNID']._serialized_end=503
  _globals['_NODERUNID']._serialized_start=505
  _globals['_NODERUNID']._serialized_end=580
  _globals['_TASKRUNID']._serialized_start=582
  _globals['_TASKRUNID']._serialized_end=631
  _globals['_USERTASKRUNID']._serialized_start=633
  _globals['_USERTASKRUNID']._serialized_end=691
  _globals['_TASKDEFMETRICSID']._serialized_start=694
  _globals['_TASKDEFMETRICSID']._serialized_end=840
  _globals['_WFSPECMETRICSID']._serialized_start=843
  _globals['_WFSPECMETRICSID']._serialized_end=1012
  _globals['_PRINCIPALID']._serialized_start=1014
  _globals['_PRINCIPALID']._serialized_end=1039
# @@protoc_insertion_point(module_scope)
