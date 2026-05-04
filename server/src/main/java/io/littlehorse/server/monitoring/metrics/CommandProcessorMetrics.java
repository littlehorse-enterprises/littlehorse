package io.littlehorse.server.monitoring.metrics;

import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.MetadataCommand;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandProcessorMetrics implements MeterBinder {

    private static final String CORE_COMMAND_TYPE = "core";
    private static final String METADATA_COMMAND_TYPE = "metadata";
    private final Logger logger = LoggerFactory.getLogger(CommandProcessorMetrics.class);

    static final String METRIC_NAME = "lh.commands.processed";
    static final String METRIC_NAME_BY_TYPE = "lh.subcommands.processed";
    static final String COMMAND_TYPE_TAG = "type";
    private MeterRegistry registry;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        registry.counter(METRIC_NAME, COMMAND_TYPE_TAG, CORE_COMMAND_TYPE);
        registry.counter(METRIC_NAME, COMMAND_TYPE_TAG, METADATA_COMMAND_TYPE);
        // Register core subcommands
        for (Command.CommandCase commandType : Command.CommandCase.values()) {
            if (commandType != Command.CommandCase.COMMAND_NOT_SET) {
                registry.counter(METRIC_NAME_BY_TYPE, COMMAND_TYPE_TAG, commandType.name());
            }
        }
        // Register metadata subcommands
        for (MetadataCommand.MetadataCommandCase metadataCommandType : MetadataCommand.MetadataCommandCase.values()) {
            if (metadataCommandType != MetadataCommand.MetadataCommandCase.METADATACOMMAND_NOT_SET) {
                registry.counter(METRIC_NAME_BY_TYPE, COMMAND_TYPE_TAG, metadataCommandType.name());
            }
        }
    }

    public void observe(CommandModel command) {
        if (registry == null) {
            logger.warn("Ignoring command: " + command.getType().name() + " because metrics are initialized yet.");
            return;
        }
        if (command.getType() != Command.CommandCase.COMMAND_NOT_SET) {
            // Increase both the general counter and the counter for the specific command type
            registry.get(METRIC_NAME)
                    .tag(COMMAND_TYPE_TAG, CORE_COMMAND_TYPE)
                    .counter()
                    .increment();
            registry.counter(
                            METRIC_NAME_BY_TYPE,
                            COMMAND_TYPE_TAG,
                            command.getType().name())
                    .increment();
        }
    }

    public void observe(MetadataCommandModel metadataCommand) {
        if (registry == null) {
            logger.warn(
                    "Ignoring command: " + metadataCommand.getType().name() + " because metrics are initialized yet.");
            return;
        }
        if (metadataCommand.getType() != MetadataCommand.MetadataCommandCase.METADATACOMMAND_NOT_SET) {
            // Increase both the general counter and the counter for the specific command type
            registry.get(METRIC_NAME)
                    .tag(COMMAND_TYPE_TAG, METADATA_COMMAND_TYPE)
                    .counter()
                    .increment();
            registry.counter(
                            METRIC_NAME_BY_TYPE,
                            COMMAND_TYPE_TAG,
                            metadataCommand.getType().name())
                    .increment();
        }
    }
}
