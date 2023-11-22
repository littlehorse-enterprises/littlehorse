// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.RegisterTaskWorkerRequest}
 */
public final class RegisterTaskWorkerRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.RegisterTaskWorkerRequest)
    RegisterTaskWorkerRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use RegisterTaskWorkerRequest.newBuilder() to construct.
  private RegisterTaskWorkerRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private RegisterTaskWorkerRequest() {
    clientId_ = "";
    listenerName_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new RegisterTaskWorkerRequest();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_RegisterTaskWorkerRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_RegisterTaskWorkerRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest.class, io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest.Builder.class);
  }

  public static final int CLIENT_ID_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object clientId_ = "";
  /**
   * <code>string client_id = 1;</code>
   * @return The clientId.
   */
  @java.lang.Override
  public java.lang.String getClientId() {
    java.lang.Object ref = clientId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      clientId_ = s;
      return s;
    }
  }
  /**
   * <code>string client_id = 1;</code>
   * @return The bytes for clientId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getClientIdBytes() {
    java.lang.Object ref = clientId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      clientId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int TASK_DEF_ID_FIELD_NUMBER = 2;
  private io.littlehorse.sdk.common.proto.TaskDefId taskDefId_;
  /**
   * <code>.littlehorse.TaskDefId task_def_id = 2;</code>
   * @return Whether the taskDefId field is set.
   */
  @java.lang.Override
  public boolean hasTaskDefId() {
    return taskDefId_ != null;
  }
  /**
   * <code>.littlehorse.TaskDefId task_def_id = 2;</code>
   * @return The taskDefId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TaskDefId getTaskDefId() {
    return taskDefId_ == null ? io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance() : taskDefId_;
  }
  /**
   * <code>.littlehorse.TaskDefId task_def_id = 2;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder getTaskDefIdOrBuilder() {
    return taskDefId_ == null ? io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance() : taskDefId_;
  }

  public static final int LISTENER_NAME_FIELD_NUMBER = 3;
  @SuppressWarnings("serial")
  private volatile java.lang.Object listenerName_ = "";
  /**
   * <code>string listener_name = 3;</code>
   * @return The listenerName.
   */
  @java.lang.Override
  public java.lang.String getListenerName() {
    java.lang.Object ref = listenerName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      listenerName_ = s;
      return s;
    }
  }
  /**
   * <code>string listener_name = 3;</code>
   * @return The bytes for listenerName.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getListenerNameBytes() {
    java.lang.Object ref = listenerName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      listenerName_ = b;
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(clientId_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, clientId_);
    }
    if (taskDefId_ != null) {
      output.writeMessage(2, getTaskDefId());
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(listenerName_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, listenerName_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(clientId_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, clientId_);
    }
    if (taskDefId_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getTaskDefId());
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(listenerName_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, listenerName_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest other = (io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest) obj;

    if (!getClientId()
        .equals(other.getClientId())) return false;
    if (hasTaskDefId() != other.hasTaskDefId()) return false;
    if (hasTaskDefId()) {
      if (!getTaskDefId()
          .equals(other.getTaskDefId())) return false;
    }
    if (!getListenerName()
        .equals(other.getListenerName())) return false;
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
    hash = (37 * hash) + CLIENT_ID_FIELD_NUMBER;
    hash = (53 * hash) + getClientId().hashCode();
    if (hasTaskDefId()) {
      hash = (37 * hash) + TASK_DEF_ID_FIELD_NUMBER;
      hash = (53 * hash) + getTaskDefId().hashCode();
    }
    hash = (37 * hash) + LISTENER_NAME_FIELD_NUMBER;
    hash = (53 * hash) + getListenerName().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest prototype) {
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
   * Protobuf type {@code littlehorse.RegisterTaskWorkerRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.RegisterTaskWorkerRequest)
      io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_RegisterTaskWorkerRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_RegisterTaskWorkerRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest.class, io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest.newBuilder()
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
      clientId_ = "";
      taskDefId_ = null;
      if (taskDefIdBuilder_ != null) {
        taskDefIdBuilder_.dispose();
        taskDefIdBuilder_ = null;
      }
      listenerName_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_RegisterTaskWorkerRequest_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest build() {
      io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest buildPartial() {
      io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest result = new io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.clientId_ = clientId_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.taskDefId_ = taskDefIdBuilder_ == null
            ? taskDefId_
            : taskDefIdBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.listenerName_ = listenerName_;
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
      if (other instanceof io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest) {
        return mergeFrom((io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest other) {
      if (other == io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest.getDefaultInstance()) return this;
      if (!other.getClientId().isEmpty()) {
        clientId_ = other.clientId_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (other.hasTaskDefId()) {
        mergeTaskDefId(other.getTaskDefId());
      }
      if (!other.getListenerName().isEmpty()) {
        listenerName_ = other.listenerName_;
        bitField0_ |= 0x00000004;
        onChanged();
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
              clientId_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getTaskDefIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 26: {
              listenerName_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000004;
              break;
            } // case 26
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

    private java.lang.Object clientId_ = "";
    /**
     * <code>string client_id = 1;</code>
     * @return The clientId.
     */
    public java.lang.String getClientId() {
      java.lang.Object ref = clientId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        clientId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string client_id = 1;</code>
     * @return The bytes for clientId.
     */
    public com.google.protobuf.ByteString
        getClientIdBytes() {
      java.lang.Object ref = clientId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        clientId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string client_id = 1;</code>
     * @param value The clientId to set.
     * @return This builder for chaining.
     */
    public Builder setClientId(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      clientId_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>string client_id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearClientId() {
      clientId_ = getDefaultInstance().getClientId();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>string client_id = 1;</code>
     * @param value The bytes for clientId to set.
     * @return This builder for chaining.
     */
    public Builder setClientIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      clientId_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private io.littlehorse.sdk.common.proto.TaskDefId taskDefId_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.TaskDefId, io.littlehorse.sdk.common.proto.TaskDefId.Builder, io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder> taskDefIdBuilder_;
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 2;</code>
     * @return Whether the taskDefId field is set.
     */
    public boolean hasTaskDefId() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 2;</code>
     * @return The taskDefId.
     */
    public io.littlehorse.sdk.common.proto.TaskDefId getTaskDefId() {
      if (taskDefIdBuilder_ == null) {
        return taskDefId_ == null ? io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance() : taskDefId_;
      } else {
        return taskDefIdBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 2;</code>
     */
    public Builder setTaskDefId(io.littlehorse.sdk.common.proto.TaskDefId value) {
      if (taskDefIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        taskDefId_ = value;
      } else {
        taskDefIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 2;</code>
     */
    public Builder setTaskDefId(
        io.littlehorse.sdk.common.proto.TaskDefId.Builder builderForValue) {
      if (taskDefIdBuilder_ == null) {
        taskDefId_ = builderForValue.build();
      } else {
        taskDefIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 2;</code>
     */
    public Builder mergeTaskDefId(io.littlehorse.sdk.common.proto.TaskDefId value) {
      if (taskDefIdBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          taskDefId_ != null &&
          taskDefId_ != io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance()) {
          getTaskDefIdBuilder().mergeFrom(value);
        } else {
          taskDefId_ = value;
        }
      } else {
        taskDefIdBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 2;</code>
     */
    public Builder clearTaskDefId() {
      bitField0_ = (bitField0_ & ~0x00000002);
      taskDefId_ = null;
      if (taskDefIdBuilder_ != null) {
        taskDefIdBuilder_.dispose();
        taskDefIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 2;</code>
     */
    public io.littlehorse.sdk.common.proto.TaskDefId.Builder getTaskDefIdBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getTaskDefIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 2;</code>
     */
    public io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder getTaskDefIdOrBuilder() {
      if (taskDefIdBuilder_ != null) {
        return taskDefIdBuilder_.getMessageOrBuilder();
      } else {
        return taskDefId_ == null ?
            io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance() : taskDefId_;
      }
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.TaskDefId, io.littlehorse.sdk.common.proto.TaskDefId.Builder, io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder> 
        getTaskDefIdFieldBuilder() {
      if (taskDefIdBuilder_ == null) {
        taskDefIdBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.TaskDefId, io.littlehorse.sdk.common.proto.TaskDefId.Builder, io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder>(
                getTaskDefId(),
                getParentForChildren(),
                isClean());
        taskDefId_ = null;
      }
      return taskDefIdBuilder_;
    }

    private java.lang.Object listenerName_ = "";
    /**
     * <code>string listener_name = 3;</code>
     * @return The listenerName.
     */
    public java.lang.String getListenerName() {
      java.lang.Object ref = listenerName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        listenerName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string listener_name = 3;</code>
     * @return The bytes for listenerName.
     */
    public com.google.protobuf.ByteString
        getListenerNameBytes() {
      java.lang.Object ref = listenerName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        listenerName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string listener_name = 3;</code>
     * @param value The listenerName to set.
     * @return This builder for chaining.
     */
    public Builder setListenerName(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      listenerName_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>string listener_name = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearListenerName() {
      listenerName_ = getDefaultInstance().getListenerName();
      bitField0_ = (bitField0_ & ~0x00000004);
      onChanged();
      return this;
    }
    /**
     * <code>string listener_name = 3;</code>
     * @param value The bytes for listenerName to set.
     * @return This builder for chaining.
     */
    public Builder setListenerNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      listenerName_ = value;
      bitField0_ |= 0x00000004;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.RegisterTaskWorkerRequest)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.RegisterTaskWorkerRequest)
  private static final io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest();
  }

  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<RegisterTaskWorkerRequest>
      PARSER = new com.google.protobuf.AbstractParser<RegisterTaskWorkerRequest>() {
    @java.lang.Override
    public RegisterTaskWorkerRequest parsePartialFrom(
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

  public static com.google.protobuf.Parser<RegisterTaskWorkerRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<RegisterTaskWorkerRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

