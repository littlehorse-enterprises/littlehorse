package io.littlehorse.common.model.corecommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.SubCommand;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public abstract class CoreSubCommand<T extends Message> extends LHSerializable<T> implements SubCommand<T> {

    public abstract Message process(ExecutionContext executionContext, LHServerConfig config);

    public abstract String getPartitionKey();
}
