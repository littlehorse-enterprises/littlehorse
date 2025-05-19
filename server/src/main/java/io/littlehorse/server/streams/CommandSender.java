package io.littlehorse.server.streams;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.proto.WaitForCommandRequest;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.LHInternalClient;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.state.HostInfo;

@Slf4j
public class CommandSender {

    private final Duration successDurationTimeout;
    private final ExecutorService networkThreadpool;
    private final LHProducer commandProducer;
    private final LHProducer taskClaimProducer;
    private final LHServerConfig serverConfig;
    private final AsyncWaiters asyncWaiters;
    private final BiFunction<String, String, KeyQueryMetadata> lookupPartitionKey;
    private final HostInfo thisHost;
    private final LHInternalClient internalClient;

    public CommandSender(
            ExecutorService networkThreadpool,
            LHProducer commandProducer,
            LHProducer taskClaimProducer,
            long streamsSessionTimeout,
            LHServerConfig serverConfig,
            AsyncWaiters asyncWaiters,
            BiFunction<String, String, KeyQueryMetadata> lookupPartitionKey,
            LHInternalClient internalClient) {
        // Streams Session Timeout is how long it takes to notice that the server is down.
        // Then we need the rebalance to occur, and the new server must process the command.
        // So we give it a buffer of 10 additional seconds.
        this.successDurationTimeout = Duration.ofMillis(streamsSessionTimeout).plusSeconds(10);
        this.networkThreadpool = networkThreadpool;
        this.commandProducer = commandProducer;
        this.taskClaimProducer = taskClaimProducer;
        // The only reason for this is to resolve using the method AbstractCommand#getTopic
        this.serverConfig = serverConfig;
        this.asyncWaiters = asyncWaiters;
        this.lookupPartitionKey = lookupPartitionKey;
        this.thisHost =
                new HostInfo(serverConfig.getInternalAdvertisedHost(), serverConfig.getInternalAdvertisedPort());
        this.internalClient = internalClient;
    }

    /*
     * This method is called from within the `CommandProcessor#process()` method (specifically, on the
     * TaskClaimEvent#process()) method. Therefore, we cannot infer the RequestExecutionContext like
     * we do in the other places, because the GRPC context does not exist in this case.
     * Note that this is not a GRPC method that @Override's a super method and takes in
     * a protobuf + StreamObserver.
     */
    public <T extends Message> CompletableFuture<T> doSendAndWaitForResponse(
            AbstractCommand<?> command, Class<T> responseType, RequestExecutionContext executionContext) {
        AuthorizationContext auth = executionContext.authorization();
        KeyQueryMetadata keyMetadata =
                lookupPartitionKey.apply(storeName(command.getStore()), command.getPartitionKey());
        command.setCommandId(LHUtil.generateGuid());
        Headers commandMetadata = HeadersUtil.metadataHeadersFor(auth.tenantId(), auth.principalId());
        try {
            commandProducer
                    .send(command.getPartitionKey(), command, command.getTopic(serverConfig), commandMetadata.toArray())
                    .get(60, TimeUnit.SECONDS);
            if (isLocalKey(keyMetadata)) {
                CompletableFuture<Message> out = asyncWaiters.getOrRegisterFuture(
                        command.getCommandId(), responseType, new CompletableFuture<>());
                return (CompletableFuture<T>) out;
            } else {
                WaitForCommandRequest request = WaitForCommandRequest.newBuilder()
                        .setCommandId(command.getCommandId())
                        .setPartition(keyMetadata.partition())
                        .build();
                return internalClient.remoteWaitForCommand(
                        request, responseType, keyMetadata.activeHost(), executionContext);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private boolean isLocalKey(KeyQueryMetadata keyMetadata) {
        return keyMetadata.activeHost().equals(thisHost);
    }

    private String storeName(LHStoreType storeType) {
        return switch (storeType) {
            case CORE -> ServerTopology.CORE_STORE;
            case METADATA -> ServerTopology.METADATA_STORE;
            case REPARTITION -> ServerTopology.CORE_REPARTITION_STORE;
            case UNRECOGNIZED -> throw new LHApiException(Status.INTERNAL);
        };
    }

    public synchronized void registerErrorAndNotifyWaitingThreads(String commandId, Throwable cause) {}

    public CompletableFuture<RecordMetadata> doSend(AbstractCommand<?> command, AuthorizationContext auth) {
        TenantIdModel tenantId = auth.tenantId();
        PrincipalIdModel principalId = auth.principalId();
        Headers commandMetadata = HeadersUtil.metadataHeadersFor(tenantId, principalId);
        return commandProducer.send(
                command.getPartitionKey(), command, command.getTopic(serverConfig), commandMetadata.toArray());
    }

    public CompletableFuture<RecordMetadata> doSend(TaskClaimEvent taskClaimEvent, AuthorizationContext auth) {
        CommandModel taskClaim = new CommandModel(taskClaimEvent);
        TenantIdModel tenantId = auth.tenantId();
        PrincipalIdModel principalId = auth.principalId();
        Headers commandMetadata = HeadersUtil.metadataHeadersFor(tenantId, principalId);
        return taskClaimProducer.send(
                taskClaim.getPartitionKey(), taskClaim, taskClaim.getTopic(serverConfig), commandMetadata.toArray());
    }
}
