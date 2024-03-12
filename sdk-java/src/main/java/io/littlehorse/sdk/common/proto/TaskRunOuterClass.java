// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: task_run.proto

package io.littlehorse.sdk.common.proto;

public final class TaskRunOuterClass {
  private TaskRunOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_TaskRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_TaskRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_VarNameAndVal_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_VarNameAndVal_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_TaskAttempt_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_TaskAttempt_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_TaskRunSource_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_TaskRunSource_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_TaskNodeReference_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_TaskNodeReference_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_LHTaskError_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_LHTaskError_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_LHTaskException_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_LHTaskException_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\016task_run.proto\022\013littlehorse\032\037google/pr" +
      "otobuf/timestamp.proto\032\022common_enums.pro" +
      "to\032\023common_wfspec.proto\032\016variable.proto\032" +
      "\017object_id.proto\032\020user_tasks.proto\"\331\003\n\007T" +
      "askRun\022\"\n\002id\030\001 \001(\0132\026.littlehorse.TaskRun" +
      "Id\022+\n\013task_def_id\030\002 \001(\0132\026.littlehorse.Ta" +
      "skDefId\022*\n\010attempts\030\003 \003(\0132\030.littlehorse." +
      "TaskAttempt\0223\n\017input_variables\030\005 \003(\0132\032.l" +
      "ittlehorse.VarNameAndVal\022*\n\006source\030\006 \001(\013" +
      "2\032.littlehorse.TaskRunSource\0220\n\014schedule" +
      "d_at\030\007 \001(\0132\032.google.protobuf.Timestamp\022\'" +
      "\n\006status\030\010 \001(\0162\027.littlehorse.TaskStatus\022" +
      "\027\n\017timeout_seconds\030\t \001(\005\022\026\n\016total_attemp" +
      "ts\030\004 \001(\005\022L\n\023exponential_backoff\030\n \001(\0132*." +
      "littlehorse.ExponentialBackoffRetryPolic" +
      "yH\000\210\001\001B\026\n\024_exponential_backoff\"L\n\rVarNam" +
      "eAndVal\022\020\n\010var_name\030\001 \001(\t\022)\n\005value\030\002 \001(\013" +
      "2\032.littlehorse.VariableValue\"\260\004\n\013TaskAtt" +
      "empt\0223\n\nlog_output\030\002 \001(\0132\032.littlehorse.V" +
      "ariableValueH\001\210\001\001\0226\n\rschedule_time\030\003 \001(\013" +
      "2\032.google.protobuf.TimestampH\002\210\001\001\0223\n\nsta" +
      "rt_time\030\004 \001(\0132\032.google.protobuf.Timestam" +
      "pH\003\210\001\001\0221\n\010end_time\030\005 \001(\0132\032.google.protob" +
      "uf.TimestampH\004\210\001\001\022\026\n\016task_worker_id\030\007 \001(" +
      "\t\022 \n\023task_worker_version\030\010 \001(\tH\005\210\001\001\022\'\n\006s" +
      "tatus\030\t \001(\0162\027.littlehorse.TaskStatus\022,\n\006" +
      "output\030\001 \001(\0132\032.littlehorse.VariableValue" +
      "H\000\022)\n\005error\030\n \001(\0132\030.littlehorse.LHTaskEr" +
      "rorH\000\0221\n\texception\030\013 \001(\0132\034.littlehorse.L" +
      "HTaskExceptionH\000B\010\n\006resultB\r\n\013_log_outpu" +
      "tB\020\n\016_schedule_timeB\r\n\013_start_timeB\013\n\t_e" +
      "nd_timeB\026\n\024_task_worker_version\"\332\001\n\rTask" +
      "RunSource\0223\n\ttask_node\030\001 \001(\0132\036.littlehor" +
      "se.TaskNodeReferenceH\000\022B\n\021user_task_trig" +
      "ger\030\002 \001(\0132%.littlehorse.UserTaskTriggerR" +
      "eferenceH\000\022.\n\nwf_spec_id\030\003 \001(\0132\025.littleh" +
      "orse.WfSpecIdH\001\210\001\001B\021\n\017task_run_sourceB\r\n" +
      "\013_wf_spec_id\"@\n\021TaskNodeReference\022+\n\013nod" +
      "e_run_id\030\001 \001(\0132\026.littlehorse.NodeRunId\"F" +
      "\n\013LHTaskError\022&\n\004type\030\001 \001(\0162\030.littlehors" +
      "e.LHErrorType\022\017\n\007message\030\002 \001(\t\"0\n\017LHTask" +
      "Exception\022\014\n\004name\030\001 \001(\t\022\017\n\007message\030\002 \001(\t" +
      "BG\n\037io.littlehorse.sdk.common.protoP\001Z\007." +
      ";model\252\002\030LittleHorse.Common.Protob\006proto" +
      "3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor(),
          io.littlehorse.sdk.common.proto.CommonWfspec.getDescriptor(),
          io.littlehorse.sdk.common.proto.VariableOuterClass.getDescriptor(),
          io.littlehorse.sdk.common.proto.ObjectId.getDescriptor(),
          io.littlehorse.sdk.common.proto.UserTasks.getDescriptor(),
        });
    internal_static_littlehorse_TaskRun_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_TaskRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_TaskRun_descriptor,
        new java.lang.String[] { "Id", "TaskDefId", "Attempts", "InputVariables", "Source", "ScheduledAt", "Status", "TimeoutSeconds", "TotalAttempts", "ExponentialBackoff", "ExponentialBackoff", });
    internal_static_littlehorse_VarNameAndVal_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_VarNameAndVal_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_VarNameAndVal_descriptor,
        new java.lang.String[] { "VarName", "Value", });
    internal_static_littlehorse_TaskAttempt_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_littlehorse_TaskAttempt_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_TaskAttempt_descriptor,
        new java.lang.String[] { "LogOutput", "ScheduleTime", "StartTime", "EndTime", "TaskWorkerId", "TaskWorkerVersion", "Status", "Output", "Error", "Exception", "Result", "LogOutput", "ScheduleTime", "StartTime", "EndTime", "TaskWorkerVersion", });
    internal_static_littlehorse_TaskRunSource_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_littlehorse_TaskRunSource_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_TaskRunSource_descriptor,
        new java.lang.String[] { "TaskNode", "UserTaskTrigger", "WfSpecId", "TaskRunSource", "WfSpecId", });
    internal_static_littlehorse_TaskNodeReference_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_littlehorse_TaskNodeReference_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_TaskNodeReference_descriptor,
        new java.lang.String[] { "NodeRunId", });
    internal_static_littlehorse_LHTaskError_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_littlehorse_LHTaskError_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_LHTaskError_descriptor,
        new java.lang.String[] { "Type", "Message", });
    internal_static_littlehorse_LHTaskException_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_littlehorse_LHTaskException_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_LHTaskException_descriptor,
        new java.lang.String[] { "Name", "Message", });
    com.google.protobuf.TimestampProto.getDescriptor();
    io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor();
    io.littlehorse.sdk.common.proto.CommonWfspec.getDescriptor();
    io.littlehorse.sdk.common.proto.VariableOuterClass.getDescriptor();
    io.littlehorse.sdk.common.proto.ObjectId.getDescriptor();
    io.littlehorse.sdk.common.proto.UserTasks.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
