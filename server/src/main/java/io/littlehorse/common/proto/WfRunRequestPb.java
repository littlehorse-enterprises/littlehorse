// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: internal_server.proto

package io.littlehorse.common.proto;

/**
 * <pre>
 * This section defines the "Command"
 * </pre>
 *
 * Protobuf type {@code littlehorse.WfRunRequestPb}
 */
public final class WfRunRequestPb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.WfRunRequestPb)
    WfRunRequestPbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use WfRunRequestPb.newBuilder() to construct.
  private WfRunRequestPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private WfRunRequestPb() {
    wfRunId_ = "";
    wfSpecId_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new WfRunRequestPb();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_WfRunRequestPb_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  @java.lang.Override
  protected com.google.protobuf.MapField internalGetMapField(
      int number) {
    switch (number) {
      case 3:
        return internalGetVariables();
      default:
        throw new RuntimeException(
            "Invalid map field number: " + number);
    }
  }
  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_WfRunRequestPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.WfRunRequestPb.class, io.littlehorse.common.proto.WfRunRequestPb.Builder.class);
  }

  private int bitField0_;
  public static final int WF_RUN_ID_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object wfRunId_ = "";
  /**
   * <code>optional string wf_run_id = 1;</code>
   * @return Whether the wfRunId field is set.
   */
  @java.lang.Override
  public boolean hasWfRunId() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional string wf_run_id = 1;</code>
   * @return The wfRunId.
   */
  @java.lang.Override
  public java.lang.String getWfRunId() {
    java.lang.Object ref = wfRunId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      wfRunId_ = s;
      return s;
    }
  }
  /**
   * <code>optional string wf_run_id = 1;</code>
   * @return The bytes for wfRunId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getWfRunIdBytes() {
    java.lang.Object ref = wfRunId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      wfRunId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int WF_SPEC_ID_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private volatile java.lang.Object wfSpecId_ = "";
  /**
   * <code>string wf_spec_id = 2;</code>
   * @return The wfSpecId.
   */
  @java.lang.Override
  public java.lang.String getWfSpecId() {
    java.lang.Object ref = wfSpecId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      wfSpecId_ = s;
      return s;
    }
  }
  /**
   * <code>string wf_spec_id = 2;</code>
   * @return The bytes for wfSpecId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getWfSpecIdBytes() {
    java.lang.Object ref = wfSpecId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      wfSpecId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int VARIABLES_FIELD_NUMBER = 3;
  private static final class VariablesDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
        java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb> defaultEntry =
            com.google.protobuf.MapEntry
            .<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb>newDefaultInstance(
                io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_WfRunRequestPb_VariablesEntry_descriptor, 
                com.google.protobuf.WireFormat.FieldType.STRING,
                "",
                com.google.protobuf.WireFormat.FieldType.MESSAGE,
                io.littlehorse.sdk.common.proto.VariableValuePb.getDefaultInstance());
  }
  @SuppressWarnings("serial")
  private com.google.protobuf.MapField<
      java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb> variables_;
  private com.google.protobuf.MapField<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb>
  internalGetVariables() {
    if (variables_ == null) {
      return com.google.protobuf.MapField.emptyMapField(
          VariablesDefaultEntryHolder.defaultEntry);
    }
    return variables_;
  }
  public int getVariablesCount() {
    return internalGetVariables().getMap().size();
  }
  /**
   * <code>map&lt;string, .littlehorse.VariableValuePb&gt; variables = 3;</code>
   */
  @java.lang.Override
  public boolean containsVariables(
      java.lang.String key) {
    if (key == null) { throw new NullPointerException("map key"); }
    return internalGetVariables().getMap().containsKey(key);
  }
  /**
   * Use {@link #getVariablesMap()} instead.
   */
  @java.lang.Override
  @java.lang.Deprecated
  public java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb> getVariables() {
    return getVariablesMap();
  }
  /**
   * <code>map&lt;string, .littlehorse.VariableValuePb&gt; variables = 3;</code>
   */
  @java.lang.Override
  public java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb> getVariablesMap() {
    return internalGetVariables().getMap();
  }
  /**
   * <code>map&lt;string, .littlehorse.VariableValuePb&gt; variables = 3;</code>
   */
  @java.lang.Override
  public /* nullable */
