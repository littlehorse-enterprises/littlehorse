# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: node_run.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import timestamp_pb2 as google_dot_protobuf_dot_timestamp__pb2
import littlehorse.model.common_enums_pb2 as common__enums__pb2
import littlehorse.model.variable_pb2 as variable__pb2
import littlehorse.model.object_id_pb2 as object__id__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x0enode_run.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x12\x63ommon_enums.proto\x1a\x0evariable.proto\x1a\x0fobject_id.proto\"\xed\x06\n\x07NodeRun\x12\"\n\x02id\x18\x01 \x01(\x0b\x32\x16.littlehorse.NodeRunId\x12)\n\nwf_spec_id\x18\x04 \x01(\x0b\x32\x15.littlehorse.WfSpecId\x12\x1b\n\x13\x66\x61ilure_handler_ids\x18\x05 \x03(\x05\x12%\n\x06status\x18\x06 \x01(\x0e\x32\x15.littlehorse.LHStatus\x12\x30\n\x0c\x61rrival_time\x18\x07 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x31\n\x08\x65nd_time\x18\x08 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x01\x88\x01\x01\x12\x18\n\x10thread_spec_name\x18\t \x01(\t\x12\x11\n\tnode_name\x18\n \x01(\t\x12\x1a\n\rerror_message\x18\x0b \x01(\tH\x02\x88\x01\x01\x12&\n\x08\x66\x61ilures\x18\x0c \x03(\x0b\x32\x14.littlehorse.Failure\x12(\n\x04task\x18\r \x01(\x0b\x32\x18.littlehorse.TaskNodeRunH\x00\x12\x37\n\x0e\x65xternal_event\x18\x0e \x01(\x0b\x32\x1d.littlehorse.ExternalEventRunH\x00\x12\x30\n\nentrypoint\x18\x0f \x01(\x0b\x32\x1a.littlehorse.EntrypointRunH\x00\x12$\n\x04\x65xit\x18\x10 \x01(\x0b\x32\x14.littlehorse.ExitRunH\x00\x12\x33\n\x0cstart_thread\x18\x11 \x01(\x0b\x32\x1b.littlehorse.StartThreadRunH\x00\x12\x36\n\x0cwait_threads\x18\x12 \x01(\x0b\x32\x1e.littlehorse.WaitForThreadsRunH\x00\x12*\n\x05sleep\x18\x13 \x01(\x0b\x32\x19.littlehorse.SleepNodeRunH\x00\x12\x31\n\tuser_task\x18\x14 \x01(\x0b\x32\x1c.littlehorse.UserTaskNodeRunH\x00\x12\x46\n\x16start_multiple_threads\x18\x15 \x01(\x0b\x32$.littlehorse.StartMultipleThreadsRunH\x00\x42\x0b\n\tnode_typeB\x0b\n\t_end_timeB\x10\n\x0e_error_message\"O\n\x0bTaskNodeRun\x12\x30\n\x0btask_run_id\x18\x01 \x01(\x0b\x32\x16.littlehorse.TaskRunIdH\x00\x88\x01\x01\x42\x0e\n\x0c_task_run_id\"a\n\x0fUserTaskNodeRun\x12\x39\n\x10user_task_run_id\x18\x01 \x01(\x0b\x32\x1a.littlehorse.UserTaskRunIdH\x00\x88\x01\x01\x42\x13\n\x11_user_task_run_id\"\x0f\n\rEntrypointRun\"\t\n\x07\x45xitRun\"\\\n\x0eStartThreadRun\x12\x1c\n\x0f\x63hild_thread_id\x18\x01 \x01(\x05H\x00\x88\x01\x01\x12\x18\n\x10thread_spec_name\x18\x02 \x01(\tB\x12\n\x10_child_thread_id\"M\n\x17StartMultipleThreadsRun\x12\x18\n\x10thread_spec_name\x18\x01 \x01(\t\x12\x18\n\x10\x63hild_thread_ids\x18\x02 \x03(\x05\"\xa4\x04\n\x11WaitForThreadsRun\x12=\n\x07threads\x18\x01 \x03(\x0b\x32,.littlehorse.WaitForThreadsRun.WaitForThread\x1a\xc0\x02\n\rWaitForThread\x12\x38\n\x0fthread_end_time\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x00\x88\x01\x01\x12,\n\rthread_status\x18\x02 \x01(\x0e\x32\x15.littlehorse.LHStatus\x12\x19\n\x11thread_run_number\x18\x03 \x01(\x05\x12J\n\x0ewaiting_status\x18\x04 \x01(\x0e\x32\x32.littlehorse.WaitForThreadsRun.WaitingThreadStatus\x12*\n\x1d\x66\x61ilure_handler_thread_run_id\x18\x05 \x01(\x05H\x01\x88\x01\x01\x42\x12\n\x10_thread_end_timeB \n\x1e_failure_handler_thread_run_id\"\x8c\x01\n\x13WaitingThreadStatus\x12\x16\n\x12THREAD_IN_PROGRESS\x10\x00\x12\x1b\n\x17THREAD_HANDLING_FAILURE\x10\x01\x12\'\n#THREAD_COMPLETED_OR_FAILURE_HANDLED\x10\x02\x12\x17\n\x13THREAD_UNSUCCESSFUL\x10\x03\"\xfd\x01\n\x10\x45xternalEventRun\x12>\n\x15\x65xternal_event_def_id\x18\x01 \x01(\x0b\x32\x1f.littlehorse.ExternalEventDefId\x12\x33\n\nevent_time\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x00\x88\x01\x01\x12<\n\x11\x65xternal_event_id\x18\x03 \x01(\x0b\x32\x1c.littlehorse.ExternalEventIdH\x01\x88\x01\x01\x12\x11\n\ttimed_out\x18\x04 \x01(\x08\x42\r\n\x0b_event_timeB\x14\n\x12_external_event_id\"T\n\x0cSleepNodeRun\x12\x33\n\x0fmaturation_time\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x0f\n\x07matured\x18\x02 \x01(\x08\"\xd8\x01\n\x07\x46\x61ilure\x12\x14\n\x0c\x66\x61ilure_name\x18\x01 \x01(\t\x12\x0f\n\x07message\x18\x02 \x01(\t\x12\x30\n\x07\x63ontent\x18\x03 \x01(\x0b\x32\x1a.littlehorse.VariableValueH\x00\x88\x01\x01\x12\x1c\n\x14was_properly_handled\x18\x04 \x01(\x08\x12)\n\x1c\x66\x61ilure_handler_threadrun_id\x18\x05 \x01(\x05H\x01\x88\x01\x01\x42\n\n\x08_contentB\x1f\n\x1d_failure_handler_threadrun_idBG\n\x1fio.littlehorse.sdk.common.protoP\x01Z\x07.;model\xaa\x02\x18LittleHorse.Common.Protob\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'node_run_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\007.;model\252\002\030LittleHorse.Common.Proto'
  _globals['_NODERUN']._serialized_start=118
  _globals['_NODERUN']._serialized_end=995
  _globals['_TASKNODERUN']._serialized_start=997
  _globals['_TASKNODERUN']._serialized_end=1076
  _globals['_USERTASKNODERUN']._serialized_start=1078
  _globals['_USERTASKNODERUN']._serialized_end=1175
  _globals['_ENTRYPOINTRUN']._serialized_start=1177
  _globals['_ENTRYPOINTRUN']._serialized_end=1192
  _globals['_EXITRUN']._serialized_start=1194
  _globals['_EXITRUN']._serialized_end=1203
  _globals['_STARTTHREADRUN']._serialized_start=1205
  _globals['_STARTTHREADRUN']._serialized_end=1297
  _globals['_STARTMULTIPLETHREADSRUN']._serialized_start=1299
  _globals['_STARTMULTIPLETHREADSRUN']._serialized_end=1376
  _globals['_WAITFORTHREADSRUN']._serialized_start=1379
  _globals['_WAITFORTHREADSRUN']._serialized_end=1927
  _globals['_WAITFORTHREADSRUN_WAITFORTHREAD']._serialized_start=1464
  _globals['_WAITFORTHREADSRUN_WAITFORTHREAD']._serialized_end=1784
  _globals['_WAITFORTHREADSRUN_WAITINGTHREADSTATUS']._serialized_start=1787
  _globals['_WAITFORTHREADSRUN_WAITINGTHREADSTATUS']._serialized_end=1927
  _globals['_EXTERNALEVENTRUN']._serialized_start=1930
  _globals['_EXTERNALEVENTRUN']._serialized_end=2183
  _globals['_SLEEPNODERUN']._serialized_start=2185
  _globals['_SLEEPNODERUN']._serialized_end=2269
  _globals['_FAILURE']._serialized_start=2272
  _globals['_FAILURE']._serialized_end=2488
# @@protoc_insertion_point(module_scope)
