package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.util.LHUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractCommand<T extends Message> extends LHSerializable<T> {

    public AbstractCommand() {
        commandId = LHUtil.generateGuid();
    }

    private String commandId;

    public abstract LHStoreType getStore();

    public abstract String getTopic(LHConfig config);

    // Metadata commands will return a dummy value
    public abstract String getPartitionKey();
}
