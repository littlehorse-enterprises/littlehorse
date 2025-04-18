package io.littlehorse.server.streams;

import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.server.streams.taskqueue.PollTaskRequestObserver;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.common.header.Headers;

@Slf4j
public class CommandSender {

    private final Duration successDurationTimeout;
    private final ExecutorService networkThreadpool;
    private final LHProducer commandProducer;
    private final LHProducer taskClaimProducer;
    private final LHServerConfig serverConfig;
    private final ConcurrentHashMap<String, CommandSender.FutureAndType> responses;

    public CommandSender(
            ExecutorService networkThreadpool,
            LHProducer commandProducer,
            LHProducer taskClaimProducer,
            long streamsSessionTimeout,
            LHServerConfig serverConfig,
            ConcurrentHashMap<String, CommandSender.FutureAndType> responses) {
        // Streams Session Timeout is how long it takes to notice that the server is down.
        // Then we need the rebalance to occur, and the new server must process the command.
        // So we give it a buffer of 10 additional seconds.
        this.successDurationTimeout = Duration.ofMillis(streamsSessionTimeout).plusSeconds(10);
        this.networkThreadpool = networkThreadpool;
        this.commandProducer = commandProducer;
        this.taskClaimProducer = taskClaimProducer;
        // The only reason for this is to resolve using the method AbstractCommand#getTopic
        this.serverConfig = serverConfig;
        log.info("Creating CommandSender ");
        this.responses = responses;
    }

    /*
     * This method is called from within the `CommandProcessor#process()` method (specifically, on the
     * TaskClaimEvent#process()) method. Therefore, we cannot infer the RequestExecutionContext like
     * we do in the other places, because the GRPC context does not exist in this case.
     * Note that this is not a GRPC method that @Override's a super method and takes in
     * a protobuf + StreamObserver.
     */
    public synchronized <T extends Message> Future<T> doSend(
            AbstractCommand<?> command, Class<T> responseType, PrincipalIdModel principalId, TenantIdModel tenantId) {
        CompletableFuture<Message> out = new CompletableFuture<>();
        Callback callback = (recordMetadata, e) -> {
            if (e != null) {
                out.completeExceptionally(e);
            }
        };
        command.setCommandId(LHUtil.generateGuid());
        responses.put(command.getCommandId(), new FutureAndType(out, responseType));
        if (responseType.isAssignableFrom(RegisterTaskWorkerResponse.class)
                || responseType.isAssignableFrom(Tenant.class)) {
            log.info(
                    "Registering future for task worker response cmd={}, responses={}, {} {}",
                    command.getCommandId(),
                    responses,
                    this.toString(),
                    Thread.currentThread());
        }

        Headers commandMetadata = HeadersUtil.metadataHeadersFor(tenantId, principalId);
        commandProducer.send(
                command.getPartitionKey(),
                command,
                command.getTopic(serverConfig),
                callback,
                commandMetadata.toArray());
        return (CompletableFuture<T>) out;
    }

    public synchronized <T extends Message> void registerResponseAndNotifyWaitingThreads(String commandId, T response) {
        FutureAndType removed = responses.remove(commandId);
        if (removed == null) {
            log.error(
                    "No responses found for commandId={} responses={} clazz {} thread {}",
                    commandId,
                    responses,
                    this.toString(),
                    Thread.currentThread());
        }
        if (removed.responseType.equals(response.getClass())) {
            CompletableFuture<T> completable = (CompletableFuture<T>) removed.completable;
            completable.complete(response);
        }
    }

    public synchronized void registerErrorAndNotifyWaitingThreads(String commandId, Throwable cause) {
        FutureAndType removed = responses.remove(commandId);
        removed.completable.completeExceptionally(cause);
    }

    public void doSend(ScheduledTaskModel scheduledTask, PollTaskRequestObserver client) {
        CommandModel taskClaim = new CommandModel(new TaskClaimEvent(scheduledTask, client));
        taskClaimProducer.send(
                taskClaim.getPartitionKey(),
                taskClaim,
                taskClaim.getTopic(serverConfig),
                new TaskClaimEventProducerCallback(scheduledTask, client),
                HeadersUtil.metadataHeadersFor(client.getTenantId(), client.getPrincipalId())
                        .toArray());
    }

    public record FutureAndType(CompletableFuture<Message> completable, Class<?> responseType) {}
}
