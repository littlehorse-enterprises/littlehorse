// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: interactive_query.proto

package io.littlehorse.common.proto;

public interface GetObjectRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.GetObjectRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.GetableClassEnum object_type = 1;</code>
   * @return The enum numeric value on the wire for objectType.
   */
  int getObjectTypeValue();
  /**
   * <code>.littlehorse.GetableClassEnum object_type = 1;</code>
   * @return The objectType.
   */
  io.littlehorse.common.proto.GetableClassEnum getObjectType();

  /**
   * <code>string object_id = 2;</code>
   * @return The objectId.
   */
  java.lang.String getObjectId();
  /**
   * <code>string object_id = 2;</code>
   * @return The bytes for objectId.
   */
  com.google.protobuf.ByteString
      getObjectIdBytes();
}
