package io.littlehorse.server.streams;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.TestStreamObserver;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.ReportTaskRunModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.LHInternalsGrpc;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.server.auth.internalport.InternalCallCredentials;
import io.littlehorse.server.streams.taskqueue.PollTaskRequestObserver;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.utils.CompletedListenableFuture;
import io.littlehorse.utils.FailedListenableFuture;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.state.HostInfo;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;

class CommandSenderTest {

    private final BackendInternalComms internalComms = mock(BackendInternalComms.class);
    private final ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
    private final LHProducer commandProducer = mock(LHProducer.class);
    private final LHProducer taskClaimProducer = mock(LHProducer.class);
    private final LHServerConfig serverConfig = mock(LHServerConfig.class);
    private final AsyncWaiters asyncWaiters = mock(AsyncWaiters.class);
    private final RecordMetadata recordMetadata = mock(RecordMetadata.class);
    private final CommandSender sender = new CommandSender(
            internalComms, threadPool, commandProducer, taskClaimProducer, 60L, serverConfig, asyncWaiters);
    private final HostInfo localHost = new HostInfo("localhost", 2024);
    private final HostInfo remoteHost = new HostInfo("localhost", 2023);
    private final LHInternalsGrpc.LHInternalsFutureStub internalFutureStub =
            mock(LHInternalsGrpc.LHInternalsFutureStub.class);

    @BeforeEach
    public void setup() {
        String topicName = "test-topic";
        when(serverConfig.getCoreCmdTopicName()).thenReturn(topicName);
        when(internalComms.getThisHost()).thenReturn(localHost);
        when(recordMetadata.topic()).thenReturn(topicName);
    }

    @Test
    @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
    public void shouldSendTaskClaimEvent() throws Exception {
        ScheduledTaskModel scheduledTask = mock(ScheduledTaskModel.class);
        when(scheduledTask.getTaskRunId()).thenReturn(new TaskRunIdModel(new WfRunIdModel("test"), "test-guid"));
        PollTaskRequestObserver client = mock(PollTaskRequestObserver.class);
        when(client.getTenantId()).thenReturn(new TenantIdModel("test-tenant"));
        when(client.getPrincipalId()).thenReturn(new PrincipalIdModel("test-principal"));
        StreamObserver<PollTaskResponse> responseObserver = mock(StreamObserver.class);
        when(client.getResponseObserver()).thenReturn(responseObserver);
        CompletableFuture<RecordMetadata> producerResult = CompletableFuture.completedFuture(recordMetadata);
        producerWithResult(taskClaimProducer, producerResult);
        sender.tryToClaimTaskAndReturnToClient(scheduledTask.getTaskRunId(), client);
        threadPool.shutdown();
        threadPool.awaitTermination(10, TimeUnit.SECONDS);
        verify(responseObserver).onNext(any());
    }

    @Test
    @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
    public void shouldThrowApiExceptionWhenLHProducerFailed() throws Exception {
        ScheduledTaskModel scheduledTask = mock(ScheduledTaskModel.class);
        when(scheduledTask.getTaskRunId()).thenReturn(new TaskRunIdModel(new WfRunIdModel("test"), "test-guid"));
        PollTaskRequestObserver client = mock(PollTaskRequestObserver.class);
        when(client.getTenantId()).thenReturn(new TenantIdModel("test-tenant"));
        when(client.getPrincipalId()).thenReturn(new PrincipalIdModel("test-principal"));
        CompletableFuture<RecordMetadata> producerResult = CompletableFuture.failedFuture(new TimeoutException());
        producerWithResult(taskClaimProducer, producerResult);
        ArgumentCaptor<LHApiException> exceptionCaptor = ArgumentCaptor.forClass(LHApiException.class);

        sender.tryToClaimTaskAndReturnToClient(scheduledTask.getTaskRunId(), client);
        threadPool.shutdown();
        threadPool.awaitTermination(10, TimeUnit.SECONDS);
        verify(client).onError(exceptionCaptor.capture());
        LHApiException thrown = exceptionCaptor.getValue();
        assertThat(thrown.getMessage()).isEqualTo("UNAVAILABLE: Failed recording task claim to Kafka");
    }

