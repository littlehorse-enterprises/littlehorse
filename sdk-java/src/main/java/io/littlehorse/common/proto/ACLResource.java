// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: acls.proto

package io.littlehorse.common.proto;

/**
 * Protobuf enum {@code littlehorse.ACLResource}
 */
public enum ACLResource
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>ACL_WORKFLOW = 0;</code>
   */
  ACL_WORKFLOW(0),
  /**
   * <code>ACL_TASK = 1;</code>
   */
  ACL_TASK(1),
  /**
   * <code>ACL_EXTERNAL_EVENT = 2;</code>
   */
  ACL_EXTERNAL_EVENT(2),
  /**
   * <code>ACL_USER_TASK = 3;</code>
   */
  ACL_USER_TASK(3),
  /**
   * <code>ACL_PRINCIPAL = 4;</code>
   */
  ACL_PRINCIPAL(4),
  /**
   * <code>ACL_TENANT = 5;</code>
   */
  ACL_TENANT(5),
  /**
   * <code>ACL_ALL_RESOURCES = 6;</code>
   */
  ACL_ALL_RESOURCES(6),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>ACL_WORKFLOW = 0;</code>
   */
  public static final int ACL_WORKFLOW_VALUE = 0;
  /**
   * <code>ACL_TASK = 1;</code>
   */
  public static final int ACL_TASK_VALUE = 1;
  /**
   * <code>ACL_EXTERNAL_EVENT = 2;</code>
   */
  public static final int ACL_EXTERNAL_EVENT_VALUE = 2;
  /**
   * <code>ACL_USER_TASK = 3;</code>
   */
  public static final int ACL_USER_TASK_VALUE = 3;
  /**
   * <code>ACL_PRINCIPAL = 4;</code>
   */
  public static final int ACL_PRINCIPAL_VALUE = 4;
  /**
   * <code>ACL_TENANT = 5;</code>
   */
  public static final int ACL_TENANT_VALUE = 5;
  /**
   * <code>ACL_ALL_RESOURCES = 6;</code>
   */
  public static final int ACL_ALL_RESOURCES_VALUE = 6;


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
  public static ACLResource valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static ACLResource forNumber(int value) {
    switch (value) {
      case 0: return ACL_WORKFLOW;
      case 1: return ACL_TASK;
      case 2: return ACL_EXTERNAL_EVENT;
      case 3: return ACL_USER_TASK;
      case 4: return ACL_PRINCIPAL;
      case 5: return ACL_TENANT;
      case 6: return ACL_ALL_RESOURCES;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<ACLResource>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      ACLResource> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<ACLResource>() {
          public ACLResource findValueByNumber(int number) {
            return ACLResource.forNumber(number);
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
    return io.littlehorse.common.proto.Acls.getDescriptor().getEnumTypes().get(0);
  }

  private static final ACLResource[] VALUES = values();

  public static ACLResource valueOf(
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

  private ACLResource(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:littlehorse.ACLResource)
}

