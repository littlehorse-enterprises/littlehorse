package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.adapter.LHIntegerAdapter;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@LHTest
public class WorkflowConfigTest {
    private LittleHorseBlockingStub client;
    private LHConfig config;

    @Test
    void sharesConfigurationAcrossConcurrentWorkflowClients() throws Exception {
        String name = "concurrent-config-" + UUID.randomUUID();
        Workflow.newWorkflow(name, thread -> thread.complete(), config).registerWfSpec(client);
        int callers = 8;
        var executor = Executors.newFixedThreadPool(callers);
        CountDownLatch ready = new CountDownLatch(callers);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<WfRunId>> results = new ArrayList<>();
            for (int i = 0; i < callers; i++) {
                boolean useFuture = i % 2 == 0;
                results.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Concurrent clients did not start");
                    }
                    RunWfRequest request =
                            RunWfRequest.newBuilder().setWfSpecName(name).build();
                    return useFuture
                            ? config.getFutureStub()
                                    .runWf(request)
                                    .get(5, TimeUnit.SECONDS)
                                    .getId()
                            : config.getBlockingStub()
                                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                                    .runWf(request)
                                    .getId();
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            List<WfRunId> ids = new ArrayList<>();
            for (Future<WfRunId> result : results) {
                ids.add(result.get(10, TimeUnit.SECONDS));
            }
            assertThat(ids).doesNotHaveDuplicates();
            Awaitility.await().untilAsserted(() -> {
                for (WfRunId id : ids) {
                    assertThat(client.getWfRun(id).getStatus()).isEqualTo(LHStatus.COMPLETED);
                }
            });
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void usesConfiguredAdapterWhenWorkflowCompilesBeforeRegistration() {
        UUID defaultValue = UUID.randomUUID();
        LHConfig config =
                LHConfig.newBuilder().addTypeAdapter(new UUIDAdapter()).build();
        Workflow workflow = Workflow.newWorkflow(
                "configured-adapter-" + UUID.randomUUID(),
                thread -> {
                    thread.addVariable("id", defaultValue);
                },
                config);

        // Dependency inspection can compile the workflow before it is registered.
        assertThat(workflow.getRequiredTaskDefNames()).isEmpty();
        WfSpec registered = client.putWfSpec(workflow.compileWorkflow());
        VariableDef id = registered
                .getThreadSpecsOrThrow("entrypoint")
                .getVariableDefs(0)
                .getVarDef();

        assertThat(id.getTypeDef().getPrimitiveType()).isEqualTo(VariableType.INT);
        assertThat(id.getDefaultValue().getInt()).isEqualTo(defaultValue.hashCode());
    }

    private static class UUIDAdapter extends LHIntegerAdapter<UUID> {
        @Override
        public Integer toInteger(UUID src) {
            return src.hashCode();
        }

        @Override
        public UUID fromInteger(Integer src) {
            return new UUID(0, src);
        }

        @Override
        public Class<UUID> getTypeClass() {
            return UUID.class;
        }
    }
}
