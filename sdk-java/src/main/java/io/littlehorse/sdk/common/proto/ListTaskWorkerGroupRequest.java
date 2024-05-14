// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.ListTaskWorkerGroupRequest}
 */
public final class ListTaskWorkerGroupRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.ListTaskWorkerGroupRequest)
    ListTaskWorkerGroupRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ListTaskWorkerGroupRequest.newBuilder() to construct.
  private ListTaskWorkerGroupRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ListTaskWorkerGroupRequest() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ListTaskWorkerGroupRequest();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListTaskWorkerGroupRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListTaskWorkerGroupRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest.class, io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest.Builder.class);
  }

  private int taskWorkerGroupCriteriaCase_ = 0;
  @SuppressWarnings("serial")
  private java.lang.Object taskWorkerGroupCriteria_;
  public enum TaskWorkerGroupCriteriaCase
      implements com.google.protobuf.Internal.EnumLite,
          com.google.protobuf.AbstractMessage.InternalOneOfEnum {
    TASK_DEF_ID(1),
    TASKWORKERGROUPCRITERIA_NOT_SET(0);
    private final int value;
    private TaskWorkerGroupCriteriaCase(int value) {
      this.value = value;
    }
    /**
     * @param value The number of the enum to look for.
     * @return The enum associated with the given number.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static TaskWorkerGroupCriteriaCase valueOf(int value) {
      return forNumber(value);
    }

    public static TaskWorkerGroupCriteriaCase forNumber(int value) {
      switch (value) {
        case 1: return TASK_DEF_ID;
        case 0: return TASKWORKERGROUPCRITERIA_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  public TaskWorkerGroupCriteriaCase
  getTaskWorkerGroupCriteriaCase() {
    return TaskWorkerGroupCriteriaCase.forNumber(
        taskWorkerGroupCriteriaCase_);
  }

  public static final int TASK_DEF_ID_FIELD_NUMBER = 1;
  /**
   * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
   * @return Whether the taskDefId field is set.
   */
  @java.lang.Override
  public boolean hasTaskDefId() {
    return taskWorkerGroupCriteriaCase_ == 1;
  }
  /**
   * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
   * @return The taskDefId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TaskDefId getTaskDefId() {
    if (taskWorkerGroupCriteriaCase_ == 1) {
       return (io.littlehorse.sdk.common.proto.TaskDefId) taskWorkerGroupCriteria_;
    }
    return io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance();
  }
  /**
   * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder getTaskDefIdOrBuilder() {
    if (taskWorkerGroupCriteriaCase_ == 1) {
       return (io.littlehorse.sdk.common.proto.TaskDefId) taskWorkerGroupCriteria_;
    }
    return io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance();
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
    if (taskWorkerGroupCriteriaCase_ == 1) {
      output.writeMessage(1, (io.littlehorse.sdk.common.proto.TaskDefId) taskWorkerGroupCriteria_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (taskWorkerGroupCriteriaCase_ == 1) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, (io.littlehorse.sdk.common.proto.TaskDefId) taskWorkerGroupCriteria_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest other = (io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest) obj;

    if (!getTaskWorkerGroupCriteriaCase().equals(other.getTaskWorkerGroupCriteriaCase())) return false;
    switch (taskWorkerGroupCriteriaCase_) {
      case 1:
        if (!getTaskDefId()
            .equals(other.getTaskDefId())) return false;
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
    switch (taskWorkerGroupCriteriaCase_) {
      case 1:
        hash = (37 * hash) + TASK_DEF_ID_FIELD_NUMBER;
        hash = (53 * hash) + getTaskDefId().hashCode();
        break;
      case 0:
      default:
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest prototype) {
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
   * Protobuf type {@code littlehorse.ListTaskWorkerGroupRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.ListTaskWorkerGroupRequest)
      io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListTaskWorkerGroupRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListTaskWorkerGroupRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest.class, io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest.newBuilder()
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
      if (taskDefIdBuilder_ != null) {
        taskDefIdBuilder_.clear();
      }
      taskWorkerGroupCriteriaCase_ = 0;
      taskWorkerGroupCriteria_ = null;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListTaskWorkerGroupRequest_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest build() {
      io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest buildPartial() {
      io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest result = new io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      buildPartialOneofs(result);
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest result) {
      int from_bitField0_ = bitField0_;
    }

    private void buildPartialOneofs(io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest result) {
      result.taskWorkerGroupCriteriaCase_ = taskWorkerGroupCriteriaCase_;
      result.taskWorkerGroupCriteria_ = this.taskWorkerGroupCriteria_;
      if (taskWorkerGroupCriteriaCase_ == 1 &&
          taskDefIdBuilder_ != null) {
        result.taskWorkerGroupCriteria_ = taskDefIdBuilder_.build();
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
      if (other instanceof io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest) {
        return mergeFrom((io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest other) {
      if (other == io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest.getDefaultInstance()) return this;
      switch (other.getTaskWorkerGroupCriteriaCase()) {
        case TASK_DEF_ID: {
          mergeTaskDefId(other.getTaskDefId());
          break;
        }
        case TASKWORKERGROUPCRITERIA_NOT_SET: {
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
              input.readMessage(
                  getTaskDefIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              taskWorkerGroupCriteriaCase_ = 1;
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
    private int taskWorkerGroupCriteriaCase_ = 0;
    private java.lang.Object taskWorkerGroupCriteria_;
    public TaskWorkerGroupCriteriaCase
        getTaskWorkerGroupCriteriaCase() {
      return TaskWorkerGroupCriteriaCase.forNumber(
          taskWorkerGroupCriteriaCase_);
    }

    public Builder clearTaskWorkerGroupCriteria() {
      taskWorkerGroupCriteriaCase_ = 0;
      taskWorkerGroupCriteria_ = null;
      onChanged();
      return this;
    }

    private int bitField0_;

    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.TaskDefId, io.littlehorse.sdk.common.proto.TaskDefId.Builder, io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder> taskDefIdBuilder_;
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
     * @return Whether the taskDefId field is set.
     */
    @java.lang.Override
    public boolean hasTaskDefId() {
      return taskWorkerGroupCriteriaCase_ == 1;
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
     * @return The taskDefId.
     */
    @java.lang.Override
    public io.littlehorse.sdk.common.proto.TaskDefId getTaskDefId() {
      if (taskDefIdBuilder_ == null) {
        if (taskWorkerGroupCriteriaCase_ == 1) {
          return (io.littlehorse.sdk.common.proto.TaskDefId) taskWorkerGroupCriteria_;
        }
        return io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance();
      } else {
        if (taskWorkerGroupCriteriaCase_ == 1) {
          return taskDefIdBuilder_.getMessage();
        }
        return io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance();
      }
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
     */
    public Builder setTaskDefId(io.littlehorse.sdk.common.proto.TaskDefId value) {
      if (taskDefIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        taskWorkerGroupCriteria_ = value;
        onChanged();
      } else {
        taskDefIdBuilder_.setMessage(value);
      }
      taskWorkerGroupCriteriaCase_ = 1;
      return this;
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
     */
    public Builder setTaskDefId(
        io.littlehorse.sdk.common.proto.TaskDefId.Builder builderForValue) {
      if (taskDefIdBuilder_ == null) {
        taskWorkerGroupCriteria_ = builderForValue.build();
        onChanged();
      } else {
        taskDefIdBuilder_.setMessage(builderForValue.build());
      }
      taskWorkerGroupCriteriaCase_ = 1;
      return this;
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
     */
    public Builder mergeTaskDefId(io.littlehorse.sdk.common.proto.TaskDefId value) {
      if (taskDefIdBuilder_ == null) {
        if (taskWorkerGroupCriteriaCase_ == 1 &&
            taskWorkerGroupCriteria_ != io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance()) {
          taskWorkerGroupCriteria_ = io.littlehorse.sdk.common.proto.TaskDefId.newBuilder((io.littlehorse.sdk.common.proto.TaskDefId) taskWorkerGroupCriteria_)
              .mergeFrom(value).buildPartial();
        } else {
          taskWorkerGroupCriteria_ = value;
        }
        onChanged();
      } else {
        if (taskWorkerGroupCriteriaCase_ == 1) {
          taskDefIdBuilder_.mergeFrom(value);
        } else {
          taskDefIdBuilder_.setMessage(value);
        }
      }
      taskWorkerGroupCriteriaCase_ = 1;
      return this;
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
     */
    public Builder clearTaskDefId() {
      if (taskDefIdBuilder_ == null) {
        if (taskWorkerGroupCriteriaCase_ == 1) {
          taskWorkerGroupCriteriaCase_ = 0;
          taskWorkerGroupCriteria_ = null;
          onChanged();
        }
      } else {
        if (taskWorkerGroupCriteriaCase_ == 1) {
          taskWorkerGroupCriteriaCase_ = 0;
          taskWorkerGroupCriteria_ = null;
        }
        taskDefIdBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.TaskDefId.Builder getTaskDefIdBuilder() {
      return getTaskDefIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
     */
    @java.lang.Override
    public io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder getTaskDefIdOrBuilder() {
      if ((taskWorkerGroupCriteriaCase_ == 1) && (taskDefIdBuilder_ != null)) {
        return taskDefIdBuilder_.getMessageOrBuilder();
      } else {
        if (taskWorkerGroupCriteriaCase_ == 1) {
          return (io.littlehorse.sdk.common.proto.TaskDefId) taskWorkerGroupCriteria_;
        }
        return io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance();
      }
    }
    /**
     * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.TaskDefId, io.littlehorse.sdk.common.proto.TaskDefId.Builder, io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder> 
        getTaskDefIdFieldBuilder() {
      if (taskDefIdBuilder_ == null) {
        if (!(taskWorkerGroupCriteriaCase_ == 1)) {
          taskWorkerGroupCriteria_ = io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance();
        }
        taskDefIdBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.TaskDefId, io.littlehorse.sdk.common.proto.TaskDefId.Builder, io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder>(
                (io.littlehorse.sdk.common.proto.TaskDefId) taskWorkerGroupCriteria_,
                getParentForChildren(),
                isClean());
        taskWorkerGroupCriteria_ = null;
      }
      taskWorkerGroupCriteriaCase_ = 1;
      onChanged();
      return taskDefIdBuilder_;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.ListTaskWorkerGroupRequest)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.ListTaskWorkerGroupRequest)
  private static final io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest();
  }

  public static io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ListTaskWorkerGroupRequest>
      PARSER = new com.google.protobuf.AbstractParser<ListTaskWorkerGroupRequest>() {
    @java.lang.Override
    public ListTaskWorkerGroupRequest parsePartialFrom(
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

  public static com.google.protobuf.Parser<ListTaskWorkerGroupRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ListTaskWorkerGroupRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

