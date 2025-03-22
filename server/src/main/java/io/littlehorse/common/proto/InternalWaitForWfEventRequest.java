// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: interactive_query.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.common.proto;

/**
 * Protobuf type {@code littlehorse.InternalWaitForWfEventRequest}
 */
public final class InternalWaitForWfEventRequest extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.InternalWaitForWfEventRequest)
    InternalWaitForWfEventRequestOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      InternalWaitForWfEventRequest.class.getName());
  }
  // Use InternalWaitForWfEventRequest.newBuilder() to construct.
  private InternalWaitForWfEventRequest(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private InternalWaitForWfEventRequest() {
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.InteractiveQuery.internal_static_littlehorse_InternalWaitForWfEventRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.InteractiveQuery.internal_static_littlehorse_InternalWaitForWfEventRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.InternalWaitForWfEventRequest.class, io.littlehorse.common.proto.InternalWaitForWfEventRequest.Builder.class);
  }

  private int bitField0_;
  public static final int REQUEST_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest request_;
  /**
   * <pre>
   * For now, we just pass the raw input from the external server. That's all we need to know.
   * </pre>
   *
   * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
   * @return Whether the request field is set.
   */
  @java.lang.Override
  public boolean hasRequest() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * For now, we just pass the raw input from the external server. That's all we need to know.
   * </pre>
   *
   * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
   * @return The request.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest getRequest() {
    return request_ == null ? io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest.getDefaultInstance() : request_;
  }
  /**
   * <pre>
   * For now, we just pass the raw input from the external server. That's all we need to know.
   * </pre>
   *
   * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequestOrBuilder getRequestOrBuilder() {
    return request_ == null ? io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest.getDefaultInstance() : request_;
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
      output.writeMessage(1, getRequest());
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
        .computeMessageSize(1, getRequest());
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
    if (!(obj instanceof io.littlehorse.common.proto.InternalWaitForWfEventRequest)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.InternalWaitForWfEventRequest other = (io.littlehorse.common.proto.InternalWaitForWfEventRequest) obj;

    if (hasRequest() != other.hasRequest()) return false;
    if (hasRequest()) {
      if (!getRequest()
          .equals(other.getRequest())) return false;
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
    if (hasRequest()) {
      hash = (37 * hash) + REQUEST_FIELD_NUMBER;
      hash = (53 * hash) + getRequest().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.InternalWaitForWfEventRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.InternalWaitForWfEventRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.InternalWaitForWfEventRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.InternalWaitForWfEventRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.InternalWaitForWfEventRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.InternalWaitForWfEventRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.InternalWaitForWfEventRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.InternalWaitForWfEventRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.InternalWaitForWfEventRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.InternalWaitForWfEventRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.InternalWaitForWfEventRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.InternalWaitForWfEventRequest parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.InternalWaitForWfEventRequest prototype) {
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
   * Protobuf type {@code littlehorse.InternalWaitForWfEventRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.InternalWaitForWfEventRequest)
      io.littlehorse.common.proto.InternalWaitForWfEventRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.InteractiveQuery.internal_static_littlehorse_InternalWaitForWfEventRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.InteractiveQuery.internal_static_littlehorse_InternalWaitForWfEventRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.InternalWaitForWfEventRequest.class, io.littlehorse.common.proto.InternalWaitForWfEventRequest.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.InternalWaitForWfEventRequest.newBuilder()
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
        internalGetRequestFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      request_ = null;
      if (requestBuilder_ != null) {
        requestBuilder_.dispose();
        requestBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.InteractiveQuery.internal_static_littlehorse_InternalWaitForWfEventRequest_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.InternalWaitForWfEventRequest getDefaultInstanceForType() {
      return io.littlehorse.common.proto.InternalWaitForWfEventRequest.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.InternalWaitForWfEventRequest build() {
      io.littlehorse.common.proto.InternalWaitForWfEventRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.InternalWaitForWfEventRequest buildPartial() {
      io.littlehorse.common.proto.InternalWaitForWfEventRequest result = new io.littlehorse.common.proto.InternalWaitForWfEventRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.common.proto.InternalWaitForWfEventRequest result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.request_ = requestBuilder_ == null
            ? request_
            : requestBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.common.proto.InternalWaitForWfEventRequest) {
        return mergeFrom((io.littlehorse.common.proto.InternalWaitForWfEventRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.InternalWaitForWfEventRequest other) {
      if (other == io.littlehorse.common.proto.InternalWaitForWfEventRequest.getDefaultInstance()) return this;
      if (other.hasRequest()) {
        mergeRequest(other.getRequest());
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
                  internalGetRequestFieldBuilder().getBuilder(),
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

    private io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest request_;
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest, io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest.Builder, io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequestOrBuilder> requestBuilder_;
    /**
     * <pre>
     * For now, we just pass the raw input from the external server. That's all we need to know.
     * </pre>
     *
     * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
     * @return Whether the request field is set.
     */
    public boolean hasRequest() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * For now, we just pass the raw input from the external server. That's all we need to know.
     * </pre>
     *
     * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
     * @return The request.
     */
    public io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest getRequest() {
      if (requestBuilder_ == null) {
        return request_ == null ? io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest.getDefaultInstance() : request_;
      } else {
        return requestBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * For now, we just pass the raw input from the external server. That's all we need to know.
     * </pre>
     *
     * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
     */
    public Builder setRequest(io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest value) {
      if (requestBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        request_ = value;
      } else {
        requestBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * For now, we just pass the raw input from the external server. That's all we need to know.
     * </pre>
     *
     * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
     */
    public Builder setRequest(
        io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest.Builder builderForValue) {
      if (requestBuilder_ == null) {
        request_ = builderForValue.build();
      } else {
        requestBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * For now, we just pass the raw input from the external server. That's all we need to know.
     * </pre>
     *
     * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
     */
    public Builder mergeRequest(io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest value) {
      if (requestBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          request_ != null &&
          request_ != io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest.getDefaultInstance()) {
          getRequestBuilder().mergeFrom(value);
        } else {
          request_ = value;
        }
      } else {
        requestBuilder_.mergeFrom(value);
      }
      if (request_ != null) {
        bitField0_ |= 0x00000001;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * For now, we just pass the raw input from the external server. That's all we need to know.
     * </pre>
     *
     * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
     */
    public Builder clearRequest() {
      bitField0_ = (bitField0_ & ~0x00000001);
      request_ = null;
      if (requestBuilder_ != null) {
        requestBuilder_.dispose();
        requestBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * For now, we just pass the raw input from the external server. That's all we need to know.
     * </pre>
     *
     * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest.Builder getRequestBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return internalGetRequestFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * For now, we just pass the raw input from the external server. That's all we need to know.
     * </pre>
     *
     * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequestOrBuilder getRequestOrBuilder() {
      if (requestBuilder_ != null) {
        return requestBuilder_.getMessageOrBuilder();
      } else {
        return request_ == null ?
            io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest.getDefaultInstance() : request_;
      }
    }
    /**
     * <pre>
     * For now, we just pass the raw input from the external server. That's all we need to know.
     * </pre>
     *
     * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest, io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest.Builder, io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequestOrBuilder> 
        internalGetRequestFieldBuilder() {
      if (requestBuilder_ == null) {
        requestBuilder_ = new com.google.protobuf.SingleFieldBuilder<
            io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest, io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest.Builder, io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequestOrBuilder>(
                getRequest(),
                getParentForChildren(),
                isClean());
        request_ = null;
      }
      return requestBuilder_;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.InternalWaitForWfEventRequest)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.InternalWaitForWfEventRequest)
  private static final io.littlehorse.common.proto.InternalWaitForWfEventRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.InternalWaitForWfEventRequest();
  }

  public static io.littlehorse.common.proto.InternalWaitForWfEventRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<InternalWaitForWfEventRequest>
      PARSER = new com.google.protobuf.AbstractParser<InternalWaitForWfEventRequest>() {
    @java.lang.Override
    public InternalWaitForWfEventRequest parsePartialFrom(
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

  public static com.google.protobuf.Parser<InternalWaitForWfEventRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<InternalWaitForWfEventRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.InternalWaitForWfEventRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

