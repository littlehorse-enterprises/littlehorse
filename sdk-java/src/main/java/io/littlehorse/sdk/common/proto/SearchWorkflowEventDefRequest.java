// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: service.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Search for WorkflowEventDefs based on certain criteria.
 * </pre>
 *
 * Protobuf type {@code littlehorse.SearchWorkflowEventDefRequest}
 */
public final class SearchWorkflowEventDefRequest extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.SearchWorkflowEventDefRequest)
    SearchWorkflowEventDefRequestOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      SearchWorkflowEventDefRequest.class.getName());
  }
  // Use SearchWorkflowEventDefRequest.newBuilder() to construct.
  private SearchWorkflowEventDefRequest(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private SearchWorkflowEventDefRequest() {
    bookmark_ = com.google.protobuf.ByteString.EMPTY;
    prefix_ = "";
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchWorkflowEventDefRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchWorkflowEventDefRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest.class, io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest.Builder.class);
  }

  private int bitField0_;
  public static final int BOOKMARK_FIELD_NUMBER = 1;
  private com.google.protobuf.ByteString bookmark_ = com.google.protobuf.ByteString.EMPTY;
  /**
   * <pre>
   * Bookmark for cursor-based pagination; pass if applicable.
   * </pre>
   *
   * <code>optional bytes bookmark = 1;</code>
   * @return Whether the bookmark field is set.
   */
  @java.lang.Override
  public boolean hasBookmark() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * Bookmark for cursor-based pagination; pass if applicable.
   * </pre>
   *
   * <code>optional bytes bookmark = 1;</code>
   * @return The bookmark.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getBookmark() {
    return bookmark_;
  }

  public static final int LIMIT_FIELD_NUMBER = 2;
  private int limit_ = 0;
  /**
   * <pre>
   * Maximum results to return in one request.
   * </pre>
   *
   * <code>optional int32 limit = 2;</code>
   * @return Whether the limit field is set.
   */
  @java.lang.Override
  public boolean hasLimit() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <pre>
   * Maximum results to return in one request.
   * </pre>
   *
   * <code>optional int32 limit = 2;</code>
   * @return The limit.
   */
  @java.lang.Override
  public int getLimit() {
    return limit_;
  }

  public static final int PREFIX_FIELD_NUMBER = 3;
  @SuppressWarnings("serial")
  private volatile java.lang.Object prefix_ = "";
  /**
   * <pre>
   * Optionally search only for WorkflowEventDef's whose name starts with this prefix.
   * </pre>
   *
   * <code>optional string prefix = 3;</code>
   * @return Whether the prefix field is set.
   */
  @java.lang.Override
  public boolean hasPrefix() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <pre>
   * Optionally search only for WorkflowEventDef's whose name starts with this prefix.
   * </pre>
   *
   * <code>optional string prefix = 3;</code>
   * @return The prefix.
   */
  @java.lang.Override
  public java.lang.String getPrefix() {
    java.lang.Object ref = prefix_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      prefix_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * Optionally search only for WorkflowEventDef's whose name starts with this prefix.
   * </pre>
   *
   * <code>optional string prefix = 3;</code>
   * @return The bytes for prefix.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getPrefixBytes() {
    java.lang.Object ref = prefix_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      prefix_ = b;
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
      output.writeBytes(1, bookmark_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      output.writeInt32(2, limit_);
    }
    if (((bitField0_ & 0x00000004) != 0)) {
      com.google.protobuf.GeneratedMessage.writeString(output, 3, prefix_);
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
        .computeBytesSize(1, bookmark_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, limit_);
    }
    if (((bitField0_ & 0x00000004) != 0)) {
      size += com.google.protobuf.GeneratedMessage.computeStringSize(3, prefix_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest other = (io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest) obj;

    if (hasBookmark() != other.hasBookmark()) return false;
    if (hasBookmark()) {
      if (!getBookmark()
          .equals(other.getBookmark())) return false;
    }
    if (hasLimit() != other.hasLimit()) return false;
    if (hasLimit()) {
      if (getLimit()
          != other.getLimit()) return false;
    }
    if (hasPrefix() != other.hasPrefix()) return false;
    if (hasPrefix()) {
      if (!getPrefix()
          .equals(other.getPrefix())) return false;
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
    if (hasBookmark()) {
      hash = (37 * hash) + BOOKMARK_FIELD_NUMBER;
      hash = (53 * hash) + getBookmark().hashCode();
    }
    if (hasLimit()) {
      hash = (37 * hash) + LIMIT_FIELD_NUMBER;
      hash = (53 * hash) + getLimit();
    }
    if (hasPrefix()) {
      hash = (37 * hash) + PREFIX_FIELD_NUMBER;
      hash = (53 * hash) + getPrefix().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest prototype) {
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
   * Search for WorkflowEventDefs based on certain criteria.
   * </pre>
   *
   * Protobuf type {@code littlehorse.SearchWorkflowEventDefRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.SearchWorkflowEventDefRequest)
      io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchWorkflowEventDefRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchWorkflowEventDefRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest.class, io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest.newBuilder()
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
      bookmark_ = com.google.protobuf.ByteString.EMPTY;
      limit_ = 0;
      prefix_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchWorkflowEventDefRequest_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest build() {
      io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest buildPartial() {
      io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest result = new io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.bookmark_ = bookmark_;
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.limit_ = limit_;
        to_bitField0_ |= 0x00000002;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.prefix_ = prefix_;
        to_bitField0_ |= 0x00000004;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest) {
        return mergeFrom((io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest other) {
      if (other == io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest.getDefaultInstance()) return this;
      if (other.hasBookmark()) {
        setBookmark(other.getBookmark());
      }
      if (other.hasLimit()) {
        setLimit(other.getLimit());
      }
      if (other.hasPrefix()) {
        prefix_ = other.prefix_;
        bitField0_ |= 0x00000004;
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
              bookmark_ = input.readBytes();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 16: {
              limit_ = input.readInt32();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
            case 26: {
              prefix_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000004;
              break;
            } // case 26
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

    private com.google.protobuf.ByteString bookmark_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <pre>
     * Bookmark for cursor-based pagination; pass if applicable.
     * </pre>
     *
     * <code>optional bytes bookmark = 1;</code>
     * @return Whether the bookmark field is set.
     */
    @java.lang.Override
    public boolean hasBookmark() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * Bookmark for cursor-based pagination; pass if applicable.
     * </pre>
     *
     * <code>optional bytes bookmark = 1;</code>
     * @return The bookmark.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getBookmark() {
      return bookmark_;
    }
    /**
     * <pre>
     * Bookmark for cursor-based pagination; pass if applicable.
     * </pre>
     *
     * <code>optional bytes bookmark = 1;</code>
     * @param value The bookmark to set.
     * @return This builder for chaining.
     */
    public Builder setBookmark(com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      bookmark_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Bookmark for cursor-based pagination; pass if applicable.
     * </pre>
     *
     * <code>optional bytes bookmark = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearBookmark() {
      bitField0_ = (bitField0_ & ~0x00000001);
      bookmark_ = getDefaultInstance().getBookmark();
      onChanged();
      return this;
    }

    private int limit_ ;
    /**
     * <pre>
     * Maximum results to return in one request.
     * </pre>
     *
     * <code>optional int32 limit = 2;</code>
     * @return Whether the limit field is set.
     */
    @java.lang.Override
    public boolean hasLimit() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * Maximum results to return in one request.
     * </pre>
     *
     * <code>optional int32 limit = 2;</code>
     * @return The limit.
     */
    @java.lang.Override
    public int getLimit() {
      return limit_;
    }
    /**
     * <pre>
     * Maximum results to return in one request.
     * </pre>
     *
     * <code>optional int32 limit = 2;</code>
     * @param value The limit to set.
     * @return This builder for chaining.
     */
    public Builder setLimit(int value) {

      limit_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Maximum results to return in one request.
     * </pre>
     *
     * <code>optional int32 limit = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearLimit() {
      bitField0_ = (bitField0_ & ~0x00000002);
      limit_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object prefix_ = "";
    /**
     * <pre>
     * Optionally search only for WorkflowEventDef's whose name starts with this prefix.
     * </pre>
     *
     * <code>optional string prefix = 3;</code>
     * @return Whether the prefix field is set.
     */
    public boolean hasPrefix() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <pre>
     * Optionally search only for WorkflowEventDef's whose name starts with this prefix.
     * </pre>
     *
     * <code>optional string prefix = 3;</code>
     * @return The prefix.
     */
    public java.lang.String getPrefix() {
      java.lang.Object ref = prefix_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        prefix_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * Optionally search only for WorkflowEventDef's whose name starts with this prefix.
     * </pre>
     *
     * <code>optional string prefix = 3;</code>
     * @return The bytes for prefix.
     */
    public com.google.protobuf.ByteString
        getPrefixBytes() {
      java.lang.Object ref = prefix_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        prefix_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * Optionally search only for WorkflowEventDef's whose name starts with this prefix.
     * </pre>
     *
     * <code>optional string prefix = 3;</code>
     * @param value The prefix to set.
     * @return This builder for chaining.
     */
    public Builder setPrefix(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      prefix_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Optionally search only for WorkflowEventDef's whose name starts with this prefix.
     * </pre>
     *
     * <code>optional string prefix = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearPrefix() {
      prefix_ = getDefaultInstance().getPrefix();
      bitField0_ = (bitField0_ & ~0x00000004);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Optionally search only for WorkflowEventDef's whose name starts with this prefix.
     * </pre>
     *
     * <code>optional string prefix = 3;</code>
     * @param value The bytes for prefix to set.
     * @return This builder for chaining.
     */
    public Builder setPrefixBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      prefix_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.SearchWorkflowEventDefRequest)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.SearchWorkflowEventDefRequest)
  private static final io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest();
  }

  public static io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SearchWorkflowEventDefRequest>
      PARSER = new com.google.protobuf.AbstractParser<SearchWorkflowEventDefRequest>() {
    @java.lang.Override
    public SearchWorkflowEventDefRequest parsePartialFrom(
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

  public static com.google.protobuf.Parser<SearchWorkflowEventDefRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SearchWorkflowEventDefRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

