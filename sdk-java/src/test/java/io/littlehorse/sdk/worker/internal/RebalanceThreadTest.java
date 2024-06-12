package io.littlehorse.sdk.worker.internal;

import static org.mockito.Mockito.*;

import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class RebalanceThreadTest {

    private RebalanceThread rebalanceThread;
    private final LittleHorseGrpc.LittleHorseStub bootstrapStub = mock();
    private final LHLivenessController livenessController = mock();
    private final String taskWorkerId = "testId";
    private final String connectListenerName = "my-connection";
    private final TaskDefId taskDefId =
            TaskDefId.newBuilder().setName("my-task").build();
    private final TaskDef taskDef = TaskDef.newBuilder()
            .setId(taskDefId)
            .setCreatedAt(LHLibUtil.fromDate(new Date()))
            .build();
    private final List<VariableMapping> mappings = List.of();

    private Method workerMethod;
    private final Object executable = this;
    private final LHConfig config = mock();
    private final long heartbeatIntervalMs = 1L;

    @BeforeEach
    public void setup() throws Exception {
        workerMethod = this.getClass().getMethod("myTestWorkerMethod");
        rebalanceThread = new RebalanceThread(
                bootstrapStub,
                taskWorkerId,
                connectListenerName,
                taskDef,
                workerMethod,
                mappings,
                executable,
                config,
                livenessController,
                heartbeatIntervalMs);
    }

    @Test
    public void shouldCreatePollThreadsForEveryHost() {
        when(livenessController.keepWorkerRunning()).thenReturn(true, true, true, false);
        LHHostInfo serverA =
                LHHostInfo.newBuilder().setHost("abc").setPort(1234).build();
        LHHostInfo serverB =
                LHHostInfo.newBuilder().setHost("def").setPort(4567).build();
        LHHostInfo serverC =
                LHHostInfo.newBuilder().setHost("ghi").setPort(8901).build();
        RegisterTaskWorkerResponse.Builder response1 = RegisterTaskWorkerResponse.newBuilder()
                .addYourHosts(serverA)
                .addYourHosts(serverB)
                .addYourHosts(serverC);
        RegisterTaskWorkerResponse.Builder response2 = RegisterTaskWorkerResponse.newBuilder()
                .addYourHosts(serverA)
                .addYourHosts(serverB)
                .addYourHosts(serverC);
        doAnswer(invocation -> {
                    StreamObserver<RegisterTaskWorkerResponse> observer = invocation.getArgument(1);
                    observer.onNext(response1.build());
                    observer.onNext(response2.build());
                    return null;
                })
                .when(bootstrapStub)
                .registerTaskWorker(any(), any());

        rebalanceThread.start();

        Awaitility.await().ignoreExceptions().until(() -> !rebalanceThread.isAlive());
        Assertions.assertThat(rebalanceThread.getRunningConnections().keySet()).hasSize(3);
    }

    @Test
    public void shouldCreatePollThreadsForEveryHostAfterARebalance() {
        when(livenessController.keepWorkerRunning()).thenReturn(true, true, true, false);
        LHHostInfo serverA =
                LHHostInfo.newBuilder().setHost("abc").setPort(1234).build();
        LHHostInfo serverB =
                LHHostInfo.newBuilder().setHost("def").setPort(4567).build();
        LHHostInfo serverC =
                LHHostInfo.newBuilder().setHost("ghi").setPort(8901).build();
        RegisterTaskWorkerResponse.Builder response1 = RegisterTaskWorkerResponse.newBuilder()
                .addYourHosts(serverA)
                .addYourHosts(serverB)
                .addYourHosts(serverC);
        RegisterTaskWorkerResponse.Builder response2 = RegisterTaskWorkerResponse.newBuilder()
                .addYourHosts(serverA)
                .addYourHosts(serverB)
                .addYourHosts(serverC);
        RegisterTaskWorkerResponse.Builder response3 =
                RegisterTaskWorkerResponse.newBuilder().addYourHosts(serverA).addYourHosts(serverC);
        doAnswer(invocation -> {
                    StreamObserver<RegisterTaskWorkerResponse> observer = invocation.getArgument(1);
                    observer.onNext(response1.build());
                    observer.onNext(response2.build());
                    observer.onNext(response3.build());
                    return null;
                })
                .when(bootstrapStub)
                .registerTaskWorker(any(), any());

        rebalanceThread.start();

        Awaitility.await().ignoreExceptions().until(() -> !rebalanceThread.isAlive());
        Assertions.assertThat(rebalanceThread.getRunningConnections().keySet()).hasSize(2);
    }

    @Test
    public void shouldCreatePollThreadsForEveryHostAfterAClusterScaleUp() {
        when(livenessController.keepWorkerRunning()).thenReturn(true, true, true, false);
        LHHostInfo serverA =
                LHHostInfo.newBuilder().setHost("abc").setPort(1234).build();
        LHHostInfo serverB =
                LHHostInfo.newBuilder().setHost("def").setPort(4567).build();
        LHHostInfo serverC =
                LHHostInfo.newBuilder().setHost("ghi").setPort(8901).build();
        LHHostInfo serverD =
                LHHostInfo.newBuilder().setHost("ghi2").setPort(8902).build();
        RegisterTaskWorkerResponse.Builder response1 = RegisterTaskWorkerResponse.newBuilder()
                .addYourHosts(serverA)
                .addYourHosts(serverB)
                .addYourHosts(serverC);
        RegisterTaskWorkerResponse.Builder response2 = RegisterTaskWorkerResponse.newBuilder()
                .addYourHosts(serverA)
                .addYourHosts(serverB)
                .addYourHosts(serverC);
        RegisterTaskWorkerResponse.Builder response3 = RegisterTaskWorkerResponse.newBuilder()
                .addYourHosts(serverA)
                .addYourHosts(serverB)
                .addYourHosts(serverC)
                .addYourHosts(serverD);
        doAnswer(invocation -> {
                    StreamObserver<RegisterTaskWorkerResponse> observer = invocation.getArgument(1);
                    observer.onNext(response1.build());
                    observer.onNext(response2.build());
                    observer.onNext(response3.build());
                    return null;
                })
                .when(bootstrapStub)
                .registerTaskWorker(any(), any());

        rebalanceThread.start();

        Awaitility.await().ignoreExceptions().until(() -> !rebalanceThread.isAlive());
        Assertions.assertThat(rebalanceThread.getRunningConnections().keySet()).hasSize(4);
    }

    public void myTestWorkerMethod() {
        System.out.println("Hello from worker method");
    }
}
