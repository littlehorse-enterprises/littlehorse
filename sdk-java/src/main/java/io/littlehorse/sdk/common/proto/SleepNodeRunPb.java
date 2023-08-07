// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.SleepNodeRunPb}
 */
public final class SleepNodeRunPb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.SleepNodeRunPb)
    SleepNodeRunPbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use SleepNodeRunPb.newBuilder() to construct.
  private SleepNodeRunPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private SleepNodeRunPb() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new SleepNodeRunPb();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SleepNodeRunPb_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SleepNodeRunPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.SleepNodeRunPb.class, io.littlehorse.sdk.common.proto.SleepNodeRunPb.Builder.class);
  }

  public static final int MATURATION_TIME_FIELD_NUMBER = 1;
  private com.google.protobuf.Timestamp maturationTime_;
  /**
   * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
   * @return Whether the maturationTime field is set.
   */
  @java.lang.Override
  public boolean hasMaturationTime() {
    return maturationTime_ != null;
  }
  /**
   * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
   * @return The maturationTime.
   */
  @java.lang.Override
  public com.google.protobuf.Timestamp getMaturationTime() {
    return maturationTime_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : maturationTime_;
  }
  /**
   * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
   */
  @java.lang.Override
  public com.google.protobuf.TimestampOrBuilder getMaturationTimeOrBuilder() {
    return maturationTime_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : maturationTime_;
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
    if (maturationTime_ != null) {
      output.writeMessage(1, getMaturationTime());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (maturationTime_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getMaturationTime());
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.SleepNodeRunPb)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.SleepNodeRunPb other = (io.littlehorse.sdk.common.proto.SleepNodeRunPb) obj;

    if (hasMaturationTime() != other.hasMaturationTime()) return false;
    if (hasMaturationTime()) {
      if (!getMaturationTime()
          .equals(other.getMaturationTime())) return false;
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
    if (hasMaturationTime()) {
      hash = (37 * hash) + MATURATION_TIME_FIELD_NUMBER;
      hash = (53 * hash) + getMaturationTime().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.SleepNodeRunPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SleepNodeRunPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SleepNodeRunPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SleepNodeRunPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SleepNodeRunPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SleepNodeRunPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SleepNodeRunPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.SleepNodeRunPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.SleepNodeRunPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.SleepNodeRunPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SleepNodeRunPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.SleepNodeRunPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.SleepNodeRunPb prototype) {
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
   * Protobuf type {@code littlehorse.SleepNodeRunPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.SleepNodeRunPb)
      io.littlehorse.sdk.common.proto.SleepNodeRunPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SleepNodeRunPb_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SleepNodeRunPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.SleepNodeRunPb.class, io.littlehorse.sdk.common.proto.SleepNodeRunPb.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.SleepNodeRunPb.newBuilder()
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
      maturationTime_ = null;
      if (maturationTimeBuilder_ != null) {
        maturationTimeBuilder_.dispose();
        maturationTimeBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SleepNodeRunPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SleepNodeRunPb getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.SleepNodeRunPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SleepNodeRunPb build() {
      io.littlehorse.sdk.common.proto.SleepNodeRunPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SleepNodeRunPb buildPartial() {
      io.littlehorse.sdk.common.proto.SleepNodeRunPb result = new io.littlehorse.sdk.common.proto.SleepNodeRunPb(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.SleepNodeRunPb result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.maturationTime_ = maturationTimeBuilder_ == null
            ? maturationTime_
            : maturationTimeBuilder_.build();
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
      if (other instanceof io.littlehorse.sdk.common.proto.SleepNodeRunPb) {
        return mergeFrom((io.littlehorse.sdk.common.proto.SleepNodeRunPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.SleepNodeRunPb other) {
      if (other == io.littlehorse.sdk.common.proto.SleepNodeRunPb.getDefaultInstance()) return this;
      if (other.hasMaturationTime()) {
        mergeMaturationTime(other.getMaturationTime());
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
                  getMaturationTimeFieldBuilder().getBuilder(),
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

    private com.google.protobuf.Timestamp maturationTime_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder> maturationTimeBuilder_;
    /**
     * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
     * @return Whether the maturationTime field is set.
     */
    public boolean hasMaturationTime() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
     * @return The maturationTime.
     */
    public com.google.protobuf.Timestamp getMaturationTime() {
      if (maturationTimeBuilder_ == null) {
        return maturationTime_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : maturationTime_;
      } else {
        return maturationTimeBuilder_.getMessage();
      }
    }
    /**
     * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
     */
    public Builder setMaturationTime(com.google.protobuf.Timestamp value) {
      if (maturationTimeBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        maturationTime_ = value;
      } else {
        maturationTimeBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
     */
    public Builder setMaturationTime(
        com.google.protobuf.Timestamp.Builder builderForValue) {
      if (maturationTimeBuilder_ == null) {
        maturationTime_ = builderForValue.build();
      } else {
        maturationTimeBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
     */
    public Builder mergeMaturationTime(com.google.protobuf.Timestamp value) {
      if (maturationTimeBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          maturationTime_ != null &&
          maturationTime_ != com.google.protobuf.Timestamp.getDefaultInstance()) {
          getMaturationTimeBuilder().mergeFrom(value);
        } else {
          maturationTime_ = value;
        }
      } else {
        maturationTimeBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
     */
    public Builder clearMaturationTime() {
      bitField0_ = (bitField0_ & ~0x00000001);
      maturationTime_ = null;
      if (maturationTimeBuilder_ != null) {
        maturationTimeBuilder_.dispose();
        maturationTimeBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
     */
    public com.google.protobuf.Timestamp.Builder getMaturationTimeBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getMaturationTimeFieldBuilder().getBuilder();
    }
    /**
     * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
     */
    public com.google.protobuf.TimestampOrBuilder getMaturationTimeOrBuilder() {
      if (maturationTimeBuilder_ != null) {
        return maturationTimeBuilder_.getMessageOrBuilder();
      } else {
        return maturationTime_ == null ?
            com.google.protobuf.Timestamp.getDefaultInstance() : maturationTime_;
      }
    }
    /**
     * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder> 
        getMaturationTimeFieldBuilder() {
      if (maturationTimeBuilder_ == null) {
        maturationTimeBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder>(
                getMaturationTime(),
                getParentForChildren(),
                isClean());
        maturationTime_ = null;
      }
      return maturationTimeBuilder_;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.SleepNodeRunPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.SleepNodeRunPb)
  private static final io.littlehorse.sdk.common.proto.SleepNodeRunPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.SleepNodeRunPb();
  }

  public static io.littlehorse.sdk.common.proto.SleepNodeRunPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SleepNodeRunPb>
      PARSER = new com.google.protobuf.AbstractParser<SleepNodeRunPb>() {
    @java.lang.Override
    public SleepNodeRunPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<SleepNodeRunPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SleepNodeRunPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.SleepNodeRunPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

