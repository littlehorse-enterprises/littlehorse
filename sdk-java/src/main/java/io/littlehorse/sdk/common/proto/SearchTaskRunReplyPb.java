// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.SearchTaskRunReplyPb}
 */
public final class SearchTaskRunReplyPb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.SearchTaskRunReplyPb)
    SearchTaskRunReplyPbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use SearchTaskRunReplyPb.newBuilder() to construct.
  private SearchTaskRunReplyPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private SearchTaskRunReplyPb() {
    code_ = 0;
    message_ = "";
    results_ = java.util.Collections.emptyList();
    bookmark_ = com.google.protobuf.ByteString.EMPTY;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new SearchTaskRunReplyPb();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchTaskRunReplyPb_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchTaskRunReplyPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb.class, io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb.Builder.class);
  }

  private int bitField0_;
  public static final int CODE_FIELD_NUMBER = 1;
  private int code_ = 0;
  /**
   * <code>.littlehorse.LHResponseCodePb code = 1;</code>
   * @return The enum numeric value on the wire for code.
   */
  @java.lang.Override public int getCodeValue() {
    return code_;
  }
  /**
   * <code>.littlehorse.LHResponseCodePb code = 1;</code>
   * @return The code.
   */
  @java.lang.Override public io.littlehorse.sdk.common.proto.LHResponseCodePb getCode() {
    io.littlehorse.sdk.common.proto.LHResponseCodePb result = io.littlehorse.sdk.common.proto.LHResponseCodePb.forNumber(code_);
    return result == null ? io.littlehorse.sdk.common.proto.LHResponseCodePb.UNRECOGNIZED : result;
  }

  public static final int MESSAGE_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private volatile java.lang.Object message_ = "";
  /**
   * <code>optional string message = 2;</code>
   * @return Whether the message field is set.
   */
  @java.lang.Override
  public boolean hasMessage() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional string message = 2;</code>
   * @return The message.
   */
  @java.lang.Override
  public java.lang.String getMessage() {
    java.lang.Object ref = message_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      message_ = s;
      return s;
    }
  }
  /**
   * <code>optional string message = 2;</code>
   * @return The bytes for message.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getMessageBytes() {
    java.lang.Object ref = message_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      message_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int RESULTS_FIELD_NUMBER = 3;
  @SuppressWarnings("serial")
  private java.util.List<io.littlehorse.sdk.common.proto.TaskRunIdPb> results_;
  /**
   * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
   */
  @java.lang.Override
  public java.util.List<io.littlehorse.sdk.common.proto.TaskRunIdPb> getResultsList() {
    return results_;
  }
  /**
   * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
   */
  @java.lang.Override
  public java.util.List<? extends io.littlehorse.sdk.common.proto.TaskRunIdPbOrBuilder> 
      getResultsOrBuilderList() {
    return results_;
  }
  /**
   * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
   */
  @java.lang.Override
  public int getResultsCount() {
    return results_.size();
  }
  /**
   * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TaskRunIdPb getResults(int index) {
    return results_.get(index);
  }
  /**
   * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TaskRunIdPbOrBuilder getResultsOrBuilder(
      int index) {
    return results_.get(index);
  }

  public static final int BOOKMARK_FIELD_NUMBER = 4;
  private com.google.protobuf.ByteString bookmark_ = com.google.protobuf.ByteString.EMPTY;
  /**
   * <code>optional bytes bookmark = 4;</code>
   * @return Whether the bookmark field is set.
   */
  @java.lang.Override
  public boolean hasBookmark() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional bytes bookmark = 4;</code>
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
    if (code_ != io.littlehorse.sdk.common.proto.LHResponseCodePb.OK.getNumber()) {
      output.writeEnum(1, code_);
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, message_);
    }
    for (int i = 0; i < results_.size(); i++) {
      output.writeMessage(3, results_.get(i));
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      output.writeBytes(4, bookmark_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (code_ != io.littlehorse.sdk.common.proto.LHResponseCodePb.OK.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(1, code_);
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, message_);
    }
    for (int i = 0; i < results_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, results_.get(i));
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(4, bookmark_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb other = (io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb) obj;

    if (code_ != other.code_) return false;
    if (hasMessage() != other.hasMessage()) return false;
    if (hasMessage()) {
      if (!getMessage()
          .equals(other.getMessage())) return false;
    }
    if (!getResultsList()
        .equals(other.getResultsList())) return false;
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
    hash = (37 * hash) + CODE_FIELD_NUMBER;
    hash = (53 * hash) + code_;
    if (hasMessage()) {
      hash = (37 * hash) + MESSAGE_FIELD_NUMBER;
      hash = (53 * hash) + getMessage().hashCode();
    }
    if (getResultsCount() > 0) {
      hash = (37 * hash) + RESULTS_FIELD_NUMBER;
      hash = (53 * hash) + getResultsList().hashCode();
    }
    if (hasBookmark()) {
      hash = (37 * hash) + BOOKMARK_FIELD_NUMBER;
      hash = (53 * hash) + getBookmark().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb prototype) {
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
   * Protobuf type {@code littlehorse.SearchTaskRunReplyPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.SearchTaskRunReplyPb)
      io.littlehorse.sdk.common.proto.SearchTaskRunReplyPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchTaskRunReplyPb_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchTaskRunReplyPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb.class, io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb.newBuilder()
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
      code_ = 0;
      message_ = "";
      if (resultsBuilder_ == null) {
        results_ = java.util.Collections.emptyList();
      } else {
        results_ = null;
        resultsBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000004);
      bookmark_ = com.google.protobuf.ByteString.EMPTY;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchTaskRunReplyPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb build() {
      io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb buildPartial() {
      io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb result = new io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb result) {
      if (resultsBuilder_ == null) {
        if (((bitField0_ & 0x00000004) != 0)) {
          results_ = java.util.Collections.unmodifiableList(results_);
          bitField0_ = (bitField0_ & ~0x00000004);
        }
        result.results_ = results_;
      } else {
        result.results_ = resultsBuilder_.build();
      }
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.code_ = code_;
      }
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.message_ = message_;
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        result.bookmark_ = bookmark_;
        to_bitField0_ |= 0x00000002;
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
      if (other instanceof io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb) {
        return mergeFrom((io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb other) {
      if (other == io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb.getDefaultInstance()) return this;
      if (other.code_ != 0) {
        setCodeValue(other.getCodeValue());
      }
      if (other.hasMessage()) {
        message_ = other.message_;
        bitField0_ |= 0x00000002;
        onChanged();
      }
      if (resultsBuilder_ == null) {
        if (!other.results_.isEmpty()) {
          if (results_.isEmpty()) {
            results_ = other.results_;
            bitField0_ = (bitField0_ & ~0x00000004);
          } else {
            ensureResultsIsMutable();
            results_.addAll(other.results_);
          }
          onChanged();
        }
      } else {
        if (!other.results_.isEmpty()) {
          if (resultsBuilder_.isEmpty()) {
            resultsBuilder_.dispose();
            resultsBuilder_ = null;
            results_ = other.results_;
            bitField0_ = (bitField0_ & ~0x00000004);
            resultsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getResultsFieldBuilder() : null;
          } else {
            resultsBuilder_.addAllMessages(other.results_);
          }
        }
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
              code_ = input.readEnum();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 18: {
              message_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 26: {
              io.littlehorse.sdk.common.proto.TaskRunIdPb m =
                  input.readMessage(
                      io.littlehorse.sdk.common.proto.TaskRunIdPb.parser(),
                      extensionRegistry);
              if (resultsBuilder_ == null) {
                ensureResultsIsMutable();
                results_.add(m);
              } else {
                resultsBuilder_.addMessage(m);
              }
              break;
            } // case 26
            case 34: {
              bookmark_ = input.readBytes();
              bitField0_ |= 0x00000008;
              break;
            } // case 34
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

    private int code_ = 0;
    /**
     * <code>.littlehorse.LHResponseCodePb code = 1;</code>
     * @return The enum numeric value on the wire for code.
     */
    @java.lang.Override public int getCodeValue() {
      return code_;
    }
    /**
     * <code>.littlehorse.LHResponseCodePb code = 1;</code>
     * @param value The enum numeric value on the wire for code to set.
     * @return This builder for chaining.
     */
    public Builder setCodeValue(int value) {
      code_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.LHResponseCodePb code = 1;</code>
     * @return The code.
     */
    @java.lang.Override
    public io.littlehorse.sdk.common.proto.LHResponseCodePb getCode() {
      io.littlehorse.sdk.common.proto.LHResponseCodePb result = io.littlehorse.sdk.common.proto.LHResponseCodePb.forNumber(code_);
      return result == null ? io.littlehorse.sdk.common.proto.LHResponseCodePb.UNRECOGNIZED : result;
    }
    /**
     * <code>.littlehorse.LHResponseCodePb code = 1;</code>
     * @param value The code to set.
     * @return This builder for chaining.
     */
    public Builder setCode(io.littlehorse.sdk.common.proto.LHResponseCodePb value) {
      if (value == null) {
        throw new NullPointerException();
      }
      bitField0_ |= 0x00000001;
      code_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.LHResponseCodePb code = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearCode() {
      bitField0_ = (bitField0_ & ~0x00000001);
      code_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object message_ = "";
    /**
     * <code>optional string message = 2;</code>
     * @return Whether the message field is set.
     */
    public boolean hasMessage() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>optional string message = 2;</code>
     * @return The message.
     */
    public java.lang.String getMessage() {
      java.lang.Object ref = message_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        message_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>optional string message = 2;</code>
     * @return The bytes for message.
     */
    public com.google.protobuf.ByteString
        getMessageBytes() {
      java.lang.Object ref = message_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        message_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>optional string message = 2;</code>
     * @param value The message to set.
     * @return This builder for chaining.
     */
    public Builder setMessage(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      message_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>optional string message = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearMessage() {
      message_ = getDefaultInstance().getMessage();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /**
     * <code>optional string message = 2;</code>
     * @param value The bytes for message to set.
     * @return This builder for chaining.
     */
    public Builder setMessageBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      message_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }

    private java.util.List<io.littlehorse.sdk.common.proto.TaskRunIdPb> results_ =
      java.util.Collections.emptyList();
    private void ensureResultsIsMutable() {
      if (!((bitField0_ & 0x00000004) != 0)) {
        results_ = new java.util.ArrayList<io.littlehorse.sdk.common.proto.TaskRunIdPb>(results_);
        bitField0_ |= 0x00000004;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.littlehorse.sdk.common.proto.TaskRunIdPb, io.littlehorse.sdk.common.proto.TaskRunIdPb.Builder, io.littlehorse.sdk.common.proto.TaskRunIdPbOrBuilder> resultsBuilder_;

    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public java.util.List<io.littlehorse.sdk.common.proto.TaskRunIdPb> getResultsList() {
      if (resultsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(results_);
      } else {
        return resultsBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public int getResultsCount() {
      if (resultsBuilder_ == null) {
        return results_.size();
      } else {
        return resultsBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public io.littlehorse.sdk.common.proto.TaskRunIdPb getResults(int index) {
      if (resultsBuilder_ == null) {
        return results_.get(index);
      } else {
        return resultsBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public Builder setResults(
        int index, io.littlehorse.sdk.common.proto.TaskRunIdPb value) {
      if (resultsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureResultsIsMutable();
        results_.set(index, value);
        onChanged();
      } else {
        resultsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public Builder setResults(
        int index, io.littlehorse.sdk.common.proto.TaskRunIdPb.Builder builderForValue) {
      if (resultsBuilder_ == null) {
        ensureResultsIsMutable();
        results_.set(index, builderForValue.build());
        onChanged();
      } else {
        resultsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public Builder addResults(io.littlehorse.sdk.common.proto.TaskRunIdPb value) {
      if (resultsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureResultsIsMutable();
        results_.add(value);
        onChanged();
      } else {
        resultsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public Builder addResults(
        int index, io.littlehorse.sdk.common.proto.TaskRunIdPb value) {
      if (resultsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureResultsIsMutable();
        results_.add(index, value);
        onChanged();
      } else {
        resultsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public Builder addResults(
        io.littlehorse.sdk.common.proto.TaskRunIdPb.Builder builderForValue) {
      if (resultsBuilder_ == null) {
        ensureResultsIsMutable();
        results_.add(builderForValue.build());
        onChanged();
      } else {
        resultsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public Builder addResults(
        int index, io.littlehorse.sdk.common.proto.TaskRunIdPb.Builder builderForValue) {
      if (resultsBuilder_ == null) {
        ensureResultsIsMutable();
        results_.add(index, builderForValue.build());
        onChanged();
      } else {
        resultsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public Builder addAllResults(
        java.lang.Iterable<? extends io.littlehorse.sdk.common.proto.TaskRunIdPb> values) {
      if (resultsBuilder_ == null) {
        ensureResultsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, results_);
        onChanged();
      } else {
        resultsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public Builder clearResults() {
      if (resultsBuilder_ == null) {
        results_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000004);
        onChanged();
      } else {
        resultsBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public Builder removeResults(int index) {
      if (resultsBuilder_ == null) {
        ensureResultsIsMutable();
        results_.remove(index);
        onChanged();
      } else {
        resultsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public io.littlehorse.sdk.common.proto.TaskRunIdPb.Builder getResultsBuilder(
        int index) {
      return getResultsFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public io.littlehorse.sdk.common.proto.TaskRunIdPbOrBuilder getResultsOrBuilder(
        int index) {
      if (resultsBuilder_ == null) {
        return results_.get(index);  } else {
        return resultsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public java.util.List<? extends io.littlehorse.sdk.common.proto.TaskRunIdPbOrBuilder> 
         getResultsOrBuilderList() {
      if (resultsBuilder_ != null) {
        return resultsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(results_);
      }
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public io.littlehorse.sdk.common.proto.TaskRunIdPb.Builder addResultsBuilder() {
      return getResultsFieldBuilder().addBuilder(
          io.littlehorse.sdk.common.proto.TaskRunIdPb.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public io.littlehorse.sdk.common.proto.TaskRunIdPb.Builder addResultsBuilder(
        int index) {
      return getResultsFieldBuilder().addBuilder(
          index, io.littlehorse.sdk.common.proto.TaskRunIdPb.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.TaskRunIdPb results = 3;</code>
     */
    public java.util.List<io.littlehorse.sdk.common.proto.TaskRunIdPb.Builder> 
         getResultsBuilderList() {
      return getResultsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.littlehorse.sdk.common.proto.TaskRunIdPb, io.littlehorse.sdk.common.proto.TaskRunIdPb.Builder, io.littlehorse.sdk.common.proto.TaskRunIdPbOrBuilder> 
        getResultsFieldBuilder() {
      if (resultsBuilder_ == null) {
        resultsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            io.littlehorse.sdk.common.proto.TaskRunIdPb, io.littlehorse.sdk.common.proto.TaskRunIdPb.Builder, io.littlehorse.sdk.common.proto.TaskRunIdPbOrBuilder>(
                results_,
                ((bitField0_ & 0x00000004) != 0),
                getParentForChildren(),
                isClean());
        results_ = null;
      }
      return resultsBuilder_;
    }

    private com.google.protobuf.ByteString bookmark_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>optional bytes bookmark = 4;</code>
     * @return Whether the bookmark field is set.
     */
    @java.lang.Override
    public boolean hasBookmark() {
      return ((bitField0_ & 0x00000008) != 0);
    }
    /**
     * <code>optional bytes bookmark = 4;</code>
     * @return The bookmark.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getBookmark() {
      return bookmark_;
    }
    /**
     * <code>optional bytes bookmark = 4;</code>
     * @param value The bookmark to set.
     * @return This builder for chaining.
     */
    public Builder setBookmark(com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      bookmark_ = value;
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>optional bytes bookmark = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearBookmark() {
      bitField0_ = (bitField0_ & ~0x00000008);
      bookmark_ = getDefaultInstance().getBookmark();
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


    // @@protoc_insertion_point(builder_scope:littlehorse.SearchTaskRunReplyPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.SearchTaskRunReplyPb)
  private static final io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb();
  }

  public static io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SearchTaskRunReplyPb>
      PARSER = new com.google.protobuf.AbstractParser<SearchTaskRunReplyPb>() {
    @java.lang.Override
    public SearchTaskRunReplyPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<SearchTaskRunReplyPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SearchTaskRunReplyPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

