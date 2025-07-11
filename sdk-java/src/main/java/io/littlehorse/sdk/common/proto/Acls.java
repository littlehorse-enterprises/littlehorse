// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: acls.proto

package io.littlehorse.sdk.common.proto;

public final class Acls {
  private Acls() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_Principal_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_Principal_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_Principal_PerTenantAclsEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_Principal_PerTenantAclsEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_Tenant_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_Tenant_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_ServerACLs_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_ServerACLs_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_ServerACL_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_ServerACL_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_PutPrincipalRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_PutPrincipalRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_PutPrincipalRequest_PerTenantAclsEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_PutPrincipalRequest_PerTenantAclsEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_DeletePrincipalRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_DeletePrincipalRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_OutputTopicConfig_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_OutputTopicConfig_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_PutTenantRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_PutTenantRequest_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\nacls.proto\022\013littlehorse\032\037google/protob" +
      "uf/timestamp.proto\032\017object_id.proto\"\242\002\n\t" +
      "Principal\022$\n\002id\030\001 \001(\0132\030.littlehorse.Prin" +
      "cipalId\022.\n\ncreated_at\030\002 \001(\0132\032.google.pro" +
      "tobuf.Timestamp\022B\n\017per_tenant_acls\030\003 \003(\013" +
      "2).littlehorse.Principal.PerTenantAclsEn" +
      "try\022,\n\013global_acls\030\004 \001(\0132\027.littlehorse.S" +
      "erverACLs\032M\n\022PerTenantAclsEntry\022\013\n\003key\030\001" +
      " \001(\t\022&\n\005value\030\002 \001(\0132\027.littlehorse.Server" +
      "ACLs:\0028\001\"\265\001\n\006Tenant\022!\n\002id\030\001 \001(\0132\025.little" +
      "horse.TenantId\022.\n\ncreated_at\030\002 \001(\0132\032.goo" +
      "gle.protobuf.Timestamp\022@\n\023output_topic_c" +
      "onfig\030\003 \001(\0132\036.littlehorse.OutputTopicCon" +
      "figH\000\210\001\001B\026\n\024_output_topic_config\"2\n\nServ" +
      "erACLs\022$\n\004acls\030\001 \003(\0132\026.littlehorse.Serve" +
      "rACL\"\236\001\n\tServerACL\022+\n\tresources\030\001 \003(\0162\030." +
      "littlehorse.ACLResource\022/\n\017allowed_actio" +
      "ns\030\002 \003(\0162\026.littlehorse.ACLAction\022\016\n\004name" +
      "\030\003 \001(\tH\000\022\020\n\006prefix\030\004 \001(\tH\000B\021\n\017resource_f" +
      "ilter\"\377\001\n\023PutPrincipalRequest\022\n\n\002id\030\001 \001(" +
      "\t\022L\n\017per_tenant_acls\030\002 \003(\01323.littlehorse" +
      ".PutPrincipalRequest.PerTenantAclsEntry\022" +
      ",\n\013global_acls\030\003 \001(\0132\027.littlehorse.Serve" +
      "rACLs\022\021\n\toverwrite\030\005 \001(\010\032M\n\022PerTenantAcl" +
      "sEntry\022\013\n\003key\030\001 \001(\t\022&\n\005value\030\002 \001(\0132\027.lit" +
      "tlehorse.ServerACLs:\0028\001\">\n\026DeletePrincip" +
      "alRequest\022$\n\002id\030\001 \001(\0132\030.littlehorse.Prin" +
      "cipalId\"\270\001\n\021OutputTopicConfig\022Y\n\027default" +
      "_recording_level\030\001 \001(\01628.littlehorse.Out" +
      "putTopicConfig.OutputTopicRecordingLevel" +
      "\"H\n\031OutputTopicRecordingLevel\022\025\n\021ALL_ENT" +
      "ITY_EVENTS\020\000\022\024\n\020NO_ENTITY_EVENTS\020\001\"x\n\020Pu" +
      "tTenantRequest\022\n\n\002id\030\001 \001(\t\022@\n\023output_top" +
      "ic_config\030\002 \001(\0132\036.littlehorse.OutputTopi" +
      "cConfigH\000\210\001\001B\026\n\024_output_topic_config*\325\001\n" +
      "\013ACLResource\022\020\n\014ACL_WORKFLOW\020\000\022\014\n\010ACL_TA" +
      "SK\020\001\022\026\n\022ACL_EXTERNAL_EVENT\020\002\022\021\n\rACL_USER" +
      "_TASK\020\003\022\021\n\rACL_PRINCIPAL\020\004\022\016\n\nACL_TENANT" +
      "\020\005\022\025\n\021ACL_ALL_RESOURCES\020\006\022\031\n\025ACL_TASK_WO" +
      "RKER_GROUP\020\007\022\026\n\022ACL_WORKFLOW_EVENT\020\010\022\016\n\n" +
      "ACL_STRUCT\020\t*C\n\tACLAction\022\010\n\004READ\020\000\022\007\n\003R" +
      "UN\020\001\022\022\n\016WRITE_METADATA\020\002\022\017\n\013ALL_ACTIONS\020" +
      "\003BM\n\037io.littlehorse.sdk.common.protoP\001Z\t" +
      ".;lhproto\252\002\034LittleHorse.Sdk.Common.Proto" +
      "b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          io.littlehorse.sdk.common.proto.ObjectId.getDescriptor(),
        });
    internal_static_littlehorse_Principal_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_Principal_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_Principal_descriptor,
        new java.lang.String[] { "Id", "CreatedAt", "PerTenantAcls", "GlobalAcls", });
    internal_static_littlehorse_Principal_PerTenantAclsEntry_descriptor =
      internal_static_littlehorse_Principal_descriptor.getNestedTypes().get(0);
    internal_static_littlehorse_Principal_PerTenantAclsEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_Principal_PerTenantAclsEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_littlehorse_Tenant_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_Tenant_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_Tenant_descriptor,
        new java.lang.String[] { "Id", "CreatedAt", "OutputTopicConfig", "OutputTopicConfig", });
    internal_static_littlehorse_ServerACLs_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_littlehorse_ServerACLs_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_ServerACLs_descriptor,
        new java.lang.String[] { "Acls", });
    internal_static_littlehorse_ServerACL_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_littlehorse_ServerACL_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_ServerACL_descriptor,
        new java.lang.String[] { "Resources", "AllowedActions", "Name", "Prefix", "ResourceFilter", });
    internal_static_littlehorse_PutPrincipalRequest_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_littlehorse_PutPrincipalRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_PutPrincipalRequest_descriptor,
        new java.lang.String[] { "Id", "PerTenantAcls", "GlobalAcls", "Overwrite", });
    internal_static_littlehorse_PutPrincipalRequest_PerTenantAclsEntry_descriptor =
      internal_static_littlehorse_PutPrincipalRequest_descriptor.getNestedTypes().get(0);
    internal_static_littlehorse_PutPrincipalRequest_PerTenantAclsEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_PutPrincipalRequest_PerTenantAclsEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_littlehorse_DeletePrincipalRequest_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_littlehorse_DeletePrincipalRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_DeletePrincipalRequest_descriptor,
        new java.lang.String[] { "Id", });
    internal_static_littlehorse_OutputTopicConfig_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_littlehorse_OutputTopicConfig_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_OutputTopicConfig_descriptor,
        new java.lang.String[] { "DefaultRecordingLevel", });
    internal_static_littlehorse_PutTenantRequest_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_littlehorse_PutTenantRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_PutTenantRequest_descriptor,
        new java.lang.String[] { "Id", "OutputTopicConfig", "OutputTopicConfig", });
    com.google.protobuf.TimestampProto.getDescriptor();
    io.littlehorse.sdk.common.proto.ObjectId.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
