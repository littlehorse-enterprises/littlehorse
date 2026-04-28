package io.littlehorse.server.monitoring.metrics;

import static io.littlehorse.server.monitoring.metrics.CommandProcessorMetrics.COMMAND_TYPE_TAG;
import static io.littlehorse.server.monitoring.metrics.CommandProcessorMetrics.METRIC_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.proto.Command;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CommandProcessorMetricsTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final CommandProcessorMetrics metrics = new CommandProcessorMetrics();

    @BeforeEach
    void setUp() {
        metrics.bindTo(registry);
    }

    @Test
    void shouldRegisterCountersForAllCommandTypes() {
        for (Command.CommandCase commandType : Command.CommandCase.values()) {
            if (commandType != Command.CommandCase.COMMAND_NOT_SET) {
                Counter counter = registry.get(METRIC_NAME)
                        .tag(COMMAND_TYPE_TAG, commandType.name())
                        .counter();
                assertThat(counter).isNotNull();
                assertThat(counter.count()).isZero();
            }
        }
    }

    @Test
    void shouldIncrementCounterOnObserve() {
        CommandModel command = Mockito.mock(CommandModel.class);
        Mockito.when(command.getType()).thenReturn(Command.CommandCase.RUN_WF);

        metrics.observe(command);

        Counter counter = registry.get(METRIC_NAME)
                .tag(COMMAND_TYPE_TAG, Command.CommandCase.RUN_WF.name())
                .counter();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldIncrementCounterMultipleTimes() {
        CommandModel command = Mockito.mock(CommandModel.class);
        Mockito.when(command.getType()).thenReturn(Command.CommandCase.RUN_WF);

        metrics.observe(command);
        metrics.observe(command);
        metrics.observe(command);

        Counter counter = registry.get(METRIC_NAME)
                .tag(COMMAND_TYPE_TAG, Command.CommandCase.RUN_WF.name())
                .counter();
        assertThat(counter.count()).isEqualTo(3.0);
    }

    @Test
    void shouldTrackDifferentCommandTypesIndependently() {
        CommandModel runWfCommand = Mockito.mock(CommandModel.class);
        Mockito.when(runWfCommand.getType()).thenReturn(Command.CommandCase.RUN_WF);

        CommandModel taskClaimCommand = Mockito.mock(CommandModel.class);
        Mockito.when(taskClaimCommand.getType()).thenReturn(Command.CommandCase.TASK_CLAIM_EVENT);

        metrics.observe(runWfCommand);
        metrics.observe(runWfCommand);
        metrics.observe(taskClaimCommand);

        assertThat(registry.get(METRIC_NAME)
                        .tag(COMMAND_TYPE_TAG, Command.CommandCase.RUN_WF.name())
                        .counter()
                        .count())
                .isEqualTo(2.0);

        assertThat(registry.get(METRIC_NAME)
                        .tag(COMMAND_TYPE_TAG, Command.CommandCase.TASK_CLAIM_EVENT.name())
                        .counter()
                        .count())
                .isEqualTo(1.0);
    }

    @Test
    void shouldNotThrowWhenObserveIsCalledBeforeBindTo() {
        CommandProcessorMetrics unboundMetrics = new CommandProcessorMetrics();
        CommandModel command = Mockito.mock(CommandModel.class);
        Mockito.when(command.getType()).thenReturn(Command.CommandCase.RUN_WF);

        Assertions.assertThatCode(() -> unboundMetrics.observe(command)).doesNotThrowAnyException();
    }

    @Test
    void shouldIgnoreCommandNotSet() {
        CommandModel command = Mockito.mock(CommandModel.class);
        Mockito.when(command.getType()).thenReturn(Command.CommandCase.COMMAND_NOT_SET);

        metrics.observe(command);

        // All counters should remain at zero
        for (Command.CommandCase commandType : Command.CommandCase.values()) {
            if (commandType != Command.CommandCase.COMMAND_NOT_SET) {
                assertThat(registry.get(METRIC_NAME)
                                .tag(COMMAND_TYPE_TAG, commandType.name())
                                .counter()
                                .count())
                        .isZero();
            }
        }
    }
}
