// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: node_run.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * The sub-node structure for a START_THREAD NodeRun.
 * </pre>
 *
 * Protobuf type {@code littlehorse.StartThreadRun}
 */
public final class StartThreadRun extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.StartThreadRun)
    StartThreadRunOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      StartThreadRun.class.getName());
  }
  // Use StartThreadRun.newBuilder() to construct.
  private StartThreadRun(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private StartThreadRun() {
    threadSpecName_ = "";
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_StartThreadRun_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_StartThreadRun_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.StartThreadRun.class, io.littlehorse.sdk.common.proto.StartThreadRun.Builder.class);
  }

  private int bitField0_;
  public static final int CHILD_THREAD_ID_FIELD_NUMBER = 1;
  private int childThreadId_ = 0;
  /**
   * <pre>
   * Contains the thread_run_number of the created Child ThreadRun, if it has
   * been created already.
   * </pre>
   *
   * <code>optional int32 child_thread_id = 1;</code>
   * @return Whether the childThreadId field is set.
   */
  @java.lang.Override
  public boolean hasChildThreadId() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * Contains the thread_run_number of the created Child ThreadRun, if it has
   * been created already.
   * </pre>
   *
   * <code>optional int32 child_thread_id = 1;</code>
   * @return The childThreadId.
   */
  @java.lang.Override
  public int getChildThreadId() {
    return childThreadId_;
  }

  public static final int THREAD_SPEC_NAME_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private volatile java.lang.Object threadSpecName_ = "";
  /**
   * <pre>
   * The thread_spec_name of the child thread_run.
   * </pre>
   *
   * <code>string thread_spec_name = 2;</code>
   * @return The threadSpecName.
   */
  @java.lang.Override
  public java.lang.String getThreadSpecName() {
    java.lang.Object ref = threadSpecName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      threadSpecName_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The thread_spec_name of the child thread_run.
   * </pre>
   *
   * <code>string thread_spec_name = 2;</code>
   * @return The bytes for threadSpecName.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getThreadSpecNameBytes() {
    java.lang.Object ref = threadSpecName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      threadSpecName_ = b;
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
      output.writeInt32(1, childThreadId_);
    }
    if (!com.google.protobuf.GeneratedMessage.isStringEmpty(threadSpecName_)) {
      com.google.protobuf.GeneratedMessage.writeString(output, 2, threadSpecName_);
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
        .computeInt32Size(1, childThreadId_);
    }
    if (!com.google.protobuf.GeneratedMessage.isStringEmpty(threadSpecName_)) {
      size += com.google.protobuf.GeneratedMessage.computeStringSize(2, threadSpecName_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.StartThreadRun)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.StartThreadRun other = (io.littlehorse.sdk.common.proto.StartThreadRun) obj;

    if (hasChildThreadId() != other.hasChildThreadId()) return false;
    if (hasChildThreadId()) {
      if (getChildThreadId()
          != other.getChildThreadId()) return false;
    }
    if (!getThreadSpecName()
        .equals(other.getThreadSpecName())) return false;
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
    if (hasChildThreadId()) {
      hash = (37 * hash) + CHILD_THREAD_ID_FIELD_NUMBER;
      hash = (53 * hash) + getChildThreadId();
    }
    hash = (37 * hash) + THREAD_SPEC_NAME_FIELD_NUMBER;
    hash = (53 * hash) + getThreadSpecName().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.StartThreadRun parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.StartThreadRun parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.StartThreadRun parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.StartThreadRun parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.StartThreadRun parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.StartThreadRun parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.StartThreadRun parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.StartThreadRun parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.StartThreadRun parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.StartThreadRun parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.StartThreadRun parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.StartThreadRun parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.StartThreadRun prototype) {
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
   * The sub-node structure for a START_THREAD NodeRun.
   * </pre>
   *
   * Protobuf type {@code littlehorse.StartThreadRun}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.StartThreadRun)
      io.littlehorse.sdk.common.proto.StartThreadRunOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_StartThreadRun_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_StartThreadRun_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.StartThreadRun.class, io.littlehorse.sdk.common.proto.StartThreadRun.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.StartThreadRun.newBuilder()
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
      childThreadId_ = 0;
      threadSpecName_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.NodeRunOuterClass.internal_static_littlehorse_StartThreadRun_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.StartThreadRun getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.StartThreadRun.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.StartThreadRun build() {
      io.littlehorse.sdk.common.proto.StartThreadRun result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.StartThreadRun buildPartial() {
      io.littlehorse.sdk.common.proto.StartThreadRun result = new io.littlehorse.sdk.common.proto.StartThreadRun(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.StartThreadRun result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.childThreadId_ = childThreadId_;
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.threadSpecName_ = threadSpecName_;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.sdk.common.proto.StartThreadRun) {
        return mergeFrom((io.littlehorse.sdk.common.proto.StartThreadRun)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.StartThreadRun other) {
      if (other == io.littlehorse.sdk.common.proto.StartThreadRun.getDefaultInstance()) return this;
      if (other.hasChildThreadId()) {
        setChildThreadId(other.getChildThreadId());
      }
      if (!other.getThreadSpecName().isEmpty()) {
        threadSpecName_ = other.threadSpecName_;
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
              childThreadId_ = input.readInt32();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 18: {
              threadSpecName_ = input.readStringRequireUtf8();
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

    private int childThreadId_ ;
    /**
     * <pre>
     * Contains the thread_run_number of the created Child ThreadRun, if it has
     * been created already.
     * </pre>
     *
     * <code>optional int32 child_thread_id = 1;</code>
     * @return Whether the childThreadId field is set.
     */
    @java.lang.Override
    public boolean hasChildThreadId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * Contains the thread_run_number of the created Child ThreadRun, if it has
     * been created already.
     * </pre>
     *
     * <code>optional int32 child_thread_id = 1;</code>
     * @return The childThreadId.
     */
    @java.lang.Override
    public int getChildThreadId() {
      return childThreadId_;
    }
    /**
     * <pre>
     * Contains the thread_run_number of the created Child ThreadRun, if it has
     * been created already.
     * </pre>
     *
     * <code>optional int32 child_thread_id = 1;</code>
     * @param value The childThreadId to set.
     * @return This builder for chaining.
     */
    public Builder setChildThreadId(int value) {

      childThreadId_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Contains the thread_run_number of the created Child ThreadRun, if it has
     * been created already.
     * </pre>
     *
     * <code>optional int32 child_thread_id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearChildThreadId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      childThreadId_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object threadSpecName_ = "";
    /**
     * <pre>
     * The thread_spec_name of the child thread_run.
     * </pre>
     *
     * <code>string thread_spec_name = 2;</code>
     * @return The threadSpecName.
     */
    public java.lang.String getThreadSpecName() {
      java.lang.Object ref = threadSpecName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        threadSpecName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The thread_spec_name of the child thread_run.
     * </pre>
     *
     * <code>string thread_spec_name = 2;</code>
     * @return The bytes for threadSpecName.
     */
    public com.google.protobuf.ByteString
        getThreadSpecNameBytes() {
      java.lang.Object ref = threadSpecName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        threadSpecName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The thread_spec_name of the child thread_run.
     * </pre>
     *
     * <code>string thread_spec_name = 2;</code>
     * @param value The threadSpecName to set.
     * @return This builder for chaining.
     */
    public Builder setThreadSpecName(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      threadSpecName_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The thread_spec_name of the child thread_run.
     * </pre>
     *
     * <code>string thread_spec_name = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearThreadSpecName() {
      threadSpecName_ = getDefaultInstance().getThreadSpecName();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The thread_spec_name of the child thread_run.
     * </pre>
     *
     * <code>string thread_spec_name = 2;</code>
     * @param value The bytes for threadSpecName to set.
     * @return This builder for chaining.
     */
    public Builder setThreadSpecNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      threadSpecName_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.StartThreadRun)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.StartThreadRun)
  private static final io.littlehorse.sdk.common.proto.StartThreadRun DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.StartThreadRun();
  }

  public static io.littlehorse.sdk.common.proto.StartThreadRun getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<StartThreadRun>
      PARSER = new com.google.protobuf.AbstractParser<StartThreadRun>() {
    @java.lang.Override
    public StartThreadRun parsePartialFrom(
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

  public static com.google.protobuf.Parser<StartThreadRun> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<StartThreadRun> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.StartThreadRun getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

