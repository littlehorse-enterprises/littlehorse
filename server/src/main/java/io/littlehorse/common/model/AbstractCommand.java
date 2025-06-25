package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.proto.LHStoreType;
import java.util.Optional;
import lombok.Setter;

@Setter
public abstract class AbstractCommand<T extends Message> extends LHSerializable<T> {

    protected String commandId;

    public abstract LHStoreType getStore();

    public abstract String getTopic(LHServerConfig config);

    // Metadata commands will return a dummy value
    public abstract String getPartitionKey();

    public Optional<String> getCommandId() {
        return Optional.ofNullable(commandId);
    }
}
