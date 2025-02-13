// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: node_run.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

public interface SleepNodeRunOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.SleepNodeRun)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The time at which the NodeRun is *SCHEDULED TO* wake up. In rare cases, if
   * the LH Server is back-pressuring clients due to extreme load, the timer
   * event which marks the sleep node as "matured" may come in slightly late.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
   * @return Whether the maturationTime field is set.
   */
  boolean hasMaturationTime();
  /**
   * <pre>
   * The time at which the NodeRun is *SCHEDULED TO* wake up. In rare cases, if
   * the LH Server is back-pressuring clients due to extreme load, the timer
   * event which marks the sleep node as "matured" may come in slightly late.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
   * @return The maturationTime.
   */
  com.google.protobuf.Timestamp getMaturationTime();
  /**
   * <pre>
   * The time at which the NodeRun is *SCHEDULED TO* wake up. In rare cases, if
   * the LH Server is back-pressuring clients due to extreme load, the timer
   * event which marks the sleep node as "matured" may come in slightly late.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
   */
  com.google.protobuf.TimestampOrBuilder getMaturationTimeOrBuilder();

  /**
   * <pre>
   * Whether the SleepNodeRun has been matured.
   * </pre>
   *
   * <code>bool matured = 2;</code>
   * @return The matured.
   */
  boolean getMatured();
}
