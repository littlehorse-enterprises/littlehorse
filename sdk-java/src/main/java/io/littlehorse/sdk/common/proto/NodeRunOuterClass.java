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
      "to\032\016variable.proto\032\017object_id.proto\"\273\007\n\007" +
      "NodeRun\022\021\n\twf_run_id\030\001 \001(\t\022\031\n\021thread_run" +
      "_number\030\002 \001(\005\022\020\n\010position\030\003 \001(\005\022\034\n\017wf_sp" +
      "ec_version\030\004 \001(\005H\001\210\001\001\022%\n\006status\030\006 \001(\0162\025." +
      "littlehorse.LHStatus\0220\n\014arrival_time\030\007 \001" +
      "(\0132\032.google.protobuf.Timestamp\0221\n\010end_ti" +
      "me\030\010 \001(\0132\032.google.protobuf.TimestampH\002\210\001" +
      "\001\022)\n\nwf_spec_id\030\t \001(\0132\025.littlehorse.WfSp" +
      "ecId\022\030\n\020thread_spec_name\030\n \001(\t\022\021\n\tnode_n" +
      "ame\030\013 \001(\t\022\032\n\rerror_message\030\r \001(\tH\003\210\001\001\022&\n" +
      "\010failures\030\016 \003(\0132\024.littlehorse.Failure\022(\n" +
      "\004task\030\017 \001(\0132\030.littlehorse.TaskNodeRunH\000\022" +
      "7\n\016external_event\030\020 \001(\0132\035.littlehorse.Ex" +
      "ternalEventRunH\000\0220\n\nentrypoint\030\021 \001(\0132\032.l" +
      "ittlehorse.EntrypointRunH\000\022$\n\004exit\030\023 \001(\013" +
      "2\024.littlehorse.ExitRunH\000\0223\n\014start_thread" +
      "\030\024 \001(\0132\033.littlehorse.StartThreadRunH\000\0226\n" +
      "\014wait_threads\030\025 \001(\0132\036.littlehorse.WaitFo" +
      "rThreadsRunH\000\022*\n\005sleep\030\026 \001(\0132\031.littlehor" +
      "se.SleepNodeRunH\000\0221\n\tuser_task\030\030 \001(\0132\034.l" +
      "ittlehorse.UserTaskNodeRunH\000\022F\n\026start_mu" +
      "ltiple_threads\030\031 \001(\0132$.littlehorse.Start" +
      "MultipleThreadsRunH\000\022\033\n\023failure_handler_" +
      "ids\030\027 \003(\005B\013\n\tnode_typeB\022\n\020_wf_spec_versi" +
      "onB\013\n\t_end_timeB\020\n\016_error_message\"O\n\013Tas" +
      "kNodeRun\0220\n\013task_run_id\030\001 \001(\0132\026.littleho" +
      "rse.TaskRunIdH\000\210\001\001B\016\n\014_task_run_id\"a\n\017Us" +
      "erTaskNodeRun\0229\n\020user_task_run_id\030\001 \001(\0132" +
      "\032.littlehorse.UserTaskRunIdH\000\210\001\001B\023\n\021_use" +
      "r_task_run_id\"\017\n\rEntrypointRun\"\t\n\007ExitRu" +
      "n\"\\\n\016StartThreadRun\022\034\n\017child_thread_id\030\001" +
      " \001(\005H\000\210\001\001\022\030\n\020thread_spec_name\030\002 \001(\tB\022\n\020_" +
      "child_thread_id\"3\n\027StartMultipleThreadsR" +
      "un\022\030\n\020thread_spec_name\030\001 \001(\t\"\307\002\n\021WaitFor" +
      "ThreadsRun\022=\n\007threads\030\001 \003(\0132,.littlehors" +
      "e.WaitForThreadsRun.WaitForThread\0221\n\006pol" +
      "icy\030\002 \001(\0162!.littlehorse.WaitForThreadsPo" +
      "licy\032\277\001\n\rWaitForThread\0228\n\017thread_end_tim" +
      "e\030\001 \001(\0132\032.google.protobuf.TimestampH\000\210\001\001" +
      "\022,\n\rthread_status\030\002 \001(\0162\025.littlehorse.LH" +
      "Status\022\031\n\021thread_run_number\030\003 \001(\005\022\027\n\017alr" +
      "eady_handled\030\005 \001(\010B\022\n\020_thread_end_time\"\313" +
      "\001\n\020ExternalEventRun\022\037\n\027external_event_de" +
      "f_name\030\001 \001(\t\0223\n\nevent_time\030\002 \001(\0132\032.googl" +
      "e.protobuf.TimestampH\000\210\001\001\022<\n\021external_ev" +
      "ent_id\030\003 \001(\0132\034.littlehorse.ExternalEvent" +
      "IdH\001\210\001\001B\r\n\013_event_timeB\024\n\022_external_even" +
      "t_id\"C\n\014SleepNodeRun\0223\n\017maturation_time\030" +
      "\001 \001(\0132\032.google.protobuf.Timestamp\"\214\001\n\007Fa" +
      "ilure\022\024\n\014failure_name\030\001 \001(\t\022\017\n\007message\030\002" +
      " \001(\t\0220\n\007content\030\003 \001(\0132\032.littlehorse.Vari" +
      "ableValueH\000\210\001\001\022\034\n\024was_properly_handled\030\004" +
      " \001(\010B\n\n\010_contentBG\n\037io.littlehorse.sdk.c" +
      "ommon.protoP\001Z\007.;model\252\002\030LittleHorse.Com" +
      "mon.Protob\006proto3"
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
        new java.lang.String[] { "WfRunId", "ThreadRunNumber", "Position", "WfSpecVersion", "Status", "ArrivalTime", "EndTime", "WfSpecId", "ThreadSpecName", "NodeName", "ErrorMessage", "Failures", "Task", "ExternalEvent", "Entrypoint", "Exit", "StartThread", "WaitThreads", "Sleep", "UserTask", "StartMultipleThreads", "FailureHandlerIds", "NodeType", "WfSpecVersion", "EndTime", "ErrorMessage", });
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
