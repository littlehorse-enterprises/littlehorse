# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: task_run.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import timestamp_pb2 as google_dot_protobuf_dot_timestamp__pb2
import littlehorse.model.common_enums_pb2 as common__enums__pb2
import littlehorse.model.common_wfspec_pb2 as common__wfspec__pb2
import littlehorse.model.variable_pb2 as variable__pb2
import littlehorse.model.object_id_pb2 as object__id__pb2
import littlehorse.model.user_tasks_pb2 as user__tasks__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x0etask_run.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x12\x63ommon_enums.proto\x1a\x13\x63ommon_wfspec.proto\x1a\x0evariable.proto\x1a\x0fobject_id.proto\x1a\x10user_tasks.proto\"\xd9\x03\n\x07TaskRun\x12\"\n\x02id\x18\x01 \x01(\x0b\x32\x16.littlehorse.TaskRunId\x12+\n\x0btask_def_id\x18\x02 \x01(\x0b\x32\x16.littlehorse.TaskDefId\x12*\n\x08\x61ttempts\x18\x03 \x03(\x0b\x32\x18.littlehorse.TaskAttempt\x12\x33\n\x0finput_variables\x18\x05 \x03(\x0b\x32\x1a.littlehorse.VarNameAndVal\x12*\n\x06source\x18\x06 \x01(\x0b\x32\x1a.littlehorse.TaskRunSource\x12\x30\n\x0cscheduled_at\x18\x07 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\'\n\x06status\x18\x08 \x01(\x0e\x32\x17.littlehorse.TaskStatus\x12\x17\n\x0ftimeout_seconds\x18\t \x01(\x05\x12\x16\n\x0etotal_attempts\x18\x04 \x01(\x05\x12L\n\x13\x65xponential_backoff\x18\n \x01(\x0b\x32*.littlehorse.ExponentialBackoffRetryPolicyH\x00\x88\x01\x01\x42\x16\n\x14_exponential_backoff\"\\\n\rVarNameAndVal\x12\x10\n\x08var_name\x18\x01 \x01(\t\x12)\n\x05value\x18\x02 \x01(\x0b\x32\x1a.littlehorse.VariableValue\x12\x0e\n\x06masked\x18\x03 \x01(\x08\"\xc6\x04\n\x0bTaskAttempt\x12\x33\n\nlog_output\x18\x02 \x01(\x0b\x32\x1a.littlehorse.VariableValueH\x01\x88\x01\x01\x12\x36\n\rschedule_time\x18\x03 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x02\x88\x01\x01\x12\x33\n\nstart_time\x18\x04 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x03\x88\x01\x01\x12\x31\n\x08\x65nd_time\x18\x05 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x04\x88\x01\x01\x12\x16\n\x0etask_worker_id\x18\x07 \x01(\t\x12 \n\x13task_worker_version\x18\x08 \x01(\tH\x05\x88\x01\x01\x12\'\n\x06status\x18\t \x01(\x0e\x32\x17.littlehorse.TaskStatus\x12,\n\x06output\x18\x01 \x01(\x0b\x32\x1a.littlehorse.VariableValueH\x00\x12)\n\x05\x65rror\x18\n \x01(\x0b\x32\x18.littlehorse.LHTaskErrorH\x00\x12\x31\n\texception\x18\x0b \x01(\x0b\x32\x1c.littlehorse.LHTaskExceptionH\x00\x12\x14\n\x0cmasked_value\x18\x0c \x01(\x08\x42\x08\n\x06resultB\r\n\x0b_log_outputB\x10\n\x0e_schedule_timeB\r\n\x0b_start_timeB\x0b\n\t_end_timeB\x16\n\x14_task_worker_version\"\xda\x01\n\rTaskRunSource\x12\x33\n\ttask_node\x18\x01 \x01(\x0b\x32\x1e.littlehorse.TaskNodeReferenceH\x00\x12\x42\n\x11user_task_trigger\x18\x02 \x01(\x0b\x32%.littlehorse.UserTaskTriggerReferenceH\x00\x12.\n\nwf_spec_id\x18\x03 \x01(\x0b\x32\x15.littlehorse.WfSpecIdH\x01\x88\x01\x01\x42\x11\n\x0ftask_run_sourceB\r\n\x0b_wf_spec_id\"@\n\x11TaskNodeReference\x12+\n\x0bnode_run_id\x18\x01 \x01(\x0b\x32\x16.littlehorse.NodeRunId\"F\n\x0bLHTaskError\x12&\n\x04type\x18\x01 \x01(\x0e\x32\x18.littlehorse.LHErrorType\x12\x0f\n\x07message\x18\x02 \x01(\t\"]\n\x0fLHTaskException\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x0f\n\x07message\x18\x02 \x01(\t\x12+\n\x07\x63ontent\x18\x03 \x01(\x0b\x32\x1a.littlehorse.VariableValueBI\n\x1fio.littlehorse.sdk.common.protoP\x01Z\t.;lhproto\xaa\x02\x18LittleHorse.Common.Protob\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'task_run_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\t.;lhproto\252\002\030LittleHorse.Common.Proto'
  _globals['_TASKRUN']._serialized_start=157
  _globals['_TASKRUN']._serialized_end=630
  _globals['_VARNAMEANDVAL']._serialized_start=632
  _globals['_VARNAMEANDVAL']._serialized_end=724
  _globals['_TASKATTEMPT']._serialized_start=727
  _globals['_TASKATTEMPT']._serialized_end=1309
  _globals['_TASKRUNSOURCE']._serialized_start=1312
  _globals['_TASKRUNSOURCE']._serialized_end=1530
  _globals['_TASKNODEREFERENCE']._serialized_start=1532
  _globals['_TASKNODEREFERENCE']._serialized_end=1596
  _globals['_LHTASKERROR']._serialized_start=1598
  _globals['_LHTASKERROR']._serialized_end=1668
  _globals['_LHTASKEXCEPTION']._serialized_start=1670
  _globals['_LHTASKEXCEPTION']._serialized_end=1763
# @@protoc_insertion_point(module_scope)
