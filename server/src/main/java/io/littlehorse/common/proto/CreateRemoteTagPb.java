// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: command.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.common.proto;

/**
 * Protobuf type {@code littlehorse.CreateRemoteTagPb}
 */
public final class CreateRemoteTagPb extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.CreateRemoteTagPb)
    CreateRemoteTagPbOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 29,
      /* patch= */ 3,
      /* suffix= */ "",
      CreateRemoteTagPb.class.getName());
  }
  // Use CreateRemoteTagPb.newBuilder() to construct.
  private CreateRemoteTagPb(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private CreateRemoteTagPb() {
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_CreateRemoteTagPb_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_CreateRemoteTagPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.CreateRemoteTagPb.class, io.littlehorse.common.proto.CreateRemoteTagPb.Builder.class);
  }

  private int bitField0_;
  public static final int TAG_FIELD_NUMBER = 1;
  private io.littlehorse.common.proto.TagPb tag_;
  /**
   * <code>.littlehorse.TagPb tag = 1;</code>
   * @return Whether the tag field is set.
   */
  @java.lang.Override
  public boolean hasTag() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>.littlehorse.TagPb tag = 1;</code>
   * @return The tag.
   */
  @java.lang.Override
  public io.littlehorse.common.proto.TagPb getTag() {
    return tag_ == null ? io.littlehorse.common.proto.TagPb.getDefaultInstance() : tag_;
  }
  /**
   * <code>.littlehorse.TagPb tag = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.TagPbOrBuilder getTagOrBuilder() {
    return tag_ == null ? io.littlehorse.common.proto.TagPb.getDefaultInstance() : tag_;
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
      output.writeMessage(1, getTag());
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
        .computeMessageSize(1, getTag());
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
    if (!(obj instanceof io.littlehorse.common.proto.CreateRemoteTagPb)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.CreateRemoteTagPb other = (io.littlehorse.common.proto.CreateRemoteTagPb) obj;

    if (hasTag() != other.hasTag()) return false;
    if (hasTag()) {
      if (!getTag()
          .equals(other.getTag())) return false;
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
    if (hasTag()) {
      hash = (37 * hash) + TAG_FIELD_NUMBER;
      hash = (53 * hash) + getTag().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.CreateRemoteTagPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.CreateRemoteTagPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.CreateRemoteTagPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.CreateRemoteTagPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.CreateRemoteTagPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.CreateRemoteTagPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.CreateRemoteTagPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.CreateRemoteTagPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.CreateRemoteTagPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.CreateRemoteTagPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.CreateRemoteTagPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.CreateRemoteTagPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.CreateRemoteTagPb prototype) {
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
   * Protobuf type {@code littlehorse.CreateRemoteTagPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.CreateRemoteTagPb)
      io.littlehorse.common.proto.CreateRemoteTagPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_CreateRemoteTagPb_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_CreateRemoteTagPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.CreateRemoteTagPb.class, io.littlehorse.common.proto.CreateRemoteTagPb.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.CreateRemoteTagPb.newBuilder()
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
        getTagFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      tag_ = null;
      if (tagBuilder_ != null) {
        tagBuilder_.dispose();
        tagBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_CreateRemoteTagPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.CreateRemoteTagPb getDefaultInstanceForType() {
      return io.littlehorse.common.proto.CreateRemoteTagPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.CreateRemoteTagPb build() {
      io.littlehorse.common.proto.CreateRemoteTagPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.CreateRemoteTagPb buildPartial() {
      io.littlehorse.common.proto.CreateRemoteTagPb result = new io.littlehorse.common.proto.CreateRemoteTagPb(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.common.proto.CreateRemoteTagPb result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.tag_ = tagBuilder_ == null
            ? tag_
            : tagBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.common.proto.CreateRemoteTagPb) {
        return mergeFrom((io.littlehorse.common.proto.CreateRemoteTagPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.CreateRemoteTagPb other) {
      if (other == io.littlehorse.common.proto.CreateRemoteTagPb.getDefaultInstance()) return this;
      if (other.hasTag()) {
        mergeTag(other.getTag());
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
                  getTagFieldBuilder().getBuilder(),
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

    private io.littlehorse.common.proto.TagPb tag_;
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.common.proto.TagPb, io.littlehorse.common.proto.TagPb.Builder, io.littlehorse.common.proto.TagPbOrBuilder> tagBuilder_;
    /**
     * <code>.littlehorse.TagPb tag = 1;</code>
     * @return Whether the tag field is set.
     */
    public boolean hasTag() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.littlehorse.TagPb tag = 1;</code>
     * @return The tag.
     */
    public io.littlehorse.common.proto.TagPb getTag() {
      if (tagBuilder_ == null) {
        return tag_ == null ? io.littlehorse.common.proto.TagPb.getDefaultInstance() : tag_;
      } else {
        return tagBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.TagPb tag = 1;</code>
     */
    public Builder setTag(io.littlehorse.common.proto.TagPb value) {
      if (tagBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        tag_ = value;
      } else {
        tagBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TagPb tag = 1;</code>
     */
    public Builder setTag(
        io.littlehorse.common.proto.TagPb.Builder builderForValue) {
      if (tagBuilder_ == null) {
        tag_ = builderForValue.build();
      } else {
        tagBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TagPb tag = 1;</code>
     */
    public Builder mergeTag(io.littlehorse.common.proto.TagPb value) {
      if (tagBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          tag_ != null &&
          tag_ != io.littlehorse.common.proto.TagPb.getDefaultInstance()) {
          getTagBuilder().mergeFrom(value);
        } else {
          tag_ = value;
        }
      } else {
        tagBuilder_.mergeFrom(value);
      }
      if (tag_ != null) {
        bitField0_ |= 0x00000001;
        onChanged();
      }
      return this;
    }
    /**
     * <code>.littlehorse.TagPb tag = 1;</code>
     */
    public Builder clearTag() {
      bitField0_ = (bitField0_ & ~0x00000001);
      tag_ = null;
      if (tagBuilder_ != null) {
        tagBuilder_.dispose();
        tagBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TagPb tag = 1;</code>
     */
    public io.littlehorse.common.proto.TagPb.Builder getTagBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getTagFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.TagPb tag = 1;</code>
     */
    public io.littlehorse.common.proto.TagPbOrBuilder getTagOrBuilder() {
      if (tagBuilder_ != null) {
        return tagBuilder_.getMessageOrBuilder();
      } else {
        return tag_ == null ?
            io.littlehorse.common.proto.TagPb.getDefaultInstance() : tag_;
      }
    }
    /**
     * <code>.littlehorse.TagPb tag = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.common.proto.TagPb, io.littlehorse.common.proto.TagPb.Builder, io.littlehorse.common.proto.TagPbOrBuilder> 
        getTagFieldBuilder() {
      if (tagBuilder_ == null) {
        tagBuilder_ = new com.google.protobuf.SingleFieldBuilder<
            io.littlehorse.common.proto.TagPb, io.littlehorse.common.proto.TagPb.Builder, io.littlehorse.common.proto.TagPbOrBuilder>(
                getTag(),
                getParentForChildren(),
                isClean());
        tag_ = null;
      }
      return tagBuilder_;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.CreateRemoteTagPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.CreateRemoteTagPb)
  private static final io.littlehorse.common.proto.CreateRemoteTagPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.CreateRemoteTagPb();
  }

  public static io.littlehorse.common.proto.CreateRemoteTagPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<CreateRemoteTagPb>
      PARSER = new com.google.protobuf.AbstractParser<CreateRemoteTagPb>() {
    @java.lang.Override
    public CreateRemoteTagPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<CreateRemoteTagPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<CreateRemoteTagPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.CreateRemoteTagPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

