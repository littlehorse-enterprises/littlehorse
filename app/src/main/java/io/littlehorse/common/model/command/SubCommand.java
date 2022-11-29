package io.littlehorse.common.model.command;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.server.CommandProcessorDao;

public abstract class SubCommand<T extends MessageOrBuilder>
    extends LHSerializable<T> {

    public abstract LHSerializable<?> process(
        CommandProcessorDao dao,
        LHConfig config
    );

    public abstract boolean hasResponse();

    public abstract String getPartitionKey();
}