io.littlehorse.sdk.common.proto.VariableValuePb getVariablesOrDefault(
      java.lang.String key,
      /* nullable */
io.littlehorse.sdk.common.proto.VariableValuePb defaultValue) {
    if (key == null) { throw new NullPointerException("map key"); }
    java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb> map =
        internalGetVariables().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /**
   * <code>map&lt;string, .littlehorse.VariableValuePb&gt; variables = 3;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.VariableValuePb getVariablesOrThrow(
      java.lang.String key) {
    if (key == null) { throw new NullPointerException("map key"); }
    java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb> map =
        internalGetVariables().getMap();
    if (!map.containsKey(key)) {
      throw new java.lang.IllegalArgumentException();
    }
    return map.get(key);
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
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, wfRunId_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(wfSpecId_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, wfSpecId_);
    }
    com.google.protobuf.GeneratedMessageV3
      .serializeStringMapTo(
        output,
        internalGetVariables(),
        VariablesDefaultEntryHolder.defaultEntry,
        3);
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, wfRunId_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(wfSpecId_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, wfSpecId_);
    }
    for (java.util.Map.Entry<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb> entry
         : internalGetVariables().getMap().entrySet()) {
      com.google.protobuf.MapEntry<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb>
      variables__ = VariablesDefaultEntryHolder.defaultEntry.newBuilderForType()
          .setKey(entry.getKey())
          .setValue(entry.getValue())
          .build();
      size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(3, variables__);
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
    if (!(obj instanceof io.littlehorse.common.proto.WfRunRequestPb)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.WfRunRequestPb other = (io.littlehorse.common.proto.WfRunRequestPb) obj;

    if (hasWfRunId() != other.hasWfRunId()) return false;
    if (hasWfRunId()) {
      if (!getWfRunId()
          .equals(other.getWfRunId())) return false;
    }
    if (!getWfSpecId()
        .equals(other.getWfSpecId())) return false;
    if (!internalGetVariables().equals(
        other.internalGetVariables())) return false;
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
    if (hasWfRunId()) {
      hash = (37 * hash) + WF_RUN_ID_FIELD_NUMBER;
      hash = (53 * hash) + getWfRunId().hashCode();
    }
    hash = (37 * hash) + WF_SPEC_ID_FIELD_NUMBER;
    hash = (53 * hash) + getWfSpecId().hashCode();
    if (!internalGetVariables().getMap().isEmpty()) {
      hash = (37 * hash) + VARIABLES_FIELD_NUMBER;
      hash = (53 * hash) + internalGetVariables().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.WfRunRequestPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.WfRunRequestPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.WfRunRequestPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.WfRunRequestPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.WfRunRequestPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.WfRunRequestPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.WfRunRequestPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.WfRunRequestPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.WfRunRequestPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.WfRunRequestPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.WfRunRequestPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.WfRunRequestPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.WfRunRequestPb prototype) {
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
   * This section defines the "Command"
   * </pre>
   *
   * Protobuf type {@code littlehorse.WfRunRequestPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.WfRunRequestPb)
      io.littlehorse.common.proto.WfRunRequestPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_WfRunRequestPb_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(
        int number) {
      switch (number) {
        case 3:
          return internalGetVariables();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(
        int number) {
      switch (number) {
        case 3:
          return internalGetMutableVariables();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_WfRunRequestPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.WfRunRequestPb.class, io.littlehorse.common.proto.WfRunRequestPb.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.WfRunRequestPb.newBuilder()
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
      wfRunId_ = "";
      wfSpecId_ = "";
      internalGetMutableVariables().clear();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_WfRunRequestPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.WfRunRequestPb getDefaultInstanceForType() {
      return io.littlehorse.common.proto.WfRunRequestPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.WfRunRequestPb build() {
      io.littlehorse.common.proto.WfRunRequestPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.WfRunRequestPb buildPartial() {
      io.littlehorse.common.proto.WfRunRequestPb result = new io.littlehorse.common.proto.WfRunRequestPb(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.common.proto.WfRunRequestPb result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.wfRunId_ = wfRunId_;
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.wfSpecId_ = wfSpecId_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.variables_ = internalGetVariables();
        result.variables_.makeImmutable();
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
      if (other instanceof io.littlehorse.common.proto.WfRunRequestPb) {
        return mergeFrom((io.littlehorse.common.proto.WfRunRequestPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.WfRunRequestPb other) {
      if (other == io.littlehorse.common.proto.WfRunRequestPb.getDefaultInstance()) return this;
      if (other.hasWfRunId()) {
        wfRunId_ = other.wfRunId_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (!other.getWfSpecId().isEmpty()) {
        wfSpecId_ = other.wfSpecId_;
        bitField0_ |= 0x00000002;
        onChanged();
      }
      internalGetMutableVariables().mergeFrom(
          other.internalGetVariables());
      bitField0_ |= 0x00000004;
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
              wfRunId_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              wfSpecId_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 26: {
              com.google.protobuf.MapEntry<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb>
              variables__ = input.readMessage(
                  VariablesDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
              internalGetMutableVariables().getMutableMap().put(
                  variables__.getKey(), variables__.getValue());
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

    private java.lang.Object wfRunId_ = "";
    /**
     * <code>optional string wf_run_id = 1;</code>
     * @return Whether the wfRunId field is set.
     */
    public boolean hasWfRunId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>optional string wf_run_id = 1;</code>
     * @return The wfRunId.
     */
    public java.lang.String getWfRunId() {
      java.lang.Object ref = wfRunId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        wfRunId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>optional string wf_run_id = 1;</code>
     * @return The bytes for wfRunId.
     */
    public com.google.protobuf.ByteString
        getWfRunIdBytes() {
      java.lang.Object ref = wfRunId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        wfRunId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>optional string wf_run_id = 1;</code>
     * @param value The wfRunId to set.
     * @return This builder for chaining.
     */
    public Builder setWfRunId(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      wfRunId_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>optional string wf_run_id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearWfRunId() {
      wfRunId_ = getDefaultInstance().getWfRunId();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>optional string wf_run_id = 1;</code>
     * @param value The bytes for wfRunId to set.
     * @return This builder for chaining.
     */
    public Builder setWfRunIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      wfRunId_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private java.lang.Object wfSpecId_ = "";
    /**
     * <code>string wf_spec_id = 2;</code>
     * @return The wfSpecId.
     */
    public java.lang.String getWfSpecId() {
      java.lang.Object ref = wfSpecId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        wfSpecId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string wf_spec_id = 2;</code>
     * @return The bytes for wfSpecId.
     */
    public com.google.protobuf.ByteString
        getWfSpecIdBytes() {
      java.lang.Object ref = wfSpecId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        wfSpecId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string wf_spec_id = 2;</code>
     * @param value The wfSpecId to set.
     * @return This builder for chaining.
     */
    public Builder setWfSpecId(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      wfSpecId_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>string wf_spec_id = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearWfSpecId() {
      wfSpecId_ = getDefaultInstance().getWfSpecId();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /**
     * <code>string wf_spec_id = 2;</code>
     * @param value The bytes for wfSpecId to set.
     * @return This builder for chaining.
     */
    public Builder setWfSpecIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      wfSpecId_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }

    private com.google.protobuf.MapField<
        java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb> variables_;
    private com.google.protobuf.MapField<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb>
        internalGetVariables() {
      if (variables_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            VariablesDefaultEntryHolder.defaultEntry);
      }
      return variables_;
    }
    private com.google.protobuf.MapField<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb>
        internalGetMutableVariables() {
      if (variables_ == null) {
        variables_ = com.google.protobuf.MapField.newMapField(
            VariablesDefaultEntryHolder.defaultEntry);
      }
      if (!variables_.isMutable()) {
        variables_ = variables_.copy();
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return variables_;
    }
    public int getVariablesCount() {
      return internalGetVariables().getMap().size();
    }
    /**
     * <code>map&lt;string, .littlehorse.VariableValuePb&gt; variables = 3;</code>
     */
    @java.lang.Override
    public boolean containsVariables(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      return internalGetVariables().getMap().containsKey(key);
    }
    /**
     * Use {@link #getVariablesMap()} instead.
     */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb> getVariables() {
      return getVariablesMap();
    }
    /**
     * <code>map&lt;string, .littlehorse.VariableValuePb&gt; variables = 3;</code>
     */
    @java.lang.Override
    public java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb> getVariablesMap() {
      return internalGetVariables().getMap();
    }
    /**
     * <code>map&lt;string, .littlehorse.VariableValuePb&gt; variables = 3;</code>
     */
    @java.lang.Override
    public /* nullable */
io.littlehorse.sdk.common.proto.VariableValuePb getVariablesOrDefault(
        java.lang.String key,
        /* nullable */
io.littlehorse.sdk.common.proto.VariableValuePb defaultValue) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb> map =
          internalGetVariables().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <code>map&lt;string, .littlehorse.VariableValuePb&gt; variables = 3;</code>
     */
    @java.lang.Override
    public io.littlehorse.sdk.common.proto.VariableValuePb getVariablesOrThrow(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb> map =
          internalGetVariables().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }
    public Builder clearVariables() {
      bitField0_ = (bitField0_ & ~0x00000004);
      internalGetMutableVariables().getMutableMap()
          .clear();
      return this;
    }
    /**
     * <code>map&lt;string, .littlehorse.VariableValuePb&gt; variables = 3;</code>
     */
    public Builder removeVariables(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      internalGetMutableVariables().getMutableMap()
          .remove(key);
      return this;
    }
    /**
     * Use alternate mutation accessors instead.
     */
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb>
        getMutableVariables() {
      bitField0_ |= 0x00000004;
      return internalGetMutableVariables().getMutableMap();
    }
    /**
     * <code>map&lt;string, .littlehorse.VariableValuePb&gt; variables = 3;</code>
     */
    public Builder putVariables(
        java.lang.String key,
        io.littlehorse.sdk.common.proto.VariableValuePb value) {
      if (key == null) { throw new NullPointerException("map key"); }
      if (value == null) { throw new NullPointerException("map value"); }
      internalGetMutableVariables().getMutableMap()
          .put(key, value);
      bitField0_ |= 0x00000004;
      return this;
    }
    /**
     * <code>map&lt;string, .littlehorse.VariableValuePb&gt; variables = 3;</code>
     */
    public Builder putAllVariables(
        java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValuePb> values) {
      internalGetMutableVariables().getMutableMap()
          .putAll(values);
      bitField0_ |= 0x00000004;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.WfRunRequestPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.WfRunRequestPb)
  private static final io.littlehorse.common.proto.WfRunRequestPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.WfRunRequestPb();
  }

  public static io.littlehorse.common.proto.WfRunRequestPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<WfRunRequestPb>
      PARSER = new com.google.protobuf.AbstractParser<WfRunRequestPb>() {
    @java.lang.Override
    public WfRunRequestPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<WfRunRequestPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<WfRunRequestPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.WfRunRequestPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

