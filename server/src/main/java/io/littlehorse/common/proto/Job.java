// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: job.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.common.proto;

public final class Job {
  private Job() {}
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      Job.class.getName());
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
    internal_static_littlehorse_BulkUpdateJob_descriptor;
  static final 
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_littlehorse_BulkUpdateJob_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_NoOpJob_descriptor;
  static final 
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_littlehorse_NoOpJob_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\tjob.proto\022\013littlehorse\"\246\001\n\rBulkUpdateJ" +
      "ob\022\021\n\tpartition\030\001 \001(\005\022\021\n\tstart_key\030\002 \001(\t" +
      "\022\017\n\007end_key\030\003 \001(\t\022\034\n\017resume_from_key\030\005 \001" +
      "(\tH\001\210\001\001\022%\n\005no_op\030\006 \001(\0132\024.littlehorse.NoO" +
      "pJobH\000B\005\n\003jobB\022\n\020_resume_from_key\"\t\n\007NoO" +
      "pJobB\037\n\033io.littlehorse.common.protoP\001b\006p" +
      "roto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_littlehorse_BulkUpdateJob_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_BulkUpdateJob_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_littlehorse_BulkUpdateJob_descriptor,
        new java.lang.String[] { "Partition", "StartKey", "EndKey", "ResumeFromKey", "NoOp", "Job", });
    internal_static_littlehorse_NoOpJob_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_NoOpJob_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_littlehorse_NoOpJob_descriptor,
        new java.lang.String[] { });
    descriptor.resolveAllFeaturesImmutable();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
