// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: struct_def.proto

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * A `SchemaFieldDef` defines a field inside a `StructDef`.
 * </pre>
 *
 * Protobuf type {@code littlehorse.StructFieldDef}
 */
public final class StructFieldDef extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.StructFieldDef)
    StructFieldDefOrBuilder {
private static final long serialVersionUID = 0L;
  // Use StructFieldDef.newBuilder() to construct.
  private StructFieldDef(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private StructFieldDef() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new StructFieldDef();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.StructDefOuterClass.internal_static_littlehorse_StructFieldDef_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.StructDefOuterClass.internal_static_littlehorse_StructFieldDef_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.StructFieldDef.class, io.littlehorse.sdk.common.proto.StructFieldDef.Builder.class);
  }

  private int bitField0_;
  public static final int FIELD_TYPE_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.TypeDefinition fieldType_;
  /**
   * <pre>
   * The type of the field.
   * </pre>
   *
   * <code>.littlehorse.TypeDefinition field_type = 1;</code>
   * @return Whether the fieldType field is set.
   */
  @java.lang.Override
  public boolean hasFieldType() {
    return fieldType_ != null;
  }
  /**
   * <pre>
   * The type of the field.
   * </pre>
   *
   * <code>.littlehorse.TypeDefinition field_type = 1;</code>
   * @return The fieldType.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TypeDefinition getFieldType() {
    return fieldType_ == null ? io.littlehorse.sdk.common.proto.TypeDefinition.getDefaultInstance() : fieldType_;
  }
  /**
   * <pre>
   * The type of the field.
   * </pre>
   *
   * <code>.littlehorse.TypeDefinition field_type = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TypeDefinitionOrBuilder getFieldTypeOrBuilder() {
    return fieldType_ == null ? io.littlehorse.sdk.common.proto.TypeDefinition.getDefaultInstance() : fieldType_;
  }

  public static final int DEFAULT_VALUE_FIELD_NUMBER = 2;
  private io.littlehorse.sdk.common.proto.VariableValue defaultValue_;
  /**
   * <pre>
   * The default value of the field, which should match the Field Type. If not
   * provided, then the field is treated as required.
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue default_value = 2;</code>
   * @return Whether the defaultValue field is set.
   */
  @java.lang.Override
  public boolean hasDefaultValue() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * The default value of the field, which should match the Field Type. If not
   * provided, then the field is treated as required.
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue default_value = 2;</code>
   * @return The defaultValue.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.VariableValue getDefaultValue() {
    return defaultValue_ == null ? io.littlehorse.sdk.common.proto.VariableValue.getDefaultInstance() : defaultValue_;
  }
  /**
   * <pre>
   * The default value of the field, which should match the Field Type. If not
   * provided, then the field is treated as required.
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue default_value = 2;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.VariableValueOrBuilder getDefaultValueOrBuilder() {
    return defaultValue_ == null ? io.littlehorse.sdk.common.proto.VariableValue.getDefaultInstance() : defaultValue_;
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
    if (fieldType_ != null) {
      output.writeMessage(1, getFieldType());
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      output.writeMessage(2, getDefaultValue());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (fieldType_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getFieldType());
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getDefaultValue());
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.StructFieldDef)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.StructFieldDef other = (io.littlehorse.sdk.common.proto.StructFieldDef) obj;

    if (hasFieldType() != other.hasFieldType()) return false;
    if (hasFieldType()) {
      if (!getFieldType()
          .equals(other.getFieldType())) return false;
    }
    if (hasDefaultValue() != other.hasDefaultValue()) return false;
    if (hasDefaultValue()) {
      if (!getDefaultValue()
          .equals(other.getDefaultValue())) return false;
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
    if (hasFieldType()) {
      hash = (37 * hash) + FIELD_TYPE_FIELD_NUMBER;
      hash = (53 * hash) + getFieldType().hashCode();
    }
    if (hasDefaultValue()) {
      hash = (37 * hash) + DEFAULT_VALUE_FIELD_NUMBER;
      hash = (53 * hash) + getDefaultValue().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.StructFieldDef parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.StructFieldDef parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.StructFieldDef parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.StructFieldDef parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.StructFieldDef parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.StructFieldDef parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.StructFieldDef parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.StructFieldDef parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.StructFieldDef parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.StructFieldDef parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.StructFieldDef parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.StructFieldDef parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.StructFieldDef prototype) {
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
   * A `SchemaFieldDef` defines a field inside a `StructDef`.
   * </pre>
   *
   * Protobuf type {@code littlehorse.StructFieldDef}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.StructFieldDef)
      io.littlehorse.sdk.common.proto.StructFieldDefOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.StructDefOuterClass.internal_static_littlehorse_StructFieldDef_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.StructDefOuterClass.internal_static_littlehorse_StructFieldDef_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.StructFieldDef.class, io.littlehorse.sdk.common.proto.StructFieldDef.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.StructFieldDef.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
        getFieldTypeFieldBuilder();
        getDefaultValueFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      fieldType_ = null;
      if (fieldTypeBuilder_ != null) {
        fieldTypeBuilder_.dispose();
        fieldTypeBuilder_ = null;
      }
      defaultValue_ = null;
      if (defaultValueBuilder_ != null) {
        defaultValueBuilder_.dispose();
        defaultValueBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.StructDefOuterClass.internal_static_littlehorse_StructFieldDef_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.StructFieldDef getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.StructFieldDef.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.StructFieldDef build() {
      io.littlehorse.sdk.common.proto.StructFieldDef result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.StructFieldDef buildPartial() {
      io.littlehorse.sdk.common.proto.StructFieldDef result = new io.littlehorse.sdk.common.proto.StructFieldDef(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.StructFieldDef result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.fieldType_ = fieldTypeBuilder_ == null
            ? fieldType_
            : fieldTypeBuilder_.build();
      }
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.defaultValue_ = defaultValueBuilder_ == null
            ? defaultValue_
            : defaultValueBuilder_.build();
        to_bitField0_ |= 0x00000001;
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
      if (other instanceof io.littlehorse.sdk.common.proto.StructFieldDef) {
        return mergeFrom((io.littlehorse.sdk.common.proto.StructFieldDef)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.StructFieldDef other) {
      if (other == io.littlehorse.sdk.common.proto.StructFieldDef.getDefaultInstance()) return this;
      if (other.hasFieldType()) {
        mergeFieldType(other.getFieldType());
      }
      if (other.hasDefaultValue()) {
        mergeDefaultValue(other.getDefaultValue());
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
                  getFieldTypeFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getDefaultValueFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
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

    private io.littlehorse.sdk.common.proto.TypeDefinition fieldType_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.TypeDefinition, io.littlehorse.sdk.common.proto.TypeDefinition.Builder, io.littlehorse.sdk.common.proto.TypeDefinitionOrBuilder> fieldTypeBuilder_;
    /**
     * <pre>
     * The type of the field.
     * </pre>
     *
     * <code>.littlehorse.TypeDefinition field_type = 1;</code>
     * @return Whether the fieldType field is set.
     */
    public boolean hasFieldType() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * The type of the field.
     * </pre>
     *
     * <code>.littlehorse.TypeDefinition field_type = 1;</code>
     * @return The fieldType.
     */
    public io.littlehorse.sdk.common.proto.TypeDefinition getFieldType() {
      if (fieldTypeBuilder_ == null) {
        return fieldType_ == null ? io.littlehorse.sdk.common.proto.TypeDefinition.getDefaultInstance() : fieldType_;
      } else {
        return fieldTypeBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The type of the field.
     * </pre>
     *
     * <code>.littlehorse.TypeDefinition field_type = 1;</code>
     */
    public Builder setFieldType(io.littlehorse.sdk.common.proto.TypeDefinition value) {
      if (fieldTypeBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        fieldType_ = value;
      } else {
        fieldTypeBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The type of the field.
     * </pre>
     *
     * <code>.littlehorse.TypeDefinition field_type = 1;</code>
     */
    public Builder setFieldType(
        io.littlehorse.sdk.common.proto.TypeDefinition.Builder builderForValue) {
      if (fieldTypeBuilder_ == null) {
        fieldType_ = builderForValue.build();
      } else {
        fieldTypeBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The type of the field.
     * </pre>
     *
     * <code>.littlehorse.TypeDefinition field_type = 1;</code>
     */
    public Builder mergeFieldType(io.littlehorse.sdk.common.proto.TypeDefinition value) {
      if (fieldTypeBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          fieldType_ != null &&
          fieldType_ != io.littlehorse.sdk.common.proto.TypeDefinition.getDefaultInstance()) {
          getFieldTypeBuilder().mergeFrom(value);
        } else {
          fieldType_ = value;
        }
      } else {
        fieldTypeBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The type of the field.
     * </pre>
     *
     * <code>.littlehorse.TypeDefinition field_type = 1;</code>
     */
    public Builder clearFieldType() {
      bitField0_ = (bitField0_ & ~0x00000001);
      fieldType_ = null;
      if (fieldTypeBuilder_ != null) {
        fieldTypeBuilder_.dispose();
        fieldTypeBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The type of the field.
     * </pre>
     *
     * <code>.littlehorse.TypeDefinition field_type = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.TypeDefinition.Builder getFieldTypeBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getFieldTypeFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The type of the field.
     * </pre>
     *
     * <code>.littlehorse.TypeDefinition field_type = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.TypeDefinitionOrBuilder getFieldTypeOrBuilder() {
      if (fieldTypeBuilder_ != null) {
        return fieldTypeBuilder_.getMessageOrBuilder();
      } else {
        return fieldType_ == null ?
            io.littlehorse.sdk.common.proto.TypeDefinition.getDefaultInstance() : fieldType_;
      }
    }
    /**
     * <pre>
     * The type of the field.
     * </pre>
     *
     * <code>.littlehorse.TypeDefinition field_type = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.TypeDefinition, io.littlehorse.sdk.common.proto.TypeDefinition.Builder, io.littlehorse.sdk.common.proto.TypeDefinitionOrBuilder> 
        getFieldTypeFieldBuilder() {
      if (fieldTypeBuilder_ == null) {
        fieldTypeBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.TypeDefinition, io.littlehorse.sdk.common.proto.TypeDefinition.Builder, io.littlehorse.sdk.common.proto.TypeDefinitionOrBuilder>(
                getFieldType(),
                getParentForChildren(),
                isClean());
        fieldType_ = null;
      }
      return fieldTypeBuilder_;
    }

    private io.littlehorse.sdk.common.proto.VariableValue defaultValue_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.VariableValue, io.littlehorse.sdk.common.proto.VariableValue.Builder, io.littlehorse.sdk.common.proto.VariableValueOrBuilder> defaultValueBuilder_;
    /**
     * <pre>
     * The default value of the field, which should match the Field Type. If not
     * provided, then the field is treated as required.
     * </pre>
     *
     * <code>optional .littlehorse.VariableValue default_value = 2;</code>
     * @return Whether the defaultValue field is set.
     */
    public boolean hasDefaultValue() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * The default value of the field, which should match the Field Type. If not
     * provided, then the field is treated as required.
     * </pre>
     *
     * <code>optional .littlehorse.VariableValue default_value = 2;</code>
     * @return The defaultValue.
     */
    public io.littlehorse.sdk.common.proto.VariableValue getDefaultValue() {
      if (defaultValueBuilder_ == null) {
        return defaultValue_ == null ? io.littlehorse.sdk.common.proto.VariableValue.getDefaultInstance() : defaultValue_;
      } else {
        return defaultValueBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The default value of the field, which should match the Field Type. If not
     * provided, then the field is treated as required.
     * </pre>
     *
     * <code>optional .littlehorse.VariableValue default_value = 2;</code>
     */
    public Builder setDefaultValue(io.littlehorse.sdk.common.proto.VariableValue value) {
      if (defaultValueBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        defaultValue_ = value;
      } else {
        defaultValueBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The default value of the field, which should match the Field Type. If not
     * provided, then the field is treated as required.
     * </pre>
     *
     * <code>optional .littlehorse.VariableValue default_value = 2;</code>
     */
    public Builder setDefaultValue(
        io.littlehorse.sdk.common.proto.VariableValue.Builder builderForValue) {
      if (defaultValueBuilder_ == null) {
        defaultValue_ = builderForValue.build();
      } else {
        defaultValueBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The default value of the field, which should match the Field Type. If not
     * provided, then the field is treated as required.
     * </pre>
     *
     * <code>optional .littlehorse.VariableValue default_value = 2;</code>
     */
    public Builder mergeDefaultValue(io.littlehorse.sdk.common.proto.VariableValue value) {
      if (defaultValueBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          defaultValue_ != null &&
          defaultValue_ != io.littlehorse.sdk.common.proto.VariableValue.getDefaultInstance()) {
          getDefaultValueBuilder().mergeFrom(value);
        } else {
          defaultValue_ = value;
        }
      } else {
        defaultValueBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The default value of the field, which should match the Field Type. If not
     * provided, then the field is treated as required.
     * </pre>
     *
     * <code>optional .littlehorse.VariableValue default_value = 2;</code>
     */
    public Builder clearDefaultValue() {
      bitField0_ = (bitField0_ & ~0x00000002);
      defaultValue_ = null;
      if (defaultValueBuilder_ != null) {
        defaultValueBuilder_.dispose();
        defaultValueBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The default value of the field, which should match the Field Type. If not
     * provided, then the field is treated as required.
     * </pre>
     *
     * <code>optional .littlehorse.VariableValue default_value = 2;</code>
     */
    public io.littlehorse.sdk.common.proto.VariableValue.Builder getDefaultValueBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getDefaultValueFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The default value of the field, which should match the Field Type. If not
     * provided, then the field is treated as required.
     * </pre>
     *
     * <code>optional .littlehorse.VariableValue default_value = 2;</code>
     */
    public io.littlehorse.sdk.common.proto.VariableValueOrBuilder getDefaultValueOrBuilder() {
      if (defaultValueBuilder_ != null) {
        return defaultValueBuilder_.getMessageOrBuilder();
      } else {
        return defaultValue_ == null ?
            io.littlehorse.sdk.common.proto.VariableValue.getDefaultInstance() : defaultValue_;
      }
    }
    /**
     * <pre>
     * The default value of the field, which should match the Field Type. If not
     * provided, then the field is treated as required.
     * </pre>
     *
     * <code>optional .littlehorse.VariableValue default_value = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.VariableValue, io.littlehorse.sdk.common.proto.VariableValue.Builder, io.littlehorse.sdk.common.proto.VariableValueOrBuilder> 
        getDefaultValueFieldBuilder() {
      if (defaultValueBuilder_ == null) {
        defaultValueBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.VariableValue, io.littlehorse.sdk.common.proto.VariableValue.Builder, io.littlehorse.sdk.common.proto.VariableValueOrBuilder>(
                getDefaultValue(),
                getParentForChildren(),
                isClean());
        defaultValue_ = null;
      }
      return defaultValueBuilder_;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.StructFieldDef)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.StructFieldDef)
  private static final io.littlehorse.sdk.common.proto.StructFieldDef DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.StructFieldDef();
  }

  public static io.littlehorse.sdk.common.proto.StructFieldDef getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<StructFieldDef>
      PARSER = new com.google.protobuf.AbstractParser<StructFieldDef>() {
    @java.lang.Override
    public StructFieldDef parsePartialFrom(
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

  public static com.google.protobuf.Parser<StructFieldDef> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<StructFieldDef> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.StructFieldDef getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

