# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: user_tasks.proto
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
import littlehorse.model.variable_pb2 as variable__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x10user_tasks.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x12\x63ommon_enums.proto\x1a\x0fobject_id.proto\x1a\x0evariable.proto\"\xb2\x01\n\x0bUserTaskDef\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x0f\n\x07version\x18\x02 \x01(\x05\x12\x18\n\x0b\x64\x65scription\x18\x03 \x01(\tH\x00\x88\x01\x01\x12*\n\x06\x66ields\x18\x04 \x03(\x0b\x32\x1a.littlehorse.UserTaskField\x12.\n\ncreated_at\x18\x05 \x01(\x0b\x32\x1a.google.protobuf.TimestampB\x0e\n\x0c_description\"\x98\x01\n\rUserTaskField\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\'\n\x04type\x18\x02 \x01(\x0e\x32\x19.littlehorse.VariableType\x12\x18\n\x0b\x64\x65scription\x18\x03 \x01(\tH\x00\x88\x01\x01\x12\x14\n\x0c\x64isplay_name\x18\x04 \x01(\t\x12\x10\n\x08required\x18\x05 \x01(\x08\x42\x0e\n\x0c_description\"\xa3\x04\n\x0bUserTaskRun\x12&\n\x02id\x18\x01 \x01(\x0b\x32\x1a.littlehorse.UserTaskRunId\x12\x34\n\x10user_task_def_id\x18\x02 \x01(\x0b\x32\x1a.littlehorse.UserTaskDefId\x12\x17\n\nuser_group\x18\x03 \x01(\tH\x00\x88\x01\x01\x12\x14\n\x07user_id\x18\x04 \x01(\tH\x01\x88\x01\x01\x12\x36\n\x07results\x18\x06 \x03(\x0b\x32%.littlehorse.UserTaskRun.ResultsEntry\x12.\n\x06status\x18\x07 \x01(\x0e\x32\x1e.littlehorse.UserTaskRunStatus\x12*\n\x06\x65vents\x18\x08 \x03(\x0b\x32\x1a.littlehorse.UserTaskEvent\x12\x12\n\x05notes\x18\t \x01(\tH\x02\x88\x01\x01\x12\x32\n\x0escheduled_time\x18\n \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12+\n\x0bnode_run_id\x18\x0b \x01(\x0b\x32\x16.littlehorse.NodeRunId\x12\r\n\x05\x65poch\x18\x0c \x01(\x05\x1aJ\n\x0cResultsEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12)\n\x05value\x18\x02 \x01(\x0b\x32\x1a.littlehorse.VariableValue:\x02\x38\x01\x42\r\n\x0b_user_groupB\n\n\x08_user_idB\x08\n\x06_notes\"\xb2\x01\n\x18\x41ssignUserTaskRunRequest\x12\x34\n\x10user_task_run_id\x18\x01 \x01(\x0b\x32\x1a.littlehorse.UserTaskRunId\x12\x16\n\x0eoverride_claim\x18\x02 \x01(\x08\x12\x17\n\nuser_group\x18\x03 \x01(\tH\x00\x88\x01\x01\x12\x14\n\x07user_id\x18\x04 \x01(\tH\x01\x88\x01\x01\x42\r\n\x0b_user_groupB\n\n\x08_user_id\"\xf6\x01\n\x1a\x43ompleteUserTaskRunRequest\x12\x34\n\x10user_task_run_id\x18\x01 \x01(\x0b\x32\x1a.littlehorse.UserTaskRunId\x12\x45\n\x07results\x18\x02 \x03(\x0b\x32\x34.littlehorse.CompleteUserTaskRunRequest.ResultsEntry\x12\x0f\n\x07user_id\x18\x03 \x01(\t\x1aJ\n\x0cResultsEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12)\n\x05value\x18\x02 \x01(\x0b\x32\x1a.littlehorse.VariableValue:\x02\x38\x01\"\xae\x03\n\x1eSaveUserTaskRunProgressRequest\x12\x34\n\x10user_task_run_id\x18\x01 \x01(\x0b\x32\x1a.littlehorse.UserTaskRunId\x12I\n\x07results\x18\x02 \x03(\x0b\x32\x38.littlehorse.SaveUserTaskRunProgressRequest.ResultsEntry\x12\x0f\n\x07user_id\x18\x03 \x01(\t\x12[\n\x06policy\x18\x04 \x01(\x0e\x32K.littlehorse.SaveUserTaskRunProgressRequest.SaveUserTaskRunAssignmentPolicy\x1aJ\n\x0cResultsEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12)\n\x05value\x18\x02 \x01(\x0b\x32\x1a.littlehorse.VariableValue:\x02\x38\x01\"Q\n\x1fSaveUserTaskRunAssignmentPolicy\x12\x1c\n\x18\x46\x41IL_IF_CLAIMED_BY_OTHER\x10\x00\x12\x10\n\x0cIGNORE_CLAIM\x10\x01\"P\n\x18\x43\x61ncelUserTaskRunRequest\x12\x34\n\x10user_task_run_id\x18\x01 \x01(\x0b\x32\x1a.littlehorse.UserTaskRunId\"\xb1\x01\n\x18UserTaskTriggerReference\x12+\n\x0bnode_run_id\x18\x01 \x01(\x0b\x32\x16.littlehorse.NodeRunId\x12\x1e\n\x16user_task_event_number\x18\x02 \x01(\x05\x12\x14\n\x07user_id\x18\x03 \x01(\tH\x00\x88\x01\x01\x12\x17\n\nuser_group\x18\x04 \x01(\tH\x01\x88\x01\x01\x42\n\n\x08_user_idB\r\n\x0b_user_group\"\x86\x06\n\rUserTaskEvent\x12(\n\x04time\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x43\n\rtask_executed\x18\x02 \x01(\x0b\x32*.littlehorse.UserTaskEvent.UTETaskExecutedH\x00\x12:\n\x08\x61ssigned\x18\x03 \x01(\x0b\x32&.littlehorse.UserTaskEvent.UTEAssignedH\x00\x12<\n\tcancelled\x18\x04 \x01(\x0b\x32\'.littlehorse.UserTaskEvent.UTECancelledH\x00\x12\x34\n\x05saved\x18\x05 \x01(\x0b\x32#.littlehorse.UserTaskEvent.UTESavedH\x00\x1a\x1f\n\x0cUTECancelled\x12\x0f\n\x07message\x18\x01 \x01(\t\x1a;\n\x0fUTETaskExecuted\x12(\n\x08task_run\x18\x01 \x01(\x0b\x32\x16.littlehorse.TaskRunId\x1a\xaa\x01\n\x08UTESaved\x12\x0f\n\x07user_id\x18\x01 \x01(\t\x12\x41\n\x07results\x18\x02 \x03(\x0b\x32\x30.littlehorse.UserTaskEvent.UTESaved.ResultsEntry\x1aJ\n\x0cResultsEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12)\n\x05value\x18\x02 \x01(\x0b\x32\x1a.littlehorse.VariableValue:\x02\x38\x01\x1a\xc1\x01\n\x0bUTEAssigned\x12\x18\n\x0bold_user_id\x18\x01 \x01(\tH\x00\x88\x01\x01\x12\x1b\n\x0eold_user_group\x18\x02 \x01(\tH\x01\x88\x01\x01\x12\x18\n\x0bnew_user_id\x18\x03 \x01(\tH\x02\x88\x01\x01\x12\x1b\n\x0enew_user_group\x18\x04 \x01(\tH\x03\x88\x01\x01\x42\x0e\n\x0c_old_user_idB\x11\n\x0f_old_user_groupB\x0e\n\x0c_new_user_idB\x11\n\x0f_new_user_groupB\x07\n\x05\x65vent*J\n\x11UserTaskRunStatus\x12\x0e\n\nUNASSIGNED\x10\x00\x12\x0c\n\x08\x41SSIGNED\x10\x01\x12\x08\n\x04\x44ONE\x10\x03\x12\r\n\tCANCELLED\x10\x04\x42I\n\x1fio.littlehorse.sdk.common.protoP\x01Z\t.;lhproto\xaa\x02\x18LittleHorse.Common.Protob\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'user_tasks_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\t.;lhproto\252\002\030LittleHorse.Common.Proto'
  _USERTASKRUN_RESULTSENTRY._options = None
  _USERTASKRUN_RESULTSENTRY._serialized_options = b'8\001'
  _COMPLETEUSERTASKRUNREQUEST_RESULTSENTRY._options = None
  _COMPLETEUSERTASKRUNREQUEST_RESULTSENTRY._serialized_options = b'8\001'
  _SAVEUSERTASKRUNPROGRESSREQUEST_RESULTSENTRY._options = None
  _SAVEUSERTASKRUNPROGRESSREQUEST_RESULTSENTRY._serialized_options = b'8\001'
  _USERTASKEVENT_UTESAVED_RESULTSENTRY._options = None
  _USERTASKEVENT_UTESAVED_RESULTSENTRY._serialized_options = b'8\001'
  _globals['_USERTASKRUNSTATUS']._serialized_start=2907
  _globals['_USERTASKRUNSTATUS']._serialized_end=2981
  _globals['_USERTASKDEF']._serialized_start=120
  _globals['_USERTASKDEF']._serialized_end=298
  _globals['_USERTASKFIELD']._serialized_start=301
  _globals['_USERTASKFIELD']._serialized_end=453
  _globals['_USERTASKRUN']._serialized_start=456
  _globals['_USERTASKRUN']._serialized_end=1003
  _globals['_USERTASKRUN_RESULTSENTRY']._serialized_start=892
  _globals['_USERTASKRUN_RESULTSENTRY']._serialized_end=966
  _globals['_ASSIGNUSERTASKRUNREQUEST']._serialized_start=1006
  _globals['_ASSIGNUSERTASKRUNREQUEST']._serialized_end=1184
  _globals['_COMPLETEUSERTASKRUNREQUEST']._serialized_start=1187
  _globals['_COMPLETEUSERTASKRUNREQUEST']._serialized_end=1433
  _globals['_COMPLETEUSERTASKRUNREQUEST_RESULTSENTRY']._serialized_start=892
  _globals['_COMPLETEUSERTASKRUNREQUEST_RESULTSENTRY']._serialized_end=966
  _globals['_SAVEUSERTASKRUNPROGRESSREQUEST']._serialized_start=1436
  _globals['_SAVEUSERTASKRUNPROGRESSREQUEST']._serialized_end=1866
  _globals['_SAVEUSERTASKRUNPROGRESSREQUEST_RESULTSENTRY']._serialized_start=892
  _globals['_SAVEUSERTASKRUNPROGRESSREQUEST_RESULTSENTRY']._serialized_end=966
  _globals['_SAVEUSERTASKRUNPROGRESSREQUEST_SAVEUSERTASKRUNASSIGNMENTPOLICY']._serialized_start=1785
  _globals['_SAVEUSERTASKRUNPROGRESSREQUEST_SAVEUSERTASKRUNASSIGNMENTPOLICY']._serialized_end=1866
  _globals['_CANCELUSERTASKRUNREQUEST']._serialized_start=1868
  _globals['_CANCELUSERTASKRUNREQUEST']._serialized_end=1948
  _globals['_USERTASKTRIGGERREFERENCE']._serialized_start=1951
  _globals['_USERTASKTRIGGERREFERENCE']._serialized_end=2128
  _globals['_USERTASKEVENT']._serialized_start=2131
  _globals['_USERTASKEVENT']._serialized_end=2905
  _globals['_USERTASKEVENT_UTECANCELLED']._serialized_start=2435
  _globals['_USERTASKEVENT_UTECANCELLED']._serialized_end=2466
  _globals['_USERTASKEVENT_UTETASKEXECUTED']._serialized_start=2468
  _globals['_USERTASKEVENT_UTETASKEXECUTED']._serialized_end=2527
  _globals['_USERTASKEVENT_UTESAVED']._serialized_start=2530
  _globals['_USERTASKEVENT_UTESAVED']._serialized_end=2700
  _globals['_USERTASKEVENT_UTESAVED_RESULTSENTRY']._serialized_start=892
  _globals['_USERTASKEVENT_UTESAVED_RESULTSENTRY']._serialized_end=966
  _globals['_USERTASKEVENT_UTEASSIGNED']._serialized_start=2703
  _globals['_USERTASKEVENT_UTEASSIGNED']._serialized_end=2896
# @@protoc_insertion_point(module_scope)
