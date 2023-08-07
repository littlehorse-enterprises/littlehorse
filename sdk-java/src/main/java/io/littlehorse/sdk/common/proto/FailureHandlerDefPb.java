// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.FailureHandlerDefPb}
 */
public final class FailureHandlerDefPb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.FailureHandlerDefPb)
    FailureHandlerDefPbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use FailureHandlerDefPb.newBuilder() to construct.
  private FailureHandlerDefPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private FailureHandlerDefPb() {
    specificFailure_ = "";
    handlerSpecName_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new FailureHandlerDefPb();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_FailureHandlerDefPb_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_FailureHandlerDefPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.FailureHandlerDefPb.class, io.littlehorse.sdk.common.proto.FailureHandlerDefPb.Builder.class);
  }

  private int bitField0_;
  public static final int SPECIFIC_FAILURE_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object specificFailure_ = "";
  /**
   * <code>optional string specific_failure = 1;</code>
   * @return Whether the specificFailure field is set.
   */
  @java.lang.Override
  public boolean hasSpecificFailure() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional string specific_failure = 1;</code>
   * @return The specificFailure.
   */
  @java.lang.Override
  public java.lang.String getSpecificFailure() {
    java.lang.Object ref = specificFailure_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      specificFailure_ = s;
      return s;
    }
  }
  /**
   * <code>optional string specific_failure = 1;</code>
   * @return The bytes for specificFailure.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getSpecificFailureBytes() {
    java.lang.Object ref = specificFailure_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      specificFailure_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int HANDLER_SPEC_NAME_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private volatile java.lang.Object handlerSpecName_ = "";
  /**
   * <code>string handler_spec_name = 2;</code>
   * @return The handlerSpecName.
   */
  @java.lang.Override
  public java.lang.String getHandlerSpecName() {
    java.lang.Object ref = handlerSpecName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      handlerSpecName_ = s;
      return s;
    }
  }
  /**
   * <code>string handler_spec_name = 2;</code>
   * @return The bytes for handlerSpecName.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getHandlerSpecNameBytes() {
    java.lang.Object ref = handlerSpecName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      handlerSpecName_ = b;
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
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, specificFailure_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(handlerSpecName_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, handlerSpecName_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, specificFailure_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(handlerSpecName_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, handlerSpecName_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.FailureHandlerDefPb)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.FailureHandlerDefPb other = (io.littlehorse.sdk.common.proto.FailureHandlerDefPb) obj;

    if (hasSpecificFailure() != other.hasSpecificFailure()) return false;
    if (hasSpecificFailure()) {
      if (!getSpecificFailure()
          .equals(other.getSpecificFailure())) return false;
    }
    if (!getHandlerSpecName()
        .equals(other.getHandlerSpecName())) return false;
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
    if (hasSpecificFailure()) {
      hash = (37 * hash) + SPECIFIC_FAILURE_FIELD_NUMBER;
      hash = (53 * hash) + getSpecificFailure().hashCode();
    }
    hash = (37 * hash) + HANDLER_SPEC_NAME_FIELD_NUMBER;
    hash = (53 * hash) + getHandlerSpecName().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.FailureHandlerDefPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.FailureHandlerDefPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.FailureHandlerDefPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.FailureHandlerDefPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.FailureHandlerDefPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.FailureHandlerDefPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.FailureHandlerDefPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.FailureHandlerDefPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.FailureHandlerDefPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.FailureHandlerDefPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.FailureHandlerDefPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.FailureHandlerDefPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.FailureHandlerDefPb prototype) {
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
   * Protobuf type {@code littlehorse.FailureHandlerDefPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.FailureHandlerDefPb)
      io.littlehorse.sdk.common.proto.FailureHandlerDefPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_FailureHandlerDefPb_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_FailureHandlerDefPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.FailureHandlerDefPb.class, io.littlehorse.sdk.common.proto.FailureHandlerDefPb.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.FailureHandlerDefPb.newBuilder()
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
      specificFailure_ = "";
      handlerSpecName_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_FailureHandlerDefPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.FailureHandlerDefPb getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.FailureHandlerDefPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.FailureHandlerDefPb build() {
      io.littlehorse.sdk.common.proto.FailureHandlerDefPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.FailureHandlerDefPb buildPartial() {
      io.littlehorse.sdk.common.proto.FailureHandlerDefPb result = new io.littlehorse.sdk.common.proto.FailureHandlerDefPb(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.FailureHandlerDefPb result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.specificFailure_ = specificFailure_;
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.handlerSpecName_ = handlerSpecName_;
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
      if (other instanceof io.littlehorse.sdk.common.proto.FailureHandlerDefPb) {
        return mergeFrom((io.littlehorse.sdk.common.proto.FailureHandlerDefPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.FailureHandlerDefPb other) {
      if (other == io.littlehorse.sdk.common.proto.FailureHandlerDefPb.getDefaultInstance()) return this;
      if (other.hasSpecificFailure()) {
        specificFailure_ = other.specificFailure_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (!other.getHandlerSpecName().isEmpty()) {
        handlerSpecName_ = other.handlerSpecName_;
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
              specificFailure_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              handlerSpecName_ = input.readStringRequireUtf8();
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

    private java.lang.Object specificFailure_ = "";
    /**
     * <code>optional string specific_failure = 1;</code>
     * @return Whether the specificFailure field is set.
     */
    public boolean hasSpecificFailure() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>optional string specific_failure = 1;</code>
     * @return The specificFailure.
     */
    public java.lang.String getSpecificFailure() {
      java.lang.Object ref = specificFailure_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        specificFailure_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>optional string specific_failure = 1;</code>
     * @return The bytes for specificFailure.
     */
    public com.google.protobuf.ByteString
        getSpecificFailureBytes() {
      java.lang.Object ref = specificFailure_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        specificFailure_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>optional string specific_failure = 1;</code>
     * @param value The specificFailure to set.
     * @return This builder for chaining.
     */
    public Builder setSpecificFailure(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      specificFailure_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>optional string specific_failure = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearSpecificFailure() {
      specificFailure_ = getDefaultInstance().getSpecificFailure();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>optional string specific_failure = 1;</code>
     * @param value The bytes for specificFailure to set.
     * @return This builder for chaining.
     */
    public Builder setSpecificFailureBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      specificFailure_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private java.lang.Object handlerSpecName_ = "";
    /**
     * <code>string handler_spec_name = 2;</code>
     * @return The handlerSpecName.
     */
    public java.lang.String getHandlerSpecName() {
      java.lang.Object ref = handlerSpecName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        handlerSpecName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string handler_spec_name = 2;</code>
     * @return The bytes for handlerSpecName.
     */
    public com.google.protobuf.ByteString
        getHandlerSpecNameBytes() {
      java.lang.Object ref = handlerSpecName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        handlerSpecName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string handler_spec_name = 2;</code>
     * @param value The handlerSpecName to set.
     * @return This builder for chaining.
     */
    public Builder setHandlerSpecName(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      handlerSpecName_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>string handler_spec_name = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearHandlerSpecName() {
      handlerSpecName_ = getDefaultInstance().getHandlerSpecName();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /**
     * <code>string handler_spec_name = 2;</code>
     * @param value The bytes for handlerSpecName to set.
     * @return This builder for chaining.
     */
    public Builder setHandlerSpecNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      handlerSpecName_ = value;
      bitField0_ |= 0x00000002;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.FailureHandlerDefPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.FailureHandlerDefPb)
  private static final io.littlehorse.sdk.common.proto.FailureHandlerDefPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.FailureHandlerDefPb();
  }

  public static io.littlehorse.sdk.common.proto.FailureHandlerDefPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<FailureHandlerDefPb>
      PARSER = new com.google.protobuf.AbstractParser<FailureHandlerDefPb>() {
    @java.lang.Override
    public FailureHandlerDefPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<FailureHandlerDefPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<FailureHandlerDefPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.FailureHandlerDefPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

