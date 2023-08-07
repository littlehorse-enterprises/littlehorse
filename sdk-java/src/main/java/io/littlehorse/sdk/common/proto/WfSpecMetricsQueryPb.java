// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.WfSpecMetricsQueryPb}
 */
public final class WfSpecMetricsQueryPb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.WfSpecMetricsQueryPb)
    WfSpecMetricsQueryPbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use WfSpecMetricsQueryPb.newBuilder() to construct.
  private WfSpecMetricsQueryPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private WfSpecMetricsQueryPb() {
    windowType_ = 0;
    wfSpecName_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new WfSpecMetricsQueryPb();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_WfSpecMetricsQueryPb_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_WfSpecMetricsQueryPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb.class, io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb.Builder.class);
  }

  public static final int WINDOW_START_FIELD_NUMBER = 1;
  private com.google.protobuf.Timestamp windowStart_;
  /**
   * <code>.google.protobuf.Timestamp window_start = 1;</code>
   * @return Whether the windowStart field is set.
   */
  @java.lang.Override
  public boolean hasWindowStart() {
    return windowStart_ != null;
  }
  /**
   * <code>.google.protobuf.Timestamp window_start = 1;</code>
   * @return The windowStart.
   */
  @java.lang.Override
  public com.google.protobuf.Timestamp getWindowStart() {
    return windowStart_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : windowStart_;
  }
  /**
   * <code>.google.protobuf.Timestamp window_start = 1;</code>
   */
  @java.lang.Override
  public com.google.protobuf.TimestampOrBuilder getWindowStartOrBuilder() {
    return windowStart_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : windowStart_;
  }

  public static final int WINDOW_TYPE_FIELD_NUMBER = 2;
  private int windowType_ = 0;
  /**
   * <code>.littlehorse.MetricsWindowLengthPb window_type = 2;</code>
   * @return The enum numeric value on the wire for windowType.
   */
  @java.lang.Override public int getWindowTypeValue() {
    return windowType_;
  }
  /**
   * <code>.littlehorse.MetricsWindowLengthPb window_type = 2;</code>
   * @return The windowType.
   */
  @java.lang.Override public io.littlehorse.sdk.common.proto.MetricsWindowLengthPb getWindowType() {
    io.littlehorse.sdk.common.proto.MetricsWindowLengthPb result = io.littlehorse.sdk.common.proto.MetricsWindowLengthPb.forNumber(windowType_);
    return result == null ? io.littlehorse.sdk.common.proto.MetricsWindowLengthPb.UNRECOGNIZED : result;
  }

  public static final int WF_SPEC_NAME_FIELD_NUMBER = 3;
  @SuppressWarnings("serial")
  private volatile java.lang.Object wfSpecName_ = "";
  /**
   * <code>string wf_spec_name = 3;</code>
   * @return The wfSpecName.
   */
  @java.lang.Override
  public java.lang.String getWfSpecName() {
    java.lang.Object ref = wfSpecName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      wfSpecName_ = s;
      return s;
    }
  }
  /**
   * <code>string wf_spec_name = 3;</code>
   * @return The bytes for wfSpecName.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getWfSpecNameBytes() {
    java.lang.Object ref = wfSpecName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      wfSpecName_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int WF_SPEC_VERSION_FIELD_NUMBER = 4;
  private int wfSpecVersion_ = 0;
  /**
   * <code>int32 wf_spec_version = 4;</code>
   * @return The wfSpecVersion.
   */
  @java.lang.Override
  public int getWfSpecVersion() {
    return wfSpecVersion_;
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
    if (windowStart_ != null) {
      output.writeMessage(1, getWindowStart());
    }
    if (windowType_ != io.littlehorse.sdk.common.proto.MetricsWindowLengthPb.MINUTES_5.getNumber()) {
      output.writeEnum(2, windowType_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(wfSpecName_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, wfSpecName_);
    }
    if (wfSpecVersion_ != 0) {
      output.writeInt32(4, wfSpecVersion_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (windowStart_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getWindowStart());
    }
    if (windowType_ != io.littlehorse.sdk.common.proto.MetricsWindowLengthPb.MINUTES_5.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(2, windowType_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(wfSpecName_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, wfSpecName_);
    }
    if (wfSpecVersion_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(4, wfSpecVersion_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb other = (io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb) obj;

    if (hasWindowStart() != other.hasWindowStart()) return false;
    if (hasWindowStart()) {
      if (!getWindowStart()
          .equals(other.getWindowStart())) return false;
    }
    if (windowType_ != other.windowType_) return false;
    if (!getWfSpecName()
        .equals(other.getWfSpecName())) return false;
    if (getWfSpecVersion()
        != other.getWfSpecVersion()) return false;
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
    if (hasWindowStart()) {
      hash = (37 * hash) + WINDOW_START_FIELD_NUMBER;
      hash = (53 * hash) + getWindowStart().hashCode();
    }
    hash = (37 * hash) + WINDOW_TYPE_FIELD_NUMBER;
    hash = (53 * hash) + windowType_;
    hash = (37 * hash) + WF_SPEC_NAME_FIELD_NUMBER;
    hash = (53 * hash) + getWfSpecName().hashCode();
    hash = (37 * hash) + WF_SPEC_VERSION_FIELD_NUMBER;
    hash = (53 * hash) + getWfSpecVersion();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb prototype) {
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
   * Protobuf type {@code littlehorse.WfSpecMetricsQueryPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.WfSpecMetricsQueryPb)
      io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_WfSpecMetricsQueryPb_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_WfSpecMetricsQueryPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb.class, io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb.newBuilder()
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
      windowStart_ = null;
      if (windowStartBuilder_ != null) {
        windowStartBuilder_.dispose();
        windowStartBuilder_ = null;
      }
      windowType_ = 0;
      wfSpecName_ = "";
      wfSpecVersion_ = 0;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_WfSpecMetricsQueryPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb build() {
      io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb buildPartial() {
      io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb result = new io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.windowStart_ = windowStartBuilder_ == null
            ? windowStart_
            : windowStartBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.windowType_ = windowType_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.wfSpecName_ = wfSpecName_;
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        result.wfSpecVersion_ = wfSpecVersion_;
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
      if (other instanceof io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb) {
        return mergeFrom((io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb other) {
      if (other == io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb.getDefaultInstance()) return this;
      if (other.hasWindowStart()) {
        mergeWindowStart(other.getWindowStart());
      }
      if (other.windowType_ != 0) {
        setWindowTypeValue(other.getWindowTypeValue());
      }
      if (!other.getWfSpecName().isEmpty()) {
        wfSpecName_ = other.wfSpecName_;
        bitField0_ |= 0x00000004;
        onChanged();
      }
      if (other.getWfSpecVersion() != 0) {
        setWfSpecVersion(other.getWfSpecVersion());
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
                  getWindowStartFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 16: {
              windowType_ = input.readEnum();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
            case 26: {
              wfSpecName_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000004;
              break;
            } // case 26
            case 32: {
              wfSpecVersion_ = input.readInt32();
              bitField0_ |= 0x00000008;
              break;
            } // case 32
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

    private com.google.protobuf.Timestamp windowStart_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder> windowStartBuilder_;
    /**
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     * @return Whether the windowStart field is set.
     */
    public boolean hasWindowStart() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     * @return The windowStart.
     */
    public com.google.protobuf.Timestamp getWindowStart() {
      if (windowStartBuilder_ == null) {
        return windowStart_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : windowStart_;
      } else {
        return windowStartBuilder_.getMessage();
      }
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     */
    public Builder setWindowStart(com.google.protobuf.Timestamp value) {
      if (windowStartBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        windowStart_ = value;
      } else {
        windowStartBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     */
    public Builder setWindowStart(
        com.google.protobuf.Timestamp.Builder builderForValue) {
      if (windowStartBuilder_ == null) {
        windowStart_ = builderForValue.build();
      } else {
        windowStartBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     */
    public Builder mergeWindowStart(com.google.protobuf.Timestamp value) {
      if (windowStartBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          windowStart_ != null &&
          windowStart_ != com.google.protobuf.Timestamp.getDefaultInstance()) {
          getWindowStartBuilder().mergeFrom(value);
        } else {
          windowStart_ = value;
        }
      } else {
        windowStartBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     */
    public Builder clearWindowStart() {
      bitField0_ = (bitField0_ & ~0x00000001);
      windowStart_ = null;
      if (windowStartBuilder_ != null) {
        windowStartBuilder_.dispose();
        windowStartBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     */
    public com.google.protobuf.Timestamp.Builder getWindowStartBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getWindowStartFieldBuilder().getBuilder();
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     */
    public com.google.protobuf.TimestampOrBuilder getWindowStartOrBuilder() {
      if (windowStartBuilder_ != null) {
        return windowStartBuilder_.getMessageOrBuilder();
      } else {
        return windowStart_ == null ?
            com.google.protobuf.Timestamp.getDefaultInstance() : windowStart_;
      }
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder> 
        getWindowStartFieldBuilder() {
      if (windowStartBuilder_ == null) {
        windowStartBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder>(
                getWindowStart(),
                getParentForChildren(),
                isClean());
        windowStart_ = null;
      }
      return windowStartBuilder_;
    }

    private int windowType_ = 0;
    /**
     * <code>.littlehorse.MetricsWindowLengthPb window_type = 2;</code>
     * @return The enum numeric value on the wire for windowType.
     */
    @java.lang.Override public int getWindowTypeValue() {
      return windowType_;
    }
    /**
     * <code>.littlehorse.MetricsWindowLengthPb window_type = 2;</code>
     * @param value The enum numeric value on the wire for windowType to set.
     * @return This builder for chaining.
     */
    public Builder setWindowTypeValue(int value) {
      windowType_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.MetricsWindowLengthPb window_type = 2;</code>
     * @return The windowType.
     */
    @java.lang.Override
    public io.littlehorse.sdk.common.proto.MetricsWindowLengthPb getWindowType() {
      io.littlehorse.sdk.common.proto.MetricsWindowLengthPb result = io.littlehorse.sdk.common.proto.MetricsWindowLengthPb.forNumber(windowType_);
      return result == null ? io.littlehorse.sdk.common.proto.MetricsWindowLengthPb.UNRECOGNIZED : result;
    }
    /**
     * <code>.littlehorse.MetricsWindowLengthPb window_type = 2;</code>
     * @param value The windowType to set.
     * @return This builder for chaining.
     */
    public Builder setWindowType(io.littlehorse.sdk.common.proto.MetricsWindowLengthPb value) {
      if (value == null) {
        throw new NullPointerException();
      }
      bitField0_ |= 0x00000002;
      windowType_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.MetricsWindowLengthPb window_type = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearWindowType() {
      bitField0_ = (bitField0_ & ~0x00000002);
      windowType_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object wfSpecName_ = "";
    /**
     * <code>string wf_spec_name = 3;</code>
     * @return The wfSpecName.
     */
    public java.lang.String getWfSpecName() {
      java.lang.Object ref = wfSpecName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        wfSpecName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string wf_spec_name = 3;</code>
     * @return The bytes for wfSpecName.
     */
    public com.google.protobuf.ByteString
        getWfSpecNameBytes() {
      java.lang.Object ref = wfSpecName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        wfSpecName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string wf_spec_name = 3;</code>
     * @param value The wfSpecName to set.
     * @return This builder for chaining.
     */
    public Builder setWfSpecName(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      wfSpecName_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>string wf_spec_name = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearWfSpecName() {
      wfSpecName_ = getDefaultInstance().getWfSpecName();
      bitField0_ = (bitField0_ & ~0x00000004);
      onChanged();
      return this;
    }
    /**
     * <code>string wf_spec_name = 3;</code>
     * @param value The bytes for wfSpecName to set.
     * @return This builder for chaining.
     */
    public Builder setWfSpecNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      wfSpecName_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }

    private int wfSpecVersion_ ;
    /**
     * <code>int32 wf_spec_version = 4;</code>
     * @return The wfSpecVersion.
     */
    @java.lang.Override
    public int getWfSpecVersion() {
      return wfSpecVersion_;
    }
    /**
     * <code>int32 wf_spec_version = 4;</code>
     * @param value The wfSpecVersion to set.
     * @return This builder for chaining.
     */
    public Builder setWfSpecVersion(int value) {
      
      wfSpecVersion_ = value;
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>int32 wf_spec_version = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearWfSpecVersion() {
      bitField0_ = (bitField0_ & ~0x00000008);
      wfSpecVersion_ = 0;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.WfSpecMetricsQueryPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.WfSpecMetricsQueryPb)
  private static final io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb();
  }

  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<WfSpecMetricsQueryPb>
      PARSER = new com.google.protobuf.AbstractParser<WfSpecMetricsQueryPb>() {
    @java.lang.Override
    public WfSpecMetricsQueryPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<WfSpecMetricsQueryPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<WfSpecMetricsQueryPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

