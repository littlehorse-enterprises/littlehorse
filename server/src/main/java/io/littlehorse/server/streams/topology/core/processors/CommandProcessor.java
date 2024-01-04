package io.littlehorse.server.streams.topology.core.processors;

import com.google.protobuf.Message;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.PartitionMetricsModel;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.AggregateTaskMetricsModel;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.AggregateWfMetricsModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.time.Duration;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class CommandProcessor implements Processor<String, Command, String, CommandProcessorOutput> {

    private ProcessorContext<String, CommandProcessorOutput> ctx;
    private LHServerConfig config;
    private KafkaStreamsServerImpl server;
    private final MetadataCache metadataCache;
    private final TaskQueueManager globalTaskQueueManager;

    private KeyValueStore<String, Bytes> nativeStore;
    private boolean partitionIsClaimed;

    public CommandProcessor(
            LHServerConfig config,
            KafkaStreamsServerImpl server,
            MetadataCache metadataCache,
            TaskQueueManager globalTaskQueueManager) {
        this.config = config;
        this.server = server;
        this.metadataCache = metadataCache;
        this.globalTaskQueueManager = globalTaskQueueManager;
    }

    @Override
    public void init(final ProcessorContext<String, CommandProcessorOutput> ctx) {
        this.ctx = ctx;
        this.nativeStore = ctx.getStateStore(ServerTopology.CORE_STORE);
        onPartitionClaimed();
        ctx.schedule(Duration.ofSeconds(30), PunctuationType.WALL_CLOCK_TIME, this::forwardMetricsUpdates);
    }

    @Override
    public void process(final Record<String, Command> commandRecord) {
        // We have another wrapper here as a guard against a poison pill (even
        // though we test extensively to prevent poison pills, it's better
        // to be safe than sorry.)
        try {
            processHelper(commandRecord);
        } catch (Exception exn) {
            log.error("Unexpected error processing record: ", exn);
        }
    }

    private void processHelper(final Record<String, Command> commandRecord) {
        ProcessorExecutionContext executionContext = buildExecutionContext(commandRecord);
        CommandModel command = executionContext.currentCommand();
        log.trace(
                "{} Processing command of type {} with commandId {} with partition key {}",
                config.getLHInstanceId(),
                command.type,
                command.commandId,
                command.getPartitionKey());

        try {
            Message response = command.process(executionContext, config);
            // coreDao.commit();
            executionContext.endExecution();
            if (command.hasResponse() && command.getCommandId() != null) {
                WaitForCommandResponse cmdReply = WaitForCommandResponse.newBuilder()
                        .setCommandId(command.getCommandId())
                        .setResultTime(LHUtil.fromDate(new Date()))
                        .setResult(response.toByteString())
                        .build();

                server.onResponseReceived(command.getCommandId(), cmdReply);
            }
        } catch (Exception exn) {
            if (isUserError(exn)) {
                StatusRuntimeException sre = (StatusRuntimeException) exn;
                log.debug(
                        "Caught exception processing {}:\nStatus: {}\nDescription: {}\nCause: {}",
                        command.getType(),
                        sre.getStatus().getCode(),
                        sre.getStatus().getDescription(),
                        sre.getMessage(),
                        sre.getCause());
            } else {
                log.error("Caught exception processing {} command:", command.getType(), exn);
            }
            if (command.hasResponse() && command.getCommandId() != null) {
                server.sendErrorToClient(command.getCommandId(), exn);
            }

            // If we get here, then a Really Bad Thing has happened and we should
            // let the sysadmin of this LH Server know, and provide as much debugging
            // information as possible.
        }
    }

    private ProcessorExecutionContext buildExecutionContext(Record<String, Command> commandRecord) {
        Headers metadataHeaders = commandRecord.headers();
        Command commandToProcess = commandRecord.value();
        return new ProcessorExecutionContext(
                commandToProcess, metadataHeaders, config, ctx, globalTaskQueueManager, metadataCache, server);
    }

    private boolean isUserError(Exception exn) {
        if (StatusRuntimeException.class.isAssignableFrom(exn.getClass())) {
            StatusRuntimeException sre = (StatusRuntimeException) exn;

            switch (sre.getStatus().getCode()) {
                case NOT_FOUND,
                        INVALID_ARGUMENT,
                        ALREADY_EXISTS,
                        OUT_OF_RANGE,
                        PERMISSION_DENIED,
                        UNAUTHENTICATED,
                        FAILED_PRECONDITION,
                        // RESOURCE_EXHAUSTED used for quota violations.
                        RESOURCE_EXHAUSTED:
                    return true;

                case OK,
                        UNKNOWN,
                        UNIMPLEMENTED,
                        UNAVAILABLE,
                        INTERNAL,
                        DEADLINE_EXCEEDED,
                        DATA_LOSS,
                        ABORTED,
                        CANCELLED:
            }
        }
        return false;
    }

    public void onPartitionClaimed() {
        TenantScopedStore coreDefaultStore =
                TenantScopedStore.newInstance(this.nativeStore, LHConstants.DEFAULT_TENANT, new BackgroundContext());
        if (partitionIsClaimed) {
            throw new RuntimeException("Re-claiming partition! Yikes!");
        }
        partitionIsClaimed = true;

        try (LHKeyValueIterator<ScheduledTaskModel> iter = coreDefaultStore.prefixScan("", ScheduledTaskModel.class)) {
            while (iter.hasNext()) {
                LHIterKeyValue<ScheduledTaskModel> next = iter.next();
                ScheduledTaskModel scheduledTask = next.getValue();
                log.debug("Rehydration: scheduling task: {}", scheduledTask.getStoreKey());
                server.onTaskScheduled(scheduledTask.getTaskDefId(), scheduledTask);
            }
        }
    }

    @Override
    public void close() {
        this.partitionIsClaimed = false;
    }

    private void forwardMetricsUpdates(long timestamp) {
        ClusterScopedStore coreDefaultStore =
                ClusterScopedStore.newInstance(ctx.getStateStore(ServerTopology.CORE_STORE), new BackgroundContext());
        PartitionMetricsModel metricsOnCurrentPartition =
                coreDefaultStore.get(LHConstants.PARTITION_METRICS_KEY, PartitionMetricsModel.class);

        if (metricsOnCurrentPartition != null) {
            for (AggregateWfMetricsModel aggregateWfMetrics : metricsOnCurrentPartition.buildWfRepartitionCommands()) {
                forwardMetricSubcommand(aggregateWfMetrics);
            }
            for (AggregateTaskMetricsModel aggregateTaskMetrics :
                    metricsOnCurrentPartition.buildTaskMetricRepartitionCommand()) {
                forwardMetricSubcommand(aggregateTaskMetrics);
            }
            coreDefaultStore.delete(metricsOnCurrentPartition);
        }
    }

    private void forwardMetricSubcommand(RepartitionSubCommand repartitionSubCommand) {
        RepartitionCommand repartitionCommand =
                new RepartitionCommand(repartitionSubCommand, new Date(), repartitionSubCommand.getPartitionKey());
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = repartitionSubCommand.getPartitionKey();
        cpo.topic = this.config.getRepartitionTopicName();
        cpo.payload = repartitionCommand;
        Record<String, CommandProcessorOutput> out = new Record<>(
                cpo.partitionKey,
                cpo,
                System.currentTimeMillis(),
                HeadersUtil.metadataHeadersFor(LHConstants.DEFAULT_TENANT, LHConstants.ANONYMOUS_PRINCIPAL));
        this.ctx.forward(out);
    }
}
