// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Response detailing the validity of a StructDef evolution.
 * </pre>
 *
 * Protobuf type {@code littlehorse.ValidateStructDefEvolutionResponse}
 */
public final class ValidateStructDefEvolutionResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.ValidateStructDefEvolutionResponse)
    ValidateStructDefEvolutionResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ValidateStructDefEvolutionResponse.newBuilder() to construct.
  private ValidateStructDefEvolutionResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ValidateStructDefEvolutionResponse() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ValidateStructDefEvolutionResponse();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ValidateStructDefEvolutionResponse_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ValidateStructDefEvolutionResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse.class, io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse.Builder.class);
  }

  public static final int IS_VALID_FIELD_NUMBER = 1;
  private boolean isValid_ = false;
  /**
   * <pre>
   * Whether or not the StructDef evolution specified in the request is valid.
   * </pre>
   *
   * <code>bool is_valid = 1;</code>
   * @return The isValid.
   */
  @java.lang.Override
  public boolean getIsValid() {
    return isValid_;
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
    if (isValid_ != false) {
      output.writeBool(1, isValid_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (isValid_ != false) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(1, isValid_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse other = (io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse) obj;

    if (getIsValid()
        != other.getIsValid()) return false;
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
    hash = (37 * hash) + IS_VALID_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getIsValid());
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse prototype) {
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
   * Response detailing the validity of a StructDef evolution.
   * </pre>
   *
   * Protobuf type {@code littlehorse.ValidateStructDefEvolutionResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.ValidateStructDefEvolutionResponse)
      io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ValidateStructDefEvolutionResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ValidateStructDefEvolutionResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse.class, io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse.newBuilder()
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
      isValid_ = false;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ValidateStructDefEvolutionResponse_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse build() {
      io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse buildPartial() {
      io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse result = new io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.isValid_ = isValid_;
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
      if (other instanceof io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse) {
        return mergeFrom((io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse other) {
      if (other == io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse.getDefaultInstance()) return this;
      if (other.getIsValid() != false) {
        setIsValid(other.getIsValid());
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
            case 8: {
              isValid_ = input.readBool();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
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

    private boolean isValid_ ;
    /**
     * <pre>
     * Whether or not the StructDef evolution specified in the request is valid.
     * </pre>
     *
     * <code>bool is_valid = 1;</code>
     * @return The isValid.
     */
    @java.lang.Override
    public boolean getIsValid() {
      return isValid_;
    }
    /**
     * <pre>
     * Whether or not the StructDef evolution specified in the request is valid.
     * </pre>
     *
     * <code>bool is_valid = 1;</code>
     * @param value The isValid to set.
     * @return This builder for chaining.
     */
    public Builder setIsValid(boolean value) {

      isValid_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Whether or not the StructDef evolution specified in the request is valid.
     * </pre>
     *
     * <code>bool is_valid = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearIsValid() {
      bitField0_ = (bitField0_ & ~0x00000001);
      isValid_ = false;
      onChanged();
      return this;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.ValidateStructDefEvolutionResponse)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.ValidateStructDefEvolutionResponse)
  private static final io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse();
  }

  public static io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ValidateStructDefEvolutionResponse>
      PARSER = new com.google.protobuf.AbstractParser<ValidateStructDefEvolutionResponse>() {
    @java.lang.Override
    public ValidateStructDefEvolutionResponse parsePartialFrom(
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

  public static com.google.protobuf.Parser<ValidateStructDefEvolutionResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ValidateStructDefEvolutionResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

