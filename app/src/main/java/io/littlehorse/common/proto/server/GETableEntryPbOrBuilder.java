// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package io.littlehorse.common.proto.server;

public interface GETableEntryPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:lh_proto.GETableEntryPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>bytes entry = 1;</code>
   * @return The entry.
   */
  com.google.protobuf.ByteString getEntry();

  /**
   * <code>int32 partition = 2;</code>
   * @return The partition.
   */
  int getPartition();

  /**
   * <code>int64 last_updated_offset = 3;</code>
   * @return The lastUpdatedOffset.
   */
  long getLastUpdatedOffset();
}
