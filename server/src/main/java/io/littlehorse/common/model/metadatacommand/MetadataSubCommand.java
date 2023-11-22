package io.littlehorse.common.model.metadatacommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.ExecutionContext;
import io.littlehorse.common.model.SubCommand;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;

public abstract class MetadataSubCommand<T extends Message> extends LHSerializable<T> implements SubCommand<T> {

    public abstract Message process(MetadataCommandExecution executionContext, LHServerConfig config);
}
