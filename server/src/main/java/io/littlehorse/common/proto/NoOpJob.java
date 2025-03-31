// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: job.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.common.proto;

/**
 * <pre>
 * Will be removed in the future when we add an actual job
 * </pre>
 *
 * Protobuf type {@code littlehorse.NoOpJob}
 */
public final class NoOpJob extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.NoOpJob)
    NoOpJobOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      NoOpJob.class.getName());
  }
  // Use NoOpJob.newBuilder() to construct.
  private NoOpJob(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private NoOpJob() {
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.Job.internal_static_littlehorse_NoOpJob_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.Job.internal_static_littlehorse_NoOpJob_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.NoOpJob.class, io.littlehorse.common.proto.NoOpJob.Builder.class);
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
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof io.littlehorse.common.proto.NoOpJob)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.NoOpJob other = (io.littlehorse.common.proto.NoOpJob) obj;

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
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.NoOpJob parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.NoOpJob parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.NoOpJob parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.NoOpJob parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.NoOpJob parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.NoOpJob parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.NoOpJob parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.NoOpJob parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.NoOpJob parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.NoOpJob parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.NoOpJob parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.NoOpJob parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.NoOpJob prototype) {
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
   * Will be removed in the future when we add an actual job
   * </pre>
   *
   * Protobuf type {@code littlehorse.NoOpJob}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.NoOpJob)
      io.littlehorse.common.proto.NoOpJobOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.Job.internal_static_littlehorse_NoOpJob_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.Job.internal_static_littlehorse_NoOpJob_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.NoOpJob.class, io.littlehorse.common.proto.NoOpJob.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.NoOpJob.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.Job.internal_static_littlehorse_NoOpJob_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.NoOpJob getDefaultInstanceForType() {
      return io.littlehorse.common.proto.NoOpJob.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.NoOpJob build() {
      io.littlehorse.common.proto.NoOpJob result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.NoOpJob buildPartial() {
      io.littlehorse.common.proto.NoOpJob result = new io.littlehorse.common.proto.NoOpJob(this);
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.common.proto.NoOpJob) {
        return mergeFrom((io.littlehorse.common.proto.NoOpJob)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.NoOpJob other) {
      if (other == io.littlehorse.common.proto.NoOpJob.getDefaultInstance()) return this;
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

    // @@protoc_insertion_point(builder_scope:littlehorse.NoOpJob)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.NoOpJob)
  private static final io.littlehorse.common.proto.NoOpJob DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.NoOpJob();
  }

  public static io.littlehorse.common.proto.NoOpJob getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<NoOpJob>
      PARSER = new com.google.protobuf.AbstractParser<NoOpJob>() {
    @java.lang.Override
    public NoOpJob parsePartialFrom(
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

  public static com.google.protobuf.Parser<NoOpJob> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<NoOpJob> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.NoOpJob getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

