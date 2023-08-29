// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: user_tasks.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.UserTaskTriggerReference}
 */
public final class UserTaskTriggerReference extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.UserTaskTriggerReference)
    UserTaskTriggerReferenceOrBuilder {
private static final long serialVersionUID = 0L;
  // Use UserTaskTriggerReference.newBuilder() to construct.
  private UserTaskTriggerReference(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private UserTaskTriggerReference() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new UserTaskTriggerReference();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_UserTaskTriggerReference_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_UserTaskTriggerReference_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.UserTaskTriggerReference.class, io.littlehorse.sdk.common.proto.UserTaskTriggerReference.Builder.class);
  }

  private int bitField0_;
  public static final int NODE_RUN_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.NodeRunId nodeRunId_;
  /**
   * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
   * @return Whether the nodeRunId field is set.
   */
  @java.lang.Override
  public boolean hasNodeRunId() {
    return nodeRunId_ != null;
  }
  /**
   * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
   * @return The nodeRunId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.NodeRunId getNodeRunId() {
    return nodeRunId_ == null ? io.littlehorse.sdk.common.proto.NodeRunId.getDefaultInstance() : nodeRunId_;
  }
  /**
   * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.NodeRunIdOrBuilder getNodeRunIdOrBuilder() {
    return nodeRunId_ == null ? io.littlehorse.sdk.common.proto.NodeRunId.getDefaultInstance() : nodeRunId_;
  }

  public static final int USER_TASK_EVENT_NUMBER_FIELD_NUMBER = 2;
  private int userTaskEventNumber_ = 0;
  /**
   * <code>int32 user_task_event_number = 2;</code>
   * @return The userTaskEventNumber.
   */
  @java.lang.Override
  public int getUserTaskEventNumber() {
    return userTaskEventNumber_;
  }

  public static final int WF_SPEC_ID_FIELD_NUMBER = 3;
  private io.littlehorse.sdk.common.proto.WfSpecId wfSpecId_;
  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
   * @return Whether the wfSpecId field is set.
   */
  @java.lang.Override
  public boolean hasWfSpecId() {
    return wfSpecId_ != null;
  }
  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
   * @return The wfSpecId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WfSpecId getWfSpecId() {
    return wfSpecId_ == null ? io.littlehorse.sdk.common.proto.WfSpecId.getDefaultInstance() : wfSpecId_;
  }
  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder getWfSpecIdOrBuilder() {
    return wfSpecId_ == null ? io.littlehorse.sdk.common.proto.WfSpecId.getDefaultInstance() : wfSpecId_;
  }

  public static final int CONTEXT_FIELD_NUMBER = 4;
  private io.littlehorse.sdk.common.proto.UserTaskTriggerContext context_;
  /**
   * <code>optional .littlehorse.UserTaskTriggerContext context = 4;</code>
   * @return Whether the context field is set.
   */
  @java.lang.Override
  public boolean hasContext() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional .littlehorse.UserTaskTriggerContext context = 4;</code>
   * @return The context.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.UserTaskTriggerContext getContext() {
    return context_ == null ? io.littlehorse.sdk.common.proto.UserTaskTriggerContext.getDefaultInstance() : context_;
  }
  /**
   * <code>optional .littlehorse.UserTaskTriggerContext context = 4;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.UserTaskTriggerContextOrBuilder getContextOrBuilder() {
    return context_ == null ? io.littlehorse.sdk.common.proto.UserTaskTriggerContext.getDefaultInstance() : context_;
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
    if (nodeRunId_ != null) {
      output.writeMessage(1, getNodeRunId());
    }
    if (userTaskEventNumber_ != 0) {
      output.writeInt32(2, userTaskEventNumber_);
    }
    if (wfSpecId_ != null) {
      output.writeMessage(3, getWfSpecId());
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      output.writeMessage(4, getContext());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (nodeRunId_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getNodeRunId());
    }
    if (userTaskEventNumber_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, userTaskEventNumber_);
    }
    if (wfSpecId_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, getWfSpecId());
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(4, getContext());
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.UserTaskTriggerReference)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.UserTaskTriggerReference other = (io.littlehorse.sdk.common.proto.UserTaskTriggerReference) obj;

    if (hasNodeRunId() != other.hasNodeRunId()) return false;
    if (hasNodeRunId()) {
      if (!getNodeRunId()
          .equals(other.getNodeRunId())) return false;
    }
    if (getUserTaskEventNumber()
        != other.getUserTaskEventNumber()) return false;
    if (hasWfSpecId() != other.hasWfSpecId()) return false;
    if (hasWfSpecId()) {
      if (!getWfSpecId()
          .equals(other.getWfSpecId())) return false;
    }
    if (hasContext() != other.hasContext()) return false;
    if (hasContext()) {
      if (!getContext()
          .equals(other.getContext())) return false;
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
    if (hasNodeRunId()) {
      hash = (37 * hash) + NODE_RUN_ID_FIELD_NUMBER;
      hash = (53 * hash) + getNodeRunId().hashCode();
    }
    hash = (37 * hash) + USER_TASK_EVENT_NUMBER_FIELD_NUMBER;
    hash = (53 * hash) + getUserTaskEventNumber();
    if (hasWfSpecId()) {
      hash = (37 * hash) + WF_SPEC_ID_FIELD_NUMBER;
      hash = (53 * hash) + getWfSpecId().hashCode();
    }
    if (hasContext()) {
      hash = (37 * hash) + CONTEXT_FIELD_NUMBER;
      hash = (53 * hash) + getContext().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReference parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReference parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReference parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReference parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReference parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReference parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReference parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReference parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReference parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReference parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReference parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReference parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.UserTaskTriggerReference prototype) {
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
   * Protobuf type {@code littlehorse.UserTaskTriggerReference}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.UserTaskTriggerReference)
      io.littlehorse.sdk.common.proto.UserTaskTriggerReferenceOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_UserTaskTriggerReference_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_UserTaskTriggerReference_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.UserTaskTriggerReference.class, io.littlehorse.sdk.common.proto.UserTaskTriggerReference.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.UserTaskTriggerReference.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
        getNodeRunIdFieldBuilder();
        getWfSpecIdFieldBuilder();
        getContextFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      nodeRunId_ = null;
      if (nodeRunIdBuilder_ != null) {
        nodeRunIdBuilder_.dispose();
        nodeRunIdBuilder_ = null;
      }
      userTaskEventNumber_ = 0;
      wfSpecId_ = null;
      if (wfSpecIdBuilder_ != null) {
        wfSpecIdBuilder_.dispose();
        wfSpecIdBuilder_ = null;
      }
      context_ = null;
      if (contextBuilder_ != null) {
        contextBuilder_.dispose();
        contextBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.UserTasks.internal_static_littlehorse_UserTaskTriggerReference_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.UserTaskTriggerReference getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.UserTaskTriggerReference.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.UserTaskTriggerReference build() {
      io.littlehorse.sdk.common.proto.UserTaskTriggerReference result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.UserTaskTriggerReference buildPartial() {
      io.littlehorse.sdk.common.proto.UserTaskTriggerReference result = new io.littlehorse.sdk.common.proto.UserTaskTriggerReference(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.UserTaskTriggerReference result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.nodeRunId_ = nodeRunIdBuilder_ == null
            ? nodeRunId_
            : nodeRunIdBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.userTaskEventNumber_ = userTaskEventNumber_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.wfSpecId_ = wfSpecIdBuilder_ == null
            ? wfSpecId_
            : wfSpecIdBuilder_.build();
      }
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000008) != 0)) {
        result.context_ = contextBuilder_ == null
            ? context_
            : contextBuilder_.build();
        to_bitField0_ |= 0x00000001;
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
      if (other instanceof io.littlehorse.sdk.common.proto.UserTaskTriggerReference) {
        return mergeFrom((io.littlehorse.sdk.common.proto.UserTaskTriggerReference)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.UserTaskTriggerReference other) {
      if (other == io.littlehorse.sdk.common.proto.UserTaskTriggerReference.getDefaultInstance()) return this;
      if (other.hasNodeRunId()) {
        mergeNodeRunId(other.getNodeRunId());
      }
      if (other.getUserTaskEventNumber() != 0) {
        setUserTaskEventNumber(other.getUserTaskEventNumber());
      }
      if (other.hasWfSpecId()) {
        mergeWfSpecId(other.getWfSpecId());
      }
      if (other.hasContext()) {
        mergeContext(other.getContext());
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
                  getNodeRunIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 16: {
              userTaskEventNumber_ = input.readInt32();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
            case 26: {
              input.readMessage(
                  getWfSpecIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000004;
              break;
            } // case 26
            case 34: {
              input.readMessage(
                  getContextFieldBuilder().getBuilder(),
                  extensionRegistry);
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

    private io.littlehorse.sdk.common.proto.NodeRunId nodeRunId_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.NodeRunId, io.littlehorse.sdk.common.proto.NodeRunId.Builder, io.littlehorse.sdk.common.proto.NodeRunIdOrBuilder> nodeRunIdBuilder_;
    /**
     * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
     * @return Whether the nodeRunId field is set.
     */
    public boolean hasNodeRunId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
     * @return The nodeRunId.
     */
    public io.littlehorse.sdk.common.proto.NodeRunId getNodeRunId() {
      if (nodeRunIdBuilder_ == null) {
        return nodeRunId_ == null ? io.littlehorse.sdk.common.proto.NodeRunId.getDefaultInstance() : nodeRunId_;
      } else {
        return nodeRunIdBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
     */
    public Builder setNodeRunId(io.littlehorse.sdk.common.proto.NodeRunId value) {
      if (nodeRunIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        nodeRunId_ = value;
      } else {
        nodeRunIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
     */
    public Builder setNodeRunId(
        io.littlehorse.sdk.common.proto.NodeRunId.Builder builderForValue) {
      if (nodeRunIdBuilder_ == null) {
        nodeRunId_ = builderForValue.build();
      } else {
        nodeRunIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
     */
    public Builder mergeNodeRunId(io.littlehorse.sdk.common.proto.NodeRunId value) {
      if (nodeRunIdBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          nodeRunId_ != null &&
          nodeRunId_ != io.littlehorse.sdk.common.proto.NodeRunId.getDefaultInstance()) {
          getNodeRunIdBuilder().mergeFrom(value);
        } else {
          nodeRunId_ = value;
        }
      } else {
        nodeRunIdBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
     */
    public Builder clearNodeRunId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      nodeRunId_ = null;
      if (nodeRunIdBuilder_ != null) {
        nodeRunIdBuilder_.dispose();
        nodeRunIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.NodeRunId.Builder getNodeRunIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getNodeRunIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.NodeRunIdOrBuilder getNodeRunIdOrBuilder() {
      if (nodeRunIdBuilder_ != null) {
        return nodeRunIdBuilder_.getMessageOrBuilder();
      } else {
        return nodeRunId_ == null ?
            io.littlehorse.sdk.common.proto.NodeRunId.getDefaultInstance() : nodeRunId_;
      }
    }
    /**
     * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.NodeRunId, io.littlehorse.sdk.common.proto.NodeRunId.Builder, io.littlehorse.sdk.common.proto.NodeRunIdOrBuilder> 
        getNodeRunIdFieldBuilder() {
      if (nodeRunIdBuilder_ == null) {
        nodeRunIdBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.NodeRunId, io.littlehorse.sdk.common.proto.NodeRunId.Builder, io.littlehorse.sdk.common.proto.NodeRunIdOrBuilder>(
                getNodeRunId(),
                getParentForChildren(),
                isClean());
        nodeRunId_ = null;
      }
      return nodeRunIdBuilder_;
    }

    private int userTaskEventNumber_ ;
    /**
     * <code>int32 user_task_event_number = 2;</code>
     * @return The userTaskEventNumber.
     */
    @java.lang.Override
    public int getUserTaskEventNumber() {
      return userTaskEventNumber_;
    }
    /**
     * <code>int32 user_task_event_number = 2;</code>
     * @param value The userTaskEventNumber to set.
     * @return This builder for chaining.
     */
    public Builder setUserTaskEventNumber(int value) {

      userTaskEventNumber_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>int32 user_task_event_number = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearUserTaskEventNumber() {
      bitField0_ = (bitField0_ & ~0x00000002);
      userTaskEventNumber_ = 0;
      onChanged();
      return this;
    }

    private io.littlehorse.sdk.common.proto.WfSpecId wfSpecId_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.WfSpecId, io.littlehorse.sdk.common.proto.WfSpecId.Builder, io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder> wfSpecIdBuilder_;
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
     * @return Whether the wfSpecId field is set.
     */
    public boolean hasWfSpecId() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
     * @return The wfSpecId.
     */
    public io.littlehorse.sdk.common.proto.WfSpecId getWfSpecId() {
      if (wfSpecIdBuilder_ == null) {
        return wfSpecId_ == null ? io.littlehorse.sdk.common.proto.WfSpecId.getDefaultInstance() : wfSpecId_;
      } else {
        return wfSpecIdBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
     */
    public Builder setWfSpecId(io.littlehorse.sdk.common.proto.WfSpecId value) {
      if (wfSpecIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        wfSpecId_ = value;
      } else {
        wfSpecIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
     */
    public Builder setWfSpecId(
        io.littlehorse.sdk.common.proto.WfSpecId.Builder builderForValue) {
      if (wfSpecIdBuilder_ == null) {
        wfSpecId_ = builderForValue.build();
      } else {
        wfSpecIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
     */
    public Builder mergeWfSpecId(io.littlehorse.sdk.common.proto.WfSpecId value) {
      if (wfSpecIdBuilder_ == null) {
        if (((bitField0_ & 0x00000004) != 0) &&
          wfSpecId_ != null &&
          wfSpecId_ != io.littlehorse.sdk.common.proto.WfSpecId.getDefaultInstance()) {
          getWfSpecIdBuilder().mergeFrom(value);
        } else {
          wfSpecId_ = value;
        }
      } else {
        wfSpecIdBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
     */
    public Builder clearWfSpecId() {
      bitField0_ = (bitField0_ & ~0x00000004);
      wfSpecId_ = null;
      if (wfSpecIdBuilder_ != null) {
        wfSpecIdBuilder_.dispose();
        wfSpecIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
     */
    public io.littlehorse.sdk.common.proto.WfSpecId.Builder getWfSpecIdBuilder() {
      bitField0_ |= 0x00000004;
      onChanged();
      return getWfSpecIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
     */
    public io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder getWfSpecIdOrBuilder() {
      if (wfSpecIdBuilder_ != null) {
        return wfSpecIdBuilder_.getMessageOrBuilder();
      } else {
        return wfSpecId_ == null ?
            io.littlehorse.sdk.common.proto.WfSpecId.getDefaultInstance() : wfSpecId_;
      }
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.WfSpecId, io.littlehorse.sdk.common.proto.WfSpecId.Builder, io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder> 
        getWfSpecIdFieldBuilder() {
      if (wfSpecIdBuilder_ == null) {
        wfSpecIdBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.WfSpecId, io.littlehorse.sdk.common.proto.WfSpecId.Builder, io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder>(
                getWfSpecId(),
                getParentForChildren(),
                isClean());
        wfSpecId_ = null;
      }
      return wfSpecIdBuilder_;
    }

    private io.littlehorse.sdk.common.proto.UserTaskTriggerContext context_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.UserTaskTriggerContext, io.littlehorse.sdk.common.proto.UserTaskTriggerContext.Builder, io.littlehorse.sdk.common.proto.UserTaskTriggerContextOrBuilder> contextBuilder_;
    /**
     * <code>optional .littlehorse.UserTaskTriggerContext context = 4;</code>
     * @return Whether the context field is set.
     */
    public boolean hasContext() {
      return ((bitField0_ & 0x00000008) != 0);
    }
    /**
     * <code>optional .littlehorse.UserTaskTriggerContext context = 4;</code>
     * @return The context.
     */
    public io.littlehorse.sdk.common.proto.UserTaskTriggerContext getContext() {
      if (contextBuilder_ == null) {
        return context_ == null ? io.littlehorse.sdk.common.proto.UserTaskTriggerContext.getDefaultInstance() : context_;
      } else {
        return contextBuilder_.getMessage();
      }
    }
    /**
     * <code>optional .littlehorse.UserTaskTriggerContext context = 4;</code>
     */
    public Builder setContext(io.littlehorse.sdk.common.proto.UserTaskTriggerContext value) {
      if (contextBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        context_ = value;
      } else {
        contextBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>optional .littlehorse.UserTaskTriggerContext context = 4;</code>
     */
    public Builder setContext(
        io.littlehorse.sdk.common.proto.UserTaskTriggerContext.Builder builderForValue) {
      if (contextBuilder_ == null) {
        context_ = builderForValue.build();
      } else {
        contextBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>optional .littlehorse.UserTaskTriggerContext context = 4;</code>
     */
    public Builder mergeContext(io.littlehorse.sdk.common.proto.UserTaskTriggerContext value) {
      if (contextBuilder_ == null) {
        if (((bitField0_ & 0x00000008) != 0) &&
          context_ != null &&
          context_ != io.littlehorse.sdk.common.proto.UserTaskTriggerContext.getDefaultInstance()) {
          getContextBuilder().mergeFrom(value);
        } else {
          context_ = value;
        }
      } else {
        contextBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>optional .littlehorse.UserTaskTriggerContext context = 4;</code>
     */
    public Builder clearContext() {
      bitField0_ = (bitField0_ & ~0x00000008);
      context_ = null;
      if (contextBuilder_ != null) {
        contextBuilder_.dispose();
        contextBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>optional .littlehorse.UserTaskTriggerContext context = 4;</code>
     */
    public io.littlehorse.sdk.common.proto.UserTaskTriggerContext.Builder getContextBuilder() {
      bitField0_ |= 0x00000008;
      onChanged();
      return getContextFieldBuilder().getBuilder();
    }
    /**
     * <code>optional .littlehorse.UserTaskTriggerContext context = 4;</code>
     */
    public io.littlehorse.sdk.common.proto.UserTaskTriggerContextOrBuilder getContextOrBuilder() {
      if (contextBuilder_ != null) {
        return contextBuilder_.getMessageOrBuilder();
      } else {
        return context_ == null ?
            io.littlehorse.sdk.common.proto.UserTaskTriggerContext.getDefaultInstance() : context_;
      }
    }
    /**
     * <code>optional .littlehorse.UserTaskTriggerContext context = 4;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.UserTaskTriggerContext, io.littlehorse.sdk.common.proto.UserTaskTriggerContext.Builder, io.littlehorse.sdk.common.proto.UserTaskTriggerContextOrBuilder> 
        getContextFieldBuilder() {
      if (contextBuilder_ == null) {
        contextBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.UserTaskTriggerContext, io.littlehorse.sdk.common.proto.UserTaskTriggerContext.Builder, io.littlehorse.sdk.common.proto.UserTaskTriggerContextOrBuilder>(
                getContext(),
                getParentForChildren(),
                isClean());
        context_ = null;
      }
      return contextBuilder_;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.UserTaskTriggerReference)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.UserTaskTriggerReference)
  private static final io.littlehorse.sdk.common.proto.UserTaskTriggerReference DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.UserTaskTriggerReference();
  }

  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReference getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<UserTaskTriggerReference>
      PARSER = new com.google.protobuf.AbstractParser<UserTaskTriggerReference>() {
    @java.lang.Override
    public UserTaskTriggerReference parsePartialFrom(
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

  public static com.google.protobuf.Parser<UserTaskTriggerReference> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<UserTaskTriggerReference> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.UserTaskTriggerReference getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

