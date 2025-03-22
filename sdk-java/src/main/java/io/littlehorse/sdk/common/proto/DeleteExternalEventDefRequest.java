// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: service.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Deletes an ExternalEventDef
 * </pre>
 *
 * Protobuf type {@code littlehorse.DeleteExternalEventDefRequest}
 */
public final class DeleteExternalEventDefRequest extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.DeleteExternalEventDefRequest)
    DeleteExternalEventDefRequestOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      DeleteExternalEventDefRequest.class.getName());
  }
  // Use DeleteExternalEventDefRequest.newBuilder() to construct.
  private DeleteExternalEventDefRequest(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private DeleteExternalEventDefRequest() {
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_DeleteExternalEventDefRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_DeleteExternalEventDefRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest.class, io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest.Builder.class);
  }

  private int bitField0_;
  public static final int ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.ExternalEventDefId id_;
  /**
   * <pre>
   * The ID of the ExternalEventDef to delete.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId id = 1;</code>
   * @return Whether the id field is set.
   */
  @java.lang.Override
  public boolean hasId() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * The ID of the ExternalEventDef to delete.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId id = 1;</code>
   * @return The id.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ExternalEventDefId getId() {
    return id_ == null ? io.littlehorse.sdk.common.proto.ExternalEventDefId.getDefaultInstance() : id_;
  }
  /**
   * <pre>
   * The ID of the ExternalEventDef to delete.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder getIdOrBuilder() {
    return id_ == null ? io.littlehorse.sdk.common.proto.ExternalEventDefId.getDefaultInstance() : id_;
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
      output.writeMessage(1, getId());
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
        .computeMessageSize(1, getId());
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest other = (io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest) obj;

    if (hasId() != other.hasId()) return false;
    if (hasId()) {
      if (!getId()
          .equals(other.getId())) return false;
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
    if (hasId()) {
      hash = (37 * hash) + ID_FIELD_NUMBER;
      hash = (53 * hash) + getId().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest prototype) {
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
   * Deletes an ExternalEventDef
   * </pre>
   *
   * Protobuf type {@code littlehorse.DeleteExternalEventDefRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.DeleteExternalEventDefRequest)
      io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_DeleteExternalEventDefRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_DeleteExternalEventDefRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest.class, io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest.newBuilder()
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
        internalGetIdFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      id_ = null;
      if (idBuilder_ != null) {
        idBuilder_.dispose();
        idBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_DeleteExternalEventDefRequest_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest build() {
      io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest buildPartial() {
      io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest result = new io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.id_ = idBuilder_ == null
            ? id_
            : idBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest) {
        return mergeFrom((io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest other) {
      if (other == io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest.getDefaultInstance()) return this;
      if (other.hasId()) {
        mergeId(other.getId());
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
                  internalGetIdFieldBuilder().getBuilder(),
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

    private io.littlehorse.sdk.common.proto.ExternalEventDefId id_;
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.ExternalEventDefId, io.littlehorse.sdk.common.proto.ExternalEventDefId.Builder, io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder> idBuilder_;
    /**
     * <pre>
     * The ID of the ExternalEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId id = 1;</code>
     * @return Whether the id field is set.
     */
    public boolean hasId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId id = 1;</code>
     * @return The id.
     */
    public io.littlehorse.sdk.common.proto.ExternalEventDefId getId() {
      if (idBuilder_ == null) {
        return id_ == null ? io.littlehorse.sdk.common.proto.ExternalEventDefId.getDefaultInstance() : id_;
      } else {
        return idBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId id = 1;</code>
     */
    public Builder setId(io.littlehorse.sdk.common.proto.ExternalEventDefId value) {
      if (idBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        id_ = value;
      } else {
        idBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId id = 1;</code>
     */
    public Builder setId(
        io.littlehorse.sdk.common.proto.ExternalEventDefId.Builder builderForValue) {
      if (idBuilder_ == null) {
        id_ = builderForValue.build();
      } else {
        idBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId id = 1;</code>
     */
    public Builder mergeId(io.littlehorse.sdk.common.proto.ExternalEventDefId value) {
      if (idBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          id_ != null &&
          id_ != io.littlehorse.sdk.common.proto.ExternalEventDefId.getDefaultInstance()) {
          getIdBuilder().mergeFrom(value);
        } else {
          id_ = value;
        }
      } else {
        idBuilder_.mergeFrom(value);
      }
      if (id_ != null) {
        bitField0_ |= 0x00000001;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId id = 1;</code>
     */
    public Builder clearId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      id_ = null;
      if (idBuilder_ != null) {
        idBuilder_.dispose();
        idBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.ExternalEventDefId.Builder getIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return internalGetIdFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder getIdOrBuilder() {
      if (idBuilder_ != null) {
        return idBuilder_.getMessageOrBuilder();
      } else {
        return id_ == null ?
            io.littlehorse.sdk.common.proto.ExternalEventDefId.getDefaultInstance() : id_;
      }
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.ExternalEventDefId, io.littlehorse.sdk.common.proto.ExternalEventDefId.Builder, io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder> 
        internalGetIdFieldBuilder() {
      if (idBuilder_ == null) {
        idBuilder_ = new com.google.protobuf.SingleFieldBuilder<
            io.littlehorse.sdk.common.proto.ExternalEventDefId, io.littlehorse.sdk.common.proto.ExternalEventDefId.Builder, io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder>(
                getId(),
                getParentForChildren(),
                isClean());
        id_ = null;
      }
      return idBuilder_;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.DeleteExternalEventDefRequest)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.DeleteExternalEventDefRequest)
  private static final io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest();
  }

  public static io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<DeleteExternalEventDefRequest>
      PARSER = new com.google.protobuf.AbstractParser<DeleteExternalEventDefRequest>() {
    @java.lang.Override
    public DeleteExternalEventDefRequest parsePartialFrom(
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

  public static com.google.protobuf.Parser<DeleteExternalEventDefRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<DeleteExternalEventDefRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

