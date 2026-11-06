package io.littlehorse.server.streams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.grpc.Context;
import io.grpc.InsecureChannelCredentials;
import io.grpc.Status;
import io.grpc.testing.StreamRecorder;
import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.InternalScanResponse;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.VariableMatch;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.server.LHServerListener;
import io.littlehorse.server.TestRequestExecutionContext;
import io.littlehorse.server.auth.AuthorizationProtocol;
import io.littlehorse.server.listener.ListenerProtocol;
import io.littlehorse.server.listener.ServerListenerConfig;
import io.littlehorse.server.streams.lhinternalscan.InternalScan;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchWfRunRequestModel;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.TaskMetadata;
import org.apache.kafka.streams.ThreadMetadata;
import org.apache.kafka.streams.processor.TaskId;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SearchWfRunIndexTest {
    private final TestRequestExecutionContext ctx = TestRequestExecutionContext.create();
    private final TenantScopedStore metadata = TenantScopedStore.newInstance(
            ctx.getGlobalMetadataNativeStore(), new TenantIdModel(ctx.getTenantId()), ctx);
    private final TenantScopedStore core =
            TenantScopedStore.newInstance(ctx.getCoreNativeStore(), new TenantIdModel(ctx.getTenantId()), ctx);
    private final Context.Key<RequestExecutionContext> contextKey = Context.key("search-test");
    private Context previous;
    private BackendInternalComms backend;
    private WfSpecModel spec;

    @BeforeEach
    void setUp() {
        spec = putSpec(0, true);
        LHServerConfig config = mock();
        when(config.getInternalClientCreds()).thenReturn(InsecureChannelCredentials.create());
        when(config.getInternalAdvertisedHost()).thenReturn("localhost");
        when(config.getClusterPartitions()).thenReturn(1);
        when(config.getCoreCmdTopicName()).thenReturn("commands");
        KafkaStreams streams = mock();
        ThreadMetadata thread = mock();
        TaskMetadata task = mock();
        when(task.taskId()).thenReturn(new TaskId(0, 0));
        when(task.topicPartitions()).thenReturn(Set.of(new TopicPartition("commands", 0)));
        when(thread.activeTasks()).thenReturn(Set.of(task));
        when(streams.metadataForLocalThreads()).thenReturn(Set.of(thread));
        when(streams.store(any())).thenReturn(ctx.getCoreNativeStore());
        backend = new BackendInternalComms(config, streams, null, ctx.getMetadataCache(), contextKey, null, null);
        previous = Context.current().withValue(contextKey, ctx).attach();
    }

    @AfterEach
    void tearDown() {
        Context.current().detach(previous);
        backend.close();
        ctx.getCoreNativeStore().close();
        ctx.getGlobalMetadataNativeStore().close();
    }

    @Test
    void shouldUseVariableIndexAndPaginateAllRunningReruns() throws Exception {
        putRun("completed", "needle", LHStatus.COMPLETED);
        putRun("first", "needle", LHStatus.RUNNING);
        putRun("second", "needle", LHStatus.RUNNING);
        putRun("unrelated", "other", LHStatus.RUNNING);
        SearchWfRunRequest.Builder request =
                request("needle").setStatus(LHStatus.RUNNING).setLimit(1);
        InternalScan scan = plan(request.build());
        assertThat(scan.tagScan.getKeyPrefix()).startsWith(GetableClassEnum.VARIABLE.getNumber() + "/");
        assertThat(scan.objectType).isEqualTo(GetableClassEnum.WF_RUN);
        assertThat(searchAll(request)).containsExactlyInAnyOrder(id("first"), id("second"));
        assertThat(searchAll(request("absent").setStatus(LHStatus.RUNNING))).isEmpty();
    }

    @Test
    void shouldFallBackWhenAnOlderVersionIsNotIndexed() throws Exception {
        spec = putSpec(0, false);
        putRun("unindexed", "needle", LHStatus.RUNNING);
        spec = putSpec(1, true);
        putRun("indexed", "needle", LHStatus.RUNNING);
        assertThat(plan(request("needle").build()).tagScan.getKeyPrefix())
                .startsWith(GetableClassEnum.WF_RUN.getNumber() + "/");
        assertThat(searchAll(request("needle"))).containsExactlyInAnyOrder(id("indexed"), id("unindexed"));
        assertThat(plan(request("needle")
                                .setWfSpecMajorVersion(0)
                                .setWfSpecRevision(1)
                                .build())
                        .tagScan
                        .getKeyPrefix())
                .startsWith(GetableClassEnum.WF_RUN.getNumber() + "/");
        assertThat(searchAll(request("needle").setWfSpecMajorVersion(0).setWfSpecRevision(1)))
                .containsExactly(id("indexed"));
    }

    @Test
    void shouldPreserveUnfilteredListings() throws Exception {
        putRun("first", "needle", LHStatus.RUNNING);
        putRun("second", "other", LHStatus.COMPLETED);
        assertThat(searchAll(SearchWfRunRequest.newBuilder().setWfSpecName("indexed-search")))
                .containsExactlyInAnyOrder(id("first"), id("second"));
    }

    @Test
    void shouldFindUnindexedRunsAfterTheirWorkflowDefinitionIsDeleted() throws Exception {
        spec = putSpec(0, false);
        putRun("old", "needle", LHStatus.COMPLETED);
        spec = putSpec(1, true);
        putRun("new", "needle", LHStatus.COMPLETED);
        metadata.delete(new WfSpecIdModel("indexed-search", 0, 0).getStoreableKey(), StoreableType.STORED_GETABLE);
        assertThat(searchAll(request("needle").setStatus(LHStatus.COMPLETED).setLimit(1)))
                .containsExactlyInAnyOrder(id("old"), id("new"));
    }

    @Test
    void shouldPreserveMaskedVariableComparisons() throws Exception {
        spec.getThreadSpecs()
                .get(spec.getEntrypointThreadName())
                .getVarDef("key")
                .getVarDef()
                .setTypeDef(new TypeDefinitionModel(VariableType.STR, true));
        metadata.put(new StoredGetable<>(spec));
        putRun("masked", "secret", LHStatus.RUNNING);
        VariableModel variable =
                new VariableModel("key", new VariableValueModel("secret"), new WfRunIdModel("masked"), 0, spec, true);
        core.put(new StoredGetable<>(variable));
        assertThat(searchAll(request("*****"))).containsExactly(id("masked"));
        assertThat(searchAll(request("secret"))).isEmpty();
    }

    @Test
    void shouldSkipChildThreadsAndPreserveFullChildWorkflowIds() throws Exception {
        WfRunIdModel child = new WfRunIdModel("child", new WfRunIdModel("parent"));
        putRun(child, "needle", LHStatus.RUNNING);
        putRun("wrong-thread", "other", LHStatus.RUNNING);
        VariableModel variable = new VariableModel(
                "key", new VariableValueModel("needle"), new WfRunIdModel("wrong-thread"), 1, spec, false);
        core.put(new StoredGetable<>(variable));
        variable.getIndexEntries().forEach(core::put);
        assertThat(searchAll(request("needle").setLimit(1)))
                .containsExactly(child.toProto().build());
    }

    @Test
    void shouldApplyUnindexedPredicatesAfterTheIndexedLookup() throws Exception {
        putRun("first", "needle", LHStatus.RUNNING);
        putRun("second", "needle", LHStatus.RUNNING);
        for (String run : List.of("first", "second")) {
            VariableModel other =
                    new VariableModel("other", new VariableValueModel(run), new WfRunIdModel(run), 0, spec, false);
            core.put(new StoredGetable<>(other));
        }
        assertThat(searchAll(request("needle")
                        .setLimit(1)
                        .addVariableFilters(VariableMatch.newBuilder()
                                .setVarName("other")
                                .setValue(VariableValue.newBuilder().setStr("second")))))
                .containsExactly(id("second"));
    }

    @Test
    void shouldUseExistingStringEncodingAndKeepTenantsIsolated() throws Exception {
        String value = "item_with/slashes% and spaces";
        putRun("local", value, LHStatus.RUNNING);
        TenantScopedStore otherTenant =
                TenantScopedStore.newInstance(ctx.getCoreNativeStore(), new TenantIdModel("other-tenant"), ctx);
        VariableModel variable =
                new VariableModel("key", new VariableValueModel(value), new WfRunIdModel("foreign"), 0, spec, false);
        otherTenant.put(new StoredGetable<>(variable));
        variable.getIndexEntries().forEach(otherTenant::put);
        assertThat(searchAll(request(value))).containsExactly(id("local"));
    }

    @Test
    void shouldKeepTimeAndParentFiltersOnTheExistingPath() throws Exception {
        putRun("first", "needle", LHStatus.RUNNING);
        var earliest = LHUtil.fromDate(new Date(System.currentTimeMillis() - 60_000));
        assertThat(plan(request("needle").setEarliestStart(earliest).build())
                        .tagScan
                        .getKeyPrefix())
                .startsWith(GetableClassEnum.WF_RUN.getNumber() + "/");
        assertThat(searchAll(request("needle").setEarliestStart(earliest))).containsExactly(id("first"));
        assertThat(plan(request("needle").setParentWfRunId(id("parent")).build())
                        .tagScan
                        .getKeyPrefix())
                .startsWith(GetableClassEnum.WF_RUN.getNumber() + "/");
    }

    @Test
    void shouldRecordOneStrategyHistogramObservationPerPublicPage() throws Exception {
        putRun("first", "needle", LHStatus.RUNNING);
        putRun("second", "needle", LHStatus.RUNNING);
        var config = ServerListenerConfig.builder()
                .name("test")
                .port(0)
                .protocol(ListenerProtocol.PLAIN)
                .authorizationProtocol(AuthorizationProtocol.NONE)
                .config(ctx.getLhConfig())
                .build();
        var registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        BackendInternalComms observedBackend = spy(backend);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
                var listener = new LHServerListener(
                        config,
                        null,
                        observedBackend,
                        executor,
                        null,
                        null,
                        List.of(),
                        contextKey,
                        null,
                        null,
                        null,
                        null,
                        registry)) {
            var request = request("needle").setLimit(1);
            StreamRecorder<WfRunIdList> first = StreamRecorder.create();
            listener.searchWfRun(request.build(), first);
            assertThat(first.getError()).isNull();
            assertThat(first.getValues().getFirst().hasBookmark()).isTrue();
            StreamRecorder<WfRunIdList> second = StreamRecorder.create();
            listener.searchWfRun(
                    request.setBookmark(first.getValues().getFirst().getBookmark())
                            .build(),
                    second);
            assertThat(second.getError()).isNull();
            assertThat(registry.get("lh.search.duration")
                            .tags("strategy", "variable_index", "outcome", "success")
                            .timer()
                            .count())
                    .isEqualTo(2);

            StreamRecorder<WfRunIdList> listing = StreamRecorder.create();
            listener.searchWfRun(
                    SearchWfRunRequest.newBuilder()
                            .setWfSpecName("indexed-search")
                            .build(),
                    listing);
            assertThat(listing.getError()).isNull();
            assertThat(registry.get("lh.search.duration")
                            .tags("strategy", "wfrun_index", "outcome", "success")
                            .timer()
                            .count())
                    .isEqualTo(1);

            doThrow(Status.UNAVAILABLE.asRuntimeException())
                    .when(observedBackend)
                    .doScan(any());
            StreamRecorder<WfRunIdList> failed = StreamRecorder.create();
            listener.searchWfRun(request("needle").build(), failed);
            assertThat(Status.fromThrowable(failed.getError()).getCode()).isEqualTo(Status.Code.UNAVAILABLE);
            assertThat(registry.get("lh.search.duration")
                            .tags("strategy", "variable_index", "outcome", "error")
                            .timer()
                            .count())
                    .isEqualTo(1);

            StreamRecorder<WfRunIdList> invalid = StreamRecorder.create();
            listener.searchWfRun(request("needle").clearWfSpecName().build(), invalid);
            assertThat(Status.fromThrowable(invalid.getError()).getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
            assertThat(registry.get("lh.search.duration")
                            .tags("strategy", "unselected", "outcome", "error")
                            .timer()
                            .count())
                    .isEqualTo(1);
            assertThat(registry.scrape()).contains("lh_search_duration_seconds_bucket");
        } finally {
            registry.close();
        }
    }

    @Test
    void shouldAvoidReadingUnrelatedVariables() throws Exception {
        for (int i = 0; i < 1_000; i++) putRun("unrelated-" + i, "other", LHStatus.RUNNING);
        putRun("first", "needle", LHStatus.RUNNING);
        putRun("second", "needle", LHStatus.RUNNING);
        AtomicInteger reads = new AtomicInteger();
        ReadOnlyKeyValueStore<String, Bytes> measuredStore = mock();
        when(measuredStore.get(any())).thenAnswer(invocation -> {
            reads.incrementAndGet();
            return ctx.getCoreNativeStore().get(invocation.getArgument(0));
        });
        CoreStoreProvider provider = mock();
        when(provider.nativeCoreStore()).thenReturn(measuredStore);
        when(provider.getNativeGlobalStore()).thenReturn(ctx.getGlobalMetadataNativeStore());
        var baseline = request("needle")
                .setStatus(LHStatus.RUNNING)
                .setEarliestStart(LHUtil.fromDate(new Date(System.currentTimeMillis() - 60_000)))
                .build();
        var optimized = baseline.toBuilder().clearEarliestStart().build();
        for (var request : List.of(baseline, optimized)) {
            long[] elapsed = new long[20];
            for (int iteration = 0; iteration < 25; iteration++) {
                RequestExecutionContext fresh = new RequestExecutionContext(
                        new PrincipalIdModel(ctx.getClientId()),
                        new TenantIdModel(ctx.getTenantId()),
                        provider,
                        ctx.getMetadataCache(),
                        ctx.getLhConfig(),
                        false);
                reads.set(0);
                long started = System.nanoTime();
                InternalScanResponse result = Context.current()
                        .withValue(contextKey, fresh)
                        .call(() -> backend.doScan(SearchWfRunRequestModel.fromProto(request, fresh)
                                .getInternalSearch(fresh)));
                long duration = System.nanoTime() - started;
                assertThat(result.getResultsList())
                        .containsExactlyInAnyOrder(
                                id("first").toByteString(), id("second").toByteString());
                assertThat(result.hasUpdatedBookmark()).isFalse();
                if (request.hasEarliestStart()) assertThat(reads.get()).isGreaterThanOrEqualTo(1_002);
                else assertThat(reads.get()).isLessThanOrEqualTo(4);
                if (iteration >= 5) elapsed[iteration - 5] = duration;
            }
            Arrays.sort(elapsed);
            System.out.printf(
                    "SearchWfRun in-memory, 1002 runs, %s: p50=%.3f ms p95=%.3f ms, core reads=%d%n",
                    request.hasEarliestStart() ? "wfrun_index" : "variable_index",
                    elapsed[9] / 1_000_000.0,
                    elapsed[18] / 1_000_000.0,
                    reads.get());
        }
    }

    private WfSpecModel putSpec(int revision, boolean searchable) {
        var compiled = Workflow.newWorkflow("indexed-search", thread -> {
                    thread.declareStr("key").searchable();
                    thread.declareStr("other");
                })
                .compileWorkflow();
        WfSpecModel model = io.littlehorse.common.LHSerializable.fromProto(
                WfSpec.newBuilder()
                        .setId(new WfSpecIdModel("indexed-search", 0, revision).toProto())
                        .setEntrypointThreadName(compiled.getEntrypointThreadName())
                        .putAllThreadSpecs(compiled.getThreadSpecsMap())
                        .build(),
                WfSpecModel.class,
                ctx);
        for (ThreadVarDefModel variable :
                model.getThreadSpecs().get(model.getEntrypointThreadName()).getVariableDefs()) {
            if (variable.getVarDef().getName().equals("key")) variable.setSearchable(searchable);
        }
        metadata.put(new StoredGetable<>(model));
        return model;
    }

    private void putRun(String runId, String value, LHStatus status) {
        putRun(new WfRunIdModel(runId), value, status);
    }

    private void putRun(WfRunIdModel runId, String value, LHStatus status) {
        var run = TestUtil.wfRun(runId.getId());
        run.setId(runId);
        run.setWfSpecId(spec.getId());
        run.status = status;
        core.put(new StoredGetable<>(run));
        run.getIndexEntries().forEach(core::put);
        VariableModel variable = new VariableModel("key", new VariableValueModel(value), run.getId(), 0, spec, false);
        core.put(new StoredGetable<>(variable));
        variable.getIndexEntries().forEach(core::put);
    }

    private SearchWfRunRequest.Builder request(String value) {
        return SearchWfRunRequest.newBuilder()
                .setWfSpecName("indexed-search")
                .addVariableFilters(VariableMatch.newBuilder()
                        .setVarName("key")
                        .setValue(VariableValue.newBuilder().setStr(value)));
    }

    private InternalScan plan(SearchWfRunRequest request) {
        return SearchWfRunRequestModel.fromProto(request, ctx).getInternalSearch(ctx);
    }

    private List<WfRunId> searchAll(SearchWfRunRequest.Builder request) throws Exception {
        List<WfRunId> result = new ArrayList<>();
        for (int page = 0; page < 20; page++) {
            InternalScanResponse response = backend.doScan(plan(request.build()));
            for (var bytes : response.getResultsList()) result.add(WfRunId.parseFrom(bytes));
            if (!response.hasUpdatedBookmark()) return result;
            request.setBookmark(response.getUpdatedBookmark().toByteString());
        }
        throw new AssertionError("Search did not finish within 20 pages");
    }

    private WfRunId id(String value) {
        return new WfRunIdModel(value).toProto().build();
    }
}
