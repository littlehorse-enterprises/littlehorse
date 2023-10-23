package io.littlehorse.common.model.corecommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.SubCommand;

public abstract class CoreSubCommand<T extends Message> extends LHSerializable<T> implements SubCommand<T> {

    public abstract Message process(CoreProcessorDAO dao, LHServerConfig config, String tenantId);

    public abstract boolean hasResponse();

    public abstract String getPartitionKey();
}
