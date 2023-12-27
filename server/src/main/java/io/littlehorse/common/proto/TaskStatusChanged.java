// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: command.proto

package io.littlehorse.common.proto;

/**
 * Protobuf type {@code littlehorse.TaskStatusChanged}
 */
public final class TaskStatusChanged extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.TaskStatusChanged)
    TaskStatusChangedOrBuilder {
private static final long serialVersionUID = 0L;
  // Use TaskStatusChanged.newBuilder() to construct.
  private TaskStatusChanged(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private TaskStatusChanged() {
    previousStatus_ = 0;
    newStatus_ = 0;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new TaskStatusChanged();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_TaskStatusChanged_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_TaskStatusChanged_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.TaskStatusChanged.class, io.littlehorse.common.proto.TaskStatusChanged.Builder.class);
  }

  private int bitField0_;
  public static final int PREVIOUS_STATUS_FIELD_NUMBER = 1;
  private int previousStatus_ = 0;
  /**
   * <code>optional .littlehorse.TaskStatus previous_status = 1;</code>
   * @return Whether the previousStatus field is set.
   */
  @java.lang.Override public boolean hasPreviousStatus() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional .littlehorse.TaskStatus previous_status = 1;</code>
   * @return The enum numeric value on the wire for previousStatus.
   */
  @java.lang.Override public int getPreviousStatusValue() {
    return previousStatus_;
  }
  /**
   * <code>optional .littlehorse.TaskStatus previous_status = 1;</code>
   * @return The previousStatus.
   */
  @java.lang.Override public io.littlehorse.sdk.common.proto.TaskStatus getPreviousStatus() {
    io.littlehorse.sdk.common.proto.TaskStatus result = io.littlehorse.sdk.common.proto.TaskStatus.forNumber(previousStatus_);
    return result == null ? io.littlehorse.sdk.common.proto.TaskStatus.UNRECOGNIZED : result;
  }

  public static final int NEW_STATUS_FIELD_NUMBER = 2;
  private int newStatus_ = 0;
  /**
   * <code>.littlehorse.TaskStatus new_status = 2;</code>
   * @return The enum numeric value on the wire for newStatus.
   */
  @java.lang.Override public int getNewStatusValue() {
    return newStatus_;
  }
  /**
   * <code>.littlehorse.TaskStatus new_status = 2;</code>
   * @return The newStatus.
   */
  @java.lang.Override public io.littlehorse.sdk.common.proto.TaskStatus getNewStatus() {
    io.littlehorse.sdk.common.proto.TaskStatus result = io.littlehorse.sdk.common.proto.TaskStatus.forNumber(newStatus_);
    return result == null ? io.littlehorse.sdk.common.proto.TaskStatus.UNRECOGNIZED : result;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (((bitField0_ & 0x00000001) != 0)) {
      output.writeEnum(1, previousStatus_);
    }
    if (newStatus_ != io.littlehorse.sdk.common.proto.TaskStatus.TASK_SCHEDULED.getNumber()) {
      output.writeEnum(2, newStatus_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(1, previousStatus_);
    }
    if (newStatus_ != io.littlehorse.sdk.common.proto.TaskStatus.TASK_SCHEDULED.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(2, newStatus_);
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof io.littlehorse.common.proto.TaskStatusChanged)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.TaskStatusChanged other = (io.littlehorse.common.proto.TaskStatusChanged) obj;

    if (hasPreviousStatus() != other.hasPreviousStatus()) return false;
    if (hasPreviousStatus()) {
      if (previousStatus_ != other.previousStatus_) return false;
    }
    if (newStatus_ != other.newStatus_) return false;
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (hasPreviousStatus()) {
      hash = (37 * hash) + PREVIOUS_STATUS_FIELD_NUMBER;
      hash = (53 * hash) + previousStatus_;
    }
    hash = (37 * hash) + NEW_STATUS_FIELD_NUMBER;
    hash = (53 * hash) + newStatus_;
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.TaskStatusChanged parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.TaskStatusChanged parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.TaskStatusChanged parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.TaskStatusChanged parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.TaskStatusChanged parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.TaskStatusChanged parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.TaskStatusChanged parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.TaskStatusChanged parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.TaskStatusChanged parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.TaskStatusChanged parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.TaskStatusChanged parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.TaskStatusChanged parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(io.littlehorse.common.proto.TaskStatusChanged prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code littlehorse.TaskStatusChanged}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.TaskStatusChanged)
      io.littlehorse.common.proto.TaskStatusChangedOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_TaskStatusChanged_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_TaskStatusChanged_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.TaskStatusChanged.class, io.littlehorse.common.proto.TaskStatusChanged.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.TaskStatusChanged.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      previousStatus_ = 0;
      newStatus_ = 0;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_TaskStatusChanged_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.TaskStatusChanged getDefaultInstanceForType() {
      return io.littlehorse.common.proto.TaskStatusChanged.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.TaskStatusChanged build() {
      io.littlehorse.common.proto.TaskStatusChanged result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.TaskStatusChanged buildPartial() {
      io.littlehorse.common.proto.TaskStatusChanged result = new io.littlehorse.common.proto.TaskStatusChanged(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.common.proto.TaskStatusChanged result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.previousStatus_ = previousStatus_;
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.newStatus_ = newStatus_;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.common.proto.TaskStatusChanged) {
        return mergeFrom((io.littlehorse.common.proto.TaskStatusChanged)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.TaskStatusChanged other) {
      if (other == io.littlehorse.common.proto.TaskStatusChanged.getDefaultInstance()) return this;
      if (other.hasPreviousStatus()) {
        setPreviousStatus(other.getPreviousStatus());
      }
      if (other.newStatus_ != 0) {
        setNewStatusValue(other.getNewStatusValue());
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {
              previousStatus_ = input.readEnum();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 16: {
              newStatus_ = input.readEnum();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }
    private int bitField0_;

    private int previousStatus_ = 0;
    /**
     * <code>optional .littlehorse.TaskStatus previous_status = 1;</code>
     * @return Whether the previousStatus field is set.
     */
    @java.lang.Override public boolean hasPreviousStatus() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>optional .littlehorse.TaskStatus previous_status = 1;</code>
     * @return The enum numeric value on the wire for previousStatus.
     */
    @java.lang.Override public int getPreviousStatusValue() {
      return previousStatus_;
    }
    /**
     * <code>optional .littlehorse.TaskStatus previous_status = 1;</code>
     * @param value The enum numeric value on the wire for previousStatus to set.
     * @return This builder for chaining.
     */
    public Builder setPreviousStatusValue(int value) {
      previousStatus_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>optional .littlehorse.TaskStatus previous_status = 1;</code>
     * @return The previousStatus.
     */
    @java.lang.Override
    public io.littlehorse.sdk.common.proto.TaskStatus getPreviousStatus() {
      io.littlehorse.sdk.common.proto.TaskStatus result = io.littlehorse.sdk.common.proto.TaskStatus.forNumber(previousStatus_);
      return result == null ? io.littlehorse.sdk.common.proto.TaskStatus.UNRECOGNIZED : result;
    }
    /**
     * <code>optional .littlehorse.TaskStatus previous_status = 1;</code>
     * @param value The previousStatus to set.
     * @return This builder for chaining.
     */
    public Builder setPreviousStatus(io.littlehorse.sdk.common.proto.TaskStatus value) {
      if (value == null) {
        throw new NullPointerException();
      }
      bitField0_ |= 0x00000001;
      previousStatus_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>optional .littlehorse.TaskStatus previous_status = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearPreviousStatus() {
      bitField0_ = (bitField0_ & ~0x00000001);
      previousStatus_ = 0;
      onChanged();
      return this;
    }

    private int newStatus_ = 0;
    /**
     * <code>.littlehorse.TaskStatus new_status = 2;</code>
     * @return The enum numeric value on the wire for newStatus.
     */
    @java.lang.Override public int getNewStatusValue() {
      return newStatus_;
    }
    /**
     * <code>.littlehorse.TaskStatus new_status = 2;</code>
     * @param value The enum numeric value on the wire for newStatus to set.
     * @return This builder for chaining.
     */
    public Builder setNewStatusValue(int value) {
      newStatus_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TaskStatus new_status = 2;</code>
     * @return The newStatus.
     */
    @java.lang.Override
    public io.littlehorse.sdk.common.proto.TaskStatus getNewStatus() {
      io.littlehorse.sdk.common.proto.TaskStatus result = io.littlehorse.sdk.common.proto.TaskStatus.forNumber(newStatus_);
      return result == null ? io.littlehorse.sdk.common.proto.TaskStatus.UNRECOGNIZED : result;
    }
    /**
     * <code>.littlehorse.TaskStatus new_status = 2;</code>
     * @param value The newStatus to set.
     * @return This builder for chaining.
     */
    public Builder setNewStatus(io.littlehorse.sdk.common.proto.TaskStatus value) {
      if (value == null) {
        throw new NullPointerException();
      }
      bitField0_ |= 0x00000002;
      newStatus_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TaskStatus new_status = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearNewStatus() {
      bitField0_ = (bitField0_ & ~0x00000002);
      newStatus_ = 0;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:littlehorse.TaskStatusChanged)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.TaskStatusChanged)
  private static final io.littlehorse.common.proto.TaskStatusChanged DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.TaskStatusChanged();
  }

  public static io.littlehorse.common.proto.TaskStatusChanged getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<TaskStatusChanged>
      PARSER = new com.google.protobuf.AbstractParser<TaskStatusChanged>() {
    @java.lang.Override
    public TaskStatusChanged parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  public static com.google.protobuf.Parser<TaskStatusChanged> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<TaskStatusChanged> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.TaskStatusChanged getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

