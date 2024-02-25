// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: node_run.proto

package io.littlehorse.sdk.common.proto;

public interface FailureOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.Failure)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The name of the failure. LittleHorse has certain built-in failures, all named in
   * UPPER_UNDERSCORE_CASE. Such failures correspond with the `LHStatus.ERROR`.
   *
   * Any Failure named in `kebab-case` is a user-defined business `EXCEPTION`, treated
   * as an `LHStatus.EXCEPTION`.
   * </pre>
   *
   * <code>string failure_name = 1;</code>
   * @return The failureName.
   */
  java.lang.String getFailureName();
  /**
   * <pre>
   * The name of the failure. LittleHorse has certain built-in failures, all named in
   * UPPER_UNDERSCORE_CASE. Such failures correspond with the `LHStatus.ERROR`.
   *
   * Any Failure named in `kebab-case` is a user-defined business `EXCEPTION`, treated
   * as an `LHStatus.EXCEPTION`.
   * </pre>
   *
   * <code>string failure_name = 1;</code>
   * @return The bytes for failureName.
   */
  com.google.protobuf.ByteString
      getFailureNameBytes();

  /**
   * <pre>
   * The human-readable message associated with this Failure.
   * </pre>
   *
   * <code>string message = 2;</code>
   * @return The message.
   */
  java.lang.String getMessage();
  /**
   * <pre>
   * The human-readable message associated with this Failure.
   * </pre>
   *
   * <code>string message = 2;</code>
   * @return The bytes for message.
   */
  com.google.protobuf.ByteString
      getMessageBytes();

  /**
   * <pre>
   * A user-defined Failure can have a value; for example, in Java an Exception is an
   * Object with arbitrary properties and behaviors.
   *
   * Future versions of LH will allow FailureHandler threads to accept that value as
   * an input variable.
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue content = 3;</code>
   * @return Whether the content field is set.
   */
  boolean hasContent();
  /**
   * <pre>
   * A user-defined Failure can have a value; for example, in Java an Exception is an
   * Object with arbitrary properties and behaviors.
   *
   * Future versions of LH will allow FailureHandler threads to accept that value as
   * an input variable.
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue content = 3;</code>
   * @return The content.
   */
  io.littlehorse.sdk.common.proto.VariableValue getContent();
  /**
   * <pre>
   * A user-defined Failure can have a value; for example, in Java an Exception is an
   * Object with arbitrary properties and behaviors.
   *
   * Future versions of LH will allow FailureHandler threads to accept that value as
   * an input variable.
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue content = 3;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValueOrBuilder getContentOrBuilder();

  /**
   * <pre>
   * A boolean denoting whether a Failure Handler ThreadRun properly handled the Failure.
   * </pre>
   *
   * <code>bool was_properly_handled = 4;</code>
   * @return The wasProperlyHandled.
   */
  boolean getWasProperlyHandled();

  /**
   * <pre>
   * If there is a defined failure handler for the NodeRun, then this field is set to the
   * id of the failure handler thread run.
   * </pre>
   *
   * <code>optional int32 failure_handler_threadrun_id = 5;</code>
   * @return Whether the failureHandlerThreadrunId field is set.
   */
  boolean hasFailureHandlerThreadrunId();
  /**
   * <pre>
   * If there is a defined failure handler for the NodeRun, then this field is set to the
   * id of the failure handler thread run.
   * </pre>
   *
   * <code>optional int32 failure_handler_threadrun_id = 5;</code>
   * @return The failureHandlerThreadrunId.
   */
  int getFailureHandlerThreadrunId();
}
