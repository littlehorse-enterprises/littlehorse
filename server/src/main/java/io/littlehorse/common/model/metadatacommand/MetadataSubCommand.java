package io.littlehorse.common.model.metadatacommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.command.AbstractResponse;

public abstract class MetadataSubCommand<T extends Message> extends LHSerializable<T> {

    public abstract AbstractResponse<?> process(MetadataProcessorDAO dao, LHConfig config);

    public abstract boolean hasResponse();
}
