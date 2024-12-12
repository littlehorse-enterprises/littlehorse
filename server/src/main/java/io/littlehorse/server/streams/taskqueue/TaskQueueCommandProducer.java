package io.littlehorse.server.streams.taskqueue;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.ReportTaskRunModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.TaskClaimEventProducerCallback;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.io.Closeable;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.common.header.Headers;

/**
 * Everything related to the task protocol
 */
@Slf4j
public class TaskQueueCommandProducer implements Closeable {

    private final LHProducer producer;
    private final String commandTopic;

    public TaskQueueCommandProducer(LHProducer producer, String commandTopic) {
        this.producer = producer;
        this.commandTopic = commandTopic;
    }

    /*
     * Sends a command to Kafka and simultaneously does a waitForProcessing() internal
     * grpc call that asynchronously waits for the command to be processed. It
     * infers the request context from the GRPC Context.
     */
    public void returnTaskToClient(ScheduledTaskModel scheduledTask, PollTaskRequestObserver client) {
        TaskClaimEvent claimEvent =
                new TaskClaimEvent(scheduledTask, client.getTaskWorkerVersion(), client.getClientId());
        TaskClaimEventProducerCallback callback = new TaskClaimEventProducerCallback(scheduledTask, client);
        processCommand(claimEvent, client.getPrincipalId(), client.getTenantId(), callback);
    }

    /*
     * This method is called from within the `CommandProcessor#process()` method (specifically, on the
     * TaskClaimEvent#process()) method. Therefore, we cannot infer the RequestExecutionContext like
     * we do in the other places, because the GRPC context does not exist in this case.
     * Note that this is not a GRPC method that @Override's a super method and takes in
     * a protobuf + StreamObserver.
     */
    private void processCommand(
            TaskClaimEvent taskClaimEvent,
            PrincipalIdModel principalId,
            TenantIdModel tenantId,
            TaskClaimEventProducerCallback callback) {
        CommandModel command = new CommandModel(taskClaimEvent);

        command.setCommandId(LHUtil.generateGuid());

        Headers commandMetadata = HeadersUtil.metadataHeadersFor(tenantId, principalId);
        producer.send(command.getPartitionKey(), command, commandTopic, callback, commandMetadata.toArray());
    }

    public void send(
            ReportTaskRunModel reportTaskRun, AuthorizationContext auth, StreamObserver<Empty> clientObserver) {
        // There is no need to wait for the ReportTaskRun to actually be processed, because
        // we would just return a google.protobuf.Empty anyways. All we need to do is wait for
        // the Command to be persisted into Kafka.
        CommandModel command = new CommandModel(reportTaskRun, new Date());

        Callback kafkaProducerCallback = (meta, exn) -> {
            try {
                if (exn == null) {
                    clientObserver.onNext(Empty.getDefaultInstance());
                    clientObserver.onCompleted();
                } else {
                    clientObserver.onError(new LHApiException(Status.UNAVAILABLE, "Failed recording command to Kafka"));
                }
            } catch (IllegalStateException e) {
                log.debug("Call already closed");
            }
        };
        Headers commandMetadata = HeadersUtil.metadataHeadersFor(auth.tenantId(), auth.principalId());

        producer.send(
                command.getPartitionKey(), command, commandTopic, kafkaProducerCallback, commandMetadata.toArray());
    }

    @Override
    public void close() {
        producer.close();
    }
}
