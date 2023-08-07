// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: internal_server.proto

package io.littlehorse.common.proto;

/**
 * <pre>
 * used for paginated responses
 * </pre>
 *
 * Protobuf type {@code littlehorse.PartitionBookmarkPb}
 */
public final class PartitionBookmarkPb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.PartitionBookmarkPb)
    PartitionBookmarkPbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use PartitionBookmarkPb.newBuilder() to construct.
  private PartitionBookmarkPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private PartitionBookmarkPb() {
    lastKey_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new PartitionBookmarkPb();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_PartitionBookmarkPb_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_PartitionBookmarkPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.PartitionBookmarkPb.class, io.littlehorse.common.proto.PartitionBookmarkPb.Builder.class);
  }

  private int bitField0_;
  public static final int PARTTION_FIELD_NUMBER = 1;
  private int parttion_ = 0;
  /**
   * <code>int32 parttion = 1;</code>
   * @return The parttion.
   */
  @java.lang.Override
  public int getParttion() {
    return parttion_;
  }

  public static final int LAST_KEY_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private volatile java.lang.Object lastKey_ = "";
  /**
   * <code>optional string last_key = 2;</code>
   * @return Whether the lastKey field is set.
   */
  @java.lang.Override
  public boolean hasLastKey() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional string last_key = 2;</code>
   * @return The lastKey.
   */
  @java.lang.Override
  public java.lang.String getLastKey() {
    java.lang.Object ref = lastKey_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      lastKey_ = s;
      return s;
    }
  }
  /**
   * <code>optional string last_key = 2;</code>
   * @return The bytes for lastKey.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getLastKeyBytes() {
    java.lang.Object ref = lastKey_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      lastKey_ = b;
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
    if (parttion_ != 0) {
      output.writeInt32(1, parttion_);
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, lastKey_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (parttion_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(1, parttion_);
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, lastKey_);
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
    if (!(obj instanceof io.littlehorse.common.proto.PartitionBookmarkPb)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.PartitionBookmarkPb other = (io.littlehorse.common.proto.PartitionBookmarkPb) obj;

    if (getParttion()
        != other.getParttion()) return false;
    if (hasLastKey() != other.hasLastKey()) return false;
    if (hasLastKey()) {
      if (!getLastKey()
          .equals(other.getLastKey())) return false;
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
    hash = (37 * hash) + PARTTION_FIELD_NUMBER;
    hash = (53 * hash) + getParttion();
    if (hasLastKey()) {
      hash = (37 * hash) + LAST_KEY_FIELD_NUMBER;
      hash = (53 * hash) + getLastKey().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.PartitionBookmarkPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.PartitionBookmarkPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.PartitionBookmarkPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.PartitionBookmarkPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.PartitionBookmarkPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.PartitionBookmarkPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.PartitionBookmarkPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.PartitionBookmarkPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.PartitionBookmarkPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.PartitionBookmarkPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.PartitionBookmarkPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.PartitionBookmarkPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.PartitionBookmarkPb prototype) {
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
   * used for paginated responses
   * </pre>
   *
   * Protobuf type {@code littlehorse.PartitionBookmarkPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.PartitionBookmarkPb)
      io.littlehorse.common.proto.PartitionBookmarkPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_PartitionBookmarkPb_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_PartitionBookmarkPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.PartitionBookmarkPb.class, io.littlehorse.common.proto.PartitionBookmarkPb.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.PartitionBookmarkPb.newBuilder()
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
      parttion_ = 0;
      lastKey_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_PartitionBookmarkPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.PartitionBookmarkPb getDefaultInstanceForType() {
      return io.littlehorse.common.proto.PartitionBookmarkPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.PartitionBookmarkPb build() {
      io.littlehorse.common.proto.PartitionBookmarkPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.PartitionBookmarkPb buildPartial() {
      io.littlehorse.common.proto.PartitionBookmarkPb result = new io.littlehorse.common.proto.PartitionBookmarkPb(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.common.proto.PartitionBookmarkPb result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.parttion_ = parttion_;
      }
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.lastKey_ = lastKey_;
        to_bitField0_ |= 0x00000001;
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
      if (other instanceof io.littlehorse.common.proto.PartitionBookmarkPb) {
        return mergeFrom((io.littlehorse.common.proto.PartitionBookmarkPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.PartitionBookmarkPb other) {
      if (other == io.littlehorse.common.proto.PartitionBookmarkPb.getDefaultInstance()) return this;
      if (other.getParttion() != 0) {
        setParttion(other.getParttion());
      }
      if (other.hasLastKey()) {
        lastKey_ = other.lastKey_;
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
            case 8: {
              parttion_ = input.readInt32();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 18: {
              lastKey_ = input.readStringRequireUtf8();
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

    private int parttion_ ;
    /**
     * <code>int32 parttion = 1;</code>
     * @return The parttion.
     */
    @java.lang.Override
    public int getParttion() {
      return parttion_;
    }
    /**
     * <code>int32 parttion = 1;</code>
     * @param value The parttion to set.
     * @return This builder for chaining.
     */
    public Builder setParttion(int value) {

      parttion_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>int32 parttion = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearParttion() {
      bitField0_ = (bitField0_ & ~0x00000001);
      parttion_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object lastKey_ = "";
    /**
     * <code>optional string last_key = 2;</code>
     * @return Whether the lastKey field is set.
     */
    public boolean hasLastKey() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>optional string last_key = 2;</code>
     * @return The lastKey.
     */
    public java.lang.String getLastKey() {
      java.lang.Object ref = lastKey_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        lastKey_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>optional string last_key = 2;</code>
     * @return The bytes for lastKey.
     */
    public com.google.protobuf.ByteString
        getLastKeyBytes() {
      java.lang.Object ref = lastKey_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        lastKey_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>optional string last_key = 2;</code>
     * @param value The lastKey to set.
     * @return This builder for chaining.
     */
    public Builder setLastKey(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      lastKey_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>optional string last_key = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearLastKey() {
      lastKey_ = getDefaultInstance().getLastKey();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /**
     * <code>optional string last_key = 2;</code>
     * @param value The bytes for lastKey to set.
     * @return This builder for chaining.
     */
    public Builder setLastKeyBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      lastKey_ = value;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.PartitionBookmarkPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.PartitionBookmarkPb)
  private static final io.littlehorse.common.proto.PartitionBookmarkPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.PartitionBookmarkPb();
  }

  public static io.littlehorse.common.proto.PartitionBookmarkPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PartitionBookmarkPb>
      PARSER = new com.google.protobuf.AbstractParser<PartitionBookmarkPb>() {
    @java.lang.Override
    public PartitionBookmarkPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<PartitionBookmarkPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PartitionBookmarkPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.PartitionBookmarkPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

