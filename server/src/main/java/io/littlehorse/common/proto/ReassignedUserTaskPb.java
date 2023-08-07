// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: internal_server.proto

package io.littlehorse.common.proto;

/**
 * Protobuf type {@code littlehorse.ReassignedUserTaskPb}
 */
public final class ReassignedUserTaskPb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.ReassignedUserTaskPb)
    ReassignedUserTaskPbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ReassignedUserTaskPb.newBuilder() to construct.
  private ReassignedUserTaskPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ReassignedUserTaskPb() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ReassignedUserTaskPb();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_ReassignedUserTaskPb_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_ReassignedUserTaskPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.ReassignedUserTaskPb.class, io.littlehorse.common.proto.ReassignedUserTaskPb.Builder.class);
  }

  private int assignToCase_ = 0;
  @SuppressWarnings("serial")
  private java.lang.Object assignTo_;
  public enum AssignToCase
      implements com.google.protobuf.Internal.EnumLite,
          com.google.protobuf.AbstractMessage.InternalOneOfEnum {
    USER_ID(1),
    USER_GROUP(2),
    ASSIGNTO_NOT_SET(0);
    private final int value;
    private AssignToCase(int value) {
      this.value = value;
    }
    /**
     * @param value The number of the enum to look for.
     * @return The enum associated with the given number.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static AssignToCase valueOf(int value) {
      return forNumber(value);
    }

    public static AssignToCase forNumber(int value) {
      switch (value) {
        case 1: return USER_ID;
        case 2: return USER_GROUP;
        case 0: return ASSIGNTO_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  public AssignToCase
  getAssignToCase() {
    return AssignToCase.forNumber(
        assignToCase_);
  }

  public static final int USER_ID_FIELD_NUMBER = 1;
  /**
   * <code>string user_id = 1;</code>
   * @return Whether the userId field is set.
   */
  public boolean hasUserId() {
    return assignToCase_ == 1;
  }
  /**
   * <code>string user_id = 1;</code>
   * @return The userId.
   */
  public java.lang.String getUserId() {
    java.lang.Object ref = "";
    if (assignToCase_ == 1) {
      ref = assignTo_;
    }
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      if (assignToCase_ == 1) {
        assignTo_ = s;
      }
      return s;
    }
  }
  /**
   * <code>string user_id = 1;</code>
   * @return The bytes for userId.
   */
  public com.google.protobuf.ByteString
      getUserIdBytes() {
    java.lang.Object ref = "";
    if (assignToCase_ == 1) {
      ref = assignTo_;
    }
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      if (assignToCase_ == 1) {
        assignTo_ = b;
      }
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int USER_GROUP_FIELD_NUMBER = 2;
  /**
   * <code>string user_group = 2;</code>
   * @return Whether the userGroup field is set.
   */
  public boolean hasUserGroup() {
    return assignToCase_ == 2;
  }
  /**
   * <code>string user_group = 2;</code>
   * @return The userGroup.
   */
  public java.lang.String getUserGroup() {
    java.lang.Object ref = "";
    if (assignToCase_ == 2) {
      ref = assignTo_;
    }
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      if (assignToCase_ == 2) {
        assignTo_ = s;
      }
      return s;
    }
  }
  /**
   * <code>string user_group = 2;</code>
   * @return The bytes for userGroup.
   */
  public com.google.protobuf.ByteString
      getUserGroupBytes() {
    java.lang.Object ref = "";
    if (assignToCase_ == 2) {
      ref = assignTo_;
    }
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      if (assignToCase_ == 2) {
        assignTo_ = b;
      }
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int DELAY_IN_SECONDS_FIELD_NUMBER = 3;
  private int delayInSeconds_ = 0;
  /**
   * <code>int32 delay_in_seconds = 3;</code>
   * @return The delayInSeconds.
   */
  @java.lang.Override
  public int getDelayInSeconds() {
    return delayInSeconds_;
  }

  public static final int SOURCE_FIELD_NUMBER = 4;
  private io.littlehorse.sdk.common.proto.NodeRunIdPb source_;
  /**
   * <code>.littlehorse.NodeRunIdPb source = 4;</code>
   * @return Whether the source field is set.
   */
  @java.lang.Override
  public boolean hasSource() {
    return source_ != null;
  }
  /**
   * <code>.littlehorse.NodeRunIdPb source = 4;</code>
   * @return The source.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.NodeRunIdPb getSource() {
    return source_ == null ? io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance() : source_;
  }
  /**
   * <code>.littlehorse.NodeRunIdPb source = 4;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder getSourceOrBuilder() {
    return source_ == null ? io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance() : source_;
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
    if (assignToCase_ == 1) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, assignTo_);
    }
    if (assignToCase_ == 2) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, assignTo_);
    }
    if (delayInSeconds_ != 0) {
      output.writeInt32(3, delayInSeconds_);
    }
    if (source_ != null) {
      output.writeMessage(4, getSource());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (assignToCase_ == 1) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, assignTo_);
    }
    if (assignToCase_ == 2) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, assignTo_);
    }
    if (delayInSeconds_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(3, delayInSeconds_);
    }
    if (source_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(4, getSource());
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
    if (!(obj instanceof io.littlehorse.common.proto.ReassignedUserTaskPb)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.ReassignedUserTaskPb other = (io.littlehorse.common.proto.ReassignedUserTaskPb) obj;

    if (getDelayInSeconds()
        != other.getDelayInSeconds()) return false;
    if (hasSource() != other.hasSource()) return false;
    if (hasSource()) {
      if (!getSource()
          .equals(other.getSource())) return false;
    }
    if (!getAssignToCase().equals(other.getAssignToCase())) return false;
    switch (assignToCase_) {
      case 1:
        if (!getUserId()
            .equals(other.getUserId())) return false;
        break;
      case 2:
        if (!getUserGroup()
            .equals(other.getUserGroup())) return false;
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
    hash = (37 * hash) + DELAY_IN_SECONDS_FIELD_NUMBER;
    hash = (53 * hash) + getDelayInSeconds();
    if (hasSource()) {
      hash = (37 * hash) + SOURCE_FIELD_NUMBER;
      hash = (53 * hash) + getSource().hashCode();
    }
    switch (assignToCase_) {
      case 1:
        hash = (37 * hash) + USER_ID_FIELD_NUMBER;
        hash = (53 * hash) + getUserId().hashCode();
        break;
      case 2:
        hash = (37 * hash) + USER_GROUP_FIELD_NUMBER;
        hash = (53 * hash) + getUserGroup().hashCode();
        break;
      case 0:
      default:
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.ReassignedUserTaskPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.ReassignedUserTaskPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.ReassignedUserTaskPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.ReassignedUserTaskPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.ReassignedUserTaskPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.ReassignedUserTaskPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.ReassignedUserTaskPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.ReassignedUserTaskPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.ReassignedUserTaskPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.ReassignedUserTaskPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.ReassignedUserTaskPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.ReassignedUserTaskPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.ReassignedUserTaskPb prototype) {
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
   * Protobuf type {@code littlehorse.ReassignedUserTaskPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.ReassignedUserTaskPb)
      io.littlehorse.common.proto.ReassignedUserTaskPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_ReassignedUserTaskPb_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_ReassignedUserTaskPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.ReassignedUserTaskPb.class, io.littlehorse.common.proto.ReassignedUserTaskPb.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.ReassignedUserTaskPb.newBuilder()
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
      delayInSeconds_ = 0;
      source_ = null;
      if (sourceBuilder_ != null) {
        sourceBuilder_.dispose();
        sourceBuilder_ = null;
      }
      assignToCase_ = 0;
      assignTo_ = null;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_ReassignedUserTaskPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.ReassignedUserTaskPb getDefaultInstanceForType() {
      return io.littlehorse.common.proto.ReassignedUserTaskPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.ReassignedUserTaskPb build() {
      io.littlehorse.common.proto.ReassignedUserTaskPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.ReassignedUserTaskPb buildPartial() {
      io.littlehorse.common.proto.ReassignedUserTaskPb result = new io.littlehorse.common.proto.ReassignedUserTaskPb(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      buildPartialOneofs(result);
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.common.proto.ReassignedUserTaskPb result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.delayInSeconds_ = delayInSeconds_;
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        result.source_ = sourceBuilder_ == null
            ? source_
            : sourceBuilder_.build();
      }
    }

    private void buildPartialOneofs(io.littlehorse.common.proto.ReassignedUserTaskPb result) {
      result.assignToCase_ = assignToCase_;
      result.assignTo_ = this.assignTo_;
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
      if (other instanceof io.littlehorse.common.proto.ReassignedUserTaskPb) {
        return mergeFrom((io.littlehorse.common.proto.ReassignedUserTaskPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.ReassignedUserTaskPb other) {
      if (other == io.littlehorse.common.proto.ReassignedUserTaskPb.getDefaultInstance()) return this;
      if (other.getDelayInSeconds() != 0) {
        setDelayInSeconds(other.getDelayInSeconds());
      }
      if (other.hasSource()) {
        mergeSource(other.getSource());
      }
      switch (other.getAssignToCase()) {
        case USER_ID: {
          assignToCase_ = 1;
          assignTo_ = other.assignTo_;
          onChanged();
          break;
        }
        case USER_GROUP: {
          assignToCase_ = 2;
          assignTo_ = other.assignTo_;
          onChanged();
          break;
        }
        case ASSIGNTO_NOT_SET: {
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
            case 10: {
              java.lang.String s = input.readStringRequireUtf8();
              assignToCase_ = 1;
              assignTo_ = s;
              break;
            } // case 10
            case 18: {
              java.lang.String s = input.readStringRequireUtf8();
              assignToCase_ = 2;
              assignTo_ = s;
              break;
            } // case 18
            case 24: {
              delayInSeconds_ = input.readInt32();
              bitField0_ |= 0x00000004;
              break;
            } // case 24
            case 34: {
              input.readMessage(
                  getSourceFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000008;
              break;
            } // case 34
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
    private int assignToCase_ = 0;
    private java.lang.Object assignTo_;
    public AssignToCase
        getAssignToCase() {
      return AssignToCase.forNumber(
          assignToCase_);
    }

    public Builder clearAssignTo() {
      assignToCase_ = 0;
      assignTo_ = null;
      onChanged();
      return this;
    }

    private int bitField0_;

    /**
     * <code>string user_id = 1;</code>
     * @return Whether the userId field is set.
     */
    @java.lang.Override
    public boolean hasUserId() {
      return assignToCase_ == 1;
    }
    /**
     * <code>string user_id = 1;</code>
     * @return The userId.
     */
    @java.lang.Override
    public java.lang.String getUserId() {
      java.lang.Object ref = "";
      if (assignToCase_ == 1) {
        ref = assignTo_;
      }
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (assignToCase_ == 1) {
          assignTo_ = s;
        }
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string user_id = 1;</code>
     * @return The bytes for userId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getUserIdBytes() {
      java.lang.Object ref = "";
      if (assignToCase_ == 1) {
        ref = assignTo_;
      }
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        if (assignToCase_ == 1) {
          assignTo_ = b;
        }
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string user_id = 1;</code>
     * @param value The userId to set.
     * @return This builder for chaining.
     */
    public Builder setUserId(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      assignToCase_ = 1;
      assignTo_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string user_id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearUserId() {
      if (assignToCase_ == 1) {
        assignToCase_ = 0;
        assignTo_ = null;
        onChanged();
      }
      return this;
    }
    /**
     * <code>string user_id = 1;</code>
     * @param value The bytes for userId to set.
     * @return This builder for chaining.
     */
    public Builder setUserIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      assignToCase_ = 1;
      assignTo_ = value;
      onChanged();
      return this;
    }

    /**
     * <code>string user_group = 2;</code>
     * @return Whether the userGroup field is set.
     */
    @java.lang.Override
    public boolean hasUserGroup() {
      return assignToCase_ == 2;
    }
    /**
     * <code>string user_group = 2;</code>
     * @return The userGroup.
     */
    @java.lang.Override
    public java.lang.String getUserGroup() {
      java.lang.Object ref = "";
      if (assignToCase_ == 2) {
        ref = assignTo_;
      }
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (assignToCase_ == 2) {
          assignTo_ = s;
        }
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string user_group = 2;</code>
     * @return The bytes for userGroup.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getUserGroupBytes() {
      java.lang.Object ref = "";
      if (assignToCase_ == 2) {
        ref = assignTo_;
      }
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        if (assignToCase_ == 2) {
          assignTo_ = b;
        }
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string user_group = 2;</code>
     * @param value The userGroup to set.
     * @return This builder for chaining.
     */
    public Builder setUserGroup(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      assignToCase_ = 2;
      assignTo_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string user_group = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearUserGroup() {
      if (assignToCase_ == 2) {
        assignToCase_ = 0;
        assignTo_ = null;
        onChanged();
      }
      return this;
    }
    /**
     * <code>string user_group = 2;</code>
     * @param value The bytes for userGroup to set.
     * @return This builder for chaining.
     */
    public Builder setUserGroupBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      assignToCase_ = 2;
      assignTo_ = value;
      onChanged();
      return this;
    }

    private int delayInSeconds_ ;
    /**
     * <code>int32 delay_in_seconds = 3;</code>
     * @return The delayInSeconds.
     */
    @java.lang.Override
    public int getDelayInSeconds() {
      return delayInSeconds_;
    }
    /**
     * <code>int32 delay_in_seconds = 3;</code>
     * @param value The delayInSeconds to set.
     * @return This builder for chaining.
     */
    public Builder setDelayInSeconds(int value) {

      delayInSeconds_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>int32 delay_in_seconds = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearDelayInSeconds() {
      bitField0_ = (bitField0_ & ~0x00000004);
      delayInSeconds_ = 0;
      onChanged();
      return this;
    }

    private io.littlehorse.sdk.common.proto.NodeRunIdPb source_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.NodeRunIdPb, io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder, io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder> sourceBuilder_;
    /**
     * <code>.littlehorse.NodeRunIdPb source = 4;</code>
     * @return Whether the source field is set.
     */
    public boolean hasSource() {
      return ((bitField0_ & 0x00000008) != 0);
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 4;</code>
     * @return The source.
     */
    public io.littlehorse.sdk.common.proto.NodeRunIdPb getSource() {
      if (sourceBuilder_ == null) {
        return source_ == null ? io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance() : source_;
      } else {
        return sourceBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 4;</code>
     */
    public Builder setSource(io.littlehorse.sdk.common.proto.NodeRunIdPb value) {
      if (sourceBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        source_ = value;
      } else {
        sourceBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 4;</code>
     */
    public Builder setSource(
        io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder builderForValue) {
      if (sourceBuilder_ == null) {
        source_ = builderForValue.build();
      } else {
        sourceBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 4;</code>
     */
    public Builder mergeSource(io.littlehorse.sdk.common.proto.NodeRunIdPb value) {
      if (sourceBuilder_ == null) {
        if (((bitField0_ & 0x00000008) != 0) &&
          source_ != null &&
          source_ != io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance()) {
          getSourceBuilder().mergeFrom(value);
        } else {
          source_ = value;
        }
      } else {
        sourceBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 4;</code>
     */
    public Builder clearSource() {
      bitField0_ = (bitField0_ & ~0x00000008);
      source_ = null;
      if (sourceBuilder_ != null) {
        sourceBuilder_.dispose();
        sourceBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 4;</code>
     */
    public io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder getSourceBuilder() {
      bitField0_ |= 0x00000008;
      onChanged();
      return getSourceFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 4;</code>
     */
    public io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder getSourceOrBuilder() {
      if (sourceBuilder_ != null) {
        return sourceBuilder_.getMessageOrBuilder();
      } else {
        return source_ == null ?
            io.littlehorse.sdk.common.proto.NodeRunIdPb.getDefaultInstance() : source_;
      }
    }
    /**
     * <code>.littlehorse.NodeRunIdPb source = 4;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.NodeRunIdPb, io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder, io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder> 
        getSourceFieldBuilder() {
      if (sourceBuilder_ == null) {
        sourceBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.NodeRunIdPb, io.littlehorse.sdk.common.proto.NodeRunIdPb.Builder, io.littlehorse.sdk.common.proto.NodeRunIdPbOrBuilder>(
                getSource(),
                getParentForChildren(),
                isClean());
        source_ = null;
      }
      return sourceBuilder_;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.ReassignedUserTaskPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.ReassignedUserTaskPb)
  private static final io.littlehorse.common.proto.ReassignedUserTaskPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.ReassignedUserTaskPb();
  }

  public static io.littlehorse.common.proto.ReassignedUserTaskPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ReassignedUserTaskPb>
      PARSER = new com.google.protobuf.AbstractParser<ReassignedUserTaskPb>() {
    @java.lang.Override
    public ReassignedUserTaskPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<ReassignedUserTaskPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ReassignedUserTaskPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.ReassignedUserTaskPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

