package io.littlehorse.common.model.corecommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.CoreProcessorDAO;

public abstract class SubCommand<T extends Message> extends LHSerializable<T> {

    public abstract Message process(CoreProcessorDAO dao, LHConfig config);

    public abstract boolean hasResponse();

    public abstract String getPartitionKey();
}
