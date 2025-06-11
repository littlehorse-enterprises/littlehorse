package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.util.LHUtil;
import java.util.Optional;
import lombok.Setter;

@Setter
public abstract class AbstractCommand<T extends Message> extends LHSerializable<T> {

    public AbstractCommand() {
        commandId = LHUtil.generateGuid();
    }

    protected String commandId;

    public abstract LHStoreType getStore();

    public abstract String getTopic(LHServerConfig config);

    // Metadata commands will return a dummy value
    public abstract String getPartitionKey();

    public abstract SubCommand<? extends Message> getSubCommand();

    public Optional<String> getCommandId() {
        return Optional.ofNullable(commandId);
    }
}
