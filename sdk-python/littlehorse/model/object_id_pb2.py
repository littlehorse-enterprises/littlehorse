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


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x0fobject_id.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x12\x63ommon_enums.proto\"A\n\x08WfSpecId\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x15\n\rmajor_version\x18\x02 \x01(\x05\x12\x10\n\x08revision\x18\x03 \x01(\x05\"\x19\n\tTaskDefId\x12\x0c\n\x04name\x18\x01 \x01(\t\",\n\x0bStructDefId\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x0f\n\x07version\x18\x02 \x01(\x05\"\"\n\x12\x45xternalEventDefId\x12\x0c\n\x04name\x18\x01 \x01(\t\".\n\rUserTaskDefId\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x0f\n\x07version\x18\x02 \x01(\x05\"\"\n\x12WorkflowEventDefId\x12\x0c\n\x04name\x18\x01 \x01(\t\"@\n\x11TaskWorkerGroupId\x12+\n\x0btask_def_id\x18\x01 \x01(\x0b\x32\x16.littlehorse.TaskDefId\"^\n\nVariableId\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12\x19\n\x11thread_run_number\x18\x02 \x01(\x05\x12\x0c\n\x04name\x18\x03 \x01(\t\"\x88\x01\n\x0f\x45xternalEventId\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12>\n\x15\x65xternal_event_def_id\x18\x02 \x01(\x0b\x32\x1f.littlehorse.ExternalEventDefId\x12\x0c\n\x04guid\x18\x03 \x01(\t\"`\n\x11\x43orrelatedEventId\x12\x0b\n\x03key\x18\x01 \x01(\t\x12>\n\x15\x65xternal_event_def_id\x18\x02 \x01(\x0b\x32\x1f.littlehorse.ExternalEventDefId\"_\n\x07WfRunId\x12\n\n\x02id\x18\x01 \x01(\t\x12\x33\n\x10parent_wf_run_id\x18\x02 \x01(\x0b\x32\x14.littlehorse.WfRunIdH\x00\x88\x01\x01\x42\x13\n\x11_parent_wf_run_id\"a\n\tNodeRunId\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12\x19\n\x11thread_run_number\x18\x02 \x01(\x05\x12\x10\n\x08position\x18\x03 \x01(\x05\"\x8a\x01\n\x0fWorkflowEventId\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12>\n\x15workflow_event_def_id\x18\x02 \x01(\x0b\x32\x1f.littlehorse.WorkflowEventDefId\x12\x0e\n\x06number\x18\x03 \x01(\x05\"G\n\tTaskRunId\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12\x11\n\ttask_guid\x18\x02 \x01(\t\"P\n\rUserTaskRunId\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12\x16\n\x0euser_task_guid\x18\x02 \x01(\t\"\xa8\x01\n\x10TaskDefMetricsId\x12\x30\n\x0cwindow_start\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x35\n\x0bwindow_type\x18\x02 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12+\n\x0btask_def_id\x18\x03 \x01(\x0b\x32\x16.littlehorse.TaskDefId\"\xa5\x01\n\x0fWfSpecMetricsId\x12\x30\n\x0cwindow_start\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x35\n\x0bwindow_type\x18\x02 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12)\n\nwf_spec_id\x18\x03 \x01(\x0b\x32\x15.littlehorse.WfSpecId\"\x19\n\x0bPrincipalId\x12\n\n\x02id\x18\x01 \x01(\t\"\x16\n\x08TenantId\x12\n\n\x02id\x18\x01 \x01(\t\"\x1e\n\x10ScheduledWfRunId\x12\n\n\x02id\x18\x01 \x01(\tBM\n\x1fio.littlehorse.sdk.common.protoP\x01Z\t.;lhproto\xaa\x02\x1cLittleHorse.Sdk.Common.Protob\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'object_id_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\t.;lhproto\252\002\034LittleHorse.Sdk.Common.Proto'
  _globals['_WFSPECID']._serialized_start=85
  _globals['_WFSPECID']._serialized_end=150
  _globals['_TASKDEFID']._serialized_start=152
  _globals['_TASKDEFID']._serialized_end=177
  _globals['_STRUCTDEFID']._serialized_start=179
  _globals['_STRUCTDEFID']._serialized_end=223
  _globals['_EXTERNALEVENTDEFID']._serialized_start=225
  _globals['_EXTERNALEVENTDEFID']._serialized_end=259
  _globals['_USERTASKDEFID']._serialized_start=261
  _globals['_USERTASKDEFID']._serialized_end=307
  _globals['_WORKFLOWEVENTDEFID']._serialized_start=309
  _globals['_WORKFLOWEVENTDEFID']._serialized_end=343
  _globals['_TASKWORKERGROUPID']._serialized_start=345
  _globals['_TASKWORKERGROUPID']._serialized_end=409
  _globals['_VARIABLEID']._serialized_start=411
  _globals['_VARIABLEID']._serialized_end=505
  _globals['_EXTERNALEVENTID']._serialized_start=508
  _globals['_EXTERNALEVENTID']._serialized_end=644
  _globals['_CORRELATEDEVENTID']._serialized_start=646
  _globals['_CORRELATEDEVENTID']._serialized_end=742
  _globals['_WFRUNID']._serialized_start=744
  _globals['_WFRUNID']._serialized_end=839
  _globals['_NODERUNID']._serialized_start=841
  _globals['_NODERUNID']._serialized_end=938
  _globals['_WORKFLOWEVENTID']._serialized_start=941
  _globals['_WORKFLOWEVENTID']._serialized_end=1079
  _globals['_TASKRUNID']._serialized_start=1081
  _globals['_TASKRUNID']._serialized_end=1152
  _globals['_USERTASKRUNID']._serialized_start=1154
  _globals['_USERTASKRUNID']._serialized_end=1234
  _globals['_TASKDEFMETRICSID']._serialized_start=1237
  _globals['_TASKDEFMETRICSID']._serialized_end=1405
  _globals['_WFSPECMETRICSID']._serialized_start=1408
  _globals['_WFSPECMETRICSID']._serialized_end=1573
  _globals['_PRINCIPALID']._serialized_start=1575
  _globals['_PRINCIPALID']._serialized_end=1600
  _globals['_TENANTID']._serialized_start=1602
  _globals['_TENANTID']._serialized_end=1624
  _globals['_SCHEDULEDWFRUNID']._serialized_start=1626
  _globals['_SCHEDULEDWFRUNID']._serialized_end=1656
# @@protoc_insertion_point(module_scope)
