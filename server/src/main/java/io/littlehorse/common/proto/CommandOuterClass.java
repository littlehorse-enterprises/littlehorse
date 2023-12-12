// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: command.proto

package io.littlehorse.common.proto;

public final class CommandOuterClass {
  private CommandOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_Command_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_Command_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_MetadataCommand_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_MetadataCommand_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_RepartitionCommandPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_RepartitionCommandPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_TaskMetricUpdatePb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_TaskMetricUpdatePb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_WfMetricUpdatePb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_WfMetricUpdatePb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_CreateRemoteTagPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_CreateRemoteTagPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_RemoveRemoteTagPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_RemoveRemoteTagPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_TaskClaimEventPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_TaskClaimEventPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_ExternalEventNodeTimeoutPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_ExternalEventNodeTimeoutPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_SleepNodeMaturedPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_SleepNodeMaturedPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_TriggeredTaskRunPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_TriggeredTaskRunPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_DeadlineReassignUserTask_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_DeadlineReassignUserTask_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_LHTimerPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_LHTimerPb_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\rcommand.proto\022\013littlehorse\032\037google/pro" +
      "tobuf/timestamp.proto\032\rservice.proto\032\017ob" +
      "ject_id.proto\032\023common_wfspec.proto\032\020user" +
      "_tasks.proto\032\022common_enums.proto\032\rstorag" +
      "e.proto\032\nacls.proto\032\tjob.proto\"\235\t\n\007Comma" +
      "nd\022(\n\004time\030\001 \001(\0132\032.google.protobuf.Times" +
      "tamp\022\027\n\ncommand_id\030\002 \001(\tH\001\210\001\001\0225\n\017report_" +
      "task_run\030\004 \001(\0132\032.littlehorse.ReportTaskR" +
      "unH\000\0229\n\020task_claim_event\030\005 \001(\0132\035.littleh" +
      "orse.TaskClaimEventPbH\000\022+\n\006run_wf\030\006 \001(\0132" +
      "\031.littlehorse.RunWfRequestH\000\022B\n\022put_exte" +
      "rnal_event\030\007 \001(\0132$.littlehorse.PutExtern" +
      "alEventRequestH\000\0224\n\013stop_wf_run\030\010 \001(\0132\035." +
      "littlehorse.StopWfRunRequestH\000\0228\n\rresume" +
      "_wf_run\030\t \001(\0132\037.littlehorse.ResumeWfRunR" +
      "equestH\000\022=\n\022sleep_node_matured\030\n \001(\0132\037.l" +
      "ittlehorse.SleepNodeMaturedPbH\000\0228\n\rdelet" +
      "e_wf_run\030\013 \001(\0132\037.littlehorse.DeleteWfRun" +
      "RequestH\000\022I\n\026external_event_timeout\030\014 \001(" +
      "\0132\'.littlehorse.ExternalEventNodeTimeout" +
      "PbH\000\022I\n\026task_worker_heart_beat\030\r \001(\0132\'.l" +
      "ittlehorse.TaskWorkerHeartBeatRequestH\000\022" +
      "H\n\025delete_external_event\030\016 \001(\0132\'.littleh" +
      "orse.DeleteExternalEventRequestH\000\022E\n\024ass" +
      "ign_user_task_run\030\017 \001(\0132%.littlehorse.As" +
      "signUserTaskRunRequestH\000\022I\n\026complete_use" +
      "r_task_run\030\020 \001(\0132\'.littlehorse.CompleteU" +
      "serTaskRunRequestH\000\022=\n\022triggered_task_ru" +
      "n\030\021 \001(\0132\037.littlehorse.TriggeredTaskRunPb" +
      "H\000\022E\n\024reassigned_user_task\030\022 \001(\0132%.littl" +
      "ehorse.DeadlineReassignUserTaskH\000\022A\n\020can" +
      "cel_user_task\030\023 \001(\0132%.littlehorse.Cancel" +
      "UserTaskRunRequestH\000\022.\n\010bulk_job\030\024 \001(\0132\032" +
      ".littlehorse.BulkUpdateJobH\000B\t\n\007commandB" +
      "\r\n\013_command_id\"\264\006\n\017MetadataCommand\022(\n\004ti" +
      "me\030\001 \001(\0132\032.google.protobuf.Timestamp\022\027\n\n" +
      "command_id\030\002 \001(\tH\001\210\001\001\0224\n\013put_wf_spec\030\004 \001" +
      "(\0132\035.littlehorse.PutWfSpecRequestH\000\0226\n\014p" +
      "ut_task_def\030\005 \001(\0132\036.littlehorse.PutTaskD" +
      "efRequestH\000\022I\n\026put_external_event_def\030\006 " +
      "\001(\0132\'.littlehorse.PutExternalEventDefReq" +
      "uestH\000\022:\n\016delete_wf_spec\030\007 \001(\0132 .littleh" +
      "orse.DeleteWfSpecRequestH\000\022<\n\017delete_tas" +
      "k_def\030\010 \001(\0132!.littlehorse.DeleteTaskDefR" +
      "equestH\000\022O\n\031delete_external_event_def\030\t " +
      "\001(\0132*.littlehorse.DeleteExternalEventDef" +
      "RequestH\000\022?\n\021put_user_task_def\030\n \001(\0132\".l" +
      "ittlehorse.PutUserTaskDefRequestH\000\022E\n\024de" +
      "lete_user_task_def\030\013 \001(\0132%.littlehorse.D" +
      "eleteUserTaskDefRequestH\000\0229\n\rput_princip" +
      "al\030\014 \001(\0132 .littlehorse.PutPrincipalReque" +
      "stH\000\022?\n\020delete_principal\030\r \001(\0132#.littleh" +
      "orse.DeletePrincipalRequestH\000\0223\n\nput_ten" +
      "ant\030\016 \001(\0132\035.littlehorse.PutTenantRequest" +
      "H\000B\022\n\020metadata_commandB\r\n\013_command_id\"\363\002" +
      "\n\024RepartitionCommandPb\022(\n\004time\030\001 \001(\0132\032.g" +
      "oogle.protobuf.Timestamp\022\027\n\ncommand_id\030\002" +
      " \001(\tH\001\210\001\001\022=\n\022task_metric_update\030\003 \001(\0132\037." +
      "littlehorse.TaskMetricUpdatePbH\000\0229\n\020wf_m" +
      "etric_update\030\004 \001(\0132\035.littlehorse.WfMetri" +
      "cUpdatePbH\000\022;\n\021create_remote_tag\030\005 \001(\0132\036" +
      ".littlehorse.CreateRemoteTagPbH\000\022;\n\021remo" +
      "ve_remote_tag\030\006 \001(\0132\036.littlehorse.Remove" +
      "RemoteTagPbH\000B\025\n\023repartition_commandB\r\n\013" +
      "_command_id\"\230\003\n\022TaskMetricUpdatePb\022+\n\013ta" +
      "sk_def_id\030\001 \001(\0132\026.littlehorse.TaskDefId\022" +
      "0\n\014window_start\030\002 \001(\0132\032.google.protobuf." +
      "Timestamp\022.\n\004type\030\003 \001(\0162 .littlehorse.Me" +
      "tricsWindowLength\022\023\n\013num_entries\030\004 \001(\003\022\035" +
      "\n\025schedule_to_start_max\030\005 \001(\003\022\037\n\027schedul" +
      "e_to_start_total\030\006 \001(\003\022\035\n\025start_to_compl" +
      "ete_max\030\007 \001(\003\022\037\n\027start_to_complete_total" +
      "\030\010 \001(\003\022\027\n\017total_completed\030\t \001(\003\022\025\n\rtotal" +
      "_errored\030\n \001(\003\022\025\n\rtotal_started\030\013 \001(\003\022\027\n" +
      "\017total_scheduled\030\014 \001(\003\"\273\002\n\020WfMetricUpdat" +
      "ePb\022)\n\nwf_spec_id\030\001 \001(\0132\025.littlehorse.Wf" +
      "SpecId\0220\n\014window_start\030\002 \001(\0132\032.google.pr" +
      "otobuf.Timestamp\022.\n\004type\030\003 \001(\0162 .littleh" +
      "orse.MetricsWindowLength\022\023\n\013num_entries\030" +
      "\004 \001(\003\022\035\n\025start_to_complete_max\030\005 \001(\003\022\037\n\027" +
      "start_to_complete_total\030\006 \001(\003\022\027\n\017total_c" +
      "ompleted\030\007 \001(\003\022\025\n\rtotal_errored\030\010 \001(\003\022\025\n" +
      "\rtotal_started\030\t \001(\003\"4\n\021CreateRemoteTagP" +
      "b\022\037\n\003tag\030\001 \001(\0132\022.littlehorse.TagPb\"=\n\021Re" +
      "moveRemoteTagPb\022\021\n\tstore_key\030\001 \001(\t\022\025\n\rpa" +
      "rtition_key\030\002 \001(\t\"\273\001\n\020TaskClaimEventPb\022+" +
      "\n\013task_run_id\030\001 \001(\0132\026.littlehorse.TaskRu" +
      "nId\022(\n\004time\030\002 \001(\0132\032.google.protobuf.Time" +
      "stamp\022\026\n\016task_worker_id\030\003 \001(\t\022 \n\023task_wo" +
      "rker_version\030\004 \001(\tH\000\210\001\001B\026\n\024_task_worker_" +
      "version\"I\n\032ExternalEventNodeTimeoutPb\022+\n" +
      "\013node_run_id\030\001 \001(\0132\026.littlehorse.NodeRun" +
      "Id\"A\n\022SleepNodeMaturedPb\022+\n\013node_run_id\030" +
      "\001 \001(\0132\026.littlehorse.NodeRunId\"m\n\022Trigger" +
      "edTaskRunPb\022/\n\020task_to_schedule\030\001 \001(\0132\025." +
      "littlehorse.TaskNode\022&\n\006source\030\002 \001(\0132\026.l" +
      "ittlehorse.NodeRunId\"\345\001\n\030DeadlineReassig" +
      "nUserTask\0229\n\013new_user_id\030\001 \001(\0132\037.littleh" +
      "orse.VariableAssignmentH\000\210\001\001\022<\n\016new_user" +
      "_group\030\002 \001(\0132\037.littlehorse.VariableAssig" +
      "nmentH\001\210\001\001\022-\n\tuser_task\030\004 \001(\0132\032.littleho" +
      "rse.UserTaskRunIdB\016\n\014_new_user_idB\021\n\017_ne" +
      "w_user_group\"\277\001\n\tLHTimerPb\0223\n\017maturation" +
      "_time\030\001 \001(\0132\032.google.protobuf.Timestamp\022" +
      "\013\n\003key\030\002 \001(\t\022\r\n\005topic\030\003 \001(\t\022\017\n\007payload\030\004" +
      " \001(\014\022\026\n\ttenant_id\030\005 \001(\tH\000\210\001\001\022\031\n\014principa" +
      "l_id\030\006 \001(\tH\001\210\001\001B\014\n\n_tenant_idB\017\n\r_princi" +
      "pal_idB\037\n\033io.littlehorse.common.protoP\001b" +
      "\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          io.littlehorse.sdk.common.proto.Service.getDescriptor(),
          io.littlehorse.sdk.common.proto.ObjectId.getDescriptor(),
          io.littlehorse.sdk.common.proto.CommonWfspec.getDescriptor(),
          io.littlehorse.sdk.common.proto.UserTasks.getDescriptor(),
          io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor(),
          io.littlehorse.common.proto.Storage.getDescriptor(),
          io.littlehorse.common.proto.Acls.getDescriptor(),
          io.littlehorse.common.proto.Job.getDescriptor(),
        });
    internal_static_littlehorse_Command_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_Command_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_Command_descriptor,
        new java.lang.String[] { "Time", "CommandId", "ReportTaskRun", "TaskClaimEvent", "RunWf", "PutExternalEvent", "StopWfRun", "ResumeWfRun", "SleepNodeMatured", "DeleteWfRun", "ExternalEventTimeout", "TaskWorkerHeartBeat", "DeleteExternalEvent", "AssignUserTaskRun", "CompleteUserTaskRun", "TriggeredTaskRun", "ReassignedUserTask", "CancelUserTask", "BulkJob", "Command", "CommandId", });
    internal_static_littlehorse_MetadataCommand_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_MetadataCommand_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_MetadataCommand_descriptor,
        new java.lang.String[] { "Time", "CommandId", "PutWfSpec", "PutTaskDef", "PutExternalEventDef", "DeleteWfSpec", "DeleteTaskDef", "DeleteExternalEventDef", "PutUserTaskDef", "DeleteUserTaskDef", "PutPrincipal", "DeletePrincipal", "PutTenant", "MetadataCommand", "CommandId", });
    internal_static_littlehorse_RepartitionCommandPb_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_littlehorse_RepartitionCommandPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_RepartitionCommandPb_descriptor,
        new java.lang.String[] { "Time", "CommandId", "TaskMetricUpdate", "WfMetricUpdate", "CreateRemoteTag", "RemoveRemoteTag", "RepartitionCommand", "CommandId", });
    internal_static_littlehorse_TaskMetricUpdatePb_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_littlehorse_TaskMetricUpdatePb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_TaskMetricUpdatePb_descriptor,
        new java.lang.String[] { "TaskDefId", "WindowStart", "Type", "NumEntries", "ScheduleToStartMax", "ScheduleToStartTotal", "StartToCompleteMax", "StartToCompleteTotal", "TotalCompleted", "TotalErrored", "TotalStarted", "TotalScheduled", });
    internal_static_littlehorse_WfMetricUpdatePb_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_littlehorse_WfMetricUpdatePb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_WfMetricUpdatePb_descriptor,
        new java.lang.String[] { "WfSpecId", "WindowStart", "Type", "NumEntries", "StartToCompleteMax", "StartToCompleteTotal", "TotalCompleted", "TotalErrored", "TotalStarted", });
    internal_static_littlehorse_CreateRemoteTagPb_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_littlehorse_CreateRemoteTagPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_CreateRemoteTagPb_descriptor,
        new java.lang.String[] { "Tag", });
    internal_static_littlehorse_RemoveRemoteTagPb_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_littlehorse_RemoveRemoteTagPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_RemoveRemoteTagPb_descriptor,
        new java.lang.String[] { "StoreKey", "PartitionKey", });
    internal_static_littlehorse_TaskClaimEventPb_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_littlehorse_TaskClaimEventPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_TaskClaimEventPb_descriptor,
        new java.lang.String[] { "TaskRunId", "Time", "TaskWorkerId", "TaskWorkerVersion", "TaskWorkerVersion", });
    internal_static_littlehorse_ExternalEventNodeTimeoutPb_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_littlehorse_ExternalEventNodeTimeoutPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_ExternalEventNodeTimeoutPb_descriptor,
        new java.lang.String[] { "NodeRunId", });
    internal_static_littlehorse_SleepNodeMaturedPb_descriptor =
      getDescriptor().getMessageTypes().get(9);
    internal_static_littlehorse_SleepNodeMaturedPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_SleepNodeMaturedPb_descriptor,
        new java.lang.String[] { "NodeRunId", });
    internal_static_littlehorse_TriggeredTaskRunPb_descriptor =
      getDescriptor().getMessageTypes().get(10);
    internal_static_littlehorse_TriggeredTaskRunPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_TriggeredTaskRunPb_descriptor,
        new java.lang.String[] { "TaskToSchedule", "Source", });
    internal_static_littlehorse_DeadlineReassignUserTask_descriptor =
      getDescriptor().getMessageTypes().get(11);
    internal_static_littlehorse_DeadlineReassignUserTask_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_DeadlineReassignUserTask_descriptor,
        new java.lang.String[] { "NewUserId", "NewUserGroup", "UserTask", "NewUserId", "NewUserGroup", });
    internal_static_littlehorse_LHTimerPb_descriptor =
      getDescriptor().getMessageTypes().get(12);
    internal_static_littlehorse_LHTimerPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_LHTimerPb_descriptor,
        new java.lang.String[] { "MaturationTime", "Key", "Topic", "Payload", "TenantId", "PrincipalId", "TenantId", "PrincipalId", });
    com.google.protobuf.TimestampProto.getDescriptor();
    io.littlehorse.sdk.common.proto.Service.getDescriptor();
    io.littlehorse.sdk.common.proto.ObjectId.getDescriptor();
    io.littlehorse.sdk.common.proto.CommonWfspec.getDescriptor();
    io.littlehorse.sdk.common.proto.UserTasks.getDescriptor();
    io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor();
    io.littlehorse.common.proto.Storage.getDescriptor();
    io.littlehorse.common.proto.Acls.getDescriptor();
    io.littlehorse.common.proto.Job.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
