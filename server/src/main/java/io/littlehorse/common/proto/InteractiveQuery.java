// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: interactive_query.proto

package io.littlehorse.common.proto;

public final class InteractiveQuery {
  private InteractiveQuery() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_GetObjectRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_GetObjectRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_GetObjectResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_GetObjectResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_WaitForCommandRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_WaitForCommandRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_WaitForCommandResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_WaitForCommandResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_ScanFilter_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_ScanFilter_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_InternalScanPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_InternalScanPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_InternalScanPb_TagScanPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_InternalScanPb_TagScanPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_InternalScanPb_BoundedObjectIdScanPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_InternalScanPb_BoundedObjectIdScanPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_InternalGetAdvertisedHostsResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_InternalGetAdvertisedHostsResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_InternalGetAdvertisedHostsResponse_HostsEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_InternalGetAdvertisedHostsResponse_HostsEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_InternalScanResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_InternalScanResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_PartitionBookmarkPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_PartitionBookmarkPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_BookmarkPb_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_BookmarkPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_BookmarkPb_InProgressPartitionsEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_BookmarkPb_InProgressPartitionsEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_InternalWaitForWfEventRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_InternalWaitForWfEventRequest_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\027interactive_query.proto\022\013littlehorse\032\037" +
      "google/protobuf/timestamp.proto\032\rstorage" +
      ".proto\032\rservice.proto\032\022common_enums.prot" +
      "o\032\017object_id.proto\"l\n\020GetObjectRequest\0222" +
      "\n\013object_type\030\001 \001(\0162\035.littlehorse.Getabl" +
      "eClassEnum\022\021\n\tobject_id\030\002 \001(\t\022\021\n\tpartiti" +
      "on\030\003 \001(\005\"%\n\021GetObjectResponse\022\020\n\010respons" +
      "e\030\001 \001(\014\"+\n\025WaitForCommandRequest\022\022\n\ncomm" +
      "and_id\030\001 \001(\t\"m\n\026WaitForCommandResponse\022\022" +
      "\n\ncommand_id\030\001 \001(\t\022/\n\013result_time\030\002 \001(\0132" +
      "\032.google.protobuf.Timestamp\022\016\n\006result\030\003 " +
      "\001(\014\"~\n\nScanFilter\022.\n\rwf_run_status\030\001 \001(\016" +
      "2\025.littlehorse.LHStatusH\000\0224\n\016variable_ma" +
      "tch\030\002 \001(\0132\032.littlehorse.VariableMatchH\000B" +
      "\n\n\010criteria\"\222\006\n\016InternalScanPb\0222\n\013result" +
      "_type\030\001 \001(\0162\035.littlehorse.ScanResultType" +
      "Pb\022\r\n\005limit\030\002 \001(\005\022.\n\010bookmark\030\003 \001(\0132\027.li" +
      "ttlehorse.BookmarkPbH\001\210\001\001\0222\n\013object_type" +
      "\030\004 \001(\0162\035.littlehorse.GetableClassEnum\022\022\n" +
      "\nstore_name\030\005 \001(\t\022\032\n\rpartition_key\030\006 \001(\t" +
      "H\002\210\001\001\022S\n\026bounded_object_id_scan\030\007 \001(\01321." +
      "littlehorse.InternalScanPb.BoundedObject" +
      "IdScanPbH\000\0229\n\010tag_scan\030\010 \001(\0132%.littlehor" +
      "se.InternalScanPb.TagScanPbH\000\022\021\n\ttenant_" +
      "id\030\t \001(\t\022(\n\007filters\030\n \003(\0132\027.littlehorse." +
      "ScanFilter\032\313\001\n\tTagScanPb\022=\n\024earliest_cre" +
      "ate_time\030\001 \001(\0132\032.google.protobuf.Timesta" +
      "mpH\000\210\001\001\022;\n\022latest_create_time\030\002 \001(\0132\032.go" +
      "ogle.protobuf.TimestampH\001\210\001\001\022\022\n\nkey_pref" +
      "ix\030\003 \001(\tB\027\n\025_earliest_create_timeB\025\n\023_la" +
      "test_create_time\032^\n\025BoundedObjectIdScanP" +
      "b\022\027\n\017start_object_id\030\001 \001(\t\022\032\n\rend_object" +
      "_id\030\002 \001(\tH\000\210\001\001B\020\n\016_end_object_idB\017\n\rscan" +
      "_boundaryB\013\n\t_bookmarkB\020\n\016_partition_key" +
      "\"\266\001\n\"InternalGetAdvertisedHostsResponse\022" +
      "I\n\005hosts\030\001 \003(\0132:.littlehorse.InternalGet" +
      "AdvertisedHostsResponse.HostsEntry\032E\n\nHo" +
      "stsEntry\022\013\n\003key\030\001 \001(\t\022&\n\005value\030\002 \001(\0132\027.l" +
      "ittlehorse.LHHostInfo:\0028\001\"Z\n\024InternalSca" +
      "nResponse\022\017\n\007results\030\001 \003(\014\0221\n\020updated_bo" +
      "okmark\030\002 \001(\0132\027.littlehorse.BookmarkPb\"K\n" +
      "\023PartitionBookmarkPb\022\020\n\010parttion\030\001 \001(\005\022\025" +
      "\n\010last_key\030\002 \001(\tH\000\210\001\001B\013\n\t_last_key\"\334\001\n\nB" +
      "ookmarkPb\022Q\n\026in_progress_partitions\030\001 \003(" +
      "\01321.littlehorse.BookmarkPb.InProgressPar" +
      "titionsEntry\022\034\n\024completed_partitions\030\002 \003" +
      "(\005\032]\n\031InProgressPartitionsEntry\022\013\n\003key\030\001" +
      " \001(\005\022/\n\005value\030\002 \001(\0132 .littlehorse.Partit" +
      "ionBookmarkPb:\0028\001\"X\n\035InternalWaitForWfEv" +
      "entRequest\0227\n\007request\030\001 \001(\0132&.littlehors" +
      "e.AwaitWorkflowEventRequest*-\n\020ScanResul" +
      "tTypePb\022\r\n\tOBJECT_ID\020\000\022\n\n\006OBJECT\020\001B\037\n\033io" +
      ".littlehorse.common.protoP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          io.littlehorse.common.proto.Storage.getDescriptor(),
          io.littlehorse.sdk.common.proto.Service.getDescriptor(),
          io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor(),
          io.littlehorse.sdk.common.proto.ObjectId.getDescriptor(),
        });
    internal_static_littlehorse_GetObjectRequest_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_GetObjectRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_GetObjectRequest_descriptor,
        new java.lang.String[] { "ObjectType", "ObjectId", "Partition", });
    internal_static_littlehorse_GetObjectResponse_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_GetObjectResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_GetObjectResponse_descriptor,
        new java.lang.String[] { "Response", });
    internal_static_littlehorse_WaitForCommandRequest_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_littlehorse_WaitForCommandRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_WaitForCommandRequest_descriptor,
        new java.lang.String[] { "CommandId", });
    internal_static_littlehorse_WaitForCommandResponse_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_littlehorse_WaitForCommandResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_WaitForCommandResponse_descriptor,
        new java.lang.String[] { "CommandId", "ResultTime", "Result", });
    internal_static_littlehorse_ScanFilter_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_littlehorse_ScanFilter_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_ScanFilter_descriptor,
        new java.lang.String[] { "WfRunStatus", "VariableMatch", "Criteria", });
    internal_static_littlehorse_InternalScanPb_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_littlehorse_InternalScanPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_InternalScanPb_descriptor,
        new java.lang.String[] { "ResultType", "Limit", "Bookmark", "ObjectType", "StoreName", "PartitionKey", "BoundedObjectIdScan", "TagScan", "TenantId", "Filters", "ScanBoundary", "Bookmark", "PartitionKey", });
    internal_static_littlehorse_InternalScanPb_TagScanPb_descriptor =
      internal_static_littlehorse_InternalScanPb_descriptor.getNestedTypes().get(0);
    internal_static_littlehorse_InternalScanPb_TagScanPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_InternalScanPb_TagScanPb_descriptor,
        new java.lang.String[] { "EarliestCreateTime", "LatestCreateTime", "KeyPrefix", "EarliestCreateTime", "LatestCreateTime", });
    internal_static_littlehorse_InternalScanPb_BoundedObjectIdScanPb_descriptor =
      internal_static_littlehorse_InternalScanPb_descriptor.getNestedTypes().get(1);
    internal_static_littlehorse_InternalScanPb_BoundedObjectIdScanPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_InternalScanPb_BoundedObjectIdScanPb_descriptor,
        new java.lang.String[] { "StartObjectId", "EndObjectId", "EndObjectId", });
    internal_static_littlehorse_InternalGetAdvertisedHostsResponse_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_littlehorse_InternalGetAdvertisedHostsResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_InternalGetAdvertisedHostsResponse_descriptor,
        new java.lang.String[] { "Hosts", });
    internal_static_littlehorse_InternalGetAdvertisedHostsResponse_HostsEntry_descriptor =
      internal_static_littlehorse_InternalGetAdvertisedHostsResponse_descriptor.getNestedTypes().get(0);
    internal_static_littlehorse_InternalGetAdvertisedHostsResponse_HostsEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_InternalGetAdvertisedHostsResponse_HostsEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_littlehorse_InternalScanResponse_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_littlehorse_InternalScanResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_InternalScanResponse_descriptor,
        new java.lang.String[] { "Results", "UpdatedBookmark", });
    internal_static_littlehorse_PartitionBookmarkPb_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_littlehorse_PartitionBookmarkPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_PartitionBookmarkPb_descriptor,
        new java.lang.String[] { "Parttion", "LastKey", "LastKey", });
    internal_static_littlehorse_BookmarkPb_descriptor =
      getDescriptor().getMessageTypes().get(9);
    internal_static_littlehorse_BookmarkPb_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_BookmarkPb_descriptor,
        new java.lang.String[] { "InProgressPartitions", "CompletedPartitions", });
    internal_static_littlehorse_BookmarkPb_InProgressPartitionsEntry_descriptor =
      internal_static_littlehorse_BookmarkPb_descriptor.getNestedTypes().get(0);
    internal_static_littlehorse_BookmarkPb_InProgressPartitionsEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_BookmarkPb_InProgressPartitionsEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_littlehorse_InternalWaitForWfEventRequest_descriptor =
      getDescriptor().getMessageTypes().get(10);
    internal_static_littlehorse_InternalWaitForWfEventRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_InternalWaitForWfEventRequest_descriptor,
        new java.lang.String[] { "Request", });
    com.google.protobuf.TimestampProto.getDescriptor();
    io.littlehorse.common.proto.Storage.getDescriptor();
    io.littlehorse.sdk.common.proto.Service.getDescriptor();
    io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor();
    io.littlehorse.sdk.common.proto.ObjectId.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
