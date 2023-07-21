// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.UserTaskTriggerReferencePb}
 */
public final class UserTaskTriggerReferencePb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.UserTaskTriggerReferencePb)
    UserTaskTriggerReferencePbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use UserTaskTriggerReferencePb.newBuilder() to construct.
  private UserTaskTriggerReferencePb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private UserTaskTriggerReferencePb() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new UserTaskTriggerReferencePb();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_UserTaskTriggerReferencePb_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_UserTaskTriggerReferencePb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb.class, io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb.Builder.class);
  }

  public static final int NODE_RUN_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.NodeRunIdPb nodeRunId_;
  /**
   * <code>.littlehorse.NodeRunIdPb node_run_id = 1;</code>
   * @return Whether the nodeRunId field is set.
   */
  @java.lang.Override
  public boolean hasNodeRunId() {
    return nodeRunId_ != null;
  }
  /**
   * <code>.littlehorse.NodeRunIdPb node_run_id = 1;</code>
   * @return The nodeRunId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.NodeRunIdPb getNodeRunId() {
    return nodeRunId_ == null ? io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance() : nodeRunId_;
  }
  /**
   * <code>.littlehorse.NodeRunIdPb node_run_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder getNodeRunIdOrBuilder() {
    return nodeRunId_ == null ? io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance() : nodeRunId_;
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
  private io.littlehorse.sdk.common.proto.WfSpecIdPb wfSpecId_;
  /**
   * <code>.littlehorse.WfSpecIdPb wf_spec_id = 3;</code>
   * @return Whether the wfSpecId field is set.
   */
  @java.lang.Override
  public boolean hasWfSpecId() {
    return wfSpecId_ != null;
  }
  /**
   * <code>.littlehorse.WfSpecIdPb wf_spec_id = 3;</code>
   * @return The wfSpecId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WfSpecIdPb getWfSpecId() {
    return wfSpecId_ == null ? io.littlehorse.sdk.common.proto.WfSpecIdPb.getDefaultInstance() : wfSpecId_;
  }
  /**
   * <code>.littlehorse.WfSpecIdPb wf_spec_id = 3;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WfSpecIdPbOrBuilder getWfSpecIdOrBuilder() {
    return wfSpecId_ == null ? io.littlehorse.sdk.common.proto.WfSpecIdPb.getDefaultInstance() : wfSpecId_;
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
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb other = (io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb) obj;

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
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb prototype) {
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
   * Protobuf type {@code littlehorse.UserTaskTriggerReferencePb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.UserTaskTriggerReferencePb)
      io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_UserTaskTriggerReferencePb_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_UserTaskTriggerReferencePb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb.class, io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb.newBuilder()
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
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_UserTaskTriggerReferencePb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb build() {
      io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb buildPartial() {
      io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb result = new io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb result) {
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
      if (other instanceof io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb) {
        return mergeFrom((io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb other) {
      if (other == io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb.getDefaultInstance()) return this;
      if (other.hasNodeRunId()) {
        mergeNodeRunId(other.getNodeRunId());
      }
      if (other.getUserTaskEventNumber() != 0) {
        setUserTaskEventNumber(other.getUserTaskEventNumber());
      }
      if (other.hasWfSpecId()) {
        mergeWfSpecId(other.getWfSpecId());
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

    private io.littlehorse.sdk.common.proto.NodeRunIdPb nodeRunId_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.NodeRunIdPb, io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder, io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder> nodeRunIdBuilder_;
    /**
     * <code>.littlehorse.NodeRunIdPb node_run_id = 1;</code>
     * @return Whether the nodeRunId field is set.
     */
    public boolean hasNodeRunId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.littlehorse.NodeRunIdPb node_run_id = 1;</code>
     * @return The nodeRunId.
     */
    public io.littlehorse.sdk.common.proto.NodeRunIdPb getNodeRunId() {
      if (nodeRunIdBuilder_ == null) {
        return nodeRunId_ == null ? io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance() : nodeRunId_;
      } else {
        return nodeRunIdBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.NodeRunIdPb node_run_id = 1;</code>
     */
    public Builder setNodeRunId(io.littlehorse.sdk.common.proto.NodeRunIdPb value) {
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
     * <code>.littlehorse.NodeRunIdPb node_run_id = 1;</code>
     */
    public Builder setNodeRunId(
        io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder builderForValue) {
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
     * <code>.littlehorse.NodeRunIdPb node_run_id = 1;</code>
     */
    public Builder mergeNodeRunId(io.littlehorse.sdk.common.proto.NodeRunIdPb value) {
      if (nodeRunIdBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          nodeRunId_ != null &&
          nodeRunId_ != io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance()) {
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
     * <code>.littlehorse.NodeRunIdPb node_run_id = 1;</code>
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
     * <code>.littlehorse.NodeRunIdPb node_run_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder getNodeRunIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getNodeRunIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.NodeRunIdPb node_run_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder getNodeRunIdOrBuilder() {
      if (nodeRunIdBuilder_ != null) {
        return nodeRunIdBuilder_.getMessageOrBuilder();
      } else {
        return nodeRunId_ == null ?
            io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance() : nodeRunId_;
      }
    }
    /**
     * <code>.littlehorse.NodeRunIdPb node_run_id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.NodeRunIdPb, io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder, io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder> 
        getNodeRunIdFieldBuilder() {
      if (nodeRunIdBuilder_ == null) {
        nodeRunIdBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.NodeRunIdPb, io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder, io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder>(
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

    private io.littlehorse.sdk.common.proto.WfSpecIdPb wfSpecId_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.WfSpecIdPb, io.littlehorse.sdk.common.proto.WfSpecIdPb.Builder, io.littlehorse.sdk.common.proto.WfSpecIdPbOrBuilder> wfSpecIdBuilder_;
    /**
     * <code>.littlehorse.WfSpecIdPb wf_spec_id = 3;</code>
     * @return Whether the wfSpecId field is set.
     */
    public boolean hasWfSpecId() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <code>.littlehorse.WfSpecIdPb wf_spec_id = 3;</code>
     * @return The wfSpecId.
     */
    public io.littlehorse.sdk.common.proto.WfSpecIdPb getWfSpecId() {
      if (wfSpecIdBuilder_ == null) {
        return wfSpecId_ == null ? io.littlehorse.sdk.common.proto.WfSpecIdPb.getDefaultInstance() : wfSpecId_;
      } else {
        return wfSpecIdBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.WfSpecIdPb wf_spec_id = 3;</code>
     */
    public Builder setWfSpecId(io.littlehorse.sdk.common.proto.WfSpecIdPb value) {
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
     * <code>.littlehorse.WfSpecIdPb wf_spec_id = 3;</code>
     */
    public Builder setWfSpecId(
        io.littlehorse.sdk.common.proto.WfSpecIdPb.Builder builderForValue) {
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
     * <code>.littlehorse.WfSpecIdPb wf_spec_id = 3;</code>
     */
    public Builder mergeWfSpecId(io.littlehorse.sdk.common.proto.WfSpecIdPb value) {
      if (wfSpecIdBuilder_ == null) {
        if (((bitField0_ & 0x00000004) != 0) &&
          wfSpecId_ != null &&
          wfSpecId_ != io.littlehorse.sdk.common.proto.WfSpecIdPb.getDefaultInstance()) {
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
     * <code>.littlehorse.WfSpecIdPb wf_spec_id = 3;</code>
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
     * <code>.littlehorse.WfSpecIdPb wf_spec_id = 3;</code>
     */
    public io.littlehorse.sdk.common.proto.WfSpecIdPb.Builder getWfSpecIdBuilder() {
      bitField0_ |= 0x00000004;
      onChanged();
      return getWfSpecIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.WfSpecIdPb wf_spec_id = 3;</code>
     */
    public io.littlehorse.sdk.common.proto.WfSpecIdPbOrBuilder getWfSpecIdOrBuilder() {
      if (wfSpecIdBuilder_ != null) {
        return wfSpecIdBuilder_.getMessageOrBuilder();
      } else {
        return wfSpecId_ == null ?
            io.littlehorse.sdk.common.proto.WfSpecIdPb.getDefaultInstance() : wfSpecId_;
      }
    }
    /**
     * <code>.littlehorse.WfSpecIdPb wf_spec_id = 3;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.WfSpecIdPb, io.littlehorse.sdk.common.proto.WfSpecIdPb.Builder, io.littlehorse.sdk.common.proto.WfSpecIdPbOrBuilder> 
        getWfSpecIdFieldBuilder() {
      if (wfSpecIdBuilder_ == null) {
        wfSpecIdBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.WfSpecIdPb, io.littlehorse.sdk.common.proto.WfSpecIdPb.Builder, io.littlehorse.sdk.common.proto.WfSpecIdPbOrBuilder>(
                getWfSpecId(),
                getParentForChildren(),
                isClean());
        wfSpecId_ = null;
      }
      return wfSpecIdBuilder_;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.UserTaskTriggerReferencePb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.UserTaskTriggerReferencePb)
  private static final io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb();
  }

  public static io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<UserTaskTriggerReferencePb>
      PARSER = new com.google.protobuf.AbstractParser<UserTaskTriggerReferencePb>() {
    @java.lang.Override
    public UserTaskTriggerReferencePb parsePartialFrom(
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

  public static com.google.protobuf.Parser<UserTaskTriggerReferencePb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<UserTaskTriggerReferencePb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

