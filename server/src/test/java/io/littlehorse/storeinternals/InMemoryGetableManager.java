package io.littlehorse.storeinternals;

import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;

public class InMemoryGetableManager extends GetableManager {

    public InMemoryGetableManager(ProcessorExecutionContext executionContext) {
        super(null, null, null, null, executionContext);
    }
}
