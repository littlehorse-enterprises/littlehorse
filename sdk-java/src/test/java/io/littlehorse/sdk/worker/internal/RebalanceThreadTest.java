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
import org.mockito.Answers;
import org.mockito.stubbing.Stubber;

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
    private final PollThreadFactory pollThreadFactory = mock(Answers.RETURNS_DEEP_STUBS);

    @BeforeEach
    public void setup() throws Exception {
        workerMethod = this.getClass().getMethod("myTestWorkerMethod");
        when(config.getWorkerThreads()).thenReturn(1);
        rebalanceThread = new RebalanceThread(
                bootstrapStub,
                taskWorkerId,
                connectListenerName,
                taskDef,
                config,
                livenessController,
                heartbeatIntervalMs,
                pollThreadFactory);
    }

    @Test
    public void shouldCreatePollThreadsForEveryHost() {
        when(livenessController.keepWorkerRunning()).thenReturn(true, true, true, false);
        LHHostInfo serverA = LHHostInfo.newBuilder().setHost("a").setPort(1234).build();
        LHHostInfo serverB = LHHostInfo.newBuilder().setHost("b").setPort(4567).build();
        LHHostInfo serverC = LHHostInfo.newBuilder().setHost("c").setPort(8901).build();
        RegisterTaskWorkerResponse.Builder response1 = RegisterTaskWorkerResponse.newBuilder()
                .addYourHosts(serverA)
                .addYourHosts(serverB)
                .addYourHosts(serverC);
        RegisterTaskWorkerResponse.Builder response2 = RegisterTaskWorkerResponse.newBuilder()
                .addYourHosts(serverA)
                .addYourHosts(serverB)
                .addYourHosts(serverC);
        registerFakeResponses(response1.build(), response2.build())
                .when(bootstrapStub)
                .registerTaskWorker(any(), any());

        rebalanceThread.start();

        Awaitility.await().ignoreExceptions().until(() -> !rebalanceThread.isAlive());
        Assertions.assertThat(rebalanceThread.runningConnections.keySet()).hasSize(3);
    }

    @Test
    public void shouldCreatePollThreadsForEveryHostAfterARebalanceAndClosePreviousPollThreads() {
        when(livenessController.keepWorkerRunning()).thenReturn(true, true, true, false);
        LHHostInfo serverA = LHHostInfo.newBuilder().setHost("a").setPort(1234).build();
        LHHostInfo serverB = LHHostInfo.newBuilder().setHost("b").setPort(4567).build();
        PollThread pollThread1 = mock("pollThread1");
        PollThread pollThread2 = mock("pollThread2");
        when(pollThread1.isRunning()).thenReturn(true);
        when(pollThreadFactory.create(any(), any())).thenReturn(pollThread1, pollThread2);
        when(config.getWorkerThreads()).thenReturn(1);
        RegisterTaskWorkerResponse.Builder response1 =
                RegisterTaskWorkerResponse.newBuilder().addYourHosts(serverA).addYourHosts(serverB);
        RegisterTaskWorkerResponse.Builder response2 =
                RegisterTaskWorkerResponse.newBuilder().addYourHosts(serverA);
        registerFakeResponses(response1.build(), response2.build())
                .when(bootstrapStub)
                .registerTaskWorker(any(), any());

        rebalanceThread.start();

        Awaitility.await().ignoreExceptions().until(() -> !rebalanceThread.isAlive());
        verify(pollThread1, never()).interrupt();
        verify(pollThread1, never()).close();
        verify(pollThread2, times(1)).interrupt();
        verify(pollThread2, times(1)).close();
        Assertions.assertThat(rebalanceThread.runningConnections.keySet()).hasSize(1);
    }

    @Test
    public void shouldCreatePollThreadsForEveryHostAfterAClusterScaleUp() {
        when(livenessController.keepWorkerRunning()).thenReturn(true, true, true, false);
        LHHostInfo serverA = LHHostInfo.newBuilder().setHost("a").setPort(1234).build();
        LHHostInfo serverB = LHHostInfo.newBuilder().setHost("b").setPort(4567).build();
        LHHostInfo serverC = LHHostInfo.newBuilder().setHost("c").setPort(8901).build();
        LHHostInfo serverD = LHHostInfo.newBuilder().setHost("d").setPort(8902).build();
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
        registerFakeResponses(response1.build(), response2.build(), response3.build())
                .when(bootstrapStub)
                .registerTaskWorker(any(), any());
        rebalanceThread.start();

        Awaitility.await().ignoreExceptions().until(() -> !rebalanceThread.isAlive());
        Assertions.assertThat(rebalanceThread.runningConnections.keySet()).hasSize(4);
    }

    @Test
    public void shouldReCreateFailedPollingThreads() {
        when(livenessController.keepWorkerRunning()).thenReturn(true, true, true, false);
        when(config.getWorkerThreads()).thenReturn(3);
        LHHostInfo serverA = LHHostInfo.newBuilder().setHost("a").setPort(1234).build();
        RegisterTaskWorkerResponse.Builder response1 =
                RegisterTaskWorkerResponse.newBuilder().addYourHosts(serverA);
        RegisterTaskWorkerResponse.Builder response2 =
                RegisterTaskWorkerResponse.newBuilder().addYourHosts(serverA);
        RegisterTaskWorkerResponse.Builder response3 =
                RegisterTaskWorkerResponse.newBuilder().addYourHosts(serverA);
        PollThread pollThread1 = mock("pollThread1");
        PollThread pollThread2 = mock("pollThread2");
        PollThread pollThread3 = mock("pollThread3");
        PollThread pollThreadRecreated = mock("recreated");
        when(pollThread1.isRunning()).thenReturn(false);
        when(pollThread2.isRunning()).thenReturn(true);
        when(pollThread3.isRunning()).thenReturn(true);
        when(pollThreadFactory.create(any(), any()))
                .thenReturn(pollThread1, pollThread2, pollThread3, pollThreadRecreated);
        registerFakeResponses(response1.build(), response2.build(), response3.build())
                .when(bootstrapStub)
                .registerTaskWorker(any(), any());
        rebalanceThread.start();
        Awaitility.await().ignoreExceptions().until(() -> !rebalanceThread.isAlive());
        Assertions.assertThat(rebalanceThread.runningConnections.get(serverA))
                .containsExactlyInAnyOrder(pollThread2, pollThread3, pollThreadRecreated);
    }

    private Stubber registerFakeResponses(RegisterTaskWorkerResponse... responses) {
        Stubber out = null;
        if (responses.length == 0) {
            throw new IllegalArgumentException("At least one response is expected");
        }
        for (RegisterTaskWorkerResponse response : responses) {
            if (out != null) {
                out.doAnswer(invocation -> {
                    StreamObserver<RegisterTaskWorkerResponse> observer = invocation.getArgument(1);
                    observer.onNext(response);
                    return null;
                });
            } else {
                out = doAnswer(invocation -> {
                    StreamObserver<RegisterTaskWorkerResponse> observer = invocation.getArgument(1);
                    observer.onNext(response);
                    return null;
                });
            }
        }
        return out;
    }

    public void myTestWorkerMethod() {
        System.out.println("Hello from worker method");
    }
}
