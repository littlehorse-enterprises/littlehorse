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
    internal_static_littlehorse_MetricId_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_MetricId_fieldAccessorTable;
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
      "uration.proto\"\215\001\n\006Metric\022!\n\002id\030\001 \001(\0132\025.l" +
      "ittlehorse.MetricId\022.\n\ncreated_at\030\002 \001(\0132" +
      "\032.google.protobuf.Timestamp\0220\n\rwindow_le" +
      "ngth\030\003 \001(\0132\031.google.protobuf.Duration\"d\n" +
      "\010MetricId\0221\n\nmeasurable\030\001 \001(\0162\035.littleho" +
      "rse.MeasurableObject\022%\n\004type\030\002 \001(\0162\027.lit" +
      "tlehorse.MetricType\"\335\001\n\017PartitionMetric\022" +
      "*\n\002id\030\001 \001(\0132\036.littlehorse.PartitionMetri" +
      "cId\022.\n\ncreated_at\030\002 \001(\0132\032.google.protobu" +
      "f.Timestamp\022<\n\016active_windows\030\003 \003(\0132$.li" +
      "ttlehorse.PartitionWindowedMetric\0220\n\rwin" +
      "dow_length\030\004 \001(\0132\031.google.protobuf.Durat" +
      "ion\"Z\n\027PartitionWindowedMetric\022\r\n\005value\030" +
      "\001 \001(\001\0220\n\014window_start\030\002 \001(\0132\032.google.pro" +
      "tobuf.Timestamp\"6\n\021PartitionMetricId\022!\n\002" +
      "id\030\001 \001(\0132\025.littlehorse.MetricId**\n\020Measu" +
      "rableObject\022\014\n\010WORKFLOW\020\000\022\010\n\004TASK\020\001*+\n\nM" +
      "etricType\022\t\n\005COUNT\020\000\022\007\n\003AVG\020\001\022\t\n\005RATIO\020\002" +
      "BM\n\037io.littlehorse.sdk.common.protoP\001Z\t." +
      ";lhproto\252\002\034LittleHorse.Sdk.Common.Protob" +
      "\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          com.google.protobuf.DurationProto.getDescriptor(),
        });
    internal_static_littlehorse_Metric_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_Metric_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_Metric_descriptor,
        new java.lang.String[] { "Id", "CreatedAt", "WindowLength", });
    internal_static_littlehorse_MetricId_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_MetricId_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_MetricId_descriptor,
        new java.lang.String[] { "Measurable", "Type", });
    internal_static_littlehorse_PartitionMetric_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_littlehorse_PartitionMetric_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_PartitionMetric_descriptor,
        new java.lang.String[] { "Id", "CreatedAt", "ActiveWindows", "WindowLength", });
    internal_static_littlehorse_PartitionWindowedMetric_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_littlehorse_PartitionWindowedMetric_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_PartitionWindowedMetric_descriptor,
        new java.lang.String[] { "Value", "WindowStart", });
    internal_static_littlehorse_PartitionMetricId_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_littlehorse_PartitionMetricId_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_PartitionMetricId_descriptor,
        new java.lang.String[] { "Id", });
    com.google.protobuf.TimestampProto.getDescriptor();
    com.google.protobuf.DurationProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
