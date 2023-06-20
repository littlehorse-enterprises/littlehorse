package io.littlehorse.common.model.command;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.LHSerializable;

public abstract class SubCommand<T extends Message> extends LHSerializable<T> {

    public abstract AbstractResponse<?> process(LHDAO dao, LHConfig config);

    public abstract boolean hasResponse();

    public abstract String getPartitionKey();
}
