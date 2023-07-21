// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.PendingInterruptPb}
 */
public final class PendingInterruptPb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.PendingInterruptPb)
    PendingInterruptPbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use PendingInterruptPb.newBuilder() to construct.
  private PendingInterruptPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private PendingInterruptPb() {
    handlerSpecName_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new PendingInterruptPb();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_PendingInterruptPb_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_PendingInterruptPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.PendingInterruptPb.class, io.littlehorse.sdk.common.proto.PendingInterruptPb.Builder.class);
  }

  public static final int EXTERNAL_EVENT_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.ExternalEventIdPb externalEventId_;
  /**
   * <code>.littlehorse.ExternalEventIdPb external_event_id = 1;</code>
   * @return Whether the externalEventId field is set.
   */
  @java.lang.Override
  public boolean hasExternalEventId() {
    return externalEventId_ != null;
  }
  /**
   * <code>.littlehorse.ExternalEventIdPb external_event_id = 1;</code>
   * @return The externalEventId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ExternalEventIdPb getExternalEventId() {
    return externalEventId_ == null ? io.littlehorse.sdk.common.proto.ExternalEventIdPb.getDefaultInstance() : externalEventId_;
  }
  /**
   * <code>.littlehorse.ExternalEventIdPb external_event_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ExternalEventIdPbOrBuilder getExternalEventIdOrBuilder() {
    return externalEventId_ == null ? io.littlehorse.sdk.common.proto.ExternalEventIdPb.getDefaultInstance() : externalEventId_;
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

  public static final int INTERRUPTED_THREAD_ID_FIELD_NUMBER = 3;
  private int interruptedThreadId_ = 0;
  /**
   * <code>int32 interrupted_thread_id = 3;</code>
   * @return The interruptedThreadId.
   */
  @java.lang.Override
  public int getInterruptedThreadId() {
    return interruptedThreadId_;
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
    if (externalEventId_ != null) {
      output.writeMessage(1, getExternalEventId());
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(handlerSpecName_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, handlerSpecName_);
    }
    if (interruptedThreadId_ != 0) {
      output.writeInt32(3, interruptedThreadId_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (externalEventId_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getExternalEventId());
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(handlerSpecName_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, handlerSpecName_);
    }
    if (interruptedThreadId_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(3, interruptedThreadId_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.PendingInterruptPb)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.PendingInterruptPb other = (io.littlehorse.sdk.common.proto.PendingInterruptPb) obj;

    if (hasExternalEventId() != other.hasExternalEventId()) return false;
    if (hasExternalEventId()) {
      if (!getExternalEventId()
          .equals(other.getExternalEventId())) return false;
    }
    if (!getHandlerSpecName()
        .equals(other.getHandlerSpecName())) return false;
    if (getInterruptedThreadId()
        != other.getInterruptedThreadId()) return false;
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
    if (hasExternalEventId()) {
      hash = (37 * hash) + EXTERNAL_EVENT_ID_FIELD_NUMBER;
      hash = (53 * hash) + getExternalEventId().hashCode();
    }
    hash = (37 * hash) + HANDLER_SPEC_NAME_FIELD_NUMBER;
    hash = (53 * hash) + getHandlerSpecName().hashCode();
    hash = (37 * hash) + INTERRUPTED_THREAD_ID_FIELD_NUMBER;
    hash = (53 * hash) + getInterruptedThreadId();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.PendingInterruptPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.PendingInterruptPb prototype) {
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
   * Protobuf type {@code littlehorse.PendingInterruptPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.PendingInterruptPb)
      io.littlehorse.sdk.common.proto.PendingInterruptPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_PendingInterruptPb_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_PendingInterruptPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.PendingInterruptPb.class, io.littlehorse.sdk.common.proto.PendingInterruptPb.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.PendingInterruptPb.newBuilder()
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
      externalEventId_ = null;
      if (externalEventIdBuilder_ != null) {
        externalEventIdBuilder_.dispose();
        externalEventIdBuilder_ = null;
      }
      handlerSpecName_ = "";
      interruptedThreadId_ = 0;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_PendingInterruptPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.PendingInterruptPb getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.PendingInterruptPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.PendingInterruptPb build() {
      io.littlehorse.sdk.common.proto.PendingInterruptPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.PendingInterruptPb buildPartial() {
      io.littlehorse.sdk.common.proto.PendingInterruptPb result = new io.littlehorse.sdk.common.proto.PendingInterruptPb(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.PendingInterruptPb result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.externalEventId_ = externalEventIdBuilder_ == null
            ? externalEventId_
            : externalEventIdBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.handlerSpecName_ = handlerSpecName_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.interruptedThreadId_ = interruptedThreadId_;
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
      if (other instanceof io.littlehorse.sdk.common.proto.PendingInterruptPb) {
        return mergeFrom((io.littlehorse.sdk.common.proto.PendingInterruptPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.PendingInterruptPb other) {
      if (other == io.littlehorse.sdk.common.proto.PendingInterruptPb.getDefaultInstance()) return this;
      if (other.hasExternalEventId()) {
        mergeExternalEventId(other.getExternalEventId());
      }
      if (!other.getHandlerSpecName().isEmpty()) {
        handlerSpecName_ = other.handlerSpecName_;
        bitField0_ |= 0x00000002;
        onChanged();
      }
      if (other.getInterruptedThreadId() != 0) {
        setInterruptedThreadId(other.getInterruptedThreadId());
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
                  getExternalEventIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              handlerSpecName_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 24: {
              interruptedThreadId_ = input.readInt32();
              bitField0_ |= 0x00000004;
              break;
            } // case 24
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

    private io.littlehorse.sdk.common.proto.ExternalEventIdPb externalEventId_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.ExternalEventIdPb, io.littlehorse.sdk.common.proto.ExternalEventIdPb.Builder, io.littlehorse.sdk.common.proto.ExternalEventIdPbOrBuilder> externalEventIdBuilder_;
    /**
     * <code>.littlehorse.ExternalEventIdPb external_event_id = 1;</code>
     * @return Whether the externalEventId field is set.
     */
    public boolean hasExternalEventId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.littlehorse.ExternalEventIdPb external_event_id = 1;</code>
     * @return The externalEventId.
     */
    public io.littlehorse.sdk.common.proto.ExternalEventIdPb getExternalEventId() {
      if (externalEventIdBuilder_ == null) {
        return externalEventId_ == null ? io.littlehorse.sdk.common.proto.ExternalEventIdPb.getDefaultInstance() : externalEventId_;
      } else {
        return externalEventIdBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.ExternalEventIdPb external_event_id = 1;</code>
     */
    public Builder setExternalEventId(io.littlehorse.sdk.common.proto.ExternalEventIdPb value) {
      if (externalEventIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        externalEventId_ = value;
      } else {
        externalEventIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.ExternalEventIdPb external_event_id = 1;</code>
     */
    public Builder setExternalEventId(
        io.littlehorse.sdk.common.proto.ExternalEventIdPb.Builder builderForValue) {
      if (externalEventIdBuilder_ == null) {
        externalEventId_ = builderForValue.build();
      } else {
        externalEventIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.ExternalEventIdPb external_event_id = 1;</code>
     */
    public Builder mergeExternalEventId(io.littlehorse.sdk.common.proto.ExternalEventIdPb value) {
      if (externalEventIdBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          externalEventId_ != null &&
          externalEventId_ != io.littlehorse.sdk.common.proto.ExternalEventIdPb.getDefaultInstance()) {
          getExternalEventIdBuilder().mergeFrom(value);
        } else {
          externalEventId_ = value;
        }
      } else {
        externalEventIdBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.ExternalEventIdPb external_event_id = 1;</code>
     */
    public Builder clearExternalEventId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      externalEventId_ = null;
      if (externalEventIdBuilder_ != null) {
        externalEventIdBuilder_.dispose();
        externalEventIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.ExternalEventIdPb external_event_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.ExternalEventIdPb.Builder getExternalEventIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getExternalEventIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.ExternalEventIdPb external_event_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.ExternalEventIdPbOrBuilder getExternalEventIdOrBuilder() {
      if (externalEventIdBuilder_ != null) {
        return externalEventIdBuilder_.getMessageOrBuilder();
      } else {
        return externalEventId_ == null ?
            io.littlehorse.sdk.common.proto.ExternalEventIdPb.getDefaultInstance() : externalEventId_;
      }
    }
    /**
     * <code>.littlehorse.ExternalEventIdPb external_event_id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.ExternalEventIdPb, io.littlehorse.sdk.common.proto.ExternalEventIdPb.Builder, io.littlehorse.sdk.common.proto.ExternalEventIdPbOrBuilder> 
        getExternalEventIdFieldBuilder() {
      if (externalEventIdBuilder_ == null) {
        externalEventIdBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.ExternalEventIdPb, io.littlehorse.sdk.common.proto.ExternalEventIdPb.Builder, io.littlehorse.sdk.common.proto.ExternalEventIdPbOrBuilder>(
                getExternalEventId(),
                getParentForChildren(),
                isClean());
        externalEventId_ = null;
      }
      return externalEventIdBuilder_;
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

    private int interruptedThreadId_ ;
    /**
     * <code>int32 interrupted_thread_id = 3;</code>
     * @return The interruptedThreadId.
     */
    @java.lang.Override
    public int getInterruptedThreadId() {
      return interruptedThreadId_;
    }
    /**
     * <code>int32 interrupted_thread_id = 3;</code>
     * @param value The interruptedThreadId to set.
     * @return This builder for chaining.
     */
    public Builder setInterruptedThreadId(int value) {
      
      interruptedThreadId_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>int32 interrupted_thread_id = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearInterruptedThreadId() {
      bitField0_ = (bitField0_ & ~0x00000004);
      interruptedThreadId_ = 0;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.PendingInterruptPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.PendingInterruptPb)
  private static final io.littlehorse.sdk.common.proto.PendingInterruptPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.PendingInterruptPb();
  }

  public static io.littlehorse.sdk.common.proto.PendingInterruptPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PendingInterruptPb>
      PARSER = new com.google.protobuf.AbstractParser<PendingInterruptPb>() {
    @java.lang.Override
    public PendingInterruptPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<PendingInterruptPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PendingInterruptPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.PendingInterruptPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

