// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: variable.proto

package io.littlehorse.sdk.common.proto;

public final class VariableOuterClass {
  private VariableOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_VariableValue_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_VariableValue_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_Variable_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_Variable_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\016variable.proto\022\013littlehorse\032\037google/pr" +
      "otobuf/timestamp.proto\032\017object_id.proto\"" +
      "\227\001\n\rVariableValue\022\022\n\010json_obj\030\002 \001(\tH\000\022\022\n" +
      "\010json_arr\030\003 \001(\tH\000\022\020\n\006double\030\004 \001(\001H\000\022\016\n\004b" +
      "ool\030\005 \001(\010H\000\022\r\n\003str\030\006 \001(\tH\000\022\r\n\003int\030\007 \001(\003H" +
      "\000\022\017\n\005bytes\030\010 \001(\014H\000B\007\n\005valueJ\004\010\001\020\002\"\305\001\n\010Va" +
      "riable\022#\n\002id\030\001 \001(\0132\027.littlehorse.Variabl" +
      "eId\022)\n\005value\030\002 \001(\0132\032.littlehorse.Variabl" +
      "eValue\022.\n\ncreated_at\030\003 \001(\0132\032.google.prot" +
      "obuf.Timestamp\022)\n\nwf_spec_id\030\004 \001(\0132\025.lit" +
      "tlehorse.WfSpecId\022\016\n\006masked\030\005 \001(\010BI\n\037io." +
      "littlehorse.sdk.common.protoP\001Z\t.;lhprot" +
      "o\252\002\030LittleHorse.Common.Protob\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          io.littlehorse.sdk.common.proto.ObjectId.getDescriptor(),
        });
    internal_static_littlehorse_VariableValue_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_VariableValue_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_VariableValue_descriptor,
        new java.lang.String[] { "JsonObj", "JsonArr", "Double", "Bool", "Str", "Int", "Bytes", "Value", });
    internal_static_littlehorse_Variable_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_Variable_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_Variable_descriptor,
        new java.lang.String[] { "Id", "Value", "CreatedAt", "WfSpecId", "Masked", });
    com.google.protobuf.TimestampProto.getDescriptor();
    io.littlehorse.sdk.common.proto.ObjectId.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
