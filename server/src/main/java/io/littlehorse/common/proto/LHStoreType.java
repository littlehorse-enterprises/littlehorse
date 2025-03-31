// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: storage.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.common.proto;

/**
 * Protobuf enum {@code littlehorse.LHStoreType}
 */
public enum LHStoreType
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>CORE = 0;</code>
   */
  CORE(0),
  /**
   * <code>METADATA = 1;</code>
   */
  METADATA(1),
  /**
   * <code>REPARTITION = 2;</code>
   */
  REPARTITION(2),
  UNRECOGNIZED(-1),
  ;

  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      LHStoreType.class.getName());
  }
  /**
   * <code>CORE = 0;</code>
   */
  public static final int CORE_VALUE = 0;
  /**
   * <code>METADATA = 1;</code>
   */
  public static final int METADATA_VALUE = 1;
  /**
   * <code>REPARTITION = 2;</code>
   */
  public static final int REPARTITION_VALUE = 2;


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
  public static LHStoreType valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static LHStoreType forNumber(int value) {
    switch (value) {
      case 0: return CORE;
      case 1: return METADATA;
      case 2: return REPARTITION;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<LHStoreType>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      LHStoreType> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<LHStoreType>() {
          public LHStoreType findValueByNumber(int number) {
            return LHStoreType.forNumber(number);
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
    return io.littlehorse.common.proto.Storage.getDescriptor().getEnumTypes().get(0);
  }

  private static final LHStoreType[] VALUES = values();

  public static LHStoreType valueOf(
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

  private LHStoreType(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:littlehorse.LHStoreType)
}

