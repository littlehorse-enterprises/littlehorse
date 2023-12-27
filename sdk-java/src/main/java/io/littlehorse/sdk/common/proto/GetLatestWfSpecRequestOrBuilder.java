// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface GetLatestWfSpecRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.GetLatestWfSpecRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The name of the WfSpec to get. This is required.
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <pre>
   * The name of the WfSpec to get. This is required.
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <pre>
   * Optionally get only WfSpec's that have the same major version. This can be useful
   * if you want to guarantee that there have been no breaking changes to the API of the
   * WfSpec, for example, to ensure that there have been no changes to searchable variables
   * or required input variables.
   * </pre>
   *
   * <code>optional int32 major_version = 2;</code>
   * @return Whether the majorVersion field is set.
   */
  boolean hasMajorVersion();
  /**
   * <pre>
   * Optionally get only WfSpec's that have the same major version. This can be useful
   * if you want to guarantee that there have been no breaking changes to the API of the
   * WfSpec, for example, to ensure that there have been no changes to searchable variables
   * or required input variables.
   * </pre>
   *
   * <code>optional int32 major_version = 2;</code>
   * @return The majorVersion.
   */
  int getMajorVersion();
}
