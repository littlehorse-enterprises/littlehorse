// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: common_wfspec.proto

package io.littlehorse.sdk.common.proto;

public interface ExponentialBackoffRetryPolicyOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ExponentialBackoffRetryPolicy)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Base delay in ms for the first retry. Note that in LittleHorse, timers have a
   * resolution of 500-1000 milliseconds. Must be greater than zero.
   * </pre>
   *
   * <code>int32 base_interval_ms = 1;</code>
   * @return The baseIntervalMs.
   */
  int getBaseIntervalMs();

  /**
   * <pre>
   * Maximum delay in milliseconds between retries.
   * </pre>
   *
   * <code>int64 max_delay_ms = 2;</code>
   * @return The maxDelayMs.
   */
  long getMaxDelayMs();

  /**
   * <pre>
   * The multiplier to use in calculating the retry backoff policy. We recommend
   * starting with 2.0. Must be at least 1.0.
   * </pre>
   *
   * <code>float multiplier = 3;</code>
   * @return The multiplier.
   */
  float getMultiplier();
}
