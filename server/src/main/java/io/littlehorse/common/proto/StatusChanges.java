// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: command.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.common.proto;

/**
 * Protobuf type {@code littlehorse.StatusChanges}
 */
public final class StatusChanges extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.StatusChanges)
    StatusChangesOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      StatusChanges.class.getName());
  }
  // Use StatusChanges.newBuilder() to construct.
  private StatusChanges(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private StatusChanges() {
    changes_ = java.util.Collections.emptyList();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_StatusChanges_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_StatusChanges_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.StatusChanges.class, io.littlehorse.common.proto.StatusChanges.Builder.class);
  }

  public static final int CHANGES_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<io.littlehorse.common.proto.StatusChanged> changes_;
  /**
   * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
   */
  @java.lang.Override
  public java.util.List<io.littlehorse.common.proto.StatusChanged> getChangesList() {
    return changes_;
  }
  /**
   * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
   */
  @java.lang.Override
  public java.util.List<? extends io.littlehorse.common.proto.StatusChangedOrBuilder> 
      getChangesOrBuilderList() {
    return changes_;
  }
  /**
   * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
   */
  @java.lang.Override
  public int getChangesCount() {
    return changes_.size();
  }
  /**
   * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.StatusChanged getChanges(int index) {
    return changes_.get(index);
  }
  /**
   * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.StatusChangedOrBuilder getChangesOrBuilder(
      int index) {
    return changes_.get(index);
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
    for (int i = 0; i < changes_.size(); i++) {
      output.writeMessage(1, changes_.get(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < changes_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, changes_.get(i));
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
    if (!(obj instanceof io.littlehorse.common.proto.StatusChanges)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.StatusChanges other = (io.littlehorse.common.proto.StatusChanges) obj;

    if (!getChangesList()
        .equals(other.getChangesList())) return false;
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
    if (getChangesCount() > 0) {
      hash = (37 * hash) + CHANGES_FIELD_NUMBER;
      hash = (53 * hash) + getChangesList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.StatusChanges parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.StatusChanges parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.StatusChanges parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.StatusChanges parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.StatusChanges parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.StatusChanges parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.StatusChanges parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.StatusChanges parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.StatusChanges parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.StatusChanges parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.StatusChanges parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.StatusChanges parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(io.littlehorse.common.proto.StatusChanges prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessage.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code littlehorse.StatusChanges}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.StatusChanges)
      io.littlehorse.common.proto.StatusChangesOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_StatusChanges_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_StatusChanges_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.StatusChanges.class, io.littlehorse.common.proto.StatusChanges.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.StatusChanges.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      if (changesBuilder_ == null) {
        changes_ = java.util.Collections.emptyList();
      } else {
        changes_ = null;
        changesBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_StatusChanges_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.StatusChanges getDefaultInstanceForType() {
      return io.littlehorse.common.proto.StatusChanges.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.StatusChanges build() {
      io.littlehorse.common.proto.StatusChanges result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.StatusChanges buildPartial() {
      io.littlehorse.common.proto.StatusChanges result = new io.littlehorse.common.proto.StatusChanges(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(io.littlehorse.common.proto.StatusChanges result) {
      if (changesBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          changes_ = java.util.Collections.unmodifiableList(changes_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.changes_ = changes_;
      } else {
        result.changes_ = changesBuilder_.build();
      }
    }

    private void buildPartial0(io.littlehorse.common.proto.StatusChanges result) {
      int from_bitField0_ = bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.common.proto.StatusChanges) {
        return mergeFrom((io.littlehorse.common.proto.StatusChanges)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.StatusChanges other) {
      if (other == io.littlehorse.common.proto.StatusChanges.getDefaultInstance()) return this;
      if (changesBuilder_ == null) {
        if (!other.changes_.isEmpty()) {
          if (changes_.isEmpty()) {
            changes_ = other.changes_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureChangesIsMutable();
            changes_.addAll(other.changes_);
          }
          onChanged();
        }
      } else {
        if (!other.changes_.isEmpty()) {
          if (changesBuilder_.isEmpty()) {
            changesBuilder_.dispose();
            changesBuilder_ = null;
            changes_ = other.changes_;
            bitField0_ = (bitField0_ & ~0x00000001);
            changesBuilder_ = 
              com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ?
                 internalGetChangesFieldBuilder() : null;
          } else {
            changesBuilder_.addAllMessages(other.changes_);
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
              io.littlehorse.common.proto.StatusChanged m =
                  input.readMessage(
                      io.littlehorse.common.proto.StatusChanged.parser(),
                      extensionRegistry);
              if (changesBuilder_ == null) {
                ensureChangesIsMutable();
                changes_.add(m);
              } else {
                changesBuilder_.addMessage(m);
              }
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
    private int bitField0_;

    private java.util.List<io.littlehorse.common.proto.StatusChanged> changes_ =
      java.util.Collections.emptyList();
    private void ensureChangesIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        changes_ = new java.util.ArrayList<io.littlehorse.common.proto.StatusChanged>(changes_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilder<
        io.littlehorse.common.proto.StatusChanged, io.littlehorse.common.proto.StatusChanged.Builder, io.littlehorse.common.proto.StatusChangedOrBuilder> changesBuilder_;

    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public java.util.List<io.littlehorse.common.proto.StatusChanged> getChangesList() {
      if (changesBuilder_ == null) {
        return java.util.Collections.unmodifiableList(changes_);
      } else {
        return changesBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public int getChangesCount() {
      if (changesBuilder_ == null) {
        return changes_.size();
      } else {
        return changesBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public io.littlehorse.common.proto.StatusChanged getChanges(int index) {
      if (changesBuilder_ == null) {
        return changes_.get(index);
      } else {
        return changesBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public Builder setChanges(
        int index, io.littlehorse.common.proto.StatusChanged value) {
      if (changesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureChangesIsMutable();
        changes_.set(index, value);
        onChanged();
      } else {
        changesBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public Builder setChanges(
        int index, io.littlehorse.common.proto.StatusChanged.Builder builderForValue) {
      if (changesBuilder_ == null) {
        ensureChangesIsMutable();
        changes_.set(index, builderForValue.build());
        onChanged();
      } else {
        changesBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public Builder addChanges(io.littlehorse.common.proto.StatusChanged value) {
      if (changesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureChangesIsMutable();
        changes_.add(value);
        onChanged();
      } else {
        changesBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public Builder addChanges(
        int index, io.littlehorse.common.proto.StatusChanged value) {
      if (changesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureChangesIsMutable();
        changes_.add(index, value);
        onChanged();
      } else {
        changesBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public Builder addChanges(
        io.littlehorse.common.proto.StatusChanged.Builder builderForValue) {
      if (changesBuilder_ == null) {
        ensureChangesIsMutable();
        changes_.add(builderForValue.build());
        onChanged();
      } else {
        changesBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public Builder addChanges(
        int index, io.littlehorse.common.proto.StatusChanged.Builder builderForValue) {
      if (changesBuilder_ == null) {
        ensureChangesIsMutable();
        changes_.add(index, builderForValue.build());
        onChanged();
      } else {
        changesBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public Builder addAllChanges(
        java.lang.Iterable<? extends io.littlehorse.common.proto.StatusChanged> values) {
      if (changesBuilder_ == null) {
        ensureChangesIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, changes_);
        onChanged();
      } else {
        changesBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public Builder clearChanges() {
      if (changesBuilder_ == null) {
        changes_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        changesBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public Builder removeChanges(int index) {
      if (changesBuilder_ == null) {
        ensureChangesIsMutable();
        changes_.remove(index);
        onChanged();
      } else {
        changesBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public io.littlehorse.common.proto.StatusChanged.Builder getChangesBuilder(
        int index) {
      return internalGetChangesFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public io.littlehorse.common.proto.StatusChangedOrBuilder getChangesOrBuilder(
        int index) {
      if (changesBuilder_ == null) {
        return changes_.get(index);  } else {
        return changesBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public java.util.List<? extends io.littlehorse.common.proto.StatusChangedOrBuilder> 
         getChangesOrBuilderList() {
      if (changesBuilder_ != null) {
        return changesBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(changes_);
      }
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public io.littlehorse.common.proto.StatusChanged.Builder addChangesBuilder() {
      return internalGetChangesFieldBuilder().addBuilder(
          io.littlehorse.common.proto.StatusChanged.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public io.littlehorse.common.proto.StatusChanged.Builder addChangesBuilder(
        int index) {
      return internalGetChangesFieldBuilder().addBuilder(
          index, io.littlehorse.common.proto.StatusChanged.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.StatusChanged changes = 1;</code>
     */
    public java.util.List<io.littlehorse.common.proto.StatusChanged.Builder> 
         getChangesBuilderList() {
      return internalGetChangesFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilder<
        io.littlehorse.common.proto.StatusChanged, io.littlehorse.common.proto.StatusChanged.Builder, io.littlehorse.common.proto.StatusChangedOrBuilder> 
        internalGetChangesFieldBuilder() {
      if (changesBuilder_ == null) {
        changesBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<
            io.littlehorse.common.proto.StatusChanged, io.littlehorse.common.proto.StatusChanged.Builder, io.littlehorse.common.proto.StatusChangedOrBuilder>(
                changes_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        changes_ = null;
      }
      return changesBuilder_;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.StatusChanges)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.StatusChanges)
  private static final io.littlehorse.common.proto.StatusChanges DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.StatusChanges();
  }

  public static io.littlehorse.common.proto.StatusChanges getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<StatusChanges>
      PARSER = new com.google.protobuf.AbstractParser<StatusChanges>() {
    @java.lang.Override
    public StatusChanges parsePartialFrom(
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

  public static com.google.protobuf.Parser<StatusChanges> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<StatusChanges> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.StatusChanges getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

