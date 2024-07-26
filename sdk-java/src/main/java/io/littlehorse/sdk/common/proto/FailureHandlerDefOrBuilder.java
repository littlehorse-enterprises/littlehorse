// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: wf_spec.proto

package io.littlehorse.sdk.common.proto;

public interface FailureHandlerDefOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.FailureHandlerDef)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The name of the ThreadSpec to run as a
   * </pre>
   *
   * <code>string handler_spec_name = 2;</code>
   * @return The handlerSpecName.
   */
  java.lang.String getHandlerSpecName();
  /**
   * <pre>
   * The name of the ThreadSpec to run as a
   * </pre>
   *
   * <code>string handler_spec_name = 2;</code>
   * @return The bytes for handlerSpecName.
   */
  com.google.protobuf.ByteString
      getHandlerSpecNameBytes();

  /**
   * <pre>
   * Specifies that this FailureHandlerDef will be triggered for a Failure with this
   * exact name.
   *
   * If this and `specific_failure` are both unset, then any failure is caught.
   * </pre>
   *
   * <code>string specific_failure = 1;</code>
   * @return Whether the specificFailure field is set.
   */
  boolean hasSpecificFailure();
  /**
   * <pre>
   * Specifies that this FailureHandlerDef will be triggered for a Failure with this
   * exact name.
   *
   * If this and `specific_failure` are both unset, then any failure is caught.
   * </pre>
   *
   * <code>string specific_failure = 1;</code>
   * @return The specificFailure.
   */
  java.lang.String getSpecificFailure();
  /**
   * <pre>
   * Specifies that this FailureHandlerDef will be triggered for a Failure with this
   * exact name.
   *
   * If this and `specific_failure` are both unset, then any failure is caught.
   * </pre>
   *
   * <code>string specific_failure = 1;</code>
   * @return The bytes for specificFailure.
   */
  com.google.protobuf.ByteString
      getSpecificFailureBytes();

  /**
   * <pre>
   * Specifies that this FailureHandlerDef will be triggered for any failure matching
   * this type (ERROR or EXCEPTION).
   * </pre>
   *
   * <code>.littlehorse.FailureHandlerDef.LHFailureType any_failure_of_type = 3;</code>
   * @return Whether the anyFailureOfType field is set.
   */
  boolean hasAnyFailureOfType();
  /**
   * <pre>
   * Specifies that this FailureHandlerDef will be triggered for any failure matching
   * this type (ERROR or EXCEPTION).
   * </pre>
   *
   * <code>.littlehorse.FailureHandlerDef.LHFailureType any_failure_of_type = 3;</code>
   * @return The enum numeric value on the wire for anyFailureOfType.
   */
  int getAnyFailureOfTypeValue();
  /**
   * <pre>
   * Specifies that this FailureHandlerDef will be triggered for any failure matching
   * this type (ERROR or EXCEPTION).
   * </pre>
   *
   * <code>.littlehorse.FailureHandlerDef.LHFailureType any_failure_of_type = 3;</code>
   * @return The anyFailureOfType.
   */
  io.littlehorse.sdk.common.proto.FailureHandlerDef.LHFailureType getAnyFailureOfType();

  io.littlehorse.sdk.common.proto.FailureHandlerDef.FailureToCatchCase getFailureToCatchCase();
}
