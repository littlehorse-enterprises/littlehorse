// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: node_run.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * The sub-node structure for a THROW_EVENT NodeRun.
 * </pre>
 *
 * Protobuf type {@code littlehorse.ThrowEventNodeRun}
 */
public final class ThrowEventNodeRun extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.ThrowEventNodeRun)
    ThrowEventNodeRunOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 29,
      /* patch= */ 3,
      /* suffix= */ "",
      ThrowEventNodeRun.class.getName());
  }
  // Use ThrowEventNodeRun.newBuilder() to construct.
  private ThrowEventNodeRun(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private ThrowEventNodeRun() {
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_ThrowEventNodeRun_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_ThrowEventNodeRun_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.ThrowEventNodeRun.class, io.littlehorse.sdk.common.proto.ThrowEventNodeRun.Builder.class);
  }

  private int bitField0_;
  public static final int WORKFLOW_EVENT_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.WorkflowEventId workflowEventId_;
  /**
   * <pre>
   * The ID of the `WorkflowEvent` that was thrown by this `ThrowEventNodeRun`.
   * </pre>
   *
   * <code>.littlehorse.WorkflowEventId workflow_event_id = 1;</code>
   * @return Whether the workflowEventId field is set.
   */
  @java.lang.Override
  public boolean hasWorkflowEventId() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * The ID of the `WorkflowEvent` that was thrown by this `ThrowEventNodeRun`.
   * </pre>
   *
   * <code>.littlehorse.WorkflowEventId workflow_event_id = 1;</code>
   * @return The workflowEventId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WorkflowEventId getWorkflowEventId() {
    return workflowEventId_ == null ? io.littlehorse.sdk.common.proto.WorkflowEventId.getDefaultInstance() : workflowEventId_;
  }
  /**
   * <pre>
   * The ID of the `WorkflowEvent` that was thrown by this `ThrowEventNodeRun`.
   * </pre>
   *
   * <code>.littlehorse.WorkflowEventId workflow_event_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WorkflowEventIdOrBuilder getWorkflowEventIdOrBuilder() {
    return workflowEventId_ == null ? io.littlehorse.sdk.common.proto.WorkflowEventId.getDefaultInstance() : workflowEventId_;
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
      output.writeMessage(1, getWorkflowEventId());
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
        .computeMessageSize(1, getWorkflowEventId());
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.ThrowEventNodeRun)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.ThrowEventNodeRun other = (io.littlehorse.sdk.common.proto.ThrowEventNodeRun) obj;

    if (hasWorkflowEventId() != other.hasWorkflowEventId()) return false;
    if (hasWorkflowEventId()) {
      if (!getWorkflowEventId()
          .equals(other.getWorkflowEventId())) return false;
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
    if (hasWorkflowEventId()) {
      hash = (37 * hash) + WORKFLOW_EVENT_ID_FIELD_NUMBER;
      hash = (53 * hash) + getWorkflowEventId().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.ThrowEventNodeRun parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ThrowEventNodeRun parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ThrowEventNodeRun parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ThrowEventNodeRun parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ThrowEventNodeRun parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ThrowEventNodeRun parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ThrowEventNodeRun parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ThrowEventNodeRun parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.ThrowEventNodeRun parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.ThrowEventNodeRun parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ThrowEventNodeRun parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ThrowEventNodeRun parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.ThrowEventNodeRun prototype) {
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
   * The sub-node structure for a THROW_EVENT NodeRun.
   * </pre>
   *
   * Protobuf type {@code littlehorse.ThrowEventNodeRun}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.ThrowEventNodeRun)
      io.littlehorse.sdk.common.proto.ThrowEventNodeRunOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_ThrowEventNodeRun_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_ThrowEventNodeRun_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.ThrowEventNodeRun.class, io.littlehorse.sdk.common.proto.ThrowEventNodeRun.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.ThrowEventNodeRun.newBuilder()
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
        getWorkflowEventIdFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      workflowEventId_ = null;
      if (workflowEventIdBuilder_ != null) {
        workflowEventIdBuilder_.dispose();
        workflowEventIdBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_ThrowEventNodeRun_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ThrowEventNodeRun getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.ThrowEventNodeRun.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ThrowEventNodeRun build() {
      io.littlehorse.sdk.common.proto.ThrowEventNodeRun result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ThrowEventNodeRun buildPartial() {
      io.littlehorse.sdk.common.proto.ThrowEventNodeRun result = new io.littlehorse.sdk.common.proto.ThrowEventNodeRun(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.ThrowEventNodeRun result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.workflowEventId_ = workflowEventIdBuilder_ == null
            ? workflowEventId_
            : workflowEventIdBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.sdk.common.proto.ThrowEventNodeRun) {
        return mergeFrom((io.littlehorse.sdk.common.proto.ThrowEventNodeRun)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.ThrowEventNodeRun other) {
      if (other == io.littlehorse.sdk.common.proto.ThrowEventNodeRun.getDefaultInstance()) return this;
      if (other.hasWorkflowEventId()) {
        mergeWorkflowEventId(other.getWorkflowEventId());
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
                  getWorkflowEventIdFieldBuilder().getBuilder(),
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

    private io.littlehorse.sdk.common.proto.WorkflowEventId workflowEventId_;
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.WorkflowEventId, io.littlehorse.sdk.common.proto.WorkflowEventId.Builder, io.littlehorse.sdk.common.proto.WorkflowEventIdOrBuilder> workflowEventIdBuilder_;
    /**
     * <pre>
     * The ID of the `WorkflowEvent` that was thrown by this `ThrowEventNodeRun`.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventId workflow_event_id = 1;</code>
     * @return Whether the workflowEventId field is set.
     */
    public boolean hasWorkflowEventId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * The ID of the `WorkflowEvent` that was thrown by this `ThrowEventNodeRun`.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventId workflow_event_id = 1;</code>
     * @return The workflowEventId.
     */
    public io.littlehorse.sdk.common.proto.WorkflowEventId getWorkflowEventId() {
      if (workflowEventIdBuilder_ == null) {
        return workflowEventId_ == null ? io.littlehorse.sdk.common.proto.WorkflowEventId.getDefaultInstance() : workflowEventId_;
      } else {
        return workflowEventIdBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The ID of the `WorkflowEvent` that was thrown by this `ThrowEventNodeRun`.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventId workflow_event_id = 1;</code>
     */
    public Builder setWorkflowEventId(io.littlehorse.sdk.common.proto.WorkflowEventId value) {
      if (workflowEventIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        workflowEventId_ = value;
      } else {
        workflowEventIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The ID of the `WorkflowEvent` that was thrown by this `ThrowEventNodeRun`.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventId workflow_event_id = 1;</code>
     */
    public Builder setWorkflowEventId(
        io.littlehorse.sdk.common.proto.WorkflowEventId.Builder builderForValue) {
      if (workflowEventIdBuilder_ == null) {
        workflowEventId_ = builderForValue.build();
      } else {
        workflowEventIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The ID of the `WorkflowEvent` that was thrown by this `ThrowEventNodeRun`.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventId workflow_event_id = 1;</code>
     */
    public Builder mergeWorkflowEventId(io.littlehorse.sdk.common.proto.WorkflowEventId value) {
      if (workflowEventIdBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          workflowEventId_ != null &&
          workflowEventId_ != io.littlehorse.sdk.common.proto.WorkflowEventId.getDefaultInstance()) {
          getWorkflowEventIdBuilder().mergeFrom(value);
        } else {
          workflowEventId_ = value;
        }
      } else {
        workflowEventIdBuilder_.mergeFrom(value);
      }
      if (workflowEventId_ != null) {
        bitField0_ |= 0x00000001;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * The ID of the `WorkflowEvent` that was thrown by this `ThrowEventNodeRun`.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventId workflow_event_id = 1;</code>
     */
    public Builder clearWorkflowEventId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      workflowEventId_ = null;
      if (workflowEventIdBuilder_ != null) {
        workflowEventIdBuilder_.dispose();
        workflowEventIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The ID of the `WorkflowEvent` that was thrown by this `ThrowEventNodeRun`.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventId workflow_event_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.WorkflowEventId.Builder getWorkflowEventIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getWorkflowEventIdFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The ID of the `WorkflowEvent` that was thrown by this `ThrowEventNodeRun`.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventId workflow_event_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.WorkflowEventIdOrBuilder getWorkflowEventIdOrBuilder() {
      if (workflowEventIdBuilder_ != null) {
        return workflowEventIdBuilder_.getMessageOrBuilder();
      } else {
        return workflowEventId_ == null ?
            io.littlehorse.sdk.common.proto.WorkflowEventId.getDefaultInstance() : workflowEventId_;
      }
    }
    /**
     * <pre>
     * The ID of the `WorkflowEvent` that was thrown by this `ThrowEventNodeRun`.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventId workflow_event_id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.WorkflowEventId, io.littlehorse.sdk.common.proto.WorkflowEventId.Builder, io.littlehorse.sdk.common.proto.WorkflowEventIdOrBuilder> 
        getWorkflowEventIdFieldBuilder() {
      if (workflowEventIdBuilder_ == null) {
        workflowEventIdBuilder_ = new com.google.protobuf.SingleFieldBuilder<
            io.littlehorse.sdk.common.proto.WorkflowEventId, io.littlehorse.sdk.common.proto.WorkflowEventId.Builder, io.littlehorse.sdk.common.proto.WorkflowEventIdOrBuilder>(
                getWorkflowEventId(),
                getParentForChildren(),
                isClean());
        workflowEventId_ = null;
      }
      return workflowEventIdBuilder_;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.ThrowEventNodeRun)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.ThrowEventNodeRun)
  private static final io.littlehorse.sdk.common.proto.ThrowEventNodeRun DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.ThrowEventNodeRun();
  }

  public static io.littlehorse.sdk.common.proto.ThrowEventNodeRun getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ThrowEventNodeRun>
      PARSER = new com.google.protobuf.AbstractParser<ThrowEventNodeRun>() {
    @java.lang.Override
    public ThrowEventNodeRun parsePartialFrom(
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

  public static com.google.protobuf.Parser<ThrowEventNodeRun> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ThrowEventNodeRun> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ThrowEventNodeRun getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

