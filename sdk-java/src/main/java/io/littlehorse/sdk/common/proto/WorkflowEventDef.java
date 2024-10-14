// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: workflow_event.proto

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * The WorkflowEventDef defines the blueprint for a WorkflowEvent.
 * </pre>
 *
 * Protobuf type {@code littlehorse.WorkflowEventDef}
 */
public final class WorkflowEventDef extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.WorkflowEventDef)
    WorkflowEventDefOrBuilder {
private static final long serialVersionUID = 0L;
  // Use WorkflowEventDef.newBuilder() to construct.
  private WorkflowEventDef(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private WorkflowEventDef() {
    type_ = 0;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new WorkflowEventDef();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.WorkflowEventOuterClass.internal_static_littlehorse_WorkflowEventDef_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.WorkflowEventOuterClass.internal_static_littlehorse_WorkflowEventDef_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.WorkflowEventDef.class, io.littlehorse.sdk.common.proto.WorkflowEventDef.Builder.class);
  }

  public static final int ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.WorkflowEventDefId id_;
  /**
   * <pre>
   * The ID of the WorkflowEventDef. Contains the name of the WorkflowEventDef.
   * </pre>
   *
   * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
   * @return Whether the id field is set.
   */
  @java.lang.Override
  public boolean hasId() {
    return id_ != null;
  }
  /**
   * <pre>
   * The ID of the WorkflowEventDef. Contains the name of the WorkflowEventDef.
   * </pre>
   *
   * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
   * @return The id.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WorkflowEventDefId getId() {
    return id_ == null ? io.littlehorse.sdk.common.proto.WorkflowEventDefId.getDefaultInstance() : id_;
  }
  /**
   * <pre>
   * The ID of the WorkflowEventDef. Contains the name of the WorkflowEventDef.
   * </pre>
   *
   * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WorkflowEventDefIdOrBuilder getIdOrBuilder() {
    return id_ == null ? io.littlehorse.sdk.common.proto.WorkflowEventDefId.getDefaultInstance() : id_;
  }

  public static final int CREATED_AT_FIELD_NUMBER = 2;
  private com.google.protobuf.Timestamp createdAt_;
  /**
   * <pre>
   * The time that the WorkflowEventDef was created at.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 2;</code>
   * @return Whether the createdAt field is set.
   */
  @java.lang.Override
  public boolean hasCreatedAt() {
    return createdAt_ != null;
  }
  /**
   * <pre>
   * The time that the WorkflowEventDef was created at.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 2;</code>
   * @return The createdAt.
   */
  @java.lang.Override
  public com.google.protobuf.Timestamp getCreatedAt() {
    return createdAt_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : createdAt_;
  }
  /**
   * <pre>
   * The time that the WorkflowEventDef was created at.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 2;</code>
   */
  @java.lang.Override
  public com.google.protobuf.TimestampOrBuilder getCreatedAtOrBuilder() {
    return createdAt_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : createdAt_;
  }

  public static final int TYPE_FIELD_NUMBER = 3;
  private int type_ = 0;
  /**
   * <pre>
   * The type of the content of a WorkflowEvent based on this WorkflowEventDef.
   * </pre>
   *
   * <code>.littlehorse.VariableType type = 3;</code>
   * @return The enum numeric value on the wire for type.
   */
  @java.lang.Override public int getTypeValue() {
    return type_;
  }
  /**
   * <pre>
   * The type of the content of a WorkflowEvent based on this WorkflowEventDef.
   * </pre>
   *
   * <code>.littlehorse.VariableType type = 3;</code>
   * @return The type.
   */
  @java.lang.Override public io.littlehorse.sdk.common.proto.VariableType getType() {
    io.littlehorse.sdk.common.proto.VariableType result = io.littlehorse.sdk.common.proto.VariableType.forNumber(type_);
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
    if (id_ != null) {
      output.writeMessage(1, getId());
    }
    if (createdAt_ != null) {
      output.writeMessage(2, getCreatedAt());
    }
    if (type_ != io.littlehorse.sdk.common.proto.VariableType.JSON_OBJ.getNumber()) {
      output.writeEnum(3, type_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (id_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getId());
    }
    if (createdAt_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getCreatedAt());
    }
    if (type_ != io.littlehorse.sdk.common.proto.VariableType.JSON_OBJ.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(3, type_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.WorkflowEventDef)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.WorkflowEventDef other = (io.littlehorse.sdk.common.proto.WorkflowEventDef) obj;

    if (hasId() != other.hasId()) return false;
    if (hasId()) {
      if (!getId()
          .equals(other.getId())) return false;
    }
    if (hasCreatedAt() != other.hasCreatedAt()) return false;
    if (hasCreatedAt()) {
      if (!getCreatedAt()
          .equals(other.getCreatedAt())) return false;
    }
    if (type_ != other.type_) return false;
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
    if (hasId()) {
      hash = (37 * hash) + ID_FIELD_NUMBER;
      hash = (53 * hash) + getId().hashCode();
    }
    if (hasCreatedAt()) {
      hash = (37 * hash) + CREATED_AT_FIELD_NUMBER;
      hash = (53 * hash) + getCreatedAt().hashCode();
    }
    hash = (37 * hash) + TYPE_FIELD_NUMBER;
    hash = (53 * hash) + type_;
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.WorkflowEventDef parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.WorkflowEventDef parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.WorkflowEventDef parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.WorkflowEventDef parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.WorkflowEventDef parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.WorkflowEventDef parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.WorkflowEventDef parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.WorkflowEventDef parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.WorkflowEventDef parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.WorkflowEventDef parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.WorkflowEventDef parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.WorkflowEventDef parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.WorkflowEventDef prototype) {
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
   * The WorkflowEventDef defines the blueprint for a WorkflowEvent.
   * </pre>
   *
   * Protobuf type {@code littlehorse.WorkflowEventDef}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.WorkflowEventDef)
      io.littlehorse.sdk.common.proto.WorkflowEventDefOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.WorkflowEventOuterClass.internal_static_littlehorse_WorkflowEventDef_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.WorkflowEventOuterClass.internal_static_littlehorse_WorkflowEventDef_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.WorkflowEventDef.class, io.littlehorse.sdk.common.proto.WorkflowEventDef.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.WorkflowEventDef.newBuilder()
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
      id_ = null;
      if (idBuilder_ != null) {
        idBuilder_.dispose();
        idBuilder_ = null;
      }
      createdAt_ = null;
      if (createdAtBuilder_ != null) {
        createdAtBuilder_.dispose();
        createdAtBuilder_ = null;
      }
      type_ = 0;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.WorkflowEventOuterClass.internal_static_littlehorse_WorkflowEventDef_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.WorkflowEventDef getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.WorkflowEventDef.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.WorkflowEventDef build() {
      io.littlehorse.sdk.common.proto.WorkflowEventDef result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.WorkflowEventDef buildPartial() {
      io.littlehorse.sdk.common.proto.WorkflowEventDef result = new io.littlehorse.sdk.common.proto.WorkflowEventDef(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.WorkflowEventDef result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.id_ = idBuilder_ == null
            ? id_
            : idBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.createdAt_ = createdAtBuilder_ == null
            ? createdAt_
            : createdAtBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.type_ = type_;
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
      if (other instanceof io.littlehorse.sdk.common.proto.WorkflowEventDef) {
        return mergeFrom((io.littlehorse.sdk.common.proto.WorkflowEventDef)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.WorkflowEventDef other) {
      if (other == io.littlehorse.sdk.common.proto.WorkflowEventDef.getDefaultInstance()) return this;
      if (other.hasId()) {
        mergeId(other.getId());
      }
      if (other.hasCreatedAt()) {
        mergeCreatedAt(other.getCreatedAt());
      }
      if (other.type_ != 0) {
        setTypeValue(other.getTypeValue());
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
                  getIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getCreatedAtFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 24: {
              type_ = input.readEnum();
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

    private io.littlehorse.sdk.common.proto.WorkflowEventDefId id_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.WorkflowEventDefId, io.littlehorse.sdk.common.proto.WorkflowEventDefId.Builder, io.littlehorse.sdk.common.proto.WorkflowEventDefIdOrBuilder> idBuilder_;
    /**
     * <pre>
     * The ID of the WorkflowEventDef. Contains the name of the WorkflowEventDef.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     * @return Whether the id field is set.
     */
    public boolean hasId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * The ID of the WorkflowEventDef. Contains the name of the WorkflowEventDef.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     * @return The id.
     */
    public io.littlehorse.sdk.common.proto.WorkflowEventDefId getId() {
      if (idBuilder_ == null) {
        return id_ == null ? io.littlehorse.sdk.common.proto.WorkflowEventDefId.getDefaultInstance() : id_;
      } else {
        return idBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The ID of the WorkflowEventDef. Contains the name of the WorkflowEventDef.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     */
    public Builder setId(io.littlehorse.sdk.common.proto.WorkflowEventDefId value) {
      if (idBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        id_ = value;
      } else {
        idBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The ID of the WorkflowEventDef. Contains the name of the WorkflowEventDef.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     */
    public Builder setId(
        io.littlehorse.sdk.common.proto.WorkflowEventDefId.Builder builderForValue) {
      if (idBuilder_ == null) {
        id_ = builderForValue.build();
      } else {
        idBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The ID of the WorkflowEventDef. Contains the name of the WorkflowEventDef.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     */
    public Builder mergeId(io.littlehorse.sdk.common.proto.WorkflowEventDefId value) {
      if (idBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          id_ != null &&
          id_ != io.littlehorse.sdk.common.proto.WorkflowEventDefId.getDefaultInstance()) {
          getIdBuilder().mergeFrom(value);
        } else {
          id_ = value;
        }
      } else {
        idBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The ID of the WorkflowEventDef. Contains the name of the WorkflowEventDef.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     */
    public Builder clearId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      id_ = null;
      if (idBuilder_ != null) {
        idBuilder_.dispose();
        idBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The ID of the WorkflowEventDef. Contains the name of the WorkflowEventDef.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.WorkflowEventDefId.Builder getIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getIdFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The ID of the WorkflowEventDef. Contains the name of the WorkflowEventDef.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.WorkflowEventDefIdOrBuilder getIdOrBuilder() {
      if (idBuilder_ != null) {
        return idBuilder_.getMessageOrBuilder();
      } else {
        return id_ == null ?
            io.littlehorse.sdk.common.proto.WorkflowEventDefId.getDefaultInstance() : id_;
      }
    }
    /**
     * <pre>
     * The ID of the WorkflowEventDef. Contains the name of the WorkflowEventDef.
     * </pre>
     *
     * <code>.littlehorse.WorkflowEventDefId id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.WorkflowEventDefId, io.littlehorse.sdk.common.proto.WorkflowEventDefId.Builder, io.littlehorse.sdk.common.proto.WorkflowEventDefIdOrBuilder> 
        getIdFieldBuilder() {
      if (idBuilder_ == null) {
        idBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.WorkflowEventDefId, io.littlehorse.sdk.common.proto.WorkflowEventDefId.Builder, io.littlehorse.sdk.common.proto.WorkflowEventDefIdOrBuilder>(
                getId(),
                getParentForChildren(),
                isClean());
        id_ = null;
      }
      return idBuilder_;
    }

    private com.google.protobuf.Timestamp createdAt_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder> createdAtBuilder_;
    /**
     * <pre>
     * The time that the WorkflowEventDef was created at.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp created_at = 2;</code>
     * @return Whether the createdAt field is set.
     */
    public boolean hasCreatedAt() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * The time that the WorkflowEventDef was created at.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp created_at = 2;</code>
     * @return The createdAt.
     */
    public com.google.protobuf.Timestamp getCreatedAt() {
      if (createdAtBuilder_ == null) {
        return createdAt_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : createdAt_;
      } else {
        return createdAtBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * The time that the WorkflowEventDef was created at.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp created_at = 2;</code>
     */
    public Builder setCreatedAt(com.google.protobuf.Timestamp value) {
      if (createdAtBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        createdAt_ = value;
      } else {
        createdAtBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The time that the WorkflowEventDef was created at.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp created_at = 2;</code>
     */
    public Builder setCreatedAt(
        com.google.protobuf.Timestamp.Builder builderForValue) {
      if (createdAtBuilder_ == null) {
        createdAt_ = builderForValue.build();
      } else {
        createdAtBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The time that the WorkflowEventDef was created at.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp created_at = 2;</code>
     */
    public Builder mergeCreatedAt(com.google.protobuf.Timestamp value) {
      if (createdAtBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          createdAt_ != null &&
          createdAt_ != com.google.protobuf.Timestamp.getDefaultInstance()) {
          getCreatedAtBuilder().mergeFrom(value);
        } else {
          createdAt_ = value;
        }
      } else {
        createdAtBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The time that the WorkflowEventDef was created at.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp created_at = 2;</code>
     */
    public Builder clearCreatedAt() {
      bitField0_ = (bitField0_ & ~0x00000002);
      createdAt_ = null;
      if (createdAtBuilder_ != null) {
        createdAtBuilder_.dispose();
        createdAtBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The time that the WorkflowEventDef was created at.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp created_at = 2;</code>
     */
    public com.google.protobuf.Timestamp.Builder getCreatedAtBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getCreatedAtFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * The time that the WorkflowEventDef was created at.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp created_at = 2;</code>
     */
    public com.google.protobuf.TimestampOrBuilder getCreatedAtOrBuilder() {
      if (createdAtBuilder_ != null) {
        return createdAtBuilder_.getMessageOrBuilder();
      } else {
        return createdAt_ == null ?
            com.google.protobuf.Timestamp.getDefaultInstance() : createdAt_;
      }
    }
    /**
     * <pre>
     * The time that the WorkflowEventDef was created at.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp created_at = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder> 
        getCreatedAtFieldBuilder() {
      if (createdAtBuilder_ == null) {
        createdAtBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder>(
                getCreatedAt(),
                getParentForChildren(),
                isClean());
        createdAt_ = null;
      }
      return createdAtBuilder_;
    }

    private int type_ = 0;
    /**
     * <pre>
     * The type of the content of a WorkflowEvent based on this WorkflowEventDef.
     * </pre>
     *
     * <code>.littlehorse.VariableType type = 3;</code>
     * @return The enum numeric value on the wire for type.
     */
    @java.lang.Override public int getTypeValue() {
      return type_;
    }
    /**
     * <pre>
     * The type of the content of a WorkflowEvent based on this WorkflowEventDef.
     * </pre>
     *
     * <code>.littlehorse.VariableType type = 3;</code>
     * @param value The enum numeric value on the wire for type to set.
     * @return This builder for chaining.
     */
    public Builder setTypeValue(int value) {
      type_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The type of the content of a WorkflowEvent based on this WorkflowEventDef.
     * </pre>
     *
     * <code>.littlehorse.VariableType type = 3;</code>
     * @return The type.
     */
    @java.lang.Override
    public io.littlehorse.sdk.common.proto.VariableType getType() {
      io.littlehorse.sdk.common.proto.VariableType result = io.littlehorse.sdk.common.proto.VariableType.forNumber(type_);
      return result == null ? io.littlehorse.sdk.common.proto.VariableType.UNRECOGNIZED : result;
    }
    /**
     * <pre>
     * The type of the content of a WorkflowEvent based on this WorkflowEventDef.
     * </pre>
     *
     * <code>.littlehorse.VariableType type = 3;</code>
     * @param value The type to set.
     * @return This builder for chaining.
     */
    public Builder setType(io.littlehorse.sdk.common.proto.VariableType value) {
      if (value == null) {
        throw new NullPointerException();
      }
      bitField0_ |= 0x00000004;
      type_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The type of the content of a WorkflowEvent based on this WorkflowEventDef.
     * </pre>
     *
     * <code>.littlehorse.VariableType type = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearType() {
      bitField0_ = (bitField0_ & ~0x00000004);
      type_ = 0;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.WorkflowEventDef)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.WorkflowEventDef)
  private static final io.littlehorse.sdk.common.proto.WorkflowEventDef DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.WorkflowEventDef();
  }

  public static io.littlehorse.sdk.common.proto.WorkflowEventDef getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<WorkflowEventDef>
      PARSER = new com.google.protobuf.AbstractParser<WorkflowEventDef>() {
    @java.lang.Override
    public WorkflowEventDef parsePartialFrom(
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

  public static com.google.protobuf.Parser<WorkflowEventDef> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<WorkflowEventDef> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WorkflowEventDef getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

