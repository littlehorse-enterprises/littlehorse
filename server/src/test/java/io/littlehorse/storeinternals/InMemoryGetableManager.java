package io.littlehorse.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.HashMap;
import java.util.Map;

public class InMemoryGetableManager extends GetableManager {

    private final Map<ObjectIdModel<?, ?, ?>, AbstractGetable<?>> buffer = new HashMap<>();

    public InMemoryGetableManager(ProcessorExecutionContext executionContext) {
        super(null, null, null, null, executionContext);
    }

    @Override
    public <U extends Message, T extends CoreGetable<U>> void put(T getable) throws IllegalStateException {
        buffer.put(getable.getObjectId(), getable);
    }

    @Override
    public <U extends Message, T extends CoreGetable<U>> T get(CoreObjectId<?, U, T> id) {
        return (T) buffer.get(id);
    }
}
