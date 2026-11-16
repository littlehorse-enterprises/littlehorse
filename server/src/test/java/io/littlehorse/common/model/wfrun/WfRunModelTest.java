package io.littlehorse.common.model.wfrun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.model.getable.core.wfrun.InlineWfSpecModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.InlineWfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.InlineWfSpec;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.MetricWindowType;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.server.TestCoreProcessorContext;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.Test;

public class WfRunModelTest {

    @Test
    void resolvesSeparateDefinitionWithoutEmbeddingItInSerializedRun() {
        WfRunId owner = WfRunId.newBuilder()
                .setId("separate-inline-run")
                .setParentWfRunId(WfRunId.newBuilder().setId("parent-run"))
                .build();
        InlineWfSpec snapshot = InlineWfSpec.newBuilder()
                .setId(owner)
                .setCreatedAt(LHUtil.fromDate(new Date()))
                .setEntrypointThreadName("main")
                .build();
        InlineWfSpecModel definition = InlineWfSpecModel.fromProto(snapshot, InlineWfSpecModel.class, testContext);
        testContext.getableManager().put(definition);
        WfRun proto = WfRun.newBuilder()
                .setId(owner)
                .setIsInline(true)
                .setStatus(LHStatus.RUNNING)
                .build();
        WfRunModel run = WfRunModel.fromProto(proto, WfRunModel.class, testContext);
        assertThat(run.isInline()).isTrue();
        assertThat(run.getWfSpec().getEntrypointThreadName()).isEqualTo("main");
        assertThat(run.toProto().getWfSpecSourceCase()).isEqualTo(WfRun.WfSpecSourceCase.IS_INLINE);
        assertThat(run.toProto().getIsInline()).isTrue();
        assertThat(definition.toProto().build()).isEqualTo(snapshot);
        InlineWfSpecIdModel parsedId = new InlineWfSpecIdModel();
        parsedId.initFromString(definition.getId().toString());
        assertThat(parsedId.toProto().build()).isEqualTo(owner);
        assertThat(parsedId.getPartitionKey()).isEqualTo(run.getId().getPartitionKey());
        assertThat(parsedId.getGroupingWfRunId()).contains(run.getId());
        assertThat(parsedId.getStoreableKey()).isNotEqualTo(run.getId().getStoreableKey());
    }

    @Test
    void rejectsFalseInlineSourceFlag() {
        WfRun proto = WfRun.newBuilder()
                .setId(WfRunId.newBuilder().setId("invalid-inline-run"))
                .setIsInline(false)
                .build();

        assertThatThrownBy(() -> WfRunModel.fromProto(proto, WfRunModel.class, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inline workflow source flag must be true");
    }

    @Test
    void preservesRegisteredWorkflowReferenceAcrossSerialization() {
        WfSpecId specId = WfSpecId.newBuilder().setName("registered").build();
        WfRun proto = WfRun.newBuilder()
                .setId(WfRunId.newBuilder().setId("registered-run"))
                .setWfSpecId(specId)
                .setStatus(LHStatus.RUNNING)
                .build();

        WfRunModel model = WfRunModel.fromProto(proto, WfRunModel.class, null);
        WfRun serialized = model.toProto().build();

        assertThat(model.getInlineWfSpec()).isNull();
        assertThat(model.isInline()).isFalse();
        assertThat(serialized.getWfSpecSourceCase()).isEqualTo(WfRun.WfSpecSourceCase.WF_SPEC_ID);
        assertThat(serialized.getWfSpecId()).isEqualTo(specId);
    }

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
        thread.setNumber(0);
        wfRunModel.setThreadRunsUseMeCarefully(new ArrayList<>(List.of(thread)));
        wfRunModel.setGreatestThreadRunNumber(1);

        assertThat(wfRunModel.getThreadRun(0)).isSameAs(thread);
        assertThat(wfRunModel.getThreadRun(-1)).isNull();
    }

    @Test
    void shouldTrackMetricsinStoreWhenStateChange() {
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
        Date windowStart = LHUtil.getCurrentWindowDate();
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

    @Test
    void shouldTrackMetricsinMemoryWhenStateChange() {
        WfSpecIdModel wfSpecId = new WfSpecIdModel("metrics-test-workflow", 1, 0);
        int numberOfWorkflows = 20;
        for (int i = 0; i < numberOfWorkflows; i++) {
            WfRunModel wfRun = createWfRun("wf-run-" + i, wfSpecId, testContext);
            wfRun.transitionTo(LHStatus.RUNNING);
            if (i % 2 == 0) {
                wfRun.transitionTo(LHStatus.COMPLETED);
            } else {
                wfRun.transitionTo(LHStatus.ERROR);
            }
        }
        Date windowStart = LHUtil.getCurrentWindowDate();
        String metricKey = String.format(
                "%s/%s/%s/%s/%s",
                LHConstants.PARTITION_METRICS_KEY,
                LHUtil.toLhDbFormat(windowStart),
                MetricWindowType.WORKFLOW_METRIC.name(),
                this.tenantId,
                wfSpecId);

        PartitionMetricWindowModel storedMetrics =
                testContext.getMetricWindows().get(metricKey);

        assertThat(storedMetrics).isNotNull();
        assertThat(storedMetrics.getMetrics()).isNotEmpty();
        assertThat(storedMetrics.getMetrics()).containsKeys("started", "running_to_completed", "running_to_error");

        long started = storedMetrics.getMetrics().get("started").getCount();
        long completed = storedMetrics.getMetrics().get("running_to_completed").getCount();
        long error = storedMetrics.getMetrics().get("running_to_error").getCount();

        assertThat(started).isEqualTo(20);
        assertThat(completed).isEqualTo(10);
        assertThat(error).isEqualTo(10);
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
