// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: user_tasks.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Re-Assigns a UserTaskRun to a specific userId or userGroup.
 * </pre>
 *
 * Protobuf type {@code littlehorse.AssignUserTaskRunRequest}
 */
public final class AssignUserTaskRunRequest extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.AssignUserTaskRunRequest)
    AssignUserTaskRunRequestOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      AssignUserTaskRunRequest.class.getName());
  }
  // Use AssignUserTaskRunRequest.newBuilder() to construct.
  private AssignUserTaskRunRequest(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private AssignUserTaskRunRequest() {
    userGroup_ = "";
    userId_ = "";
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_AssignUserTaskRunRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_AssignUserTaskRunRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest.class, io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest.Builder.class);
  }

  private int bitField0_;
  public static final int USER_TASK_RUN_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.UserTaskRunId userTaskRunId_;
  /**
   * <pre>
   * The UserTaskRun to assign to a new user_id or user_group.
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
   * The UserTaskRun to assign to a new user_id or user_group.
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
   * The UserTaskRun to assign to a new user_id or user_group.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.UserTaskRunIdOrBuilder getUserTaskRunIdOrBuilder() {
    return userTaskRunId_ == null ? io.littlehorse.sdk.common.proto.UserTaskRunId.getDefaultInstance() : userTaskRunId_;
  }

  public static final int OVERRIDE_CLAIM_FIELD_NUMBER = 2;
  private boolean overrideClaim_ = false;
  /**
   * <pre>
   * If override_claim is set to false and the UserTaskRun is already assigned to
   * a user_id, then the request throws a FAILED_PRECONDITION error. If set to
   * true, then the old claim is overriden and the UserTaskRun is assigned to
   * the new user.
   * </pre>
   *
   * <code>bool override_claim = 2;</code>
   * @return The overrideClaim.
   */
  @java.lang.Override
  public boolean getOverrideClaim() {
    return overrideClaim_;
  }

  public static final int USER_GROUP_FIELD_NUMBER = 3;
  @SuppressWarnings("serial")
  private volatile java.lang.Object userGroup_ = "";
  /**
   * <pre>
   * The new user_group to which the UserTaskRun is assigned. If not set, then
   * the user_group of the UserTaskRun is actively unset by this request. At least
   * one of the user_group and user_id must be set.
   * </pre>
   *
   * <code>optional string user_group = 3;</code>
   * @return Whether the userGroup field is set.
   */
  @java.lang.Override
  public boolean hasUserGroup() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <pre>
   * The new user_group to which the UserTaskRun is assigned. If not set, then
   * the user_group of the UserTaskRun is actively unset by this request. At least
   * one of the user_group and user_id must be set.
   * </pre>
   *
   * <code>optional string user_group = 3;</code>
   * @return The userGroup.
   */
  @java.lang.Override
  public java.lang.String getUserGroup() {
    java.lang.Object ref = userGroup_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      userGroup_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The new user_group to which the UserTaskRun is assigned. If not set, then
   * the user_group of the UserTaskRun is actively unset by this request. At least
   * one of the user_group and user_id must be set.
   * </pre>
   *
   * <code>optional string user_group = 3;</code>
   * @return The bytes for userGroup.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getUserGroupBytes() {
    java.lang.Object ref = userGroup_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      userGroup_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int USER_ID_FIELD_NUMBER = 4;
  @SuppressWarnings("serial")
  private volatile java.lang.Object userId_ = "";
  /**
   * <pre>
   * The new user_id to which the UserTaskRun is assigned. If not set, then
   * the user_id of the UserTaskRun is actively unset by this request. At least
   * one of the user_group and user_id must be set.
   * </pre>
   *
   * <code>optional string user_id = 4;</code>
   * @return Whether the userId field is set.
   */
  @java.lang.Override
  public boolean hasUserId() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <pre>
   * The new user_id to which the UserTaskRun is assigned. If not set, then
   * the user_id of the UserTaskRun is actively unset by this request. At least
   * one of the user_group and user_id must be set.
   * </pre>
   *
   * <code>optional string user_id = 4;</code>
   * @return The userId.
   */
  @java.lang.Override
  public java.lang.String getUserId() {
    java.lang.Object ref = userId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      userId_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The new user_id to which the UserTaskRun is assigned. If not set, then
   * the user_id of the UserTaskRun is actively unset by this request. At least
   * one of the user_group and user_id must be set.
   * </pre>
   *
   * <code>optional string user_id = 4;</code>
   * @return The bytes for userId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getUserIdBytes() {
    java.lang.Object ref = userId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      userId_ = b;
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
      output.writeMessage(1, getUserTaskRunId());
    }
    if (overrideClaim_ != false) {
      output.writeBool(2, overrideClaim_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      com.google.protobuf.GeneratedMessage.writeString(output, 3, userGroup_);
    }
    if (((bitField0_ & 0x00000004) != 0)) {
      com.google.protobuf.GeneratedMessage.writeString(output, 4, userId_);
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
    if (overrideClaim_ != false) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(2, overrideClaim_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      size += com.google.protobuf.GeneratedMessage.computeStringSize(3, userGroup_);
    }
    if (((bitField0_ & 0x00000004) != 0)) {
      size += com.google.protobuf.GeneratedMessage.computeStringSize(4, userId_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest other = (io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest) obj;

    if (hasUserTaskRunId() != other.hasUserTaskRunId()) return false;
    if (hasUserTaskRunId()) {
      if (!getUserTaskRunId()
          .equals(other.getUserTaskRunId())) return false;
    }
    if (getOverrideClaim()
        != other.getOverrideClaim()) return false;
    if (hasUserGroup() != other.hasUserGroup()) return false;
    if (hasUserGroup()) {
      if (!getUserGroup()
          .equals(other.getUserGroup())) return false;
    }
    if (hasUserId() != other.hasUserId()) return false;
    if (hasUserId()) {
      if (!getUserId()
          .equals(other.getUserId())) return false;
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
    hash = (37 * hash) + OVERRIDE_CLAIM_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getOverrideClaim());
    if (hasUserGroup()) {
      hash = (37 * hash) + USER_GROUP_FIELD_NUMBER;
      hash = (53 * hash) + getUserGroup().hashCode();
    }
    if (hasUserId()) {
      hash = (37 * hash) + USER_ID_FIELD_NUMBER;
      hash = (53 * hash) + getUserId().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest prototype) {
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
   * Re-Assigns a UserTaskRun to a specific userId or userGroup.
   * </pre>
   *
   * Protobuf type {@code littlehorse.AssignUserTaskRunRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.AssignUserTaskRunRequest)
      io.littlehorse.sdk.common.proto.AssignUserTaskRunRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_AssignUserTaskRunRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_AssignUserTaskRunRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest.class, io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest.newBuilder()
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
      overrideClaim_ = false;
      userGroup_ = "";
      userId_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_AssignUserTaskRunRequest_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest build() {
      io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest buildPartial() {
      io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest result = new io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.userTaskRunId_ = userTaskRunIdBuilder_ == null
            ? userTaskRunId_
            : userTaskRunIdBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.overrideClaim_ = overrideClaim_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.userGroup_ = userGroup_;
        to_bitField0_ |= 0x00000002;
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        result.userId_ = userId_;
        to_bitField0_ |= 0x00000004;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest) {
        return mergeFrom((io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest other) {
      if (other == io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest.getDefaultInstance()) return this;
      if (other.hasUserTaskRunId()) {
        mergeUserTaskRunId(other.getUserTaskRunId());
      }
      if (other.getOverrideClaim() != false) {
        setOverrideClaim(other.getOverrideClaim());
      }
      if (other.hasUserGroup()) {
        userGroup_ = other.userGroup_;
        bitField0_ |= 0x00000004;
        onChanged();
      }
      if (other.hasUserId()) {
        userId_ = other.userId_;
        bitField0_ |= 0x00000008;
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
                  internalGetUserTaskRunIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 16: {
              overrideClaim_ = input.readBool();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
            case 26: {
              userGroup_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000004;
              break;
            } // case 26
            case 34: {
              userId_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000008;
              break;
            } // case 34
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
     * The UserTaskRun to assign to a new user_id or user_group.
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
     * The UserTaskRun to assign to a new user_id or user_group.
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
     * The UserTaskRun to assign to a new user_id or user_group.
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
     * The UserTaskRun to assign to a new user_id or user_group.
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
     * The UserTaskRun to assign to a new user_id or user_group.
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
     * The UserTaskRun to assign to a new user_id or user_group.
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
     * The UserTaskRun to assign to a new user_id or user_group.
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
     * The UserTaskRun to assign to a new user_id or user_group.
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
     * The UserTaskRun to assign to a new user_id or user_group.
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

    private boolean overrideClaim_ ;
    /**
     * <pre>
     * If override_claim is set to false and the UserTaskRun is already assigned to
     * a user_id, then the request throws a FAILED_PRECONDITION error. If set to
     * true, then the old claim is overriden and the UserTaskRun is assigned to
     * the new user.
     * </pre>
     *
     * <code>bool override_claim = 2;</code>
     * @return The overrideClaim.
     */
    @java.lang.Override
    public boolean getOverrideClaim() {
      return overrideClaim_;
    }
    /**
     * <pre>
     * If override_claim is set to false and the UserTaskRun is already assigned to
     * a user_id, then the request throws a FAILED_PRECONDITION error. If set to
     * true, then the old claim is overriden and the UserTaskRun is assigned to
     * the new user.
     * </pre>
     *
     * <code>bool override_claim = 2;</code>
     * @param value The overrideClaim to set.
     * @return This builder for chaining.
     */
    public Builder setOverrideClaim(boolean value) {

      overrideClaim_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * If override_claim is set to false and the UserTaskRun is already assigned to
     * a user_id, then the request throws a FAILED_PRECONDITION error. If set to
     * true, then the old claim is overriden and the UserTaskRun is assigned to
     * the new user.
     * </pre>
     *
     * <code>bool override_claim = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearOverrideClaim() {
      bitField0_ = (bitField0_ & ~0x00000002);
      overrideClaim_ = false;
      onChanged();
      return this;
    }

    private java.lang.Object userGroup_ = "";
    /**
     * <pre>
     * The new user_group to which the UserTaskRun is assigned. If not set, then
     * the user_group of the UserTaskRun is actively unset by this request. At least
     * one of the user_group and user_id must be set.
     * </pre>
     *
     * <code>optional string user_group = 3;</code>
     * @return Whether the userGroup field is set.
     */
    public boolean hasUserGroup() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <pre>
     * The new user_group to which the UserTaskRun is assigned. If not set, then
     * the user_group of the UserTaskRun is actively unset by this request. At least
     * one of the user_group and user_id must be set.
     * </pre>
     *
     * <code>optional string user_group = 3;</code>
     * @return The userGroup.
     */
    public java.lang.String getUserGroup() {
      java.lang.Object ref = userGroup_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        userGroup_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The new user_group to which the UserTaskRun is assigned. If not set, then
     * the user_group of the UserTaskRun is actively unset by this request. At least
     * one of the user_group and user_id must be set.
     * </pre>
     *
     * <code>optional string user_group = 3;</code>
     * @return The bytes for userGroup.
     */
    public com.google.protobuf.ByteString
        getUserGroupBytes() {
      java.lang.Object ref = userGroup_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        userGroup_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The new user_group to which the UserTaskRun is assigned. If not set, then
     * the user_group of the UserTaskRun is actively unset by this request. At least
     * one of the user_group and user_id must be set.
     * </pre>
     *
     * <code>optional string user_group = 3;</code>
     * @param value The userGroup to set.
     * @return This builder for chaining.
     */
    public Builder setUserGroup(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      userGroup_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The new user_group to which the UserTaskRun is assigned. If not set, then
     * the user_group of the UserTaskRun is actively unset by this request. At least
     * one of the user_group and user_id must be set.
     * </pre>
     *
     * <code>optional string user_group = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearUserGroup() {
      userGroup_ = getDefaultInstance().getUserGroup();
      bitField0_ = (bitField0_ & ~0x00000004);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The new user_group to which the UserTaskRun is assigned. If not set, then
     * the user_group of the UserTaskRun is actively unset by this request. At least
     * one of the user_group and user_id must be set.
     * </pre>
     *
     * <code>optional string user_group = 3;</code>
     * @param value The bytes for userGroup to set.
     * @return This builder for chaining.
     */
    public Builder setUserGroupBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      userGroup_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }

    private java.lang.Object userId_ = "";
    /**
     * <pre>
     * The new user_id to which the UserTaskRun is assigned. If not set, then
     * the user_id of the UserTaskRun is actively unset by this request. At least
     * one of the user_group and user_id must be set.
     * </pre>
     *
     * <code>optional string user_id = 4;</code>
     * @return Whether the userId field is set.
     */
    public boolean hasUserId() {
      return ((bitField0_ & 0x00000008) != 0);
    }
    /**
     * <pre>
     * The new user_id to which the UserTaskRun is assigned. If not set, then
     * the user_id of the UserTaskRun is actively unset by this request. At least
     * one of the user_group and user_id must be set.
     * </pre>
     *
     * <code>optional string user_id = 4;</code>
     * @return The userId.
     */
    public java.lang.String getUserId() {
      java.lang.Object ref = userId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        userId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The new user_id to which the UserTaskRun is assigned. If not set, then
     * the user_id of the UserTaskRun is actively unset by this request. At least
     * one of the user_group and user_id must be set.
     * </pre>
     *
     * <code>optional string user_id = 4;</code>
     * @return The bytes for userId.
     */
    public com.google.protobuf.ByteString
        getUserIdBytes() {
      java.lang.Object ref = userId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        userId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The new user_id to which the UserTaskRun is assigned. If not set, then
     * the user_id of the UserTaskRun is actively unset by this request. At least
     * one of the user_group and user_id must be set.
     * </pre>
     *
     * <code>optional string user_id = 4;</code>
     * @param value The userId to set.
     * @return This builder for chaining.
     */
    public Builder setUserId(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      userId_ = value;
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The new user_id to which the UserTaskRun is assigned. If not set, then
     * the user_id of the UserTaskRun is actively unset by this request. At least
     * one of the user_group and user_id must be set.
     * </pre>
     *
     * <code>optional string user_id = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearUserId() {
      userId_ = getDefaultInstance().getUserId();
      bitField0_ = (bitField0_ & ~0x00000008);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The new user_id to which the UserTaskRun is assigned. If not set, then
     * the user_id of the UserTaskRun is actively unset by this request. At least
     * one of the user_group and user_id must be set.
     * </pre>
     *
     * <code>optional string user_id = 4;</code>
     * @param value The bytes for userId to set.
     * @return This builder for chaining.
     */
    public Builder setUserIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      userId_ = value;
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.AssignUserTaskRunRequest)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.AssignUserTaskRunRequest)
  private static final io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest();
  }

  public static io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<AssignUserTaskRunRequest>
      PARSER = new com.google.protobuf.AbstractParser<AssignUserTaskRunRequest>() {
    @java.lang.Override
    public AssignUserTaskRunRequest parsePartialFrom(
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

  public static com.google.protobuf.Parser<AssignUserTaskRunRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<AssignUserTaskRunRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

