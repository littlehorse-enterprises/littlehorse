// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: wf_spec.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.JsonIndex}
 */
public final class JsonIndex extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.JsonIndex)
    JsonIndexOrBuilder {
private static final long serialVersionUID = 0L;
  // Use JsonIndex.newBuilder() to construct.
  private JsonIndex(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private JsonIndex() {
    fieldPath_ = "";
    fieldType_ = 0;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new JsonIndex();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_JsonIndex_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_JsonIndex_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.JsonIndex.class, io.littlehorse.sdk.common.proto.JsonIndex.Builder.class);
  }

  public static final int FIELD_PATH_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object fieldPath_ = "";
  /**
   * <code>string field_path = 1;</code>
   * @return The fieldPath.
   */
  @java.lang.Override
  public java.lang.String getFieldPath() {
    java.lang.Object ref = fieldPath_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      fieldPath_ = s;
      return s;
    }
  }
  /**
   * <code>string field_path = 1;</code>
   * @return The bytes for fieldPath.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getFieldPathBytes() {
    java.lang.Object ref = fieldPath_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      fieldPath_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int FIELD_TYPE_FIELD_NUMBER = 2;
  private int fieldType_ = 0;
  /**
   * <code>.littlehorse.VariableType field_type = 2;</code>
   * @return The enum numeric value on the wire for fieldType.
   */
  @java.lang.Override public int getFieldTypeValue() {
    return fieldType_;
  }
  /**
   * <code>.littlehorse.VariableType field_type = 2;</code>
   * @return The fieldType.
   */
  @java.lang.Override public io.littlehorse.sdk.common.proto.VariableType getFieldType() {
    io.littlehorse.sdk.common.proto.VariableType result = io.littlehorse.sdk.common.proto.VariableType.forNumber(fieldType_);
    return result == null ? io.littlehorse.sdk.common.proto.VariableType.UNRECOGNIZED : result;
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(fieldPath_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, fieldPath_);
    }
    if (fieldType_ != io.littlehorse.sdk.common.proto.VariableType.JSON_OBJ.getNumber()) {
      output.writeEnum(2, fieldType_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(fieldPath_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, fieldPath_);
    }
    if (fieldType_ != io.littlehorse.sdk.common.proto.VariableType.JSON_OBJ.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(2, fieldType_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.JsonIndex)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.JsonIndex other = (io.littlehorse.sdk.common.proto.JsonIndex) obj;

    if (!getFieldPath()
        .equals(other.getFieldPath())) return false;
    if (fieldType_ != other.fieldType_) return false;
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
    hash = (37 * hash) + FIELD_PATH_FIELD_NUMBER;
    hash = (53 * hash) + getFieldPath().hashCode();
    hash = (37 * hash) + FIELD_TYPE_FIELD_NUMBER;
    hash = (53 * hash) + fieldType_;
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.JsonIndex parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.JsonIndex parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.JsonIndex parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.JsonIndex parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.JsonIndex parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.JsonIndex parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.JsonIndex parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.JsonIndex parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.JsonIndex parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.JsonIndex parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.JsonIndex parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.JsonIndex parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.JsonIndex prototype) {
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
   * Protobuf type {@code littlehorse.JsonIndex}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.JsonIndex)
      io.littlehorse.sdk.common.proto.JsonIndexOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_JsonIndex_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_JsonIndex_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.JsonIndex.class, io.littlehorse.sdk.common.proto.JsonIndex.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.JsonIndex.newBuilder()
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
      fieldPath_ = "";
      fieldType_ = 0;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_JsonIndex_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.JsonIndex getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.JsonIndex.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.JsonIndex build() {
      io.littlehorse.sdk.common.proto.JsonIndex result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.JsonIndex buildPartial() {
      io.littlehorse.sdk.common.proto.JsonIndex result = new io.littlehorse.sdk.common.proto.JsonIndex(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.JsonIndex result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.fieldPath_ = fieldPath_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.fieldType_ = fieldType_;
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
      if (other instanceof io.littlehorse.sdk.common.proto.JsonIndex) {
        return mergeFrom((io.littlehorse.sdk.common.proto.JsonIndex)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.JsonIndex other) {
      if (other == io.littlehorse.sdk.common.proto.JsonIndex.getDefaultInstance()) return this;
      if (!other.getFieldPath().isEmpty()) {
        fieldPath_ = other.fieldPath_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (other.fieldType_ != 0) {
        setFieldTypeValue(other.getFieldTypeValue());
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
              fieldPath_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 16: {
              fieldType_ = input.readEnum();
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

    private java.lang.Object fieldPath_ = "";
    /**
     * <code>string field_path = 1;</code>
     * @return The fieldPath.
     */
    public java.lang.String getFieldPath() {
      java.lang.Object ref = fieldPath_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        fieldPath_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string field_path = 1;</code>
     * @return The bytes for fieldPath.
     */
    public com.google.protobuf.ByteString
        getFieldPathBytes() {
      java.lang.Object ref = fieldPath_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        fieldPath_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string field_path = 1;</code>
     * @param value The fieldPath to set.
     * @return This builder for chaining.
     */
    public Builder setFieldPath(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      fieldPath_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>string field_path = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearFieldPath() {
      fieldPath_ = getDefaultInstance().getFieldPath();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>string field_path = 1;</code>
     * @param value The bytes for fieldPath to set.
     * @return This builder for chaining.
     */
    public Builder setFieldPathBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      fieldPath_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private int fieldType_ = 0;
    /**
     * <code>.littlehorse.VariableType field_type = 2;</code>
     * @return The enum numeric value on the wire for fieldType.
     */
    @java.lang.Override public int getFieldTypeValue() {
      return fieldType_;
    }
    /**
     * <code>.littlehorse.VariableType field_type = 2;</code>
     * @param value The enum numeric value on the wire for fieldType to set.
     * @return This builder for chaining.
     */
    public Builder setFieldTypeValue(int value) {
      fieldType_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.VariableType field_type = 2;</code>
     * @return The fieldType.
     */
    @java.lang.Override
    public io.littlehorse.sdk.common.proto.VariableType getFieldType() {
      io.littlehorse.sdk.common.proto.VariableType result = io.littlehorse.sdk.common.proto.VariableType.forNumber(fieldType_);
      return result == null ? io.littlehorse.sdk.common.proto.VariableType.UNRECOGNIZED : result;
    }
    /**
     * <code>.littlehorse.VariableType field_type = 2;</code>
     * @param value The fieldType to set.
     * @return This builder for chaining.
     */
    public Builder setFieldType(io.littlehorse.sdk.common.proto.VariableType value) {
      if (value == null) {
        throw new NullPointerException();
      }
      bitField0_ |= 0x00000002;
      fieldType_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.VariableType field_type = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearFieldType() {
      bitField0_ = (bitField0_ & ~0x00000002);
      fieldType_ = 0;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.JsonIndex)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.JsonIndex)
  private static final io.littlehorse.sdk.common.proto.JsonIndex DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.JsonIndex();
  }

  public static io.littlehorse.sdk.common.proto.JsonIndex getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<JsonIndex>
      PARSER = new com.google.protobuf.AbstractParser<JsonIndex>() {
    @java.lang.Override
    public JsonIndex parsePartialFrom(
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

  public static com.google.protobuf.Parser<JsonIndex> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<JsonIndex> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.JsonIndex getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

