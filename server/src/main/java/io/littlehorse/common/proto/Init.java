// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: init.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.common.proto;

public final class Init {
  private Init() {}
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      Init.class.getName());
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
    internal_static_littlehorse_InitializationLog_descriptor;
  static final 
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_littlehorse_InitializationLog_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\ninit.proto\022\013littlehorse\032\037google/protob" +
      "uf/timestamp.proto\032\rservice.proto\032\nacls." +
      "proto\"\340\001\n\021InitializationLog\0220\n\014init_vers" +
      "ion\030\001 \001(\0132\032.littlehorse.ServerVersion\022-\n" +
      "\tinit_time\030\002 \001(\0132\032.google.protobuf.Times" +
      "tamp\0228\n\030init_anonymous_principal\030\003 \001(\0132\026" +
      ".littlehorse.Principal\0220\n\023init_default_t" +
      "enant\030\004 \001(\0132\023.littlehorse.TenantB\037\n\033io.l" +
      "ittlehorse.common.protoP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          io.littlehorse.sdk.common.proto.Service.getDescriptor(),
          io.littlehorse.sdk.common.proto.Acls.getDescriptor(),
        });
    internal_static_littlehorse_InitializationLog_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_InitializationLog_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_littlehorse_InitializationLog_descriptor,
        new java.lang.String[] { "InitVersion", "InitTime", "InitAnonymousPrincipal", "InitDefaultTenant", });
    descriptor.resolveAllFeaturesImmutable();
    com.google.protobuf.TimestampProto.getDescriptor();
    io.littlehorse.sdk.common.proto.Service.getDescriptor();
    io.littlehorse.sdk.common.proto.Acls.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
