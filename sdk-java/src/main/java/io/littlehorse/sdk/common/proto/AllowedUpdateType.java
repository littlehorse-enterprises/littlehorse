// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * This enum controls the behavior of a PutWfSpecRequest when a WfSpec with the same
 * name previously exists.
 * </pre>
 *
 * Protobuf enum {@code littlehorse.AllowedUpdateType}
 */
public enum AllowedUpdateType
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <pre>
   * Allows any update: both minor (revsion) changes and breaking (majorVersion) changes
   * are accepted
   * </pre>
   *
   * <code>ALL_UPDATES = 0;</code>
   */
  ALL_UPDATES(0),
  /**
   * <pre>
   * Allows only backwards-compatible changes that do not change the required input variables
   * or the searchable variables in the WfSpec.
   * </pre>
   *
   * <code>MINOR_REVISION_UPDATES = 1;</code>
   */
  MINOR_REVISION_UPDATES(1),
  /**
   * <pre>
   * Rejects any changes to the WfSpec.
   * </pre>
   *
   * <code>NO_UPDATES = 2;</code>
   */
  NO_UPDATES(2),
  UNRECOGNIZED(-1),
  ;

  /**
   * <pre>
   * Allows any update: both minor (revsion) changes and breaking (majorVersion) changes
   * are accepted
   * </pre>
   *
   * <code>ALL_UPDATES = 0;</code>
   */
  public static final int ALL_UPDATES_VALUE = 0;
  /**
   * <pre>
   * Allows only backwards-compatible changes that do not change the required input variables
   * or the searchable variables in the WfSpec.
   * </pre>
   *
   * <code>MINOR_REVISION_UPDATES = 1;</code>
   */
  public static final int MINOR_REVISION_UPDATES_VALUE = 1;
  /**
   * <pre>
   * Rejects any changes to the WfSpec.
   * </pre>
   *
   * <code>NO_UPDATES = 2;</code>
   */
  public static final int NO_UPDATES_VALUE = 2;


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
  public static AllowedUpdateType valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static AllowedUpdateType forNumber(int value) {
    switch (value) {
      case 0: return ALL_UPDATES;
      case 1: return MINOR_REVISION_UPDATES;
      case 2: return NO_UPDATES;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<AllowedUpdateType>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      AllowedUpdateType> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<AllowedUpdateType>() {
          public AllowedUpdateType findValueByNumber(int number) {
            return AllowedUpdateType.forNumber(number);
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
    return io.littlehorse.sdk.common.proto.Service.getDescriptor().getEnumTypes().get(0);
  }

  private static final AllowedUpdateType[] VALUES = values();

  public static AllowedUpdateType valueOf(
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

  private AllowedUpdateType(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:littlehorse.AllowedUpdateType)
}

