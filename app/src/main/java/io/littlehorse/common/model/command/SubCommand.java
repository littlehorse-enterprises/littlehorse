package io.littlehorse.common.model.command;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.CommandProcessorDao;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHSerializable;

public abstract class SubCommand<T extends MessageOrBuilder>
    extends LHSerializable<T> {

    public abstract AbstractResponse<?> process(
        CommandProcessorDao dao,
        LHConfig config
    );

    public abstract boolean hasResponse();

    public abstract String getPartitionKey();
}
