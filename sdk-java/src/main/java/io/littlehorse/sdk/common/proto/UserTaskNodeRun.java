// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: node_run.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * The sub-node structure for a USER_TASK NodeRun.
 * </pre>
 *
 * Protobuf type {@code littlehorse.UserTaskNodeRun}
 */
public final class UserTaskNodeRun extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.UserTaskNodeRun)
    UserTaskNodeRunOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 29,
      /* patch= */ 3,
      /* suffix= */ "",
      UserTaskNodeRun.class.getName());
  }
  // Use UserTaskNodeRun.newBuilder() to construct.
  private UserTaskNodeRun(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private UserTaskNodeRun() {
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_UserTaskNodeRun_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_UserTaskNodeRun_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.UserTaskNodeRun.class, io.littlehorse.sdk.common.proto.UserTaskNodeRun.Builder.class);
  }

  private int bitField0_;
  public static final int USER_TASK_RUN_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.UserTaskRunId userTaskRunId_;
  /**
   * <pre>
   * The ID of the UserTaskRun. Note that if the ThreadRun was halted when it arrived
   * at this USER_TASK node, then the user_task_run_id will be unset.
   * </pre>
   *
   * <code>optional .littlehorse.UserTaskRunId user_task_run_id = 1;</code>
   * @return Whether the userTaskRunId field is set.
   */
  @java.lang.Override
  public boolean hasUserTaskRunId() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * The ID of the UserTaskRun. Note that if the ThreadRun was halted when it arrived
   * at this USER_TASK node, then the user_task_run_id will be unset.
   * </pre>
   *
   * <code>optional .littlehorse.UserTaskRunId user_task_run_id = 1;</code>
   * @return The userTaskRunId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.UserTaskRunId getUserTaskRunId() {
    return userTaskRunId_ == null ? io.littlehorse.sdk.common.proto.UserTaskRunId.getDefaultInstance() : userTaskRunId_;
  }
  /**
   * <pre>
   * The ID of the UserTaskRun. Note that if the ThreadRun was halted when it arrived
   * at this USER_TASK node, then the user_task_run_id will be unset.
   * </pre>
   *
   * <code>optional .littlehorse.UserTaskRunId user_task_run_id = 1;</code>
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.UserTaskNodeRun)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.UserTaskNodeRun other = (io.littlehorse.sdk.common.proto.UserTaskNodeRun) obj;

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

  public static io.littlehorse.sdk.common.proto.UserTaskNodeRun parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskNodeRun parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskNodeRun parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskNodeRun parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskNodeRun parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskNodeRun parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskNodeRun parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskNodeRun parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.UserTaskNodeRun parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.UserTaskNodeRun parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskNodeRun parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskNodeRun parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.UserTaskNodeRun prototype) {
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
   * The sub-node structure for a USER_TASK NodeRun.
   * </pre>
   *
   * Protobuf type {@code littlehorse.UserTaskNodeRun}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.UserTaskNodeRun)
      io.littlehorse.sdk.common.proto.UserTaskNodeRunOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_UserTaskNodeRun_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_UserTaskNodeRun_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.UserTaskNodeRun.class, io.littlehorse.sdk.common.proto.UserTaskNodeRun.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.UserTaskNodeRun.newBuilder()
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
        getUserTaskRunIdFieldBuilder();
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
      return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_UserTaskNodeRun_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.UserTaskNodeRun getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.UserTaskNodeRun.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.UserTaskNodeRun build() {
      io.littlehorse.sdk.common.proto.UserTaskNodeRun result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.UserTaskNodeRun buildPartial() {
      io.littlehorse.sdk.common.proto.UserTaskNodeRun result = new io.littlehorse.sdk.common.proto.UserTaskNodeRun(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.UserTaskNodeRun result) {
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
      if (other instanceof io.littlehorse.sdk.common.proto.UserTaskNodeRun) {
        return mergeFrom((io.littlehorse.sdk.common.proto.UserTaskNodeRun)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.UserTaskNodeRun other) {
      if (other == io.littlehorse.sdk.common.proto.UserTaskNodeRun.getDefaultInstance()) return this;
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
                  getUserTaskRunIdFieldBuilder().getBuilder(),
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
     * The ID of the UserTaskRun. Note that if the ThreadRun was halted when it arrived
     * at this USER_TASK node, then the user_task_run_id will be unset.
     * </pre>
     *
     * <code>optional .littlehorse.UserTaskRunId user_task_run_id = 1;</code>
     * @return Whether the userTaskRunId field is set.
     */
    public boolean hasUserTaskRunId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * The ID of the UserTaskRun. Note that if the ThreadRun was halted when it arrived
     * at this USER_TASK node, then the user_task_run_id will be unset.
     * </pre>
     *
     * <code>optional .littlehorse.UserTaskRunId user_task_run_id = 1;</code>
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
     * The ID of the UserTaskRun. Note that if the ThreadRun was halted when it arrived
     * at this USER_TASK node, then the user_task_run_id will be unset.
     * </pre>
     *
     * <code>optional .littlehorse.UserTaskRunId user_task_run_id = 1;</code>
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
     * The ID of the UserTaskRun. Note that if the ThreadRun was halted when it arrived
     * at this USER_TASK node, then the user_task_run_id will be unset.
     * </pre>
     *
     * <code>optional .littlehorse.UserTaskRunId user_task_run_id = 1;</code>
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
     * The ID of the UserTaskRun. Note that if the ThreadRun was halted when it arrived
     * at this USER_TASK node, then the user_task_run_id will be unset.
     * </pre>
     *
     * <code>optional .littlehorse.UserTaskRunId user_task_run_id = 1;</code>
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
     * The ID of the UserTaskRun. Note that if the ThreadRun was halted when it arrived
     * at this USER_TASK node, then the user_task_run_id will be unset.
     * </pre>
     *
     * <code>optional .littlehorse.UserTaskRunId user_task_run_id = 1;</code>
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
     * The ID of the UserTaskRun. Note that if the ThreadRun was halted when it arrived
     * at this USER_TASK node, then the user_task_run_id will be unset.
     * </pre>
     *
     * <code>optional .littlehorse.UserTaskRunId user_task_run_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.UserTaskRunId.Builder getUserTaskRunIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getUserTaskRunIdFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The ID of the UserTaskRun. Note that if the ThreadRun was halted when it arrived
     * at this USER_TASK node, then the user_task_run_id will be unset.
     * </pre>
     *
     * <code>optional .littlehorse.UserTaskRunId user_task_run_id = 1;</code>
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
     * The ID of the UserTaskRun. Note that if the ThreadRun was halted when it arrived
     * at this USER_TASK node, then the user_task_run_id will be unset.
     * </pre>
     *
     * <code>optional .littlehorse.UserTaskRunId user_task_run_id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.UserTaskRunId, io.littlehorse.sdk.common.proto.UserTaskRunId.Builder, io.littlehorse.sdk.common.proto.UserTaskRunIdOrBuilder> 
        getUserTaskRunIdFieldBuilder() {
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

    // @@protoc_insertion_point(builder_scope:littlehorse.UserTaskNodeRun)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.UserTaskNodeRun)
  private static final io.littlehorse.sdk.common.proto.UserTaskNodeRun DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.UserTaskNodeRun();
  }

  public static io.littlehorse.sdk.common.proto.UserTaskNodeRun getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<UserTaskNodeRun>
      PARSER = new com.google.protobuf.AbstractParser<UserTaskNodeRun>() {
    @java.lang.Override
    public UserTaskNodeRun parsePartialFrom(
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

  public static com.google.protobuf.Parser<UserTaskNodeRun> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<UserTaskNodeRun> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.UserTaskNodeRun getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

