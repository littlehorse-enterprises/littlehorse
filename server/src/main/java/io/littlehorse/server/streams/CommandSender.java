package io.littlehorse.server.streams;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.ReportTaskRunModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.LHInternalsGrpc;
import io.littlehorse.common.proto.WaitForCommandRequest;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.server.auth.internalport.InternalCallCredentials;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.state.HostInfo;

@Slf4j
public class CommandSender {

    private final BackendInternalComms internalComms;
    private final ExecutorService networkThreadpool;
    private final LHProducer commandProducer;
    private final LHProducer taskClaimProducer;
    private final LHServerConfig serverConfig;
    private final HostInfo thisHost;
    private final AsyncWaiters asyncWaiters;

    public CommandSender(
            BackendInternalComms internalComms,
            ExecutorService networkThreadpool,
            LHProducer commandProducer,
            LHProducer taskClaimProducer,
            long streamsSessionTimeout,
            LHServerConfig serverConfig,
            AsyncWaiters asyncWaiters) {
        this.internalComms = internalComms;
        // Streams Session Timeout is how long it takes to notice that the server is down.
        // Then we need the rebalance to occur, and the new server must process the command.
        // So we give it a buffer of 10 additional seconds.
        this.networkThreadpool = networkThreadpool;
        this.commandProducer = commandProducer;
        this.taskClaimProducer = taskClaimProducer;
        // The only reason for this is to resolve using the method AbstractCommand#getTopic
        this.serverConfig = serverConfig;
        this.thisHost = internalComms.getThisHost();
        this.asyncWaiters = asyncWaiters;
    }

    /*
     * This method is called from within the `CommandProcessor#process()` method (specifically, on the
     * TaskClaimEvent#process()) method. Therefore, we cannot infer the RequestExecutionContext like
     * we do in the other places, because the GRPC context does not exist in this case.
     * Note that this is not a GRPC method that @Override's a super method and takes in
     * a protobuf + StreamObserver.
     */
    public <T extends Message> CompletableFuture<T> doSend(
            AbstractCommand<?> command,
            Class<T> responseCls,
            PrincipalIdModel principalId,
            TenantIdModel tenantId,
            RequestExecutionContext context) {
        return doSend(commandProducer, command, responseCls, principalId, tenantId, context);
    }

    private <T extends Message> CompletableFuture<T> doSend(
            LHProducer producer,
            AbstractCommand<?> command,
            Class<T> responseCls,
            PrincipalIdModel principalId,
            TenantIdModel tenantId,
            RequestExecutionContext context) {
        Headers commandMetadata = HeadersUtil.metadataHeadersFor(tenantId, principalId);
        return producer.send(
                        command.getPartitionKey(), command, command.getTopic(serverConfig), commandMetadata.toArray())
                .thenCompose((recordMetadata) -> waitForCommand(command, responseCls, context));
    }

    @SuppressWarnings("unchecked")
    private <U extends Message> U buildRespFromBytes(ByteString bytes, Class<?> responseCls) {
        try {
            return (U) responseCls.getMethod("parseFrom", ByteString.class).invoke(null, bytes);
        } catch (Exception exn) {
            log.error(exn.getMessage(), exn);
            throw new RuntimeException("Not possible");
        }
    }

    public CompletableFuture<RecordMetadata> reportTaskAndDontWaitForResponse(
            ReportTaskRunModel reportTaskRun,
            StreamObserver<Empty> client,
            PrincipalIdModel principalId,
            TenantIdModel tenantId) {
        CommandModel commandToSend = new CommandModel(reportTaskRun);
        BiFunction<RecordMetadata, Throwable, RecordMetadata> completeReportTask = (recordMetadata, exception) -> {
            if (exception != null) {
                client.onError(new LHApiException(Status.UNAVAILABLE, "Failed recording task claim to Kafka"));
            } else {
                client.onNext(Empty.getDefaultInstance());
                client.onCompleted();
            }
            return recordMetadata;
        };
        return taskClaimProducer
                .send(
                        commandToSend.getPartitionKey(),
                        commandToSend,
                        commandToSend.getTopic(serverConfig),
                        HeadersUtil.metadataHeadersFor(tenantId, principalId).toArray())
                .handleAsync(completeReportTask, networkThreadpool);
    }

    private <T extends Message> CompletableFuture<T> waitForCommand(
            AbstractCommand<?> command, Class<T> responseCls, RequestExecutionContext context) {
        String storeName =
                switch (command.getStore()) {
                    case CORE -> ServerTopology.CORE_STORE;
                    case METADATA -> ServerTopology.METADATA_STORE;
                    case REPARTITION -> ServerTopology.CORE_REPARTITION_STORE;
                    case UNRECOGNIZED -> throw new LHApiException(Status.INTERNAL);
                };
        KeyQueryMetadata meta = internalComms.lookupPartitionKey(storeName, command.getPartitionKey());
        Optional<String> commandId = command.getCommandId();
        if (commandId.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        if (meta.activeHost().equals(thisHost)) {
            return asyncWaiters.getOrRegisterFuture(commandId.get(), responseCls, new CompletableFuture<>());
        } else {
            WaitForCommandRequest req = WaitForCommandRequest.newBuilder()
                    .setCommandId(commandId.get())
                    .setPartition(meta.partition())
                    .build();
            LHInternalsGrpc.LHInternalsFutureStub internalClient = internalComms.getInternalFutureClient(
                    meta.activeHost(), InternalCallCredentials.forContext(context));
            ListenableFuture<WaitForCommandResponse> futureResponse = internalClient.waitForCommand(req);
            CompletableFuture<T> out = new CompletableFuture<>();
            futureResponse.addListener(
                    () -> {
                        try {
                            WaitForCommandResponse response = futureResponse.get();
                            out.complete(buildRespFromBytes(response.getResult(), responseCls));
                        } catch (ExecutionException e) {
                            out.completeExceptionally(e.getCause());
                        } catch (InterruptedException e) {
                            log.error("Unexpected interrupted exception", e);
                            out.completeExceptionally(new StatusRuntimeException(Status.INTERNAL));
                        }
                    },
                    Runnable::run);
            return out;
        }
    }
}
