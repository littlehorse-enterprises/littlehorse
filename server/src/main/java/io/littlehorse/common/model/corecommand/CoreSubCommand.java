package io.littlehorse.common.model.corecommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;

public abstract class CoreSubCommand<T extends Message> extends LHSerializable<T> {

    public abstract Message process(ProcessorExecutionContext executionContext, LHServerConfig config);

    public abstract String getPartitionKey();
}
