// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: service.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Search for all available TenantIds for current Principal
 * </pre>
 *
 * Protobuf type {@code littlehorse.SearchTenantRequest}
 */
public final class SearchTenantRequest extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.SearchTenantRequest)
    SearchTenantRequestOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 29,
      /* patch= */ 3,
      /* suffix= */ "",
      SearchTenantRequest.class.getName());
  }
  // Use SearchTenantRequest.newBuilder() to construct.
  private SearchTenantRequest(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private SearchTenantRequest() {
    bookmark_ = com.google.protobuf.ByteString.EMPTY;
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchTenantRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchTenantRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.SearchTenantRequest.class, io.littlehorse.sdk.common.proto.SearchTenantRequest.Builder.class);
  }

  private int bitField0_;
  public static final int LIMIT_FIELD_NUMBER = 1;
  private int limit_ = 0;
  /**
   * <pre>
   * Maximum results to return in one request.
   * </pre>
   *
   * <code>optional int32 limit = 1;</code>
   * @return Whether the limit field is set.
   */
  @java.lang.Override
  public boolean hasLimit() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * Maximum results to return in one request.
   * </pre>
   *
   * <code>optional int32 limit = 1;</code>
   * @return The limit.
   */
  @java.lang.Override
  public int getLimit() {
    return limit_;
  }

  public static final int BOOKMARK_FIELD_NUMBER = 2;
  private com.google.protobuf.ByteString bookmark_ = com.google.protobuf.ByteString.EMPTY;
  /**
   * <pre>
   * Bookmark for cursor-based pagination; pass if applicable.
   * </pre>
   *
   * <code>optional bytes bookmark = 2;</code>
   * @return Whether the bookmark field is set.
   */
  @java.lang.Override
  public boolean hasBookmark() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <pre>
   * Bookmark for cursor-based pagination; pass if applicable.
   * </pre>
   *
   * <code>optional bytes bookmark = 2;</code>
   * @return The bookmark.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getBookmark() {
    return bookmark_;
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
      output.writeInt32(1, limit_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      output.writeBytes(2, bookmark_);
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
        .computeInt32Size(1, limit_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(2, bookmark_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.SearchTenantRequest)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.SearchTenantRequest other = (io.littlehorse.sdk.common.proto.SearchTenantRequest) obj;

    if (hasLimit() != other.hasLimit()) return false;
    if (hasLimit()) {
      if (getLimit()
          != other.getLimit()) return false;
    }
    if (hasBookmark() != other.hasBookmark()) return false;
    if (hasBookmark()) {
      if (!getBookmark()
          .equals(other.getBookmark())) return false;
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
    if (hasLimit()) {
      hash = (37 * hash) + LIMIT_FIELD_NUMBER;
      hash = (53 * hash) + getLimit();
    }
    if (hasBookmark()) {
      hash = (37 * hash) + BOOKMARK_FIELD_NUMBER;
      hash = (53 * hash) + getBookmark().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.SearchTenantRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SearchTenantRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchTenantRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SearchTenantRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchTenantRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SearchTenantRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchTenantRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.SearchTenantRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.SearchTenantRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.SearchTenantRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchTenantRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.SearchTenantRequest parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.SearchTenantRequest prototype) {
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
   * Search for all available TenantIds for current Principal
   * </pre>
   *
   * Protobuf type {@code littlehorse.SearchTenantRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.SearchTenantRequest)
      io.littlehorse.sdk.common.proto.SearchTenantRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchTenantRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchTenantRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.SearchTenantRequest.class, io.littlehorse.sdk.common.proto.SearchTenantRequest.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.SearchTenantRequest.newBuilder()
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
      limit_ = 0;
      bookmark_ = com.google.protobuf.ByteString.EMPTY;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchTenantRequest_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SearchTenantRequest getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.SearchTenantRequest.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SearchTenantRequest build() {
      io.littlehorse.sdk.common.proto.SearchTenantRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SearchTenantRequest buildPartial() {
      io.littlehorse.sdk.common.proto.SearchTenantRequest result = new io.littlehorse.sdk.common.proto.SearchTenantRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.SearchTenantRequest result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.limit_ = limit_;
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.bookmark_ = bookmark_;
        to_bitField0_ |= 0x00000002;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.sdk.common.proto.SearchTenantRequest) {
        return mergeFrom((io.littlehorse.sdk.common.proto.SearchTenantRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.SearchTenantRequest other) {
      if (other == io.littlehorse.sdk.common.proto.SearchTenantRequest.getDefaultInstance()) return this;
      if (other.hasLimit()) {
        setLimit(other.getLimit());
      }
      if (other.hasBookmark()) {
        setBookmark(other.getBookmark());
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
              limit_ = input.readInt32();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 18: {
              bookmark_ = input.readBytes();
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

    private int limit_ ;
    /**
     * <pre>
     * Maximum results to return in one request.
     * </pre>
     *
     * <code>optional int32 limit = 1;</code>
     * @return Whether the limit field is set.
     */
    @java.lang.Override
    public boolean hasLimit() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * Maximum results to return in one request.
     * </pre>
     *
     * <code>optional int32 limit = 1;</code>
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
     * <code>optional int32 limit = 1;</code>
     * @param value The limit to set.
     * @return This builder for chaining.
     */
    public Builder setLimit(int value) {

      limit_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Maximum results to return in one request.
     * </pre>
     *
     * <code>optional int32 limit = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearLimit() {
      bitField0_ = (bitField0_ & ~0x00000001);
      limit_ = 0;
      onChanged();
      return this;
    }

    private com.google.protobuf.ByteString bookmark_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <pre>
     * Bookmark for cursor-based pagination; pass if applicable.
     * </pre>
     *
     * <code>optional bytes bookmark = 2;</code>
     * @return Whether the bookmark field is set.
     */
    @java.lang.Override
    public boolean hasBookmark() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * Bookmark for cursor-based pagination; pass if applicable.
     * </pre>
     *
     * <code>optional bytes bookmark = 2;</code>
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
     * <code>optional bytes bookmark = 2;</code>
     * @param value The bookmark to set.
     * @return This builder for chaining.
     */
    public Builder setBookmark(com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      bookmark_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Bookmark for cursor-based pagination; pass if applicable.
     * </pre>
     *
     * <code>optional bytes bookmark = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearBookmark() {
      bitField0_ = (bitField0_ & ~0x00000002);
      bookmark_ = getDefaultInstance().getBookmark();
      onChanged();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.SearchTenantRequest)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.SearchTenantRequest)
  private static final io.littlehorse.sdk.common.proto.SearchTenantRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.SearchTenantRequest();
  }

  public static io.littlehorse.sdk.common.proto.SearchTenantRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SearchTenantRequest>
      PARSER = new com.google.protobuf.AbstractParser<SearchTenantRequest>() {
    @java.lang.Override
    public SearchTenantRequest parsePartialFrom(
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

  public static com.google.protobuf.Parser<SearchTenantRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SearchTenantRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.SearchTenantRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

