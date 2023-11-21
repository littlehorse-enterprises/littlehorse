# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: service.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import timestamp_pb2 as google_dot_protobuf_dot_timestamp__pb2
from google.protobuf import empty_pb2 as google_dot_protobuf_dot_empty__pb2
import littlehorse.model.common_wfspec_pb2 as common__wfspec__pb2
import littlehorse.model.common_enums_pb2 as common__enums__pb2
import littlehorse.model.object_id_pb2 as object__id__pb2
import littlehorse.model.variable_pb2 as variable__pb2
import littlehorse.model.external_event_pb2 as external__event__pb2
import littlehorse.model.wf_run_pb2 as wf__run__pb2
import littlehorse.model.node_run_pb2 as node__run__pb2
import littlehorse.model.task_run_pb2 as task__run__pb2
import littlehorse.model.user_tasks_pb2 as user__tasks__pb2
import littlehorse.model.wf_spec_pb2 as wf__spec__pb2
import littlehorse.model.task_def_pb2 as task__def__pb2
import littlehorse.model.acls_pb2 as acls__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\rservice.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x1bgoogle/protobuf/empty.proto\x1a\x13\x63ommon_wfspec.proto\x1a\x12\x63ommon_enums.proto\x1a\x0fobject_id.proto\x1a\x0evariable.proto\x1a\x14\x65xternal_event.proto\x1a\x0cwf_run.proto\x1a\x0enode_run.proto\x1a\x0etask_run.proto\x1a\x10user_tasks.proto\x1a\rwf_spec.proto\x1a\x0etask_def.proto\x1a\nacls.proto\"+\n\x1bGetLatestUserTaskDefRequest\x12\x0c\n\x04name\x18\x01 \x01(\t\"\xc5\x02\n\x10PutWfSpecRequest\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x44\n\x0cthread_specs\x18\x05 \x03(\x0b\x32..littlehorse.PutWfSpecRequest.ThreadSpecsEntry\x12\x1e\n\x16\x65ntrypoint_thread_name\x18\x06 \x01(\t\x12\x43\n\x10retention_policy\x18\x08 \x01(\x0b\x32$.littlehorse.WorkflowRetentionPolicyH\x00\x88\x01\x01\x1aK\n\x10ThreadSpecsEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12&\n\x05value\x18\x02 \x01(\x0b\x32\x17.littlehorse.ThreadSpec:\x02\x38\x01\x42\x13\n\x11_retention_policyJ\x04\x08\x02\x10\x03J\x04\x08\x03\x10\x04J\x04\x08\x04\x10\x05J\x04\x08\t\x10\n\"O\n\x11PutTaskDefRequest\x12\x0c\n\x04name\x18\x01 \x01(\t\x12,\n\ninput_vars\x18\x02 \x03(\x0b\x32\x18.littlehorse.VariableDef\"{\n\x15PutUserTaskDefRequest\x12\x0c\n\x04name\x18\x01 \x01(\t\x12*\n\x06\x66ields\x18\x02 \x03(\x0b\x32\x1a.littlehorse.UserTaskField\x12\x18\n\x0b\x64\x65scription\x18\x03 \x01(\tH\x00\x88\x01\x01\x42\x0e\n\x0c_description\"\\\n\x1aPutExternalEventDefRequest\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x1c\n\x0fretention_hours\x18\x02 \x01(\x05H\x00\x88\x01\x01\x42\x12\n\x10_retention_hours\"\x8e\x02\n\x17PutExternalEventRequest\x12\x11\n\twf_run_id\x18\x01 \x01(\t\x12\x1f\n\x17\x65xternal_event_def_name\x18\x02 \x01(\t\x12\x11\n\x04guid\x18\x03 \x01(\tH\x00\x88\x01\x01\x12+\n\x07\x63ontent\x18\x05 \x01(\x0b\x32\x1a.littlehorse.VariableValue\x12\x1e\n\x11thread_run_number\x18\x06 \x01(\x05H\x01\x88\x01\x01\x12\x1e\n\x11node_run_position\x18\x07 \x01(\x05H\x02\x88\x01\x01\x42\x07\n\x05_guidB\x14\n\x12_thread_run_numberB\x14\n\x12_node_run_positionJ\x04\x08\x04\x10\x05J\x04\x08\x08\x10\t\"F\n\x1a\x44\x65leteExternalEventRequest\x12(\n\x02id\x18\x01 \x01(\x0b\x32\x1c.littlehorse.ExternalEventId\"6\n\x12\x44\x65leteWfRunRequest\x12 \n\x02id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\":\n\x14\x44\x65leteTaskDefRequest\x12\"\n\x02id\x18\x01 \x01(\x0b\x32\x16.littlehorse.TaskDefId\"B\n\x18\x44\x65leteUserTaskDefRequest\x12&\n\x02id\x18\x01 \x01(\x0b\x32\x1a.littlehorse.UserTaskDefId\"8\n\x13\x44\x65leteWfSpecRequest\x12!\n\x02id\x18\x01 \x01(\x0b\x32\x15.littlehorse.WfSpecId\"L\n\x1d\x44\x65leteExternalEventDefRequest\x12+\n\x02id\x18\x01 \x01(\x0b\x32\x1f.littlehorse.ExternalEventDefId\"\xf9\x01\n\x0cRunWfRequest\x12\x14\n\x0cwf_spec_name\x18\x01 \x01(\t\x12\x1c\n\x0fwf_spec_version\x18\x02 \x01(\x05H\x00\x88\x01\x01\x12;\n\tvariables\x18\x03 \x03(\x0b\x32(.littlehorse.RunWfRequest.VariablesEntry\x12\x0f\n\x02id\x18\x04 \x01(\tH\x01\x88\x01\x01\x1aL\n\x0eVariablesEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12)\n\x05value\x18\x02 \x01(\x0b\x32\x1a.littlehorse.VariableValue:\x02\x38\x01\x42\x12\n\x10_wf_spec_versionB\x05\n\x03_id\"\xee\x07\n\x12SearchWfRunRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x01\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x02\x88\x01\x01\x12O\n\x0fstatus_and_spec\x18\x03 \x01(\x0b\x32\x34.littlehorse.SearchWfRunRequest.StatusAndSpecRequestH\x00\x12;\n\x04name\x18\x04 \x01(\x0b\x32+.littlehorse.SearchWfRunRequest.NameRequestH\x00\x12O\n\x0fstatus_and_name\x18\x05 \x01(\x0b\x32\x34.littlehorse.SearchWfRunRequest.StatusAndNameRequestH\x00\x1a\x80\x02\n\x14StatusAndSpecRequest\x12\x14\n\x0cwf_spec_name\x18\x01 \x01(\t\x12%\n\x06status\x18\x02 \x01(\x0e\x32\x15.littlehorse.LHStatus\x12\x17\n\x0fwf_spec_version\x18\x03 \x01(\x05\x12\x37\n\x0e\x65\x61rliest_start\x18\x04 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x00\x88\x01\x01\x12\x35\n\x0clatest_start\x18\x05 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x01\x88\x01\x01\x42\x11\n\x0f_earliest_startB\x0f\n\r_latest_start\x1a\xb7\x01\n\x0bNameRequest\x12\x14\n\x0cwf_spec_name\x18\x01 \x01(\t\x12\x37\n\x0e\x65\x61rliest_start\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x00\x88\x01\x01\x12\x35\n\x0clatest_start\x18\x03 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x01\x88\x01\x01\x42\x11\n\x0f_earliest_startB\x0f\n\r_latest_start\x1a\xe7\x01\n\x14StatusAndNameRequest\x12\x14\n\x0cwf_spec_name\x18\x01 \x01(\t\x12%\n\x06status\x18\x02 \x01(\x0e\x32\x15.littlehorse.LHStatus\x12\x37\n\x0e\x65\x61rliest_start\x18\x03 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x00\x88\x01\x01\x12\x35\n\x0clatest_start\x18\x04 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x01\x88\x01\x01\x42\x11\n\x0f_earliest_startB\x0f\n\r_latest_startB\x10\n\x0ewfrun_criteriaB\x0b\n\t_bookmarkB\x08\n\x06_limit\"X\n\x0bWfRunIdList\x12%\n\x07results\x18\x01 \x03(\x0b\x32\x14.littlehorse.WfRunId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\xbf\x05\n\x14SearchTaskRunRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x01\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x02\x88\x01\x01\x12X\n\x13status_and_task_def\x18\x03 \x01(\x0b\x32\x39.littlehorse.SearchTaskRunRequest.StatusAndTaskDefRequestH\x00\x12\x46\n\x08task_def\x18\x04 \x01(\x0b\x32\x32.littlehorse.SearchTaskRunRequest.ByTaskDefRequestH\x00\x1a\xed\x01\n\x17StatusAndTaskDefRequest\x12\'\n\x06status\x18\x01 \x01(\x0e\x32\x17.littlehorse.TaskStatus\x12\x15\n\rtask_def_name\x18\x02 \x01(\t\x12\x37\n\x0e\x65\x61rliest_start\x18\x03 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x00\x88\x01\x01\x12\x35\n\x0clatest_start\x18\x04 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x01\x88\x01\x01\x42\x11\n\x0f_earliest_startB\x0f\n\r_latest_start\x1a\xbd\x01\n\x10\x42yTaskDefRequest\x12\x15\n\rtask_def_name\x18\x01 \x01(\t\x12\x37\n\x0e\x65\x61rliest_start\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x00\x88\x01\x01\x12\x35\n\x0clatest_start\x18\x03 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x01\x88\x01\x01\x42\x11\n\x0f_earliest_startB\x0f\n\r_latest_startB\x13\n\x11task_run_criteriaB\x0b\n\t_bookmarkB\x08\n\x06_limit\"\\\n\rTaskRunIdList\x12\'\n\x07results\x18\x01 \x03(\x0b\x32\x16.littlehorse.TaskRunId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\x81\x01\n\x14SearchNodeRunRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x01\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x02\x88\x01\x01\x12\x13\n\twf_run_id\x18\x03 \x01(\tH\x00\x42\x12\n\x10noderun_criteriaB\x0b\n\t_bookmarkB\x08\n\x06_limit\"\\\n\rNodeRunIdList\x12\'\n\x07results\x18\x01 \x03(\x0b\x32\x16.littlehorse.NodeRunId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\xb2\x03\n\x18SearchUserTaskRunRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x00\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x01\x88\x01\x01\x12\x33\n\x06status\x18\x03 \x01(\x0e\x32\x1e.littlehorse.UserTaskRunStatusH\x02\x88\x01\x01\x12\x1f\n\x12user_task_def_name\x18\x04 \x01(\tH\x03\x88\x01\x01\x12\x14\n\x07user_id\x18\x05 \x01(\tH\x04\x88\x01\x01\x12\x17\n\nuser_group\x18\x06 \x01(\tH\x05\x88\x01\x01\x12\x37\n\x0e\x65\x61rliest_start\x18\x07 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x06\x88\x01\x01\x12\x35\n\x0clatest_start\x18\x08 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x07\x88\x01\x01\x42\x0b\n\t_bookmarkB\x08\n\x06_limitB\t\n\x07_statusB\x15\n\x13_user_task_def_nameB\n\n\x08_user_idB\r\n\x0b_user_groupB\x11\n\x0f_earliest_startB\x0f\n\r_latest_start\"d\n\x11UserTaskRunIdList\x12+\n\x07results\x18\x01 \x03(\x0b\x32\x1a.littlehorse.UserTaskRunId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\xe9\x02\n\x15SearchVariableRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x01\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x02\x88\x01\x01\x12\x13\n\twf_run_id\x18\x03 \x01(\tH\x00\x12G\n\x05value\x18\x04 \x01(\x0b\x32\x36.littlehorse.SearchVariableRequest.NameAndValueRequestH\x00\x1a\x9a\x01\n\x13NameAndValueRequest\x12)\n\x05value\x18\x01 \x01(\x0b\x32\x1a.littlehorse.VariableValue\x12\x1c\n\x0fwf_spec_version\x18\x02 \x01(\x05H\x00\x88\x01\x01\x12\x10\n\x08var_name\x18\x03 \x01(\t\x12\x14\n\x0cwf_spec_name\x18\x04 \x01(\tB\x12\n\x10_wf_spec_versionB\x13\n\x11variable_criteriaB\x0b\n\t_bookmarkB\x08\n\x06_limit\"^\n\x0eVariableIdList\x12(\n\x07results\x18\x01 \x03(\x0b\x32\x17.littlehorse.VariableId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"x\n\x14SearchTaskDefRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x00\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x01\x88\x01\x01\x12\x13\n\x06prefix\x18\x03 \x01(\tH\x02\x88\x01\x01\x42\x0b\n\t_bookmarkB\x08\n\x06_limitB\t\n\x07_prefix\"\\\n\rTaskDefIdList\x12\'\n\x07results\x18\x01 \x03(\x0b\x32\x16.littlehorse.TaskDefId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\x98\x01\n\x18SearchUserTaskDefRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x01\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x02\x88\x01\x01\x12\x10\n\x06prefix\x18\x03 \x01(\tH\x00\x12\x0e\n\x04name\x18\x04 \x01(\tH\x00\x42\x18\n\x16user_task_def_criteriaB\x0b\n\t_bookmarkB\x08\n\x06_limit\"d\n\x11UserTaskDefIdList\x12+\n\x07results\x18\x01 \x03(\x0b\x32\x1a.littlehorse.UserTaskDefId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\xa6\x01\n\x13SearchWfSpecRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x01\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x02\x88\x01\x01\x12\x0e\n\x04name\x18\x03 \x01(\tH\x00\x12\x10\n\x06prefix\x18\x04 \x01(\tH\x00\x12\x17\n\rtask_def_name\x18\x05 \x01(\tH\x00\x42\x12\n\x10wf_spec_criteriaB\x0b\n\t_bookmarkB\x08\n\x06_limit\"Z\n\x0cWfSpecIdList\x12&\n\x07results\x18\x01 \x03(\x0b\x32\x15.littlehorse.WfSpecId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\x81\x01\n\x1dSearchExternalEventDefRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x00\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x01\x88\x01\x01\x12\x13\n\x06prefix\x18\x03 \x01(\tH\x02\x88\x01\x01\x42\x0b\n\t_bookmarkB\x08\n\x06_limitB\t\n\x07_prefix\"n\n\x16\x45xternalEventDefIdList\x12\x30\n\x07results\x18\x01 \x03(\x0b\x32\x1f.littlehorse.ExternalEventDefId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\xea\x02\n\x1aSearchExternalEventRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x01\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x02\x88\x01\x01\x12\x13\n\twf_run_id\x18\x03 \x01(\tH\x00\x12u\n\"external_event_def_name_and_status\x18\x04 \x01(\x0b\x32G.littlehorse.SearchExternalEventRequest.ByExtEvtDefNameAndStatusRequestH\x00\x1aj\n\x1f\x42yExtEvtDefNameAndStatusRequest\x12\x1f\n\x17\x65xternal_event_def_name\x18\x01 \x01(\t\x12\x17\n\nis_claimed\x18\x02 \x01(\x08H\x00\x88\x01\x01\x42\r\n\x0b_is_claimedB\x12\n\x10\x65xt_evt_criteriaB\x0b\n\t_bookmarkB\x08\n\x06_limit\"h\n\x13\x45xternalEventIdList\x12-\n\x07results\x18\x01 \x03(\x0b\x32\x1c.littlehorse.ExternalEventId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"(\n\x13ListNodeRunsRequest\x12\x11\n\twf_run_id\x18\x01 \x01(\t\"4\n\x0bNodeRunList\x12%\n\x07results\x18\x01 \x03(\x0b\x32\x14.littlehorse.NodeRun\")\n\x14ListVariablesRequest\x12\x11\n\twf_run_id\x18\x01 \x01(\t\"6\n\x0cVariableList\x12&\n\x07results\x18\x01 \x03(\x0b\x32\x15.littlehorse.Variable\".\n\x19ListExternalEventsRequest\x12\x11\n\twf_run_id\x18\x01 \x01(\t\"@\n\x11\x45xternalEventList\x12+\n\x07results\x18\x01 \x03(\x0b\x32\x1a.littlehorse.ExternalEvent\"\\\n\x19RegisterTaskWorkerRequest\x12\x11\n\tclient_id\x18\x01 \x01(\t\x12\x15\n\rtask_def_name\x18\x02 \x01(\t\x12\x15\n\rlistener_name\x18\x03 \x01(\t\"]\n\x1aTaskWorkerHeartBeatRequest\x12\x11\n\tclient_id\x18\x01 \x01(\t\x12\x15\n\rtask_def_name\x18\x02 \x01(\t\x12\x15\n\rlistener_name\x18\x03 \x01(\t\"\x81\x01\n\x1aRegisterTaskWorkerResponse\x12+\n\nyour_hosts\x18\x01 \x03(\x0b\x32\x17.littlehorse.LHHostInfo\x12\x1f\n\x12is_cluster_healthy\x18\x02 \x01(\x08H\x00\x88\x01\x01\x42\x15\n\x13_is_cluster_healthy\"(\n\nLHHostInfo\x12\x0c\n\x04host\x18\x01 \x01(\t\x12\x0c\n\x04port\x18\x02 \x01(\x05\"\x85\x01\n\x12TaskWorkerMetadata\x12\x11\n\tclient_id\x18\x01 \x01(\t\x12\x34\n\x10latest_heartbeat\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12&\n\x05hosts\x18\x03 \x03(\x0b\x32\x17.littlehorse.LHHostInfo\"\xf2\x01\n\x0fTaskWorkerGroup\x12\x15\n\rtask_def_name\x18\x01 \x01(\t\x12.\n\ncreated_at\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x43\n\x0ctask_workers\x18\x03 \x03(\x0b\x32-.littlehorse.TaskWorkerGroup.TaskWorkersEntry\x1aS\n\x10TaskWorkersEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12.\n\x05value\x18\x02 \x01(\x0b\x32\x1f.littlehorse.TaskWorkerMetadata:\x02\x38\x01\"u\n\x0fPollTaskRequest\x12\x15\n\rtask_def_name\x18\x01 \x01(\t\x12\x11\n\tclient_id\x18\x02 \x01(\t\x12 \n\x13task_worker_version\x18\x03 \x01(\tH\x00\x88\x01\x01\x42\x16\n\x14_task_worker_version\"\x8c\x02\n\rScheduledTask\x12+\n\x0btask_run_id\x18\x01 \x01(\x0b\x32\x16.littlehorse.TaskRunId\x12+\n\x0btask_def_id\x18\x02 \x01(\x0b\x32\x16.littlehorse.TaskDefId\x12\x16\n\x0e\x61ttempt_number\x18\x03 \x01(\x05\x12-\n\tvariables\x18\x04 \x03(\x0b\x32\x1a.littlehorse.VarNameAndVal\x12.\n\ncreated_at\x18\x05 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12*\n\x06source\x18\x06 \x01(\x0b\x32\x1a.littlehorse.TaskRunSource\"N\n\x10PollTaskResponse\x12/\n\x06result\x18\x01 \x01(\x0b\x32\x1a.littlehorse.ScheduledTaskH\x00\x88\x01\x01\x42\t\n\x07_result\"\x81\x03\n\rReportTaskRun\x12+\n\x0btask_run_id\x18\x01 \x01(\x0b\x32\x16.littlehorse.TaskRunId\x12(\n\x04time\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\'\n\x06status\x18\x03 \x01(\x0e\x32\x17.littlehorse.TaskStatus\x12\x33\n\nlog_output\x18\x05 \x01(\x0b\x32\x1a.littlehorse.VariableValueH\x01\x88\x01\x01\x12\x16\n\x0e\x61ttempt_number\x18\x06 \x01(\x05\x12,\n\x06output\x18\x04 \x01(\x0b\x32\x1a.littlehorse.VariableValueH\x00\x12)\n\x05\x65rror\x18\x07 \x01(\x0b\x32\x18.littlehorse.LHTaskErrorH\x00\x12\x31\n\texception\x18\x08 \x01(\x0b\x32\x1c.littlehorse.LHTaskExceptionH\x00\x42\x08\n\x06resultB\r\n\x0b_log_output\"@\n\x10StopWfRunRequest\x12\x11\n\twf_run_id\x18\x01 \x01(\t\x12\x19\n\x11thread_run_number\x18\x02 \x01(\x05\"B\n\x12ResumeWfRunRequest\x12\x11\n\twf_run_id\x18\x01 \x01(\t\x12\x19\n\x11thread_run_number\x18\x02 \x01(\x05\"\xb3\x01\n\x1aTaskDefMetricsQueryRequest\x12\x30\n\x0cwindow_start\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x35\n\x0bwindow_type\x18\x02 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12\x1a\n\rtask_def_name\x18\x03 \x01(\tH\x00\x88\x01\x01\x42\x10\n\x0e_task_def_name\"\xb4\x01\n\x16ListTaskMetricsRequest\x12\x35\n\x11last_window_start\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x13\n\x0bnum_windows\x18\x02 \x01(\x05\x12\x15\n\rtask_def_name\x18\x03 \x01(\t\x12\x37\n\rwindow_length\x18\x04 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\"G\n\x17ListTaskMetricsResponse\x12,\n\x07results\x18\x01 \x03(\x0b\x32\x1b.littlehorse.TaskDefMetrics\"\xb3\x01\n\x19WfSpecMetricsQueryRequest\x12\x30\n\x0cwindow_start\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x35\n\x0bwindow_type\x18\x02 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12\x14\n\x0cwf_spec_name\x18\x03 \x01(\t\x12\x17\n\x0fwf_spec_version\x18\x04 \x01(\x05\"\xca\x01\n\x14ListWfMetricsRequest\x12\x35\n\x11last_window_start\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x13\n\x0bnum_windows\x18\x02 \x01(\x05\x12\x14\n\x0cwf_spec_name\x18\x03 \x01(\t\x12\x17\n\x0fwf_spec_version\x18\x04 \x01(\x05\x12\x37\n\rwindow_length\x18\x05 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\"D\n\x15ListWfMetricsResponse\x12+\n\x07results\x18\x01 \x03(\x0b\x32\x1a.littlehorse.WfSpecMetrics\"\xe3\x02\n\x0eTaskDefMetrics\x12\x30\n\x0cwindow_start\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12.\n\x04type\x18\x02 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12\x13\n\x0btaskDefName\x18\x03 \x01(\t\x12\x1d\n\x15schedule_to_start_max\x18\x04 \x01(\x03\x12\x1d\n\x15schedule_to_start_avg\x18\x05 \x01(\x03\x12\x1d\n\x15start_to_complete_max\x18\x06 \x01(\x03\x12\x1d\n\x15start_to_complete_avg\x18\x07 \x01(\x03\x12\x17\n\x0ftotal_completed\x18\x08 \x01(\x03\x12\x15\n\rtotal_errored\x18\t \x01(\x03\x12\x15\n\rtotal_started\x18\n \x01(\x03\x12\x17\n\x0ftotal_scheduled\x18\x0b \x01(\x03\"\xa1\x02\n\rWfSpecMetrics\x12\x30\n\x0cwindow_start\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12.\n\x04type\x18\x02 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12\x12\n\nwfSpecName\x18\x03 \x01(\t\x12\x15\n\rwfSpecVersion\x18\x04 \x01(\x05\x12\x15\n\rtotal_started\x18\x05 \x01(\x03\x12\x17\n\x0ftotal_completed\x18\x06 \x01(\x03\x12\x15\n\rtotal_errored\x18\x07 \x01(\x03\x12\x1d\n\x15start_to_complete_max\x18\x08 \x01(\x03\x12\x1d\n\x15start_to_complete_avg\x18\t \x01(\x03\"+\n\x16ListUserTaskRunRequest\x12\x11\n\twf_run_id\x18\x01 \x01(\t\"<\n\x0fUserTaskRunList\x12)\n\x07results\x18\x01 \x03(\x0b\x32\x18.littlehorse.UserTaskRun\"(\n\x13ListTaskRunsRequest\x12\x11\n\twf_run_id\x18\x01 \x01(\t\"4\n\x0bTaskRunList\x12%\n\x07results\x18\x01 \x03(\x0b\x32\x14.littlehorse.TaskRun\"z\n\x14MigrateWfSpecRequest\x12*\n\x0bold_wf_spec\x18\x01 \x01(\x0b\x32\x15.littlehorse.WfSpecId\x12\x36\n\tmigration\x18\x02 \x01(\x0b\x32#.littlehorse.WfSpecVersionMigration*W\n\x0eLHHealthResult\x12\x15\n\x11LH_HEALTH_RUNNING\x10\x00\x12\x19\n\x15LH_HEALTH_REBALANCING\x10\x01\x12\x13\n\x0fLH_HEALTH_ERROR\x10\x02\x32\x92\"\n\x0bLHPublicApi\x12\x44\n\nPutTaskDef\x12\x1e.littlehorse.PutTaskDefRequest\x1a\x14.littlehorse.TaskDef\"\x00\x12<\n\nGetTaskDef\x12\x16.littlehorse.TaskDefId\x1a\x14.littlehorse.TaskDef\"\x00\x12_\n\x13PutExternalEventDef\x12\'.littlehorse.PutExternalEventDefRequest\x1a\x1d.littlehorse.ExternalEventDef\"\x00\x12W\n\x13GetExternalEventDef\x12\x1f.littlehorse.ExternalEventDefId\x1a\x1d.littlehorse.ExternalEventDef\"\x00\x12\x41\n\tPutWfSpec\x12\x1d.littlehorse.PutWfSpecRequest\x1a\x13.littlehorse.WfSpec\"\x00\x12\x39\n\tGetWfSpec\x12\x15.littlehorse.WfSpecId\x1a\x13.littlehorse.WfSpec\"\x00\x12M\n\x0fGetLatestWfSpec\x12#.littlehorse.GetLatestWfSpecRequest\x1a\x13.littlehorse.WfSpec\"\x00\x12I\n\rMigrateWfSpec\x12!.littlehorse.MigrateWfSpecRequest\x1a\x13.littlehorse.WfSpec\"\x00\x12P\n\x0ePutUserTaskDef\x12\".littlehorse.PutUserTaskDefRequest\x1a\x18.littlehorse.UserTaskDef\"\x00\x12H\n\x0eGetUserTaskDef\x12\x1a.littlehorse.UserTaskDefId\x1a\x18.littlehorse.UserTaskDef\"\x00\x12\\\n\x14GetLatestUserTaskDef\x12(.littlehorse.GetLatestUserTaskDefRequest\x1a\x18.littlehorse.UserTaskDef\"\x00\x12\x38\n\x05RunWf\x12\x19.littlehorse.RunWfRequest\x1a\x12.littlehorse.WfRun\"\x00\x12\x36\n\x08GetWfRun\x12\x14.littlehorse.WfRunId\x1a\x12.littlehorse.WfRun\"\x00\x12H\n\x0eGetUserTaskRun\x12\x1a.littlehorse.UserTaskRunId\x1a\x18.littlehorse.UserTaskRun\"\x00\x12T\n\x11\x41ssignUserTaskRun\x12%.littlehorse.AssignUserTaskRunRequest\x1a\x16.google.protobuf.Empty\"\x00\x12X\n\x13\x43ompleteUserTaskRun\x12\'.littlehorse.CompleteUserTaskRunRequest\x1a\x16.google.protobuf.Empty\"\x00\x12T\n\x11\x43\x61ncelUserTaskRun\x12%.littlehorse.CancelUserTaskRunRequest\x1a\x16.google.protobuf.Empty\"\x00\x12W\n\x10ListUserTaskRuns\x12#.littlehorse.ListUserTaskRunRequest\x1a\x1c.littlehorse.UserTaskRunList\"\x00\x12<\n\nGetNodeRun\x12\x16.littlehorse.NodeRunId\x1a\x14.littlehorse.NodeRun\"\x00\x12L\n\x0cListNodeRuns\x12 .littlehorse.ListNodeRunsRequest\x1a\x18.littlehorse.NodeRunList\"\x00\x12<\n\nGetTaskRun\x12\x16.littlehorse.TaskRunId\x1a\x14.littlehorse.TaskRun\"\x00\x12L\n\x0cListTaskRuns\x12 .littlehorse.ListTaskRunsRequest\x1a\x18.littlehorse.TaskRunList\"\x00\x12?\n\x0bGetVariable\x12\x17.littlehorse.VariableId\x1a\x15.littlehorse.Variable\"\x00\x12O\n\rListVariables\x12!.littlehorse.ListVariablesRequest\x1a\x19.littlehorse.VariableList\"\x00\x12V\n\x10PutExternalEvent\x12$.littlehorse.PutExternalEventRequest\x1a\x1a.littlehorse.ExternalEvent\"\x00\x12N\n\x10GetExternalEvent\x12\x1c.littlehorse.ExternalEventId\x1a\x1a.littlehorse.ExternalEvent\"\x00\x12^\n\x12ListExternalEvents\x12&.littlehorse.ListExternalEventsRequest\x1a\x1e.littlehorse.ExternalEventList\"\x00\x12J\n\x0bSearchWfRun\x12\x1f.littlehorse.SearchWfRunRequest\x1a\x18.littlehorse.WfRunIdList\"\x00\x12P\n\rSearchNodeRun\x12!.littlehorse.SearchNodeRunRequest\x1a\x1a.littlehorse.NodeRunIdList\"\x00\x12P\n\rSearchTaskRun\x12!.littlehorse.SearchTaskRunRequest\x1a\x1a.littlehorse.TaskRunIdList\"\x00\x12\\\n\x11SearchUserTaskRun\x12%.littlehorse.SearchUserTaskRunRequest\x1a\x1e.littlehorse.UserTaskRunIdList\"\x00\x12S\n\x0eSearchVariable\x12\".littlehorse.SearchVariableRequest\x1a\x1b.littlehorse.VariableIdList\"\x00\x12\x62\n\x13SearchExternalEvent\x12\'.littlehorse.SearchExternalEventRequest\x1a .littlehorse.ExternalEventIdList\"\x00\x12P\n\rSearchTaskDef\x12!.littlehorse.SearchTaskDefRequest\x1a\x1a.littlehorse.TaskDefIdList\"\x00\x12\\\n\x11SearchUserTaskDef\x12%.littlehorse.SearchUserTaskDefRequest\x1a\x1e.littlehorse.UserTaskDefIdList\"\x00\x12M\n\x0cSearchWfSpec\x12 .littlehorse.SearchWfSpecRequest\x1a\x19.littlehorse.WfSpecIdList\"\x00\x12k\n\x16SearchExternalEventDef\x12*.littlehorse.SearchExternalEventDefRequest\x1a#.littlehorse.ExternalEventDefIdList\"\x00\x12g\n\x12RegisterTaskWorker\x12&.littlehorse.RegisterTaskWorkerRequest\x1a\'.littlehorse.RegisterTaskWorkerResponse\"\x00\x12M\n\x08PollTask\x12\x1c.littlehorse.PollTaskRequest\x1a\x1d.littlehorse.PollTaskResponse\"\x00(\x01\x30\x01\x12\x42\n\nReportTask\x12\x1a.littlehorse.ReportTaskRun\x1a\x16.google.protobuf.Empty\"\x00\x12\x44\n\tStopWfRun\x12\x1d.littlehorse.StopWfRunRequest\x1a\x16.google.protobuf.Empty\"\x00\x12H\n\x0bResumeWfRun\x12\x1f.littlehorse.ResumeWfRunRequest\x1a\x16.google.protobuf.Empty\"\x00\x12H\n\x0b\x44\x65leteWfRun\x12\x1f.littlehorse.DeleteWfRunRequest\x1a\x16.google.protobuf.Empty\"\x00\x12L\n\rDeleteTaskDef\x12!.littlehorse.DeleteTaskDefRequest\x1a\x16.google.protobuf.Empty\"\x00\x12J\n\x0c\x44\x65leteWfSpec\x12 .littlehorse.DeleteWfSpecRequest\x1a\x16.google.protobuf.Empty\"\x00\x12T\n\x11\x44\x65leteUserTaskDef\x12%.littlehorse.DeleteUserTaskDefRequest\x1a\x16.google.protobuf.Empty\"\x00\x12^\n\x16\x44\x65leteExternalEventDef\x12*.littlehorse.DeleteExternalEventDefRequest\x1a\x16.google.protobuf.Empty\"\x00\x12\x61\n\x17GetTaskDefMetricsWindow\x12\'.littlehorse.TaskDefMetricsQueryRequest\x1a\x1b.littlehorse.TaskDefMetrics\"\x00\x12^\n\x16GetWfSpecMetricsWindow\x12&.littlehorse.WfSpecMetricsQueryRequest\x1a\x1a.littlehorse.WfSpecMetrics\"\x00\x12\x61\n\x12ListTaskDefMetrics\x12#.littlehorse.ListTaskMetricsRequest\x1a$.littlehorse.ListTaskMetricsResponse\"\x00\x12\\\n\x11ListWfSpecMetrics\x12!.littlehorse.ListWfMetricsRequest\x1a\".littlehorse.ListWfMetricsResponse\"\x00\x12\x41\n\tPutTenant\x12\x1d.littlehorse.PutTenantRequest\x1a\x13.littlehorse.Tenant\"\x00\x12J\n\x0cPutPrincipal\x12 .littlehorse.PutPrincipalRequest\x1a\x16.littlehorse.Principal\"\x00\x12:\n\x06Whoami\x12\x16.google.protobuf.Empty\x1a\x16.littlehorse.Principal\"\x00\x42G\n\x1fio.littlehorse.sdk.common.protoP\x01Z\x07.;model\xaa\x02\x18LittleHorse.Common.Protob\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'service_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\037io.littlehorse.sdk.common.protoP\001Z\007.;model\252\002\030LittleHorse.Common.Proto'
  _PUTWFSPECREQUEST_THREADSPECSENTRY._options = None
  _PUTWFSPECREQUEST_THREADSPECSENTRY._serialized_options = b'8\001'
  _RUNWFREQUEST_VARIABLESENTRY._options = None
  _RUNWFREQUEST_VARIABLESENTRY._serialized_options = b'8\001'
  _TASKWORKERGROUP_TASKWORKERSENTRY._options = None
  _TASKWORKERGROUP_TASKWORKERSENTRY._serialized_options = b'8\001'
  _globals['_LHHEALTHRESULT']._serialized_start=10375
  _globals['_LHHEALTHRESULT']._serialized_end=10462
  _globals['_GETLATESTUSERTASKDEFREQUEST']._serialized_start=295
  _globals['_GETLATESTUSERTASKDEFREQUEST']._serialized_end=338
  _globals['_PUTWFSPECREQUEST']._serialized_start=341
  _globals['_PUTWFSPECREQUEST']._serialized_end=666
  _globals['_PUTWFSPECREQUEST_THREADSPECSENTRY']._serialized_start=546
  _globals['_PUTWFSPECREQUEST_THREADSPECSENTRY']._serialized_end=621
  _globals['_PUTTASKDEFREQUEST']._serialized_start=668
  _globals['_PUTTASKDEFREQUEST']._serialized_end=747
  _globals['_PUTUSERTASKDEFREQUEST']._serialized_start=749
  _globals['_PUTUSERTASKDEFREQUEST']._serialized_end=872
  _globals['_PUTEXTERNALEVENTDEFREQUEST']._serialized_start=874
  _globals['_PUTEXTERNALEVENTDEFREQUEST']._serialized_end=966
  _globals['_PUTEXTERNALEVENTREQUEST']._serialized_start=969
  _globals['_PUTEXTERNALEVENTREQUEST']._serialized_end=1239
  _globals['_DELETEEXTERNALEVENTREQUEST']._serialized_start=1241
  _globals['_DELETEEXTERNALEVENTREQUEST']._serialized_end=1311
  _globals['_DELETEWFRUNREQUEST']._serialized_start=1313
  _globals['_DELETEWFRUNREQUEST']._serialized_end=1367
  _globals['_DELETETASKDEFREQUEST']._serialized_start=1369
  _globals['_DELETETASKDEFREQUEST']._serialized_end=1427
  _globals['_DELETEUSERTASKDEFREQUEST']._serialized_start=1429
  _globals['_DELETEUSERTASKDEFREQUEST']._serialized_end=1495
  _globals['_DELETEWFSPECREQUEST']._serialized_start=1497
  _globals['_DELETEWFSPECREQUEST']._serialized_end=1553
  _globals['_DELETEEXTERNALEVENTDEFREQUEST']._serialized_start=1555
  _globals['_DELETEEXTERNALEVENTDEFREQUEST']._serialized_end=1631
  _globals['_RUNWFREQUEST']._serialized_start=1634
  _globals['_RUNWFREQUEST']._serialized_end=1883
  _globals['_RUNWFREQUEST_VARIABLESENTRY']._serialized_start=1780
  _globals['_RUNWFREQUEST_VARIABLESENTRY']._serialized_end=1856
  _globals['_SEARCHWFRUNREQUEST']._serialized_start=1886
  _globals['_SEARCHWFRUNREQUEST']._serialized_end=2892
  _globals['_SEARCHWFRUNREQUEST_STATUSANDSPECREQUEST']._serialized_start=2175
  _globals['_SEARCHWFRUNREQUEST_STATUSANDSPECREQUEST']._serialized_end=2431
  _globals['_SEARCHWFRUNREQUEST_NAMEREQUEST']._serialized_start=2434
  _globals['_SEARCHWFRUNREQUEST_NAMEREQUEST']._serialized_end=2617
  _globals['_SEARCHWFRUNREQUEST_STATUSANDNAMEREQUEST']._serialized_start=2620
  _globals['_SEARCHWFRUNREQUEST_STATUSANDNAMEREQUEST']._serialized_end=2851
  _globals['_WFRUNIDLIST']._serialized_start=2894
  _globals['_WFRUNIDLIST']._serialized_end=2982
  _globals['_SEARCHTASKRUNREQUEST']._serialized_start=2985
  _globals['_SEARCHTASKRUNREQUEST']._serialized_end=3688
  _globals['_SEARCHTASKRUNREQUEST_STATUSANDTASKDEFREQUEST']._serialized_start=3215
  _globals['_SEARCHTASKRUNREQUEST_STATUSANDTASKDEFREQUEST']._serialized_end=3452
  _globals['_SEARCHTASKRUNREQUEST_BYTASKDEFREQUEST']._serialized_start=3455
  _globals['_SEARCHTASKRUNREQUEST_BYTASKDEFREQUEST']._serialized_end=3644
  _globals['_TASKRUNIDLIST']._serialized_start=3690
  _globals['_TASKRUNIDLIST']._serialized_end=3782
  _globals['_SEARCHNODERUNREQUEST']._serialized_start=3785
  _globals['_SEARCHNODERUNREQUEST']._serialized_end=3914
  _globals['_NODERUNIDLIST']._serialized_start=3916
  _globals['_NODERUNIDLIST']._serialized_end=4008
  _globals['_SEARCHUSERTASKRUNREQUEST']._serialized_start=4011
  _globals['_SEARCHUSERTASKRUNREQUEST']._serialized_end=4445
  _globals['_USERTASKRUNIDLIST']._serialized_start=4447
  _globals['_USERTASKRUNIDLIST']._serialized_end=4547
  _globals['_SEARCHVARIABLEREQUEST']._serialized_start=4550
  _globals['_SEARCHVARIABLEREQUEST']._serialized_end=4911
  _globals['_SEARCHVARIABLEREQUEST_NAMEANDVALUEREQUEST']._serialized_start=4713
  _globals['_SEARCHVARIABLEREQUEST_NAMEANDVALUEREQUEST']._serialized_end=4867
  _globals['_VARIABLEIDLIST']._serialized_start=4913
  _globals['_VARIABLEIDLIST']._serialized_end=5007
  _globals['_SEARCHTASKDEFREQUEST']._serialized_start=5009
  _globals['_SEARCHTASKDEFREQUEST']._serialized_end=5129
  _globals['_TASKDEFIDLIST']._serialized_start=5131
  _globals['_TASKDEFIDLIST']._serialized_end=5223
  _globals['_SEARCHUSERTASKDEFREQUEST']._serialized_start=5226
  _globals['_SEARCHUSERTASKDEFREQUEST']._serialized_end=5378
  _globals['_USERTASKDEFIDLIST']._serialized_start=5380
  _globals['_USERTASKDEFIDLIST']._serialized_end=5480
  _globals['_SEARCHWFSPECREQUEST']._serialized_start=5483
  _globals['_SEARCHWFSPECREQUEST']._serialized_end=5649
  _globals['_WFSPECIDLIST']._serialized_start=5651
  _globals['_WFSPECIDLIST']._serialized_end=5741
  _globals['_SEARCHEXTERNALEVENTDEFREQUEST']._serialized_start=5744
  _globals['_SEARCHEXTERNALEVENTDEFREQUEST']._serialized_end=5873
  _globals['_EXTERNALEVENTDEFIDLIST']._serialized_start=5875
  _globals['_EXTERNALEVENTDEFIDLIST']._serialized_end=5985
  _globals['_SEARCHEXTERNALEVENTREQUEST']._serialized_start=5988
  _globals['_SEARCHEXTERNALEVENTREQUEST']._serialized_end=6350
  _globals['_SEARCHEXTERNALEVENTREQUEST_BYEXTEVTDEFNAMEANDSTATUSREQUEST']._serialized_start=6201
  _globals['_SEARCHEXTERNALEVENTREQUEST_BYEXTEVTDEFNAMEANDSTATUSREQUEST']._serialized_end=6307
  _globals['_EXTERNALEVENTIDLIST']._serialized_start=6352
  _globals['_EXTERNALEVENTIDLIST']._serialized_end=6456
  _globals['_LISTNODERUNSREQUEST']._serialized_start=6458
  _globals['_LISTNODERUNSREQUEST']._serialized_end=6498
  _globals['_NODERUNLIST']._serialized_start=6500
  _globals['_NODERUNLIST']._serialized_end=6552
  _globals['_LISTVARIABLESREQUEST']._serialized_start=6554
  _globals['_LISTVARIABLESREQUEST']._serialized_end=6595
  _globals['_VARIABLELIST']._serialized_start=6597
  _globals['_VARIABLELIST']._serialized_end=6651
  _globals['_LISTEXTERNALEVENTSREQUEST']._serialized_start=6653
  _globals['_LISTEXTERNALEVENTSREQUEST']._serialized_end=6699
  _globals['_EXTERNALEVENTLIST']._serialized_start=6701
  _globals['_EXTERNALEVENTLIST']._serialized_end=6765
  _globals['_REGISTERTASKWORKERREQUEST']._serialized_start=6767
  _globals['_REGISTERTASKWORKERREQUEST']._serialized_end=6859
  _globals['_TASKWORKERHEARTBEATREQUEST']._serialized_start=6861
  _globals['_TASKWORKERHEARTBEATREQUEST']._serialized_end=6954
  _globals['_REGISTERTASKWORKERRESPONSE']._serialized_start=6957
  _globals['_REGISTERTASKWORKERRESPONSE']._serialized_end=7086
  _globals['_LHHOSTINFO']._serialized_start=7088
  _globals['_LHHOSTINFO']._serialized_end=7128
  _globals['_TASKWORKERMETADATA']._serialized_start=7131
  _globals['_TASKWORKERMETADATA']._serialized_end=7264
  _globals['_TASKWORKERGROUP']._serialized_start=7267
  _globals['_TASKWORKERGROUP']._serialized_end=7509
  _globals['_TASKWORKERGROUP_TASKWORKERSENTRY']._serialized_start=7426
  _globals['_TASKWORKERGROUP_TASKWORKERSENTRY']._serialized_end=7509
  _globals['_POLLTASKREQUEST']._serialized_start=7511
  _globals['_POLLTASKREQUEST']._serialized_end=7628
  _globals['_SCHEDULEDTASK']._serialized_start=7631
  _globals['_SCHEDULEDTASK']._serialized_end=7899
  _globals['_POLLTASKRESPONSE']._serialized_start=7901
  _globals['_POLLTASKRESPONSE']._serialized_end=7979
  _globals['_REPORTTASKRUN']._serialized_start=7982
  _globals['_REPORTTASKRUN']._serialized_end=8367
  _globals['_STOPWFRUNREQUEST']._serialized_start=8369
  _globals['_STOPWFRUNREQUEST']._serialized_end=8433
  _globals['_RESUMEWFRUNREQUEST']._serialized_start=8435
  _globals['_RESUMEWFRUNREQUEST']._serialized_end=8501
  _globals['_TASKDEFMETRICSQUERYREQUEST']._serialized_start=8504
  _globals['_TASKDEFMETRICSQUERYREQUEST']._serialized_end=8683
  _globals['_LISTTASKMETRICSREQUEST']._serialized_start=8686
  _globals['_LISTTASKMETRICSREQUEST']._serialized_end=8866
  _globals['_LISTTASKMETRICSRESPONSE']._serialized_start=8868
  _globals['_LISTTASKMETRICSRESPONSE']._serialized_end=8939
  _globals['_WFSPECMETRICSQUERYREQUEST']._serialized_start=8942
  _globals['_WFSPECMETRICSQUERYREQUEST']._serialized_end=9121
  _globals['_LISTWFMETRICSREQUEST']._serialized_start=9124
  _globals['_LISTWFMETRICSREQUEST']._serialized_end=9326
  _globals['_LISTWFMETRICSRESPONSE']._serialized_start=9328
  _globals['_LISTWFMETRICSRESPONSE']._serialized_end=9396
  _globals['_TASKDEFMETRICS']._serialized_start=9399
  _globals['_TASKDEFMETRICS']._serialized_end=9754
  _globals['_WFSPECMETRICS']._serialized_start=9757
  _globals['_WFSPECMETRICS']._serialized_end=10046
  _globals['_LISTUSERTASKRUNREQUEST']._serialized_start=10048
  _globals['_LISTUSERTASKRUNREQUEST']._serialized_end=10091
  _globals['_USERTASKRUNLIST']._serialized_start=10093
  _globals['_USERTASKRUNLIST']._serialized_end=10153
  _globals['_LISTTASKRUNSREQUEST']._serialized_start=10155
  _globals['_LISTTASKRUNSREQUEST']._serialized_end=10195
  _globals['_TASKRUNLIST']._serialized_start=10197
  _globals['_TASKRUNLIST']._serialized_end=10249
  _globals['_MIGRATEWFSPECREQUEST']._serialized_start=10251
  _globals['_MIGRATEWFSPECREQUEST']._serialized_end=10373
  _globals['_LHPUBLICAPI']._serialized_start=10465
  _globals['_LHPUBLICAPI']._serialized_end=14835
# @@protoc_insertion_point(module_scope)
