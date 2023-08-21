// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: internal_server.proto

package io.littlehorse.common.proto;

/**
 * Protobuf enum {@code littlehorse.ScanResultTypePb}
 */
public enum ScanResultTypePb
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <pre>
   * Just return the IDs of the objects.
   * </pre>
   *
   * <code>OBJECT_ID = 0;</code>
   */
  OBJECT_ID(0),
  /**
   * <pre>
   * Return the objects themselves. This can only be done for the object id prefix
   * scan type.
   * </pre>
   *
   * <code>OBJECT = 1;</code>
   */
  OBJECT(1),
  UNRECOGNIZED(-1),
  ;

  /**
   * <pre>
   * Just return the IDs of the objects.
   * </pre>
   *
   * <code>OBJECT_ID = 0;</code>
   */
  public static final int OBJECT_ID_VALUE = 0;
  /**
   * <pre>
   * Return the objects themselves. This can only be done for the object id prefix
   * scan type.
   * </pre>
   *
   * <code>OBJECT = 1;</code>
   */
  public static final int OBJECT_VALUE = 1;


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
  public static ScanResultTypePb valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static ScanResultTypePb forNumber(int value) {
    switch (value) {
      case 0: return OBJECT_ID;
      case 1: return OBJECT;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<ScanResultTypePb>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      ScanResultTypePb> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<ScanResultTypePb>() {
          public ScanResultTypePb findValueByNumber(int number) {
            return ScanResultTypePb.forNumber(number);
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
    return io.littlehorse.common.proto.InternalServer.getDescriptor().getEnumTypes().get(2);
  }

  private static final ScanResultTypePb[] VALUES = values();

  public static ScanResultTypePb valueOf(
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

  private ScanResultTypePb(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:littlehorse.ScanResultTypePb)
}

