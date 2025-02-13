// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: object_id.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * ID for a UserTaskRun
 * </pre>
 *
 * Protobuf type {@code littlehorse.UserTaskRunId}
 */
public final class UserTaskRunId extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.UserTaskRunId)
    UserTaskRunIdOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 29,
      /* patch= */ 3,
      /* suffix= */ "",
      UserTaskRunId.class.getName());
  }
  // Use UserTaskRunId.newBuilder() to construct.
  private UserTaskRunId(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private UserTaskRunId() {
    userTaskGuid_ = "";
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_UserTaskRunId_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_UserTaskRunId_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.UserTaskRunId.class, io.littlehorse.sdk.common.proto.UserTaskRunId.Builder.class);
  }

  private int bitField0_;
  public static final int WF_RUN_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.WfRunId wfRunId_;
  /**
   * <pre>
   * WfRunId for this UserTaskRun. Note that every UserTaskRun is associated
   * with a WfRun.
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   * @return Whether the wfRunId field is set.
   */
  @java.lang.Override
  public boolean hasWfRunId() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * WfRunId for this UserTaskRun. Note that every UserTaskRun is associated
   * with a WfRun.
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   * @return The wfRunId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WfRunId getWfRunId() {
    return wfRunId_ == null ? io.littlehorse.sdk.common.proto.WfRunId.getDefaultInstance() : wfRunId_;
  }
  /**
   * <pre>
   * WfRunId for this UserTaskRun. Note that every UserTaskRun is associated
   * with a WfRun.
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WfRunIdOrBuilder getWfRunIdOrBuilder() {
    return wfRunId_ == null ? io.littlehorse.sdk.common.proto.WfRunId.getDefaultInstance() : wfRunId_;
  }

  public static final int USER_TASK_GUID_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private volatile java.lang.Object userTaskGuid_ = "";
  /**
   * <pre>
   * Unique identifier for this UserTaskRun.
   * </pre>
   *
   * <code>string user_task_guid = 2;</code>
   * @return The userTaskGuid.
   */
  @java.lang.Override
  public java.lang.String getUserTaskGuid() {
    java.lang.Object ref = userTaskGuid_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      userTaskGuid_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * Unique identifier for this UserTaskRun.
   * </pre>
   *
   * <code>string user_task_guid = 2;</code>
   * @return The bytes for userTaskGuid.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getUserTaskGuidBytes() {
    java.lang.Object ref = userTaskGuid_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      userTaskGuid_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
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
      output.writeMessage(1, getWfRunId());
    }
    if (!com.google.protobuf.GeneratedMessage.isStringEmpty(userTaskGuid_)) {
      com.google.protobuf.GeneratedMessage.writeString(output, 2, userTaskGuid_);
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
        .computeMessageSize(1, getWfRunId());
    }
    if (!com.google.protobuf.GeneratedMessage.isStringEmpty(userTaskGuid_)) {
      size += com.google.protobuf.GeneratedMessage.computeStringSize(2, userTaskGuid_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.UserTaskRunId)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.UserTaskRunId other = (io.littlehorse.sdk.common.proto.UserTaskRunId) obj;

    if (hasWfRunId() != other.hasWfRunId()) return false;
    if (hasWfRunId()) {
      if (!getWfRunId()
          .equals(other.getWfRunId())) return false;
    }
    if (!getUserTaskGuid()
        .equals(other.getUserTaskGuid())) return false;
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
    if (hasWfRunId()) {
      hash = (37 * hash) + WF_RUN_ID_FIELD_NUMBER;
      hash = (53 * hash) + getWfRunId().hashCode();
    }
    hash = (37 * hash) + USER_TASK_GUID_FIELD_NUMBER;
    hash = (53 * hash) + getUserTaskGuid().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.UserTaskRunId parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskRunId parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskRunId parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskRunId parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskRunId parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskRunId parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskRunId parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskRunId parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.UserTaskRunId parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.UserTaskRunId parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskRunId parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskRunId parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.UserTaskRunId prototype) {
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
   * ID for a UserTaskRun
   * </pre>
   *
   * Protobuf type {@code littlehorse.UserTaskRunId}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.UserTaskRunId)
      io.littlehorse.sdk.common.proto.UserTaskRunIdOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_UserTaskRunId_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_UserTaskRunId_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.UserTaskRunId.class, io.littlehorse.sdk.common.proto.UserTaskRunId.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.UserTaskRunId.newBuilder()
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
        getWfRunIdFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      wfRunId_ = null;
      if (wfRunIdBuilder_ != null) {
        wfRunIdBuilder_.dispose();
        wfRunIdBuilder_ = null;
      }
      userTaskGuid_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_UserTaskRunId_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.UserTaskRunId getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.UserTaskRunId.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.UserTaskRunId build() {
      io.littlehorse.sdk.common.proto.UserTaskRunId result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.UserTaskRunId buildPartial() {
      io.littlehorse.sdk.common.proto.UserTaskRunId result = new io.littlehorse.sdk.common.proto.UserTaskRunId(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.UserTaskRunId result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.wfRunId_ = wfRunIdBuilder_ == null
            ? wfRunId_
            : wfRunIdBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.userTaskGuid_ = userTaskGuid_;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.sdk.common.proto.UserTaskRunId) {
        return mergeFrom((io.littlehorse.sdk.common.proto.UserTaskRunId)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.UserTaskRunId other) {
      if (other == io.littlehorse.sdk.common.proto.UserTaskRunId.getDefaultInstance()) return this;
      if (other.hasWfRunId()) {
        mergeWfRunId(other.getWfRunId());
      }
      if (!other.getUserTaskGuid().isEmpty()) {
        userTaskGuid_ = other.userTaskGuid_;
        bitField0_ |= 0x00000002;
        onChanged();
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
                  getWfRunIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              userTaskGuid_ = input.readStringRequireUtf8();
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

    private io.littlehorse.sdk.common.proto.WfRunId wfRunId_;
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.WfRunId, io.littlehorse.sdk.common.proto.WfRunId.Builder, io.littlehorse.sdk.common.proto.WfRunIdOrBuilder> wfRunIdBuilder_;
    /**
     * <pre>
     * WfRunId for this UserTaskRun. Note that every UserTaskRun is associated
     * with a WfRun.
     * </pre>
     *
     * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
     * @return Whether the wfRunId field is set.
     */
    public boolean hasWfRunId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * WfRunId for this UserTaskRun. Note that every UserTaskRun is associated
     * with a WfRun.
     * </pre>
     *
     * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
     * @return The wfRunId.
     */
    public io.littlehorse.sdk.common.proto.WfRunId getWfRunId() {
      if (wfRunIdBuilder_ == null) {
        return wfRunId_ == null ? io.littlehorse.sdk.common.proto.WfRunId.getDefaultInstance() : wfRunId_;
      } else {
        return wfRunIdBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * WfRunId for this UserTaskRun. Note that every UserTaskRun is associated
     * with a WfRun.
     * </pre>
     *
     * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
     */
    public Builder setWfRunId(io.littlehorse.sdk.common.proto.WfRunId value) {
      if (wfRunIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        wfRunId_ = value;
      } else {
        wfRunIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * WfRunId for this UserTaskRun. Note that every UserTaskRun is associated
     * with a WfRun.
     * </pre>
     *
     * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
     */
    public Builder setWfRunId(
        io.littlehorse.sdk.common.proto.WfRunId.Builder builderForValue) {
      if (wfRunIdBuilder_ == null) {
        wfRunId_ = builderForValue.build();
      } else {
        wfRunIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * WfRunId for this UserTaskRun. Note that every UserTaskRun is associated
     * with a WfRun.
     * </pre>
     *
     * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
     */
    public Builder mergeWfRunId(io.littlehorse.sdk.common.proto.WfRunId value) {
      if (wfRunIdBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          wfRunId_ != null &&
          wfRunId_ != io.littlehorse.sdk.common.proto.WfRunId.getDefaultInstance()) {
          getWfRunIdBuilder().mergeFrom(value);
        } else {
          wfRunId_ = value;
        }
      } else {
        wfRunIdBuilder_.mergeFrom(value);
      }
      if (wfRunId_ != null) {
        bitField0_ |= 0x00000001;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * WfRunId for this UserTaskRun. Note that every UserTaskRun is associated
     * with a WfRun.
     * </pre>
     *
     * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
     */
    public Builder clearWfRunId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      wfRunId_ = null;
      if (wfRunIdBuilder_ != null) {
        wfRunIdBuilder_.dispose();
        wfRunIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * WfRunId for this UserTaskRun. Note that every UserTaskRun is associated
     * with a WfRun.
     * </pre>
     *
     * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.WfRunId.Builder getWfRunIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getWfRunIdFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * WfRunId for this UserTaskRun. Note that every UserTaskRun is associated
     * with a WfRun.
     * </pre>
     *
     * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.WfRunIdOrBuilder getWfRunIdOrBuilder() {
      if (wfRunIdBuilder_ != null) {
        return wfRunIdBuilder_.getMessageOrBuilder();
      } else {
        return wfRunId_ == null ?
            io.littlehorse.sdk.common.proto.WfRunId.getDefaultInstance() : wfRunId_;
      }
    }
    /**
     * <pre>
     * WfRunId for this UserTaskRun. Note that every UserTaskRun is associated
     * with a WfRun.
     * </pre>
     *
     * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.WfRunId, io.littlehorse.sdk.common.proto.WfRunId.Builder, io.littlehorse.sdk.common.proto.WfRunIdOrBuilder> 
        getWfRunIdFieldBuilder() {
      if (wfRunIdBuilder_ == null) {
        wfRunIdBuilder_ = new com.google.protobuf.SingleFieldBuilder<
            io.littlehorse.sdk.common.proto.WfRunId, io.littlehorse.sdk.common.proto.WfRunId.Builder, io.littlehorse.sdk.common.proto.WfRunIdOrBuilder>(
                getWfRunId(),
                getParentForChildren(),
                isClean());
        wfRunId_ = null;
      }
      return wfRunIdBuilder_;
    }

    private java.lang.Object userTaskGuid_ = "";
    /**
     * <pre>
     * Unique identifier for this UserTaskRun.
     * </pre>
     *
     * <code>string user_task_guid = 2;</code>
     * @return The userTaskGuid.
     */
    public java.lang.String getUserTaskGuid() {
      java.lang.Object ref = userTaskGuid_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        userTaskGuid_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * Unique identifier for this UserTaskRun.
     * </pre>
     *
     * <code>string user_task_guid = 2;</code>
     * @return The bytes for userTaskGuid.
     */
    public com.google.protobuf.ByteString
        getUserTaskGuidBytes() {
      java.lang.Object ref = userTaskGuid_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        userTaskGuid_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * Unique identifier for this UserTaskRun.
     * </pre>
     *
     * <code>string user_task_guid = 2;</code>
     * @param value The userTaskGuid to set.
     * @return This builder for chaining.
     */
    public Builder setUserTaskGuid(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      userTaskGuid_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Unique identifier for this UserTaskRun.
     * </pre>
     *
     * <code>string user_task_guid = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearUserTaskGuid() {
      userTaskGuid_ = getDefaultInstance().getUserTaskGuid();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Unique identifier for this UserTaskRun.
     * </pre>
     *
     * <code>string user_task_guid = 2;</code>
     * @param value The bytes for userTaskGuid to set.
     * @return This builder for chaining.
     */
    public Builder setUserTaskGuidBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      userTaskGuid_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.UserTaskRunId)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.UserTaskRunId)
  private static final io.littlehorse.sdk.common.proto.UserTaskRunId DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.UserTaskRunId();
  }

  public static io.littlehorse.sdk.common.proto.UserTaskRunId getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<UserTaskRunId>
      PARSER = new com.google.protobuf.AbstractParser<UserTaskRunId>() {
    @java.lang.Override
    public UserTaskRunId parsePartialFrom(
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

  public static com.google.protobuf.Parser<UserTaskRunId> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<UserTaskRunId> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.UserTaskRunId getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

