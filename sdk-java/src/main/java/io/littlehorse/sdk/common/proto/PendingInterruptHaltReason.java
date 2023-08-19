// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: wf_run.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.PendingInterruptHaltReason}
 */
public final class PendingInterruptHaltReason extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.PendingInterruptHaltReason)
    PendingInterruptHaltReasonOrBuilder {
private static final long serialVersionUID = 0L;
  // Use PendingInterruptHaltReason.newBuilder() to construct.
  private PendingInterruptHaltReason(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private PendingInterruptHaltReason() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new PendingInterruptHaltReason();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.WfRunOuterClass.internal_static_littlehorse_PendingInterruptHaltReason_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.WfRunOuterClass.internal_static_littlehorse_PendingInterruptHaltReason_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.PendingInterruptHaltReason.class, io.littlehorse.sdk.common.proto.PendingInterruptHaltReason.Builder.class);
  }

  public static final int EXTERNAL_EVENT_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.ExternalEventId externalEventId_;
  /**
   * <code>.littlehorse.ExternalEventId external_event_id = 1;</code>
   * @return Whether the externalEventId field is set.
   */
  @java.lang.Override
  public boolean hasExternalEventId() {
    return externalEventId_ != null;
  }
  /**
   * <code>.littlehorse.ExternalEventId external_event_id = 1;</code>
   * @return The externalEventId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ExternalEventId getExternalEventId() {
    return externalEventId_ == null ? io.littlehorse.sdk.common.proto.ExternalEventId.getDefaultInstance() : externalEventId_;
  }
  /**
   * <code>.littlehorse.ExternalEventId external_event_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ExternalEventIdOrBuilder getExternalEventIdOrBuilder() {
    return externalEventId_ == null ? io.littlehorse.sdk.common.proto.ExternalEventId.getDefaultInstance() : externalEventId_;
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
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof io.littlehorse.sdk.common.proto.PendingInterruptHaltReason)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.PendingInterruptHaltReason other = (io.littlehorse.sdk.common.proto.PendingInterruptHaltReason) obj;

    if (hasExternalEventId() != other.hasExternalEventId()) return false;
    if (hasExternalEventId()) {
      if (!getExternalEventId()
          .equals(other.getExternalEventId())) return false;
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
    if (hasExternalEventId()) {
      hash = (37 * hash) + EXTERNAL_EVENT_ID_FIELD_NUMBER;
      hash = (53 * hash) + getExternalEventId().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.PendingInterruptHaltReason parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptHaltReason parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptHaltReason parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptHaltReason parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptHaltReason parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptHaltReason parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptHaltReason parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptHaltReason parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptHaltReason parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptHaltReason parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptHaltReason parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.PendingInterruptHaltReason parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.PendingInterruptHaltReason prototype) {
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
   * Protobuf type {@code littlehorse.PendingInterruptHaltReason}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.PendingInterruptHaltReason)
      io.littlehorse.sdk.common.proto.PendingInterruptHaltReasonOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.WfRunOuterClass.internal_static_littlehorse_PendingInterruptHaltReason_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.WfRunOuterClass.internal_static_littlehorse_PendingInterruptHaltReason_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.PendingInterruptHaltReason.class, io.littlehorse.sdk.common.proto.PendingInterruptHaltReason.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.PendingInterruptHaltReason.newBuilder()
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
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.WfRunOuterClass.internal_static_littlehorse_PendingInterruptHaltReason_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.PendingInterruptHaltReason getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.PendingInterruptHaltReason.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.PendingInterruptHaltReason build() {
      io.littlehorse.sdk.common.proto.PendingInterruptHaltReason result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.PendingInterruptHaltReason buildPartial() {
      io.littlehorse.sdk.common.proto.PendingInterruptHaltReason result = new io.littlehorse.sdk.common.proto.PendingInterruptHaltReason(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.PendingInterruptHaltReason result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.externalEventId_ = externalEventIdBuilder_ == null
            ? externalEventId_
            : externalEventIdBuilder_.build();
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
      if (other instanceof io.littlehorse.sdk.common.proto.PendingInterruptHaltReason) {
        return mergeFrom((io.littlehorse.sdk.common.proto.PendingInterruptHaltReason)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.PendingInterruptHaltReason other) {
      if (other == io.littlehorse.sdk.common.proto.PendingInterruptHaltReason.getDefaultInstance()) return this;
      if (other.hasExternalEventId()) {
        mergeExternalEventId(other.getExternalEventId());
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

    private io.littlehorse.sdk.common.proto.ExternalEventId externalEventId_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.ExternalEventId, io.littlehorse.sdk.common.proto.ExternalEventId.Builder, io.littlehorse.sdk.common.proto.ExternalEventIdOrBuilder> externalEventIdBuilder_;
    /**
     * <code>.littlehorse.ExternalEventId external_event_id = 1;</code>
     * @return Whether the externalEventId field is set.
     */
    public boolean hasExternalEventId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.littlehorse.ExternalEventId external_event_id = 1;</code>
     * @return The externalEventId.
     */
    public io.littlehorse.sdk.common.proto.ExternalEventId getExternalEventId() {
      if (externalEventIdBuilder_ == null) {
        return externalEventId_ == null ? io.littlehorse.sdk.common.proto.ExternalEventId.getDefaultInstance() : externalEventId_;
      } else {
        return externalEventIdBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.ExternalEventId external_event_id = 1;</code>
     */
    public Builder setExternalEventId(io.littlehorse.sdk.common.proto.ExternalEventId value) {
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
     * <code>.littlehorse.ExternalEventId external_event_id = 1;</code>
     */
    public Builder setExternalEventId(
        io.littlehorse.sdk.common.proto.ExternalEventId.Builder builderForValue) {
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
     * <code>.littlehorse.ExternalEventId external_event_id = 1;</code>
     */
    public Builder mergeExternalEventId(io.littlehorse.sdk.common.proto.ExternalEventId value) {
      if (externalEventIdBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          externalEventId_ != null &&
          externalEventId_ != io.littlehorse.sdk.common.proto.ExternalEventId.getDefaultInstance()) {
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
     * <code>.littlehorse.ExternalEventId external_event_id = 1;</code>
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
     * <code>.littlehorse.ExternalEventId external_event_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.ExternalEventId.Builder getExternalEventIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getExternalEventIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.ExternalEventId external_event_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.ExternalEventIdOrBuilder getExternalEventIdOrBuilder() {
      if (externalEventIdBuilder_ != null) {
        return externalEventIdBuilder_.getMessageOrBuilder();
      } else {
        return externalEventId_ == null ?
            io.littlehorse.sdk.common.proto.ExternalEventId.getDefaultInstance() : externalEventId_;
      }
    }
    /**
     * <code>.littlehorse.ExternalEventId external_event_id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.ExternalEventId, io.littlehorse.sdk.common.proto.ExternalEventId.Builder, io.littlehorse.sdk.common.proto.ExternalEventIdOrBuilder> 
        getExternalEventIdFieldBuilder() {
      if (externalEventIdBuilder_ == null) {
        externalEventIdBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.ExternalEventId, io.littlehorse.sdk.common.proto.ExternalEventId.Builder, io.littlehorse.sdk.common.proto.ExternalEventIdOrBuilder>(
                getExternalEventId(),
                getParentForChildren(),
                isClean());
        externalEventId_ = null;
      }
      return externalEventIdBuilder_;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.PendingInterruptHaltReason)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.PendingInterruptHaltReason)
  private static final io.littlehorse.sdk.common.proto.PendingInterruptHaltReason DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.PendingInterruptHaltReason();
  }

  public static io.littlehorse.sdk.common.proto.PendingInterruptHaltReason getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PendingInterruptHaltReason>
      PARSER = new com.google.protobuf.AbstractParser<PendingInterruptHaltReason>() {
    @java.lang.Override
    public PendingInterruptHaltReason parsePartialFrom(
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

  public static com.google.protobuf.Parser<PendingInterruptHaltReason> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PendingInterruptHaltReason> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.PendingInterruptHaltReason getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

