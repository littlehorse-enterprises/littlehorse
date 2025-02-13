// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: common_enums.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Status of a Metadata Object, such as WfSpec or TaskDef
 * </pre>
 *
 * Protobuf enum {@code littlehorse.MetadataStatus}
 */
public enum MetadataStatus
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <pre>
   * ACTIVE means the object can be used.
   * </pre>
   *
   * <code>ACTIVE = 0;</code>
   */
  ACTIVE(0),
  /**
   * <pre>
   * An ARCHIVED WfSpec can no longer be used to create new WfRun's, but
   * existing WfRun's will be allowed to run to completion.
   * </pre>
   *
   * <code>ARCHIVED = 1;</code>
   */
  ARCHIVED(1),
  /**
   * <pre>
   * A TERMINATING WfSpec is actively deleting all running WfRun's, and will
   * self-destruct once all of its child WfRun's are terminated.
   * </pre>
   *
   * <code>TERMINATING = 2;</code>
   */
  TERMINATING(2),
  UNRECOGNIZED(-1),
  ;

  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 29,
      /* patch= */ 3,
      /* suffix= */ "",
      MetadataStatus.class.getName());
  }
  /**
   * <pre>
   * ACTIVE means the object can be used.
   * </pre>
   *
   * <code>ACTIVE = 0;</code>
   */
  public static final int ACTIVE_VALUE = 0;
  /**
   * <pre>
   * An ARCHIVED WfSpec can no longer be used to create new WfRun's, but
   * existing WfRun's will be allowed to run to completion.
   * </pre>
   *
   * <code>ARCHIVED = 1;</code>
   */
  public static final int ARCHIVED_VALUE = 1;
  /**
   * <pre>
   * A TERMINATING WfSpec is actively deleting all running WfRun's, and will
   * self-destruct once all of its child WfRun's are terminated.
   * </pre>
   *
   * <code>TERMINATING = 2;</code>
   */
  public static final int TERMINATING_VALUE = 2;


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
  public static MetadataStatus valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static MetadataStatus forNumber(int value) {
    switch (value) {
      case 0: return ACTIVE;
      case 1: return ARCHIVED;
      case 2: return TERMINATING;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<MetadataStatus>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      MetadataStatus> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<MetadataStatus>() {
          public MetadataStatus findValueByNumber(int number) {
            return MetadataStatus.forNumber(number);
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
    return io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor().getEnumTypes().get(1);
  }

  private static final MetadataStatus[] VALUES = values();

  public static MetadataStatus valueOf(
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

  private MetadataStatus(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:littlehorse.MetadataStatus)
}

