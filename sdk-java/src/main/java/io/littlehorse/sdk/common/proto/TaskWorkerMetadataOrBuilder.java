// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface TaskWorkerMetadataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.TaskWorkerMetadata)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * User-defined identifier for the worker.
   * </pre>
   *
   * <code>string task_worker_id = 1;</code>
   * @return The taskWorkerId.
   */
  java.lang.String getTaskWorkerId();
  /**
   * <pre>
   * User-defined identifier for the worker.
   * </pre>
   *
   * <code>string task_worker_id = 1;</code>
   * @return The bytes for taskWorkerId.
   */
  com.google.protobuf.ByteString
      getTaskWorkerIdBytes();

  /**
   * <pre>
   * Timestamp indicating the last heartbeat sent by the worker.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp latest_heartbeat = 2;</code>
   * @return Whether the latestHeartbeat field is set.
   */
  boolean hasLatestHeartbeat();
  /**
   * <pre>
   * Timestamp indicating the last heartbeat sent by the worker.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp latest_heartbeat = 2;</code>
   * @return The latestHeartbeat.
   */
  com.google.protobuf.Timestamp getLatestHeartbeat();
  /**
   * <pre>
   * Timestamp indicating the last heartbeat sent by the worker.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp latest_heartbeat = 2;</code>
   */
  com.google.protobuf.TimestampOrBuilder getLatestHeartbeatOrBuilder();

  /**
   * <pre>
   * The host(s) where the worker is polling tasks
   * </pre>
   *
   * <code>repeated .littlehorse.LHHostInfo hosts = 3;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.LHHostInfo> 
      getHostsList();
  /**
   * <pre>
   * The host(s) where the worker is polling tasks
   * </pre>
   *
   * <code>repeated .littlehorse.LHHostInfo hosts = 3;</code>
   */
  io.littlehorse.sdk.common.proto.LHHostInfo getHosts(int index);
  /**
   * <pre>
   * The host(s) where the worker is polling tasks
   * </pre>
   *
   * <code>repeated .littlehorse.LHHostInfo hosts = 3;</code>
   */
  int getHostsCount();
  /**
   * <pre>
   * The host(s) where the worker is polling tasks
   * </pre>
   *
   * <code>repeated .littlehorse.LHHostInfo hosts = 3;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.LHHostInfoOrBuilder> 
      getHostsOrBuilderList();
  /**
   * <pre>
   * The host(s) where the worker is polling tasks
   * </pre>
   *
   * <code>repeated .littlehorse.LHHostInfo hosts = 3;</code>
   */
  io.littlehorse.sdk.common.proto.LHHostInfoOrBuilder getHostsOrBuilder(
      int index);
}
