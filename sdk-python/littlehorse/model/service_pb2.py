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
import littlehorse.model.workflow_event_pb2 as workflow__event__pb2
import littlehorse.model.scheduled_wf_run_pb2 as scheduled__wf__run__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\rservice.proto\x12\x0blittlehorse\x1a\x1fgoogle/protobuf/timestamp.proto\x1a\x1bgoogle/protobuf/empty.proto\x1a\x13\x63ommon_wfspec.proto\x1a\x12\x63ommon_enums.proto\x1a\x0fobject_id.proto\x1a\x0evariable.proto\x1a\x14\x65xternal_event.proto\x1a\x0cwf_run.proto\x1a\x0enode_run.proto\x1a\x0etask_run.proto\x1a\x10user_tasks.proto\x1a\rwf_spec.proto\x1a\x0etask_def.proto\x1a\nacls.proto\x1a\x14workflow_event.proto\x1a\x16scheduled_wf_run.proto\"+\n\x1bGetLatestUserTaskDefRequest\x12\x0c\n\x04name\x18\x01 \x01(\t\"\xd3\x03\n\x10PutWfSpecRequest\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x44\n\x0cthread_specs\x18\x05 \x03(\x0b\x32..littlehorse.PutWfSpecRequest.ThreadSpecsEntry\x12\x1e\n\x16\x65ntrypoint_thread_name\x18\x06 \x01(\t\x12\x43\n\x10retention_policy\x18\x08 \x01(\x0b\x32$.littlehorse.WorkflowRetentionPolicyH\x00\x88\x01\x01\x12\x46\n\x0eparent_wf_spec\x18\t \x01(\x0b\x32).littlehorse.WfSpec.ParentWfSpecReferenceH\x01\x88\x01\x01\x12\x37\n\x0f\x61llowed_updates\x18\n \x01(\x0e\x32\x1e.littlehorse.AllowedUpdateType\x1aK\n\x10ThreadSpecsEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12&\n\x05value\x18\x02 \x01(\x0b\x32\x17.littlehorse.ThreadSpec:\x02\x38\x01\x42\x13\n\x11_retention_policyB\x11\n\x0f_parent_wf_specJ\x04\x08\x02\x10\x03J\x04\x08\x03\x10\x04J\x04\x08\x04\x10\x05\"O\n\x11PutTaskDefRequest\x12\x0c\n\x04name\x18\x01 \x01(\t\x12,\n\ninput_vars\x18\x02 \x03(\x0b\x32\x18.littlehorse.VariableDef\"S\n\x1aPutWorkflowEventDefRequest\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\'\n\x04type\x18\x02 \x01(\x0e\x32\x19.littlehorse.VariableType\"{\n\x15PutUserTaskDefRequest\x12\x0c\n\x04name\x18\x01 \x01(\t\x12*\n\x06\x66ields\x18\x02 \x03(\x0b\x32\x1a.littlehorse.UserTaskField\x12\x18\n\x0b\x64\x65scription\x18\x03 \x01(\tH\x00\x88\x01\x01\x42\x0e\n\x0c_description\"o\n\x1aPutExternalEventDefRequest\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x43\n\x10retention_policy\x18\x02 \x01(\x0b\x32).littlehorse.ExternalEventRetentionPolicy\"\xc3\x02\n\x17PutExternalEventRequest\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12>\n\x15\x65xternal_event_def_id\x18\x02 \x01(\x0b\x32\x1f.littlehorse.ExternalEventDefId\x12\x11\n\x04guid\x18\x03 \x01(\tH\x00\x88\x01\x01\x12+\n\x07\x63ontent\x18\x05 \x01(\x0b\x32\x1a.littlehorse.VariableValue\x12\x1e\n\x11thread_run_number\x18\x06 \x01(\x05H\x01\x88\x01\x01\x12\x1e\n\x11node_run_position\x18\x07 \x01(\x05H\x02\x88\x01\x01\x42\x07\n\x05_guidB\x14\n\x12_thread_run_numberB\x14\n\x12_node_run_positionJ\x04\x08\x04\x10\x05J\x04\x08\x08\x10\t\"F\n\x1a\x44\x65leteExternalEventRequest\x12(\n\x02id\x18\x01 \x01(\x0b\x32\x1c.littlehorse.ExternalEventId\"6\n\x12\x44\x65leteWfRunRequest\x12 \n\x02id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\":\n\x14\x44\x65leteTaskDefRequest\x12\"\n\x02id\x18\x01 \x01(\x0b\x32\x16.littlehorse.TaskDefId\"B\n\x18\x44\x65leteUserTaskDefRequest\x12&\n\x02id\x18\x01 \x01(\x0b\x32\x1a.littlehorse.UserTaskDefId\"8\n\x13\x44\x65leteWfSpecRequest\x12!\n\x02id\x18\x01 \x01(\x0b\x32\x15.littlehorse.WfSpecId\"L\n\x1d\x44\x65leteExternalEventDefRequest\x12+\n\x02id\x18\x01 \x01(\x0b\x32\x1f.littlehorse.ExternalEventDefId\"\xe3\x02\n\x0cRunWfRequest\x12\x14\n\x0cwf_spec_name\x18\x01 \x01(\t\x12\x1a\n\rmajor_version\x18\x02 \x01(\x05H\x00\x88\x01\x01\x12\x15\n\x08revision\x18\x03 \x01(\x05H\x01\x88\x01\x01\x12;\n\tvariables\x18\x04 \x03(\x0b\x32(.littlehorse.RunWfRequest.VariablesEntry\x12\x0f\n\x02id\x18\x05 \x01(\tH\x02\x88\x01\x01\x12\x33\n\x10parent_wf_run_id\x18\x06 \x01(\x0b\x32\x14.littlehorse.WfRunIdH\x03\x88\x01\x01\x1aL\n\x0eVariablesEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12)\n\x05value\x18\x02 \x01(\x0b\x32\x1a.littlehorse.VariableValue:\x02\x38\x01\x42\x10\n\x0e_major_versionB\x0b\n\t_revisionB\x05\n\x03_idB\x13\n\x11_parent_wf_run_id\"\x86\x03\n\x11ScheduleWfRequest\x12\x0f\n\x02id\x18\x01 \x01(\tH\x00\x88\x01\x01\x12\x14\n\x0cwf_spec_name\x18\x02 \x01(\t\x12\x1a\n\rmajor_version\x18\x03 \x01(\x05H\x01\x88\x01\x01\x12\x15\n\x08revision\x18\x04 \x01(\x05H\x02\x88\x01\x01\x12@\n\tvariables\x18\x05 \x03(\x0b\x32-.littlehorse.ScheduleWfRequest.VariablesEntry\x12\x33\n\x10parent_wf_run_id\x18\x06 \x01(\x0b\x32\x14.littlehorse.WfRunIdH\x03\x88\x01\x01\x12\x17\n\x0f\x63ron_expression\x18\x07 \x01(\t\x1aL\n\x0eVariablesEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12)\n\x05value\x18\x02 \x01(\x0b\x32\x1a.littlehorse.VariableValue:\x02\x38\x01\x42\x05\n\x03_idB\x10\n\x0e_major_versionB\x0b\n\t_revisionB\x13\n\x11_parent_wf_run_id\"L\n\rVariableMatch\x12\x10\n\x08var_name\x18\x01 \x01(\t\x12)\n\x05value\x18\x02 \x01(\x0b\x32\x1a.littlehorse.VariableValue\"\xbd\x01\n\x19\x41waitWorkflowEventRequest\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12\x36\n\revent_def_ids\x18\x02 \x03(\x0b\x32\x1f.littlehorse.WorkflowEventDefId\x12?\n\x19workflow_events_to_ignore\x18\x03 \x03(\x0b\x32\x1c.littlehorse.WorkflowEventId\"\xdf\x03\n\x12SearchWfRunRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x00\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x01\x88\x01\x01\x12\x14\n\x0cwf_spec_name\x18\x03 \x01(\t\x12\"\n\x15wf_spec_major_version\x18\x04 \x01(\x05H\x02\x88\x01\x01\x12\x1d\n\x10wf_spec_revision\x18\x05 \x01(\x05H\x03\x88\x01\x01\x12*\n\x06status\x18\x06 \x01(\x0e\x32\x15.littlehorse.LHStatusH\x04\x88\x01\x01\x12\x37\n\x0e\x65\x61rliest_start\x18\x07 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x05\x88\x01\x01\x12\x35\n\x0clatest_start\x18\x08 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x06\x88\x01\x01\x12\x34\n\x10variable_filters\x18\t \x03(\x0b\x32\x1a.littlehorse.VariableMatchB\x0b\n\t_bookmarkB\x08\n\x06_limitB\x18\n\x16_wf_spec_major_versionB\x13\n\x11_wf_spec_revisionB\t\n\x07_statusB\x11\n\x0f_earliest_startB\x0f\n\r_latest_start\"X\n\x0bWfRunIdList\x12%\n\x07results\x18\x01 \x03(\x0b\x32\x14.littlehorse.WfRunId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\xbc\x02\n\x14SearchTaskRunRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x00\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x01\x88\x01\x01\x12\x15\n\rtask_def_name\x18\x03 \x01(\t\x12,\n\x06status\x18\x04 \x01(\x0e\x32\x17.littlehorse.TaskStatusH\x02\x88\x01\x01\x12\x37\n\x0e\x65\x61rliest_start\x18\x05 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x03\x88\x01\x01\x12\x35\n\x0clatest_start\x18\x06 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x04\x88\x01\x01\x42\x0b\n\t_bookmarkB\x08\n\x06_limitB\t\n\x07_statusB\x11\n\x0f_earliest_startB\x0f\n\r_latest_start\"\\\n\rTaskRunIdList\x12\'\n\x07results\x18\x01 \x03(\x0b\x32\x16.littlehorse.TaskRunId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\xf1\x03\n\x14SearchNodeRunRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x00\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x01\x88\x01\x01\x12\x37\n\x0e\x65\x61rliest_start\x18\x03 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x02\x88\x01\x01\x12\x35\n\x0clatest_start\x18\x04 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x03\x88\x01\x01\x12=\n\tnode_type\x18\x05 \x01(\x0e\x32*.littlehorse.SearchNodeRunRequest.NodeType\x12%\n\x06status\x18\x06 \x01(\x0e\x32\x15.littlehorse.LHStatus\"\x9c\x01\n\x08NodeType\x12\x08\n\x04TASK\x10\x00\x12\x12\n\x0e\x45XTERNAL_EVENT\x10\x01\x12\x0e\n\nENTRYPOINT\x10\x02\x12\x08\n\x04\x45XIT\x10\x03\x12\x10\n\x0cSTART_THREAD\x10\x04\x12\x10\n\x0cWAIT_THREADS\x10\x05\x12\t\n\x05SLEEP\x10\x06\x12\r\n\tUSER_TASK\x10\x07\x12\x1a\n\x16START_MULTIPLE_THREADS\x10\x08\x42\x0b\n\t_bookmarkB\x08\n\x06_limitB\x11\n\x0f_earliest_startB\x0f\n\r_latest_start\"\\\n\rNodeRunIdList\x12\'\n\x07results\x18\x01 \x03(\x0b\x32\x16.littlehorse.NodeRunId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\xb2\x03\n\x18SearchUserTaskRunRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x00\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x01\x88\x01\x01\x12\x33\n\x06status\x18\x03 \x01(\x0e\x32\x1e.littlehorse.UserTaskRunStatusH\x02\x88\x01\x01\x12\x1f\n\x12user_task_def_name\x18\x04 \x01(\tH\x03\x88\x01\x01\x12\x14\n\x07user_id\x18\x05 \x01(\tH\x04\x88\x01\x01\x12\x17\n\nuser_group\x18\x06 \x01(\tH\x05\x88\x01\x01\x12\x37\n\x0e\x65\x61rliest_start\x18\x07 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x06\x88\x01\x01\x12\x35\n\x0clatest_start\x18\x08 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x07\x88\x01\x01\x42\x0b\n\t_bookmarkB\x08\n\x06_limitB\t\n\x07_statusB\x15\n\x13_user_task_def_nameB\n\n\x08_user_idB\r\n\x0b_user_groupB\x11\n\x0f_earliest_startB\x0f\n\r_latest_start\"d\n\x11UserTaskRunIdList\x12+\n\x07results\x18\x01 \x03(\x0b\x32\x1a.littlehorse.UserTaskRunId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\x9e\x02\n\x15SearchVariableRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x00\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x01\x88\x01\x01\x12)\n\x05value\x18\x03 \x01(\x0b\x32\x1a.littlehorse.VariableValue\x12\"\n\x15wf_spec_major_version\x18\x04 \x01(\x05H\x02\x88\x01\x01\x12\x1d\n\x10wf_spec_revision\x18\x05 \x01(\x05H\x03\x88\x01\x01\x12\x10\n\x08var_name\x18\x06 \x01(\t\x12\x14\n\x0cwf_spec_name\x18\x07 \x01(\tB\x0b\n\t_bookmarkB\x08\n\x06_limitB\x18\n\x16_wf_spec_major_versionB\x13\n\x11_wf_spec_revision\"^\n\x0eVariableIdList\x12(\n\x07results\x18\x01 \x03(\x0b\x32\x17.littlehorse.VariableId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"x\n\x14SearchTaskDefRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x00\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x01\x88\x01\x01\x12\x13\n\x06prefix\x18\x03 \x01(\tH\x02\x88\x01\x01\x42\x0b\n\t_bookmarkB\x08\n\x06_limitB\t\n\x07_prefix\"\\\n\rTaskDefIdList\x12\'\n\x07results\x18\x01 \x03(\x0b\x32\x16.littlehorse.TaskDefId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\x98\x01\n\x18SearchUserTaskDefRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x01\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x02\x88\x01\x01\x12\x10\n\x06prefix\x18\x03 \x01(\tH\x00\x12\x0e\n\x04name\x18\x04 \x01(\tH\x00\x42\x18\n\x16user_task_def_criteriaB\x0b\n\t_bookmarkB\x08\n\x06_limit\"d\n\x11UserTaskDefIdList\x12+\n\x07results\x18\x01 \x03(\x0b\x32\x1a.littlehorse.UserTaskDefId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\xa6\x01\n\x13SearchWfSpecRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x01\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x02\x88\x01\x01\x12\x0e\n\x04name\x18\x03 \x01(\tH\x00\x12\x10\n\x06prefix\x18\x04 \x01(\tH\x00\x12\x17\n\rtask_def_name\x18\x05 \x01(\tH\x00\x42\x12\n\x10wf_spec_criteriaB\x0b\n\t_bookmarkB\x08\n\x06_limit\"Z\n\x0cWfSpecIdList\x12&\n\x07results\x18\x01 \x03(\x0b\x32\x15.littlehorse.WfSpecId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\x81\x01\n\x1dSearchExternalEventDefRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x00\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x01\x88\x01\x01\x12\x13\n\x06prefix\x18\x03 \x01(\tH\x02\x88\x01\x01\x42\x0b\n\t_bookmarkB\x08\n\x06_limitB\t\n\x07_prefix\"n\n\x16\x45xternalEventDefIdList\x12\x30\n\x07results\x18\x01 \x03(\x0b\x32\x1f.littlehorse.ExternalEventDefId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"W\n\x13SearchTenantRequest\x12\x12\n\x05limit\x18\x01 \x01(\x05H\x00\x88\x01\x01\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x01\x88\x01\x01\x42\x08\n\x06_limitB\x0b\n\t_bookmark\"Z\n\x0cTenantIdList\x12&\n\x07results\x18\x01 \x03(\x0b\x32\x15.littlehorse.TenantId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\xab\x02\n\x16SearchPrincipalRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x01\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x02\x88\x01\x01\x12\x37\n\x0e\x65\x61rliest_start\x18\x03 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x03\x88\x01\x01\x12\x35\n\x0clatest_start\x18\x04 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x04\x88\x01\x01\x12\x11\n\x07isAdmin\x18\x05 \x01(\x08H\x00\x12\x12\n\x08tenantId\x18\x06 \x01(\tH\x00\x42\x14\n\x12principal_criteriaB\x0b\n\t_bookmarkB\x08\n\x06_limitB\x11\n\x0f_earliest_startB\x0f\n\r_latest_start\"`\n\x0fPrincipalIdList\x12)\n\x07results\x18\x01 \x03(\x0b\x32\x18.littlehorse.PrincipalId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\xda\x02\n\x1aSearchExternalEventRequest\x12\x15\n\x08\x62ookmark\x18\x01 \x01(\x0cH\x00\x88\x01\x01\x12\x12\n\x05limit\x18\x02 \x01(\x05H\x01\x88\x01\x01\x12\x37\n\x0e\x65\x61rliest_start\x18\x03 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x02\x88\x01\x01\x12\x35\n\x0clatest_start\x18\x04 \x01(\x0b\x32\x1a.google.protobuf.TimestampH\x03\x88\x01\x01\x12>\n\x15\x65xternal_event_def_id\x18\x05 \x01(\x0b\x32\x1f.littlehorse.ExternalEventDefId\x12\x17\n\nis_claimed\x18\x06 \x01(\x08H\x04\x88\x01\x01\x42\x0b\n\t_bookmarkB\x08\n\x06_limitB\x11\n\x0f_earliest_startB\x0f\n\r_latest_startB\r\n\x0b_is_claimed\"h\n\x13\x45xternalEventIdList\x12-\n\x07results\x18\x01 \x03(\x0b\x32\x1c.littlehorse.ExternalEventId\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"\xb6\x01\n\x13ListNodeRunsRequest\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12\x1e\n\x11thread_run_number\x18\x02 \x01(\x05H\x00\x88\x01\x01\x12\x15\n\x08\x62ookmark\x18\x03 \x01(\x0cH\x01\x88\x01\x01\x12\x12\n\x05limit\x18\x04 \x01(\x05H\x02\x88\x01\x01\x42\x14\n\x12_thread_run_numberB\x0b\n\t_bookmarkB\x08\n\x06_limit\"X\n\x0bNodeRunList\x12%\n\x07results\x18\x01 \x03(\x0b\x32\x14.littlehorse.NodeRun\x12\x15\n\x08\x62ookmark\x18\x02 \x01(\x0cH\x00\x88\x01\x01\x42\x0b\n\t_bookmark\"?\n\x14ListVariablesRequest\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\"6\n\x0cVariableList\x12&\n\x07results\x18\x01 \x03(\x0b\x32\x15.littlehorse.Variable\"D\n\x19ListExternalEventsRequest\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\"@\n\x11\x45xternalEventList\x12+\n\x07results\x18\x01 \x03(\x0b\x32\x1a.littlehorse.ExternalEvent\"w\n\x19RegisterTaskWorkerRequest\x12\x16\n\x0etask_worker_id\x18\x01 \x01(\t\x12+\n\x0btask_def_id\x18\x02 \x01(\x0b\x32\x16.littlehorse.TaskDefId\x12\x15\n\rlistener_name\x18\x03 \x01(\t\"s\n\x1aTaskWorkerHeartBeatRequest\x12\x11\n\tclient_id\x18\x01 \x01(\t\x12+\n\x0btask_def_id\x18\x02 \x01(\x0b\x32\x16.littlehorse.TaskDefId\x12\x15\n\rlistener_name\x18\x03 \x01(\t\"\x81\x01\n\x1aRegisterTaskWorkerResponse\x12+\n\nyour_hosts\x18\x01 \x03(\x0b\x32\x17.littlehorse.LHHostInfo\x12\x1f\n\x12is_cluster_healthy\x18\x02 \x01(\x08H\x00\x88\x01\x01\x42\x15\n\x13_is_cluster_healthy\"(\n\nLHHostInfo\x12\x0c\n\x04host\x18\x01 \x01(\t\x12\x0c\n\x04port\x18\x02 \x01(\x05\"\x8b\x01\n\x0fPollTaskRequest\x12+\n\x0btask_def_id\x18\x01 \x01(\x0b\x32\x16.littlehorse.TaskDefId\x12\x11\n\tclient_id\x18\x02 \x01(\t\x12 \n\x13task_worker_version\x18\x03 \x01(\tH\x00\x88\x01\x01\x42\x16\n\x14_task_worker_version\"\x8c\x02\n\rScheduledTask\x12+\n\x0btask_run_id\x18\x01 \x01(\x0b\x32\x16.littlehorse.TaskRunId\x12+\n\x0btask_def_id\x18\x02 \x01(\x0b\x32\x16.littlehorse.TaskDefId\x12\x16\n\x0e\x61ttempt_number\x18\x03 \x01(\x05\x12-\n\tvariables\x18\x04 \x03(\x0b\x32\x1a.littlehorse.VarNameAndVal\x12.\n\ncreated_at\x18\x05 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12*\n\x06source\x18\x06 \x01(\x0b\x32\x1a.littlehorse.TaskRunSource\"N\n\x10PollTaskResponse\x12/\n\x06result\x18\x01 \x01(\x0b\x32\x1a.littlehorse.ScheduledTaskH\x00\x88\x01\x01\x42\t\n\x07_result\"\x81\x03\n\rReportTaskRun\x12+\n\x0btask_run_id\x18\x01 \x01(\x0b\x32\x16.littlehorse.TaskRunId\x12(\n\x04time\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\'\n\x06status\x18\x03 \x01(\x0e\x32\x17.littlehorse.TaskStatus\x12\x33\n\nlog_output\x18\x05 \x01(\x0b\x32\x1a.littlehorse.VariableValueH\x01\x88\x01\x01\x12\x16\n\x0e\x61ttempt_number\x18\x06 \x01(\x05\x12,\n\x06output\x18\x04 \x01(\x0b\x32\x1a.littlehorse.VariableValueH\x00\x12)\n\x05\x65rror\x18\x07 \x01(\x0b\x32\x18.littlehorse.LHTaskErrorH\x00\x12\x31\n\texception\x18\x08 \x01(\x0b\x32\x1c.littlehorse.LHTaskExceptionH\x00\x42\x08\n\x06resultB\r\n\x0b_log_output\"V\n\x10StopWfRunRequest\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12\x19\n\x11thread_run_number\x18\x02 \x01(\x05\"X\n\x12ResumeWfRunRequest\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12\x19\n\x11thread_run_number\x18\x02 \x01(\x05\"w\n\x16RescueThreadRunRequest\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\x12\x19\n\x11thread_run_number\x18\x02 \x01(\x05\x12\x19\n\x11skip_current_node\x18\x03 \x01(\x08\"\xb3\x01\n\x1aTaskDefMetricsQueryRequest\x12\x30\n\x0cwindow_start\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x35\n\x0bwindow_type\x18\x02 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12\x1a\n\rtask_def_name\x18\x03 \x01(\tH\x00\x88\x01\x01\x42\x10\n\x0e_task_def_name\"\xca\x01\n\x16ListTaskMetricsRequest\x12+\n\x0btask_def_id\x18\x01 \x01(\x0b\x32\x16.littlehorse.TaskDefId\x12\x35\n\x11last_window_start\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x37\n\rwindow_length\x18\x03 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12\x13\n\x0bnum_windows\x18\x04 \x01(\x05\"G\n\x17ListTaskMetricsResponse\x12,\n\x07results\x18\x01 \x03(\x0b\x32\x1b.littlehorse.TaskDefMetrics\"\xb1\x01\n\x19WfSpecMetricsQueryRequest\x12)\n\nwf_spec_id\x18\x01 \x01(\x0b\x32\x15.littlehorse.WfSpecId\x12\x30\n\x0cwindow_start\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x37\n\rwindow_length\x18\x03 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\"\xc6\x01\n\x14ListWfMetricsRequest\x12)\n\nwf_spec_id\x18\x01 \x01(\x0b\x32\x15.littlehorse.WfSpecId\x12\x35\n\x11last_window_start\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x37\n\rwindow_length\x18\x03 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12\x13\n\x0bnum_windows\x18\x04 \x01(\x05\"D\n\x15ListWfMetricsResponse\x12+\n\x07results\x18\x01 \x03(\x0b\x32\x1a.littlehorse.WfSpecMetrics\"\xfb\x02\n\x0eTaskDefMetrics\x12+\n\x0btask_def_id\x18\x01 \x01(\x0b\x32\x16.littlehorse.TaskDefId\x12\x30\n\x0cwindow_start\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12.\n\x04type\x18\x03 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12\x1d\n\x15schedule_to_start_max\x18\x04 \x01(\x03\x12\x1d\n\x15schedule_to_start_avg\x18\x05 \x01(\x03\x12\x1d\n\x15start_to_complete_max\x18\x06 \x01(\x03\x12\x1d\n\x15start_to_complete_avg\x18\x07 \x01(\x03\x12\x17\n\x0ftotal_completed\x18\x08 \x01(\x03\x12\x15\n\rtotal_errored\x18\t \x01(\x03\x12\x15\n\rtotal_started\x18\n \x01(\x03\x12\x17\n\x0ftotal_scheduled\x18\x0b \x01(\x03\"\xa1\x02\n\rWfSpecMetrics\x12)\n\nwf_spec_id\x18\x01 \x01(\x0b\x32\x15.littlehorse.WfSpecId\x12\x30\n\x0cwindow_start\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12.\n\x04type\x18\x03 \x01(\x0e\x32 .littlehorse.MetricsWindowLength\x12\x15\n\rtotal_started\x18\x04 \x01(\x03\x12\x17\n\x0ftotal_completed\x18\x05 \x01(\x03\x12\x15\n\rtotal_errored\x18\x06 \x01(\x03\x12\x1d\n\x15start_to_complete_max\x18\x07 \x01(\x03\x12\x1d\n\x15start_to_complete_avg\x18\x08 \x01(\x03\"A\n\x16ListUserTaskRunRequest\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\"<\n\x0fUserTaskRunList\x12)\n\x07results\x18\x01 \x03(\x0b\x32\x18.littlehorse.UserTaskRun\"\x8a\x01\n\x12TaskWorkerMetadata\x12\x16\n\x0etask_worker_id\x18\x01 \x01(\t\x12\x34\n\x10latest_heartbeat\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12&\n\x05hosts\x18\x03 \x03(\x0b\x32\x17.littlehorse.LHHostInfo\"\x87\x02\n\x0fTaskWorkerGroup\x12*\n\x02id\x18\x01 \x01(\x0b\x32\x1e.littlehorse.TaskWorkerGroupId\x12.\n\ncreated_at\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x43\n\x0ctask_workers\x18\x03 \x03(\x0b\x32-.littlehorse.TaskWorkerGroup.TaskWorkersEntry\x1aS\n\x10TaskWorkersEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12.\n\x05value\x18\x02 \x01(\x0b\x32\x1f.littlehorse.TaskWorkerMetadata:\x02\x38\x01\">\n\x13ListTaskRunsRequest\x12\'\n\twf_run_id\x18\x01 \x01(\x0b\x32\x14.littlehorse.WfRunId\"4\n\x0bTaskRunList\x12%\n\x07results\x18\x01 \x03(\x0b\x32\x14.littlehorse.TaskRun\"z\n\x14MigrateWfSpecRequest\x12*\n\x0bold_wf_spec\x18\x01 \x01(\x0b\x32\x15.littlehorse.WfSpecId\x12\x36\n\tmigration\x18\x02 \x01(\x0b\x32#.littlehorse.WfSpecVersionMigration\"T\n\x16GetLatestWfSpecRequest\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x1a\n\rmajor_version\x18\x02 \x01(\x05H\x00\x88\x01\x01\x42\x10\n\x0e_major_version\"\x9c\x01\n\x15ServerVersionResponse\x12\x15\n\rmajor_version\x18\x01 \x01(\x05\x12\x15\n\rminor_version\x18\x02 \x01(\x05\x12\x15\n\rpatch_version\x18\x03 \x01(\x05\x12#\n\x16pre_release_identifier\x18\x04 \x01(\tH\x00\x88\x01\x01\x42\x19\n\x17_pre_release_identifier*P\n\x11\x41llowedUpdateType\x12\x0f\n\x0b\x41LL_UPDATES\x10\x00\x12\x1a\n\x16MINOR_REVISION_UPDATES\x10\x01\x12\x0e\n\nNO_UPDATES\x10\x02\x32\xbe(\n\x0bLittleHorse\x12\x44\n\nPutTaskDef\x12\x1e.littlehorse.PutTaskDefRequest\x1a\x14.littlehorse.TaskDef\"\x00\x12<\n\nGetTaskDef\x12\x16.littlehorse.TaskDefId\x1a\x14.littlehorse.TaskDef\"\x00\x12L\n\x12GetTaskWorkerGroup\x12\x16.littlehorse.TaskDefId\x1a\x1c.littlehorse.TaskWorkerGroup\"\x00\x12_\n\x13PutExternalEventDef\x12\'.littlehorse.PutExternalEventDefRequest\x1a\x1d.littlehorse.ExternalEventDef\"\x00\x12W\n\x13GetExternalEventDef\x12\x1f.littlehorse.ExternalEventDefId\x1a\x1d.littlehorse.ExternalEventDef\"\x00\x12_\n\x13PutWorkflowEventDef\x12\'.littlehorse.PutWorkflowEventDefRequest\x1a\x1d.littlehorse.WorkflowEventDef\"\x00\x12\x41\n\tPutWfSpec\x12\x1d.littlehorse.PutWfSpecRequest\x1a\x13.littlehorse.WfSpec\"\x00\x12\x39\n\tGetWfSpec\x12\x15.littlehorse.WfSpecId\x1a\x13.littlehorse.WfSpec\"\x00\x12M\n\x0fGetLatestWfSpec\x12#.littlehorse.GetLatestWfSpecRequest\x1a\x13.littlehorse.WfSpec\"\x00\x12I\n\rMigrateWfSpec\x12!.littlehorse.MigrateWfSpecRequest\x1a\x13.littlehorse.WfSpec\"\x00\x12P\n\x0ePutUserTaskDef\x12\".littlehorse.PutUserTaskDefRequest\x1a\x18.littlehorse.UserTaskDef\"\x00\x12H\n\x0eGetUserTaskDef\x12\x1a.littlehorse.UserTaskDefId\x1a\x18.littlehorse.UserTaskDef\"\x00\x12\\\n\x14GetLatestUserTaskDef\x12(.littlehorse.GetLatestUserTaskDefRequest\x1a\x18.littlehorse.UserTaskDef\"\x00\x12\x38\n\x05RunWf\x12\x19.littlehorse.RunWfRequest\x1a\x12.littlehorse.WfRun\"\x00\x12K\n\nScheduleWf\x12\x1e.littlehorse.ScheduleWfRequest\x1a\x1b.littlehorse.ScheduledWfRun\"\x00\x12\x36\n\x08GetWfRun\x12\x14.littlehorse.WfRunId\x1a\x12.littlehorse.WfRun\"\x00\x12H\n\x0eGetUserTaskRun\x12\x1a.littlehorse.UserTaskRunId\x1a\x18.littlehorse.UserTaskRun\"\x00\x12T\n\x11\x41ssignUserTaskRun\x12%.littlehorse.AssignUserTaskRunRequest\x1a\x16.google.protobuf.Empty\"\x00\x12X\n\x13\x43ompleteUserTaskRun\x12\'.littlehorse.CompleteUserTaskRunRequest\x1a\x16.google.protobuf.Empty\"\x00\x12T\n\x11\x43\x61ncelUserTaskRun\x12%.littlehorse.CancelUserTaskRunRequest\x1a\x16.google.protobuf.Empty\"\x00\x12W\n\x10ListUserTaskRuns\x12#.littlehorse.ListUserTaskRunRequest\x1a\x1c.littlehorse.UserTaskRunList\"\x00\x12<\n\nGetNodeRun\x12\x16.littlehorse.NodeRunId\x1a\x14.littlehorse.NodeRun\"\x00\x12L\n\x0cListNodeRuns\x12 .littlehorse.ListNodeRunsRequest\x1a\x18.littlehorse.NodeRunList\"\x00\x12<\n\nGetTaskRun\x12\x16.littlehorse.TaskRunId\x1a\x14.littlehorse.TaskRun\"\x00\x12L\n\x0cListTaskRuns\x12 .littlehorse.ListTaskRunsRequest\x1a\x18.littlehorse.TaskRunList\"\x00\x12?\n\x0bGetVariable\x12\x17.littlehorse.VariableId\x1a\x15.littlehorse.Variable\"\x00\x12O\n\rListVariables\x12!.littlehorse.ListVariablesRequest\x1a\x19.littlehorse.VariableList\"\x00\x12V\n\x10PutExternalEvent\x12$.littlehorse.PutExternalEventRequest\x1a\x1a.littlehorse.ExternalEvent\"\x00\x12N\n\x10GetExternalEvent\x12\x1c.littlehorse.ExternalEventId\x1a\x1a.littlehorse.ExternalEvent\"\x00\x12Z\n\x12\x41waitWorkflowEvent\x12&.littlehorse.AwaitWorkflowEventRequest\x1a\x1a.littlehorse.WorkflowEvent\"\x00\x12^\n\x12ListExternalEvents\x12&.littlehorse.ListExternalEventsRequest\x1a\x1e.littlehorse.ExternalEventList\"\x00\x12J\n\x0bSearchWfRun\x12\x1f.littlehorse.SearchWfRunRequest\x1a\x18.littlehorse.WfRunIdList\"\x00\x12P\n\rSearchNodeRun\x12!.littlehorse.SearchNodeRunRequest\x1a\x1a.littlehorse.NodeRunIdList\"\x00\x12P\n\rSearchTaskRun\x12!.littlehorse.SearchTaskRunRequest\x1a\x1a.littlehorse.TaskRunIdList\"\x00\x12\\\n\x11SearchUserTaskRun\x12%.littlehorse.SearchUserTaskRunRequest\x1a\x1e.littlehorse.UserTaskRunIdList\"\x00\x12S\n\x0eSearchVariable\x12\".littlehorse.SearchVariableRequest\x1a\x1b.littlehorse.VariableIdList\"\x00\x12\x62\n\x13SearchExternalEvent\x12\'.littlehorse.SearchExternalEventRequest\x1a .littlehorse.ExternalEventIdList\"\x00\x12P\n\rSearchTaskDef\x12!.littlehorse.SearchTaskDefRequest\x1a\x1a.littlehorse.TaskDefIdList\"\x00\x12\\\n\x11SearchUserTaskDef\x12%.littlehorse.SearchUserTaskDefRequest\x1a\x1e.littlehorse.UserTaskDefIdList\"\x00\x12M\n\x0cSearchWfSpec\x12 .littlehorse.SearchWfSpecRequest\x1a\x19.littlehorse.WfSpecIdList\"\x00\x12k\n\x16SearchExternalEventDef\x12*.littlehorse.SearchExternalEventDefRequest\x1a#.littlehorse.ExternalEventDefIdList\"\x00\x12M\n\x0cSearchTenant\x12 .littlehorse.SearchTenantRequest\x1a\x19.littlehorse.TenantIdList\"\x00\x12V\n\x0fSearchPrincipal\x12#.littlehorse.SearchPrincipalRequest\x1a\x1c.littlehorse.PrincipalIdList\"\x00\x12g\n\x12RegisterTaskWorker\x12&.littlehorse.RegisterTaskWorkerRequest\x1a\'.littlehorse.RegisterTaskWorkerResponse\"\x00\x12M\n\x08PollTask\x12\x1c.littlehorse.PollTaskRequest\x1a\x1d.littlehorse.PollTaskResponse\"\x00(\x01\x30\x01\x12\x42\n\nReportTask\x12\x1a.littlehorse.ReportTaskRun\x1a\x16.google.protobuf.Empty\"\x00\x12\x44\n\tStopWfRun\x12\x1d.littlehorse.StopWfRunRequest\x1a\x16.google.protobuf.Empty\"\x00\x12H\n\x0bResumeWfRun\x12\x1f.littlehorse.ResumeWfRunRequest\x1a\x16.google.protobuf.Empty\"\x00\x12L\n\x0fRescueThreadRun\x12#.littlehorse.RescueThreadRunRequest\x1a\x12.littlehorse.WfRun\"\x00\x12H\n\x0b\x44\x65leteWfRun\x12\x1f.littlehorse.DeleteWfRunRequest\x1a\x16.google.protobuf.Empty\"\x00\x12L\n\rDeleteTaskDef\x12!.littlehorse.DeleteTaskDefRequest\x1a\x16.google.protobuf.Empty\"\x00\x12J\n\x0c\x44\x65leteWfSpec\x12 .littlehorse.DeleteWfSpecRequest\x1a\x16.google.protobuf.Empty\"\x00\x12T\n\x11\x44\x65leteUserTaskDef\x12%.littlehorse.DeleteUserTaskDefRequest\x1a\x16.google.protobuf.Empty\"\x00\x12^\n\x16\x44\x65leteExternalEventDef\x12*.littlehorse.DeleteExternalEventDefRequest\x1a\x16.google.protobuf.Empty\"\x00\x12P\n\x0f\x44\x65letePrincipal\x12#.littlehorse.DeletePrincipalRequest\x1a\x16.google.protobuf.Empty\"\x00\x12\x61\n\x17GetTaskDefMetricsWindow\x12\'.littlehorse.TaskDefMetricsQueryRequest\x1a\x1b.littlehorse.TaskDefMetrics\"\x00\x12^\n\x16GetWfSpecMetricsWindow\x12&.littlehorse.WfSpecMetricsQueryRequest\x1a\x1a.littlehorse.WfSpecMetrics\"\x00\x12\x61\n\x12ListTaskDefMetrics\x12#.littlehorse.ListTaskMetricsRequest\x1a$.littlehorse.ListTaskMetricsResponse\"\x00\x12\\\n\x11ListWfSpecMetrics\x12!.littlehorse.ListWfMetricsRequest\x1a\".littlehorse.ListWfMetricsResponse\"\x00\x12\x41\n\tPutTenant\x12\x1d.littlehorse.PutTenantRequest\x1a\x13.littlehorse.Tenant\"\x00\x12\x39\n\tGetTenant\x12\x15.littlehorse.TenantId\x1a\x13.littlehorse.Tenant\"\x00\x12J\n\x0cPutPrincipal\x12 .littlehorse.PutPrincipalRequest\x1a\x16.littlehorse.Principal\"\x00\x12:\n\x06Whoami\x12\x16.google.protobuf.Empty\x1a\x16.littlehorse.Principal\"\x00\x12P\n\x10GetServerVersion\x12\x16.google.protobuf.Empty\x1a\".littlehorse.ServerVersionResponse\"\x00\x42G\n\x1fio.littlehorse.sdk.common.protoP\x01Z\x07.;model\xaa\x02\x18LittleHorse.Common.Protob\x06proto3')

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
  _SCHEDULEWFREQUEST_VARIABLESENTRY._options = None
  _SCHEDULEWFREQUEST_VARIABLESENTRY._serialized_options = b'8\001'
  _TASKWORKERGROUP_TASKWORKERSENTRY._options = None
  _TASKWORKERGROUP_TASKWORKERSENTRY._serialized_options = b'8\001'
  _globals['_ALLOWEDUPDATETYPE']._serialized_start=12248
  _globals['_ALLOWEDUPDATETYPE']._serialized_end=12328
  _globals['_GETLATESTUSERTASKDEFREQUEST']._serialized_start=341
  _globals['_GETLATESTUSERTASKDEFREQUEST']._serialized_end=384
  _globals['_PUTWFSPECREQUEST']._serialized_start=387
  _globals['_PUTWFSPECREQUEST']._serialized_end=854
  _globals['_PUTWFSPECREQUEST_THREADSPECSENTRY']._serialized_start=721
  _globals['_PUTWFSPECREQUEST_THREADSPECSENTRY']._serialized_end=796
  _globals['_PUTTASKDEFREQUEST']._serialized_start=856
  _globals['_PUTTASKDEFREQUEST']._serialized_end=935
  _globals['_PUTWORKFLOWEVENTDEFREQUEST']._serialized_start=937
  _globals['_PUTWORKFLOWEVENTDEFREQUEST']._serialized_end=1020
  _globals['_PUTUSERTASKDEFREQUEST']._serialized_start=1022
  _globals['_PUTUSERTASKDEFREQUEST']._serialized_end=1145
  _globals['_PUTEXTERNALEVENTDEFREQUEST']._serialized_start=1147
  _globals['_PUTEXTERNALEVENTDEFREQUEST']._serialized_end=1258
  _globals['_PUTEXTERNALEVENTREQUEST']._serialized_start=1261
  _globals['_PUTEXTERNALEVENTREQUEST']._serialized_end=1584
  _globals['_DELETEEXTERNALEVENTREQUEST']._serialized_start=1586
  _globals['_DELETEEXTERNALEVENTREQUEST']._serialized_end=1656
  _globals['_DELETEWFRUNREQUEST']._serialized_start=1658
  _globals['_DELETEWFRUNREQUEST']._serialized_end=1712
  _globals['_DELETETASKDEFREQUEST']._serialized_start=1714
  _globals['_DELETETASKDEFREQUEST']._serialized_end=1772
  _globals['_DELETEUSERTASKDEFREQUEST']._serialized_start=1774
  _globals['_DELETEUSERTASKDEFREQUEST']._serialized_end=1840
  _globals['_DELETEWFSPECREQUEST']._serialized_start=1842
  _globals['_DELETEWFSPECREQUEST']._serialized_end=1898
  _globals['_DELETEEXTERNALEVENTDEFREQUEST']._serialized_start=1900
  _globals['_DELETEEXTERNALEVENTDEFREQUEST']._serialized_end=1976
  _globals['_RUNWFREQUEST']._serialized_start=1979
  _globals['_RUNWFREQUEST']._serialized_end=2334
  _globals['_RUNWFREQUEST_VARIABLESENTRY']._serialized_start=2199
  _globals['_RUNWFREQUEST_VARIABLESENTRY']._serialized_end=2275
  _globals['_SCHEDULEWFREQUEST']._serialized_start=2337
  _globals['_SCHEDULEWFREQUEST']._serialized_end=2727
  _globals['_SCHEDULEWFREQUEST_VARIABLESENTRY']._serialized_start=2199
  _globals['_SCHEDULEWFREQUEST_VARIABLESENTRY']._serialized_end=2275
  _globals['_VARIABLEMATCH']._serialized_start=2729
  _globals['_VARIABLEMATCH']._serialized_end=2805
  _globals['_AWAITWORKFLOWEVENTREQUEST']._serialized_start=2808
  _globals['_AWAITWORKFLOWEVENTREQUEST']._serialized_end=2997
  _globals['_SEARCHWFRUNREQUEST']._serialized_start=3000
  _globals['_SEARCHWFRUNREQUEST']._serialized_end=3479
  _globals['_WFRUNIDLIST']._serialized_start=3481
  _globals['_WFRUNIDLIST']._serialized_end=3569
  _globals['_SEARCHTASKRUNREQUEST']._serialized_start=3572
  _globals['_SEARCHTASKRUNREQUEST']._serialized_end=3888
  _globals['_TASKRUNIDLIST']._serialized_start=3890
  _globals['_TASKRUNIDLIST']._serialized_end=3982
  _globals['_SEARCHNODERUNREQUEST']._serialized_start=3985
  _globals['_SEARCHNODERUNREQUEST']._serialized_end=4482
  _globals['_SEARCHNODERUNREQUEST_NODETYPE']._serialized_start=4267
  _globals['_SEARCHNODERUNREQUEST_NODETYPE']._serialized_end=4423
  _globals['_NODERUNIDLIST']._serialized_start=4484
  _globals['_NODERUNIDLIST']._serialized_end=4576
  _globals['_SEARCHUSERTASKRUNREQUEST']._serialized_start=4579
  _globals['_SEARCHUSERTASKRUNREQUEST']._serialized_end=5013
  _globals['_USERTASKRUNIDLIST']._serialized_start=5015
  _globals['_USERTASKRUNIDLIST']._serialized_end=5115
  _globals['_SEARCHVARIABLEREQUEST']._serialized_start=5118
  _globals['_SEARCHVARIABLEREQUEST']._serialized_end=5404
  _globals['_VARIABLEIDLIST']._serialized_start=5406
  _globals['_VARIABLEIDLIST']._serialized_end=5500
  _globals['_SEARCHTASKDEFREQUEST']._serialized_start=5502
  _globals['_SEARCHTASKDEFREQUEST']._serialized_end=5622
  _globals['_TASKDEFIDLIST']._serialized_start=5624
  _globals['_TASKDEFIDLIST']._serialized_end=5716
  _globals['_SEARCHUSERTASKDEFREQUEST']._serialized_start=5719
  _globals['_SEARCHUSERTASKDEFREQUEST']._serialized_end=5871
  _globals['_USERTASKDEFIDLIST']._serialized_start=5873
  _globals['_USERTASKDEFIDLIST']._serialized_end=5973
  _globals['_SEARCHWFSPECREQUEST']._serialized_start=5976
  _globals['_SEARCHWFSPECREQUEST']._serialized_end=6142
  _globals['_WFSPECIDLIST']._serialized_start=6144
  _globals['_WFSPECIDLIST']._serialized_end=6234
  _globals['_SEARCHEXTERNALEVENTDEFREQUEST']._serialized_start=6237
  _globals['_SEARCHEXTERNALEVENTDEFREQUEST']._serialized_end=6366
  _globals['_EXTERNALEVENTDEFIDLIST']._serialized_start=6368
  _globals['_EXTERNALEVENTDEFIDLIST']._serialized_end=6478
  _globals['_SEARCHTENANTREQUEST']._serialized_start=6480
  _globals['_SEARCHTENANTREQUEST']._serialized_end=6567
  _globals['_TENANTIDLIST']._serialized_start=6569
  _globals['_TENANTIDLIST']._serialized_end=6659
  _globals['_SEARCHPRINCIPALREQUEST']._serialized_start=6662
  _globals['_SEARCHPRINCIPALREQUEST']._serialized_end=6961
  _globals['_PRINCIPALIDLIST']._serialized_start=6963
  _globals['_PRINCIPALIDLIST']._serialized_end=7059
  _globals['_SEARCHEXTERNALEVENTREQUEST']._serialized_start=7062
  _globals['_SEARCHEXTERNALEVENTREQUEST']._serialized_end=7408
  _globals['_EXTERNALEVENTIDLIST']._serialized_start=7410
  _globals['_EXTERNALEVENTIDLIST']._serialized_end=7514
  _globals['_LISTNODERUNSREQUEST']._serialized_start=7517
  _globals['_LISTNODERUNSREQUEST']._serialized_end=7699
  _globals['_NODERUNLIST']._serialized_start=7701
  _globals['_NODERUNLIST']._serialized_end=7789
  _globals['_LISTVARIABLESREQUEST']._serialized_start=7791
  _globals['_LISTVARIABLESREQUEST']._serialized_end=7854
  _globals['_VARIABLELIST']._serialized_start=7856
  _globals['_VARIABLELIST']._serialized_end=7910
  _globals['_LISTEXTERNALEVENTSREQUEST']._serialized_start=7912
  _globals['_LISTEXTERNALEVENTSREQUEST']._serialized_end=7980
  _globals['_EXTERNALEVENTLIST']._serialized_start=7982
  _globals['_EXTERNALEVENTLIST']._serialized_end=8046
  _globals['_REGISTERTASKWORKERREQUEST']._serialized_start=8048
  _globals['_REGISTERTASKWORKERREQUEST']._serialized_end=8167
  _globals['_TASKWORKERHEARTBEATREQUEST']._serialized_start=8169
  _globals['_TASKWORKERHEARTBEATREQUEST']._serialized_end=8284
  _globals['_REGISTERTASKWORKERRESPONSE']._serialized_start=8287
  _globals['_REGISTERTASKWORKERRESPONSE']._serialized_end=8416
  _globals['_LHHOSTINFO']._serialized_start=8418
  _globals['_LHHOSTINFO']._serialized_end=8458
  _globals['_POLLTASKREQUEST']._serialized_start=8461
  _globals['_POLLTASKREQUEST']._serialized_end=8600
  _globals['_SCHEDULEDTASK']._serialized_start=8603
  _globals['_SCHEDULEDTASK']._serialized_end=8871
  _globals['_POLLTASKRESPONSE']._serialized_start=8873
  _globals['_POLLTASKRESPONSE']._serialized_end=8951
  _globals['_REPORTTASKRUN']._serialized_start=8954
  _globals['_REPORTTASKRUN']._serialized_end=9339
  _globals['_STOPWFRUNREQUEST']._serialized_start=9341
  _globals['_STOPWFRUNREQUEST']._serialized_end=9427
  _globals['_RESUMEWFRUNREQUEST']._serialized_start=9429
  _globals['_RESUMEWFRUNREQUEST']._serialized_end=9517
  _globals['_RESCUETHREADRUNREQUEST']._serialized_start=9519
  _globals['_RESCUETHREADRUNREQUEST']._serialized_end=9638
  _globals['_TASKDEFMETRICSQUERYREQUEST']._serialized_start=9641
  _globals['_TASKDEFMETRICSQUERYREQUEST']._serialized_end=9820
  _globals['_LISTTASKMETRICSREQUEST']._serialized_start=9823
  _globals['_LISTTASKMETRICSREQUEST']._serialized_end=10025
  _globals['_LISTTASKMETRICSRESPONSE']._serialized_start=10027
  _globals['_LISTTASKMETRICSRESPONSE']._serialized_end=10098
  _globals['_WFSPECMETRICSQUERYREQUEST']._serialized_start=10101
  _globals['_WFSPECMETRICSQUERYREQUEST']._serialized_end=10278
  _globals['_LISTWFMETRICSREQUEST']._serialized_start=10281
  _globals['_LISTWFMETRICSREQUEST']._serialized_end=10479
  _globals['_LISTWFMETRICSRESPONSE']._serialized_start=10481
  _globals['_LISTWFMETRICSRESPONSE']._serialized_end=10549
  _globals['_TASKDEFMETRICS']._serialized_start=10552
  _globals['_TASKDEFMETRICS']._serialized_end=10931
  _globals['_WFSPECMETRICS']._serialized_start=10934
  _globals['_WFSPECMETRICS']._serialized_end=11223
  _globals['_LISTUSERTASKRUNREQUEST']._serialized_start=11225
  _globals['_LISTUSERTASKRUNREQUEST']._serialized_end=11290
  _globals['_USERTASKRUNLIST']._serialized_start=11292
  _globals['_USERTASKRUNLIST']._serialized_end=11352
  _globals['_TASKWORKERMETADATA']._serialized_start=11355
  _globals['_TASKWORKERMETADATA']._serialized_end=11493
  _globals['_TASKWORKERGROUP']._serialized_start=11496
  _globals['_TASKWORKERGROUP']._serialized_end=11759
  _globals['_TASKWORKERGROUP_TASKWORKERSENTRY']._serialized_start=11676
  _globals['_TASKWORKERGROUP_TASKWORKERSENTRY']._serialized_end=11759
  _globals['_LISTTASKRUNSREQUEST']._serialized_start=11761
  _globals['_LISTTASKRUNSREQUEST']._serialized_end=11823
  _globals['_TASKRUNLIST']._serialized_start=11825
  _globals['_TASKRUNLIST']._serialized_end=11877
  _globals['_MIGRATEWFSPECREQUEST']._serialized_start=11879
  _globals['_MIGRATEWFSPECREQUEST']._serialized_end=12001
  _globals['_GETLATESTWFSPECREQUEST']._serialized_start=12003
  _globals['_GETLATESTWFSPECREQUEST']._serialized_end=12087
  _globals['_SERVERVERSIONRESPONSE']._serialized_start=12090
  _globals['_SERVERVERSIONRESPONSE']._serialized_end=12246
  _globals['_LITTLEHORSE']._serialized_start=12331
  _globals['_LITTLEHORSE']._serialized_end=17513
# @@protoc_insertion_point(module_scope)
