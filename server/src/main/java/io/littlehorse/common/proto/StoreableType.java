// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: storage.proto

package io.littlehorse.common.proto;

/**
 * Protobuf enum {@code littlehorse.StoreableType}
 */
public enum StoreableType
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>STORED_GETABLE = 0;</code>
   */
  STORED_GETABLE(0),
  /**
   * <code>SCHEDULED_TASK = 1;</code>
   */
  SCHEDULED_TASK(1),
  /**
   * <code>WF_METRIC_UPDATE = 2;</code>
   */
  WF_METRIC_UPDATE(2),
  /**
   * <code>TASK_METRIC_UPDATE = 3;</code>
   */
  TASK_METRIC_UPDATE(3),
  /**
   * <code>LH_TIMER = 4;</code>
   */
  LH_TIMER(4),
  /**
   * <code>TAG = 5;</code>
   */
  TAG(5),
  /**
   * <code>PARTITION_METRICS = 6;</code>
   */
  PARTITION_METRICS(6),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>STORED_GETABLE = 0;</code>
   */
  public static final int STORED_GETABLE_VALUE = 0;
  /**
   * <code>SCHEDULED_TASK = 1;</code>
   */
  public static final int SCHEDULED_TASK_VALUE = 1;
  /**
   * <code>WF_METRIC_UPDATE = 2;</code>
   */
  public static final int WF_METRIC_UPDATE_VALUE = 2;
  /**
   * <code>TASK_METRIC_UPDATE = 3;</code>
   */
  public static final int TASK_METRIC_UPDATE_VALUE = 3;
  /**
   * <code>LH_TIMER = 4;</code>
   */
  public static final int LH_TIMER_VALUE = 4;
  /**
   * <code>TAG = 5;</code>
   */
  public static final int TAG_VALUE = 5;
  /**
   * <code>PARTITION_METRICS = 6;</code>
   */
  public static final int PARTITION_METRICS_VALUE = 6;


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
  public static StoreableType valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static StoreableType forNumber(int value) {
    switch (value) {
      case 0: return STORED_GETABLE;
      case 1: return SCHEDULED_TASK;
      case 2: return WF_METRIC_UPDATE;
      case 3: return TASK_METRIC_UPDATE;
      case 4: return LH_TIMER;
      case 5: return TAG;
      case 6: return PARTITION_METRICS;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<StoreableType>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      StoreableType> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<StoreableType>() {
          public StoreableType findValueByNumber(int number) {
            return StoreableType.forNumber(number);
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
    return io.littlehorse.common.proto.Storage.getDescriptor().getEnumTypes().get(1);
  }

  private static final StoreableType[] VALUES = values();

  public static StoreableType valueOf(
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

  private StoreableType(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:littlehorse.StoreableType)
}

