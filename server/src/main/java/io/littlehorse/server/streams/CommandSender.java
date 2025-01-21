package io.littlehorse.server.streams;

import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.taskqueue.PollTaskRequestObserver;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.POSTStreamObserver;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.common.header.Headers;

public class CommandSender {

    private final BackendInternalComms internalComms;
    private final Duration successDurationTimeout;
    private final ScheduledExecutorService networkThreadpool;
    private final LHProducer commandProducer;
    private final LHProducer taskClaimProducer;
    private final LHServerConfig serverConfig;

    public CommandSender(
            BackendInternalComms internalComms,
            ScheduledExecutorService networkThreadpool,
            LHProducer commandProducer,
            LHProducer taskClaimProducer,
            long streamsSessionTimeout,
            LHServerConfig serverConfig) {
        this.internalComms = internalComms;
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
    public <AC extends Message, RC extends Message> void doSend(
            AbstractCommand<AC> command,
            StreamObserver<RC> responseObserver,
            Class<RC> responseCls,
            boolean shouldCompleteStream,
            PrincipalIdModel principalId,
            TenantIdModel tenantId,
            RequestExecutionContext context) {
        StreamObserver<WaitForCommandResponse> commandObserver = new POSTStreamObserver<>(
                responseObserver,
                responseCls,
                shouldCompleteStream,
                internalComms,
                command,
                context,
                successDurationTimeout,
                networkThreadpool);

        Callback callback = this.internalComms.createProducerCommandCallback(command, commandObserver, context);

        command.setCommandId(LHUtil.generateGuid());

        Headers commandMetadata = HeadersUtil.metadataHeadersFor(tenantId, principalId);
        commandProducer.send(
                command.getPartitionKey(),
                command,
                command.getTopic(serverConfig),
                callback,
                commandMetadata.toArray());
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
}
