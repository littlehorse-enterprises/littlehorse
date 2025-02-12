// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: common_enums.proto

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Status of a TaskRun.
 * </pre>
 *
 * Protobuf enum {@code littlehorse.TaskStatus}
 */
public enum TaskStatus
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <pre>
   * Scheduled in the Task Queue but not yet picked up by a Task Worker.
   * </pre>
   *
   * <code>TASK_SCHEDULED = 0;</code>
   */
  TASK_SCHEDULED(0),
  /**
   * <pre>
   * Picked up by a Task Worker, but not yet reported or timed out.
   * </pre>
   *
   * <code>TASK_RUNNING = 1;</code>
   */
  TASK_RUNNING(1),
  /**
   * <pre>
   * Successfully completed.
   * </pre>
   *
   * <code>TASK_SUCCESS = 2;</code>
   */
  TASK_SUCCESS(2),
  /**
   * <pre>
   * Task Worker reported a technical failure while attempting to execute the TaskRun
   * </pre>
   *
   * <code>TASK_FAILED = 3;</code>
   */
  TASK_FAILED(3),
  /**
   * <pre>
   * Task Worker did not report a result in time 
   * </pre>
   *
   * <code>TASK_TIMEOUT = 4;</code>
   */
  TASK_TIMEOUT(4),
  /**
   * <pre>
   * The output of the TaskRun was unable to be serialized or deserialized.
   * </pre>
   *
   * <code>TASK_OUTPUT_SERDE_ERROR = 5;</code>
   */
  TASK_OUTPUT_SERDE_ERROR(5),
  /**
   * <pre>
   * Task Worker was unable to deserialize the input variables into appropriate language-specific
   * objects to pass into the Task Function
   * </pre>
   *
   * <code>TASK_INPUT_VAR_SUB_ERROR = 6;</code>
   */
  TASK_INPUT_VAR_SUB_ERROR(6),
  /**
   * <pre>
   * Task Function business logic determined that there was a business exception.
   * </pre>
   *
   * <code>TASK_EXCEPTION = 8;</code>
   */
  TASK_EXCEPTION(8),
  /**
   * <pre>
   * Refers to a TaskAttempt that is not yet scheduled. This happens when using retries
   * with an ExponentialBackoffRetryPolicy: the TaskAttempt isn't supposed to be scheduled
   * until it "matures", but it does already exist.
   * </pre>
   *
   * <code>TASK_PENDING = 9;</code>
   */
  TASK_PENDING(9),
  UNRECOGNIZED(-1),
  ;

  /**
   * <pre>
   * Scheduled in the Task Queue but not yet picked up by a Task Worker.
   * </pre>
   *
   * <code>TASK_SCHEDULED = 0;</code>
   */
  public static final int TASK_SCHEDULED_VALUE = 0;
  /**
   * <pre>
   * Picked up by a Task Worker, but not yet reported or timed out.
   * </pre>
   *
   * <code>TASK_RUNNING = 1;</code>
   */
  public static final int TASK_RUNNING_VALUE = 1;
  /**
   * <pre>
   * Successfully completed.
   * </pre>
   *
   * <code>TASK_SUCCESS = 2;</code>
   */
  public static final int TASK_SUCCESS_VALUE = 2;
  /**
   * <pre>
   * Task Worker reported a technical failure while attempting to execute the TaskRun
   * </pre>
   *
   * <code>TASK_FAILED = 3;</code>
   */
  public static final int TASK_FAILED_VALUE = 3;
  /**
   * <pre>
   * Task Worker did not report a result in time 
   * </pre>
   *
   * <code>TASK_TIMEOUT = 4;</code>
   */
  public static final int TASK_TIMEOUT_VALUE = 4;
  /**
   * <pre>
   * The output of the TaskRun was unable to be serialized or deserialized.
   * </pre>
   *
   * <code>TASK_OUTPUT_SERDE_ERROR = 5;</code>
   */
  public static final int TASK_OUTPUT_SERDE_ERROR_VALUE = 5;
  /**
   * <pre>
   * Task Worker was unable to deserialize the input variables into appropriate language-specific
   * objects to pass into the Task Function
   * </pre>
   *
   * <code>TASK_INPUT_VAR_SUB_ERROR = 6;</code>
   */
  public static final int TASK_INPUT_VAR_SUB_ERROR_VALUE = 6;
  /**
   * <pre>
   * Task Function business logic determined that there was a business exception.
   * </pre>
   *
   * <code>TASK_EXCEPTION = 8;</code>
   */
  public static final int TASK_EXCEPTION_VALUE = 8;
  /**
   * <pre>
   * Refers to a TaskAttempt that is not yet scheduled. This happens when using retries
   * with an ExponentialBackoffRetryPolicy: the TaskAttempt isn't supposed to be scheduled
   * until it "matures", but it does already exist.
   * </pre>
   *
   * <code>TASK_PENDING = 9;</code>
   */
  public static final int TASK_PENDING_VALUE = 9;


  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static TaskStatus valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static TaskStatus forNumber(int value) {
    switch (value) {
      case 0: return TASK_SCHEDULED;
      case 1: return TASK_RUNNING;
      case 2: return TASK_SUCCESS;
      case 3: return TASK_FAILED;
      case 4: return TASK_TIMEOUT;
      case 5: return TASK_OUTPUT_SERDE_ERROR;
      case 6: return TASK_INPUT_VAR_SUB_ERROR;
      case 8: return TASK_EXCEPTION;
      case 9: return TASK_PENDING;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<TaskStatus>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      TaskStatus> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<TaskStatus>() {
          public TaskStatus findValueByNumber(int number) {
            return TaskStatus.forNumber(number);
          }
        };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor
      getValueDescriptor() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalStateException(
          "Can't get the descriptor of an unrecognized enum value.");
    }
    return getDescriptor().getValues().get(ordinal());
  }
  public final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptorForType() {
    return getDescriptor();
  }
  public static final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor().getEnumTypes().get(2);
  }

  private static final TaskStatus[] VALUES = values();

  public static TaskStatus valueOf(
      com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private TaskStatus(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:littlehorse.TaskStatus)
}

