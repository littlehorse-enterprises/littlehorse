// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: common_wfspec.proto

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Defines an Exponential backoff policy for TaskRun retries. The delay for a retry
 * attempt `N` is defined as:
 *
 * min(base_interval_seconds * (multiplier ^N), max_delay_seconds)
 *
 * Note that timers in LittleHorse have a resolution of about 500-1000 milliseconds,
 * so timing is not exact.
 * </pre>
 *
 * Protobuf type {@code littlehorse.ExponentialBackoffRetryPolicy}
 */
public final class ExponentialBackoffRetryPolicy extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.ExponentialBackoffRetryPolicy)
    ExponentialBackoffRetryPolicyOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ExponentialBackoffRetryPolicy.newBuilder() to construct.
  private ExponentialBackoffRetryPolicy(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ExponentialBackoffRetryPolicy() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ExponentialBackoffRetryPolicy();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.CommonWfspec.internal_static_littlehorse_ExponentialBackoffRetryPolicy_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.CommonWfspec.internal_static_littlehorse_ExponentialBackoffRetryPolicy_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy.class, io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy.Builder.class);
  }

  public static final int BASE_INTERVAL_MS_FIELD_NUMBER = 1;
  private int baseIntervalMs_ = 0;
  /**
   * <pre>
   * Base delay in ms for the first retry. Note that in LittleHorse, timers have a
   * resolution of 500-1000 milliseconds. Must be greater than zero.
   * </pre>
   *
   * <code>int32 base_interval_ms = 1;</code>
   * @return The baseIntervalMs.
   */
  @java.lang.Override
  public int getBaseIntervalMs() {
    return baseIntervalMs_;
  }

  public static final int MAX_DELAY_MS_FIELD_NUMBER = 2;
  private long maxDelayMs_ = 0L;
  /**
   * <pre>
   * Maximum delay in milliseconds between retries.
   * </pre>
   *
   * <code>int64 max_delay_ms = 2;</code>
   * @return The maxDelayMs.
   */
  @java.lang.Override
  public long getMaxDelayMs() {
    return maxDelayMs_;
  }

  public static final int MAX_RETRIES_FIELD_NUMBER = 3;
  private int maxRetries_ = 0;
  /**
   * <pre>
   * Maximum number of retries to schedule. Setting this to `1` means that one retry
   * will be scheduled after a failed first task attempt.
   * </pre>
   *
   * <code>int32 max_retries = 3;</code>
   * @return The maxRetries.
   */
  @java.lang.Override
  public int getMaxRetries() {
    return maxRetries_;
  }

  public static final int MULTIPLIER_FIELD_NUMBER = 4;
  private float multiplier_ = 0F;
  /**
   * <pre>
   * The multiplier to use in calculating the retry backoff policy. We recommend
   * starting with 2.0. Must be at least 1.0.
   * </pre>
   *
   * <code>float multiplier = 4;</code>
   * @return The multiplier.
   */
  @java.lang.Override
  public float getMultiplier() {
    return multiplier_;
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
    if (baseIntervalMs_ != 0) {
      output.writeInt32(1, baseIntervalMs_);
    }
    if (maxDelayMs_ != 0L) {
      output.writeInt64(2, maxDelayMs_);
    }
    if (maxRetries_ != 0) {
      output.writeInt32(3, maxRetries_);
    }
    if (java.lang.Float.floatToRawIntBits(multiplier_) != 0) {
      output.writeFloat(4, multiplier_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (baseIntervalMs_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(1, baseIntervalMs_);
    }
    if (maxDelayMs_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(2, maxDelayMs_);
    }
    if (maxRetries_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(3, maxRetries_);
    }
    if (java.lang.Float.floatToRawIntBits(multiplier_) != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeFloatSize(4, multiplier_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy other = (io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy) obj;

    if (getBaseIntervalMs()
        != other.getBaseIntervalMs()) return false;
    if (getMaxDelayMs()
        != other.getMaxDelayMs()) return false;
    if (getMaxRetries()
        != other.getMaxRetries()) return false;
    if (java.lang.Float.floatToIntBits(getMultiplier())
        != java.lang.Float.floatToIntBits(
            other.getMultiplier())) return false;
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
    hash = (37 * hash) + BASE_INTERVAL_MS_FIELD_NUMBER;
    hash = (53 * hash) + getBaseIntervalMs();
    hash = (37 * hash) + MAX_DELAY_MS_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getMaxDelayMs());
    hash = (37 * hash) + MAX_RETRIES_FIELD_NUMBER;
    hash = (53 * hash) + getMaxRetries();
    hash = (37 * hash) + MULTIPLIER_FIELD_NUMBER;
    hash = (53 * hash) + java.lang.Float.floatToIntBits(
        getMultiplier());
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy prototype) {
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
   * Defines an Exponential backoff policy for TaskRun retries. The delay for a retry
   * attempt `N` is defined as:
   *
   * min(base_interval_seconds * (multiplier ^N), max_delay_seconds)
   *
   * Note that timers in LittleHorse have a resolution of about 500-1000 milliseconds,
   * so timing is not exact.
   * </pre>
   *
   * Protobuf type {@code littlehorse.ExponentialBackoffRetryPolicy}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.ExponentialBackoffRetryPolicy)
      io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicyOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.CommonWfspec.internal_static_littlehorse_ExponentialBackoffRetryPolicy_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.CommonWfspec.internal_static_littlehorse_ExponentialBackoffRetryPolicy_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy.class, io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy.newBuilder()
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
      baseIntervalMs_ = 0;
      maxDelayMs_ = 0L;
      maxRetries_ = 0;
      multiplier_ = 0F;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.CommonWfspec.internal_static_littlehorse_ExponentialBackoffRetryPolicy_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy build() {
      io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy buildPartial() {
      io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy result = new io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.baseIntervalMs_ = baseIntervalMs_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.maxDelayMs_ = maxDelayMs_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.maxRetries_ = maxRetries_;
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        result.multiplier_ = multiplier_;
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
      if (other instanceof io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy) {
        return mergeFrom((io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy other) {
      if (other == io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy.getDefaultInstance()) return this;
      if (other.getBaseIntervalMs() != 0) {
        setBaseIntervalMs(other.getBaseIntervalMs());
      }
      if (other.getMaxDelayMs() != 0L) {
        setMaxDelayMs(other.getMaxDelayMs());
      }
      if (other.getMaxRetries() != 0) {
        setMaxRetries(other.getMaxRetries());
      }
      if (other.getMultiplier() != 0F) {
        setMultiplier(other.getMultiplier());
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
              baseIntervalMs_ = input.readInt32();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 16: {
              maxDelayMs_ = input.readInt64();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
            case 24: {
              maxRetries_ = input.readInt32();
              bitField0_ |= 0x00000004;
              break;
            } // case 24
            case 37: {
              multiplier_ = input.readFloat();
              bitField0_ |= 0x00000008;
              break;
            } // case 37
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

    private int baseIntervalMs_ ;
    /**
     * <pre>
     * Base delay in ms for the first retry. Note that in LittleHorse, timers have a
     * resolution of 500-1000 milliseconds. Must be greater than zero.
     * </pre>
     *
     * <code>int32 base_interval_ms = 1;</code>
     * @return The baseIntervalMs.
     */
    @java.lang.Override
    public int getBaseIntervalMs() {
      return baseIntervalMs_;
    }
    /**
     * <pre>
     * Base delay in ms for the first retry. Note that in LittleHorse, timers have a
     * resolution of 500-1000 milliseconds. Must be greater than zero.
     * </pre>
     *
     * <code>int32 base_interval_ms = 1;</code>
     * @param value The baseIntervalMs to set.
     * @return This builder for chaining.
     */
    public Builder setBaseIntervalMs(int value) {

      baseIntervalMs_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Base delay in ms for the first retry. Note that in LittleHorse, timers have a
     * resolution of 500-1000 milliseconds. Must be greater than zero.
     * </pre>
     *
     * <code>int32 base_interval_ms = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearBaseIntervalMs() {
      bitField0_ = (bitField0_ & ~0x00000001);
      baseIntervalMs_ = 0;
      onChanged();
      return this;
    }

    private long maxDelayMs_ ;
    /**
     * <pre>
     * Maximum delay in milliseconds between retries.
     * </pre>
     *
     * <code>int64 max_delay_ms = 2;</code>
     * @return The maxDelayMs.
     */
    @java.lang.Override
    public long getMaxDelayMs() {
      return maxDelayMs_;
    }
    /**
     * <pre>
     * Maximum delay in milliseconds between retries.
     * </pre>
     *
     * <code>int64 max_delay_ms = 2;</code>
     * @param value The maxDelayMs to set.
     * @return This builder for chaining.
     */
    public Builder setMaxDelayMs(long value) {

      maxDelayMs_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Maximum delay in milliseconds between retries.
     * </pre>
     *
     * <code>int64 max_delay_ms = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearMaxDelayMs() {
      bitField0_ = (bitField0_ & ~0x00000002);
      maxDelayMs_ = 0L;
      onChanged();
      return this;
    }

    private int maxRetries_ ;
    /**
     * <pre>
     * Maximum number of retries to schedule. Setting this to `1` means that one retry
     * will be scheduled after a failed first task attempt.
     * </pre>
     *
     * <code>int32 max_retries = 3;</code>
     * @return The maxRetries.
     */
    @java.lang.Override
    public int getMaxRetries() {
      return maxRetries_;
    }
    /**
     * <pre>
     * Maximum number of retries to schedule. Setting this to `1` means that one retry
     * will be scheduled after a failed first task attempt.
     * </pre>
     *
     * <code>int32 max_retries = 3;</code>
     * @param value The maxRetries to set.
     * @return This builder for chaining.
     */
    public Builder setMaxRetries(int value) {

      maxRetries_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Maximum number of retries to schedule. Setting this to `1` means that one retry
     * will be scheduled after a failed first task attempt.
     * </pre>
     *
     * <code>int32 max_retries = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearMaxRetries() {
      bitField0_ = (bitField0_ & ~0x00000004);
      maxRetries_ = 0;
      onChanged();
      return this;
    }

    private float multiplier_ ;
    /**
     * <pre>
     * The multiplier to use in calculating the retry backoff policy. We recommend
     * starting with 2.0. Must be at least 1.0.
     * </pre>
     *
     * <code>float multiplier = 4;</code>
     * @return The multiplier.
     */
    @java.lang.Override
    public float getMultiplier() {
      return multiplier_;
    }
    /**
     * <pre>
     * The multiplier to use in calculating the retry backoff policy. We recommend
     * starting with 2.0. Must be at least 1.0.
     * </pre>
     *
     * <code>float multiplier = 4;</code>
     * @param value The multiplier to set.
     * @return This builder for chaining.
     */
    public Builder setMultiplier(float value) {

      multiplier_ = value;
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The multiplier to use in calculating the retry backoff policy. We recommend
     * starting with 2.0. Must be at least 1.0.
     * </pre>
     *
     * <code>float multiplier = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearMultiplier() {
      bitField0_ = (bitField0_ & ~0x00000008);
      multiplier_ = 0F;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.ExponentialBackoffRetryPolicy)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.ExponentialBackoffRetryPolicy)
  private static final io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy();
  }

  public static io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ExponentialBackoffRetryPolicy>
      PARSER = new com.google.protobuf.AbstractParser<ExponentialBackoffRetryPolicy>() {
    @java.lang.Override
    public ExponentialBackoffRetryPolicy parsePartialFrom(
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

  public static com.google.protobuf.Parser<ExponentialBackoffRetryPolicy> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ExponentialBackoffRetryPolicy> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

