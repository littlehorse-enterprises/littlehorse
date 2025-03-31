// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: user_tasks.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Cancels a UserTaskRun.
 * </pre>
 *
 * Protobuf type {@code littlehorse.CancelUserTaskRunRequest}
 */
public final class CancelUserTaskRunRequest extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.CancelUserTaskRunRequest)
    CancelUserTaskRunRequestOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      CancelUserTaskRunRequest.class.getName());
  }
  // Use CancelUserTaskRunRequest.newBuilder() to construct.
  private CancelUserTaskRunRequest(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private CancelUserTaskRunRequest() {
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_CancelUserTaskRunRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_CancelUserTaskRunRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest.class, io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest.Builder.class);
  }

  private int bitField0_;
  public static final int USER_TASK_RUN_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.UserTaskRunId userTaskRunId_;
  /**
   * <pre>
   * The id of the UserTaskRun to cancel.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
   * @return Whether the userTaskRunId field is set.
   */
  @java.lang.Override
  public boolean hasUserTaskRunId() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * The id of the UserTaskRun to cancel.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
   * @return The userTaskRunId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.UserTaskRunId getUserTaskRunId() {
    return userTaskRunId_ == null ? io.littlehorse.sdk.common.proto.UserTaskRunId.getDefaultInstance() : userTaskRunId_;
  }
  /**
   * <pre>
   * The id of the UserTaskRun to cancel.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.UserTaskRunIdOrBuilder getUserTaskRunIdOrBuilder() {
    return userTaskRunId_ == null ? io.littlehorse.sdk.common.proto.UserTaskRunId.getDefaultInstance() : userTaskRunId_;
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
      output.writeMessage(1, getUserTaskRunId());
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
        .computeMessageSize(1, getUserTaskRunId());
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest other = (io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest) obj;

    if (hasUserTaskRunId() != other.hasUserTaskRunId()) return false;
    if (hasUserTaskRunId()) {
      if (!getUserTaskRunId()
          .equals(other.getUserTaskRunId())) return false;
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
    if (hasUserTaskRunId()) {
      hash = (37 * hash) + USER_TASK_RUN_ID_FIELD_NUMBER;
      hash = (53 * hash) + getUserTaskRunId().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest prototype) {
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
   * Cancels a UserTaskRun.
   * </pre>
   *
   * Protobuf type {@code littlehorse.CancelUserTaskRunRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.CancelUserTaskRunRequest)
      io.littlehorse.sdk.common.proto.CancelUserTaskRunRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_CancelUserTaskRunRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_CancelUserTaskRunRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest.class, io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest.newBuilder()
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
        internalGetUserTaskRunIdFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      userTaskRunId_ = null;
      if (userTaskRunIdBuilder_ != null) {
        userTaskRunIdBuilder_.dispose();
        userTaskRunIdBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_CancelUserTaskRunRequest_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest build() {
      io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest buildPartial() {
      io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest result = new io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.userTaskRunId_ = userTaskRunIdBuilder_ == null
            ? userTaskRunId_
            : userTaskRunIdBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest) {
        return mergeFrom((io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest other) {
      if (other == io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest.getDefaultInstance()) return this;
      if (other.hasUserTaskRunId()) {
        mergeUserTaskRunId(other.getUserTaskRunId());
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
                  internalGetUserTaskRunIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
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

    private io.littlehorse.sdk.common.proto.UserTaskRunId userTaskRunId_;
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.UserTaskRunId, io.littlehorse.sdk.common.proto.UserTaskRunId.Builder, io.littlehorse.sdk.common.proto.UserTaskRunIdOrBuilder> userTaskRunIdBuilder_;
    /**
     * <pre>
     * The id of the UserTaskRun to cancel.
     * </pre>
     *
     * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
     * @return Whether the userTaskRunId field is set.
     */
    public boolean hasUserTaskRunId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * The id of the UserTaskRun to cancel.
     * </pre>
     *
     * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
     * @return The userTaskRunId.
     */
    public io.littlehorse.sdk.common.proto.UserTaskRunId getUserTaskRunId() {
      if (userTaskRunIdBuilder_ == null) {
        return userTaskRunId_ == null ? io.littlehorse.sdk.common.proto.UserTaskRunId.getDefaultInstance() : userTaskRunId_;
      } else {
        return userTaskRunIdBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The id of the UserTaskRun to cancel.
     * </pre>
     *
     * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
     */
    public Builder setUserTaskRunId(io.littlehorse.sdk.common.proto.UserTaskRunId value) {
      if (userTaskRunIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        userTaskRunId_ = value;
      } else {
        userTaskRunIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The id of the UserTaskRun to cancel.
     * </pre>
     *
     * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
     */
    public Builder setUserTaskRunId(
        io.littlehorse.sdk.common.proto.UserTaskRunId.Builder builderForValue) {
      if (userTaskRunIdBuilder_ == null) {
        userTaskRunId_ = builderForValue.build();
      } else {
        userTaskRunIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The id of the UserTaskRun to cancel.
     * </pre>
     *
     * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
     */
    public Builder mergeUserTaskRunId(io.littlehorse.sdk.common.proto.UserTaskRunId value) {
      if (userTaskRunIdBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          userTaskRunId_ != null &&
          userTaskRunId_ != io.littlehorse.sdk.common.proto.UserTaskRunId.getDefaultInstance()) {
          getUserTaskRunIdBuilder().mergeFrom(value);
        } else {
          userTaskRunId_ = value;
        }
      } else {
        userTaskRunIdBuilder_.mergeFrom(value);
      }
      if (userTaskRunId_ != null) {
        bitField0_ |= 0x00000001;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * The id of the UserTaskRun to cancel.
     * </pre>
     *
     * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
     */
    public Builder clearUserTaskRunId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      userTaskRunId_ = null;
      if (userTaskRunIdBuilder_ != null) {
        userTaskRunIdBuilder_.dispose();
        userTaskRunIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The id of the UserTaskRun to cancel.
     * </pre>
     *
     * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.UserTaskRunId.Builder getUserTaskRunIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return internalGetUserTaskRunIdFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The id of the UserTaskRun to cancel.
     * </pre>
     *
     * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.UserTaskRunIdOrBuilder getUserTaskRunIdOrBuilder() {
      if (userTaskRunIdBuilder_ != null) {
        return userTaskRunIdBuilder_.getMessageOrBuilder();
      } else {
        return userTaskRunId_ == null ?
            io.littlehorse.sdk.common.proto.UserTaskRunId.getDefaultInstance() : userTaskRunId_;
      }
    }
    /**
     * <pre>
     * The id of the UserTaskRun to cancel.
     * </pre>
     *
     * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.UserTaskRunId, io.littlehorse.sdk.common.proto.UserTaskRunId.Builder, io.littlehorse.sdk.common.proto.UserTaskRunIdOrBuilder> 
        internalGetUserTaskRunIdFieldBuilder() {
      if (userTaskRunIdBuilder_ == null) {
        userTaskRunIdBuilder_ = new com.google.protobuf.SingleFieldBuilder<
            io.littlehorse.sdk.common.proto.UserTaskRunId, io.littlehorse.sdk.common.proto.UserTaskRunId.Builder, io.littlehorse.sdk.common.proto.UserTaskRunIdOrBuilder>(
                getUserTaskRunId(),
                getParentForChildren(),
                isClean());
        userTaskRunId_ = null;
      }
      return userTaskRunIdBuilder_;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.CancelUserTaskRunRequest)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.CancelUserTaskRunRequest)
  private static final io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest();
  }

  public static io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<CancelUserTaskRunRequest>
      PARSER = new com.google.protobuf.AbstractParser<CancelUserTaskRunRequest>() {
    @java.lang.Override
    public CancelUserTaskRunRequest parsePartialFrom(
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

  public static com.google.protobuf.Parser<CancelUserTaskRunRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<CancelUserTaskRunRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

