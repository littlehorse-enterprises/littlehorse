// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: metrics.proto

package io.littlehorse.sdk.common.proto;

public final class Metrics {
  private Metrics() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_Metric_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_Metric_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_PartitionMetric_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_PartitionMetric_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_PartitionWindowedMetric_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_PartitionWindowedMetric_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_PartitionMetricId_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_PartitionMetricId_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_MetricRun_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_MetricRun_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_MetricRun_ValuePerPartitionEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_MetricRun_ValuePerPartitionEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\rmetrics.proto\022\013littlehorse\032\037google/pro" +
      "tobuf/timestamp.proto\032\036google/protobuf/d" +
      "uration.proto\032\017object_id.proto\"\215\001\n\006Metri" +
      "c\022!\n\002id\030\001 \001(\0132\025.littlehorse.MetricId\022.\n\n" +
      "created_at\030\002 \001(\0132\032.google.protobuf.Times" +
      "tamp\0220\n\rwindow_length\030\003 \001(\0132\031.google.pro" +
      "tobuf.Duration\"\335\001\n\017PartitionMetric\022*\n\002id" +
      "\030\001 \001(\0132\036.littlehorse.PartitionMetricId\022." +
      "\n\ncreated_at\030\002 \001(\0132\032.google.protobuf.Tim" +
      "estamp\022<\n\016active_windows\030\003 \003(\0132$.littleh" +
      "orse.PartitionWindowedMetric\0220\n\rwindow_l" +
      "ength\030\004 \001(\0132\031.google.protobuf.Duration\"Z" +
      "\n\027PartitionWindowedMetric\022\r\n\005value\030\001 \001(\001" +
      "\0220\n\014window_start\030\002 \001(\0132\032.google.protobuf" +
      ".Timestamp\"`\n\021PartitionMetricId\022!\n\002id\030\001 " +
      "\001(\0132\025.littlehorse.MetricId\022(\n\ttenant_id\030" +
      "\002 \001(\0132\025.littlehorse.TenantId\"\366\001\n\tMetricR" +
      "un\022$\n\002id\030\001 \001(\0132\030.littlehorse.MetricRunId" +
      "\022\r\n\005value\030\002 \001(\001\022.\n\ncreated_at\030\004 \001(\0132\032.go" +
      "ogle.protobuf.Timestamp\022J\n\023value_per_par" +
      "tition\030\005 \003(\0132-.littlehorse.MetricRun.Val" +
      "uePerPartitionEntry\0328\n\026ValuePerPartition" +
      "Entry\022\013\n\003key\030\001 \001(\005\022\r\n\005value\030\002 \001(\001:\0028\001BM\n" +
      "\037io.littlehorse.sdk.common.protoP\001Z\t.;lh" +
      "proto\252\002\034LittleHorse.Sdk.Common.Protob\006pr" +
      "oto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          com.google.protobuf.DurationProto.getDescriptor(),
          io.littlehorse.sdk.common.proto.ObjectId.getDescriptor(),
        });
    internal_static_littlehorse_Metric_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_Metric_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_Metric_descriptor,
        new java.lang.String[] { "Id", "CreatedAt", "WindowLength", });
    internal_static_littlehorse_PartitionMetric_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_PartitionMetric_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_PartitionMetric_descriptor,
        new java.lang.String[] { "Id", "CreatedAt", "ActiveWindows", "WindowLength", });
    internal_static_littlehorse_PartitionWindowedMetric_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_littlehorse_PartitionWindowedMetric_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_PartitionWindowedMetric_descriptor,
        new java.lang.String[] { "Value", "WindowStart", });
    internal_static_littlehorse_PartitionMetricId_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_littlehorse_PartitionMetricId_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_PartitionMetricId_descriptor,
        new java.lang.String[] { "Id", "TenantId", });
    internal_static_littlehorse_MetricRun_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_littlehorse_MetricRun_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_MetricRun_descriptor,
        new java.lang.String[] { "Id", "Value", "CreatedAt", "ValuePerPartition", });
    internal_static_littlehorse_MetricRun_ValuePerPartitionEntry_descriptor =
      internal_static_littlehorse_MetricRun_descriptor.getNestedTypes().get(0);
    internal_static_littlehorse_MetricRun_ValuePerPartitionEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_MetricRun_ValuePerPartitionEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    com.google.protobuf.TimestampProto.getDescriptor();
    com.google.protobuf.DurationProto.getDescriptor();
    io.littlehorse.sdk.common.proto.ObjectId.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
