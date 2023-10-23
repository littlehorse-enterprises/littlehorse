package io.littlehorse.common.model.metadatacommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.SubCommand;

public abstract class MetadataSubCommand<T extends Message> extends LHSerializable<T> implements SubCommand<T> {

    public abstract Message process(MetadataProcessorDAO dao, LHServerConfig config);
}
