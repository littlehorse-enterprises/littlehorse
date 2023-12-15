# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: wf_spec.proto
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
import littlehorse.model.object_id_pb2 as object__id__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\rwf_spec.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x12\x63ommon_enums.proto\x1a\x13\x63ommon_wfspec.proto\x1a\x0fobject_id.proto\"\x95\x05\n\x06WfSpec\x12!\n\x02id\x18\x01 \x01(\x0b\x32\x15.littlehorse.WfSpecId\x12.\n\ncreated_at\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x33\n\x10\x66rozen_variables\x18\x03 \x03(\x0b\x32\x19.littlehorse.ThreadVarDef\x12+\n\x06status\x18\x04 \x01(\x0e\x32\x1b.littlehorse.MetadataStatus\x12:\n\x0cthread_specs\x18\x05 \x03(\x0b\x32$.littlehorse.WfSpec.ThreadSpecsEntry\x12\x1e\n\x16\x65ntrypoint_thread_name\x18\x06 \x01(\t\x12\x43\n\x10retention_policy\x18\x07 \x01(\x0b\x32$.littlehorse.WorkflowRetentionPolicyH\x00\x88\x01\x01\x12;\n\tmigration\x18\x08 \x01(\x0b\x32#.littlehorse.WfSpecVersionMigrationH\x01\x88\x01\x01\x12\x46\n\x0eparent_wf_spec\x18\t \x01(\x0b\x32).littlehorse.WfSpec.ParentWfSpecReferenceH\x02\x88\x01\x01\x1aK\n\x10ThreadSpecsEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12&\n\x05value\x18\x02 \x01(\x0b\x32\x17.littlehorse.ThreadSpec:\x02\x38\x01\x1a-\n\x15ParentWfSpecReference\x12\x14\n\x0cwf_spec_name\x18\x01 \x01(\tB\x13\n\x11_retention_policyB\x0c\n\n_migrationB\x11\n\x0f_parent_wf_spec\"Q\n\x17WorkflowRetentionPolicy\x12&\n\x1cseconds_after_wf_termination\x18\x01 \x01(\x03H\x00\x42\x0e\n\x0cwf_gc_policy\"N\n\tJsonIndex\x12\x12\n\nfield_path\x18\x01 \x01(\t\x12-\n\nfield_type\x18\x02 \x01(\x0e\x32\x19.littlehorse.VariableType\"B\n\x15SearchableVariableDef\x12)\n\x07var_def\x18\x01 \x01(\x0b\x32\x18.littlehorse.VariableDef\"\x8d\x01\n\x0cThreadVarDef\x12)\n\x07var_def\x18\x01 \x01(\x0b\x32\x18.littlehorse.VariableDef\x12\x10\n\x08required\x18\x02 \x01(\x08\x12\x12\n\nsearchable\x18\x03 \x01(\x08\x12,\n\x0cjson_indexes\x18\x04 \x03(\x0b\x32\x16.littlehorse.JsonIndex\"\xbd\x02\n\nThreadSpec\x12\x31\n\x05nodes\x18\x01 \x03(\x0b\x32\".littlehorse.ThreadSpec.NodesEntry\x12\x30\n\rvariable_defs\x18\x02 \x03(\x0b\x32\x19.littlehorse.ThreadVarDef\x12\x31\n\x0einterrupt_defs\x18\x03 \x03(\x0b\x32\x19.littlehorse.InterruptDef\x12\x41\n\x10retention_policy\x18\x04 \x01(\x0b\x32\".littlehorse.ThreadRetentionPolicyH\x00\x88\x01\x01\x1a?\n\nNodesEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12 \n\x05value\x18\x02 \x01(\x0b\x32\x11.littlehorse.Node:\x02\x38\x01\x42\x13\n\x11_retention_policy\"W\n\x15ThreadRetentionPolicy\x12*\n seconds_after_thread_termination\x18\x01 \x01(\x03H\x00\x42\x12\n\x10thread_gc_policy\"i\n\x0cInterruptDef\x12>\n\x15\x65xternal_event_def_id\x18\x01 \x01(\x0b\x32\x1f.littlehorse.ExternalEventDefId\x12\x19\n\x11handler_spec_name\x18\x02 \x01(\t\"\xbe\x01\n\x0fStartThreadNode\x12\x18\n\x10thread_spec_name\x18\x01 \x01(\t\x12>\n\tvariables\x18\x02 \x03(\x0b\x32+.littlehorse.StartThreadNode.VariablesEntry\x1aQ\n\x0eVariablesEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12.\n\x05value\x18\x02 \x01(\x0b\x32\x1f.littlehorse.VariableAssignment:\x02\x38\x01\"\x83\x02\n\x18StartMultipleThreadsNode\x12\x18\n\x10thread_spec_name\x18\x01 \x01(\t\x12G\n\tvariables\x18\x02 \x03(\x0b\x32\x34.littlehorse.StartMultipleThreadsNode.VariablesEntry\x12\x31\n\x08iterable\x18\x03 \x01(\x0b\x32\x1f.littlehorse.VariableAssignment\x1aQ\n\x0eVariablesEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12.\n\x05value\x18\x02 \x01(\x0b\x32\x1f.littlehorse.VariableAssignment:\x02\x38\x01\"\xf0\x01\n\x11\x46\x61ilureHandlerDef\x12\x19\n\x11handler_spec_name\x18\x02 \x01(\t\x12\x1a\n\x10specific_failure\x18\x01 \x01(\tH\x00\x12K\n\x13\x61ny_failure_of_type\x18\x03 \x01(\x0e\x32,.littlehorse.FailureHandlerDef.LHFailureTypeH\x00\"C\n\rLHFailureType\x12\x16\n\x12\x46\x41ILURE_TYPE_ERROR\x10\x00\x12\x1a\n\x16\x46\x41ILURE_TYPE_EXCEPTION\x10\x01\x42\x12\n\x10\x66\x61ilure_to_catch\"\xa3\x02\n\x12WaitForThreadsNode\x12@\n\x07threads\x18\x01 \x03(\x0b\x32/.littlehorse.WaitForThreadsNode.ThreadToWaitFor\x12\x39\n\x0bthread_list\x18\x03 \x01(\x0b\x32\x1f.littlehorse.VariableAssignmentH\x00\x88\x01\x01\x12\x31\n\x06policy\x18\x02 \x01(\x0e\x32!.littlehorse.WaitForThreadsPolicy\x1aM\n\x0fThreadToWaitFor\x12:\n\x11thread_run_number\x18\x01 \x01(\x0b\x32\x1f.littlehorse.VariableAssignmentB\x0e\n\x0c_thread_list\"\x8d\x01\n\x11\x45xternalEventNode\x12>\n\x15\x65xternal_event_def_id\x18\x01 \x01(\x0b\x32\x1f.littlehorse.ExternalEventDefId\x12\x38\n\x0ftimeout_seconds\x18\x02 \x01(\x0b\x32\x1f.littlehorse.VariableAssignment\"\x10\n\x0e\x45ntrypointNode\"M\n\x08\x45xitNode\x12\x31\n\x0b\x66\x61ilure_def\x18\x01 \x01(\x0b\x32\x17.littlehorse.FailureDefH\x00\x88\x01\x01\x42\x0e\n\x0c_failure_def\"v\n\nFailureDef\x12\x14\n\x0c\x66\x61ilure_name\x18\x01 \x01(\t\x12\x0f\n\x07message\x18\x02 \x01(\t\x12\x35\n\x07\x63ontent\x18\x03 \x01(\x0b\x32\x1f.littlehorse.VariableAssignmentH\x00\x88\x01\x01\x42\n\n\x08_content\"\xa3\x05\n\x04Node\x12)\n\x0eoutgoing_edges\x18\x01 \x03(\x0b\x32\x11.littlehorse.Edge\x12\x39\n\x12variable_mutations\x18\x02 \x03(\x0b\x32\x1d.littlehorse.VariableMutation\x12\x38\n\x10\x66\x61ilure_handlers\x18\x04 \x03(\x0b\x32\x1e.littlehorse.FailureHandlerDef\x12\x31\n\nentrypoint\x18\x05 \x01(\x0b\x32\x1b.littlehorse.EntrypointNodeH\x00\x12%\n\x04\x65xit\x18\x06 \x01(\x0b\x32\x15.littlehorse.ExitNodeH\x00\x12%\n\x04task\x18\x07 \x01(\x0b\x32\x15.littlehorse.TaskNodeH\x00\x12\x38\n\x0e\x65xternal_event\x18\x08 \x01(\x0b\x32\x1e.littlehorse.ExternalEventNodeH\x00\x12\x34\n\x0cstart_thread\x18\t \x01(\x0b\x32\x1c.littlehorse.StartThreadNodeH\x00\x12;\n\x10wait_for_threads\x18\n \x01(\x0b\x32\x1f.littlehorse.WaitForThreadsNodeH\x00\x12#\n\x03nop\x18\x0b \x01(\x0b\x32\x14.littlehorse.NopNodeH\x00\x12\'\n\x05sleep\x18\x0c \x01(\x0b\x32\x16.littlehorse.SleepNodeH\x00\x12.\n\tuser_task\x18\r \x01(\x0b\x32\x19.littlehorse.UserTaskNodeH\x00\x12G\n\x16start_multiple_threads\x18\x0f \x01(\x0b\x32%.littlehorse.StartMultipleThreadsNodeH\x00\x42\x06\n\x04node\"\xe2\x02\n\x0cUserTaskNode\x12\x1a\n\x12user_task_def_name\x18\x01 \x01(\t\x12\x38\n\nuser_group\x18\x02 \x01(\x0b\x32\x1f.littlehorse.VariableAssignmentH\x00\x88\x01\x01\x12\x35\n\x07user_id\x18\x03 \x01(\x0b\x32\x1f.littlehorse.VariableAssignmentH\x01\x88\x01\x01\x12-\n\x07\x61\x63tions\x18\x04 \x03(\x0b\x32\x1c.littlehorse.UTActionTrigger\x12\"\n\x15user_task_def_version\x18\x05 \x01(\x05H\x02\x88\x01\x01\x12\x33\n\x05notes\x18\x06 \x01(\x0b\x32\x1f.littlehorse.VariableAssignmentH\x03\x88\x01\x01\x42\r\n\x0b_user_groupB\n\n\x08_user_idB\x18\n\x16_user_task_def_versionB\x08\n\x06_notes\"\x9b\x01\n\rEdgeCondition\x12+\n\ncomparator\x18\x01 \x01(\x0e\x32\x17.littlehorse.Comparator\x12-\n\x04left\x18\x02 \x01(\x0b\x32\x1f.littlehorse.VariableAssignment\x12.\n\x05right\x18\x03 \x01(\x0b\x32\x1f.littlehorse.VariableAssignment\"`\n\x04\x45\x64ge\x12\x16\n\x0esink_node_name\x18\x01 \x01(\t\x12\x32\n\tcondition\x18\x02 \x01(\x0b\x32\x1a.littlehorse.EdgeConditionH\x00\x88\x01\x01\x42\x0c\n\n_condition\"\t\n\x07NopNode\"\xbe\x01\n\tSleepNode\x12\x36\n\x0braw_seconds\x18\x01 \x01(\x0b\x32\x1f.littlehorse.VariableAssignmentH\x00\x12\x34\n\ttimestamp\x18\x02 \x01(\x0b\x32\x1f.littlehorse.VariableAssignmentH\x00\x12\x33\n\x08iso_date\x18\x03 \x01(\x0b\x32\x1f.littlehorse.VariableAssignmentH\x00\x42\x0e\n\x0csleep_length\"\x87\x02\n\x16WfSpecVersionMigration\x12\x19\n\x11new_major_version\x18\x01 \x01(\x05\x12\x14\n\x0cnew_revision\x18\x02 \x01(\x05\x12]\n\x16thread_spec_migrations\x18\x03 \x03(\x0b\x32=.littlehorse.WfSpecVersionMigration.ThreadSpecMigrationsEntry\x1a]\n\x19ThreadSpecMigrationsEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12/\n\x05value\x18\x02 \x01(\x0b\x32 .littlehorse.ThreadSpecMigration:\x02\x38\x01\"\xd5\x01\n\x13ThreadSpecMigration\x12\x1c\n\x14new_thread_spec_name\x18\x01 \x01(\t\x12M\n\x0fnode_migrations\x18\x02 \x03(\x0b\x32\x34.littlehorse.ThreadSpecMigration.NodeMigrationsEntry\x1aQ\n\x13NodeMigrationsEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12)\n\x05value\x18\x02 \x01(\x0b\x32\x1a.littlehorse.NodeMigration:\x02\x38\x01\"&\n\rNodeMigration\x12\x15\n\rnew_node_name\x18\x01 \x01(\tBG\n\x1fio.littlehorse.sdk.common.protoP\x01Z\x07.;model\xaa\x02\x18LittleHorse.Common.Protob\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'wf_spec_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\007.;model\252\002\030LittleHorse.Common.Proto'
  _WFSPEC_THREADSPECSENTRY._options = None
  _WFSPEC_THREADSPECSENTRY._serialized_options = b'8\001'
  _THREADSPEC_NODESENTRY._options = None
  _THREADSPEC_NODESENTRY._serialized_options = b'8\001'
  _STARTTHREADNODE_VARIABLESENTRY._options = None
  _STARTTHREADNODE_VARIABLESENTRY._serialized_options = b'8\001'
  _STARTMULTIPLETHREADSNODE_VARIABLESENTRY._options = None
  _STARTMULTIPLETHREADSNODE_VARIABLESENTRY._serialized_options = b'8\001'
  _WFSPECVERSIONMIGRATION_THREADSPECMIGRATIONSENTRY._options = None
  _WFSPECVERSIONMIGRATION_THREADSPECMIGRATIONSENTRY._serialized_options = b'8\001'
  _THREADSPECMIGRATION_NODEMIGRATIONSENTRY._options = None
  _THREADSPECMIGRATION_NODEMIGRATIONSENTRY._serialized_options = b'8\001'
  _globals['_WFSPEC']._serialized_start=122
  _globals['_WFSPEC']._serialized_end=783
  _globals['_WFSPEC_THREADSPECSENTRY']._serialized_start=607
  _globals['_WFSPEC_THREADSPECSENTRY']._serialized_end=682
  _globals['_WFSPEC_PARENTWFSPECREFERENCE']._serialized_start=684
  _globals['_WFSPEC_PARENTWFSPECREFERENCE']._serialized_end=729
  _globals['_WORKFLOWRETENTIONPOLICY']._serialized_start=785
  _globals['_WORKFLOWRETENTIONPOLICY']._serialized_end=866
  _globals['_JSONINDEX']._serialized_start=868
  _globals['_JSONINDEX']._serialized_end=946
  _globals['_SEARCHABLEVARIABLEDEF']._serialized_start=948
  _globals['_SEARCHABLEVARIABLEDEF']._serialized_end=1014
  _globals['_THREADVARDEF']._serialized_start=1017
  _globals['_THREADVARDEF']._serialized_end=1158
  _globals['_THREADSPEC']._serialized_start=1161
  _globals['_THREADSPEC']._serialized_end=1478
  _globals['_THREADSPEC_NODESENTRY']._serialized_start=1394
  _globals['_THREADSPEC_NODESENTRY']._serialized_end=1457
  _globals['_THREADRETENTIONPOLICY']._serialized_start=1480
  _globals['_THREADRETENTIONPOLICY']._serialized_end=1567
  _globals['_INTERRUPTDEF']._serialized_start=1569
  _globals['_INTERRUPTDEF']._serialized_end=1674
  _globals['_STARTTHREADNODE']._serialized_start=1677
  _globals['_STARTTHREADNODE']._serialized_end=1867
  _globals['_STARTTHREADNODE_VARIABLESENTRY']._serialized_start=1786
  _globals['_STARTTHREADNODE_VARIABLESENTRY']._serialized_end=1867
  _globals['_STARTMULTIPLETHREADSNODE']._serialized_start=1870
  _globals['_STARTMULTIPLETHREADSNODE']._serialized_end=2129
  _globals['_STARTMULTIPLETHREADSNODE_VARIABLESENTRY']._serialized_start=1786
  _globals['_STARTMULTIPLETHREADSNODE_VARIABLESENTRY']._serialized_end=1867
  _globals['_FAILUREHANDLERDEF']._serialized_start=2132
  _globals['_FAILUREHANDLERDEF']._serialized_end=2372
  _globals['_FAILUREHANDLERDEF_LHFAILURETYPE']._serialized_start=2285
  _globals['_FAILUREHANDLERDEF_LHFAILURETYPE']._serialized_end=2352
  _globals['_WAITFORTHREADSNODE']._serialized_start=2375
  _globals['_WAITFORTHREADSNODE']._serialized_end=2666
  _globals['_WAITFORTHREADSNODE_THREADTOWAITFOR']._serialized_start=2573
  _globals['_WAITFORTHREADSNODE_THREADTOWAITFOR']._serialized_end=2650
  _globals['_EXTERNALEVENTNODE']._serialized_start=2669
  _globals['_EXTERNALEVENTNODE']._serialized_end=2810
  _globals['_ENTRYPOINTNODE']._serialized_start=2812
  _globals['_ENTRYPOINTNODE']._serialized_end=2828
  _globals['_EXITNODE']._serialized_start=2830
  _globals['_EXITNODE']._serialized_end=2907
  _globals['_FAILUREDEF']._serialized_start=2909
  _globals['_FAILUREDEF']._serialized_end=3027
  _globals['_NODE']._serialized_start=3030
  _globals['_NODE']._serialized_end=3705
  _globals['_USERTASKNODE']._serialized_start=3708
  _globals['_USERTASKNODE']._serialized_end=4062
  _globals['_EDGECONDITION']._serialized_start=4065
  _globals['_EDGECONDITION']._serialized_end=4220
  _globals['_EDGE']._serialized_start=4222
  _globals['_EDGE']._serialized_end=4318
  _globals['_NOPNODE']._serialized_start=4320
  _globals['_NOPNODE']._serialized_end=4329
  _globals['_SLEEPNODE']._serialized_start=4332
  _globals['_SLEEPNODE']._serialized_end=4522
  _globals['_WFSPECVERSIONMIGRATION']._serialized_start=4525
  _globals['_WFSPECVERSIONMIGRATION']._serialized_end=4788
  _globals['_WFSPECVERSIONMIGRATION_THREADSPECMIGRATIONSENTRY']._serialized_start=4695
  _globals['_WFSPECVERSIONMIGRATION_THREADSPECMIGRATIONSENTRY']._serialized_end=4788
  _globals['_THREADSPECMIGRATION']._serialized_start=4791
  _globals['_THREADSPECMIGRATION']._serialized_end=5004
  _globals['_THREADSPECMIGRATION_NODEMIGRATIONSENTRY']._serialized_start=4923
  _globals['_THREADSPECMIGRATION_NODEMIGRATIONSENTRY']._serialized_end=5004
  _globals['_NODEMIGRATION']._serialized_start=5006
  _globals['_NODEMIGRATION']._serialized_end=5044
# @@protoc_insertion_point(module_scope)
