package io.littlehorse.server.monitoring.metrics;

import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.proto.Command;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandProcessorMetrics implements MeterBinder {

    private final Logger logger = LoggerFactory.getLogger(CommandProcessorMetrics.class);

    static final String METRIC_NAME = "lh_command_processor_commands_total";
    static final String COMMAND_TYPE_TAG = "type";
    private MeterRegistry registry;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        for (Command.CommandCase commandType : Command.CommandCase.values()) {
            if (commandType != Command.CommandCase.COMMAND_NOT_SET) {
                registry.counter(METRIC_NAME, COMMAND_TYPE_TAG, commandType.name());
            }
        }
    }

    public void observe(CommandModel command) {
        if (registry == null) {
            logger.warn("Ignoring command: " + command.getType().name() + " because metrics are initialized yet.");
            return;
        }
        if (command.getType() != Command.CommandCase.COMMAND_NOT_SET) {
            registry.get(METRIC_NAME)
                    .tag(COMMAND_TYPE_TAG, command.getType().name())
                    .counter()
                    .increment();
        }
    }
}
