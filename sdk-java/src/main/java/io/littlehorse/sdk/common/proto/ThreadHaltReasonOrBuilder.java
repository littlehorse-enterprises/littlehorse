// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: wf_run.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

public interface ThreadHaltReasonOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ThreadHaltReason)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Parent threadRun halted.
   * </pre>
   *
   * <code>.littlehorse.ParentHalted parent_halted = 1;</code>
   * @return Whether the parentHalted field is set.
   */
  boolean hasParentHalted();
  /**
   * <pre>
   * Parent threadRun halted.
   * </pre>
   *
   * <code>.littlehorse.ParentHalted parent_halted = 1;</code>
   * @return The parentHalted.
   */
  io.littlehorse.sdk.common.proto.ParentHalted getParentHalted();
  /**
   * <pre>
   * Parent threadRun halted.
   * </pre>
   *
   * <code>.littlehorse.ParentHalted parent_halted = 1;</code>
   */
  io.littlehorse.sdk.common.proto.ParentHaltedOrBuilder getParentHaltedOrBuilder();

  /**
   * <pre>
   * Handling an Interrupt.
   * </pre>
   *
   * <code>.littlehorse.Interrupted interrupted = 2;</code>
   * @return Whether the interrupted field is set.
   */
  boolean hasInterrupted();
  /**
   * <pre>
   * Handling an Interrupt.
   * </pre>
   *
   * <code>.littlehorse.Interrupted interrupted = 2;</code>
   * @return The interrupted.
   */
  io.littlehorse.sdk.common.proto.Interrupted getInterrupted();
  /**
   * <pre>
   * Handling an Interrupt.
   * </pre>
   *
   * <code>.littlehorse.Interrupted interrupted = 2;</code>
   */
  io.littlehorse.sdk.common.proto.InterruptedOrBuilder getInterruptedOrBuilder();

  /**
   * <pre>
   * Waiting to handle Interrupt.
   * </pre>
   *
   * <code>.littlehorse.PendingInterruptHaltReason pending_interrupt = 3;</code>
   * @return Whether the pendingInterrupt field is set.
   */
  boolean hasPendingInterrupt();
  /**
   * <pre>
   * Waiting to handle Interrupt.
   * </pre>
   *
   * <code>.littlehorse.PendingInterruptHaltReason pending_interrupt = 3;</code>
   * @return The pendingInterrupt.
   */
  io.littlehorse.sdk.common.proto.PendingInterruptHaltReason getPendingInterrupt();
  /**
   * <pre>
   * Waiting to handle Interrupt.
   * </pre>
   *
   * <code>.littlehorse.PendingInterruptHaltReason pending_interrupt = 3;</code>
   */
  io.littlehorse.sdk.common.proto.PendingInterruptHaltReasonOrBuilder getPendingInterruptOrBuilder();

  /**
   * <pre>
   * Waiting to handle a failure.
   * </pre>
   *
   * <code>.littlehorse.PendingFailureHandlerHaltReason pending_failure = 4;</code>
   * @return Whether the pendingFailure field is set.
   */
  boolean hasPendingFailure();
  /**
   * <pre>
   * Waiting to handle a failure.
   * </pre>
   *
   * <code>.littlehorse.PendingFailureHandlerHaltReason pending_failure = 4;</code>
   * @return The pendingFailure.
   */
  io.littlehorse.sdk.common.proto.PendingFailureHandlerHaltReason getPendingFailure();
  /**
   * <pre>
   * Waiting to handle a failure.
   * </pre>
   *
   * <code>.littlehorse.PendingFailureHandlerHaltReason pending_failure = 4;</code>
   */
  io.littlehorse.sdk.common.proto.PendingFailureHandlerHaltReasonOrBuilder getPendingFailureOrBuilder();

  /**
   * <pre>
   * Handling a failure.
   * </pre>
   *
   * <code>.littlehorse.HandlingFailureHaltReason handling_failure = 5;</code>
   * @return Whether the handlingFailure field is set.
   */
  boolean hasHandlingFailure();
  /**
   * <pre>
   * Handling a failure.
   * </pre>
   *
   * <code>.littlehorse.HandlingFailureHaltReason handling_failure = 5;</code>
   * @return The handlingFailure.
   */
  io.littlehorse.sdk.common.proto.HandlingFailureHaltReason getHandlingFailure();
  /**
   * <pre>
   * Handling a failure.
   * </pre>
   *
   * <code>.littlehorse.HandlingFailureHaltReason handling_failure = 5;</code>
   */
  io.littlehorse.sdk.common.proto.HandlingFailureHaltReasonOrBuilder getHandlingFailureOrBuilder();

  /**
   * <pre>
   * Manually stopped the WfRun.
   * </pre>
   *
   * <code>.littlehorse.ManualHalt manual_halt = 6;</code>
   * @return Whether the manualHalt field is set.
   */
  boolean hasManualHalt();
  /**
   * <pre>
   * Manually stopped the WfRun.
   * </pre>
   *
   * <code>.littlehorse.ManualHalt manual_halt = 6;</code>
   * @return The manualHalt.
   */
  io.littlehorse.sdk.common.proto.ManualHalt getManualHalt();
  /**
   * <pre>
   * Manually stopped the WfRun.
   * </pre>
   *
   * <code>.littlehorse.ManualHalt manual_halt = 6;</code>
   */
  io.littlehorse.sdk.common.proto.ManualHaltOrBuilder getManualHaltOrBuilder();

  io.littlehorse.sdk.common.proto.ThreadHaltReason.ReasonCase getReasonCase();
}
