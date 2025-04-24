package io.littlehorse.server;

import io.grpc.Context;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.server.auth.RequestAuthorizer;
import io.littlehorse.server.auth.internalport.InternalCallCredentials;
import io.littlehorse.server.listener.ServerListenerConfig;
import io.littlehorse.server.monitoring.HealthService;
import io.littlehorse.server.streams.BackendInternalComms;
import io.littlehorse.server.streams.CommandSender;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;
import io.micrometer.core.instrument.binder.grpc.MetricCollectingServerInterceptor;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.processor.TaskId;

@Slf4j
public class LHServer {

    private LHServerConfig config;
    private TaskQueueManager taskQueueManager;

    private KafkaStreams coreStreams;
    private KafkaStreams timerStreams;

    private BackendInternalComms internalComms;
    private HealthService healthService;
    private Context.Key<RequestExecutionContext> contextKey = Context.key("executionContextKey");
    private final MetadataCache metadataCache;
    private final CoreStoreProvider coreStoreProvider;
    private final ExecutorService networkThreadpool;
    private final List<LHServerListener> listeners;
    private final CommandSender commandSender;
    private static final ConcurrentHashMap<String, CommandSender.FutureAndType> responses = new ConcurrentHashMap<>();

    private RequestExecutionContext requestContext() {
        return contextKey.get();
    }

    public LHServer(LHServerConfig config) throws LHMisconfigurationException {
        this.metadataCache = new MetadataCache();
        this.config = config;
        this.networkThreadpool = Executors.newVirtualThreadPerTaskExecutor();
        LHInternalClient internalClient = new LHInternalClient(config.getInternalClientCreds(), this.networkThreadpool);
        this.commandSender = new CommandSender(
                networkThreadpool,
                config.getCommandProducer(),
                config.getTaskClaimProducer(),
                config.getStreamsSessionTimeout(),
                config,
                responses,
                this::lookupPartitionKey,
                internalClient);
        this.taskQueueManager = new TaskQueueManager(
                getInstanceName(), networkThreadpool, this.commandSender, LHConstants.MAX_TASKRUNS_IN_ONE_TASKQUEUE);
        // Kafka Streams Setup
        if (config.getLHInstanceId().isPresent()) {
            overrideStreamsProcessId("core");
            overrideStreamsProcessId("timer");
        }
        this.coreStreams = new KafkaStreams(
                ServerTopology.initCoreTopology(config, this, metadataCache, taskQueueManager, responses),
                config.getCoreStreamsConfig());
        this.timerStreams = new KafkaStreams(ServerTopology.initTimerTopology(config), config.getTimerStreamsConfig());

        coreStreams.setUncaughtExceptionHandler(throwable -> {
            log.error("Uncaught exception for " + throwable.getMessage());
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        });
        timerStreams.setUncaughtExceptionHandler(throwable -> {
            log.error("Uncaught exception for " + throwable.getMessage());
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        });

        coreStoreProvider = new CoreStoreProvider(this.coreStreams);
        this.internalComms = new BackendInternalComms(
                config,
                coreStreams,
                timerStreams,
                networkThreadpool,
                metadataCache,
                contextKey,
                coreStoreProvider,
                responses);

        // Health Server Setup
        this.healthService =
                new HealthService(config, coreStreams, timerStreams, taskQueueManager, metadataCache, internalComms);

        this.listeners =
                config.getListeners().stream().map(this::createListener).toList();
    }

    private LHServerListener createListener(ServerListenerConfig listenerConfig) {
        return new LHServerListener(
                listenerConfig,
                taskQueueManager,
                internalComms,
                networkThreadpool,
                coreStoreProvider,
                metadataCache,
                List.of(
                        new MetricCollectingServerInterceptor(healthService.getMeterRegistry()),
                        new RequestAuthorizer(contextKey, metadataCache, coreStoreProvider, config),
                        listenerConfig.getRequestAuthenticator()),
                contextKey,
                commandSender);
    }

