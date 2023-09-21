// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: common_enums.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf enum {@code littlehorse.WaitForThreadsPolicy}
 */
public enum WaitForThreadsPolicy
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>STOP_ON_FAILURE = 0;</code>
   */
  STOP_ON_FAILURE(0),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>STOP_ON_FAILURE = 0;</code>
   */
  public static final int STOP_ON_FAILURE_VALUE = 0;


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
  public static WaitForThreadsPolicy valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static WaitForThreadsPolicy forNumber(int value) {
    switch (value) {
      case 0: return STOP_ON_FAILURE;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<WaitForThreadsPolicy>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      WaitForThreadsPolicy> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<WaitForThreadsPolicy>() {
          public WaitForThreadsPolicy findValueByNumber(int number) {
            return WaitForThreadsPolicy.forNumber(number);
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
    return io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor().getEnumTypes().get(5);
  }

  private static final WaitForThreadsPolicy[] VALUES = values();

  public static WaitForThreadsPolicy valueOf(
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

  private WaitForThreadsPolicy(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:littlehorse.WaitForThreadsPolicy)
}

