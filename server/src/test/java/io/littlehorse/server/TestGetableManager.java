package io.littlehorse.server;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public class TestGetableManager extends GetableManager {
    private final TenantScopedStore coreStore;
    private final ProcessorContext<String, CommandProcessorOutput> ctx;
    private final LHServerConfig config;
    private final CommandModel currentCommand;
    private final ExecutionContext executionContext;

    public TestGetableManager(
            TenantScopedStore coreStore,
            ProcessorContext<String, CommandProcessorOutput> ctx,
            LHServerConfig config,
            CommandModel currentCommand,
            CoreProcessorContext executionContext) {
        super(coreStore, ctx, config, currentCommand, executionContext, null);
            ProcessorExecutionContext executionContext) {
        super(coreStore, ctx, config, currentCommand, executionContext);
        this.executionContext = executionContext;
        this.currentCommand = currentCommand;
        this.config = config;
        this.ctx = ctx;
        this.coreStore = coreStore;
    }
}
