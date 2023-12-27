// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Response containing list of Servers that the Task Worker should connect to and start polling from.
 * Only used internally by the Task Worker SDK.
 * </pre>
 *
 * Protobuf type {@code littlehorse.RegisterTaskWorkerResponse}
 */
public final class RegisterTaskWorkerResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.RegisterTaskWorkerResponse)
    RegisterTaskWorkerResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use RegisterTaskWorkerResponse.newBuilder() to construct.
  private RegisterTaskWorkerResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private RegisterTaskWorkerResponse() {
    yourHosts_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new RegisterTaskWorkerResponse();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_RegisterTaskWorkerResponse_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_RegisterTaskWorkerResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse.class, io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse.Builder.class);
  }

  private int bitField0_;
  public static final int YOUR_HOSTS_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<io.littlehorse.sdk.common.proto.LHHostInfo> yourHosts_;
  /**
   * <pre>
   * The list of LH Server hosts that the Task Worker should start polling.
   * </pre>
   *
   * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
   */
  @java.lang.Override
  public java.util.List<io.littlehorse.sdk.common.proto.LHHostInfo> getYourHostsList() {
    return yourHosts_;
  }
  /**
   * <pre>
   * The list of LH Server hosts that the Task Worker should start polling.
   * </pre>
   *
   * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
   */
  @java.lang.Override
  public java.util.List<? extends io.littlehorse.sdk.common.proto.LHHostInfoOrBuilder> 
      getYourHostsOrBuilderList() {
    return yourHosts_;
  }
  /**
   * <pre>
   * The list of LH Server hosts that the Task Worker should start polling.
   * </pre>
   *
   * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
   */
  @java.lang.Override
  public int getYourHostsCount() {
    return yourHosts_.size();
  }
  /**
   * <pre>
   * The list of LH Server hosts that the Task Worker should start polling.
   * </pre>
   *
   * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.LHHostInfo getYourHosts(int index) {
    return yourHosts_.get(index);
  }
  /**
   * <pre>
   * The list of LH Server hosts that the Task Worker should start polling.
   * </pre>
   *
   * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.LHHostInfoOrBuilder getYourHostsOrBuilder(
      int index) {
    return yourHosts_.get(index);
  }

  public static final int IS_CLUSTER_HEALTHY_FIELD_NUMBER = 2;
  private boolean isClusterHealthy_ = false;
  /**
   * <pre>
   * Whether the LH Cluster is healthy.
   * </pre>
   *
   * <code>optional bool is_cluster_healthy = 2;</code>
   * @return Whether the isClusterHealthy field is set.
   */
  @java.lang.Override
  public boolean hasIsClusterHealthy() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * Whether the LH Cluster is healthy.
   * </pre>
   *
   * <code>optional bool is_cluster_healthy = 2;</code>
   * @return The isClusterHealthy.
   */
  @java.lang.Override
  public boolean getIsClusterHealthy() {
    return isClusterHealthy_;
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
    for (int i = 0; i < yourHosts_.size(); i++) {
      output.writeMessage(1, yourHosts_.get(i));
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      output.writeBool(2, isClusterHealthy_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < yourHosts_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, yourHosts_.get(i));
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(2, isClusterHealthy_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse other = (io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse) obj;

    if (!getYourHostsList()
        .equals(other.getYourHostsList())) return false;
    if (hasIsClusterHealthy() != other.hasIsClusterHealthy()) return false;
    if (hasIsClusterHealthy()) {
      if (getIsClusterHealthy()
          != other.getIsClusterHealthy()) return false;
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
    if (getYourHostsCount() > 0) {
      hash = (37 * hash) + YOUR_HOSTS_FIELD_NUMBER;
      hash = (53 * hash) + getYourHostsList().hashCode();
    }
    if (hasIsClusterHealthy()) {
      hash = (37 * hash) + IS_CLUSTER_HEALTHY_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
          getIsClusterHealthy());
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse prototype) {
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
   * Response containing list of Servers that the Task Worker should connect to and start polling from.
   * Only used internally by the Task Worker SDK.
   * </pre>
   *
   * Protobuf type {@code littlehorse.RegisterTaskWorkerResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.RegisterTaskWorkerResponse)
      io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_RegisterTaskWorkerResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_RegisterTaskWorkerResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse.class, io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse.newBuilder()
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
      if (yourHostsBuilder_ == null) {
        yourHosts_ = java.util.Collections.emptyList();
      } else {
        yourHosts_ = null;
        yourHostsBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      isClusterHealthy_ = false;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_RegisterTaskWorkerResponse_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse build() {
      io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse buildPartial() {
      io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse result = new io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse result) {
      if (yourHostsBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          yourHosts_ = java.util.Collections.unmodifiableList(yourHosts_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.yourHosts_ = yourHosts_;
      } else {
        result.yourHosts_ = yourHostsBuilder_.build();
      }
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.isClusterHealthy_ = isClusterHealthy_;
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
      if (other instanceof io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse) {
        return mergeFrom((io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse other) {
      if (other == io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse.getDefaultInstance()) return this;
      if (yourHostsBuilder_ == null) {
        if (!other.yourHosts_.isEmpty()) {
          if (yourHosts_.isEmpty()) {
            yourHosts_ = other.yourHosts_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureYourHostsIsMutable();
            yourHosts_.addAll(other.yourHosts_);
          }
          onChanged();
        }
      } else {
        if (!other.yourHosts_.isEmpty()) {
          if (yourHostsBuilder_.isEmpty()) {
            yourHostsBuilder_.dispose();
            yourHostsBuilder_ = null;
            yourHosts_ = other.yourHosts_;
            bitField0_ = (bitField0_ & ~0x00000001);
            yourHostsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getYourHostsFieldBuilder() : null;
          } else {
            yourHostsBuilder_.addAllMessages(other.yourHosts_);
          }
        }
      }
      if (other.hasIsClusterHealthy()) {
        setIsClusterHealthy(other.getIsClusterHealthy());
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
              io.littlehorse.sdk.common.proto.LHHostInfo m =
                  input.readMessage(
                      io.littlehorse.sdk.common.proto.LHHostInfo.parser(),
                      extensionRegistry);
              if (yourHostsBuilder_ == null) {
                ensureYourHostsIsMutable();
                yourHosts_.add(m);
              } else {
                yourHostsBuilder_.addMessage(m);
              }
              break;
            } // case 10
            case 16: {
              isClusterHealthy_ = input.readBool();
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

    private java.util.List<io.littlehorse.sdk.common.proto.LHHostInfo> yourHosts_ =
      java.util.Collections.emptyList();
    private void ensureYourHostsIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        yourHosts_ = new java.util.ArrayList<io.littlehorse.sdk.common.proto.LHHostInfo>(yourHosts_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.littlehorse.sdk.common.proto.LHHostInfo, io.littlehorse.sdk.common.proto.LHHostInfo.Builder, io.littlehorse.sdk.common.proto.LHHostInfoOrBuilder> yourHostsBuilder_;

    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public java.util.List<io.littlehorse.sdk.common.proto.LHHostInfo> getYourHostsList() {
      if (yourHostsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(yourHosts_);
      } else {
        return yourHostsBuilder_.getMessageList();
      }
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public int getYourHostsCount() {
      if (yourHostsBuilder_ == null) {
        return yourHosts_.size();
      } else {
        return yourHostsBuilder_.getCount();
      }
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.LHHostInfo getYourHosts(int index) {
      if (yourHostsBuilder_ == null) {
        return yourHosts_.get(index);
      } else {
        return yourHostsBuilder_.getMessage(index);
      }
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public Builder setYourHosts(
        int index, io.littlehorse.sdk.common.proto.LHHostInfo value) {
      if (yourHostsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureYourHostsIsMutable();
        yourHosts_.set(index, value);
        onChanged();
      } else {
        yourHostsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public Builder setYourHosts(
        int index, io.littlehorse.sdk.common.proto.LHHostInfo.Builder builderForValue) {
      if (yourHostsBuilder_ == null) {
        ensureYourHostsIsMutable();
        yourHosts_.set(index, builderForValue.build());
        onChanged();
      } else {
        yourHostsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public Builder addYourHosts(io.littlehorse.sdk.common.proto.LHHostInfo value) {
      if (yourHostsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureYourHostsIsMutable();
        yourHosts_.add(value);
        onChanged();
      } else {
        yourHostsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public Builder addYourHosts(
        int index, io.littlehorse.sdk.common.proto.LHHostInfo value) {
      if (yourHostsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureYourHostsIsMutable();
        yourHosts_.add(index, value);
        onChanged();
      } else {
        yourHostsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public Builder addYourHosts(
        io.littlehorse.sdk.common.proto.LHHostInfo.Builder builderForValue) {
      if (yourHostsBuilder_ == null) {
        ensureYourHostsIsMutable();
        yourHosts_.add(builderForValue.build());
        onChanged();
      } else {
        yourHostsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public Builder addYourHosts(
        int index, io.littlehorse.sdk.common.proto.LHHostInfo.Builder builderForValue) {
      if (yourHostsBuilder_ == null) {
        ensureYourHostsIsMutable();
        yourHosts_.add(index, builderForValue.build());
        onChanged();
      } else {
        yourHostsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public Builder addAllYourHosts(
        java.lang.Iterable<? extends io.littlehorse.sdk.common.proto.LHHostInfo> values) {
      if (yourHostsBuilder_ == null) {
        ensureYourHostsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, yourHosts_);
        onChanged();
      } else {
        yourHostsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public Builder clearYourHosts() {
      if (yourHostsBuilder_ == null) {
        yourHosts_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        yourHostsBuilder_.clear();
      }
      return this;
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public Builder removeYourHosts(int index) {
      if (yourHostsBuilder_ == null) {
        ensureYourHostsIsMutable();
        yourHosts_.remove(index);
        onChanged();
      } else {
        yourHostsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.LHHostInfo.Builder getYourHostsBuilder(
        int index) {
      return getYourHostsFieldBuilder().getBuilder(index);
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.LHHostInfoOrBuilder getYourHostsOrBuilder(
        int index) {
      if (yourHostsBuilder_ == null) {
        return yourHosts_.get(index);  } else {
        return yourHostsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public java.util.List<? extends io.littlehorse.sdk.common.proto.LHHostInfoOrBuilder> 
         getYourHostsOrBuilderList() {
      if (yourHostsBuilder_ != null) {
        return yourHostsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(yourHosts_);
      }
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.LHHostInfo.Builder addYourHostsBuilder() {
      return getYourHostsFieldBuilder().addBuilder(
          io.littlehorse.sdk.common.proto.LHHostInfo.getDefaultInstance());
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.LHHostInfo.Builder addYourHostsBuilder(
        int index) {
      return getYourHostsFieldBuilder().addBuilder(
          index, io.littlehorse.sdk.common.proto.LHHostInfo.getDefaultInstance());
    }
    /**
     * <pre>
     * The list of LH Server hosts that the Task Worker should start polling.
     * </pre>
     *
     * <code>repeated .littlehorse.LHHostInfo your_hosts = 1;</code>
     */
    public java.util.List<io.littlehorse.sdk.common.proto.LHHostInfo.Builder> 
         getYourHostsBuilderList() {
      return getYourHostsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.littlehorse.sdk.common.proto.LHHostInfo, io.littlehorse.sdk.common.proto.LHHostInfo.Builder, io.littlehorse.sdk.common.proto.LHHostInfoOrBuilder> 
        getYourHostsFieldBuilder() {
      if (yourHostsBuilder_ == null) {
        yourHostsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            io.littlehorse.sdk.common.proto.LHHostInfo, io.littlehorse.sdk.common.proto.LHHostInfo.Builder, io.littlehorse.sdk.common.proto.LHHostInfoOrBuilder>(
                yourHosts_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        yourHosts_ = null;
      }
      return yourHostsBuilder_;
    }

    private boolean isClusterHealthy_ ;
    /**
     * <pre>
     * Whether the LH Cluster is healthy.
     * </pre>
     *
     * <code>optional bool is_cluster_healthy = 2;</code>
     * @return Whether the isClusterHealthy field is set.
     */
    @java.lang.Override
    public boolean hasIsClusterHealthy() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * Whether the LH Cluster is healthy.
     * </pre>
     *
     * <code>optional bool is_cluster_healthy = 2;</code>
     * @return The isClusterHealthy.
     */
    @java.lang.Override
    public boolean getIsClusterHealthy() {
      return isClusterHealthy_;
    }
    /**
     * <pre>
     * Whether the LH Cluster is healthy.
     * </pre>
     *
     * <code>optional bool is_cluster_healthy = 2;</code>
     * @param value The isClusterHealthy to set.
     * @return This builder for chaining.
     */
    public Builder setIsClusterHealthy(boolean value) {

      isClusterHealthy_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Whether the LH Cluster is healthy.
     * </pre>
     *
     * <code>optional bool is_cluster_healthy = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearIsClusterHealthy() {
      bitField0_ = (bitField0_ & ~0x00000002);
      isClusterHealthy_ = false;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.RegisterTaskWorkerResponse)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.RegisterTaskWorkerResponse)
  private static final io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse();
  }

  public static io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<RegisterTaskWorkerResponse>
      PARSER = new com.google.protobuf.AbstractParser<RegisterTaskWorkerResponse>() {
    @java.lang.Override
    public RegisterTaskWorkerResponse parsePartialFrom(
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

  public static com.google.protobuf.Parser<RegisterTaskWorkerResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<RegisterTaskWorkerResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

