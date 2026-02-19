package io.littlehorse.common.model.wfrun;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.MetricWindowType;
import io.littlehorse.server.TestCoreProcessorContext;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.Date;
import java.util.UUID;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.Test;

public class WfRunModelTest {

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();
    private final TenantIdModel tenantId = new TenantIdModel("test-tenant");
    private final Headers metadata = HeadersUtil.metadataHeadersFor(tenantId.getId(), "test-principal");
    private final TestCoreProcessorContext testContext =
            TestCoreProcessorContext.create(command(), metadata, mockProcessorContext);
    private final KeyValueStore<String, Bytes> coreStore =
            mockProcessorContext.getStateStore(ServerTopology.CORE_STORE);
    private final ClusterScopedStore clusterStore = ClusterScopedStore.newInstance(coreStore, testContext);

    @Test
    void getThreadRunReturnsNullForInvalidThreadRunNumber() {
        WfRunModel wfRunModel = new WfRunModel();
        ThreadRunModel thread = new ThreadRunModel();
        wfRunModel.getThreadRunsUseMeCarefully().add(thread);
        assertThat(wfRunModel.getThreadRun(0)).isSameAs(thread);
        assertThat(wfRunModel.getThreadRun(-1)).isNull();
        assertThat(wfRunModel.getThreadRun(1)).isNull();
    }

    @Test
    void shouldTrackMetricsWhenStateChange() {
        WfSpecIdModel wfSpecId = new WfSpecIdModel("metrics-test-workflow", 1, 0);
        int numberOfWorkflows = 10;
        for (int i = 0; i < numberOfWorkflows; i++) {
            WfRunModel wfRun = createWfRun("wf-run-" + i, wfSpecId, testContext);
            wfRun.transitionTo(LHStatus.RUNNING);
            if (i % 2 == 0) {
                wfRun.transitionTo(LHStatus.COMPLETED);
            } else {
                wfRun.transitionTo(LHStatus.ERROR);
            }
        }
        Date windowStart = LHUtil.getCurrentWindowTime();
        String metricKey = String.format(
                "%s/%s/%s/%s/%s",
                LHConstants.PARTITION_METRICS_KEY,
                LHUtil.toLhDbFormat(windowStart),
                MetricWindowType.WORKFLOW_METRIC.name(),
                this.tenantId,
                wfSpecId);

        PartitionMetricWindowModel storedMetrics = clusterStore.get(metricKey, PartitionMetricWindowModel.class);

        assertThat(storedMetrics).isNotNull();
        assertThat(storedMetrics.getMetrics()).isNotEmpty();
        assertThat(storedMetrics.getMetrics()).containsKeys("started", "running_to_completed", "running_to_error");

        long started = storedMetrics.getMetrics().get("started").getCount();
        long completed = storedMetrics.getMetrics().get("running_to_completed").getCount();
        long error = storedMetrics.getMetrics().get("running_to_error").getCount();

        assertThat(started).isEqualTo(10);
        assertThat(completed).isEqualTo(5);
        assertThat(error).isEqualTo(5);
    }

    private WfRunModel createWfRun(String wfRunId, WfSpecIdModel wfSpecId, TestCoreProcessorContext context) {
        WfRunModel wfRun = new WfRunModel(context);
        wfRun.setId(new WfRunIdModel(wfRunId));
        wfRun.setWfSpecId(wfSpecId);
        wfRun.setWfSpec(TestUtil.wfSpec(wfSpecId.getName()));
        wfRun.setStartTime(context.currentCommand().getTime());
        return wfRun;
    }

    private static Command command() {
        StopWfRunRequestModel dummyCommand = new StopWfRunRequestModel();
        dummyCommand.wfRunId = new WfRunIdModel(UUID.randomUUID().toString());
        dummyCommand.threadRunNumber = 0;
        return new CommandModel(dummyCommand).toProto().build();
    }
}
