package io.littlehorse.common.model.metadatacommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;

public abstract class MetadataSubCommand<T extends Message> extends LHSerializable<T> {

    public abstract Message process(MetadataProcessorContext executionContext);
}
