// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cluster_health.proto

package io.littlehorse.common.proto;

public interface StandByTaskStatePbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.StandByTaskStatePb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string host = 1;</code>
   * @return The host.
   */
  java.lang.String getHost();
  /**
   * <code>string host = 1;</code>
   * @return The bytes for host.
   */
  com.google.protobuf.ByteString
      getHostBytes();

  /**
   * <code>string task_id = 2;</code>
   * @return The taskId.
   */
  java.lang.String getTaskId();
  /**
   * <code>string task_id = 2;</code>
   * @return The bytes for taskId.
   */
  com.google.protobuf.ByteString
      getTaskIdBytes();

  /**
   * <code>int32 port = 3;</code>
   * @return The port.
   */
  int getPort();

  /**
   * <code>int64 current_offset = 4;</code>
   * @return The currentOffset.
   */
  long getCurrentOffset();

  /**
   * <code>int64 lag = 5;</code>
   * @return The lag.
   */
  long getLag();

  /**
   * <code>string rack_id = 6;</code>
   * @return The rackId.
   */
  java.lang.String getRackId();
  /**
   * <code>string rack_id = 6;</code>
   * @return The bytes for rackId.
   */
  com.google.protobuf.ByteString
      getRackIdBytes();
}
