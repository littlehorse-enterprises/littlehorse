// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: wf_run.proto

package io.littlehorse.sdk.common.proto;

public final class WfRunOuterClass {
  private WfRunOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_WfRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_WfRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_ThreadRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_ThreadRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_FailureBeingHandled_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_FailureBeingHandled_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_PendingInterrupt_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_PendingInterrupt_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_PendingFailureHandler_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_PendingFailureHandler_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_PendingInterruptHaltReason_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_PendingInterruptHaltReason_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_PendingFailureHandlerHaltReason_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_PendingFailureHandlerHaltReason_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_HandlingFailureHaltReason_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_HandlingFailureHaltReason_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_ParentHalted_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_ParentHalted_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_Interrupted_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_Interrupted_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_ManualHalt_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_ManualHalt_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_ThreadHaltReason_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_ThreadHaltReason_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\014wf_run.proto\022\013littlehorse\032\037google/prot" +
      "obuf/timestamp.proto\032\022common_enums.proto" +
      "\032\017object_id.proto\"\306\003\n\005WfRun\022\n\n\002id\030\001 \001(\t\022" +
      "\024\n\014wf_spec_name\030\002 \001(\t\022\027\n\017wf_spec_version" +
      "\030\003 \001(\005\022%\n\006status\030\004 \001(\0162\025.littlehorse.LHS" +
      "tatus\022.\n\nstart_time\030\006 \001(\0132\032.google.proto" +
      "buf.Timestamp\0221\n\010end_time\030\007 \001(\0132\032.google" +
      ".protobuf.TimestampH\000\210\001\001\022+\n\013thread_runs\030" +
      "\010 \003(\0132\026.littlehorse.ThreadRun\0229\n\022pending" +
      "_interrupts\030\t \003(\0132\035.littlehorse.PendingI" +
      "nterrupt\022<\n\020pending_failures\030\n \003(\0132\".lit" +
      "tlehorse.PendingFailureHandler\022!\n\031greate" +
      "st_threadrun_number\030\013 \001(\005\022\034\n\024old_wf_spec" +
      "_versions\030\014 \003(\005B\013\n\t_end_timeJ\004\010\005\020\006\"\342\005\n\tT" +
      "hreadRun\022\034\n\017wf_spec_version\030\023 \001(\005H\000\210\001\001\022\016" +
      "\n\006number\030\002 \001(\005\022%\n\006status\030\003 \001(\0162\025.littleh" +
      "orse.LHStatus\022\030\n\020thread_spec_name\030\006 \001(\t\022" +
      ".\n\nstart_time\030\007 \001(\0132\032.google.protobuf.Ti" +
      "mestamp\0221\n\010end_time\030\010 \001(\0132\032.google.proto" +
      "buf.TimestampH\001\210\001\001\022\032\n\rerror_message\030\t \001(" +
      "\tH\002\210\001\001\022\030\n\020child_thread_ids\030\013 \003(\005\022\035\n\020pare" +
      "nt_thread_id\030\014 \001(\005H\003\210\001\001\0223\n\014halt_reasons\030" +
      "\r \003(\0132\035.littlehorse.ThreadHaltReason\022?\n\024" +
      "interrupt_trigger_id\030\016 \001(\0132\034.littlehorse" +
      ".ExternalEventIdH\004\210\001\001\022D\n\025failure_being_h" +
      "andled\030\017 \001(\0132 .littlehorse.FailureBeingH" +
      "andledH\005\210\001\001\022\035\n\025current_node_position\030\020 \001" +
      "(\005\022\037\n\027handled_failed_children\030\021 \003(\005\022%\n\004t" +
      "ype\030\022 \001(\0162\027.littlehorse.ThreadTypeB\022\n\020_w" +
      "f_spec_versionB\013\n\t_end_timeB\020\n\016_error_me" +
      "ssageB\023\n\021_parent_thread_idB\027\n\025_interrupt" +
      "_trigger_idB\030\n\026_failure_being_handledJ\004\010" +
      "\001\020\002J\004\010\004\020\005J\004\010\005\020\006\"c\n\023FailureBeingHandled\022\031" +
      "\n\021thread_run_number\030\001 \001(\005\022\031\n\021node_run_po" +
      "sition\030\002 \001(\005\022\026\n\016failure_number\030\003 \001(\005\"\205\001\n" +
      "\020PendingInterrupt\0227\n\021external_event_id\030\001" +
      " \001(\0132\034.littlehorse.ExternalEventId\022\031\n\021ha" +
      "ndler_spec_name\030\002 \001(\t\022\035\n\025interrupted_thr" +
      "ead_id\030\003 \001(\005\"M\n\025PendingFailureHandler\022\031\n" +
      "\021failed_thread_run\030\001 \001(\005\022\031\n\021handler_spec" +
      "_name\030\002 \001(\t\"U\n\032PendingInterruptHaltReaso" +
      "n\0227\n\021external_event_id\030\001 \001(\0132\034.littlehor" +
      "se.ExternalEventId\"<\n\037PendingFailureHand" +
      "lerHaltReason\022\031\n\021node_run_position\030\001 \001(\005" +
      "\"6\n\031HandlingFailureHaltReason\022\031\n\021handler" +
      "_thread_id\030\001 \001(\005\"(\n\014ParentHalted\022\030\n\020pare" +
      "nt_thread_id\030\001 \001(\005\"*\n\013Interrupted\022\033\n\023int" +
      "errupt_thread_id\030\001 \001(\005\"&\n\nManualHalt\022\030\n\017" +
      "meaning_of_life\030\211\001 \001(\010\"\204\003\n\020ThreadHaltRea" +
      "son\0222\n\rparent_halted\030\001 \001(\0132\031.littlehorse" +
      ".ParentHaltedH\000\022/\n\013interrupted\030\002 \001(\0132\030.l" +
      "ittlehorse.InterruptedH\000\022D\n\021pending_inte" +
      "rrupt\030\003 \001(\0132\'.littlehorse.PendingInterru" +
      "ptHaltReasonH\000\022G\n\017pending_failure\030\004 \001(\0132" +
      ",.littlehorse.PendingFailureHandlerHaltR" +
      "easonH\000\022B\n\020handling_failure\030\005 \001(\0132&.litt" +
      "lehorse.HandlingFailureHaltReasonH\000\022.\n\013m" +
      "anual_halt\030\006 \001(\0132\027.littlehorse.ManualHal" +
      "tH\000B\010\n\006reason*K\n\nThreadType\022\016\n\nENTRYPOIN" +
      "T\020\000\022\t\n\005CHILD\020\001\022\r\n\tINTERRUPT\020\002\022\023\n\017FAILURE" +
      "_HANDLER\020\003BG\n\037io.littlehorse.sdk.common." +
      "protoP\001Z\007.;model\252\002\030LittleHorse.Common.Pr" +
      "otob\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor(),
          io.littlehorse.sdk.common.proto.ObjectId.getDescriptor(),
        });
    internal_static_littlehorse_WfRun_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_WfRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_WfRun_descriptor,
        new java.lang.String[] { "Id", "WfSpecName", "WfSpecVersion", "Status", "StartTime", "EndTime", "ThreadRuns", "PendingInterrupts", "PendingFailures", "GreatestThreadrunNumber", "OldWfSpecVersions", "EndTime", });
    internal_static_littlehorse_ThreadRun_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_ThreadRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_ThreadRun_descriptor,
        new java.lang.String[] { "WfSpecVersion", "Number", "Status", "ThreadSpecName", "StartTime", "EndTime", "ErrorMessage", "ChildThreadIds", "ParentThreadId", "HaltReasons", "InterruptTriggerId", "FailureBeingHandled", "CurrentNodePosition", "HandledFailedChildren", "Type", "WfSpecVersion", "EndTime", "ErrorMessage", "ParentThreadId", "InterruptTriggerId", "FailureBeingHandled", });
    internal_static_littlehorse_FailureBeingHandled_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_littlehorse_FailureBeingHandled_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_FailureBeingHandled_descriptor,
        new java.lang.String[] { "ThreadRunNumber", "NodeRunPosition", "FailureNumber", });
    internal_static_littlehorse_PendingInterrupt_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_littlehorse_PendingInterrupt_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_PendingInterrupt_descriptor,
        new java.lang.String[] { "ExternalEventId", "HandlerSpecName", "InterruptedThreadId", });
    internal_static_littlehorse_PendingFailureHandler_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_littlehorse_PendingFailureHandler_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_PendingFailureHandler_descriptor,
        new java.lang.String[] { "FailedThreadRun", "HandlerSpecName", });
    internal_static_littlehorse_PendingInterruptHaltReason_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_littlehorse_PendingInterruptHaltReason_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_PendingInterruptHaltReason_descriptor,
        new java.lang.String[] { "ExternalEventId", });
    internal_static_littlehorse_PendingFailureHandlerHaltReason_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_littlehorse_PendingFailureHandlerHaltReason_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_PendingFailureHandlerHaltReason_descriptor,
        new java.lang.String[] { "NodeRunPosition", });
    internal_static_littlehorse_HandlingFailureHaltReason_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_littlehorse_HandlingFailureHaltReason_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_HandlingFailureHaltReason_descriptor,
        new java.lang.String[] { "HandlerThreadId", });
    internal_static_littlehorse_ParentHalted_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_littlehorse_ParentHalted_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_ParentHalted_descriptor,
        new java.lang.String[] { "ParentThreadId", });
    internal_static_littlehorse_Interrupted_descriptor =
      getDescriptor().getMessageTypes().get(9);
    internal_static_littlehorse_Interrupted_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_Interrupted_descriptor,
        new java.lang.String[] { "InterruptThreadId", });
    internal_static_littlehorse_ManualHalt_descriptor =
      getDescriptor().getMessageTypes().get(10);
    internal_static_littlehorse_ManualHalt_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_ManualHalt_descriptor,
        new java.lang.String[] { "MeaningOfLife", });
    internal_static_littlehorse_ThreadHaltReason_descriptor =
      getDescriptor().getMessageTypes().get(11);
    internal_static_littlehorse_ThreadHaltReason_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_ThreadHaltReason_descriptor,
        new java.lang.String[] { "ParentHalted", "Interrupted", "PendingInterrupt", "PendingFailure", "HandlingFailure", "ManualHalt", "Reason", });
    com.google.protobuf.TimestampProto.getDescriptor();
    io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor();
    io.littlehorse.sdk.common.proto.ObjectId.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
