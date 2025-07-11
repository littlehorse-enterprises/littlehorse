// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: output_topic.proto

package io.littlehorse.sdk.common.proto;

public final class OutputTopic {
  private OutputTopic() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_OutputTopicRecord_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_OutputTopicRecord_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_MetadataOutputTopicRecord_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_MetadataOutputTopicRecord_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\022output_topic.proto\022\013littlehorse\032\037googl" +
      "e/protobuf/timestamp.proto\032\024workflow_eve" +
      "nt.proto\032\020user_tasks.proto\032\016task_run.pro" +
      "to\032\016task_def.proto\032\rwf_spec.proto\032\014wf_ru" +
      "n.proto\032\016variable.proto\032\020struct_def.prot" +
      "o\032\024external_event.proto\"\241\003\n\021OutputTopicR" +
      "ecord\022-\n\ttimestamp\030\001 \001(\0132\032.google.protob" +
      "uf.Timestamp\022(\n\010task_run\030\002 \001(\0132\024.littleh" +
      "orse.TaskRunH\000\0224\n\016workflow_event\030\003 \001(\0132\032" +
      ".littlehorse.WorkflowEventH\000\022$\n\006wf_run\030\004" +
      " \001(\0132\022.littlehorse.WfRunH\000\0221\n\ruser_task_" +
      "run\030\005 \001(\0132\030.littlehorse.UserTaskRunH\000\022)\n" +
      "\010variable\030\006 \001(\0132\025.littlehorse.VariableH\000" +
      "\0224\n\016external_event\030\007 \001(\0132\032.littlehorse.E" +
      "xternalEventH\000\0228\n\020correlated_event\030\010 \001(\013" +
      "2\034.littlehorse.CorrelatedEventH\000B\t\n\007payl" +
      "oad\"\333\002\n\031MetadataOutputTopicRecord\022&\n\007wf_" +
      "spec\030\001 \001(\0132\023.littlehorse.WfSpecH\000\022(\n\010tas" +
      "k_def\030\002 \001(\0132\024.littlehorse.TaskDefH\000\022;\n\022e" +
      "xternal_event_def\030\003 \001(\0132\035.littlehorse.Ex" +
      "ternalEventDefH\000\022;\n\022workflow_event_def\030\004" +
      " \001(\0132\035.littlehorse.WorkflowEventDefH\000\0221\n" +
      "\ruser_task_def\030\005 \001(\0132\030.littlehorse.UserT" +
      "askDefH\000\022,\n\nstruct_def\030\006 \001(\0132\026.littlehor" +
      "se.StructDefH\000B\021\n\017metadata_recordBM\n\037io." +
      "littlehorse.sdk.common.protoP\001Z\t.;lhprot" +
      "o\252\002\034LittleHorse.Sdk.Common.Protob\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          io.littlehorse.sdk.common.proto.WorkflowEventOuterClass.getDescriptor(),
          io.littlehorse.sdk.common.proto.UserTasks.getDescriptor(),
          io.littlehorse.sdk.common.proto.TaskRunOuterClass.getDescriptor(),
          io.littlehorse.sdk.common.proto.TaskDefOuterClass.getDescriptor(),
          io.littlehorse.sdk.common.proto.WfSpecOuterClass.getDescriptor(),
          io.littlehorse.sdk.common.proto.WfRunOuterClass.getDescriptor(),
          io.littlehorse.sdk.common.proto.VariableOuterClass.getDescriptor(),
          io.littlehorse.sdk.common.proto.StructDefOuterClass.getDescriptor(),
          io.littlehorse.sdk.common.proto.ExternalEventOuterClass.getDescriptor(),
        });
    internal_static_littlehorse_OutputTopicRecord_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_OutputTopicRecord_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_OutputTopicRecord_descriptor,
        new java.lang.String[] { "Timestamp", "TaskRun", "WorkflowEvent", "WfRun", "UserTaskRun", "Variable", "ExternalEvent", "CorrelatedEvent", "Payload", });
    internal_static_littlehorse_MetadataOutputTopicRecord_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_MetadataOutputTopicRecord_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_MetadataOutputTopicRecord_descriptor,
        new java.lang.String[] { "WfSpec", "TaskDef", "ExternalEventDef", "WorkflowEventDef", "UserTaskDef", "StructDef", "MetadataRecord", });
    com.google.protobuf.TimestampProto.getDescriptor();
    io.littlehorse.sdk.common.proto.WorkflowEventOuterClass.getDescriptor();
    io.littlehorse.sdk.common.proto.UserTasks.getDescriptor();
    io.littlehorse.sdk.common.proto.TaskRunOuterClass.getDescriptor();
    io.littlehorse.sdk.common.proto.TaskDefOuterClass.getDescriptor();
    io.littlehorse.sdk.common.proto.WfSpecOuterClass.getDescriptor();
    io.littlehorse.sdk.common.proto.WfRunOuterClass.getDescriptor();
    io.littlehorse.sdk.common.proto.VariableOuterClass.getDescriptor();
    io.littlehorse.sdk.common.proto.StructDefOuterClass.getDescriptor();
    io.littlehorse.sdk.common.proto.ExternalEventOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
