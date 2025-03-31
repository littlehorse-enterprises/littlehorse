// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: object_id.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * ID for a specific window of TaskDef metrics.
 * </pre>
 *
 * Protobuf type {@code littlehorse.TaskDefMetricsId}
 */
public final class TaskDefMetricsId extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.TaskDefMetricsId)
    TaskDefMetricsIdOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      TaskDefMetricsId.class.getName());
  }
  // Use TaskDefMetricsId.newBuilder() to construct.
  private TaskDefMetricsId(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private TaskDefMetricsId() {
    windowType_ = 0;
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_TaskDefMetricsId_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_TaskDefMetricsId_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.TaskDefMetricsId.class, io.littlehorse.sdk.common.proto.TaskDefMetricsId.Builder.class);
  }

  private int bitField0_;
  public static final int WINDOW_START_FIELD_NUMBER = 1;
  private com.google.protobuf.Timestamp windowStart_;
  /**
   * <pre>
   * The timestamp at which this metrics window starts.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp window_start = 1;</code>
   * @return Whether the windowStart field is set.
   */
  @java.lang.Override
  public boolean hasWindowStart() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * The timestamp at which this metrics window starts.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp window_start = 1;</code>
   * @return The windowStart.
   */
  @java.lang.Override
  public com.google.protobuf.Timestamp getWindowStart() {
    return windowStart_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : windowStart_;
  }
  /**
   * <pre>
   * The timestamp at which this metrics window starts.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp window_start = 1;</code>
   */
  @java.lang.Override
  public com.google.protobuf.TimestampOrBuilder getWindowStartOrBuilder() {
    return windowStart_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : windowStart_;
  }

  public static final int WINDOW_TYPE_FIELD_NUMBER = 2;
  private int windowType_ = 0;
  /**
   * <pre>
   * The length of this window.
   * </pre>
   *
   * <code>.littlehorse.MetricsWindowLength window_type = 2;</code>
   * @return The enum numeric value on the wire for windowType.
   */
  @java.lang.Override public int getWindowTypeValue() {
    return windowType_;
  }
  /**
   * <pre>
   * The length of this window.
   * </pre>
   *
   * <code>.littlehorse.MetricsWindowLength window_type = 2;</code>
   * @return The windowType.
   */
  @java.lang.Override public io.littlehorse.sdk.common.proto.MetricsWindowLength getWindowType() {
    io.littlehorse.sdk.common.proto.MetricsWindowLength result = io.littlehorse.sdk.common.proto.MetricsWindowLength.forNumber(windowType_);
    return result == null ? io.littlehorse.sdk.common.proto.MetricsWindowLength.UNRECOGNIZED : result;
  }

  public static final int TASK_DEF_ID_FIELD_NUMBER = 3;
  private io.littlehorse.sdk.common.proto.TaskDefId taskDefId_;
  /**
   * <pre>
   * The TaskDefId that this metrics window reports on.
   * </pre>
   *
   * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
   * @return Whether the taskDefId field is set.
   */
  @java.lang.Override
  public boolean hasTaskDefId() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <pre>
   * The TaskDefId that this metrics window reports on.
   * </pre>
   *
   * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
   * @return The taskDefId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TaskDefId getTaskDefId() {
    return taskDefId_ == null ? io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance() : taskDefId_;
  }
  /**
   * <pre>
   * The TaskDefId that this metrics window reports on.
   * </pre>
   *
   * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder getTaskDefIdOrBuilder() {
    return taskDefId_ == null ? io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance() : taskDefId_;
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
      output.writeMessage(1, getWindowStart());
    }
    if (windowType_ != io.littlehorse.sdk.common.proto.MetricsWindowLength.MINUTES_5.getNumber()) {
      output.writeEnum(2, windowType_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      output.writeMessage(3, getTaskDefId());
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
        .computeMessageSize(1, getWindowStart());
    }
    if (windowType_ != io.littlehorse.sdk.common.proto.MetricsWindowLength.MINUTES_5.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(2, windowType_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, getTaskDefId());
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.TaskDefMetricsId)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.TaskDefMetricsId other = (io.littlehorse.sdk.common.proto.TaskDefMetricsId) obj;

    if (hasWindowStart() != other.hasWindowStart()) return false;
    if (hasWindowStart()) {
      if (!getWindowStart()
          .equals(other.getWindowStart())) return false;
    }
    if (windowType_ != other.windowType_) return false;
    if (hasTaskDefId() != other.hasTaskDefId()) return false;
    if (hasTaskDefId()) {
      if (!getTaskDefId()
          .equals(other.getTaskDefId())) return false;
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
    if (hasWindowStart()) {
      hash = (37 * hash) + WINDOW_START_FIELD_NUMBER;
      hash = (53 * hash) + getWindowStart().hashCode();
    }
    hash = (37 * hash) + WINDOW_TYPE_FIELD_NUMBER;
    hash = (53 * hash) + windowType_;
    if (hasTaskDefId()) {
      hash = (37 * hash) + TASK_DEF_ID_FIELD_NUMBER;
      hash = (53 * hash) + getTaskDefId().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.TaskDefMetricsId parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefMetricsId parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefMetricsId parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefMetricsId parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefMetricsId parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefMetricsId parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefMetricsId parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefMetricsId parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.TaskDefMetricsId parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.TaskDefMetricsId parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefMetricsId parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefMetricsId parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.TaskDefMetricsId prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessage.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   * ID for a specific window of TaskDef metrics.
   * </pre>
   *
   * Protobuf type {@code littlehorse.TaskDefMetricsId}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.TaskDefMetricsId)
      io.littlehorse.sdk.common.proto.TaskDefMetricsIdOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_TaskDefMetricsId_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_TaskDefMetricsId_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.TaskDefMetricsId.class, io.littlehorse.sdk.common.proto.TaskDefMetricsId.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.TaskDefMetricsId.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessage
              .alwaysUseFieldBuilders) {
        internalGetWindowStartFieldBuilder();
        internalGetTaskDefIdFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      windowStart_ = null;
      if (windowStartBuilder_ != null) {
        windowStartBuilder_.dispose();
        windowStartBuilder_ = null;
      }
      windowType_ = 0;
      taskDefId_ = null;
      if (taskDefIdBuilder_ != null) {
        taskDefIdBuilder_.dispose();
        taskDefIdBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_TaskDefMetricsId_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.TaskDefMetricsId getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.TaskDefMetricsId.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.TaskDefMetricsId build() {
      io.littlehorse.sdk.common.proto.TaskDefMetricsId result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.TaskDefMetricsId buildPartial() {
      io.littlehorse.sdk.common.proto.TaskDefMetricsId result = new io.littlehorse.sdk.common.proto.TaskDefMetricsId(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.TaskDefMetricsId result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.windowStart_ = windowStartBuilder_ == null
            ? windowStart_
            : windowStartBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.windowType_ = windowType_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.taskDefId_ = taskDefIdBuilder_ == null
            ? taskDefId_
            : taskDefIdBuilder_.build();
        to_bitField0_ |= 0x00000002;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.sdk.common.proto.TaskDefMetricsId) {
        return mergeFrom((io.littlehorse.sdk.common.proto.TaskDefMetricsId)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.TaskDefMetricsId other) {
      if (other == io.littlehorse.sdk.common.proto.TaskDefMetricsId.getDefaultInstance()) return this;
      if (other.hasWindowStart()) {
        mergeWindowStart(other.getWindowStart());
      }
      if (other.windowType_ != 0) {
        setWindowTypeValue(other.getWindowTypeValue());
      }
      if (other.hasTaskDefId()) {
        mergeTaskDefId(other.getTaskDefId());
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
                  internalGetWindowStartFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 16: {
              windowType_ = input.readEnum();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
            case 26: {
              input.readMessage(
                  internalGetTaskDefIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000004;
              break;
            } // case 26
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

    private com.google.protobuf.Timestamp windowStart_;
    private com.google.protobuf.SingleFieldBuilder<
        com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder> windowStartBuilder_;
    /**
     * <pre>
     * The timestamp at which this metrics window starts.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     * @return Whether the windowStart field is set.
     */
    public boolean hasWindowStart() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * The timestamp at which this metrics window starts.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     * @return The windowStart.
     */
    public com.google.protobuf.Timestamp getWindowStart() {
      if (windowStartBuilder_ == null) {
        return windowStart_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : windowStart_;
      } else {
        return windowStartBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The timestamp at which this metrics window starts.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     */
    public Builder setWindowStart(com.google.protobuf.Timestamp value) {
      if (windowStartBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        windowStart_ = value;
      } else {
        windowStartBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The timestamp at which this metrics window starts.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     */
    public Builder setWindowStart(
        com.google.protobuf.Timestamp.Builder builderForValue) {
      if (windowStartBuilder_ == null) {
        windowStart_ = builderForValue.build();
      } else {
        windowStartBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The timestamp at which this metrics window starts.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     */
    public Builder mergeWindowStart(com.google.protobuf.Timestamp value) {
      if (windowStartBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          windowStart_ != null &&
          windowStart_ != com.google.protobuf.Timestamp.getDefaultInstance()) {
          getWindowStartBuilder().mergeFrom(value);
        } else {
          windowStart_ = value;
        }
      } else {
        windowStartBuilder_.mergeFrom(value);
      }
      if (windowStart_ != null) {
        bitField0_ |= 0x00000001;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * The timestamp at which this metrics window starts.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     */
    public Builder clearWindowStart() {
      bitField0_ = (bitField0_ & ~0x00000001);
      windowStart_ = null;
      if (windowStartBuilder_ != null) {
        windowStartBuilder_.dispose();
        windowStartBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The timestamp at which this metrics window starts.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     */
    public com.google.protobuf.Timestamp.Builder getWindowStartBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return internalGetWindowStartFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The timestamp at which this metrics window starts.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     */
    public com.google.protobuf.TimestampOrBuilder getWindowStartOrBuilder() {
      if (windowStartBuilder_ != null) {
        return windowStartBuilder_.getMessageOrBuilder();
      } else {
        return windowStart_ == null ?
            com.google.protobuf.Timestamp.getDefaultInstance() : windowStart_;
      }
    }
    /**
     * <pre>
     * The timestamp at which this metrics window starts.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilder<
        com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder> 
        internalGetWindowStartFieldBuilder() {
      if (windowStartBuilder_ == null) {
        windowStartBuilder_ = new com.google.protobuf.SingleFieldBuilder<
            com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder>(
                getWindowStart(),
                getParentForChildren(),
                isClean());
        windowStart_ = null;
      }
      return windowStartBuilder_;
    }

    private int windowType_ = 0;
    /**
     * <pre>
     * The length of this window.
     * </pre>
     *
     * <code>.littlehorse.MetricsWindowLength window_type = 2;</code>
     * @return The enum numeric value on the wire for windowType.
     */
    @java.lang.Override public int getWindowTypeValue() {
      return windowType_;
    }
    /**
     * <pre>
     * The length of this window.
     * </pre>
     *
     * <code>.littlehorse.MetricsWindowLength window_type = 2;</code>
     * @param value The enum numeric value on the wire for windowType to set.
     * @return This builder for chaining.
     */
    public Builder setWindowTypeValue(int value) {
      windowType_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The length of this window.
     * </pre>
     *
     * <code>.littlehorse.MetricsWindowLength window_type = 2;</code>
     * @return The windowType.
     */
    @java.lang.Override
    public io.littlehorse.sdk.common.proto.MetricsWindowLength getWindowType() {
      io.littlehorse.sdk.common.proto.MetricsWindowLength result = io.littlehorse.sdk.common.proto.MetricsWindowLength.forNumber(windowType_);
      return result == null ? io.littlehorse.sdk.common.proto.MetricsWindowLength.UNRECOGNIZED : result;
    }
    /**
     * <pre>
     * The length of this window.
     * </pre>
     *
     * <code>.littlehorse.MetricsWindowLength window_type = 2;</code>
     * @param value The windowType to set.
     * @return This builder for chaining.
     */
    public Builder setWindowType(io.littlehorse.sdk.common.proto.MetricsWindowLength value) {
      if (value == null) {
        throw new NullPointerException();
      }
      bitField0_ |= 0x00000002;
      windowType_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The length of this window.
     * </pre>
     *
     * <code>.littlehorse.MetricsWindowLength window_type = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearWindowType() {
      bitField0_ = (bitField0_ & ~0x00000002);
      windowType_ = 0;
      onChanged();
      return this;
    }

    private io.littlehorse.sdk.common.proto.TaskDefId taskDefId_;
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.TaskDefId, io.littlehorse.sdk.common.proto.TaskDefId.Builder, io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder> taskDefIdBuilder_;
    /**
     * <pre>
     * The TaskDefId that this metrics window reports on.
     * </pre>
     *
     * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
     * @return Whether the taskDefId field is set.
     */
    public boolean hasTaskDefId() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <pre>
     * The TaskDefId that this metrics window reports on.
     * </pre>
     *
     * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
     * @return The taskDefId.
     */
    public io.littlehorse.sdk.common.proto.TaskDefId getTaskDefId() {
      if (taskDefIdBuilder_ == null) {
        return taskDefId_ == null ? io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance() : taskDefId_;
      } else {
        return taskDefIdBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The TaskDefId that this metrics window reports on.
     * </pre>
     *
     * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
     */
    public Builder setTaskDefId(io.littlehorse.sdk.common.proto.TaskDefId value) {
      if (taskDefIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        taskDefId_ = value;
      } else {
        taskDefIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The TaskDefId that this metrics window reports on.
     * </pre>
     *
     * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
     */
    public Builder setTaskDefId(
        io.littlehorse.sdk.common.proto.TaskDefId.Builder builderForValue) {
      if (taskDefIdBuilder_ == null) {
        taskDefId_ = builderForValue.build();
      } else {
        taskDefIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The TaskDefId that this metrics window reports on.
     * </pre>
     *
     * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
     */
    public Builder mergeTaskDefId(io.littlehorse.sdk.common.proto.TaskDefId value) {
      if (taskDefIdBuilder_ == null) {
        if (((bitField0_ & 0x00000004) != 0) &&
          taskDefId_ != null &&
          taskDefId_ != io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance()) {
          getTaskDefIdBuilder().mergeFrom(value);
        } else {
          taskDefId_ = value;
        }
      } else {
        taskDefIdBuilder_.mergeFrom(value);
      }
      if (taskDefId_ != null) {
        bitField0_ |= 0x00000004;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * The TaskDefId that this metrics window reports on.
     * </pre>
     *
     * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
     */
    public Builder clearTaskDefId() {
      bitField0_ = (bitField0_ & ~0x00000004);
      taskDefId_ = null;
      if (taskDefIdBuilder_ != null) {
        taskDefIdBuilder_.dispose();
        taskDefIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The TaskDefId that this metrics window reports on.
     * </pre>
     *
     * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
     */
    public io.littlehorse.sdk.common.proto.TaskDefId.Builder getTaskDefIdBuilder() {
      bitField0_ |= 0x00000004;
      onChanged();
      return internalGetTaskDefIdFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The TaskDefId that this metrics window reports on.
     * </pre>
     *
     * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
     */
    public io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder getTaskDefIdOrBuilder() {
      if (taskDefIdBuilder_ != null) {
        return taskDefIdBuilder_.getMessageOrBuilder();
      } else {
        return taskDefId_ == null ?
            io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance() : taskDefId_;
      }
    }
    /**
     * <pre>
     * The TaskDefId that this metrics window reports on.
     * </pre>
     *
     * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
     */
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.TaskDefId, io.littlehorse.sdk.common.proto.TaskDefId.Builder, io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder> 
        internalGetTaskDefIdFieldBuilder() {
      if (taskDefIdBuilder_ == null) {
        taskDefIdBuilder_ = new com.google.protobuf.SingleFieldBuilder<
            io.littlehorse.sdk.common.proto.TaskDefId, io.littlehorse.sdk.common.proto.TaskDefId.Builder, io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder>(
                getTaskDefId(),
                getParentForChildren(),
                isClean());
        taskDefId_ = null;
      }
      return taskDefIdBuilder_;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.TaskDefMetricsId)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.TaskDefMetricsId)
  private static final io.littlehorse.sdk.common.proto.TaskDefMetricsId DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.TaskDefMetricsId();
  }

  public static io.littlehorse.sdk.common.proto.TaskDefMetricsId getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<TaskDefMetricsId>
      PARSER = new com.google.protobuf.AbstractParser<TaskDefMetricsId>() {
    @java.lang.Override
    public TaskDefMetricsId parsePartialFrom(
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

  public static com.google.protobuf.Parser<TaskDefMetricsId> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<TaskDefMetricsId> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TaskDefMetricsId getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

