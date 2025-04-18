# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: common_enums.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x12\x63ommon_enums.proto\x12\x0blittlehorse*g\n\x08LHStatus\x12\x0c\n\x08STARTING\x10\x00\x12\x0b\n\x07RUNNING\x10\x01\x12\r\n\tCOMPLETED\x10\x02\x12\x0b\n\x07HALTING\x10\x03\x12\n\n\x06HALTED\x10\x04\x12\t\n\x05\x45RROR\x10\x05\x12\r\n\tEXCEPTION\x10\x06*;\n\x0eMetadataStatus\x12\n\n\x06\x41\x43TIVE\x10\x00\x12\x0c\n\x08\x41RCHIVED\x10\x01\x12\x0f\n\x0bTERMINATING\x10\x02*\xc8\x01\n\nTaskStatus\x12\x12\n\x0eTASK_SCHEDULED\x10\x00\x12\x10\n\x0cTASK_RUNNING\x10\x01\x12\x10\n\x0cTASK_SUCCESS\x10\x02\x12\x0f\n\x0bTASK_FAILED\x10\x03\x12\x10\n\x0cTASK_TIMEOUT\x10\x04\x12\x1b\n\x17TASK_OUTPUT_SERDE_ERROR\x10\x05\x12\x1c\n\x18TASK_INPUT_VAR_SUB_ERROR\x10\x06\x12\x12\n\x0eTASK_EXCEPTION\x10\x08\x12\x10\n\x0cTASK_PENDING\x10\t*=\n\x13MetricsWindowLength\x12\r\n\tMINUTES_5\x10\x00\x12\x0b\n\x07HOURS_2\x10\x01\x12\n\n\x06\x44\x41YS_1\x10\x02*]\n\x0cVariableType\x12\x0c\n\x08JSON_OBJ\x10\x00\x12\x0c\n\x08JSON_ARR\x10\x01\x12\n\n\x06\x44OUBLE\x10\x02\x12\x08\n\x04\x42OOL\x10\x03\x12\x07\n\x03STR\x10\x04\x12\x07\n\x03INT\x10\x05\x12\t\n\x05\x42YTES\x10\x06*\xb6\x01\n\x0bLHErrorType\x12\x11\n\rCHILD_FAILURE\x10\x00\x12\x11\n\rVAR_SUB_ERROR\x10\x01\x12\x16\n\x12VAR_MUTATION_ERROR\x10\x02\x12\x17\n\x13USER_TASK_CANCELLED\x10\x03\x12\x0b\n\x07TIMEOUT\x10\x04\x12\x10\n\x0cTASK_FAILURE\x10\x05\x12\r\n\tVAR_ERROR\x10\x06\x12\x0e\n\nTASK_ERROR\x10\x07\x12\x12\n\x0eINTERNAL_ERROR\x10\x08\x42M\n\x1fio.littlehorse.sdk.common.protoP\x01Z\t.;lhproto\xaa\x02\x1cLittleHorse.Sdk.Common.Protob\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'common_enums_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\t.;lhproto\252\002\034LittleHorse.Sdk.Common.Proto'
  _globals['_LHSTATUS']._serialized_start=35
  _globals['_LHSTATUS']._serialized_end=138
  _globals['_METADATASTATUS']._serialized_start=140
  _globals['_METADATASTATUS']._serialized_end=199
  _globals['_TASKSTATUS']._serialized_start=202
  _globals['_TASKSTATUS']._serialized_end=402
  _globals['_METRICSWINDOWLENGTH']._serialized_start=404
  _globals['_METRICSWINDOWLENGTH']._serialized_end=465
  _globals['_VARIABLETYPE']._serialized_start=467
  _globals['_VARIABLETYPE']._serialized_end=560
  _globals['_LHERRORTYPE']._serialized_start=563
  _globals['_LHERRORTYPE']._serialized_end=745
# @@protoc_insertion_point(module_scope)