    @Test
    @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
    public void shouldSendReportTaskRunAndWaitForProducer() throws Exception {
        TenantIdModel tenantId = new TenantIdModel("test-tenant");
        PrincipalIdModel principalId = new PrincipalIdModel("test-principal");
        ReportTaskRunModel reportTaskRun = mock(ReportTaskRunModel.class);
        when(reportTaskRun.getPartitionKey()).thenReturn("test-partition-key");
        TestStreamObserver<Empty> clientObserver = new TestStreamObserver<>();
        CompletableFuture<RecordMetadata> producerResult = CompletableFuture.completedFuture(recordMetadata);
        producerWithResult(taskClaimProducer, producerResult);
        CompletableFuture<RecordMetadata> future =
                sender.reportTaskAndDontWaitForResponse(reportTaskRun, clientObserver, principalId, tenantId);
        assertThat(future.get()).isSameAs(recordMetadata);
        assertThat(clientObserver.getValues()).hasSize(1);
        assertThat(clientObserver.isCompleted()).isTrue();
    }

    @Test
    @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
    public void shouldThrowApiExceptionWhenLHProducerFailedOnReportTaskRun() throws Exception {
        TenantIdModel tenantId = new TenantIdModel("test-tenant");
        PrincipalIdModel principalId = new PrincipalIdModel("test-principal");
        ReportTaskRunModel reportTaskRun = mock(ReportTaskRunModel.class);
        when(reportTaskRun.getPartitionKey()).thenReturn("test-partition-key");
        TestStreamObserver<Empty> clientObserver = new TestStreamObserver<>();
        CompletableFuture<RecordMetadata> producerResult = CompletableFuture.failedFuture(new TimeoutException());
        producerWithResult(taskClaimProducer, producerResult);
        CompletableFuture<RecordMetadata> future =
                sender.reportTaskAndDontWaitForResponse(reportTaskRun, clientObserver, principalId, tenantId);
        future.get();
        assertThat(clientObserver.getValues()).isEmpty();
        assertThat(clientObserver.getThrowable())
                .isNotNull()
                .isInstanceOf(LHApiException.class)
                .hasMessage("UNAVAILABLE: Failed recording task claim to Kafka");
    }

    @Test
    public void shouldSendAndWaitForRemoteCommand()
            throws ExecutionException, InterruptedException, java.util.concurrent.TimeoutException {
        Empty protoResponse = Empty.newBuilder().build();
        WaitForCommandResponse response = mock(WaitForCommandResponse.class);
        when(response.getResult()).thenReturn(protoResponse.toByteString());
        TenantIdModel tenantId = new TenantIdModel("test-tenant");
        PrincipalIdModel principalId = new PrincipalIdModel("test-principal");
        CommandModel command = mock(CommandModel.class);
        when(command.getCommandId()).thenReturn(Optional.of("test-command-id"));
        KeyQueryMetadata remoteKeyMetadata = mock(KeyQueryMetadata.class);
        RequestExecutionContext ctx = mock(RequestExecutionContext.class);
        AuthorizationContext auth = mock(AuthorizationContext.class);
        when(ctx.authorization()).thenReturn(auth);
        when(command.getPartitionKey()).thenReturn("test-partition-key");
        when(command.getStore()).thenReturn(LHStoreType.CORE);
        CompletableFuture<RecordMetadata> producerResult = new CompletableFuture<>();
        when(commandProducer.send(eq("test-partition-key"), same(command), eq(null), any(Header[].class)))
                .thenReturn(producerResult);
        when(remoteKeyMetadata.activeHost()).thenReturn(remoteHost);
        when(internalComms.lookupPartitionKey("core-store", "test-partition-key"))
                .thenReturn(remoteKeyMetadata);
        ArgumentCaptor<InternalCallCredentials> credentialsCaptor =
                ArgumentCaptor.forClass(InternalCallCredentials.class);
        when(internalComms.getInternalFutureClient(same(remoteHost), credentialsCaptor.capture()))
                .thenReturn(internalFutureStub);
        when(internalFutureStub.waitForCommand(any())).thenReturn(new CompletedListenableFuture<>(response));
        Future<Message> future = sender.doSend(command, Empty.class, principalId, tenantId, ctx);
        producerResult.complete(recordMetadata);
        Message message = future.get(10, TimeUnit.MILLISECONDS);
        assertThat((Empty) message).isNotNull().isEqualTo(protoResponse);
        InternalCallCredentials credentials = credentialsCaptor.getValue();
        assertThat(credentials.getAuthorization()).isSameAs(auth);
    }

