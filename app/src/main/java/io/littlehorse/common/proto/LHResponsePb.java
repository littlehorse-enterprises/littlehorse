// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: lh_proto.proto

package io.littlehorse.common.proto;

/**
 * Protobuf type {@code lh_proto.LHResponsePb}
 */
public final class LHResponsePb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:lh_proto.LHResponsePb)
    LHResponsePbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use LHResponsePb.newBuilder() to construct.
  private LHResponsePb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private LHResponsePb() {
    code_ = 0;
    id_ = "";
    message_ = "";
    resultClass_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new LHResponsePb();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private LHResponsePb(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 8: {
            int rawValue = input.readEnum();

            code_ = rawValue;
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();
            bitField0_ |= 0x00000001;
            id_ = s;
            break;
          }
          case 26: {
            java.lang.String s = input.readStringRequireUtf8();
            bitField0_ |= 0x00000002;
            message_ = s;
            break;
          }
          case 34: {
            com.google.protobuf.Any.Builder subBuilder = null;
            if (((bitField0_ & 0x00000004) != 0)) {
              subBuilder = result_.toBuilder();
            }
            result_ = input.readMessage(com.google.protobuf.Any.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(result_);
              result_ = subBuilder.buildPartial();
            }
            bitField0_ |= 0x00000004;
            break;
          }
          case 42: {
            java.lang.String s = input.readStringRequireUtf8();
            bitField0_ |= 0x00000008;
            resultClass_ = s;
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.LhProto.internal_static_lh_proto_LHResponsePb_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.LhProto.internal_static_lh_proto_LHResponsePb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.LHResponsePb.class, io.littlehorse.common.proto.LHResponsePb.Builder.class);
  }

  private int bitField0_;
  public static final int CODE_FIELD_NUMBER = 1;
  private int code_;
  /**
   * <code>.lh_proto.LHResponseCodePb code = 1;</code>
   * @return The enum numeric value on the wire for code.
   */
  @java.lang.Override public int getCodeValue() {
    return code_;
  }
  /**
   * <code>.lh_proto.LHResponseCodePb code = 1;</code>
   * @return The code.
   */
  @java.lang.Override public io.littlehorse.common.proto.LHResponseCodePb getCode() {
    @SuppressWarnings("deprecation")
    io.littlehorse.common.proto.LHResponseCodePb result = io.littlehorse.common.proto.LHResponseCodePb.valueOf(code_);
    return result == null ? io.littlehorse.common.proto.LHResponseCodePb.UNRECOGNIZED : result;
  }

  public static final int ID_FIELD_NUMBER = 2;
  private volatile java.lang.Object id_;
  /**
   * <code>string id = 2;</code>
   * @return Whether the id field is set.
   */
  @java.lang.Override
  public boolean hasId() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>string id = 2;</code>
   * @return The id.
   */
  @java.lang.Override
  public java.lang.String getId() {
    java.lang.Object ref = id_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      id_ = s;
      return s;
    }
  }
  /**
   * <code>string id = 2;</code>
   * @return The bytes for id.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getIdBytes() {
    java.lang.Object ref = id_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      id_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int MESSAGE_FIELD_NUMBER = 3;
  private volatile java.lang.Object message_;
  /**
   * <code>string message = 3;</code>
   * @return Whether the message field is set.
   */
  @java.lang.Override
  public boolean hasMessage() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>string message = 3;</code>
   * @return The message.
   */
  @java.lang.Override
  public java.lang.String getMessage() {
    java.lang.Object ref = message_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      message_ = s;
      return s;
    }
  }
  /**
   * <code>string message = 3;</code>
   * @return The bytes for message.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getMessageBytes() {
    java.lang.Object ref = message_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      message_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int RESULT_FIELD_NUMBER = 4;
  private com.google.protobuf.Any result_;
  /**
   * <pre>
   * optional bytes result = 4;
   * </pre>
   *
   * <code>.google.protobuf.Any result = 4;</code>
   * @return Whether the result field is set.
   */
  @java.lang.Override
  public boolean hasResult() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <pre>
   * optional bytes result = 4;
   * </pre>
   *
   * <code>.google.protobuf.Any result = 4;</code>
   * @return The result.
   */
  @java.lang.Override
  public com.google.protobuf.Any getResult() {
    return result_ == null ? com.google.protobuf.Any.getDefaultInstance() : result_;
  }
  /**
   * <pre>
   * optional bytes result = 4;
   * </pre>
   *
   * <code>.google.protobuf.Any result = 4;</code>
   */
  @java.lang.Override
  public com.google.protobuf.AnyOrBuilder getResultOrBuilder() {
    return result_ == null ? com.google.protobuf.Any.getDefaultInstance() : result_;
  }

  public static final int RESULT_CLASS_FIELD_NUMBER = 5;
  private volatile java.lang.Object resultClass_;
  /**
   * <code>string result_class = 5;</code>
   * @return Whether the resultClass field is set.
   */
  @java.lang.Override
  public boolean hasResultClass() {
    return ((bitField0_ & 0x00000008) != 0);
  }
  /**
   * <code>string result_class = 5;</code>
   * @return The resultClass.
   */
  @java.lang.Override
  public java.lang.String getResultClass() {
    java.lang.Object ref = resultClass_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      resultClass_ = s;
      return s;
    }
  }
  /**
   * <code>string result_class = 5;</code>
   * @return The bytes for resultClass.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getResultClassBytes() {
    java.lang.Object ref = resultClass_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      resultClass_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
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
    if (code_ != io.littlehorse.common.proto.LHResponseCodePb.OK.getNumber()) {
      output.writeEnum(1, code_);
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, id_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, message_);
    }
    if (((bitField0_ & 0x00000004) != 0)) {
      output.writeMessage(4, getResult());
    }
    if (((bitField0_ & 0x00000008) != 0)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, resultClass_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (code_ != io.littlehorse.common.proto.LHResponseCodePb.OK.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(1, code_);
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, id_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, message_);
    }
    if (((bitField0_ & 0x00000004) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(4, getResult());
    }
    if (((bitField0_ & 0x00000008) != 0)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, resultClass_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof io.littlehorse.common.proto.LHResponsePb)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.LHResponsePb other = (io.littlehorse.common.proto.LHResponsePb) obj;

    if (code_ != other.code_) return false;
    if (hasId() != other.hasId()) return false;
    if (hasId()) {
      if (!getId()
          .equals(other.getId())) return false;
    }
    if (hasMessage() != other.hasMessage()) return false;
    if (hasMessage()) {
      if (!getMessage()
          .equals(other.getMessage())) return false;
    }
    if (hasResult() != other.hasResult()) return false;
    if (hasResult()) {
      if (!getResult()
          .equals(other.getResult())) return false;
    }
    if (hasResultClass() != other.hasResultClass()) return false;
    if (hasResultClass()) {
      if (!getResultClass()
          .equals(other.getResultClass())) return false;
    }
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + CODE_FIELD_NUMBER;
    hash = (53 * hash) + code_;
    if (hasId()) {
      hash = (37 * hash) + ID_FIELD_NUMBER;
      hash = (53 * hash) + getId().hashCode();
    }
    if (hasMessage()) {
      hash = (37 * hash) + MESSAGE_FIELD_NUMBER;
      hash = (53 * hash) + getMessage().hashCode();
    }
    if (hasResult()) {
      hash = (37 * hash) + RESULT_FIELD_NUMBER;
      hash = (53 * hash) + getResult().hashCode();
    }
    if (hasResultClass()) {
      hash = (37 * hash) + RESULT_CLASS_FIELD_NUMBER;
      hash = (53 * hash) + getResultClass().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.LHResponsePb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.LHResponsePb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.LHResponsePb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.LHResponsePb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.LHResponsePb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.LHResponsePb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.LHResponsePb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.LHResponsePb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.LHResponsePb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.LHResponsePb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.LHResponsePb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.LHResponsePb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.LHResponsePb prototype) {
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
   * Protobuf type {@code lh_proto.LHResponsePb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:lh_proto.LHResponsePb)
      io.littlehorse.common.proto.LHResponsePbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.LhProto.internal_static_lh_proto_LHResponsePb_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.LhProto.internal_static_lh_proto_LHResponsePb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.LHResponsePb.class, io.littlehorse.common.proto.LHResponsePb.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.LHResponsePb.newBuilder()
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
        getResultFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      code_ = 0;

      id_ = "";
      bitField0_ = (bitField0_ & ~0x00000001);
      message_ = "";
      bitField0_ = (bitField0_ & ~0x00000002);
      if (resultBuilder_ == null) {
        result_ = null;
      } else {
        resultBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000004);
      resultClass_ = "";
      bitField0_ = (bitField0_ & ~0x00000008);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.LhProto.internal_static_lh_proto_LHResponsePb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.LHResponsePb getDefaultInstanceForType() {
      return io.littlehorse.common.proto.LHResponsePb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.LHResponsePb build() {
      io.littlehorse.common.proto.LHResponsePb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.LHResponsePb buildPartial() {
      io.littlehorse.common.proto.LHResponsePb result = new io.littlehorse.common.proto.LHResponsePb(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.code_ = code_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        to_bitField0_ |= 0x00000001;
      }
      result.id_ = id_;
      if (((from_bitField0_ & 0x00000002) != 0)) {
        to_bitField0_ |= 0x00000002;
      }
      result.message_ = message_;
      if (((from_bitField0_ & 0x00000004) != 0)) {
        if (resultBuilder_ == null) {
          result.result_ = result_;
        } else {
          result.result_ = resultBuilder_.build();
        }
        to_bitField0_ |= 0x00000004;
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        to_bitField0_ |= 0x00000008;
      }
      result.resultClass_ = resultClass_;
      result.bitField0_ = to_bitField0_;
      onBuilt();
      return result;
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
      if (other instanceof io.littlehorse.common.proto.LHResponsePb) {
        return mergeFrom((io.littlehorse.common.proto.LHResponsePb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.LHResponsePb other) {
      if (other == io.littlehorse.common.proto.LHResponsePb.getDefaultInstance()) return this;
      if (other.code_ != 0) {
        setCodeValue(other.getCodeValue());
      }
      if (other.hasId()) {
        bitField0_ |= 0x00000001;
        id_ = other.id_;
        onChanged();
      }
      if (other.hasMessage()) {
        bitField0_ |= 0x00000002;
        message_ = other.message_;
        onChanged();
      }
      if (other.hasResult()) {
        mergeResult(other.getResult());
      }
      if (other.hasResultClass()) {
        bitField0_ |= 0x00000008;
        resultClass_ = other.resultClass_;
        onChanged();
      }
      this.mergeUnknownFields(other.unknownFields);
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
      io.littlehorse.common.proto.LHResponsePb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (io.littlehorse.common.proto.LHResponsePb) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private int code_ = 0;
    /**
     * <code>.lh_proto.LHResponseCodePb code = 1;</code>
     * @return The enum numeric value on the wire for code.
     */
    @java.lang.Override public int getCodeValue() {
      return code_;
    }
    /**
     * <code>.lh_proto.LHResponseCodePb code = 1;</code>
     * @param value The enum numeric value on the wire for code to set.
     * @return This builder for chaining.
     */
    public Builder setCodeValue(int value) {
      
      code_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>.lh_proto.LHResponseCodePb code = 1;</code>
     * @return The code.
     */
    @java.lang.Override
    public io.littlehorse.common.proto.LHResponseCodePb getCode() {
      @SuppressWarnings("deprecation")
      io.littlehorse.common.proto.LHResponseCodePb result = io.littlehorse.common.proto.LHResponseCodePb.valueOf(code_);
      return result == null ? io.littlehorse.common.proto.LHResponseCodePb.UNRECOGNIZED : result;
    }
    /**
     * <code>.lh_proto.LHResponseCodePb code = 1;</code>
     * @param value The code to set.
     * @return This builder for chaining.
     */
    public Builder setCode(io.littlehorse.common.proto.LHResponseCodePb value) {
      if (value == null) {
        throw new NullPointerException();
      }
      
      code_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.lh_proto.LHResponseCodePb code = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearCode() {
      
      code_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object id_ = "";
    /**
     * <code>string id = 2;</code>
     * @return Whether the id field is set.
     */
    public boolean hasId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>string id = 2;</code>
     * @return The id.
     */
    public java.lang.String getId() {
      java.lang.Object ref = id_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        id_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string id = 2;</code>
     * @return The bytes for id.
     */
    public com.google.protobuf.ByteString
        getIdBytes() {
      java.lang.Object ref = id_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        id_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string id = 2;</code>
     * @param value The id to set.
     * @return This builder for chaining.
     */
    public Builder setId(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
      id_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string id = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      id_ = getDefaultInstance().getId();
      onChanged();
      return this;
    }
    /**
     * <code>string id = 2;</code>
     * @param value The bytes for id to set.
     * @return This builder for chaining.
     */
    public Builder setIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      bitField0_ |= 0x00000001;
      id_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object message_ = "";
    /**
     * <code>string message = 3;</code>
     * @return Whether the message field is set.
     */
    public boolean hasMessage() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>string message = 3;</code>
     * @return The message.
     */
    public java.lang.String getMessage() {
      java.lang.Object ref = message_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        message_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string message = 3;</code>
     * @return The bytes for message.
     */
    public com.google.protobuf.ByteString
        getMessageBytes() {
      java.lang.Object ref = message_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        message_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string message = 3;</code>
     * @param value The message to set.
     * @return This builder for chaining.
     */
    public Builder setMessage(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
      message_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string message = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearMessage() {
      bitField0_ = (bitField0_ & ~0x00000002);
      message_ = getDefaultInstance().getMessage();
      onChanged();
      return this;
    }
    /**
     * <code>string message = 3;</code>
     * @param value The bytes for message to set.
     * @return This builder for chaining.
     */
    public Builder setMessageBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      bitField0_ |= 0x00000002;
      message_ = value;
      onChanged();
      return this;
    }

    private com.google.protobuf.Any result_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> resultBuilder_;
    /**
     * <pre>
     * optional bytes result = 4;
     * </pre>
     *
     * <code>.google.protobuf.Any result = 4;</code>
     * @return Whether the result field is set.
     */
    public boolean hasResult() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <pre>
     * optional bytes result = 4;
     * </pre>
     *
     * <code>.google.protobuf.Any result = 4;</code>
     * @return The result.
     */
    public com.google.protobuf.Any getResult() {
      if (resultBuilder_ == null) {
        return result_ == null ? com.google.protobuf.Any.getDefaultInstance() : result_;
      } else {
        return resultBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * optional bytes result = 4;
     * </pre>
     *
     * <code>.google.protobuf.Any result = 4;</code>
     */
    public Builder setResult(com.google.protobuf.Any value) {
      if (resultBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        result_ = value;
        onChanged();
      } else {
        resultBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000004;
      return this;
    }
    /**
     * <pre>
     * optional bytes result = 4;
     * </pre>
     *
     * <code>.google.protobuf.Any result = 4;</code>
     */
    public Builder setResult(
        com.google.protobuf.Any.Builder builderForValue) {
      if (resultBuilder_ == null) {
        result_ = builderForValue.build();
        onChanged();
      } else {
        resultBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000004;
      return this;
    }
    /**
     * <pre>
     * optional bytes result = 4;
     * </pre>
     *
     * <code>.google.protobuf.Any result = 4;</code>
     */
    public Builder mergeResult(com.google.protobuf.Any value) {
      if (resultBuilder_ == null) {
        if (((bitField0_ & 0x00000004) != 0) &&
            result_ != null &&
            result_ != com.google.protobuf.Any.getDefaultInstance()) {
          result_ =
            com.google.protobuf.Any.newBuilder(result_).mergeFrom(value).buildPartial();
        } else {
          result_ = value;
        }
        onChanged();
      } else {
        resultBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000004;
      return this;
    }
    /**
     * <pre>
     * optional bytes result = 4;
     * </pre>
     *
     * <code>.google.protobuf.Any result = 4;</code>
     */
    public Builder clearResult() {
      if (resultBuilder_ == null) {
        result_ = null;
        onChanged();
      } else {
        resultBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000004);
      return this;
    }
    /**
     * <pre>
     * optional bytes result = 4;
     * </pre>
     *
     * <code>.google.protobuf.Any result = 4;</code>
     */
    public com.google.protobuf.Any.Builder getResultBuilder() {
      bitField0_ |= 0x00000004;
      onChanged();
      return getResultFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * optional bytes result = 4;
     * </pre>
     *
     * <code>.google.protobuf.Any result = 4;</code>
     */
    public com.google.protobuf.AnyOrBuilder getResultOrBuilder() {
      if (resultBuilder_ != null) {
        return resultBuilder_.getMessageOrBuilder();
      } else {
        return result_ == null ?
            com.google.protobuf.Any.getDefaultInstance() : result_;
      }
    }
    /**
     * <pre>
     * optional bytes result = 4;
     * </pre>
     *
     * <code>.google.protobuf.Any result = 4;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> 
        getResultFieldBuilder() {
      if (resultBuilder_ == null) {
        resultBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder>(
                getResult(),
                getParentForChildren(),
                isClean());
        result_ = null;
      }
      return resultBuilder_;
    }

    private java.lang.Object resultClass_ = "";
    /**
     * <code>string result_class = 5;</code>
     * @return Whether the resultClass field is set.
     */
    public boolean hasResultClass() {
      return ((bitField0_ & 0x00000008) != 0);
    }
    /**
     * <code>string result_class = 5;</code>
     * @return The resultClass.
     */
    public java.lang.String getResultClass() {
      java.lang.Object ref = resultClass_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        resultClass_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string result_class = 5;</code>
     * @return The bytes for resultClass.
     */
    public com.google.protobuf.ByteString
        getResultClassBytes() {
      java.lang.Object ref = resultClass_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        resultClass_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string result_class = 5;</code>
     * @param value The resultClass to set.
     * @return This builder for chaining.
     */
    public Builder setResultClass(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000008;
      resultClass_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string result_class = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearResultClass() {
      bitField0_ = (bitField0_ & ~0x00000008);
      resultClass_ = getDefaultInstance().getResultClass();
      onChanged();
      return this;
    }
    /**
     * <code>string result_class = 5;</code>
     * @param value The bytes for resultClass to set.
     * @return This builder for chaining.
     */
    public Builder setResultClassBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      bitField0_ |= 0x00000008;
      resultClass_ = value;
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


    // @@protoc_insertion_point(builder_scope:lh_proto.LHResponsePb)
  }

  // @@protoc_insertion_point(class_scope:lh_proto.LHResponsePb)
  private static final io.littlehorse.common.proto.LHResponsePb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.LHResponsePb();
  }

  public static io.littlehorse.common.proto.LHResponsePb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<LHResponsePb>
      PARSER = new com.google.protobuf.AbstractParser<LHResponsePb>() {
    @java.lang.Override
    public LHResponsePb parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new LHResponsePb(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<LHResponsePb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<LHResponsePb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.LHResponsePb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

