package io.littlehorse.server.streams.topology.core.processors;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.AggregateMetricsModel;
import io.littlehorse.common.model.RepartitionWindowedMetricModel;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.global.metrics.PartitionMetricInventoryModel;
import io.littlehorse.common.model.getable.global.metrics.PartitionMetricModel;
import io.littlehorse.common.model.getable.objectId.PartitionMetricIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.PartitionMetric;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreCommandException;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.LHProcessingExceptionHandler;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
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
    private final LHServerConfig config;
    private final LHServer server;
    private final MetadataCache metadataCache;
    private final TaskQueueManager globalTaskQueueManager;

    private KeyValueStore<String, Bytes> nativeStore;
    private KeyValueStore<String, Bytes> globalStore;
    private boolean partitionIsClaimed;
    private final AsyncWaiters asyncWaiters;

    private final LHProcessingExceptionHandler exceptionHandler;

    public CommandProcessor(
            LHServerConfig config,
            LHServer server,
            MetadataCache metadataCache,
            TaskQueueManager globalTaskQueueManager,
            AsyncWaiters asyncWaiters) {
        this.config = config;
        this.server = server;
        this.metadataCache = metadataCache;
        this.globalTaskQueueManager = globalTaskQueueManager;
        this.exceptionHandler = new LHProcessingExceptionHandler(server, asyncWaiters);
        this.asyncWaiters = asyncWaiters;
    }

    @Override
    public void init(final ProcessorContext<String, CommandProcessorOutput> ctx) {
        this.ctx = ctx;
        this.nativeStore = ctx.getStateStore(ServerTopology.CORE_STORE);
        this.globalStore = ctx.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
        onPartitionClaimed();
        ctx.schedule(Duration.ofSeconds(5), PunctuationType.WALL_CLOCK_TIME, this::forwardMetricsUpdates);
    }

    @Override
    public void process(final Record<String, Command> commandRecord) {
        exceptionHandler.tryRun(() -> processHelper(commandRecord));
    }

    private void processHelper(final Record<String, Command> commandRecord) {
        CoreProcessorContext executionContext = buildExecutionContext(commandRecord);
        CommandModel command = executionContext.currentCommand();
        log.trace(
                "{} Processing command of type {} with commandId {} with partition key {}",
                config.getLHInstanceName(),
                command.type,
                command.getCommandId(),
                command.getPartitionKey());
        try {
            Message response = command.process(executionContext, config);
            executionContext.endExecution();

            if (command.hasResponse()) {
                CompletableFuture<Message> completable = asyncWaiters.getOrRegisterFuture(
                        command.getCommandId().get(), Message.class, new CompletableFuture<>());
                completable.complete(response);
            }
        } catch (KafkaException ke) {
            throw ke;
        } catch (Exception exn) {
            throw new CoreCommandException(exn, command);
        }
    }

    private CoreProcessorContext buildExecutionContext(Record<String, Command> commandRecord) {
        Headers metadataHeaders = commandRecord.headers();
        Command commandToProcess = commandRecord.value();
        return new CoreProcessorContext(
                commandToProcess, metadataHeaders, config, ctx, globalTaskQueueManager, metadataCache, server);
    }

    public void onPartitionClaimed() {
        if (partitionIsClaimed) {
            throw new RuntimeException("Re-claiming partition! Yikes!");
        }
        partitionIsClaimed = true;
        server.drainPartitionTaskQueue(ctx.taskId());
        ClusterScopedStore clusterStore = ClusterScopedStore.newInstance(this.globalStore, new BackgroundContext());
        rehydrateTenant(new TenantModel(LHConstants.DEFAULT_TENANT));
        try (LHKeyValueIterator<?> storedTenants = clusterStore.range(
                GetableClassEnum.TENANT.getNumber() + "/",
                GetableClassEnum.TENANT.getNumber() + "/~",
                StoredGetable.class)) {
            storedTenants.forEachRemaining(getable -> {
                TenantModel storedTenant = ((StoredGetable<Tenant, TenantModel>) getable.getValue()).getStoredObject();
                rehydrateTenant(storedTenant);
            });
        }
    }

    private void rehydrateTenant(TenantModel tenant) {
        TenantScopedStore coreDefaultStore =
                TenantScopedStore.newInstance(this.nativeStore, tenant.getId(), new BackgroundContext());
        try (LHKeyValueIterator<ScheduledTaskModel> iter = coreDefaultStore.prefixScan("", ScheduledTaskModel.class)) {
            while (iter.hasNext()) {
                LHIterKeyValue<ScheduledTaskModel> next = iter.next();
                ScheduledTaskModel scheduledTask = next.getValue();
                log.debug("Rehydration: scheduling task: {}", scheduledTask.getStoreKey());
                // This will break task rehydration for tenant specific test. this will be addressed in Issue #554
                server.onTaskScheduled(ctx.taskId(), scheduledTask.getTaskDefId(), scheduledTask, tenant.getId());
            }
        }
    }

    @Override
    public void close() {
        this.partitionIsClaimed = false;
        server.drainPartitionTaskQueue(ctx.taskId());
    }

    private void forwardMetricsUpdates(long timestamp) {
        ClusterScopedStore clusterStore = ClusterScopedStore.newInstance(this.nativeStore, new BackgroundContext());
        PartitionMetricInventoryModel metricInventory = clusterStore.get(
                PartitionMetricInventoryModel.METRIC_INVENTORY_STORE_KEY, PartitionMetricInventoryModel.class);
        if (metricInventory != null) {
            Map<TenantIdModel, List<AggregateMetricsModel>> commandsPerTenant = new HashMap<>();
            for (PartitionMetricIdModel partitionMetricId : metricInventory.getMetrics()) {
                TenantScopedStore tenantStore = TenantScopedStore.newInstance(
                        nativeStore, partitionMetricId.getTenantId(), new BackgroundContext());
                StoredGetable<PartitionMetric, PartitionMetricModel> storeable =
                        tenantStore.get(partitionMetricId.getStoreableKey(), StoredGetable.class);
                PartitionMetricModel partitionMetric = storeable.getStoredObject();
                List<RepartitionWindowedMetricModel> windowedMetrics =
                        partitionMetric.buildRepartitionCommand(LocalDateTime.now());
                tenantStore.put(new StoredGetable<>(partitionMetric));
                List<AggregateMetricsModel> metricsPerTenant =
                        commandsPerTenant.getOrDefault(partitionMetricId.getTenantId(), new ArrayList<>());
                AggregateMetricsModel current = new AggregateMetricsModel(
                        partitionMetricId.getTenantId(),
                        partitionMetricId.getMetricId(),
                        new ArrayList<>(),
                        ctx.taskId().partition(),
                        partitionMetric.getObjectId().getAggregationType());
                current.addWindowedMetric(windowedMetrics);
                metricsPerTenant.add(current);
                commandsPerTenant.putIfAbsent(partitionMetricId.getTenantId(), metricsPerTenant);
            }
            forwardRepartitionCommands(commandsPerTenant.values().stream()
                    .flatMap(Collection::stream)
                    .toList());
        }
    }

    private void forwardRepartitionCommands(Collection<AggregateMetricsModel> repartitionSubCommands) {
        String topicName = config.getRepartitionTopicName();
        for (RepartitionSubCommand repartitionSubCommand : repartitionSubCommands) {
            String partitionKey = repartitionSubCommand.getPartitionKey();
            RepartitionCommand command = new RepartitionCommand(
                    repartitionSubCommand, new Date(), UUID.randomUUID().toString());
            CommandProcessorOutput output = new CommandProcessorOutput(topicName, command, partitionKey);
            Record<String, CommandProcessorOutput> kafkaRecord =
                    new Record<>(partitionKey, output, System.currentTimeMillis());
            ctx.forward(kafkaRecord);
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
                // NOT SURE IF THIS SHOULD BE DEFAULT/ANONYMOUS.
                // I think we should mark it as "cluster-scoped" and by the "internal system" not any external
                // principal.
                HeadersUtil.metadataHeadersFor(
                        new TenantIdModel(LHConstants.DEFAULT_TENANT),
                        new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL)));
        this.ctx.forward(out);
    }
}
