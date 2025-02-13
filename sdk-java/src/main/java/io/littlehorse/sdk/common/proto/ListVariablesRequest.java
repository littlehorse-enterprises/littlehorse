// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: service.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * List all Variables for a specific WfRun. Note that List requests return actual Variable Objects,
 * not VariableId's.
 * </pre>
 *
 * Protobuf type {@code littlehorse.ListVariablesRequest}
 */
public final class ListVariablesRequest extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.ListVariablesRequest)
    ListVariablesRequestOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 29,
      /* patch= */ 3,
      /* suffix= */ "",
      ListVariablesRequest.class.getName());
  }
  // Use ListVariablesRequest.newBuilder() to construct.
  private ListVariablesRequest(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private ListVariablesRequest() {
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListVariablesRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListVariablesRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.ListVariablesRequest.class, io.littlehorse.sdk.common.proto.ListVariablesRequest.Builder.class);
  }

  private int bitField0_;
  public static final int WF_RUN_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.WfRunId wfRunId_;
  /**
   * <pre>
   * The WfRun for whom we will list Variables.
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
   * The WfRun for whom we will list Variables.
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
   * The WfRun for whom we will list Variables.
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WfRunIdOrBuilder getWfRunIdOrBuilder() {
    return wfRunId_ == null ? io.littlehorse.sdk.common.proto.WfRunId.getDefaultInstance() : wfRunId_;
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
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof io.littlehorse.sdk.common.proto.ListVariablesRequest)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.ListVariablesRequest other = (io.littlehorse.sdk.common.proto.ListVariablesRequest) obj;

    if (hasWfRunId() != other.hasWfRunId()) return false;
    if (hasWfRunId()) {
      if (!getWfRunId()
          .equals(other.getWfRunId())) return false;
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
    if (hasWfRunId()) {
      hash = (37 * hash) + WF_RUN_ID_FIELD_NUMBER;
      hash = (53 * hash) + getWfRunId().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.ListVariablesRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ListVariablesRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ListVariablesRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ListVariablesRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ListVariablesRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ListVariablesRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ListVariablesRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ListVariablesRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.ListVariablesRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.ListVariablesRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ListVariablesRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ListVariablesRequest parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.ListVariablesRequest prototype) {
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
   * List all Variables for a specific WfRun. Note that List requests return actual Variable Objects,
   * not VariableId's.
   * </pre>
   *
   * Protobuf type {@code littlehorse.ListVariablesRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.ListVariablesRequest)
      io.littlehorse.sdk.common.proto.ListVariablesRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListVariablesRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListVariablesRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.ListVariablesRequest.class, io.littlehorse.sdk.common.proto.ListVariablesRequest.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.ListVariablesRequest.newBuilder()
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
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListVariablesRequest_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ListVariablesRequest getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.ListVariablesRequest.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ListVariablesRequest build() {
      io.littlehorse.sdk.common.proto.ListVariablesRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ListVariablesRequest buildPartial() {
      io.littlehorse.sdk.common.proto.ListVariablesRequest result = new io.littlehorse.sdk.common.proto.ListVariablesRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.ListVariablesRequest result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.wfRunId_ = wfRunIdBuilder_ == null
            ? wfRunId_
            : wfRunIdBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.sdk.common.proto.ListVariablesRequest) {
        return mergeFrom((io.littlehorse.sdk.common.proto.ListVariablesRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.ListVariablesRequest other) {
      if (other == io.littlehorse.sdk.common.proto.ListVariablesRequest.getDefaultInstance()) return this;
      if (other.hasWfRunId()) {
        mergeWfRunId(other.getWfRunId());
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
     * The WfRun for whom we will list Variables.
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
     * The WfRun for whom we will list Variables.
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
     * The WfRun for whom we will list Variables.
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
     * The WfRun for whom we will list Variables.
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
     * The WfRun for whom we will list Variables.
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
     * The WfRun for whom we will list Variables.
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
     * The WfRun for whom we will list Variables.
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
     * The WfRun for whom we will list Variables.
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
     * The WfRun for whom we will list Variables.
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

    // @@protoc_insertion_point(builder_scope:littlehorse.ListVariablesRequest)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.ListVariablesRequest)
  private static final io.littlehorse.sdk.common.proto.ListVariablesRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.ListVariablesRequest();
  }

  public static io.littlehorse.sdk.common.proto.ListVariablesRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ListVariablesRequest>
      PARSER = new com.google.protobuf.AbstractParser<ListVariablesRequest>() {
    @java.lang.Override
    public ListVariablesRequest parsePartialFrom(
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

  public static com.google.protobuf.Parser<ListVariablesRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ListVariablesRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ListVariablesRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

