// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.HealthCheckReplyPb}
 */
public final class HealthCheckReplyPb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.HealthCheckReplyPb)
    HealthCheckReplyPbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use HealthCheckReplyPb.newBuilder() to construct.
  private HealthCheckReplyPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private HealthCheckReplyPb() {
    coreState_ = 0;
    timerState_ = 0;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new HealthCheckReplyPb();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_HealthCheckReplyPb_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_HealthCheckReplyPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.HealthCheckReplyPb.class, io.littlehorse.sdk.common.proto.HealthCheckReplyPb.Builder.class);
  }

  public static final int CORE_STATE_FIELD_NUMBER = 1;
  private int coreState_ = 0;
  /**
   * <code>.littlehorse.LHHealthResultPb core_state = 1;</code>
   * @return The enum numeric value on the wire for coreState.
   */
  @java.lang.Override public int getCoreStateValue() {
    return coreState_;
  }
  /**
   * <code>.littlehorse.LHHealthResultPb core_state = 1;</code>
   * @return The coreState.
   */
  @java.lang.Override public io.littlehorse.sdk.common.proto.LHHealthResultPb getCoreState() {
    io.littlehorse.sdk.common.proto.LHHealthResultPb result = io.littlehorse.sdk.common.proto.LHHealthResultPb.forNumber(coreState_);
    return result == null ? io.littlehorse.sdk.common.proto.LHHealthResultPb.UNRECOGNIZED : result;
  }

  public static final int TIMER_STATE_FIELD_NUMBER = 2;
  private int timerState_ = 0;
  /**
   * <code>.littlehorse.LHHealthResultPb timer_state = 2;</code>
   * @return The enum numeric value on the wire for timerState.
   */
  @java.lang.Override public int getTimerStateValue() {
    return timerState_;
  }
  /**
   * <code>.littlehorse.LHHealthResultPb timer_state = 2;</code>
   * @return The timerState.
   */
  @java.lang.Override public io.littlehorse.sdk.common.proto.LHHealthResultPb getTimerState() {
    io.littlehorse.sdk.common.proto.LHHealthResultPb result = io.littlehorse.sdk.common.proto.LHHealthResultPb.forNumber(timerState_);
    return result == null ? io.littlehorse.sdk.common.proto.LHHealthResultPb.UNRECOGNIZED : result;
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
    if (coreState_ != io.littlehorse.sdk.common.proto.LHHealthResultPb.LH_HEALTH_RUNNING.getNumber()) {
      output.writeEnum(1, coreState_);
    }
    if (timerState_ != io.littlehorse.sdk.common.proto.LHHealthResultPb.LH_HEALTH_RUNNING.getNumber()) {
      output.writeEnum(2, timerState_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (coreState_ != io.littlehorse.sdk.common.proto.LHHealthResultPb.LH_HEALTH_RUNNING.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(1, coreState_);
    }
    if (timerState_ != io.littlehorse.sdk.common.proto.LHHealthResultPb.LH_HEALTH_RUNNING.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(2, timerState_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.HealthCheckReplyPb)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.HealthCheckReplyPb other = (io.littlehorse.sdk.common.proto.HealthCheckReplyPb) obj;

    if (coreState_ != other.coreState_) return false;
    if (timerState_ != other.timerState_) return false;
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
    hash = (37 * hash) + CORE_STATE_FIELD_NUMBER;
    hash = (53 * hash) + coreState_;
    hash = (37 * hash) + TIMER_STATE_FIELD_NUMBER;
    hash = (53 * hash) + timerState_;
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.HealthCheckReplyPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.HealthCheckReplyPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.HealthCheckReplyPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.HealthCheckReplyPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.HealthCheckReplyPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.HealthCheckReplyPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.HealthCheckReplyPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.HealthCheckReplyPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.HealthCheckReplyPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.HealthCheckReplyPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.HealthCheckReplyPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.HealthCheckReplyPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.HealthCheckReplyPb prototype) {
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
   * Protobuf type {@code littlehorse.HealthCheckReplyPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.HealthCheckReplyPb)
      io.littlehorse.sdk.common.proto.HealthCheckReplyPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_HealthCheckReplyPb_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_HealthCheckReplyPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.HealthCheckReplyPb.class, io.littlehorse.sdk.common.proto.HealthCheckReplyPb.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.HealthCheckReplyPb.newBuilder()
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
      coreState_ = 0;
      timerState_ = 0;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_HealthCheckReplyPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.HealthCheckReplyPb getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.HealthCheckReplyPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.HealthCheckReplyPb build() {
      io.littlehorse.sdk.common.proto.HealthCheckReplyPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.HealthCheckReplyPb buildPartial() {
      io.littlehorse.sdk.common.proto.HealthCheckReplyPb result = new io.littlehorse.sdk.common.proto.HealthCheckReplyPb(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.HealthCheckReplyPb result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.coreState_ = coreState_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.timerState_ = timerState_;
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
      if (other instanceof io.littlehorse.sdk.common.proto.HealthCheckReplyPb) {
        return mergeFrom((io.littlehorse.sdk.common.proto.HealthCheckReplyPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.HealthCheckReplyPb other) {
      if (other == io.littlehorse.sdk.common.proto.HealthCheckReplyPb.getDefaultInstance()) return this;
      if (other.coreState_ != 0) {
        setCoreStateValue(other.getCoreStateValue());
      }
      if (other.timerState_ != 0) {
        setTimerStateValue(other.getTimerStateValue());
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
              coreState_ = input.readEnum();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 16: {
              timerState_ = input.readEnum();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
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

    private int coreState_ = 0;
    /**
     * <code>.littlehorse.LHHealthResultPb core_state = 1;</code>
     * @return The enum numeric value on the wire for coreState.
     */
    @java.lang.Override public int getCoreStateValue() {
      return coreState_;
    }
    /**
     * <code>.littlehorse.LHHealthResultPb core_state = 1;</code>
     * @param value The enum numeric value on the wire for coreState to set.
     * @return This builder for chaining.
     */
    public Builder setCoreStateValue(int value) {
      coreState_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.LHHealthResultPb core_state = 1;</code>
     * @return The coreState.
     */
    @java.lang.Override
    public io.littlehorse.sdk.common.proto.LHHealthResultPb getCoreState() {
      io.littlehorse.sdk.common.proto.LHHealthResultPb result = io.littlehorse.sdk.common.proto.LHHealthResultPb.forNumber(coreState_);
      return result == null ? io.littlehorse.sdk.common.proto.LHHealthResultPb.UNRECOGNIZED : result;
    }
    /**
     * <code>.littlehorse.LHHealthResultPb core_state = 1;</code>
     * @param value The coreState to set.
     * @return This builder for chaining.
     */
    public Builder setCoreState(io.littlehorse.sdk.common.proto.LHHealthResultPb value) {
      if (value == null) {
        throw new NullPointerException();
      }
      bitField0_ |= 0x00000001;
      coreState_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.LHHealthResultPb core_state = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearCoreState() {
      bitField0_ = (bitField0_ & ~0x00000001);
      coreState_ = 0;
      onChanged();
      return this;
    }

    private int timerState_ = 0;
    /**
     * <code>.littlehorse.LHHealthResultPb timer_state = 2;</code>
     * @return The enum numeric value on the wire for timerState.
     */
    @java.lang.Override public int getTimerStateValue() {
      return timerState_;
    }
    /**
     * <code>.littlehorse.LHHealthResultPb timer_state = 2;</code>
     * @param value The enum numeric value on the wire for timerState to set.
     * @return This builder for chaining.
     */
    public Builder setTimerStateValue(int value) {
      timerState_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.LHHealthResultPb timer_state = 2;</code>
     * @return The timerState.
     */
    @java.lang.Override
    public io.littlehorse.sdk.common.proto.LHHealthResultPb getTimerState() {
      io.littlehorse.sdk.common.proto.LHHealthResultPb result = io.littlehorse.sdk.common.proto.LHHealthResultPb.forNumber(timerState_);
      return result == null ? io.littlehorse.sdk.common.proto.LHHealthResultPb.UNRECOGNIZED : result;
    }
    /**
     * <code>.littlehorse.LHHealthResultPb timer_state = 2;</code>
     * @param value The timerState to set.
     * @return This builder for chaining.
     */
    public Builder setTimerState(io.littlehorse.sdk.common.proto.LHHealthResultPb value) {
      if (value == null) {
        throw new NullPointerException();
      }
      bitField0_ |= 0x00000002;
      timerState_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.LHHealthResultPb timer_state = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearTimerState() {
      bitField0_ = (bitField0_ & ~0x00000002);
      timerState_ = 0;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.HealthCheckReplyPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.HealthCheckReplyPb)
  private static final io.littlehorse.sdk.common.proto.HealthCheckReplyPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.HealthCheckReplyPb();
  }

  public static io.littlehorse.sdk.common.proto.HealthCheckReplyPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<HealthCheckReplyPb>
      PARSER = new com.google.protobuf.AbstractParser<HealthCheckReplyPb>() {
    @java.lang.Override
    public HealthCheckReplyPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<HealthCheckReplyPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<HealthCheckReplyPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.HealthCheckReplyPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

