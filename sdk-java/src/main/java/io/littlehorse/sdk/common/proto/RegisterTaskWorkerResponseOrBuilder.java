// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface RegisterTaskWorkerResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.RegisterTaskWorkerResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.LHHostInfo> 
      getYourHostsList();
  /**
   * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
   */
  io.littlehorse.sdk.common.proto.LHHostInfo getYourHosts(int index);
  /**
   * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
   */
  int getYourHostsCount();
  /**
   * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.LHHostInfoOrBuilder> 
      getYourHostsOrBuilderList();
  /**
   * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
   */
  io.littlehorse.sdk.common.proto.LHHostInfoOrBuilder getYourHostsOrBuilder(
      int index);

  /**
   * <code>optional bool is_cluster_healthy = 2;</code>
   * @return Whether the isClusterHealthy field is set.
   */
  boolean hasIsClusterHealthy();
  /**
   * <code>optional bool is_cluster_healthy = 2;</code>
   * @return The isClusterHealthy.
   */
  boolean getIsClusterHealthy();
}
