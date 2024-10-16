// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: init.proto

package io.littlehorse.common.proto;

/**
 * Protobuf type {@code littlehorse.ServerVersion}
 */
public final class ServerVersion extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.ServerVersion)
    ServerVersionOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ServerVersion.newBuilder() to construct.
  private ServerVersion(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ServerVersion() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ServerVersion();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.Init.internal_static_littlehorse_ServerVersion_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.Init.internal_static_littlehorse_ServerVersion_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.ServerVersion.class, io.littlehorse.common.proto.ServerVersion.Builder.class);
  }

  public static final int MAJOR_VERSION_FIELD_NUMBER = 1;
  private int majorVersion_ = 0;
  /**
   * <pre>
   * Server Major Version
   * </pre>
   *
   * <code>int32 major_version = 1;</code>
   * @return The majorVersion.
   */
  @java.lang.Override
  public int getMajorVersion() {
    return majorVersion_;
  }

  public static final int MINOR_VERSION_FIELD_NUMBER = 2;
  private int minorVersion_ = 0;
  /**
   * <pre>
   * Server Minor Version
   * </pre>
   *
   * <code>int32 minor_version = 2;</code>
   * @return The minorVersion.
   */
  @java.lang.Override
  public int getMinorVersion() {
    return minorVersion_;
  }

  public static final int PATCH_VERSION_FIELD_NUMBER = 3;
  private int patchVersion_ = 0;
  /**
   * <pre>
   * Server Patch Version
   * </pre>
   *
   * <code>int32 patch_version = 3;</code>
   * @return The patchVersion.
   */
  @java.lang.Override
  public int getPatchVersion() {
    return patchVersion_;
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
    if (majorVersion_ != 0) {
      output.writeInt32(1, majorVersion_);
    }
    if (minorVersion_ != 0) {
      output.writeInt32(2, minorVersion_);
    }
    if (patchVersion_ != 0) {
      output.writeInt32(3, patchVersion_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (majorVersion_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(1, majorVersion_);
    }
    if (minorVersion_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, minorVersion_);
    }
    if (patchVersion_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(3, patchVersion_);
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
    if (!(obj instanceof io.littlehorse.common.proto.ServerVersion)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.ServerVersion other = (io.littlehorse.common.proto.ServerVersion) obj;

    if (getMajorVersion()
        != other.getMajorVersion()) return false;
    if (getMinorVersion()
        != other.getMinorVersion()) return false;
    if (getPatchVersion()
        != other.getPatchVersion()) return false;
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
    hash = (37 * hash) + MAJOR_VERSION_FIELD_NUMBER;
    hash = (53 * hash) + getMajorVersion();
    hash = (37 * hash) + MINOR_VERSION_FIELD_NUMBER;
    hash = (53 * hash) + getMinorVersion();
    hash = (37 * hash) + PATCH_VERSION_FIELD_NUMBER;
    hash = (53 * hash) + getPatchVersion();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.ServerVersion parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.ServerVersion parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.ServerVersion parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.ServerVersion parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.ServerVersion parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.ServerVersion parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.ServerVersion parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.ServerVersion parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.ServerVersion parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.ServerVersion parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.ServerVersion parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.ServerVersion parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.ServerVersion prototype) {
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
   * Protobuf type {@code littlehorse.ServerVersion}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.ServerVersion)
      io.littlehorse.common.proto.ServerVersionOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.Init.internal_static_littlehorse_ServerVersion_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.Init.internal_static_littlehorse_ServerVersion_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.ServerVersion.class, io.littlehorse.common.proto.ServerVersion.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.ServerVersion.newBuilder()
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
      majorVersion_ = 0;
      minorVersion_ = 0;
      patchVersion_ = 0;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.Init.internal_static_littlehorse_ServerVersion_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.ServerVersion getDefaultInstanceForType() {
      return io.littlehorse.common.proto.ServerVersion.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.ServerVersion build() {
      io.littlehorse.common.proto.ServerVersion result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.ServerVersion buildPartial() {
      io.littlehorse.common.proto.ServerVersion result = new io.littlehorse.common.proto.ServerVersion(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.common.proto.ServerVersion result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.majorVersion_ = majorVersion_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.minorVersion_ = minorVersion_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.patchVersion_ = patchVersion_;
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
      if (other instanceof io.littlehorse.common.proto.ServerVersion) {
        return mergeFrom((io.littlehorse.common.proto.ServerVersion)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.ServerVersion other) {
      if (other == io.littlehorse.common.proto.ServerVersion.getDefaultInstance()) return this;
      if (other.getMajorVersion() != 0) {
        setMajorVersion(other.getMajorVersion());
      }
      if (other.getMinorVersion() != 0) {
        setMinorVersion(other.getMinorVersion());
      }
      if (other.getPatchVersion() != 0) {
        setPatchVersion(other.getPatchVersion());
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
              majorVersion_ = input.readInt32();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 16: {
              minorVersion_ = input.readInt32();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
            case 24: {
              patchVersion_ = input.readInt32();
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

    private int majorVersion_ ;
    /**
     * <pre>
     * Server Major Version
     * </pre>
     *
     * <code>int32 major_version = 1;</code>
     * @return The majorVersion.
     */
    @java.lang.Override
    public int getMajorVersion() {
      return majorVersion_;
    }
    /**
     * <pre>
     * Server Major Version
     * </pre>
     *
     * <code>int32 major_version = 1;</code>
     * @param value The majorVersion to set.
     * @return This builder for chaining.
     */
    public Builder setMajorVersion(int value) {

      majorVersion_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Server Major Version
     * </pre>
     *
     * <code>int32 major_version = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearMajorVersion() {
      bitField0_ = (bitField0_ & ~0x00000001);
      majorVersion_ = 0;
      onChanged();
      return this;
    }

    private int minorVersion_ ;
    /**
     * <pre>
     * Server Minor Version
     * </pre>
     *
     * <code>int32 minor_version = 2;</code>
     * @return The minorVersion.
     */
    @java.lang.Override
    public int getMinorVersion() {
      return minorVersion_;
    }
    /**
     * <pre>
     * Server Minor Version
     * </pre>
     *
     * <code>int32 minor_version = 2;</code>
     * @param value The minorVersion to set.
     * @return This builder for chaining.
     */
    public Builder setMinorVersion(int value) {

      minorVersion_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Server Minor Version
     * </pre>
     *
     * <code>int32 minor_version = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearMinorVersion() {
      bitField0_ = (bitField0_ & ~0x00000002);
      minorVersion_ = 0;
      onChanged();
      return this;
    }

    private int patchVersion_ ;
    /**
     * <pre>
     * Server Patch Version
     * </pre>
     *
     * <code>int32 patch_version = 3;</code>
     * @return The patchVersion.
     */
    @java.lang.Override
    public int getPatchVersion() {
      return patchVersion_;
    }
    /**
     * <pre>
     * Server Patch Version
     * </pre>
     *
     * <code>int32 patch_version = 3;</code>
     * @param value The patchVersion to set.
     * @return This builder for chaining.
     */
    public Builder setPatchVersion(int value) {

      patchVersion_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Server Patch Version
     * </pre>
     *
     * <code>int32 patch_version = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearPatchVersion() {
      bitField0_ = (bitField0_ & ~0x00000004);
      patchVersion_ = 0;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.ServerVersion)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.ServerVersion)
  private static final io.littlehorse.common.proto.ServerVersion DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.ServerVersion();
  }

  public static io.littlehorse.common.proto.ServerVersion getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ServerVersion>
      PARSER = new com.google.protobuf.AbstractParser<ServerVersion>() {
    @java.lang.Override
    public ServerVersion parsePartialFrom(
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

  public static com.google.protobuf.Parser<ServerVersion> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ServerVersion> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.ServerVersion getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

