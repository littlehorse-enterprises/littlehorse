// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: storage.proto

package io.littlehorse.common.proto;

public final class Storage {
  private Storage() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_AttributePb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_AttributePb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_TagPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_TagPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_WfRunStoredInventory_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_WfRunStoredInventory_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_TagsCachePb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_TagsCachePb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_TagsCachePb_CachedTagPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_TagsCachePb_CachedTagPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_StoredGetablePb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_StoredGetablePb_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\rstorage.proto\022\013littlehorse\032\037google/pro" +
      "tobuf/timestamp.proto\032\017object_id.proto\"\'" +
      "\n\013AttributePb\022\013\n\003key\030\001 \001(\t\022\013\n\003val\030\002 \001(\t\"" +
      "\365\001\n\005TagPb\0222\n\013object_type\030\001 \001(\0162\035.littleh" +
      "orse.GetableClassEnum\022,\n\nattributes\030\002 \003(" +
      "\0132\030.littlehorse.AttributePb\022\033\n\023described" +
      "_object_id\030\003 \001(\t\022+\n\007created\030\004 \001(\0132\032.goog" +
      "le.protobuf.Timestamp\022-\n\010tag_type\030\005 \001(\0162" +
      "\033.littlehorse.TagStorageType\022\021\n\tstore_ke" +
      "y\030\007 \001(\t\"v\n\024WfRunStoredInventory\022\'\n\twf_ru" +
      "n_id\030\001 \001(\0132\024.littlehorse.WfRunId\0225\n\017exte" +
      "rnal_events\030\003 \003(\0132\034.littlehorse.External" +
      "EventId\"v\n\013TagsCachePb\0229\n\013cached_tags\030\001 " +
      "\003(\0132$.littlehorse.TagsCachePb.CachedTagP" +
      "b\032,\n\013CachedTagPb\022\n\n\002id\030\001 \001(\t\022\021\n\tis_remot" +
      "e\030\002 \001(\010\"\206\001\n\017StoredGetablePb\022-\n\013index_cac" +
      "he\030\001 \001(\0132\030.littlehorse.TagsCachePb\022\027\n\017ge" +
      "table_payload\030\002 \001(\014\022+\n\004type\030\003 \001(\0162\035.litt" +
      "lehorse.GetableClassEnum*6\n\013LHStoreType\022" +
      "\010\n\004CORE\020\000\022\014\n\010METADATA\020\001\022\017\n\013REPARTITION\020\002" +
      "*\332\001\n\rStoreableType\022\022\n\016STORED_GETABLE\020\000\022\022" +
      "\n\016SCHEDULED_TASK\020\001\022\024\n\020WF_METRIC_UPDATE\020\002" +
      "\022\026\n\022TASK_METRIC_UPDATE\020\003\022\014\n\010LH_TIMER\020\004\022\007" +
      "\n\003TAG\020\005\022\025\n\021PARTITION_METRICS\020\006\022\021\n\rMETRIC" +
      "_WINDOW\020\007\022\026\n\022INITIALIZATION_LOG\020\010\022\032\n\026WFR" +
      "UN_STORED_INVENTORY\020\t*\366\002\n\020GetableClassEn" +
      "um\022\014\n\010TASK_DEF\020\000\022\026\n\022EXTERNAL_EVENT_DEF\020\001" +
      "\022\013\n\007WF_SPEC\020\002\022\n\n\006WF_RUN\020\003\022\014\n\010NODE_RUN\020\004\022" +
      "\014\n\010VARIABLE\020\005\022\022\n\016EXTERNAL_EVENT\020\006\022\024\n\020TAS" +
      "K_DEF_METRICS\020\007\022\023\n\017WF_SPEC_METRICS\020\010\022\025\n\021" +
      "TASK_WORKER_GROUP\020\t\022\021\n\rUSER_TASK_DEF\020\n\022\014" +
      "\n\010TASK_RUN\020\013\022\021\n\rUSER_TASK_RUN\020\014\022\r\n\tPRINC" +
      "IPAL\020\r\022\n\n\006TENANT\020\016\022\026\n\022WORKFLOW_EVENT_DEF" +
      "\020\017\022\022\n\016WORKFLOW_EVENT\020\020\022\024\n\020SCHEDULED_WF_R" +
      "UN\020\021\022\n\n\006METRIC\020\022\022\024\n\020PARTITION_METRIC\020\023*\033" +
      "\n\016TagStorageType\022\t\n\005LOCAL\020\000B\037\n\033io.little" +
      "horse.common.protoP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          io.littlehorse.sdk.common.proto.ObjectId.getDescriptor(),
        });
    internal_static_littlehorse_AttributePb_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_AttributePb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_AttributePb_descriptor,
        new java.lang.String[] { "Key", "Val", });
    internal_static_littlehorse_TagPb_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_TagPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_TagPb_descriptor,
        new java.lang.String[] { "ObjectType", "Attributes", "DescribedObjectId", "Created", "TagType", "StoreKey", });
    internal_static_littlehorse_WfRunStoredInventory_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_littlehorse_WfRunStoredInventory_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_WfRunStoredInventory_descriptor,
        new java.lang.String[] { "WfRunId", "ExternalEvents", });
    internal_static_littlehorse_TagsCachePb_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_littlehorse_TagsCachePb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_TagsCachePb_descriptor,
        new java.lang.String[] { "CachedTags", });
    internal_static_littlehorse_TagsCachePb_CachedTagPb_descriptor =
      internal_static_littlehorse_TagsCachePb_descriptor.getNestedTypes().get(0);
    internal_static_littlehorse_TagsCachePb_CachedTagPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_TagsCachePb_CachedTagPb_descriptor,
        new java.lang.String[] { "Id", "IsRemote", });
    internal_static_littlehorse_StoredGetablePb_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_littlehorse_StoredGetablePb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_StoredGetablePb_descriptor,
        new java.lang.String[] { "IndexCache", "GetablePayload", "Type", });
    com.google.protobuf.TimestampProto.getDescriptor();
    io.littlehorse.sdk.common.proto.ObjectId.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
