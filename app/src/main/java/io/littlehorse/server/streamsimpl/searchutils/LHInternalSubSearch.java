package io.littlehorse.server.streamsimpl.searchutils;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;

// This is just a passthrough class
public abstract class LHInternalSubSearch<T extends MessageOrBuilder>
    extends LHSerializable<T> {}
