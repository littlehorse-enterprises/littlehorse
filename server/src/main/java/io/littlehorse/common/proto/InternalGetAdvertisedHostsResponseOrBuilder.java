// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: interactive_query.proto

package io.littlehorse.common.proto;

public interface InternalGetAdvertisedHostsResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.InternalGetAdvertisedHostsResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>map&lt;string, .littlehorse.HostInfo&gt; hosts = 1;</code>
   */
  int getHostsCount();
  /**
   * <code>map&lt;string, .littlehorse.HostInfo&gt; hosts = 1;</code>
   */
  boolean containsHosts(
      java.lang.String key);
  /**
   * Use {@link #getHostsMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.LHHostInfo>
  getHosts();
  /**
   * <code>map&lt;string, .littlehorse.HostInfo&gt; hosts = 1;</code>
   */
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.LHHostInfo>
  getHostsMap();
  /**
   * <code>map&lt;string, .littlehorse.HostInfo&gt; hosts = 1;</code>
   */
  /* nullable */
io.littlehorse.sdk.common.proto.LHHostInfo getHostsOrDefault(
      java.lang.String key,
      /* nullable */
io.littlehorse.sdk.common.proto.LHHostInfo defaultValue);
  /**
   * <code>map&lt;string, .littlehorse.HostInfo&gt; hosts = 1;</code>
   */
  io.littlehorse.sdk.common.proto.LHHostInfo getHostsOrThrow(
      java.lang.String key);
}
