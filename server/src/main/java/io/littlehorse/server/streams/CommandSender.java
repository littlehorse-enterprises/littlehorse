package io.littlehorse.server.streams;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.taskqueue.PollTaskRequestObserver;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
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
    private final Map<String, ResponseOrError> responses = new ConcurrentHashMap<>();

    public CommandSender(
            ExecutorService networkThreadpool,
            LHProducer commandProducer,
            LHProducer taskClaimProducer,
            long streamsSessionTimeout,
            LHServerConfig serverConfig) {
        // Streams Session Timeout is how long it takes to notice that the server is down.
        // Then we need the rebalance to occur, and the new server must process the command.
        // So we give it a buffer of 10 additional seconds.
        this.successDurationTimeout = Duration.ofMillis(streamsSessionTimeout).plusSeconds(10);
        this.networkThreadpool = networkThreadpool;
        this.commandProducer = commandProducer;
        this.taskClaimProducer = taskClaimProducer;
        // The only reason for this is to resolve using the method AbstractCommand#getTopic
        this.serverConfig = serverConfig;
    }

    /*
     * This method is called from within the `CommandProcessor#process()` method (specifically, on the
     * TaskClaimEvent#process()) method. Therefore, we cannot infer the RequestExecutionContext like
     * we do in the other places, because the GRPC context does not exist in this case.
     * Note that this is not a GRPC method that @Override's a super method and takes in
     * a protobuf + StreamObserver.
     */
    public <T extends Message> void doSend(
            AbstractCommand<?> command,
            StreamObserver<T> responseObserver,
            PrincipalIdModel principalId,
            TenantIdModel tenantId) {

        Callback callback = (recordMetadata, e) -> {
            if (e != null) {
                log.error("Failed to send command to Kafka", e);
                responseObserver.onError(e);
                command.getCommandId().notify();
            }
        };

        command.setCommandId(LHUtil.generateGuid());

        Headers commandMetadata = HeadersUtil.metadataHeadersFor(tenantId, principalId);
        commandProducer.send(
                command.getPartitionKey(),
                command,
                command.getTopic(serverConfig),
                callback,
                commandMetadata.toArray());
        try {
            Semaphore lock = new Semaphore(0);
            synchronized (command.getCommandId()) {
                log.info(
                        "Waiting for response for command {} for command {}",
                        command.getCommandId(),
                        command.getSubCommand().getClass().getSimpleName());
                responses.put(command.getCommandId(), new ResponseOrError(lock, null, null));
                lock.acquire();
                log.info("Done waiting for response for command {}", command.getCommandId());
            }
            ResponseOrError responseOrError = responses.remove(command.getCommandId());
            if (responseOrError.isError()) {
                responseObserver.onError(responseOrError.error);
            } else if (responseOrError.isResponse()) {
                // Trust in the force
                responseObserver.onNext((T) responseOrError.response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(
                        new LHApiException(Status.INTERNAL, "Timedout waiting for response from Kafka"));
            }
        } catch (InterruptedException e) {
            log.error("Response timeout", e);
        }
    }

    public void registerResponseAndNotifyWaitingThreads(String commandId, Message response) {
        if (responses.containsKey(commandId)) {
            Semaphore lock = responses.get(commandId).lock;
            responses.put(commandId, new ResponseOrError(lock, response, null));
            log.info("releasing lock for command {}", commandId);
            lock.release();
        }
    }

    public void registerErrorAndNotifyWaitingThreads(String commandId, Throwable cause) {
        if (responses.containsKey(commandId)) {
            Semaphore lock = responses.get(commandId).lock;
            responses.put(commandId, new ResponseOrError(lock, null, cause));
            log.info("releasing lock for command {}", commandId);
            lock.release();
        }
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

    private record ResponseOrError(Semaphore lock, Message response, Throwable error) {
        public boolean isError() {
            return error != null;
        }

        public boolean isResponse() {
            return response != null;
        }
    }
}
