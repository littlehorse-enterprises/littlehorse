// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Search filters for ScheduledWfRun's
 * </pre>
 *
 * Protobuf type {@code littlehorse.SearchScheduledWfRunRequest}
 */
public final class SearchScheduledWfRunRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.SearchScheduledWfRunRequest)
    SearchScheduledWfRunRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use SearchScheduledWfRunRequest.newBuilder() to construct.
  private SearchScheduledWfRunRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private SearchScheduledWfRunRequest() {
    wfSpecName_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new SearchScheduledWfRunRequest();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchScheduledWfRunRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchScheduledWfRunRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest.class, io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest.Builder.class);
  }

  private int bitField0_;
  public static final int WF_SPEC_NAME_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object wfSpecName_ = "";
  /**
   * <pre>
   * The name of the WfSpec to filter
   * </pre>
   *
   * <code>string wf_spec_name = 1;</code>
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
   * <pre>
   * The name of the WfSpec to filter
   * </pre>
   *
   * <code>string wf_spec_name = 1;</code>
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

  public static final int MAJOR_VERSION_FIELD_NUMBER = 2;
  private int majorVersion_ = 0;
  /**
   * <pre>
   * The major version of the WfSpec to filter
   * </pre>
   *
   * <code>optional int32 major_version = 2;</code>
   * @return Whether the majorVersion field is set.
   */
  @java.lang.Override
  public boolean hasMajorVersion() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * The major version of the WfSpec to filter
   * </pre>
   *
   * <code>optional int32 major_version = 2;</code>
   * @return The majorVersion.
   */
  @java.lang.Override
  public int getMajorVersion() {
    return majorVersion_;
  }

  public static final int REVISION_FIELD_NUMBER = 3;
  private int revision_ = 0;
  /**
   * <pre>
   * The revision number of the WfSpec to filter
   * </pre>
   *
   * <code>optional int32 revision = 3;</code>
   * @return Whether the revision field is set.
   */
  @java.lang.Override
  public boolean hasRevision() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <pre>
   * The revision number of the WfSpec to filter
   * </pre>
   *
   * <code>optional int32 revision = 3;</code>
   * @return The revision.
   */
  @java.lang.Override
  public int getRevision() {
    return revision_;
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(wfSpecName_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, wfSpecName_);
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      output.writeInt32(2, majorVersion_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      output.writeInt32(3, revision_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(wfSpecName_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, wfSpecName_);
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, majorVersion_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(3, revision_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest other = (io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest) obj;

    if (!getWfSpecName()
        .equals(other.getWfSpecName())) return false;
    if (hasMajorVersion() != other.hasMajorVersion()) return false;
    if (hasMajorVersion()) {
      if (getMajorVersion()
          != other.getMajorVersion()) return false;
    }
    if (hasRevision() != other.hasRevision()) return false;
    if (hasRevision()) {
      if (getRevision()
          != other.getRevision()) return false;
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
    hash = (37 * hash) + WF_SPEC_NAME_FIELD_NUMBER;
    hash = (53 * hash) + getWfSpecName().hashCode();
    if (hasMajorVersion()) {
      hash = (37 * hash) + MAJOR_VERSION_FIELD_NUMBER;
      hash = (53 * hash) + getMajorVersion();
    }
    if (hasRevision()) {
      hash = (37 * hash) + REVISION_FIELD_NUMBER;
      hash = (53 * hash) + getRevision();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest prototype) {
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
   * Search filters for ScheduledWfRun's
   * </pre>
   *
   * Protobuf type {@code littlehorse.SearchScheduledWfRunRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.SearchScheduledWfRunRequest)
      io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchScheduledWfRunRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchScheduledWfRunRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest.class, io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest.newBuilder()
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
      wfSpecName_ = "";
      majorVersion_ = 0;
      revision_ = 0;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_SearchScheduledWfRunRequest_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest build() {
      io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest buildPartial() {
      io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest result = new io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.wfSpecName_ = wfSpecName_;
      }
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.majorVersion_ = majorVersion_;
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.revision_ = revision_;
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
      if (other instanceof io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest) {
        return mergeFrom((io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest other) {
      if (other == io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest.getDefaultInstance()) return this;
      if (!other.getWfSpecName().isEmpty()) {
        wfSpecName_ = other.wfSpecName_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (other.hasMajorVersion()) {
        setMajorVersion(other.getMajorVersion());
      }
      if (other.hasRevision()) {
        setRevision(other.getRevision());
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
              wfSpecName_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 16: {
              majorVersion_ = input.readInt32();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
            case 24: {
              revision_ = input.readInt32();
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

    private java.lang.Object wfSpecName_ = "";
    /**
     * <pre>
     * The name of the WfSpec to filter
     * </pre>
     *
     * <code>string wf_spec_name = 1;</code>
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
     * <pre>
     * The name of the WfSpec to filter
     * </pre>
     *
     * <code>string wf_spec_name = 1;</code>
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
     * <pre>
     * The name of the WfSpec to filter
     * </pre>
     *
     * <code>string wf_spec_name = 1;</code>
     * @param value The wfSpecName to set.
     * @return This builder for chaining.
     */
    public Builder setWfSpecName(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      wfSpecName_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The name of the WfSpec to filter
     * </pre>
     *
     * <code>string wf_spec_name = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearWfSpecName() {
      wfSpecName_ = getDefaultInstance().getWfSpecName();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The name of the WfSpec to filter
     * </pre>
     *
     * <code>string wf_spec_name = 1;</code>
     * @param value The bytes for wfSpecName to set.
     * @return This builder for chaining.
     */
    public Builder setWfSpecNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      wfSpecName_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private int majorVersion_ ;
    /**
     * <pre>
     * The major version of the WfSpec to filter
     * </pre>
     *
     * <code>optional int32 major_version = 2;</code>
     * @return Whether the majorVersion field is set.
     */
    @java.lang.Override
    public boolean hasMajorVersion() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * The major version of the WfSpec to filter
     * </pre>
     *
     * <code>optional int32 major_version = 2;</code>
     * @return The majorVersion.
     */
    @java.lang.Override
    public int getMajorVersion() {
      return majorVersion_;
    }
    /**
     * <pre>
     * The major version of the WfSpec to filter
     * </pre>
     *
     * <code>optional int32 major_version = 2;</code>
     * @param value The majorVersion to set.
     * @return This builder for chaining.
     */
    public Builder setMajorVersion(int value) {

      majorVersion_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The major version of the WfSpec to filter
     * </pre>
     *
     * <code>optional int32 major_version = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearMajorVersion() {
      bitField0_ = (bitField0_ & ~0x00000002);
      majorVersion_ = 0;
      onChanged();
      return this;
    }

    private int revision_ ;
    /**
     * <pre>
     * The revision number of the WfSpec to filter
     * </pre>
     *
     * <code>optional int32 revision = 3;</code>
     * @return Whether the revision field is set.
     */
    @java.lang.Override
    public boolean hasRevision() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <pre>
     * The revision number of the WfSpec to filter
     * </pre>
     *
     * <code>optional int32 revision = 3;</code>
     * @return The revision.
     */
    @java.lang.Override
    public int getRevision() {
      return revision_;
    }
    /**
     * <pre>
     * The revision number of the WfSpec to filter
     * </pre>
     *
     * <code>optional int32 revision = 3;</code>
     * @param value The revision to set.
     * @return This builder for chaining.
     */
    public Builder setRevision(int value) {

      revision_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The revision number of the WfSpec to filter
     * </pre>
     *
     * <code>optional int32 revision = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearRevision() {
      bitField0_ = (bitField0_ & ~0x00000004);
      revision_ = 0;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.SearchScheduledWfRunRequest)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.SearchScheduledWfRunRequest)
  private static final io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest();
  }

  public static io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SearchScheduledWfRunRequest>
      PARSER = new com.google.protobuf.AbstractParser<SearchScheduledWfRunRequest>() {
    @java.lang.Override
    public SearchScheduledWfRunRequest parsePartialFrom(
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

  public static com.google.protobuf.Parser<SearchScheduledWfRunRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SearchScheduledWfRunRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

