# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: wf_run.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import timestamp_pb2 as google_dot_protobuf_dot_timestamp__pb2
import littlehorse.model.common_enums_pb2 as common__enums__pb2
import littlehorse.model.object_id_pb2 as object__id__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x0cwf_run.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x12\x63ommon_enums.proto\x1a\x0fobject_id.proto\"\xe9\x03\n\x05WfRun\x12 \n\x02id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12)\n\nwf_spec_id\x18\x02 \x01(\x0b\x32\x15.littlehorse.WfSpecId\x12\x33\n\x14old_wf_spec_versions\x18\x03 \x03(\x0b\x32\x15.littlehorse.WfSpecId\x12%\n\x06status\x18\x04 \x01(\x0e\x32\x15.littlehorse.LHStatus\x12!\n\x19greatest_threadrun_number\x18\x05 \x01(\x05\x12.\n\nstart_time\x18\x06 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x31\n\x08\x65nd_time\x18\x07 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x00\x88\x01\x01\x12+\n\x0bthread_runs\x18\x08 \x03(\x0b\x32\x16.littlehorse.ThreadRun\x12\x39\n\x12pending_interrupts\x18\t \x03(\x0b\x32\x1d.littlehorse.PendingInterrupt\x12<\n\x10pending_failures\x18\n \x03(\x0b\x32\".littlehorse.PendingFailureHandlerB\x0b\n\t_end_time\"\xc9\x05\n\tThreadRun\x12)\n\nwf_spec_id\x18\x01 \x01(\x0b\x32\x15.littlehorse.WfSpecId\x12\x0e\n\x06number\x18\x02 \x01(\x05\x12%\n\x06status\x18\x03 \x01(\x0e\x32\x15.littlehorse.LHStatus\x12\x18\n\x10thread_spec_name\x18\x04 \x01(\t\x12.\n\nstart_time\x18\x05 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x31\n\x08\x65nd_time\x18\x06 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x00\x88\x01\x01\x12\x1a\n\rerror_message\x18\x07 \x01(\tH\x01\x88\x01\x01\x12\x18\n\x10\x63hild_thread_ids\x18\x08 \x03(\x05\x12\x1d\n\x10parent_thread_id\x18\t \x01(\x05H\x02\x88\x01\x01\x12\x33\n\x0chalt_reasons\x18\n \x03(\x0b\x32\x1d.littlehorse.ThreadHaltReason\x12?\n\x14interrupt_trigger_id\x18\x0b \x01(\x0b\x32\x1c.littlehorse.ExternalEventIdH\x03\x88\x01\x01\x12\x44\n\x15\x66\x61ilure_being_handled\x18\x0c \x01(\x0b\x32 .littlehorse.FailureBeingHandledH\x04\x88\x01\x01\x12\x1d\n\x15\x63urrent_node_position\x18\r \x01(\x05\x12\x1f\n\x17handled_failed_children\x18\x0e \x03(\x05\x12%\n\x04type\x18\x0f \x01(\x0e\x32\x17.littlehorse.ThreadTypeB\x0b\n\t_end_timeB\x10\n\x0e_error_messageB\x13\n\x11_parent_thread_idB\x17\n\x15_interrupt_trigger_idB\x18\n\x16_failure_being_handled\"c\n\x13\x46\x61ilureBeingHandled\x12\x19\n\x11thread_run_number\x18\x01 \x01(\x05\x12\x19\n\x11node_run_position\x18\x02 \x01(\x05\x12\x16\n\x0e\x66\x61ilure_number\x18\x03 \x01(\x05\"\x85\x01\n\x10PendingInterrupt\x12\x37\n\x11\x65xternal_event_id\x18\x01 \x01(\x0b\x32\x1c.littlehorse.ExternalEventId\x12\x19\n\x11handler_spec_name\x18\x02 \x01(\t\x12\x1d\n\x15interrupted_thread_id\x18\x03 \x01(\x05\"M\n\x15PendingFailureHandler\x12\x19\n\x11\x66\x61iled_thread_run\x18\x01 \x01(\x05\x12\x19\n\x11handler_spec_name\x18\x02 \x01(\t\"U\n\x1aPendingInterruptHaltReason\x12\x37\n\x11\x65xternal_event_id\x18\x01 \x01(\x0b\x32\x1c.littlehorse.ExternalEventId\"<\n\x1fPendingFailureHandlerHaltReason\x12\x19\n\x11node_run_position\x18\x01 \x01(\x05\"6\n\x19HandlingFailureHaltReason\x12\x19\n\x11handler_thread_id\x18\x01 \x01(\x05\"(\n\x0cParentHalted\x12\x18\n\x10parent_thread_id\x18\x01 \x01(\x05\"*\n\x0bInterrupted\x12\x1b\n\x13interrupt_thread_id\x18\x01 \x01(\x05\"&\n\nManualHalt\x12\x18\n\x0fmeaning_of_life\x18\x89\x01 \x01(\x08\"\x84\x03\n\x10ThreadHaltReason\x12\x32\n\rparent_halted\x18\x01 \x01(\x0b\x32\x19.littlehorse.ParentHaltedH\x00\x12/\n\x0binterrupted\x18\x02 \x01(\x0b\x32\x18.littlehorse.InterruptedH\x00\x12\x44\n\x11pending_interrupt\x18\x03 \x01(\x0b\x32\'.littlehorse.PendingInterruptHaltReasonH\x00\x12G\n\x0fpending_failure\x18\x04 \x01(\x0b\x32,.littlehorse.PendingFailureHandlerHaltReasonH\x00\x12\x42\n\x10handling_failure\x18\x05 \x01(\x0b\x32&.littlehorse.HandlingFailureHaltReasonH\x00\x12.\n\x0bmanual_halt\x18\x06 \x01(\x0b\x32\x17.littlehorse.ManualHaltH\x00\x42\x08\n\x06reason*K\n\nThreadType\x12\x0e\n\nENTRYPOINT\x10\x00\x12\t\n\x05\x43HILD\x10\x01\x12\r\n\tINTERRUPT\x10\x02\x12\x13\n\x0f\x46\x41ILURE_HANDLER\x10\x03\x42M\n\x1fio.littlehorse.sdk.common.protoP\x01Z\t.;lhproto\xaa\x02\x1cLittleHorse.Sdk.Common.Protob\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'wf_run_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\t.;lhproto\252\002\034LittleHorse.Sdk.Common.Proto'
  _globals['_THREADTYPE']._serialized_start=2345
  _globals['_THREADTYPE']._serialized_end=2420
  _globals['_WFRUN']._serialized_start=100
  _globals['_WFRUN']._serialized_end=589
  _globals['_THREADRUN']._serialized_start=592
  _globals['_THREADRUN']._serialized_end=1305
  _globals['_FAILUREBEINGHANDLED']._serialized_start=1307
  _globals['_FAILUREBEINGHANDLED']._serialized_end=1406
  _globals['_PENDINGINTERRUPT']._serialized_start=1409
  _globals['_PENDINGINTERRUPT']._serialized_end=1542
  _globals['_PENDINGFAILUREHANDLER']._serialized_start=1544
  _globals['_PENDINGFAILUREHANDLER']._serialized_end=1621
  _globals['_PENDINGINTERRUPTHALTREASON']._serialized_start=1623
  _globals['_PENDINGINTERRUPTHALTREASON']._serialized_end=1708
  _globals['_PENDINGFAILUREHANDLERHALTREASON']._serialized_start=1710
  _globals['_PENDINGFAILUREHANDLERHALTREASON']._serialized_end=1770
  _globals['_HANDLINGFAILUREHALTREASON']._serialized_start=1772
  _globals['_HANDLINGFAILUREHALTREASON']._serialized_end=1826
  _globals['_PARENTHALTED']._serialized_start=1828
  _globals['_PARENTHALTED']._serialized_end=1868
  _globals['_INTERRUPTED']._serialized_start=1870
  _globals['_INTERRUPTED']._serialized_end=1912
  _globals['_MANUALHALT']._serialized_start=1914
  _globals['_MANUALHALT']._serialized_end=1952
  _globals['_THREADHALTREASON']._serialized_start=1955
  _globals['_THREADHALTREASON']._serialized_end=2343
# @@protoc_insertion_point(module_scope)