    public String getInstanceName() {
        return config.getLHInstanceName();
    }

    public void onTaskScheduled(
            TaskId streamsTaskId, TaskDefIdModel taskDef, ScheduledTaskModel scheduledTask, TenantIdModel tenantId) {
        taskQueueManager.onTaskScheduled(streamsTaskId, taskDef, scheduledTask, tenantId);
    }

    private KeyQueryMetadata lookupPartitionKey(String storeName, String partitionKey) {
        try {
            KeyQueryMetadata metadata = coreStreams.queryMetadataForKey(
                    storeName, partitionKey, Serdes.String().serializer());
            if (metadata.activeHost().port() == -1
                    && metadata.activeHost().host().equals("unavailable")) {
                throw new LHApiException(Status.UNAVAILABLE, "Kafka Streams not ready yet");
            }
            return metadata;
        } catch (IllegalStateException ex) {
            throw new LHApiException(Status.UNAVAILABLE, ex.getMessage());
        }
    }

    public void drainPartitionTaskQueue(TaskId streamsTaskId) {
        taskQueueManager.drainPartition(streamsTaskId);
    }

    private void overrideStreamsProcessId(String topology) {
        String fakeUuid = String.format(
                "%08d-0000-0000-0000-000000000000", config.getLHInstanceId().get());
        String fileContent = String.format("{\"processId\":\"" + fakeUuid + "\"}");

        try {
            Path stateDir = Path.of(config.getStateDirectory());
            Path streamsDir = stateDir.resolve(config.getKafkaGroupId(topology));
            Path streamsMetadataFile = streamsDir.resolve("kafka-streams-process-metadata");
            if (!Files.exists(streamsMetadataFile.getParent())) {
                Files.createDirectories(streamsMetadataFile.getParent());
            }
            try (FileWriter writer = new FileWriter(streamsMetadataFile.toFile())) {
                writer.write(fileContent);
                log.info("Overwrote kafka-streams-process-metadata with content: {}", fileContent);
            }

        } catch (IOException exn) {
            throw new LHMisconfigurationException("Failed overriding Streams Process ID", exn);
        }
    }

    public void start() throws IOException {
        coreStreams.start();
        timerStreams.start();
        internalComms.start();
        for (LHServerListener listener : listeners) {
            listener.start();
        }
        healthService.start();
    }

    public void close() {
        CountDownLatch latch = new CountDownLatch(4 + listeners.size());

        new Thread(() -> {
                    log.info("Closing timer Kafka Streams");
                    timerStreams.close();
                    latch.countDown();
                    log.info("Done closing timer Kafka Streams");
                })
                .start();

        new Thread(() -> {
                    log.info("Closing core Kafka Streams");
                    coreStreams.close();
                    latch.countDown();
                    log.info("Done closing core Kafka Streams");
                })
                .start();

        new Thread(() -> {
                    log.info("Closing internalComms");
                    internalComms.close();
                    latch.countDown();
                })
                .start();

        new Thread(() -> {
                    log.info("Closing health service");
                    healthService.close();
                    latch.countDown();
                })
                .start();

        for (LHServerListener listener : listeners) {
            new Thread(() -> {
                        log.info("Closing listener {}", listener);
                        listener.close();
                        latch.countDown();
                    })
                    .start();
        }

        try {
            latch.await();
            log.info("Done shutting down all LHServer threads");
        } catch (Exception exn) {
            throw new RuntimeException(exn);
        }
    }

    public Set<HostModel> getAllInternalHosts() {
        return internalComms.getAllInternalHosts();
    }

    public LHHostInfo getAdvertisedHost(
            HostModel host, String listenerName, InternalCallCredentials internalCredentials) {
        return internalComms.getAdvertisedHost(host, listenerName, internalCredentials);
    }

    public void onEventThrown(WorkflowEventModel event, TenantIdModel tenantId) {
        internalComms.onWorkflowEventThrown(event, tenantId);
    }
}
