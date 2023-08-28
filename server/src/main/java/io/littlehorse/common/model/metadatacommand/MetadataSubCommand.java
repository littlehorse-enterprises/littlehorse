package io.littlehorse.common.model.metadatacommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.MetadataProcessorDAO;

public abstract class MetadataSubCommand<T extends Message> extends LHSerializable<T> {

    public abstract Message process(MetadataProcessorDAO dao, LHServerConfig config);

    public abstract boolean hasResponse();
}
