// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: wf_spec.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.ThreadRetentionPolicy}
 */
public final class ThreadRetentionPolicy extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.ThreadRetentionPolicy)
    ThreadRetentionPolicyOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ThreadRetentionPolicy.newBuilder() to construct.
  private ThreadRetentionPolicy(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ThreadRetentionPolicy() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ThreadRetentionPolicy();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_ThreadRetentionPolicy_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_ThreadRetentionPolicy_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.ThreadRetentionPolicy.class, io.littlehorse.sdk.common.proto.ThreadRetentionPolicy.Builder.class);
  }

  private int threadGcPolicyCase_ = 0;
  @SuppressWarnings("serial")
  private java.lang.Object threadGcPolicy_;
  public enum ThreadGcPolicyCase
      implements com.google.protobuf.Internal.EnumLite,
          com.google.protobuf.AbstractMessage.InternalOneOfEnum {
    SECONDS_AFTER_THREAD_TERMINATION(1),
    THREADGCPOLICY_NOT_SET(0);
    private final int value;
    private ThreadGcPolicyCase(int value) {
      this.value = value;
    }
    /**
     * @param value The number of the enum to look for.
     * @return The enum associated with the given number.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static ThreadGcPolicyCase valueOf(int value) {
      return forNumber(value);
    }

    public static ThreadGcPolicyCase forNumber(int value) {
      switch (value) {
        case 1: return SECONDS_AFTER_THREAD_TERMINATION;
        case 0: return THREADGCPOLICY_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  public ThreadGcPolicyCase
  getThreadGcPolicyCase() {
    return ThreadGcPolicyCase.forNumber(
        threadGcPolicyCase_);
  }

  public static final int SECONDS_AFTER_THREAD_TERMINATION_FIELD_NUMBER = 1;
  /**
   * <pre>
   * Delete associated ThreadRun's X seconds after they terminate, regardless
   * of status.
   * </pre>
   *
   * <code>int64 seconds_after_thread_termination = 1;</code>
   * @return Whether the secondsAfterThreadTermination field is set.
   */
  @java.lang.Override
  public boolean hasSecondsAfterThreadTermination() {
    return threadGcPolicyCase_ == 1;
  }
  /**
   * <pre>
   * Delete associated ThreadRun's X seconds after they terminate, regardless
   * of status.
   * </pre>
   *
   * <code>int64 seconds_after_thread_termination = 1;</code>
   * @return The secondsAfterThreadTermination.
   */
  @java.lang.Override
  public long getSecondsAfterThreadTermination() {
    if (threadGcPolicyCase_ == 1) {
      return (java.lang.Long) threadGcPolicy_;
    }
    return 0L;
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
    if (threadGcPolicyCase_ == 1) {
      output.writeInt64(
          1, (long)((java.lang.Long) threadGcPolicy_));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (threadGcPolicyCase_ == 1) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(
            1, (long)((java.lang.Long) threadGcPolicy_));
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.ThreadRetentionPolicy)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.ThreadRetentionPolicy other = (io.littlehorse.sdk.common.proto.ThreadRetentionPolicy) obj;

    if (!getThreadGcPolicyCase().equals(other.getThreadGcPolicyCase())) return false;
    switch (threadGcPolicyCase_) {
      case 1:
        if (getSecondsAfterThreadTermination()
            != other.getSecondsAfterThreadTermination()) return false;
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
    switch (threadGcPolicyCase_) {
      case 1:
        hash = (37 * hash) + SECONDS_AFTER_THREAD_TERMINATION_FIELD_NUMBER;
        hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
            getSecondsAfterThreadTermination());
        break;
      case 0:
      default:
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.ThreadRetentionPolicy parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ThreadRetentionPolicy parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ThreadRetentionPolicy parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ThreadRetentionPolicy parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ThreadRetentionPolicy parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ThreadRetentionPolicy parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ThreadRetentionPolicy parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ThreadRetentionPolicy parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.ThreadRetentionPolicy parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.ThreadRetentionPolicy parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ThreadRetentionPolicy parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ThreadRetentionPolicy parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.ThreadRetentionPolicy prototype) {
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
   * Protobuf type {@code littlehorse.ThreadRetentionPolicy}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.ThreadRetentionPolicy)
      io.littlehorse.sdk.common.proto.ThreadRetentionPolicyOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_ThreadRetentionPolicy_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_ThreadRetentionPolicy_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.ThreadRetentionPolicy.class, io.littlehorse.sdk.common.proto.ThreadRetentionPolicy.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.ThreadRetentionPolicy.newBuilder()
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
      threadGcPolicyCase_ = 0;
      threadGcPolicy_ = null;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_ThreadRetentionPolicy_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ThreadRetentionPolicy getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.ThreadRetentionPolicy.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ThreadRetentionPolicy build() {
      io.littlehorse.sdk.common.proto.ThreadRetentionPolicy result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ThreadRetentionPolicy buildPartial() {
      io.littlehorse.sdk.common.proto.ThreadRetentionPolicy result = new io.littlehorse.sdk.common.proto.ThreadRetentionPolicy(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      buildPartialOneofs(result);
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.ThreadRetentionPolicy result) {
      int from_bitField0_ = bitField0_;
    }

    private void buildPartialOneofs(io.littlehorse.sdk.common.proto.ThreadRetentionPolicy result) {
      result.threadGcPolicyCase_ = threadGcPolicyCase_;
      result.threadGcPolicy_ = this.threadGcPolicy_;
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
      if (other instanceof io.littlehorse.sdk.common.proto.ThreadRetentionPolicy) {
        return mergeFrom((io.littlehorse.sdk.common.proto.ThreadRetentionPolicy)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.ThreadRetentionPolicy other) {
      if (other == io.littlehorse.sdk.common.proto.ThreadRetentionPolicy.getDefaultInstance()) return this;
      switch (other.getThreadGcPolicyCase()) {
        case SECONDS_AFTER_THREAD_TERMINATION: {
          setSecondsAfterThreadTermination(other.getSecondsAfterThreadTermination());
          break;
        }
        case THREADGCPOLICY_NOT_SET: {
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
            case 8: {
              threadGcPolicy_ = input.readInt64();
              threadGcPolicyCase_ = 1;
              break;
            } // case 8
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
    private int threadGcPolicyCase_ = 0;
    private java.lang.Object threadGcPolicy_;
    public ThreadGcPolicyCase
        getThreadGcPolicyCase() {
      return ThreadGcPolicyCase.forNumber(
          threadGcPolicyCase_);
    }

    public Builder clearThreadGcPolicy() {
      threadGcPolicyCase_ = 0;
      threadGcPolicy_ = null;
      onChanged();
      return this;
    }

    private int bitField0_;

    /**
     * <pre>
     * Delete associated ThreadRun's X seconds after they terminate, regardless
     * of status.
     * </pre>
     *
     * <code>int64 seconds_after_thread_termination = 1;</code>
     * @return Whether the secondsAfterThreadTermination field is set.
     */
    public boolean hasSecondsAfterThreadTermination() {
      return threadGcPolicyCase_ == 1;
    }
    /**
     * <pre>
     * Delete associated ThreadRun's X seconds after they terminate, regardless
     * of status.
     * </pre>
     *
     * <code>int64 seconds_after_thread_termination = 1;</code>
     * @return The secondsAfterThreadTermination.
     */
    public long getSecondsAfterThreadTermination() {
      if (threadGcPolicyCase_ == 1) {
        return (java.lang.Long) threadGcPolicy_;
      }
      return 0L;
    }
    /**
     * <pre>
     * Delete associated ThreadRun's X seconds after they terminate, regardless
     * of status.
     * </pre>
     *
     * <code>int64 seconds_after_thread_termination = 1;</code>
     * @param value The secondsAfterThreadTermination to set.
     * @return This builder for chaining.
     */
    public Builder setSecondsAfterThreadTermination(long value) {

      threadGcPolicyCase_ = 1;
      threadGcPolicy_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Delete associated ThreadRun's X seconds after they terminate, regardless
     * of status.
     * </pre>
     *
     * <code>int64 seconds_after_thread_termination = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearSecondsAfterThreadTermination() {
      if (threadGcPolicyCase_ == 1) {
        threadGcPolicyCase_ = 0;
        threadGcPolicy_ = null;
        onChanged();
      }
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


    // @@protoc_insertion_point(builder_scope:littlehorse.ThreadRetentionPolicy)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.ThreadRetentionPolicy)
  private static final io.littlehorse.sdk.common.proto.ThreadRetentionPolicy DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.ThreadRetentionPolicy();
  }

  public static io.littlehorse.sdk.common.proto.ThreadRetentionPolicy getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ThreadRetentionPolicy>
      PARSER = new com.google.protobuf.AbstractParser<ThreadRetentionPolicy>() {
    @java.lang.Override
    public ThreadRetentionPolicy parsePartialFrom(
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

  public static com.google.protobuf.Parser<ThreadRetentionPolicy> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ThreadRetentionPolicy> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ThreadRetentionPolicy getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

