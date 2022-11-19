package io.littlehorse.common.model.command;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.server.streamsbackend.coreserver.CoreServerProcessorDaoImpl;

public abstract class SubCommand<T extends MessageOrBuilder>
    extends LHSerializable<T> {

    public abstract LHSerializable<?> process(CoreServerProcessorDaoImpl dao);

    public abstract boolean hasResponse();
}
