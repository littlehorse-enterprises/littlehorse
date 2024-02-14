// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: user_tasks.proto

package io.littlehorse.sdk.common.proto;

public final class UserTasks {
  private UserTasks() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_UserTaskDef_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_UserTaskDef_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_UserTaskField_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_UserTaskField_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_UserTaskRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_UserTaskRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_UserTaskRun_ResultsEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_UserTaskRun_ResultsEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_AssignUserTaskRunRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_AssignUserTaskRunRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_CompleteUserTaskRunRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_CompleteUserTaskRunRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_CompleteUserTaskRunRequest_ResultsEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_CompleteUserTaskRunRequest_ResultsEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_CancelUserTaskRunRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_CancelUserTaskRunRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_UserTaskTriggerReference_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_UserTaskTriggerReference_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_UserTaskEvent_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_UserTaskEvent_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_UserTaskEvent_UTECancelled_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_UserTaskEvent_UTECancelled_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_UserTaskEvent_UTETaskExecuted_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_UserTaskEvent_UTETaskExecuted_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_UserTaskEvent_UTEAssigned_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_UserTaskEvent_UTEAssigned_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\020user_tasks.proto\022\013littlehorse\032\037google/" +
      "protobuf/timestamp.proto\032\022common_enums.p" +
      "roto\032\017object_id.proto\032\016variable.proto\"\262\001" +
      "\n\013UserTaskDef\022\014\n\004name\030\001 \001(\t\022\017\n\007version\030\002" +
      " \001(\005\022\030\n\013description\030\003 \001(\tH\000\210\001\001\022*\n\006fields" +
      "\030\004 \003(\0132\032.littlehorse.UserTaskField\022.\n\ncr" +
      "eated_at\030\005 \001(\0132\032.google.protobuf.Timesta" +
      "mpB\016\n\014_description\"\230\001\n\rUserTaskField\022\014\n\004" +
      "name\030\001 \001(\t\022\'\n\004type\030\002 \001(\0162\031.littlehorse.V" +
      "ariableType\022\030\n\013description\030\003 \001(\tH\000\210\001\001\022\024\n" +
      "\014display_name\030\004 \001(\t\022\020\n\010required\030\005 \001(\010B\016\n" +
      "\014_description\"\243\004\n\013UserTaskRun\022&\n\002id\030\001 \001(" +
      "\0132\032.littlehorse.UserTaskRunId\0224\n\020user_ta" +
      "sk_def_id\030\002 \001(\0132\032.littlehorse.UserTaskDe" +
      "fId\022\027\n\nuser_group\030\003 \001(\tH\000\210\001\001\022\024\n\007user_id\030" +
      "\004 \001(\tH\001\210\001\001\0226\n\007results\030\006 \003(\0132%.littlehors" +
      "e.UserTaskRun.ResultsEntry\022.\n\006status\030\007 \001" +
      "(\0162\036.littlehorse.UserTaskRunStatus\022*\n\006ev" +
      "ents\030\010 \003(\0132\032.littlehorse.UserTaskEvent\022\022" +
      "\n\005notes\030\t \001(\tH\002\210\001\001\0222\n\016scheduled_time\030\n \001" +
      "(\0132\032.google.protobuf.Timestamp\022+\n\013node_r" +
      "un_id\030\013 \001(\0132\026.littlehorse.NodeRunId\022\r\n\005e" +
      "poch\030\014 \001(\003\032J\n\014ResultsEntry\022\013\n\003key\030\001 \001(\t\022" +
      ")\n\005value\030\002 \001(\0132\032.littlehorse.VariableVal" +
      "ue:\0028\001B\r\n\013_user_groupB\n\n\010_user_idB\010\n\006_no" +
      "tes\"\262\001\n\030AssignUserTaskRunRequest\0224\n\020user" +
      "_task_run_id\030\001 \001(\0132\032.littlehorse.UserTas" +
      "kRunId\022\026\n\016override_claim\030\002 \001(\010\022\027\n\nuser_g" +
      "roup\030\003 \001(\tH\000\210\001\001\022\024\n\007user_id\030\004 \001(\tH\001\210\001\001B\r\n" +
      "\013_user_groupB\n\n\010_user_id\"\366\001\n\032CompleteUse" +
      "rTaskRunRequest\0224\n\020user_task_run_id\030\001 \001(" +
      "\0132\032.littlehorse.UserTaskRunId\022E\n\007results" +
      "\030\002 \003(\01324.littlehorse.CompleteUserTaskRun" +
      "Request.ResultsEntry\022\017\n\007user_id\030\003 \001(\t\032J\n" +
      "\014ResultsEntry\022\013\n\003key\030\001 \001(\t\022)\n\005value\030\002 \001(" +
      "\0132\032.littlehorse.VariableValue:\0028\001\"P\n\030Can" +
      "celUserTaskRunRequest\0224\n\020user_task_run_i" +
      "d\030\001 \001(\0132\032.littlehorse.UserTaskRunId\"\261\001\n\030" +
      "UserTaskTriggerReference\022+\n\013node_run_id\030" +
      "\001 \001(\0132\026.littlehorse.NodeRunId\022\036\n\026user_ta" +
      "sk_event_number\030\002 \001(\005\022\024\n\007user_id\030\003 \001(\tH\000" +
      "\210\001\001\022\027\n\nuser_group\030\004 \001(\tH\001\210\001\001B\n\n\010_user_id" +
      "B\r\n\013_user_group\"\222\004\n\rUserTaskEvent\022(\n\004tim" +
      "e\030\001 \001(\0132\032.google.protobuf.Timestamp\022C\n\rt" +
      "ask_executed\030\002 \001(\0132*.littlehorse.UserTas" +
      "kEvent.UTETaskExecutedH\000\022:\n\010assigned\030\003 \001" +
      "(\0132&.littlehorse.UserTaskEvent.UTEAssign" +
      "edH\000\022<\n\tcancelled\030\004 \001(\0132\'.littlehorse.Us" +
      "erTaskEvent.UTECancelledH\000\032\016\n\014UTECancell" +
      "ed\032;\n\017UTETaskExecuted\022(\n\010task_run\030\001 \001(\0132" +
      "\026.littlehorse.TaskRunId\032\301\001\n\013UTEAssigned\022" +
      "\030\n\013old_user_id\030\001 \001(\tH\000\210\001\001\022\033\n\016old_user_gr" +
      "oup\030\002 \001(\tH\001\210\001\001\022\030\n\013new_user_id\030\003 \001(\tH\002\210\001\001" +
      "\022\033\n\016new_user_group\030\004 \001(\tH\003\210\001\001B\016\n\014_old_us" +
      "er_idB\021\n\017_old_user_groupB\016\n\014_new_user_id" +
      "B\021\n\017_new_user_groupB\007\n\005event*J\n\021UserTask" +
      "RunStatus\022\016\n\nUNASSIGNED\020\000\022\014\n\010ASSIGNED\020\001\022" +
      "\010\n\004DONE\020\003\022\r\n\tCANCELLED\020\004BG\n\037io.littlehor" +
      "se.sdk.common.protoP\001Z\007.;model\252\002\030LittleH" +
      "orse.Common.Protob\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor(),
          io.littlehorse.sdk.common.proto.ObjectId.getDescriptor(),
          io.littlehorse.sdk.common.proto.VariableOuterClass.getDescriptor(),
        });
    internal_static_littlehorse_UserTaskDef_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_UserTaskDef_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_UserTaskDef_descriptor,
        new java.lang.String[] { "Name", "Version", "Description", "Fields", "CreatedAt", "Description", });
    internal_static_littlehorse_UserTaskField_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_UserTaskField_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_UserTaskField_descriptor,
        new java.lang.String[] { "Name", "Type", "Description", "DisplayName", "Required", "Description", });
    internal_static_littlehorse_UserTaskRun_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_littlehorse_UserTaskRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_UserTaskRun_descriptor,
        new java.lang.String[] { "Id", "UserTaskDefId", "UserGroup", "UserId", "Results", "Status", "Events", "Notes", "ScheduledTime", "NodeRunId", "Epoch", "UserGroup", "UserId", "Notes", });
    internal_static_littlehorse_UserTaskRun_ResultsEntry_descriptor =
      internal_static_littlehorse_UserTaskRun_descriptor.getNestedTypes().get(0);
    internal_static_littlehorse_UserTaskRun_ResultsEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_UserTaskRun_ResultsEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_littlehorse_AssignUserTaskRunRequest_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_littlehorse_AssignUserTaskRunRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_AssignUserTaskRunRequest_descriptor,
        new java.lang.String[] { "UserTaskRunId", "OverrideClaim", "UserGroup", "UserId", "UserGroup", "UserId", });
    internal_static_littlehorse_CompleteUserTaskRunRequest_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_littlehorse_CompleteUserTaskRunRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_CompleteUserTaskRunRequest_descriptor,
        new java.lang.String[] { "UserTaskRunId", "Results", "UserId", });
    internal_static_littlehorse_CompleteUserTaskRunRequest_ResultsEntry_descriptor =
      internal_static_littlehorse_CompleteUserTaskRunRequest_descriptor.getNestedTypes().get(0);
    internal_static_littlehorse_CompleteUserTaskRunRequest_ResultsEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_CompleteUserTaskRunRequest_ResultsEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_littlehorse_CancelUserTaskRunRequest_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_littlehorse_CancelUserTaskRunRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_CancelUserTaskRunRequest_descriptor,
        new java.lang.String[] { "UserTaskRunId", });
    internal_static_littlehorse_UserTaskTriggerReference_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_littlehorse_UserTaskTriggerReference_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_UserTaskTriggerReference_descriptor,
        new java.lang.String[] { "NodeRunId", "UserTaskEventNumber", "UserId", "UserGroup", "UserId", "UserGroup", });
    internal_static_littlehorse_UserTaskEvent_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_littlehorse_UserTaskEvent_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_UserTaskEvent_descriptor,
        new java.lang.String[] { "Time", "TaskExecuted", "Assigned", "Cancelled", "Event", });
    internal_static_littlehorse_UserTaskEvent_UTECancelled_descriptor =
      internal_static_littlehorse_UserTaskEvent_descriptor.getNestedTypes().get(0);
    internal_static_littlehorse_UserTaskEvent_UTECancelled_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_UserTaskEvent_UTECancelled_descriptor,
        new java.lang.String[] { });
    internal_static_littlehorse_UserTaskEvent_UTETaskExecuted_descriptor =
      internal_static_littlehorse_UserTaskEvent_descriptor.getNestedTypes().get(1);
    internal_static_littlehorse_UserTaskEvent_UTETaskExecuted_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_UserTaskEvent_UTETaskExecuted_descriptor,
        new java.lang.String[] { "TaskRun", });
    internal_static_littlehorse_UserTaskEvent_UTEAssigned_descriptor =
      internal_static_littlehorse_UserTaskEvent_descriptor.getNestedTypes().get(2);
    internal_static_littlehorse_UserTaskEvent_UTEAssigned_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_UserTaskEvent_UTEAssigned_descriptor,
        new java.lang.String[] { "OldUserId", "OldUserGroup", "NewUserId", "NewUserGroup", "OldUserId", "OldUserGroup", "NewUserId", "NewUserGroup", });
    com.google.protobuf.TimestampProto.getDescriptor();
    io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor();
    io.littlehorse.sdk.common.proto.ObjectId.getDescriptor();
    io.littlehorse.sdk.common.proto.VariableOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
