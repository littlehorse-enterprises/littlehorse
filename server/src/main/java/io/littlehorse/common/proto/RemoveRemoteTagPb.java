// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: command.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.common.proto;

/**
 * Protobuf type {@code littlehorse.RemoveRemoteTagPb}
 */
public final class RemoveRemoteTagPb extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.RemoveRemoteTagPb)
    RemoveRemoteTagPbOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 29,
      /* patch= */ 3,
      /* suffix= */ "",
      RemoveRemoteTagPb.class.getName());
  }
  // Use RemoveRemoteTagPb.newBuilder() to construct.
  private RemoveRemoteTagPb(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private RemoveRemoteTagPb() {
    storeKey_ = "";
    partitionKey_ = "";
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_RemoveRemoteTagPb_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_RemoveRemoteTagPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.RemoveRemoteTagPb.class, io.littlehorse.common.proto.RemoveRemoteTagPb.Builder.class);
  }

  public static final int STORE_KEY_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object storeKey_ = "";
  /**
   * <code>string store_key = 1;</code>
   * @return The storeKey.
   */
  @java.lang.Override
  public java.lang.String getStoreKey() {
    java.lang.Object ref = storeKey_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      storeKey_ = s;
      return s;
    }
  }
  /**
   * <code>string store_key = 1;</code>
   * @return The bytes for storeKey.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getStoreKeyBytes() {
    java.lang.Object ref = storeKey_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      storeKey_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int PARTITION_KEY_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private volatile java.lang.Object partitionKey_ = "";
  /**
   * <code>string partition_key = 2;</code>
   * @return The partitionKey.
   */
  @java.lang.Override
  public java.lang.String getPartitionKey() {
    java.lang.Object ref = partitionKey_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      partitionKey_ = s;
      return s;
    }
  }
  /**
   * <code>string partition_key = 2;</code>
   * @return The bytes for partitionKey.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getPartitionKeyBytes() {
    java.lang.Object ref = partitionKey_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      partitionKey_ = b;
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
    if (!com.google.protobuf.GeneratedMessage.isStringEmpty(storeKey_)) {
      com.google.protobuf.GeneratedMessage.writeString(output, 1, storeKey_);
    }
    if (!com.google.protobuf.GeneratedMessage.isStringEmpty(partitionKey_)) {
      com.google.protobuf.GeneratedMessage.writeString(output, 2, partitionKey_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessage.isStringEmpty(storeKey_)) {
      size += com.google.protobuf.GeneratedMessage.computeStringSize(1, storeKey_);
    }
    if (!com.google.protobuf.GeneratedMessage.isStringEmpty(partitionKey_)) {
      size += com.google.protobuf.GeneratedMessage.computeStringSize(2, partitionKey_);
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
    if (!(obj instanceof io.littlehorse.common.proto.RemoveRemoteTagPb)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.RemoveRemoteTagPb other = (io.littlehorse.common.proto.RemoveRemoteTagPb) obj;

    if (!getStoreKey()
        .equals(other.getStoreKey())) return false;
    if (!getPartitionKey()
        .equals(other.getPartitionKey())) return false;
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
    hash = (37 * hash) + STORE_KEY_FIELD_NUMBER;
    hash = (53 * hash) + getStoreKey().hashCode();
    hash = (37 * hash) + PARTITION_KEY_FIELD_NUMBER;
    hash = (53 * hash) + getPartitionKey().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.RemoveRemoteTagPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.RemoveRemoteTagPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.RemoveRemoteTagPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.RemoveRemoteTagPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.RemoveRemoteTagPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.RemoveRemoteTagPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.RemoveRemoteTagPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.RemoveRemoteTagPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.RemoveRemoteTagPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.RemoveRemoteTagPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.RemoveRemoteTagPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.RemoveRemoteTagPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.RemoveRemoteTagPb prototype) {
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
   * Protobuf type {@code littlehorse.RemoveRemoteTagPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.RemoveRemoteTagPb)
      io.littlehorse.common.proto.RemoveRemoteTagPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_RemoveRemoteTagPb_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_RemoveRemoteTagPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.RemoveRemoteTagPb.class, io.littlehorse.common.proto.RemoveRemoteTagPb.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.RemoveRemoteTagPb.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      storeKey_ = "";
      partitionKey_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_RemoveRemoteTagPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.RemoveRemoteTagPb getDefaultInstanceForType() {
      return io.littlehorse.common.proto.RemoveRemoteTagPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.RemoveRemoteTagPb build() {
      io.littlehorse.common.proto.RemoveRemoteTagPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.RemoveRemoteTagPb buildPartial() {
      io.littlehorse.common.proto.RemoveRemoteTagPb result = new io.littlehorse.common.proto.RemoveRemoteTagPb(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.common.proto.RemoveRemoteTagPb result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.storeKey_ = storeKey_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.partitionKey_ = partitionKey_;
      }
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.common.proto.RemoveRemoteTagPb) {
        return mergeFrom((io.littlehorse.common.proto.RemoveRemoteTagPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.RemoveRemoteTagPb other) {
      if (other == io.littlehorse.common.proto.RemoveRemoteTagPb.getDefaultInstance()) return this;
      if (!other.getStoreKey().isEmpty()) {
        storeKey_ = other.storeKey_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (!other.getPartitionKey().isEmpty()) {
        partitionKey_ = other.partitionKey_;
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
              storeKey_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              partitionKey_ = input.readStringRequireUtf8();
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

    private java.lang.Object storeKey_ = "";
    /**
     * <code>string store_key = 1;</code>
     * @return The storeKey.
     */
    public java.lang.String getStoreKey() {
      java.lang.Object ref = storeKey_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        storeKey_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string store_key = 1;</code>
     * @return The bytes for storeKey.
     */
    public com.google.protobuf.ByteString
        getStoreKeyBytes() {
      java.lang.Object ref = storeKey_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        storeKey_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string store_key = 1;</code>
     * @param value The storeKey to set.
     * @return This builder for chaining.
     */
    public Builder setStoreKey(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      storeKey_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>string store_key = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearStoreKey() {
      storeKey_ = getDefaultInstance().getStoreKey();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>string store_key = 1;</code>
     * @param value The bytes for storeKey to set.
     * @return This builder for chaining.
     */
    public Builder setStoreKeyBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      storeKey_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private java.lang.Object partitionKey_ = "";
    /**
     * <code>string partition_key = 2;</code>
     * @return The partitionKey.
     */
    public java.lang.String getPartitionKey() {
      java.lang.Object ref = partitionKey_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        partitionKey_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string partition_key = 2;</code>
     * @return The bytes for partitionKey.
     */
    public com.google.protobuf.ByteString
        getPartitionKeyBytes() {
      java.lang.Object ref = partitionKey_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        partitionKey_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string partition_key = 2;</code>
     * @param value The partitionKey to set.
     * @return This builder for chaining.
     */
    public Builder setPartitionKey(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      partitionKey_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>string partition_key = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearPartitionKey() {
      partitionKey_ = getDefaultInstance().getPartitionKey();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /**
     * <code>string partition_key = 2;</code>
     * @param value The bytes for partitionKey to set.
     * @return This builder for chaining.
     */
    public Builder setPartitionKeyBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      partitionKey_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.RemoveRemoteTagPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.RemoveRemoteTagPb)
  private static final io.littlehorse.common.proto.RemoveRemoteTagPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.RemoveRemoteTagPb();
  }

  public static io.littlehorse.common.proto.RemoveRemoteTagPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<RemoveRemoteTagPb>
      PARSER = new com.google.protobuf.AbstractParser<RemoveRemoteTagPb>() {
    @java.lang.Override
    public RemoveRemoteTagPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<RemoveRemoteTagPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<RemoveRemoteTagPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.RemoveRemoteTagPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

