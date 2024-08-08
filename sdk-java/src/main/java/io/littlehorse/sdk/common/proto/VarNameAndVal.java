// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: task_run.proto

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * A key-value pair of variable name and value.
 * </pre>
 *
 * Protobuf type {@code littlehorse.VarNameAndVal}
 */
public final class VarNameAndVal extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.VarNameAndVal)
    VarNameAndValOrBuilder {
private static final long serialVersionUID = 0L;
  // Use VarNameAndVal.newBuilder() to construct.
  private VarNameAndVal(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private VarNameAndVal() {
    varName_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new VarNameAndVal();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.TaskRunOuterClass.internal_static_littlehorse_VarNameAndVal_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.TaskRunOuterClass.internal_static_littlehorse_VarNameAndVal_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.VarNameAndVal.class, io.littlehorse.sdk.common.proto.VarNameAndVal.Builder.class);
  }

  public static final int VAR_NAME_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object varName_ = "";
  /**
   * <pre>
   * The variable name.
   * </pre>
   *
   * <code>string var_name = 1;</code>
   * @return The varName.
   */
  @java.lang.Override
  public java.lang.String getVarName() {
    java.lang.Object ref = varName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      varName_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The variable name.
   * </pre>
   *
   * <code>string var_name = 1;</code>
   * @return The bytes for varName.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getVarNameBytes() {
    java.lang.Object ref = varName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      varName_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int VALUE_FIELD_NUMBER = 2;
  private io.littlehorse.sdk.common.proto.VariableValue value_;
  /**
   * <pre>
   * The value of the variable for this TaskRun.
   * </pre>
   *
   * <code>.littlehorse.VariableValue value = 2;</code>
   * @return Whether the value field is set.
   */
  @java.lang.Override
  public boolean hasValue() {
    return value_ != null;
  }
  /**
   * <pre>
   * The value of the variable for this TaskRun.
   * </pre>
   *
   * <code>.littlehorse.VariableValue value = 2;</code>
   * @return The value.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.VariableValue getValue() {
    return value_ == null ? io.littlehorse.sdk.common.proto.VariableValue.getDefaultInstance() : value_;
  }
  /**
   * <pre>
   * The value of the variable for this TaskRun.
   * </pre>
   *
   * <code>.littlehorse.VariableValue value = 2;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.VariableValueOrBuilder getValueOrBuilder() {
    return value_ == null ? io.littlehorse.sdk.common.proto.VariableValue.getDefaultInstance() : value_;
  }

  public static final int MASKED_FIELD_NUMBER = 3;
  private boolean masked_ = false;
  /**
   * <code>bool masked = 3;</code>
   * @return The masked.
   */
  @java.lang.Override
  public boolean getMasked() {
    return masked_;
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(varName_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, varName_);
    }
    if (value_ != null) {
      output.writeMessage(2, getValue());
    }
    if (masked_ != false) {
      output.writeBool(3, masked_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(varName_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, varName_);
    }
    if (value_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getValue());
    }
    if (masked_ != false) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(3, masked_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.VarNameAndVal)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.VarNameAndVal other = (io.littlehorse.sdk.common.proto.VarNameAndVal) obj;

    if (!getVarName()
        .equals(other.getVarName())) return false;
    if (hasValue() != other.hasValue()) return false;
    if (hasValue()) {
      if (!getValue()
          .equals(other.getValue())) return false;
    }
    if (getMasked()
        != other.getMasked()) return false;
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
    hash = (37 * hash) + VAR_NAME_FIELD_NUMBER;
    hash = (53 * hash) + getVarName().hashCode();
    if (hasValue()) {
      hash = (37 * hash) + VALUE_FIELD_NUMBER;
      hash = (53 * hash) + getValue().hashCode();
    }
    hash = (37 * hash) + MASKED_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getMasked());
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.VarNameAndVal parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.VarNameAndVal parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.VarNameAndVal parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.VarNameAndVal parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.VarNameAndVal parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.VarNameAndVal parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.VarNameAndVal parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.VarNameAndVal parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.VarNameAndVal parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.VarNameAndVal parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.VarNameAndVal parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.VarNameAndVal parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.VarNameAndVal prototype) {
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
   * A key-value pair of variable name and value.
   * </pre>
   *
   * Protobuf type {@code littlehorse.VarNameAndVal}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.VarNameAndVal)
      io.littlehorse.sdk.common.proto.VarNameAndValOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.TaskRunOuterClass.internal_static_littlehorse_VarNameAndVal_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.TaskRunOuterClass.internal_static_littlehorse_VarNameAndVal_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.VarNameAndVal.class, io.littlehorse.sdk.common.proto.VarNameAndVal.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.VarNameAndVal.newBuilder()
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
      varName_ = "";
      value_ = null;
      if (valueBuilder_ != null) {
        valueBuilder_.dispose();
        valueBuilder_ = null;
      }
      masked_ = false;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.TaskRunOuterClass.internal_static_littlehorse_VarNameAndVal_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.VarNameAndVal getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.VarNameAndVal.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.VarNameAndVal build() {
      io.littlehorse.sdk.common.proto.VarNameAndVal result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.VarNameAndVal buildPartial() {
      io.littlehorse.sdk.common.proto.VarNameAndVal result = new io.littlehorse.sdk.common.proto.VarNameAndVal(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.VarNameAndVal result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.varName_ = varName_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.value_ = valueBuilder_ == null
            ? value_
            : valueBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.masked_ = masked_;
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
      if (other instanceof io.littlehorse.sdk.common.proto.VarNameAndVal) {
        return mergeFrom((io.littlehorse.sdk.common.proto.VarNameAndVal)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.VarNameAndVal other) {
      if (other == io.littlehorse.sdk.common.proto.VarNameAndVal.getDefaultInstance()) return this;
      if (!other.getVarName().isEmpty()) {
        varName_ = other.varName_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (other.hasValue()) {
        mergeValue(other.getValue());
      }
      if (other.getMasked() != false) {
        setMasked(other.getMasked());
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
              varName_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getValueFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 24: {
              masked_ = input.readBool();
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

    private java.lang.Object varName_ = "";
    /**
     * <pre>
     * The variable name.
     * </pre>
     *
     * <code>string var_name = 1;</code>
     * @return The varName.
     */
    public java.lang.String getVarName() {
      java.lang.Object ref = varName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        varName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The variable name.
     * </pre>
     *
     * <code>string var_name = 1;</code>
     * @return The bytes for varName.
     */
    public com.google.protobuf.ByteString
        getVarNameBytes() {
      java.lang.Object ref = varName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        varName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The variable name.
     * </pre>
     *
     * <code>string var_name = 1;</code>
     * @param value The varName to set.
     * @return This builder for chaining.
     */
    public Builder setVarName(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      varName_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The variable name.
     * </pre>
     *
     * <code>string var_name = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearVarName() {
      varName_ = getDefaultInstance().getVarName();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The variable name.
     * </pre>
     *
     * <code>string var_name = 1;</code>
     * @param value The bytes for varName to set.
     * @return This builder for chaining.
     */
    public Builder setVarNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      varName_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private io.littlehorse.sdk.common.proto.VariableValue value_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.VariableValue, io.littlehorse.sdk.common.proto.VariableValue.Builder, io.littlehorse.sdk.common.proto.VariableValueOrBuilder> valueBuilder_;
    /**
     * <pre>
     * The value of the variable for this TaskRun.
     * </pre>
     *
     * <code>.littlehorse.VariableValue value = 2;</code>
     * @return Whether the value field is set.
     */
    public boolean hasValue() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * The value of the variable for this TaskRun.
     * </pre>
     *
     * <code>.littlehorse.VariableValue value = 2;</code>
     * @return The value.
     */
    public io.littlehorse.sdk.common.proto.VariableValue getValue() {
      if (valueBuilder_ == null) {
        return value_ == null ? io.littlehorse.sdk.common.proto.VariableValue.getDefaultInstance() : value_;
      } else {
        return valueBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The value of the variable for this TaskRun.
     * </pre>
     *
     * <code>.littlehorse.VariableValue value = 2;</code>
     */
    public Builder setValue(io.littlehorse.sdk.common.proto.VariableValue value) {
      if (valueBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        value_ = value;
      } else {
        valueBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The value of the variable for this TaskRun.
     * </pre>
     *
     * <code>.littlehorse.VariableValue value = 2;</code>
     */
    public Builder setValue(
        io.littlehorse.sdk.common.proto.VariableValue.Builder builderForValue) {
      if (valueBuilder_ == null) {
        value_ = builderForValue.build();
      } else {
        valueBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The value of the variable for this TaskRun.
     * </pre>
     *
     * <code>.littlehorse.VariableValue value = 2;</code>
     */
    public Builder mergeValue(io.littlehorse.sdk.common.proto.VariableValue value) {
      if (valueBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          value_ != null &&
          value_ != io.littlehorse.sdk.common.proto.VariableValue.getDefaultInstance()) {
          getValueBuilder().mergeFrom(value);
        } else {
          value_ = value;
        }
      } else {
        valueBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The value of the variable for this TaskRun.
     * </pre>
     *
     * <code>.littlehorse.VariableValue value = 2;</code>
     */
    public Builder clearValue() {
      bitField0_ = (bitField0_ & ~0x00000002);
      value_ = null;
      if (valueBuilder_ != null) {
        valueBuilder_.dispose();
        valueBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The value of the variable for this TaskRun.
     * </pre>
     *
     * <code>.littlehorse.VariableValue value = 2;</code>
     */
    public io.littlehorse.sdk.common.proto.VariableValue.Builder getValueBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getValueFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The value of the variable for this TaskRun.
     * </pre>
     *
     * <code>.littlehorse.VariableValue value = 2;</code>
     */
    public io.littlehorse.sdk.common.proto.VariableValueOrBuilder getValueOrBuilder() {
      if (valueBuilder_ != null) {
        return valueBuilder_.getMessageOrBuilder();
      } else {
        return value_ == null ?
            io.littlehorse.sdk.common.proto.VariableValue.getDefaultInstance() : value_;
      }
    }
    /**
     * <pre>
     * The value of the variable for this TaskRun.
     * </pre>
     *
     * <code>.littlehorse.VariableValue value = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.VariableValue, io.littlehorse.sdk.common.proto.VariableValue.Builder, io.littlehorse.sdk.common.proto.VariableValueOrBuilder> 
        getValueFieldBuilder() {
      if (valueBuilder_ == null) {
        valueBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.VariableValue, io.littlehorse.sdk.common.proto.VariableValue.Builder, io.littlehorse.sdk.common.proto.VariableValueOrBuilder>(
                getValue(),
                getParentForChildren(),
                isClean());
        value_ = null;
      }
      return valueBuilder_;
    }

    private boolean masked_ ;
    /**
     * <code>bool masked = 3;</code>
     * @return The masked.
     */
    @java.lang.Override
    public boolean getMasked() {
      return masked_;
    }
    /**
     * <code>bool masked = 3;</code>
     * @param value The masked to set.
     * @return This builder for chaining.
     */
    public Builder setMasked(boolean value) {

      masked_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>bool masked = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearMasked() {
      bitField0_ = (bitField0_ & ~0x00000004);
      masked_ = false;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.VarNameAndVal)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.VarNameAndVal)
  private static final io.littlehorse.sdk.common.proto.VarNameAndVal DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.VarNameAndVal();
  }

  public static io.littlehorse.sdk.common.proto.VarNameAndVal getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<VarNameAndVal>
      PARSER = new com.google.protobuf.AbstractParser<VarNameAndVal>() {
    @java.lang.Override
    public VarNameAndVal parsePartialFrom(
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

  public static com.google.protobuf.Parser<VarNameAndVal> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<VarNameAndVal> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.VarNameAndVal getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

