// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cluster_health.proto

package io.littlehorse.common.proto;

/**
 * Protobuf type {@code littlehorse.LocalTasksResponse}
 */
public final class LocalTasksResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.LocalTasksResponse)
    LocalTasksResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use LocalTasksResponse.newBuilder() to construct.
  private LocalTasksResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private LocalTasksResponse() {
    activeTasks_ = java.util.Collections.emptyList();
    standbyTasks_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new LocalTasksResponse();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.ClusterHealth.internal_static_littlehorse_LocalTasksResponse_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.ClusterHealth.internal_static_littlehorse_LocalTasksResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.LocalTasksResponse.class, io.littlehorse.common.proto.LocalTasksResponse.Builder.class);
  }

  public static final int ACTIVE_TASKS_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<io.littlehorse.common.proto.TaskStatePb> activeTasks_;
  /**
   * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
   */
  @java.lang.Override
  public java.util.List<io.littlehorse.common.proto.TaskStatePb> getActiveTasksList() {
    return activeTasks_;
  }
  /**
   * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
   */
  @java.lang.Override
  public java.util.List<? extends io.littlehorse.common.proto.TaskStatePbOrBuilder> 
      getActiveTasksOrBuilderList() {
    return activeTasks_;
  }
  /**
   * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
   */
  @java.lang.Override
  public int getActiveTasksCount() {
    return activeTasks_.size();
  }
  /**
   * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.TaskStatePb getActiveTasks(int index) {
    return activeTasks_.get(index);
  }
  /**
   * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.TaskStatePbOrBuilder getActiveTasksOrBuilder(
      int index) {
    return activeTasks_.get(index);
  }

  public static final int STANDBY_TASKS_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private java.util.List<io.littlehorse.common.proto.StandByTaskStatePb> standbyTasks_;
  /**
   * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
   */
  @java.lang.Override
  public java.util.List<io.littlehorse.common.proto.StandByTaskStatePb> getStandbyTasksList() {
    return standbyTasks_;
  }
  /**
   * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
   */
  @java.lang.Override
  public java.util.List<? extends io.littlehorse.common.proto.StandByTaskStatePbOrBuilder> 
      getStandbyTasksOrBuilderList() {
    return standbyTasks_;
  }
  /**
   * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
   */
  @java.lang.Override
  public int getStandbyTasksCount() {
    return standbyTasks_.size();
  }
  /**
   * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.StandByTaskStatePb getStandbyTasks(int index) {
    return standbyTasks_.get(index);
  }
  /**
   * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.StandByTaskStatePbOrBuilder getStandbyTasksOrBuilder(
      int index) {
    return standbyTasks_.get(index);
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
    for (int i = 0; i < activeTasks_.size(); i++) {
      output.writeMessage(1, activeTasks_.get(i));
    }
    for (int i = 0; i < standbyTasks_.size(); i++) {
      output.writeMessage(2, standbyTasks_.get(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < activeTasks_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, activeTasks_.get(i));
    }
    for (int i = 0; i < standbyTasks_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, standbyTasks_.get(i));
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
    if (!(obj instanceof io.littlehorse.common.proto.LocalTasksResponse)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.LocalTasksResponse other = (io.littlehorse.common.proto.LocalTasksResponse) obj;

    if (!getActiveTasksList()
        .equals(other.getActiveTasksList())) return false;
    if (!getStandbyTasksList()
        .equals(other.getStandbyTasksList())) return false;
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
    if (getActiveTasksCount() > 0) {
      hash = (37 * hash) + ACTIVE_TASKS_FIELD_NUMBER;
      hash = (53 * hash) + getActiveTasksList().hashCode();
    }
    if (getStandbyTasksCount() > 0) {
      hash = (37 * hash) + STANDBY_TASKS_FIELD_NUMBER;
      hash = (53 * hash) + getStandbyTasksList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.LocalTasksResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.LocalTasksResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.LocalTasksResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.LocalTasksResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.LocalTasksResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.LocalTasksResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.LocalTasksResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.LocalTasksResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.LocalTasksResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.LocalTasksResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.LocalTasksResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.LocalTasksResponse parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.LocalTasksResponse prototype) {
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
   * Protobuf type {@code littlehorse.LocalTasksResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.LocalTasksResponse)
      io.littlehorse.common.proto.LocalTasksResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.ClusterHealth.internal_static_littlehorse_LocalTasksResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.ClusterHealth.internal_static_littlehorse_LocalTasksResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.LocalTasksResponse.class, io.littlehorse.common.proto.LocalTasksResponse.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.LocalTasksResponse.newBuilder()
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
      if (activeTasksBuilder_ == null) {
        activeTasks_ = java.util.Collections.emptyList();
      } else {
        activeTasks_ = null;
        activeTasksBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      if (standbyTasksBuilder_ == null) {
        standbyTasks_ = java.util.Collections.emptyList();
      } else {
        standbyTasks_ = null;
        standbyTasksBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000002);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.ClusterHealth.internal_static_littlehorse_LocalTasksResponse_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.LocalTasksResponse getDefaultInstanceForType() {
      return io.littlehorse.common.proto.LocalTasksResponse.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.LocalTasksResponse build() {
      io.littlehorse.common.proto.LocalTasksResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.LocalTasksResponse buildPartial() {
      io.littlehorse.common.proto.LocalTasksResponse result = new io.littlehorse.common.proto.LocalTasksResponse(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(io.littlehorse.common.proto.LocalTasksResponse result) {
      if (activeTasksBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          activeTasks_ = java.util.Collections.unmodifiableList(activeTasks_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.activeTasks_ = activeTasks_;
      } else {
        result.activeTasks_ = activeTasksBuilder_.build();
      }
      if (standbyTasksBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0)) {
          standbyTasks_ = java.util.Collections.unmodifiableList(standbyTasks_);
          bitField0_ = (bitField0_ & ~0x00000002);
        }
        result.standbyTasks_ = standbyTasks_;
      } else {
        result.standbyTasks_ = standbyTasksBuilder_.build();
      }
    }

    private void buildPartial0(io.littlehorse.common.proto.LocalTasksResponse result) {
      int from_bitField0_ = bitField0_;
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
      if (other instanceof io.littlehorse.common.proto.LocalTasksResponse) {
        return mergeFrom((io.littlehorse.common.proto.LocalTasksResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.LocalTasksResponse other) {
      if (other == io.littlehorse.common.proto.LocalTasksResponse.getDefaultInstance()) return this;
      if (activeTasksBuilder_ == null) {
        if (!other.activeTasks_.isEmpty()) {
          if (activeTasks_.isEmpty()) {
            activeTasks_ = other.activeTasks_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureActiveTasksIsMutable();
            activeTasks_.addAll(other.activeTasks_);
          }
          onChanged();
        }
      } else {
        if (!other.activeTasks_.isEmpty()) {
          if (activeTasksBuilder_.isEmpty()) {
            activeTasksBuilder_.dispose();
            activeTasksBuilder_ = null;
            activeTasks_ = other.activeTasks_;
            bitField0_ = (bitField0_ & ~0x00000001);
            activeTasksBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getActiveTasksFieldBuilder() : null;
          } else {
            activeTasksBuilder_.addAllMessages(other.activeTasks_);
          }
        }
      }
      if (standbyTasksBuilder_ == null) {
        if (!other.standbyTasks_.isEmpty()) {
          if (standbyTasks_.isEmpty()) {
            standbyTasks_ = other.standbyTasks_;
            bitField0_ = (bitField0_ & ~0x00000002);
          } else {
            ensureStandbyTasksIsMutable();
            standbyTasks_.addAll(other.standbyTasks_);
          }
          onChanged();
        }
      } else {
        if (!other.standbyTasks_.isEmpty()) {
          if (standbyTasksBuilder_.isEmpty()) {
            standbyTasksBuilder_.dispose();
            standbyTasksBuilder_ = null;
            standbyTasks_ = other.standbyTasks_;
            bitField0_ = (bitField0_ & ~0x00000002);
            standbyTasksBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getStandbyTasksFieldBuilder() : null;
          } else {
            standbyTasksBuilder_.addAllMessages(other.standbyTasks_);
          }
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
              io.littlehorse.common.proto.TaskStatePb m =
                  input.readMessage(
                      io.littlehorse.common.proto.TaskStatePb.parser(),
                      extensionRegistry);
              if (activeTasksBuilder_ == null) {
                ensureActiveTasksIsMutable();
                activeTasks_.add(m);
              } else {
                activeTasksBuilder_.addMessage(m);
              }
              break;
            } // case 10
            case 18: {
              io.littlehorse.common.proto.StandByTaskStatePb m =
                  input.readMessage(
                      io.littlehorse.common.proto.StandByTaskStatePb.parser(),
                      extensionRegistry);
              if (standbyTasksBuilder_ == null) {
                ensureStandbyTasksIsMutable();
                standbyTasks_.add(m);
              } else {
                standbyTasksBuilder_.addMessage(m);
              }
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

    private java.util.List<io.littlehorse.common.proto.TaskStatePb> activeTasks_ =
      java.util.Collections.emptyList();
    private void ensureActiveTasksIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        activeTasks_ = new java.util.ArrayList<io.littlehorse.common.proto.TaskStatePb>(activeTasks_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.littlehorse.common.proto.TaskStatePb, io.littlehorse.common.proto.TaskStatePb.Builder, io.littlehorse.common.proto.TaskStatePbOrBuilder> activeTasksBuilder_;

    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public java.util.List<io.littlehorse.common.proto.TaskStatePb> getActiveTasksList() {
      if (activeTasksBuilder_ == null) {
        return java.util.Collections.unmodifiableList(activeTasks_);
      } else {
        return activeTasksBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public int getActiveTasksCount() {
      if (activeTasksBuilder_ == null) {
        return activeTasks_.size();
      } else {
        return activeTasksBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public io.littlehorse.common.proto.TaskStatePb getActiveTasks(int index) {
      if (activeTasksBuilder_ == null) {
        return activeTasks_.get(index);
      } else {
        return activeTasksBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public Builder setActiveTasks(
        int index, io.littlehorse.common.proto.TaskStatePb value) {
      if (activeTasksBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureActiveTasksIsMutable();
        activeTasks_.set(index, value);
        onChanged();
      } else {
        activeTasksBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public Builder setActiveTasks(
        int index, io.littlehorse.common.proto.TaskStatePb.Builder builderForValue) {
      if (activeTasksBuilder_ == null) {
        ensureActiveTasksIsMutable();
        activeTasks_.set(index, builderForValue.build());
        onChanged();
      } else {
        activeTasksBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public Builder addActiveTasks(io.littlehorse.common.proto.TaskStatePb value) {
      if (activeTasksBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureActiveTasksIsMutable();
        activeTasks_.add(value);
        onChanged();
      } else {
        activeTasksBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public Builder addActiveTasks(
        int index, io.littlehorse.common.proto.TaskStatePb value) {
      if (activeTasksBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureActiveTasksIsMutable();
        activeTasks_.add(index, value);
        onChanged();
      } else {
        activeTasksBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public Builder addActiveTasks(
        io.littlehorse.common.proto.TaskStatePb.Builder builderForValue) {
      if (activeTasksBuilder_ == null) {
        ensureActiveTasksIsMutable();
        activeTasks_.add(builderForValue.build());
        onChanged();
      } else {
        activeTasksBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public Builder addActiveTasks(
        int index, io.littlehorse.common.proto.TaskStatePb.Builder builderForValue) {
      if (activeTasksBuilder_ == null) {
        ensureActiveTasksIsMutable();
        activeTasks_.add(index, builderForValue.build());
        onChanged();
      } else {
        activeTasksBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public Builder addAllActiveTasks(
        java.lang.Iterable<? extends io.littlehorse.common.proto.TaskStatePb> values) {
      if (activeTasksBuilder_ == null) {
        ensureActiveTasksIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, activeTasks_);
        onChanged();
      } else {
        activeTasksBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public Builder clearActiveTasks() {
      if (activeTasksBuilder_ == null) {
        activeTasks_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        activeTasksBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public Builder removeActiveTasks(int index) {
      if (activeTasksBuilder_ == null) {
        ensureActiveTasksIsMutable();
        activeTasks_.remove(index);
        onChanged();
      } else {
        activeTasksBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public io.littlehorse.common.proto.TaskStatePb.Builder getActiveTasksBuilder(
        int index) {
      return getActiveTasksFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public io.littlehorse.common.proto.TaskStatePbOrBuilder getActiveTasksOrBuilder(
        int index) {
      if (activeTasksBuilder_ == null) {
        return activeTasks_.get(index);  } else {
        return activeTasksBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public java.util.List<? extends io.littlehorse.common.proto.TaskStatePbOrBuilder> 
         getActiveTasksOrBuilderList() {
      if (activeTasksBuilder_ != null) {
        return activeTasksBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(activeTasks_);
      }
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public io.littlehorse.common.proto.TaskStatePb.Builder addActiveTasksBuilder() {
      return getActiveTasksFieldBuilder().addBuilder(
          io.littlehorse.common.proto.TaskStatePb.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public io.littlehorse.common.proto.TaskStatePb.Builder addActiveTasksBuilder(
        int index) {
      return getActiveTasksFieldBuilder().addBuilder(
          index, io.littlehorse.common.proto.TaskStatePb.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.TaskStatePb active_tasks = 1;</code>
     */
    public java.util.List<io.littlehorse.common.proto.TaskStatePb.Builder> 
         getActiveTasksBuilderList() {
      return getActiveTasksFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.littlehorse.common.proto.TaskStatePb, io.littlehorse.common.proto.TaskStatePb.Builder, io.littlehorse.common.proto.TaskStatePbOrBuilder> 
        getActiveTasksFieldBuilder() {
      if (activeTasksBuilder_ == null) {
        activeTasksBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            io.littlehorse.common.proto.TaskStatePb, io.littlehorse.common.proto.TaskStatePb.Builder, io.littlehorse.common.proto.TaskStatePbOrBuilder>(
                activeTasks_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        activeTasks_ = null;
      }
      return activeTasksBuilder_;
    }

    private java.util.List<io.littlehorse.common.proto.StandByTaskStatePb> standbyTasks_ =
      java.util.Collections.emptyList();
    private void ensureStandbyTasksIsMutable() {
      if (!((bitField0_ & 0x00000002) != 0)) {
        standbyTasks_ = new java.util.ArrayList<io.littlehorse.common.proto.StandByTaskStatePb>(standbyTasks_);
        bitField0_ |= 0x00000002;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.littlehorse.common.proto.StandByTaskStatePb, io.littlehorse.common.proto.StandByTaskStatePb.Builder, io.littlehorse.common.proto.StandByTaskStatePbOrBuilder> standbyTasksBuilder_;

    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public java.util.List<io.littlehorse.common.proto.StandByTaskStatePb> getStandbyTasksList() {
      if (standbyTasksBuilder_ == null) {
        return java.util.Collections.unmodifiableList(standbyTasks_);
      } else {
        return standbyTasksBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public int getStandbyTasksCount() {
      if (standbyTasksBuilder_ == null) {
        return standbyTasks_.size();
      } else {
        return standbyTasksBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public io.littlehorse.common.proto.StandByTaskStatePb getStandbyTasks(int index) {
      if (standbyTasksBuilder_ == null) {
        return standbyTasks_.get(index);
      } else {
        return standbyTasksBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public Builder setStandbyTasks(
        int index, io.littlehorse.common.proto.StandByTaskStatePb value) {
      if (standbyTasksBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureStandbyTasksIsMutable();
        standbyTasks_.set(index, value);
        onChanged();
      } else {
        standbyTasksBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public Builder setStandbyTasks(
        int index, io.littlehorse.common.proto.StandByTaskStatePb.Builder builderForValue) {
      if (standbyTasksBuilder_ == null) {
        ensureStandbyTasksIsMutable();
        standbyTasks_.set(index, builderForValue.build());
        onChanged();
      } else {
        standbyTasksBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public Builder addStandbyTasks(io.littlehorse.common.proto.StandByTaskStatePb value) {
      if (standbyTasksBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureStandbyTasksIsMutable();
        standbyTasks_.add(value);
        onChanged();
      } else {
        standbyTasksBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public Builder addStandbyTasks(
        int index, io.littlehorse.common.proto.StandByTaskStatePb value) {
      if (standbyTasksBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureStandbyTasksIsMutable();
        standbyTasks_.add(index, value);
        onChanged();
      } else {
        standbyTasksBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public Builder addStandbyTasks(
        io.littlehorse.common.proto.StandByTaskStatePb.Builder builderForValue) {
      if (standbyTasksBuilder_ == null) {
        ensureStandbyTasksIsMutable();
        standbyTasks_.add(builderForValue.build());
        onChanged();
      } else {
        standbyTasksBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public Builder addStandbyTasks(
        int index, io.littlehorse.common.proto.StandByTaskStatePb.Builder builderForValue) {
      if (standbyTasksBuilder_ == null) {
        ensureStandbyTasksIsMutable();
        standbyTasks_.add(index, builderForValue.build());
        onChanged();
      } else {
        standbyTasksBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public Builder addAllStandbyTasks(
        java.lang.Iterable<? extends io.littlehorse.common.proto.StandByTaskStatePb> values) {
      if (standbyTasksBuilder_ == null) {
        ensureStandbyTasksIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, standbyTasks_);
        onChanged();
      } else {
        standbyTasksBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public Builder clearStandbyTasks() {
      if (standbyTasksBuilder_ == null) {
        standbyTasks_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
      } else {
        standbyTasksBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public Builder removeStandbyTasks(int index) {
      if (standbyTasksBuilder_ == null) {
        ensureStandbyTasksIsMutable();
        standbyTasks_.remove(index);
        onChanged();
      } else {
        standbyTasksBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public io.littlehorse.common.proto.StandByTaskStatePb.Builder getStandbyTasksBuilder(
        int index) {
      return getStandbyTasksFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public io.littlehorse.common.proto.StandByTaskStatePbOrBuilder getStandbyTasksOrBuilder(
        int index) {
      if (standbyTasksBuilder_ == null) {
        return standbyTasks_.get(index);  } else {
        return standbyTasksBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public java.util.List<? extends io.littlehorse.common.proto.StandByTaskStatePbOrBuilder> 
         getStandbyTasksOrBuilderList() {
      if (standbyTasksBuilder_ != null) {
        return standbyTasksBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(standbyTasks_);
      }
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public io.littlehorse.common.proto.StandByTaskStatePb.Builder addStandbyTasksBuilder() {
      return getStandbyTasksFieldBuilder().addBuilder(
          io.littlehorse.common.proto.StandByTaskStatePb.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public io.littlehorse.common.proto.StandByTaskStatePb.Builder addStandbyTasksBuilder(
        int index) {
      return getStandbyTasksFieldBuilder().addBuilder(
          index, io.littlehorse.common.proto.StandByTaskStatePb.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.StandByTaskStatePb standby_tasks = 2;</code>
     */
    public java.util.List<io.littlehorse.common.proto.StandByTaskStatePb.Builder> 
         getStandbyTasksBuilderList() {
      return getStandbyTasksFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.littlehorse.common.proto.StandByTaskStatePb, io.littlehorse.common.proto.StandByTaskStatePb.Builder, io.littlehorse.common.proto.StandByTaskStatePbOrBuilder> 
        getStandbyTasksFieldBuilder() {
      if (standbyTasksBuilder_ == null) {
        standbyTasksBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            io.littlehorse.common.proto.StandByTaskStatePb, io.littlehorse.common.proto.StandByTaskStatePb.Builder, io.littlehorse.common.proto.StandByTaskStatePbOrBuilder>(
                standbyTasks_,
                ((bitField0_ & 0x00000002) != 0),
                getParentForChildren(),
                isClean());
        standbyTasks_ = null;
      }
      return standbyTasksBuilder_;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.LocalTasksResponse)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.LocalTasksResponse)
  private static final io.littlehorse.common.proto.LocalTasksResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.LocalTasksResponse();
  }

  public static io.littlehorse.common.proto.LocalTasksResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<LocalTasksResponse>
      PARSER = new com.google.protobuf.AbstractParser<LocalTasksResponse>() {
    @java.lang.Override
    public LocalTasksResponse parsePartialFrom(
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

  public static com.google.protobuf.Parser<LocalTasksResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<LocalTasksResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.LocalTasksResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