    @Test
    public void shouldWaitForRemoteCommandAsynchronously() {
        TenantIdModel tenantId = new TenantIdModel("test-tenant");
        PrincipalIdModel principalId = new PrincipalIdModel("test-principal");
        CommandModel command = mock(CommandModel.class);
        when(command.getCommandId()).thenReturn(Optional.of("test-command-id"));
        KeyQueryMetadata remoteKeyMetadata = mock(KeyQueryMetadata.class);
        RequestExecutionContext ctx = mock(RequestExecutionContext.class);
        when(command.getPartitionKey()).thenReturn("test-partition-key");
        when(command.getStore()).thenReturn(LHStoreType.CORE);
        CompletableFuture<RecordMetadata> producerResult = new CompletableFuture<>();
        when(commandProducer.send(eq("test-partition-key"), same(command), eq(null), any(Header[].class)))
                .thenReturn(producerResult);
        when(remoteKeyMetadata.activeHost()).thenReturn(remoteHost);
        when(internalComms.lookupPartitionKey("core-store", "test-partition-key"))
                .thenReturn(remoteKeyMetadata);
        when(internalComms.getInternalFutureClient(same(remoteHost), any())).thenReturn(internalFutureStub);
        when(internalFutureStub.waitForCommand(any())).thenReturn(new AbstractFuture<>() {});
        Future<Message> future = sender.doSend(command, Empty.class, principalId, tenantId, ctx);
        Awaitility.await()
                .pollDelay(Duration.ofNanos(1))
                .atMost(10, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> producerResult.complete(recordMetadata));
        assertThatThrownBy(() -> future.get(1, TimeUnit.MILLISECONDS))
                .isInstanceOf(java.util.concurrent.TimeoutException.class);
    }

    @Test
    public void shouldPropagateExceptionWhenRemoteCommandFailed() {
        TenantIdModel tenantId = new TenantIdModel("test-tenant");
        PrincipalIdModel principalId = new PrincipalIdModel("test-principal");
        CommandModel command = mock(CommandModel.class);
        when(command.getCommandId()).thenReturn(Optional.of("test-command-id"));
        KeyQueryMetadata remoteKeyMetadata = mock(KeyQueryMetadata.class);
        RequestExecutionContext ctx = mock(RequestExecutionContext.class);
        when(command.getPartitionKey()).thenReturn("test-partition-key");
        when(command.getStore()).thenReturn(LHStoreType.CORE);
        CompletableFuture<RecordMetadata> producerResult = new CompletableFuture<>();
        when(commandProducer.send(eq("test-partition-key"), same(command), eq(null), any(Header[].class)))
                .thenReturn(producerResult);
        when(remoteKeyMetadata.activeHost()).thenReturn(remoteHost);
        when(internalComms.lookupPartitionKey("core-store", "test-partition-key"))
                .thenReturn(remoteKeyMetadata);
        when(internalComms.getInternalFutureClient(same(remoteHost), any())).thenReturn(internalFutureStub);
        when(internalFutureStub.waitForCommand(any()))
                .thenReturn(
                        new FailedListenableFuture<>("boom", new StatusRuntimeException(Status.FAILED_PRECONDITION)));
        Future<Message> future = sender.doSend(command, Empty.class, principalId, tenantId, ctx);
        producerResult.complete(recordMetadata);
        assertThatThrownBy(() -> future.get(10, TimeUnit.MILLISECONDS))
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(StatusRuntimeException.class);
    }

    public void producerWithResult(LHProducer producer, CompletableFuture<RecordMetadata> result) {
        when(producer.send(anyString(), any(), anyString(), any(Header[].class)))
                .thenReturn(result);
    }
}
