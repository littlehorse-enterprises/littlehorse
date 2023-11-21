// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: node_run.proto

package io.littlehorse.sdk.common.proto;

public final class NodeRunOuterClass {
  private NodeRunOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_NodeRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_NodeRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_TaskNodeRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_TaskNodeRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_UserTaskNodeRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_UserTaskNodeRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_EntrypointRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_EntrypointRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_ExitRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_ExitRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_StartThreadRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_StartThreadRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_StartMultipleThreadsRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_StartMultipleThreadsRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_WaitForThreadsRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_WaitForThreadsRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_WaitForThreadsRun_WaitForThread_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_WaitForThreadsRun_WaitForThread_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_ExternalEventRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_ExternalEventRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_SleepNodeRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_SleepNodeRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_Failure_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_Failure_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\016node_run.proto\022\013littlehorse\032\037google/pr" +
      "otobuf/timestamp.proto\032\022common_enums.pro" +
      "to\032\016variable.proto\032\017object_id.proto\"\211\007\n\007" +
      "NodeRun\022\021\n\twf_run_id\030\001 \001(\t\022\031\n\021thread_run" +
      "_number\030\002 \001(\005\022\020\n\010position\030\003 \001(\005\022)\n\nwf_sp" +
      "ec_id\030\004 \001(\0132\025.littlehorse.WfSpecId\022\033\n\023fa" +
      "ilure_handler_ids\030\005 \003(\005\022%\n\006status\030\006 \001(\0162" +
      "\025.littlehorse.LHStatus\0220\n\014arrival_time\030\007" +
      " \001(\0132\032.google.protobuf.Timestamp\0221\n\010end_" +
      "time\030\010 \001(\0132\032.google.protobuf.TimestampH\001" +
      "\210\001\001\022\030\n\020thread_spec_name\030\t \001(\t\022\021\n\tnode_na" +
      "me\030\n \001(\t\022\032\n\rerror_message\030\013 \001(\tH\002\210\001\001\022&\n\010" +
      "failures\030\014 \003(\0132\024.littlehorse.Failure\022(\n\004" +
      "task\030\r \001(\0132\030.littlehorse.TaskNodeRunH\000\0227" +
      "\n\016external_event\030\016 \001(\0132\035.littlehorse.Ext" +
      "ernalEventRunH\000\0220\n\nentrypoint\030\017 \001(\0132\032.li" +
      "ttlehorse.EntrypointRunH\000\022$\n\004exit\030\020 \001(\0132" +
      "\024.littlehorse.ExitRunH\000\0223\n\014start_thread\030" +
      "\021 \001(\0132\033.littlehorse.StartThreadRunH\000\0226\n\014" +
      "wait_threads\030\022 \001(\0132\036.littlehorse.WaitFor" +
      "ThreadsRunH\000\022*\n\005sleep\030\023 \001(\0132\031.littlehors" +
      "e.SleepNodeRunH\000\0221\n\tuser_task\030\024 \001(\0132\034.li" +
      "ttlehorse.UserTaskNodeRunH\000\022F\n\026start_mul" +
      "tiple_threads\030\025 \001(\0132$.littlehorse.StartM" +
      "ultipleThreadsRunH\000B\013\n\tnode_typeB\013\n\t_end" +
      "_timeB\020\n\016_error_message\"O\n\013TaskNodeRun\0220" +
      "\n\013task_run_id\030\001 \001(\0132\026.littlehorse.TaskRu" +
      "nIdH\000\210\001\001B\016\n\014_task_run_id\"a\n\017UserTaskNode" +
      "Run\0229\n\020user_task_run_id\030\001 \001(\0132\032.littleho" +
      "rse.UserTaskRunIdH\000\210\001\001B\023\n\021_user_task_run" +
      "_id\"\017\n\rEntrypointRun\"\t\n\007ExitRun\"\\\n\016Start" +
      "ThreadRun\022\034\n\017child_thread_id\030\001 \001(\005H\000\210\001\001\022" +
      "\030\n\020thread_spec_name\030\002 \001(\tB\022\n\020_child_thre" +
      "ad_id\"3\n\027StartMultipleThreadsRun\022\030\n\020thre" +
      "ad_spec_name\030\001 \001(\t\"\307\002\n\021WaitForThreadsRun" +
      "\022=\n\007threads\030\001 \003(\0132,.littlehorse.WaitForT" +
      "hreadsRun.WaitForThread\0221\n\006policy\030\002 \001(\0162" +
      "!.littlehorse.WaitForThreadsPolicy\032\277\001\n\rW" +
      "aitForThread\0228\n\017thread_end_time\030\001 \001(\0132\032." +
      "google.protobuf.TimestampH\000\210\001\001\022,\n\rthread" +
      "_status\030\002 \001(\0162\025.littlehorse.LHStatus\022\031\n\021" +
      "thread_run_number\030\003 \001(\005\022\027\n\017already_handl" +
      "ed\030\005 \001(\010B\022\n\020_thread_end_time\"\313\001\n\020Externa" +
      "lEventRun\022\037\n\027external_event_def_name\030\001 \001" +
      "(\t\0223\n\nevent_time\030\002 \001(\0132\032.google.protobuf" +
      ".TimestampH\000\210\001\001\022<\n\021external_event_id\030\003 \001" +
      "(\0132\034.littlehorse.ExternalEventIdH\001\210\001\001B\r\n" +
      "\013_event_timeB\024\n\022_external_event_id\"C\n\014Sl" +
      "eepNodeRun\0223\n\017maturation_time\030\001 \001(\0132\032.go" +
      "ogle.protobuf.Timestamp\"\214\001\n\007Failure\022\024\n\014f" +
      "ailure_name\030\001 \001(\t\022\017\n\007message\030\002 \001(\t\0220\n\007co" +
      "ntent\030\003 \001(\0132\032.littlehorse.VariableValueH" +
      "\000\210\001\001\022\034\n\024was_properly_handled\030\004 \001(\010B\n\n\010_c" +
      "ontentBG\n\037io.littlehorse.sdk.common.prot" +
      "oP\001Z\007.;model\252\002\030LittleHorse.Common.Protob" +
      "\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor(),
          io.littlehorse.sdk.common.proto.VariableOuterClass.getDescriptor(),
          io.littlehorse.sdk.common.proto.ObjectId.getDescriptor(),
        });
    internal_static_littlehorse_NodeRun_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_NodeRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_NodeRun_descriptor,
        new java.lang.String[] { "WfRunId", "ThreadRunNumber", "Position", "WfSpecId", "FailureHandlerIds", "Status", "ArrivalTime", "EndTime", "ThreadSpecName", "NodeName", "ErrorMessage", "Failures", "Task", "ExternalEvent", "Entrypoint", "Exit", "StartThread", "WaitThreads", "Sleep", "UserTask", "StartMultipleThreads", "NodeType", "EndTime", "ErrorMessage", });
    internal_static_littlehorse_TaskNodeRun_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_TaskNodeRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_TaskNodeRun_descriptor,
        new java.lang.String[] { "TaskRunId", "TaskRunId", });
    internal_static_littlehorse_UserTaskNodeRun_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_littlehorse_UserTaskNodeRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_UserTaskNodeRun_descriptor,
        new java.lang.String[] { "UserTaskRunId", "UserTaskRunId", });
    internal_static_littlehorse_EntrypointRun_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_littlehorse_EntrypointRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_EntrypointRun_descriptor,
        new java.lang.String[] { });
    internal_static_littlehorse_ExitRun_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_littlehorse_ExitRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_ExitRun_descriptor,
        new java.lang.String[] { });
    internal_static_littlehorse_StartThreadRun_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_littlehorse_StartThreadRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_StartThreadRun_descriptor,
        new java.lang.String[] { "ChildThreadId", "ThreadSpecName", "ChildThreadId", });
    internal_static_littlehorse_StartMultipleThreadsRun_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_littlehorse_StartMultipleThreadsRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_StartMultipleThreadsRun_descriptor,
        new java.lang.String[] { "ThreadSpecName", });
    internal_static_littlehorse_WaitForThreadsRun_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_littlehorse_WaitForThreadsRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_WaitForThreadsRun_descriptor,
        new java.lang.String[] { "Threads", "Policy", });
    internal_static_littlehorse_WaitForThreadsRun_WaitForThread_descriptor =
      internal_static_littlehorse_WaitForThreadsRun_descriptor.getNestedTypes().get(0);
    internal_static_littlehorse_WaitForThreadsRun_WaitForThread_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_WaitForThreadsRun_WaitForThread_descriptor,
        new java.lang.String[] { "ThreadEndTime", "ThreadStatus", "ThreadRunNumber", "AlreadyHandled", "ThreadEndTime", });
    internal_static_littlehorse_ExternalEventRun_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_littlehorse_ExternalEventRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_ExternalEventRun_descriptor,
        new java.lang.String[] { "ExternalEventDefName", "EventTime", "ExternalEventId", "EventTime", "ExternalEventId", });
    internal_static_littlehorse_SleepNodeRun_descriptor =
      getDescriptor().getMessageTypes().get(9);
    internal_static_littlehorse_SleepNodeRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_SleepNodeRun_descriptor,
        new java.lang.String[] { "MaturationTime", });
    internal_static_littlehorse_Failure_descriptor =
      getDescriptor().getMessageTypes().get(10);
    internal_static_littlehorse_Failure_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_Failure_descriptor,
        new java.lang.String[] { "FailureName", "Message", "Content", "WasProperlyHandled", "Content", });
    com.google.protobuf.TimestampProto.getDescriptor();
    io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor();
    io.littlehorse.sdk.common.proto.VariableOuterClass.getDescriptor();
    io.littlehorse.sdk.common.proto.ObjectId.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
