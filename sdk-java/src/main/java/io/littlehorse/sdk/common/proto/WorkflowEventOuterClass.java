// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: workflow_event.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

public final class WorkflowEventOuterClass {
  private WorkflowEventOuterClass() {}
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      WorkflowEventOuterClass.class.getName());
  }
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_WorkflowEvent_descriptor;
  static final 
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_littlehorse_WorkflowEvent_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_WorkflowEventDef_descriptor;
  static final 
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_littlehorse_WorkflowEventDef_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\024workflow_event.proto\022\013littlehorse\032\037goo" +
      "gle/protobuf/timestamp.proto\032\016variable.p" +
      "roto\032\017object_id.proto\032\022common_enums.prot" +
      "o\"\303\001\n\rWorkflowEvent\022(\n\002id\030\001 \001(\0132\034.little" +
      "horse.WorkflowEventId\022+\n\007content\030\002 \001(\0132\032" +
      ".littlehorse.VariableValue\022.\n\ncreated_at" +
      "\030\003 \001(\0132\032.google.protobuf.Timestamp\022+\n\013no" +
      "de_run_id\030\004 \001(\0132\026.littlehorse.NodeRunId\"" +
      "\230\001\n\020WorkflowEventDef\022+\n\002id\030\001 \001(\0132\037.littl" +
      "ehorse.WorkflowEventDefId\022.\n\ncreated_at\030" +
      "\002 \001(\0132\032.google.protobuf.Timestamp\022\'\n\004typ" +
      "e\030\003 \001(\0162\031.littlehorse.VariableTypeBM\n\037io" +
      ".littlehorse.sdk.common.protoP\001Z\t.;lhpro" +
      "to\252\002\034LittleHorse.Sdk.Common.Protob\006proto" +
      "3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          io.littlehorse.sdk.common.proto.VariableOuterClass.getDescriptor(),
          io.littlehorse.sdk.common.proto.ObjectId.getDescriptor(),
          io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor(),
        });
    internal_static_littlehorse_WorkflowEvent_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_WorkflowEvent_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_littlehorse_WorkflowEvent_descriptor,
        new java.lang.String[] { "Id", "Content", "CreatedAt", "NodeRunId", });
    internal_static_littlehorse_WorkflowEventDef_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_WorkflowEventDef_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_littlehorse_WorkflowEventDef_descriptor,
        new java.lang.String[] { "Id", "CreatedAt", "Type", });
    descriptor.resolveAllFeaturesImmutable();
    com.google.protobuf.TimestampProto.getDescriptor();
    io.littlehorse.sdk.common.proto.VariableOuterClass.getDescriptor();
    io.littlehorse.sdk.common.proto.ObjectId.getDescriptor();
    io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
