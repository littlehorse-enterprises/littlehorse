// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: acls.proto

package io.littlehorse.common.proto;

/**
 * Protobuf enum {@code littlehorse.ACLAction}
 */
public enum ACLAction
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>READ = 0;</code>
   */
  READ(0),
  /**
   * <code>EXECUTE = 1;</code>
   */
  EXECUTE(1),
  /**
   * <code>WRITE_METADATA = 2;</code>
   */
  WRITE_METADATA(2),
  /**
   * <code>ALL_ACTIONS = 3;</code>
   */
  ALL_ACTIONS(3),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>READ = 0;</code>
   */
  public static final int READ_VALUE = 0;
  /**
   * <code>EXECUTE = 1;</code>
   */
  public static final int EXECUTE_VALUE = 1;
  /**
   * <code>WRITE_METADATA = 2;</code>
   */
  public static final int WRITE_METADATA_VALUE = 2;
  /**
   * <code>ALL_ACTIONS = 3;</code>
   */
  public static final int ALL_ACTIONS_VALUE = 3;


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
  public static ACLAction valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static ACLAction forNumber(int value) {
    switch (value) {
      case 0: return READ;
      case 1: return EXECUTE;
      case 2: return WRITE_METADATA;
      case 3: return ALL_ACTIONS;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<ACLAction>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      ACLAction> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<ACLAction>() {
          public ACLAction findValueByNumber(int number) {
            return ACLAction.forNumber(number);
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
    return io.littlehorse.common.proto.Acls.getDescriptor().getEnumTypes().get(1);
  }

  private static final ACLAction[] VALUES = values();

  public static ACLAction valueOf(
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

  private ACLAction(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:littlehorse.ACLAction)
}

