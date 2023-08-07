// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: internal_server.proto

package io.littlehorse.common.proto;

/**
 * <pre>
 * This is currently used by the UserTaskRun ActionTrigger, it could be potentially
 * extended in the future to allow scheduling one-off tasks.
 * </pre>
 *
 * Protobuf type {@code littlehorse.TriggeredTaskRunPb}
 */
public final class TriggeredTaskRunPb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.TriggeredTaskRunPb)
    TriggeredTaskRunPbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use TriggeredTaskRunPb.newBuilder() to construct.
  private TriggeredTaskRunPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private TriggeredTaskRunPb() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new TriggeredTaskRunPb();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_TriggeredTaskRunPb_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_TriggeredTaskRunPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.TriggeredTaskRunPb.class, io.littlehorse.common.proto.TriggeredTaskRunPb.Builder.class);
  }

  public static final int TASK_TO_SCHEDULE_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.TaskNodePb taskToSchedule_;
  /**
   * <code>.littlehorse.TaskNodePb task_to_schedule = 1;</code>
   * @return Whether the taskToSchedule field is set.
   */
  @java.lang.Override
  public boolean hasTaskToSchedule() {
    return taskToSchedule_ != null;
  }
  /**
   * <code>.littlehorse.TaskNodePb task_to_schedule = 1;</code>
   * @return The taskToSchedule.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TaskNodePb getTaskToSchedule() {
    return taskToSchedule_ == null ? io.littlehorse.sdk.common.proto.TaskNodePb.getDefaultInstance() : taskToSchedule_;
  }
  /**
   * <code>.littlehorse.TaskNodePb task_to_schedule = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TaskNodePbOrBuilder getTaskToScheduleOrBuilder() {
    return taskToSchedule_ == null ? io.littlehorse.sdk.common.proto.TaskNodePb.getDefaultInstance() : taskToSchedule_;
  }

  public static final int SOURCE_FIELD_NUMBER = 2;
  private io.littlehorse.sdk.common.proto.NodeRunIdPb source_;
  /**
   * <code>.littlehorse.NodeRunIdPb source = 2;</code>
   * @return Whether the source field is set.
   */
  @java.lang.Override
  public boolean hasSource() {
    return source_ != null;
  }
  /**
   * <code>.littlehorse.NodeRunIdPb source = 2;</code>
   * @return The source.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.NodeRunIdPb getSource() {
    return source_ == null ? io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance() : source_;
  }
  /**
   * <code>.littlehorse.NodeRunIdPb source = 2;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder getSourceOrBuilder() {
    return source_ == null ? io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance() : source_;
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
    if (taskToSchedule_ != null) {
      output.writeMessage(1, getTaskToSchedule());
    }
    if (source_ != null) {
      output.writeMessage(2, getSource());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (taskToSchedule_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getTaskToSchedule());
    }
    if (source_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getSource());
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
    if (!(obj instanceof io.littlehorse.common.proto.TriggeredTaskRunPb)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.TriggeredTaskRunPb other = (io.littlehorse.common.proto.TriggeredTaskRunPb) obj;

    if (hasTaskToSchedule() != other.hasTaskToSchedule()) return false;
    if (hasTaskToSchedule()) {
      if (!getTaskToSchedule()
          .equals(other.getTaskToSchedule())) return false;
    }
    if (hasSource() != other.hasSource()) return false;
    if (hasSource()) {
      if (!getSource()
          .equals(other.getSource())) return false;
    }
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
    if (hasTaskToSchedule()) {
      hash = (37 * hash) + TASK_TO_SCHEDULE_FIELD_NUMBER;
      hash = (53 * hash) + getTaskToSchedule().hashCode();
    }
    if (hasSource()) {
      hash = (37 * hash) + SOURCE_FIELD_NUMBER;
      hash = (53 * hash) + getSource().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.TriggeredTaskRunPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.TriggeredTaskRunPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.TriggeredTaskRunPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.TriggeredTaskRunPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.TriggeredTaskRunPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.TriggeredTaskRunPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.TriggeredTaskRunPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.TriggeredTaskRunPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.TriggeredTaskRunPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.TriggeredTaskRunPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.TriggeredTaskRunPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.TriggeredTaskRunPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.TriggeredTaskRunPb prototype) {
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
   * <pre>
   * This is currently used by the UserTaskRun ActionTrigger, it could be potentially
   * extended in the future to allow scheduling one-off tasks.
   * </pre>
   *
   * Protobuf type {@code littlehorse.TriggeredTaskRunPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.TriggeredTaskRunPb)
      io.littlehorse.common.proto.TriggeredTaskRunPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_TriggeredTaskRunPb_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_TriggeredTaskRunPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.TriggeredTaskRunPb.class, io.littlehorse.common.proto.TriggeredTaskRunPb.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.TriggeredTaskRunPb.newBuilder()
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
      taskToSchedule_ = null;
      if (taskToScheduleBuilder_ != null) {
        taskToScheduleBuilder_.dispose();
        taskToScheduleBuilder_ = null;
      }
      source_ = null;
      if (sourceBuilder_ != null) {
        sourceBuilder_.dispose();
        sourceBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_TriggeredTaskRunPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.TriggeredTaskRunPb getDefaultInstanceForType() {
      return io.littlehorse.common.proto.TriggeredTaskRunPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.TriggeredTaskRunPb build() {
      io.littlehorse.common.proto.TriggeredTaskRunPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.TriggeredTaskRunPb buildPartial() {
      io.littlehorse.common.proto.TriggeredTaskRunPb result = new io.littlehorse.common.proto.TriggeredTaskRunPb(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.common.proto.TriggeredTaskRunPb result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.taskToSchedule_ = taskToScheduleBuilder_ == null
            ? taskToSchedule_
            : taskToScheduleBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.source_ = sourceBuilder_ == null
            ? source_
            : sourceBuilder_.build();
      }
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
      if (other instanceof io.littlehorse.common.proto.TriggeredTaskRunPb) {
        return mergeFrom((io.littlehorse.common.proto.TriggeredTaskRunPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.TriggeredTaskRunPb other) {
      if (other == io.littlehorse.common.proto.TriggeredTaskRunPb.getDefaultInstance()) return this;
      if (other.hasTaskToSchedule()) {
        mergeTaskToSchedule(other.getTaskToSchedule());
      }
      if (other.hasSource()) {
        mergeSource(other.getSource());
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
            case 10: {
              input.readMessage(
                  getTaskToScheduleFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getSourceFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
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

    private io.littlehorse.sdk.common.proto.TaskNodePb taskToSchedule_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.TaskNodePb, io.littlehorse.sdk.common.proto.TaskNodePb.Builder, io.littlehorse.sdk.common.proto.TaskNodePbOrBuilder> taskToScheduleBuilder_;
    /**
     * <code>.littlehorse.TaskNodePb task_to_schedule = 1;</code>
     * @return Whether the taskToSchedule field is set.
     */
    public boolean hasTaskToSchedule() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.littlehorse.TaskNodePb task_to_schedule = 1;</code>
     * @return The taskToSchedule.
     */
    public io.littlehorse.sdk.common.proto.TaskNodePb getTaskToSchedule() {
      if (taskToScheduleBuilder_ == null) {
        return taskToSchedule_ == null ? io.littlehorse.sdk.common.proto.TaskNodePb.getDefaultInstance() : taskToSchedule_;
      } else {
        return taskToScheduleBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.TaskNodePb task_to_schedule = 1;</code>
     */
    public Builder setTaskToSchedule(io.littlehorse.sdk.common.proto.TaskNodePb value) {
      if (taskToScheduleBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        taskToSchedule_ = value;
      } else {
        taskToScheduleBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TaskNodePb task_to_schedule = 1;</code>
     */
    public Builder setTaskToSchedule(
        io.littlehorse.sdk.common.proto.TaskNodePb.Builder builderForValue) {
      if (taskToScheduleBuilder_ == null) {
        taskToSchedule_ = builderForValue.build();
      } else {
        taskToScheduleBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TaskNodePb task_to_schedule = 1;</code>
     */
    public Builder mergeTaskToSchedule(io.littlehorse.sdk.common.proto.TaskNodePb value) {
      if (taskToScheduleBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          taskToSchedule_ != null &&
          taskToSchedule_ != io.littlehorse.sdk.common.proto.TaskNodePb.getDefaultInstance()) {
          getTaskToScheduleBuilder().mergeFrom(value);
        } else {
          taskToSchedule_ = value;
        }
      } else {
        taskToScheduleBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TaskNodePb task_to_schedule = 1;</code>
     */
    public Builder clearTaskToSchedule() {
      bitField0_ = (bitField0_ & ~0x00000001);
      taskToSchedule_ = null;
      if (taskToScheduleBuilder_ != null) {
        taskToScheduleBuilder_.dispose();
        taskToScheduleBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TaskNodePb task_to_schedule = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.TaskNodePb.Builder getTaskToScheduleBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getTaskToScheduleFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.TaskNodePb task_to_schedule = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.TaskNodePbOrBuilder getTaskToScheduleOrBuilder() {
      if (taskToScheduleBuilder_ != null) {
        return taskToScheduleBuilder_.getMessageOrBuilder();
      } else {
        return taskToSchedule_ == null ?
            io.littlehorse.sdk.common.proto.TaskNodePb.getDefaultInstance() : taskToSchedule_;
      }
    }
    /**
     * <code>.littlehorse.TaskNodePb task_to_schedule = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.TaskNodePb, io.littlehorse.sdk.common.proto.TaskNodePb.Builder, io.littlehorse.sdk.common.proto.TaskNodePbOrBuilder> 
        getTaskToScheduleFieldBuilder() {
      if (taskToScheduleBuilder_ == null) {
        taskToScheduleBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.TaskNodePb, io.littlehorse.sdk.common.proto.TaskNodePb.Builder, io.littlehorse.sdk.common.proto.TaskNodePbOrBuilder>(
                getTaskToSchedule(),
                getParentForChildren(),
                isClean());
        taskToSchedule_ = null;
      }
      return taskToScheduleBuilder_;
    }

    private io.littlehorse.sdk.common.proto.NodeRunIdPb source_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.NodeRunIdPb, io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder, io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder> sourceBuilder_;
    /**
     * <code>.littlehorse.NodeRunIdPb source = 2;</code>
     * @return Whether the source field is set.
     */
    public boolean hasSource() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 2;</code>
     * @return The source.
     */
    public io.littlehorse.sdk.common.proto.NodeRunIdPb getSource() {
      if (sourceBuilder_ == null) {
        return source_ == null ? io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance() : source_;
      } else {
        return sourceBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 2;</code>
     */
    public Builder setSource(io.littlehorse.sdk.common.proto.NodeRunIdPb value) {
      if (sourceBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        source_ = value;
      } else {
        sourceBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 2;</code>
     */
    public Builder setSource(
        io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder builderForValue) {
      if (sourceBuilder_ == null) {
        source_ = builderForValue.build();
      } else {
        sourceBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 2;</code>
     */
    public Builder mergeSource(io.littlehorse.sdk.common.proto.NodeRunIdPb value) {
      if (sourceBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          source_ != null &&
          source_ != io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance()) {
          getSourceBuilder().mergeFrom(value);
        } else {
          source_ = value;
        }
      } else {
        sourceBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 2;</code>
     */
    public Builder clearSource() {
      bitField0_ = (bitField0_ & ~0x00000002);
      source_ = null;
      if (sourceBuilder_ != null) {
        sourceBuilder_.dispose();
        sourceBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 2;</code>
     */
    public io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder getSourceBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getSourceFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 2;</code>
     */
    public io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder getSourceOrBuilder() {
      if (sourceBuilder_ != null) {
        return sourceBuilder_.getMessageOrBuilder();
      } else {
        return source_ == null ?
            io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance() : source_;
      }
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.NodeRunIdPb, io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder, io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder> 
        getSourceFieldBuilder() {
      if (sourceBuilder_ == null) {
        sourceBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.NodeRunIdPb, io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder, io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder>(
                getSource(),
                getParentForChildren(),
                isClean());
        source_ = null;
      }
      return sourceBuilder_;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.TriggeredTaskRunPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.TriggeredTaskRunPb)
  private static final io.littlehorse.common.proto.TriggeredTaskRunPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.TriggeredTaskRunPb();
  }

  public static io.littlehorse.common.proto.TriggeredTaskRunPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<TriggeredTaskRunPb>
      PARSER = new com.google.protobuf.AbstractParser<TriggeredTaskRunPb>() {
    @java.lang.Override
    public TriggeredTaskRunPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<TriggeredTaskRunPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<TriggeredTaskRunPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.TriggeredTaskRunPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

