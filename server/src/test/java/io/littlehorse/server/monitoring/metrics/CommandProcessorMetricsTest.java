package io.littlehorse.server.monitoring.metrics;

import static io.littlehorse.server.monitoring.metrics.CommandProcessorMetrics.COMMAND_TYPE_TAG;
import static io.littlehorse.server.monitoring.metrics.CommandProcessorMetrics.METRIC_NAME;
import static io.littlehorse.server.monitoring.metrics.CommandProcessorMetrics.METRIC_NAME_BY_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.MetadataCommand;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
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
                Counter counter = registry.get(METRIC_NAME_BY_TYPE)
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

        Counter counter = registry.get(METRIC_NAME_BY_TYPE)
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

        Counter counter = registry.get(METRIC_NAME_BY_TYPE)
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

        assertThat(registry.get(METRIC_NAME_BY_TYPE)
                        .tag(COMMAND_TYPE_TAG, Command.CommandCase.RUN_WF.name())
                        .counter()
                        .count())
                .isEqualTo(2.0);

        assertThat(registry.get(METRIC_NAME_BY_TYPE)
                        .tag(COMMAND_TYPE_TAG, Command.CommandCase.TASK_CLAIM_EVENT.name())
                        .counter()
                        .count())
                .isEqualTo(1.0);
    }

    @Test
    void shouldIncrementGeneralMetricOnEveryObserve() {
        CommandModel runWf = Mockito.mock(CommandModel.class);
        Mockito.when(runWf.getType()).thenReturn(Command.CommandCase.RUN_WF);

        CommandModel taskClaim = Mockito.mock(CommandModel.class);
        Mockito.when(taskClaim.getType()).thenReturn(Command.CommandCase.TASK_CLAIM_EVENT);

        metrics.observe(runWf);
        metrics.observe(taskClaim);
        metrics.observe(runWf);

        Counter general = registry.get(METRIC_NAME).tag("type", "core").counter();
        assertThat(general.count()).isEqualTo(3.0);
    }

    @Test
    void shouldIncrementBothGeneralAndTagSpecificMetrics() {
        CommandModel command = Mockito.mock(CommandModel.class);
        Mockito.when(command.getType()).thenReturn(Command.CommandCase.RUN_WF);

        metrics.observe(command);

        Counter general = registry.get(METRIC_NAME).tag("type", "core").counter();
        Counter tagSpecific = registry.get(METRIC_NAME_BY_TYPE)
                .tag(COMMAND_TYPE_TAG, Command.CommandCase.RUN_WF.name())
                .counter();

        assertThat(general.count()).isEqualTo(1.0);
        assertThat(tagSpecific.count()).isEqualTo(1.0);
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
        registry.getMeters();

        // All counters should remain at zero
        for (Command.CommandCase commandType : Command.CommandCase.values()) {
            if (commandType != Command.CommandCase.COMMAND_NOT_SET) {
                assertThat(registry.get(METRIC_NAME_BY_TYPE)
                                .tag(COMMAND_TYPE_TAG, commandType.name())
                                .counter()
                                .count())
                        .isZero();
            }
        }
    }

    @Test
    void shouldScrapeOnlyCommandsProcessedWhenInfoLevel() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        new ServerMetricFilter(registry, ServerFilterRules.fromLevel("INFO")).initialize();
        CommandProcessorMetrics metrics = new CommandProcessorMetrics();
        metrics.bindTo(registry);

        CommandModel runWfCommand = Mockito.mock(CommandModel.class);
        Mockito.when(runWfCommand.getType()).thenReturn(Command.CommandCase.RUN_WF);

        metrics.observe(runWfCommand);
        metrics.observe(runWfCommand);
        metrics.observe(runWfCommand);

        assertThat(registry.scrape()).contains("lh_commands_processed_total{type=\"core\"} 3.0");
        assertThat(registry.scrape()).contains("lh_commands_processed_total{type=\"metadata\"} 0.0");
        assertThat(registry.scrape()).doesNotContain("lh_subcommands_processed_total");
    }

    @Test
    void shouldScrapeCommandsAndSubcommandsWhenDebugLevel() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        new ServerMetricFilter(registry, ServerFilterRules.fromLevel("DEBUG")).initialize();
        CommandProcessorMetrics metrics = new CommandProcessorMetrics();
        metrics.bindTo(registry);

        CommandModel runWfCommand = Mockito.mock(CommandModel.class);
        Mockito.when(runWfCommand.getType()).thenReturn(Command.CommandCase.RUN_WF);

        CommandModel taskClaimCommand = Mockito.mock(CommandModel.class);
        Mockito.when(taskClaimCommand.getType()).thenReturn(Command.CommandCase.TASK_CLAIM_EVENT);

        metrics.observe(runWfCommand);
        metrics.observe(runWfCommand);
        metrics.observe(runWfCommand);
        metrics.observe(taskClaimCommand);

        assertThat(registry.scrape()).contains("lh_commands_processed_total{type=\"core\"} 4.0");
        assertThat(registry.scrape()).contains("lh_commands_processed_total{type=\"metadata\"} 0.0");
        assertThat(registry.scrape()).contains("lh_subcommands_processed_total{type=\"RUN_WF\"} 3.0");
        assertThat(registry.scrape()).contains("lh_subcommands_processed_total{type=\"TASK_CLAIM_EVENT\"} 1.0");
    }

    @Test
    void shouldRegisterCountersForAllMetadataCommandTypes() {
        for (MetadataCommand.MetadataCommandCase commandType : MetadataCommand.MetadataCommandCase.values()) {
            if (commandType != MetadataCommand.MetadataCommandCase.METADATACOMMAND_NOT_SET) {
                Counter counter = registry.get(METRIC_NAME_BY_TYPE)
                        .tag(COMMAND_TYPE_TAG, commandType.name())
                        .counter();
                assertThat(counter).isNotNull();
                assertThat(counter.count()).isZero();
            }
        }
    }

    @Test
    void shouldIncrementMetadataCounterOnObserve() {
        MetadataCommandModel command = Mockito.mock(MetadataCommandModel.class);
        Mockito.when(command.getType()).thenReturn(MetadataCommand.MetadataCommandCase.PUT_WF_SPEC);

        metrics.observe(command);

        Counter counter = registry.get(METRIC_NAME_BY_TYPE)
                .tag(COMMAND_TYPE_TAG, MetadataCommand.MetadataCommandCase.PUT_WF_SPEC.name())
                .counter();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldIncrementMetadataGeneralMetricOnEveryObserve() {
        MetadataCommandModel putWfSpec = Mockito.mock(MetadataCommandModel.class);
        Mockito.when(putWfSpec.getType()).thenReturn(MetadataCommand.MetadataCommandCase.PUT_WF_SPEC);

        MetadataCommandModel putTaskDef = Mockito.mock(MetadataCommandModel.class);
        Mockito.when(putTaskDef.getType()).thenReturn(MetadataCommand.MetadataCommandCase.PUT_TASK_DEF);

        metrics.observe(putWfSpec);
        metrics.observe(putTaskDef);
        metrics.observe(putWfSpec);

        Counter general = registry.get(METRIC_NAME).tag("type", "metadata").counter();
        assertThat(general.count()).isEqualTo(3.0);
    }

    @Test
    void shouldTrackMetadataCommandTypesIndependently() {
        MetadataCommandModel putWfSpec = Mockito.mock(MetadataCommandModel.class);
        Mockito.when(putWfSpec.getType()).thenReturn(MetadataCommand.MetadataCommandCase.PUT_WF_SPEC);

        MetadataCommandModel putTaskDef = Mockito.mock(MetadataCommandModel.class);
        Mockito.when(putTaskDef.getType()).thenReturn(MetadataCommand.MetadataCommandCase.PUT_TASK_DEF);

        metrics.observe(putWfSpec);
        metrics.observe(putWfSpec);
        metrics.observe(putTaskDef);

        assertThat(registry.get(METRIC_NAME_BY_TYPE)
                        .tag(COMMAND_TYPE_TAG, MetadataCommand.MetadataCommandCase.PUT_WF_SPEC.name())
                        .counter()
                        .count())
                .isEqualTo(2.0);

        assertThat(registry.get(METRIC_NAME_BY_TYPE)
                        .tag(COMMAND_TYPE_TAG, MetadataCommand.MetadataCommandCase.PUT_TASK_DEF.name())
                        .counter()
                        .count())
                .isEqualTo(1.0);
    }

    @Test
    void shouldIgnoreMetadataCommandNotSet() {
        MetadataCommandModel command = Mockito.mock(MetadataCommandModel.class);
        Mockito.when(command.getType()).thenReturn(MetadataCommand.MetadataCommandCase.METADATACOMMAND_NOT_SET);

        metrics.observe(command);

        Counter general = registry.get(METRIC_NAME).tag("type", "metadata").counter();
        assertThat(general.count()).isZero();
    }

    @Test
    void shouldTrackCoreAndMetadataCommandsIndependently() {
        CommandModel coreCommand = Mockito.mock(CommandModel.class);
        Mockito.when(coreCommand.getType()).thenReturn(Command.CommandCase.RUN_WF);

        MetadataCommandModel metadataCommand = Mockito.mock(MetadataCommandModel.class);
        Mockito.when(metadataCommand.getType()).thenReturn(MetadataCommand.MetadataCommandCase.PUT_WF_SPEC);

        metrics.observe(coreCommand);
        metrics.observe(coreCommand);
        metrics.observe(metadataCommand);

        Counter coreGeneral = registry.get(METRIC_NAME).tag("type", "core").counter();
        Counter metadataGeneral =
                registry.get(METRIC_NAME).tag("type", "metadata").counter();

        assertThat(coreGeneral.count()).isEqualTo(2.0);
        assertThat(metadataGeneral.count()).isEqualTo(1.0);
    }
}
