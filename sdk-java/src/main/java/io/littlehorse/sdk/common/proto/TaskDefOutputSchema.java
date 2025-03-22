// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: task_def.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Schema that validates the TaskDef's output
 * </pre>
 *
 * Protobuf type {@code littlehorse.TaskDefOutputSchema}
 */
public final class TaskDefOutputSchema extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.TaskDefOutputSchema)
    TaskDefOutputSchemaOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      TaskDefOutputSchema.class.getName());
  }
  // Use TaskDefOutputSchema.newBuilder() to construct.
  private TaskDefOutputSchema(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private TaskDefOutputSchema() {
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.TaskDefOuterClass.internal_static_littlehorse_TaskDefOutputSchema_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.TaskDefOuterClass.internal_static_littlehorse_TaskDefOutputSchema_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.TaskDefOutputSchema.class, io.littlehorse.sdk.common.proto.TaskDefOutputSchema.Builder.class);
  }

  private int bitField0_;
  public static final int VALUE_DEF_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.VariableDef valueDef_;
  /**
   * <pre>
   * The definition for the output content
   * </pre>
   *
   * <code>.littlehorse.VariableDef value_def = 1;</code>
   * @return Whether the valueDef field is set.
   */
  @java.lang.Override
  public boolean hasValueDef() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * The definition for the output content
   * </pre>
   *
   * <code>.littlehorse.VariableDef value_def = 1;</code>
   * @return The valueDef.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.VariableDef getValueDef() {
    return valueDef_ == null ? io.littlehorse.sdk.common.proto.VariableDef.getDefaultInstance() : valueDef_;
  }
  /**
   * <pre>
   * The definition for the output content
   * </pre>
   *
   * <code>.littlehorse.VariableDef value_def = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.VariableDefOrBuilder getValueDefOrBuilder() {
    return valueDef_ == null ? io.littlehorse.sdk.common.proto.VariableDef.getDefaultInstance() : valueDef_;
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
      output.writeMessage(1, getValueDef());
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
        .computeMessageSize(1, getValueDef());
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.TaskDefOutputSchema)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.TaskDefOutputSchema other = (io.littlehorse.sdk.common.proto.TaskDefOutputSchema) obj;

    if (hasValueDef() != other.hasValueDef()) return false;
    if (hasValueDef()) {
      if (!getValueDef()
          .equals(other.getValueDef())) return false;
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
    if (hasValueDef()) {
      hash = (37 * hash) + VALUE_DEF_FIELD_NUMBER;
      hash = (53 * hash) + getValueDef().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.TaskDefOutputSchema parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefOutputSchema parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefOutputSchema parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefOutputSchema parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefOutputSchema parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefOutputSchema parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefOutputSchema parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefOutputSchema parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.TaskDefOutputSchema parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.TaskDefOutputSchema parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefOutputSchema parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.TaskDefOutputSchema parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.TaskDefOutputSchema prototype) {
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
   * Schema that validates the TaskDef's output
   * </pre>
   *
   * Protobuf type {@code littlehorse.TaskDefOutputSchema}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.TaskDefOutputSchema)
      io.littlehorse.sdk.common.proto.TaskDefOutputSchemaOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.TaskDefOuterClass.internal_static_littlehorse_TaskDefOutputSchema_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.TaskDefOuterClass.internal_static_littlehorse_TaskDefOutputSchema_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.TaskDefOutputSchema.class, io.littlehorse.sdk.common.proto.TaskDefOutputSchema.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.TaskDefOutputSchema.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessage
              .alwaysUseFieldBuilders) {
        internalGetValueDefFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      valueDef_ = null;
      if (valueDefBuilder_ != null) {
        valueDefBuilder_.dispose();
        valueDefBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.TaskDefOuterClass.internal_static_littlehorse_TaskDefOutputSchema_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.TaskDefOutputSchema getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.TaskDefOutputSchema.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.TaskDefOutputSchema build() {
      io.littlehorse.sdk.common.proto.TaskDefOutputSchema result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.TaskDefOutputSchema buildPartial() {
      io.littlehorse.sdk.common.proto.TaskDefOutputSchema result = new io.littlehorse.sdk.common.proto.TaskDefOutputSchema(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.TaskDefOutputSchema result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.valueDef_ = valueDefBuilder_ == null
            ? valueDef_
            : valueDefBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.sdk.common.proto.TaskDefOutputSchema) {
        return mergeFrom((io.littlehorse.sdk.common.proto.TaskDefOutputSchema)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.TaskDefOutputSchema other) {
      if (other == io.littlehorse.sdk.common.proto.TaskDefOutputSchema.getDefaultInstance()) return this;
      if (other.hasValueDef()) {
        mergeValueDef(other.getValueDef());
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
                  internalGetValueDefFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
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

    private io.littlehorse.sdk.common.proto.VariableDef valueDef_;
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.VariableDef, io.littlehorse.sdk.common.proto.VariableDef.Builder, io.littlehorse.sdk.common.proto.VariableDefOrBuilder> valueDefBuilder_;
    /**
     * <pre>
     * The definition for the output content
     * </pre>
     *
     * <code>.littlehorse.VariableDef value_def = 1;</code>
     * @return Whether the valueDef field is set.
     */
    public boolean hasValueDef() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * The definition for the output content
     * </pre>
     *
     * <code>.littlehorse.VariableDef value_def = 1;</code>
     * @return The valueDef.
     */
    public io.littlehorse.sdk.common.proto.VariableDef getValueDef() {
      if (valueDefBuilder_ == null) {
        return valueDef_ == null ? io.littlehorse.sdk.common.proto.VariableDef.getDefaultInstance() : valueDef_;
      } else {
        return valueDefBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The definition for the output content
     * </pre>
     *
     * <code>.littlehorse.VariableDef value_def = 1;</code>
     */
    public Builder setValueDef(io.littlehorse.sdk.common.proto.VariableDef value) {
      if (valueDefBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        valueDef_ = value;
      } else {
        valueDefBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The definition for the output content
     * </pre>
     *
     * <code>.littlehorse.VariableDef value_def = 1;</code>
     */
    public Builder setValueDef(
        io.littlehorse.sdk.common.proto.VariableDef.Builder builderForValue) {
      if (valueDefBuilder_ == null) {
        valueDef_ = builderForValue.build();
      } else {
        valueDefBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The definition for the output content
     * </pre>
     *
     * <code>.littlehorse.VariableDef value_def = 1;</code>
     */
    public Builder mergeValueDef(io.littlehorse.sdk.common.proto.VariableDef value) {
      if (valueDefBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          valueDef_ != null &&
          valueDef_ != io.littlehorse.sdk.common.proto.VariableDef.getDefaultInstance()) {
          getValueDefBuilder().mergeFrom(value);
        } else {
          valueDef_ = value;
        }
      } else {
        valueDefBuilder_.mergeFrom(value);
      }
      if (valueDef_ != null) {
        bitField0_ |= 0x00000001;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * The definition for the output content
     * </pre>
     *
     * <code>.littlehorse.VariableDef value_def = 1;</code>
     */
    public Builder clearValueDef() {
      bitField0_ = (bitField0_ & ~0x00000001);
      valueDef_ = null;
      if (valueDefBuilder_ != null) {
        valueDefBuilder_.dispose();
        valueDefBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The definition for the output content
     * </pre>
     *
     * <code>.littlehorse.VariableDef value_def = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.VariableDef.Builder getValueDefBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return internalGetValueDefFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The definition for the output content
     * </pre>
     *
     * <code>.littlehorse.VariableDef value_def = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.VariableDefOrBuilder getValueDefOrBuilder() {
      if (valueDefBuilder_ != null) {
        return valueDefBuilder_.getMessageOrBuilder();
      } else {
        return valueDef_ == null ?
            io.littlehorse.sdk.common.proto.VariableDef.getDefaultInstance() : valueDef_;
      }
    }
    /**
     * <pre>
     * The definition for the output content
     * </pre>
     *
     * <code>.littlehorse.VariableDef value_def = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.VariableDef, io.littlehorse.sdk.common.proto.VariableDef.Builder, io.littlehorse.sdk.common.proto.VariableDefOrBuilder> 
        internalGetValueDefFieldBuilder() {
      if (valueDefBuilder_ == null) {
        valueDefBuilder_ = new com.google.protobuf.SingleFieldBuilder<
            io.littlehorse.sdk.common.proto.VariableDef, io.littlehorse.sdk.common.proto.VariableDef.Builder, io.littlehorse.sdk.common.proto.VariableDefOrBuilder>(
                getValueDef(),
                getParentForChildren(),
                isClean());
        valueDef_ = null;
      }
      return valueDefBuilder_;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.TaskDefOutputSchema)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.TaskDefOutputSchema)
  private static final io.littlehorse.sdk.common.proto.TaskDefOutputSchema DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.TaskDefOutputSchema();
  }

  public static io.littlehorse.sdk.common.proto.TaskDefOutputSchema getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<TaskDefOutputSchema>
      PARSER = new com.google.protobuf.AbstractParser<TaskDefOutputSchema>() {
    @java.lang.Override
    public TaskDefOutputSchema parsePartialFrom(
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

  public static com.google.protobuf.Parser<TaskDefOutputSchema> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<TaskDefOutputSchema> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TaskDefOutputSchema getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

