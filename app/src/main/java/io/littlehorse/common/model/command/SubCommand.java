package io.littlehorse.common.model.command;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHProcessorStores;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;

public abstract class SubCommand<T extends MessageOrBuilder>
    extends LHSerializable<T> {

    public abstract LHSerializable<?> process(LHProcessorStores stores)
        throws LHValidationError, LHConnectionError;

    public abstract boolean hasResponse();
}
