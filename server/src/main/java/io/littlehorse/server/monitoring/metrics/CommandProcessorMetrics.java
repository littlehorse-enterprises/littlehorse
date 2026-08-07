package io.littlehorse.server.monitoring.metrics;

import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.MetadataCommand;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.EnumMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandProcessorMetrics implements MeterBinder {

    private static final String CORE_COMMAND_TYPE = "core";
    private static final String METADATA_COMMAND_TYPE = "metadata";
    private final Logger logger = LoggerFactory.getLogger(CommandProcessorMetrics.class);

    static final String METRIC_NAME = "lh.commands.processed";
    static final String METRIC_NAME_BY_TYPE = "lh.subcommands.processed";
    static final String COMMAND_TYPE_TAG = "type";

    private final Map<Command.CommandCase, Counter> coreCountersByType = new EnumMap<>(Command.CommandCase.class);
    private final Map<MetadataCommand.MetadataCommandCase, Counter> metadataCountersByType =
            new EnumMap<>(MetadataCommand.MetadataCommandCase.class);

    private volatile Counter coreCommandCounter;
    private volatile Counter metadataCommandCounter;

    @Override
    public void bindTo(MeterRegistry registry) {
        // Register core subcommands
        for (Command.CommandCase commandType : Command.CommandCase.values()) {
            if (commandType != Command.CommandCase.COMMAND_NOT_SET) {
                coreCountersByType.put(
                        commandType, registry.counter(METRIC_NAME_BY_TYPE, COMMAND_TYPE_TAG, commandType.name()));
            }
        }
        // Register metadata subcommands
        for (MetadataCommand.MetadataCommandCase metadataCommandType : MetadataCommand.MetadataCommandCase.values()) {
            if (metadataCommandType != MetadataCommand.MetadataCommandCase.METADATACOMMAND_NOT_SET) {
                metadataCountersByType.put(
                        metadataCommandType,
                        registry.counter(METRIC_NAME_BY_TYPE, COMMAND_TYPE_TAG, metadataCommandType.name()));
            }
        }
        // Assigned last: these act as the "metrics are initialized" flag for observe()
        coreCommandCounter = registry.counter(METRIC_NAME, COMMAND_TYPE_TAG, CORE_COMMAND_TYPE);
        metadataCommandCounter = registry.counter(METRIC_NAME, COMMAND_TYPE_TAG, METADATA_COMMAND_TYPE);
    }

    public void observe(CommandModel command) {
        Counter total = coreCommandCounter;
        if (total == null) {
            logger.warn("Ignoring command: {} because metrics are not initialized yet.", command.getType());
            return;
        }
        // Increase both the general counter and the counter for the specific command type
        Counter byType = coreCountersByType.get(command.getType());
        if (byType == null) {
            // COMMAND_NOT_SET
            return;
        }
        total.increment();
        byType.increment();
    }

    public void observe(MetadataCommandModel metadataCommand) {
        Counter total = metadataCommandCounter;
        if (total == null) {
            logger.warn("Ignoring command: {} because metrics are not initialized yet.", metadataCommand.getType());
            return;
        }
        // Increase both the general counter and the counter for the specific command type
        Counter byType = metadataCountersByType.get(metadataCommand.getType());
        if (byType == null) {
            // METADATACOMMAND_NOT_SET
            return;
        }
        total.increment();
        byType.increment();
    }
}
