// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: wf_spec.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Defines an Interrupt for a ThreadSpec. An Interrupt means that when an ExternalEvent
 * of a certain type is registered to the WfRun, then the affected ThreadRun is HALTED
 * and a handler ThreadRun is run as an interrupt handler. The interrupted ThreadRun
 * is resumed once the interrupt handler completes.
 * </pre>
 *
 * Protobuf type {@code littlehorse.InterruptDef}
 */
public final class InterruptDef extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.InterruptDef)
    InterruptDefOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 29,
      /* patch= */ 3,
      /* suffix= */ "",
      InterruptDef.class.getName());
  }
  // Use InterruptDef.newBuilder() to construct.
  private InterruptDef(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private InterruptDef() {
    handlerSpecName_ = "";
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_InterruptDef_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_InterruptDef_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.InterruptDef.class, io.littlehorse.sdk.common.proto.InterruptDef.Builder.class);
  }

  private int bitField0_;
  public static final int EXTERNAL_EVENT_DEF_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.ExternalEventDefId externalEventDefId_;
  /**
   * <pre>
   * The ID of the ExternalEventDef which triggers an Interrupt for this ThreadSpec.
   * Note that as of 0.9.0, you cannot use an ExternalEventDefId for both an InterruptDef
   * and an ExternalEventNode in the same WfSpec.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
   * @return Whether the externalEventDefId field is set.
   */
  @java.lang.Override
  public boolean hasExternalEventDefId() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * The ID of the ExternalEventDef which triggers an Interrupt for this ThreadSpec.
   * Note that as of 0.9.0, you cannot use an ExternalEventDefId for both an InterruptDef
   * and an ExternalEventNode in the same WfSpec.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
   * @return The externalEventDefId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ExternalEventDefId getExternalEventDefId() {
    return externalEventDefId_ == null ? io.littlehorse.sdk.common.proto.ExternalEventDefId.getDefaultInstance() : externalEventDefId_;
  }
  /**
   * <pre>
   * The ID of the ExternalEventDef which triggers an Interrupt for this ThreadSpec.
   * Note that as of 0.9.0, you cannot use an ExternalEventDefId for both an InterruptDef
   * and an ExternalEventNode in the same WfSpec.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder getExternalEventDefIdOrBuilder() {
    return externalEventDefId_ == null ? io.littlehorse.sdk.common.proto.ExternalEventDefId.getDefaultInstance() : externalEventDefId_;
  }

  public static final int HANDLER_SPEC_NAME_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private volatile java.lang.Object handlerSpecName_ = "";
  /**
   * <pre>
   * The name of the ThreadSpec that we run as the interrupt handler.
   * </pre>
   *
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
   * <pre>
   * The name of the ThreadSpec that we run as the interrupt handler.
   * </pre>
   *
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
      output.writeMessage(1, getExternalEventDefId());
    }
    if (!com.google.protobuf.GeneratedMessage.isStringEmpty(handlerSpecName_)) {
      com.google.protobuf.GeneratedMessage.writeString(output, 2, handlerSpecName_);
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
        .computeMessageSize(1, getExternalEventDefId());
    }
    if (!com.google.protobuf.GeneratedMessage.isStringEmpty(handlerSpecName_)) {
      size += com.google.protobuf.GeneratedMessage.computeStringSize(2, handlerSpecName_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.InterruptDef)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.InterruptDef other = (io.littlehorse.sdk.common.proto.InterruptDef) obj;

    if (hasExternalEventDefId() != other.hasExternalEventDefId()) return false;
    if (hasExternalEventDefId()) {
      if (!getExternalEventDefId()
          .equals(other.getExternalEventDefId())) return false;
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
    if (hasExternalEventDefId()) {
      hash = (37 * hash) + EXTERNAL_EVENT_DEF_ID_FIELD_NUMBER;
      hash = (53 * hash) + getExternalEventDefId().hashCode();
    }
    hash = (37 * hash) + HANDLER_SPEC_NAME_FIELD_NUMBER;
    hash = (53 * hash) + getHandlerSpecName().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.InterruptDef parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.InterruptDef parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.InterruptDef parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.InterruptDef parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.InterruptDef parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.InterruptDef parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.InterruptDef parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.InterruptDef parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.InterruptDef parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.InterruptDef parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.InterruptDef parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.InterruptDef parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.InterruptDef prototype) {
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
   * Defines an Interrupt for a ThreadSpec. An Interrupt means that when an ExternalEvent
   * of a certain type is registered to the WfRun, then the affected ThreadRun is HALTED
   * and a handler ThreadRun is run as an interrupt handler. The interrupted ThreadRun
   * is resumed once the interrupt handler completes.
   * </pre>
   *
   * Protobuf type {@code littlehorse.InterruptDef}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.InterruptDef)
      io.littlehorse.sdk.common.proto.InterruptDefOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_InterruptDef_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_InterruptDef_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.InterruptDef.class, io.littlehorse.sdk.common.proto.InterruptDef.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.InterruptDef.newBuilder()
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
        getExternalEventDefIdFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      externalEventDefId_ = null;
      if (externalEventDefIdBuilder_ != null) {
        externalEventDefIdBuilder_.dispose();
        externalEventDefIdBuilder_ = null;
      }
      handlerSpecName_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_InterruptDef_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.InterruptDef getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.InterruptDef.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.InterruptDef build() {
      io.littlehorse.sdk.common.proto.InterruptDef result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.InterruptDef buildPartial() {
      io.littlehorse.sdk.common.proto.InterruptDef result = new io.littlehorse.sdk.common.proto.InterruptDef(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.InterruptDef result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.externalEventDefId_ = externalEventDefIdBuilder_ == null
            ? externalEventDefId_
            : externalEventDefIdBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.handlerSpecName_ = handlerSpecName_;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.sdk.common.proto.InterruptDef) {
        return mergeFrom((io.littlehorse.sdk.common.proto.InterruptDef)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.InterruptDef other) {
      if (other == io.littlehorse.sdk.common.proto.InterruptDef.getDefaultInstance()) return this;
      if (other.hasExternalEventDefId()) {
        mergeExternalEventDefId(other.getExternalEventDefId());
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
              input.readMessage(
                  getExternalEventDefIdFieldBuilder().getBuilder(),
                  extensionRegistry);
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

    private io.littlehorse.sdk.common.proto.ExternalEventDefId externalEventDefId_;
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.ExternalEventDefId, io.littlehorse.sdk.common.proto.ExternalEventDefId.Builder, io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder> externalEventDefIdBuilder_;
    /**
     * <pre>
     * The ID of the ExternalEventDef which triggers an Interrupt for this ThreadSpec.
     * Note that as of 0.9.0, you cannot use an ExternalEventDefId for both an InterruptDef
     * and an ExternalEventNode in the same WfSpec.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
     * @return Whether the externalEventDefId field is set.
     */
    public boolean hasExternalEventDefId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef which triggers an Interrupt for this ThreadSpec.
     * Note that as of 0.9.0, you cannot use an ExternalEventDefId for both an InterruptDef
     * and an ExternalEventNode in the same WfSpec.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
     * @return The externalEventDefId.
     */
    public io.littlehorse.sdk.common.proto.ExternalEventDefId getExternalEventDefId() {
      if (externalEventDefIdBuilder_ == null) {
        return externalEventDefId_ == null ? io.littlehorse.sdk.common.proto.ExternalEventDefId.getDefaultInstance() : externalEventDefId_;
      } else {
        return externalEventDefIdBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef which triggers an Interrupt for this ThreadSpec.
     * Note that as of 0.9.0, you cannot use an ExternalEventDefId for both an InterruptDef
     * and an ExternalEventNode in the same WfSpec.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
     */
    public Builder setExternalEventDefId(io.littlehorse.sdk.common.proto.ExternalEventDefId value) {
      if (externalEventDefIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        externalEventDefId_ = value;
      } else {
        externalEventDefIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef which triggers an Interrupt for this ThreadSpec.
     * Note that as of 0.9.0, you cannot use an ExternalEventDefId for both an InterruptDef
     * and an ExternalEventNode in the same WfSpec.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
     */
    public Builder setExternalEventDefId(
        io.littlehorse.sdk.common.proto.ExternalEventDefId.Builder builderForValue) {
      if (externalEventDefIdBuilder_ == null) {
        externalEventDefId_ = builderForValue.build();
      } else {
        externalEventDefIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef which triggers an Interrupt for this ThreadSpec.
     * Note that as of 0.9.0, you cannot use an ExternalEventDefId for both an InterruptDef
     * and an ExternalEventNode in the same WfSpec.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
     */
    public Builder mergeExternalEventDefId(io.littlehorse.sdk.common.proto.ExternalEventDefId value) {
      if (externalEventDefIdBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          externalEventDefId_ != null &&
          externalEventDefId_ != io.littlehorse.sdk.common.proto.ExternalEventDefId.getDefaultInstance()) {
          getExternalEventDefIdBuilder().mergeFrom(value);
        } else {
          externalEventDefId_ = value;
        }
      } else {
        externalEventDefIdBuilder_.mergeFrom(value);
      }
      if (externalEventDefId_ != null) {
        bitField0_ |= 0x00000001;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef which triggers an Interrupt for this ThreadSpec.
     * Note that as of 0.9.0, you cannot use an ExternalEventDefId for both an InterruptDef
     * and an ExternalEventNode in the same WfSpec.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
     */
    public Builder clearExternalEventDefId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      externalEventDefId_ = null;
      if (externalEventDefIdBuilder_ != null) {
        externalEventDefIdBuilder_.dispose();
        externalEventDefIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef which triggers an Interrupt for this ThreadSpec.
     * Note that as of 0.9.0, you cannot use an ExternalEventDefId for both an InterruptDef
     * and an ExternalEventNode in the same WfSpec.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.ExternalEventDefId.Builder getExternalEventDefIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getExternalEventDefIdFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef which triggers an Interrupt for this ThreadSpec.
     * Note that as of 0.9.0, you cannot use an ExternalEventDefId for both an InterruptDef
     * and an ExternalEventNode in the same WfSpec.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder getExternalEventDefIdOrBuilder() {
      if (externalEventDefIdBuilder_ != null) {
        return externalEventDefIdBuilder_.getMessageOrBuilder();
      } else {
        return externalEventDefId_ == null ?
            io.littlehorse.sdk.common.proto.ExternalEventDefId.getDefaultInstance() : externalEventDefId_;
      }
    }
    /**
     * <pre>
     * The ID of the ExternalEventDef which triggers an Interrupt for this ThreadSpec.
     * Note that as of 0.9.0, you cannot use an ExternalEventDefId for both an InterruptDef
     * and an ExternalEventNode in the same WfSpec.
     * </pre>
     *
     * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.ExternalEventDefId, io.littlehorse.sdk.common.proto.ExternalEventDefId.Builder, io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder> 
        getExternalEventDefIdFieldBuilder() {
      if (externalEventDefIdBuilder_ == null) {
        externalEventDefIdBuilder_ = new com.google.protobuf.SingleFieldBuilder<
            io.littlehorse.sdk.common.proto.ExternalEventDefId, io.littlehorse.sdk.common.proto.ExternalEventDefId.Builder, io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder>(
                getExternalEventDefId(),
                getParentForChildren(),
                isClean());
        externalEventDefId_ = null;
      }
      return externalEventDefIdBuilder_;
    }

    private java.lang.Object handlerSpecName_ = "";
    /**
     * <pre>
     * The name of the ThreadSpec that we run as the interrupt handler.
     * </pre>
     *
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
     * <pre>
     * The name of the ThreadSpec that we run as the interrupt handler.
     * </pre>
     *
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
     * <pre>
     * The name of the ThreadSpec that we run as the interrupt handler.
     * </pre>
     *
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
     * <pre>
     * The name of the ThreadSpec that we run as the interrupt handler.
     * </pre>
     *
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
     * <pre>
     * The name of the ThreadSpec that we run as the interrupt handler.
     * </pre>
     *
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

    // @@protoc_insertion_point(builder_scope:littlehorse.InterruptDef)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.InterruptDef)
  private static final io.littlehorse.sdk.common.proto.InterruptDef DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.InterruptDef();
  }

  public static io.littlehorse.sdk.common.proto.InterruptDef getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<InterruptDef>
      PARSER = new com.google.protobuf.AbstractParser<InterruptDef>() {
    @java.lang.Override
    public InterruptDef parsePartialFrom(
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

  public static com.google.protobuf.Parser<InterruptDef> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<InterruptDef> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.InterruptDef getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

