// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.jlib.common.proto;

/**
 * Protobuf type {@code littlehorse.SearchWfSpecPb}
 */
public final class SearchWfSpecPb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.SearchWfSpecPb)
    SearchWfSpecPbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use SearchWfSpecPb.newBuilder() to construct.
  private SearchWfSpecPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private SearchWfSpecPb() {
    bookmark_ = com.google.protobuf.ByteString.EMPTY;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new SearchWfSpecPb();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.jlib.common.proto.Service.internal_static_littlehorse_SearchWfSpecPb_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.jlib.common.proto.Service.internal_static_littlehorse_SearchWfSpecPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.jlib.common.proto.SearchWfSpecPb.class, io.littlehorse.jlib.common.proto.SearchWfSpecPb.Builder.class);
  }

  private int bitField0_;
  private int wfSpecCriteriaCase_ = 0;
  private java.lang.Object wfSpecCriteria_;
  public enum WfSpecCriteriaCase
      implements com.google.protobuf.Internal.EnumLite,
          com.google.protobuf.AbstractMessage.InternalOneOfEnum {
    NAME(3),
    PREFIX(4),
    TASK_DEF_NAME(5),
    WFSPECCRITERIA_NOT_SET(0);
    private final int value;
    private WfSpecCriteriaCase(int value) {
      this.value = value;
    }
    /**
     * @param value The number of the enum to look for.
     * @return The enum associated with the given number.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static WfSpecCriteriaCase valueOf(int value) {
      return forNumber(value);
    }

    public static WfSpecCriteriaCase forNumber(int value) {
      switch (value) {
        case 3: return NAME;
        case 4: return PREFIX;
        case 5: return TASK_DEF_NAME;
        case 0: return WFSPECCRITERIA_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  public WfSpecCriteriaCase
  getWfSpecCriteriaCase() {
    return WfSpecCriteriaCase.forNumber(
        wfSpecCriteriaCase_);
  }

  public static final int BOOKMARK_FIELD_NUMBER = 1;
  private com.google.protobuf.ByteString bookmark_ = com.google.protobuf.ByteString.EMPTY;
  /**
   * <code>optional bytes bookmark = 1;</code>
   * @return Whether the bookmark field is set.
   */
  @java.lang.Override
  public boolean hasBookmark() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
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
   * <code>optional int32 limit = 2;</code>
   * @return Whether the limit field is set.
   */
  @java.lang.Override
  public boolean hasLimit() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional int32 limit = 2;</code>
   * @return The limit.
   */
  @java.lang.Override
  public int getLimit() {
    return limit_;
  }

  public static final int NAME_FIELD_NUMBER = 3;
  /**
   * <code>string name = 3;</code>
   * @return Whether the name field is set.
   */
  public boolean hasName() {
    return wfSpecCriteriaCase_ == 3;
  }
  /**
   * <code>string name = 3;</code>
   * @return The name.
   */
  public java.lang.String getName() {
    java.lang.Object ref = "";
    if (wfSpecCriteriaCase_ == 3) {
      ref = wfSpecCriteria_;
    }
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      if (wfSpecCriteriaCase_ == 3) {
        wfSpecCriteria_ = s;
      }
      return s;
    }
  }
  /**
   * <code>string name = 3;</code>
   * @return The bytes for name.
   */
  public com.google.protobuf.ByteString
      getNameBytes() {
    java.lang.Object ref = "";
    if (wfSpecCriteriaCase_ == 3) {
      ref = wfSpecCriteria_;
    }
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      if (wfSpecCriteriaCase_ == 3) {
        wfSpecCriteria_ = b;
      }
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int PREFIX_FIELD_NUMBER = 4;
  /**
   * <code>string prefix = 4;</code>
   * @return Whether the prefix field is set.
   */
  public boolean hasPrefix() {
    return wfSpecCriteriaCase_ == 4;
  }
  /**
   * <code>string prefix = 4;</code>
   * @return The prefix.
   */
  public java.lang.String getPrefix() {
    java.lang.Object ref = "";
    if (wfSpecCriteriaCase_ == 4) {
      ref = wfSpecCriteria_;
    }
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      if (wfSpecCriteriaCase_ == 4) {
        wfSpecCriteria_ = s;
      }
      return s;
    }
  }
  /**
   * <code>string prefix = 4;</code>
   * @return The bytes for prefix.
   */
  public com.google.protobuf.ByteString
      getPrefixBytes() {
    java.lang.Object ref = "";
    if (wfSpecCriteriaCase_ == 4) {
      ref = wfSpecCriteria_;
    }
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      if (wfSpecCriteriaCase_ == 4) {
        wfSpecCriteria_ = b;
      }
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int TASK_DEF_NAME_FIELD_NUMBER = 5;
  /**
   * <code>string task_def_name = 5;</code>
   * @return Whether the taskDefName field is set.
   */
  public boolean hasTaskDefName() {
    return wfSpecCriteriaCase_ == 5;
  }
  /**
   * <code>string task_def_name = 5;</code>
   * @return The taskDefName.
   */
  public java.lang.String getTaskDefName() {
    java.lang.Object ref = "";
    if (wfSpecCriteriaCase_ == 5) {
      ref = wfSpecCriteria_;
    }
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      if (wfSpecCriteriaCase_ == 5) {
        wfSpecCriteria_ = s;
      }
      return s;
    }
  }
  /**
   * <code>string task_def_name = 5;</code>
   * @return The bytes for taskDefName.
   */
  public com.google.protobuf.ByteString
      getTaskDefNameBytes() {
    java.lang.Object ref = "";
    if (wfSpecCriteriaCase_ == 5) {
      ref = wfSpecCriteria_;
    }
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      if (wfSpecCriteriaCase_ == 5) {
        wfSpecCriteria_ = b;
      }
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
    if (wfSpecCriteriaCase_ == 3) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, wfSpecCriteria_);
    }
    if (wfSpecCriteriaCase_ == 4) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 4, wfSpecCriteria_);
    }
    if (wfSpecCriteriaCase_ == 5) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, wfSpecCriteria_);
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
    if (wfSpecCriteriaCase_ == 3) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, wfSpecCriteria_);
    }
    if (wfSpecCriteriaCase_ == 4) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, wfSpecCriteria_);
    }
    if (wfSpecCriteriaCase_ == 5) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, wfSpecCriteria_);
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
    if (!(obj instanceof io.littlehorse.jlib.common.proto.SearchWfSpecPb)) {
      return super.equals(obj);
    }
    io.littlehorse.jlib.common.proto.SearchWfSpecPb other = (io.littlehorse.jlib.common.proto.SearchWfSpecPb) obj;

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
    if (!getWfSpecCriteriaCase().equals(other.getWfSpecCriteriaCase())) return false;
    switch (wfSpecCriteriaCase_) {
      case 3:
        if (!getName()
            .equals(other.getName())) return false;
        break;
      case 4:
        if (!getPrefix()
            .equals(other.getPrefix())) return false;
        break;
      case 5:
        if (!getTaskDefName()
            .equals(other.getTaskDefName())) return false;
        break;
      case 0:
      default:
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
    switch (wfSpecCriteriaCase_) {
      case 3:
        hash = (37 * hash) + NAME_FIELD_NUMBER;
        hash = (53 * hash) + getName().hashCode();
        break;
      case 4:
        hash = (37 * hash) + PREFIX_FIELD_NUMBER;
        hash = (53 * hash) + getPrefix().hashCode();
        break;
      case 5:
        hash = (37 * hash) + TASK_DEF_NAME_FIELD_NUMBER;
        hash = (53 * hash) + getTaskDefName().hashCode();
        break;
      case 0:
      default:
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.jlib.common.proto.SearchWfSpecPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.jlib.common.proto.SearchWfSpecPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.jlib.common.proto.SearchWfSpecPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.jlib.common.proto.SearchWfSpecPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.jlib.common.proto.SearchWfSpecPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.jlib.common.proto.SearchWfSpecPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.jlib.common.proto.SearchWfSpecPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.jlib.common.proto.SearchWfSpecPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.jlib.common.proto.SearchWfSpecPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static io.littlehorse.jlib.common.proto.SearchWfSpecPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.jlib.common.proto.SearchWfSpecPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.jlib.common.proto.SearchWfSpecPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.jlib.common.proto.SearchWfSpecPb prototype) {
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
   * Protobuf type {@code littlehorse.SearchWfSpecPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.SearchWfSpecPb)
      io.littlehorse.jlib.common.proto.SearchWfSpecPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.jlib.common.proto.Service.internal_static_littlehorse_SearchWfSpecPb_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.jlib.common.proto.Service.internal_static_littlehorse_SearchWfSpecPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.jlib.common.proto.SearchWfSpecPb.class, io.littlehorse.jlib.common.proto.SearchWfSpecPb.Builder.class);
    }

    // Construct using io.littlehorse.jlib.common.proto.SearchWfSpecPb.newBuilder()
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
      bookmark_ = com.google.protobuf.ByteString.EMPTY;
      limit_ = 0;
      wfSpecCriteriaCase_ = 0;
      wfSpecCriteria_ = null;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.jlib.common.proto.Service.internal_static_littlehorse_SearchWfSpecPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.jlib.common.proto.SearchWfSpecPb getDefaultInstanceForType() {
      return io.littlehorse.jlib.common.proto.SearchWfSpecPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.jlib.common.proto.SearchWfSpecPb build() {
      io.littlehorse.jlib.common.proto.SearchWfSpecPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.jlib.common.proto.SearchWfSpecPb buildPartial() {
      io.littlehorse.jlib.common.proto.SearchWfSpecPb result = new io.littlehorse.jlib.common.proto.SearchWfSpecPb(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      buildPartialOneofs(result);
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.jlib.common.proto.SearchWfSpecPb result) {
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
      result.bitField0_ |= to_bitField0_;
    }

    private void buildPartialOneofs(io.littlehorse.jlib.common.proto.SearchWfSpecPb result) {
      result.wfSpecCriteriaCase_ = wfSpecCriteriaCase_;
      result.wfSpecCriteria_ = this.wfSpecCriteria_;
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
      if (other instanceof io.littlehorse.jlib.common.proto.SearchWfSpecPb) {
        return mergeFrom((io.littlehorse.jlib.common.proto.SearchWfSpecPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.jlib.common.proto.SearchWfSpecPb other) {
      if (other == io.littlehorse.jlib.common.proto.SearchWfSpecPb.getDefaultInstance()) return this;
      if (other.hasBookmark()) {
        setBookmark(other.getBookmark());
      }
      if (other.hasLimit()) {
        setLimit(other.getLimit());
      }
      switch (other.getWfSpecCriteriaCase()) {
        case NAME: {
          wfSpecCriteriaCase_ = 3;
          wfSpecCriteria_ = other.wfSpecCriteria_;
          onChanged();
          break;
        }
        case PREFIX: {
          wfSpecCriteriaCase_ = 4;
          wfSpecCriteria_ = other.wfSpecCriteria_;
          onChanged();
          break;
        }
        case TASK_DEF_NAME: {
          wfSpecCriteriaCase_ = 5;
          wfSpecCriteria_ = other.wfSpecCriteria_;
          onChanged();
          break;
        }
        case WFSPECCRITERIA_NOT_SET: {
          break;
        }
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
              java.lang.String s = input.readStringRequireUtf8();
              wfSpecCriteriaCase_ = 3;
              wfSpecCriteria_ = s;
              break;
            } // case 26
            case 34: {
              java.lang.String s = input.readStringRequireUtf8();
              wfSpecCriteriaCase_ = 4;
              wfSpecCriteria_ = s;
              break;
            } // case 34
            case 42: {
              java.lang.String s = input.readStringRequireUtf8();
              wfSpecCriteriaCase_ = 5;
              wfSpecCriteria_ = s;
              break;
            } // case 42
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
    private int wfSpecCriteriaCase_ = 0;
    private java.lang.Object wfSpecCriteria_;
    public WfSpecCriteriaCase
        getWfSpecCriteriaCase() {
      return WfSpecCriteriaCase.forNumber(
          wfSpecCriteriaCase_);
    }

    public Builder clearWfSpecCriteria() {
      wfSpecCriteriaCase_ = 0;
      wfSpecCriteria_ = null;
      onChanged();
      return this;
    }

    private int bitField0_;

    private com.google.protobuf.ByteString bookmark_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>optional bytes bookmark = 1;</code>
     * @return Whether the bookmark field is set.
     */
    @java.lang.Override
    public boolean hasBookmark() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>optional bytes bookmark = 1;</code>
     * @return The bookmark.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getBookmark() {
      return bookmark_;
    }
    /**
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
     * <code>optional int32 limit = 2;</code>
     * @return Whether the limit field is set.
     */
    @java.lang.Override
    public boolean hasLimit() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>optional int32 limit = 2;</code>
     * @return The limit.
     */
    @java.lang.Override
    public int getLimit() {
      return limit_;
    }
    /**
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
     * <code>optional int32 limit = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearLimit() {
      bitField0_ = (bitField0_ & ~0x00000002);
      limit_ = 0;
      onChanged();
      return this;
    }

    /**
     * <code>string name = 3;</code>
     * @return Whether the name field is set.
     */
    @java.lang.Override
    public boolean hasName() {
      return wfSpecCriteriaCase_ == 3;
    }
    /**
     * <code>string name = 3;</code>
     * @return The name.
     */
    @java.lang.Override
    public java.lang.String getName() {
      java.lang.Object ref = "";
      if (wfSpecCriteriaCase_ == 3) {
        ref = wfSpecCriteria_;
      }
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (wfSpecCriteriaCase_ == 3) {
          wfSpecCriteria_ = s;
        }
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string name = 3;</code>
     * @return The bytes for name.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getNameBytes() {
      java.lang.Object ref = "";
      if (wfSpecCriteriaCase_ == 3) {
        ref = wfSpecCriteria_;
      }
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        if (wfSpecCriteriaCase_ == 3) {
          wfSpecCriteria_ = b;
        }
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string name = 3;</code>
     * @param value The name to set.
     * @return This builder for chaining.
     */
    public Builder setName(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      wfSpecCriteriaCase_ = 3;
      wfSpecCriteria_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string name = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearName() {
      if (wfSpecCriteriaCase_ == 3) {
        wfSpecCriteriaCase_ = 0;
        wfSpecCriteria_ = null;
        onChanged();
      }
      return this;
    }
    /**
     * <code>string name = 3;</code>
     * @param value The bytes for name to set.
     * @return This builder for chaining.
     */
    public Builder setNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      wfSpecCriteriaCase_ = 3;
      wfSpecCriteria_ = value;
      onChanged();
      return this;
    }

    /**
     * <code>string prefix = 4;</code>
     * @return Whether the prefix field is set.
     */
    @java.lang.Override
    public boolean hasPrefix() {
      return wfSpecCriteriaCase_ == 4;
    }
    /**
     * <code>string prefix = 4;</code>
     * @return The prefix.
     */
    @java.lang.Override
    public java.lang.String getPrefix() {
      java.lang.Object ref = "";
      if (wfSpecCriteriaCase_ == 4) {
        ref = wfSpecCriteria_;
      }
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (wfSpecCriteriaCase_ == 4) {
          wfSpecCriteria_ = s;
        }
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string prefix = 4;</code>
     * @return The bytes for prefix.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getPrefixBytes() {
      java.lang.Object ref = "";
      if (wfSpecCriteriaCase_ == 4) {
        ref = wfSpecCriteria_;
      }
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        if (wfSpecCriteriaCase_ == 4) {
          wfSpecCriteria_ = b;
        }
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string prefix = 4;</code>
     * @param value The prefix to set.
     * @return This builder for chaining.
     */
    public Builder setPrefix(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      wfSpecCriteriaCase_ = 4;
      wfSpecCriteria_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string prefix = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearPrefix() {
      if (wfSpecCriteriaCase_ == 4) {
        wfSpecCriteriaCase_ = 0;
        wfSpecCriteria_ = null;
        onChanged();
      }
      return this;
    }
    /**
     * <code>string prefix = 4;</code>
     * @param value The bytes for prefix to set.
     * @return This builder for chaining.
     */
    public Builder setPrefixBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      wfSpecCriteriaCase_ = 4;
      wfSpecCriteria_ = value;
      onChanged();
      return this;
    }

    /**
     * <code>string task_def_name = 5;</code>
     * @return Whether the taskDefName field is set.
     */
    @java.lang.Override
    public boolean hasTaskDefName() {
      return wfSpecCriteriaCase_ == 5;
    }
    /**
     * <code>string task_def_name = 5;</code>
     * @return The taskDefName.
     */
    @java.lang.Override
    public java.lang.String getTaskDefName() {
      java.lang.Object ref = "";
      if (wfSpecCriteriaCase_ == 5) {
        ref = wfSpecCriteria_;
      }
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (wfSpecCriteriaCase_ == 5) {
          wfSpecCriteria_ = s;
        }
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string task_def_name = 5;</code>
     * @return The bytes for taskDefName.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getTaskDefNameBytes() {
      java.lang.Object ref = "";
      if (wfSpecCriteriaCase_ == 5) {
        ref = wfSpecCriteria_;
      }
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        if (wfSpecCriteriaCase_ == 5) {
          wfSpecCriteria_ = b;
        }
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string task_def_name = 5;</code>
     * @param value The taskDefName to set.
     * @return This builder for chaining.
     */
    public Builder setTaskDefName(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      wfSpecCriteriaCase_ = 5;
      wfSpecCriteria_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string task_def_name = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearTaskDefName() {
      if (wfSpecCriteriaCase_ == 5) {
        wfSpecCriteriaCase_ = 0;
        wfSpecCriteria_ = null;
        onChanged();
      }
      return this;
    }
    /**
     * <code>string task_def_name = 5;</code>
     * @param value The bytes for taskDefName to set.
     * @return This builder for chaining.
     */
    public Builder setTaskDefNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      wfSpecCriteriaCase_ = 5;
      wfSpecCriteria_ = value;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.SearchWfSpecPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.SearchWfSpecPb)
  private static final io.littlehorse.jlib.common.proto.SearchWfSpecPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.jlib.common.proto.SearchWfSpecPb();
  }

  public static io.littlehorse.jlib.common.proto.SearchWfSpecPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SearchWfSpecPb>
      PARSER = new com.google.protobuf.AbstractParser<SearchWfSpecPb>() {
    @java.lang.Override
    public SearchWfSpecPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<SearchWfSpecPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SearchWfSpecPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.jlib.common.proto.SearchWfSpecPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

