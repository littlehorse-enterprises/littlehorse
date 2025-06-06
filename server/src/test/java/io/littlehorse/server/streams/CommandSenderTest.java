package io.littlehorse.server.streams;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.protobuf.Empty;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.TestStreamObserver;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.subcommand.ReportTaskRunModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.server.streams.taskqueue.PollTaskRequestObserver;
import io.littlehorse.server.streams.util.AsyncWaiters;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.header.Header;
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
            internalComms, threadPool, commandProducer, taskClaimProducer, 60l, serverConfig, asyncWaiters);
    private final String topicName = "test-topic";

    @BeforeEach
    public void setup() {
        when(serverConfig.getCoreCmdTopicName()).thenReturn(topicName);
    }

    @Test
    @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
    public void shouldSendTaskClaimEvent() throws Exception {
        ScheduledTaskModel scheduledTask = mock(ScheduledTaskModel.class);
        when(scheduledTask.getTaskRunId()).thenReturn(new TaskRunIdModel(new WfRunIdModel("test"), "test-guid"));
        PollTaskRequestObserver client = mock(PollTaskRequestObserver.class);
        when(client.getTenantId()).thenReturn(new TenantIdModel("test-tenant"));
        when(client.getPrincipalId()).thenReturn(new PrincipalIdModel("test-principal"));
        CompletableFuture<RecordMetadata> producerResult = CompletableFuture.completedFuture(recordMetadata);
        producerWithResult(taskClaimProducer, producerResult);
        CompletableFuture<RecordMetadata> future = sender.doSend(scheduledTask, client);
        assertThat(future.get()).isSameAs(recordMetadata);
        verify(client).sendResponse(scheduledTask);
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

        CompletableFuture<RecordMetadata> future = sender.doSend(scheduledTask, client);
        future.get();
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
        CompletableFuture<RecordMetadata> future = sender.doSend(reportTaskRun, clientObserver, principalId, tenantId);
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
        CompletableFuture<RecordMetadata> future = sender.doSend(reportTaskRun, clientObserver, principalId, tenantId);
        future.get();
        assertThat(clientObserver.getValues()).isEmpty();
        assertThat(clientObserver.getThrowable())
                .isNotNull()
                .isInstanceOf(LHApiException.class)
                .hasMessage("UNAVAILABLE: Failed recording task claim to Kafka");
    }

    public void producerWithResult(LHProducer producer, CompletableFuture<RecordMetadata> result) {
        when(producer.send(anyString(), any(), anyString(), any(Header[].class)))
                .thenReturn(result);
    }
}
