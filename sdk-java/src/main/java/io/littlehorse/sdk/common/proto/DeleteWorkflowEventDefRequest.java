// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Deletes an WorkflowEventDef
 * </pre>
 *
 * Protobuf type {@code littlehorse.DeleteWorkflowEventDefRequest}
 */
public final class DeleteWorkflowEventDefRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.DeleteWorkflowEventDefRequest)
    DeleteWorkflowEventDefRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use DeleteWorkflowEventDefRequest.newBuilder() to construct.
  private DeleteWorkflowEventDefRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private DeleteWorkflowEventDefRequest() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new DeleteWorkflowEventDefRequest();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_DeleteWorkflowEventDefRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_DeleteWorkflowEventDefRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest.class, io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest.Builder.class);
  }

  public static final int ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.WorkflowEventDefId id_;
  /**
   * <pre>
   * The ID of the WorkflowEventDef to delete.
   * </pre>
   *
   * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
   * @return Whether the id field is set.
   */
  @java.lang.Override
  public boolean hasId() {
    return id_ != null;
  }
  /**
   * <pre>
   * The ID of the WorkflowEventDef to delete.
   * </pre>
   *
   * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
   * @return The id.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WorkflowEventDefId getId() {
    return id_ == null ? io.littlehorse.sdk.common.proto.WorkflowEventDefId.getDefaultInstance() : id_;
  }
  /**
   * <pre>
   * The ID of the WorkflowEventDef to delete.
   * </pre>
   *
   * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WorkflowEventDefIdOrBuilder getIdOrBuilder() {
    return id_ == null ? io.littlehorse.sdk.common.proto.WorkflowEventDefId.getDefaultInstance() : id_;
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
    if (id_ != null) {
      output.writeMessage(1, getId());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (id_ != null) {
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest other = (io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest) obj;

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

  public static io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest prototype) {
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
   * <pre>
   * Deletes an WorkflowEventDef
   * </pre>
   *
   * Protobuf type {@code littlehorse.DeleteWorkflowEventDefRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.DeleteWorkflowEventDefRequest)
      io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_DeleteWorkflowEventDefRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_DeleteWorkflowEventDefRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest.class, io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest.newBuilder()
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
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_DeleteWorkflowEventDefRequest_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest build() {
      io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest buildPartial() {
      io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest result = new io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.id_ = idBuilder_ == null
            ? id_
            : idBuilder_.build();
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
      if (other instanceof io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest) {
        return mergeFrom((io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest other) {
      if (other == io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest.getDefaultInstance()) return this;
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
                  getIdFieldBuilder().getBuilder(),
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

    private io.littlehorse.sdk.common.proto.WorkflowEventDefId id_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.WorkflowEventDefId, io.littlehorse.sdk.common.proto.WorkflowEventDefId.Builder, io.littlehorse.sdk.common.proto.WorkflowEventDefIdOrBuilder> idBuilder_;
    /**
     * <pre>
     * The ID of the WorkflowEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     * @return Whether the id field is set.
     */
    public boolean hasId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * The ID of the WorkflowEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     * @return The id.
     */
    public io.littlehorse.sdk.common.proto.WorkflowEventDefId getId() {
      if (idBuilder_ == null) {
        return id_ == null ? io.littlehorse.sdk.common.proto.WorkflowEventDefId.getDefaultInstance() : id_;
      } else {
        return idBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The ID of the WorkflowEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     */
    public Builder setId(io.littlehorse.sdk.common.proto.WorkflowEventDefId value) {
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
     * The ID of the WorkflowEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     */
    public Builder setId(
        io.littlehorse.sdk.common.proto.WorkflowEventDefId.Builder builderForValue) {
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
     * The ID of the WorkflowEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     */
    public Builder mergeId(io.littlehorse.sdk.common.proto.WorkflowEventDefId value) {
      if (idBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          id_ != null &&
          id_ != io.littlehorse.sdk.common.proto.WorkflowEventDefId.getDefaultInstance()) {
          getIdBuilder().mergeFrom(value);
        } else {
          id_ = value;
        }
      } else {
        idBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The ID of the WorkflowEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
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
     * The ID of the WorkflowEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.WorkflowEventDefId.Builder getIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getIdFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The ID of the WorkflowEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.WorkflowEventDefIdOrBuilder getIdOrBuilder() {
      if (idBuilder_ != null) {
        return idBuilder_.getMessageOrBuilder();
      } else {
        return id_ == null ?
            io.littlehorse.sdk.common.proto.WorkflowEventDefId.getDefaultInstance() : id_;
      }
    }
    /**
     * <pre>
     * The ID of the WorkflowEventDef to delete.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.WorkflowEventDefId, io.littlehorse.sdk.common.proto.WorkflowEventDefId.Builder, io.littlehorse.sdk.common.proto.WorkflowEventDefIdOrBuilder> 
        getIdFieldBuilder() {
      if (idBuilder_ == null) {
        idBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.WorkflowEventDefId, io.littlehorse.sdk.common.proto.WorkflowEventDefId.Builder, io.littlehorse.sdk.common.proto.WorkflowEventDefIdOrBuilder>(
                getId(),
                getParentForChildren(),
                isClean());
        id_ = null;
      }
      return idBuilder_;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.DeleteWorkflowEventDefRequest)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.DeleteWorkflowEventDefRequest)
  private static final io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest();
  }

  public static io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<DeleteWorkflowEventDefRequest>
      PARSER = new com.google.protobuf.AbstractParser<DeleteWorkflowEventDefRequest>() {
    @java.lang.Override
    public DeleteWorkflowEventDefRequest parsePartialFrom(
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

  public static com.google.protobuf.Parser<DeleteWorkflowEventDefRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<DeleteWorkflowEventDefRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

