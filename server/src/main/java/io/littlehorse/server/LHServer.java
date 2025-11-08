package io.littlehorse.server;

import io.grpc.Context;
import io.grpc.Status;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEventModel;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.server.auth.RequestAuthorizer;
import io.littlehorse.server.auth.internalport.InternalCallCredentials;
import io.littlehorse.server.interceptors.RequestBlocker;
import io.littlehorse.server.listener.ServerListenerConfig;
import io.littlehorse.server.monitoring.HealthService;
import io.littlehorse.server.streams.BackendInternalComms;
import io.littlehorse.server.streams.CommandSender;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.taskqueue.PollTaskRequestObserver;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.MetadataCache;
import io.micrometer.core.instrument.binder.grpc.MetricCollectingServerInterceptor;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.processor.TaskId;
import org.apache.logging.log4j.LogManager;

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
    private final LHInternalClient lhInternalClient;
    private final AsyncWaiters asyncWaiters = new AsyncWaiters();
    private final RequestBlocker requestBlocker = new RequestBlocker();

    private RequestExecutionContext requestContext() {
        return contextKey.get();
    }

    public LHServer(LHServerConfig config) throws LHMisconfigurationException {
        this.metadataCache = new MetadataCache();
        this.config = config;
        this.networkThreadpool = Executors.newVirtualThreadPerTaskExecutor();
        this.taskQueueManager = new TaskQueueManager(this);
        this.lhInternalClient = new LHInternalClient(config.getInternalClientCreds(), this.networkThreadpool);
        // Kafka Streams Setup
        if (config.getLHInstanceId().isPresent()) {
            overrideStreamsProcessId("core");
            overrideStreamsProcessId("timer");
        }
        this.coreStreams = new KafkaStreams(
                ServerTopology.initCoreTopology(config, this, metadataCache, taskQueueManager, asyncWaiters),
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
                config, coreStreams, timerStreams, metadataCache, contextKey, coreStoreProvider, asyncWaiters);

        // Health Server Setup
        this.healthService =
                new HealthService(config, coreStreams, timerStreams, taskQueueManager, metadataCache, internalComms);
        this.commandSender = new CommandSender(
                internalComms,
                networkThreadpool,
                internalComms.getCommandProducer(),
                internalComms.getTaskClaimProducer(),
                config.getStreamsSessionTimeout(),
                config,
                internalComms.getAsyncWaiters());
        this.listeners = config.getListeners().stream()
                .map(s -> this.createListener(s, networkThreadpool))
                .toList();
    }

    private LHServerListener createListener(ServerListenerConfig listenerConfig, ExecutorService networkThreads) {
        return new LHServerListener(
                listenerConfig,
                taskQueueManager,
                internalComms,
                networkThreads,
                coreStoreProvider,
                metadataCache,
                List.of(
                        new MetricCollectingServerInterceptor(healthService.getMeterRegistry()),
                        new RequestAuthorizer(contextKey, metadataCache, coreStoreProvider, config),
                        listenerConfig.getRequestAuthenticator(),
                        requestBlocker),
                contextKey,
                commandSender,
                internalComms.getAsyncWaiters(),
                lhInternalClient);
    }

    public String getInstanceName() {
        return config.getLHInstanceName();
    }

    /*
     * Sends a command to Kafka and simultaneously does a waitForProcessing() internal grpc call
     * that asynchronously waits for the command to be processed. It infers the request context from
     * the GRPC Context.
     */
    public void tryToReturnTaskToClient(TaskRunIdModel taskToClaim, PollTaskRequestObserver client) {
        CommandModel taskClaim = new CommandModel(new TaskClaimEventModel(taskToClaim, client));
        taskClaim.setCommandId(LHUtil.generateGuid());

        BiFunction<PollTaskResponse, Throwable, Void> completeTaskClaim = (taskClaimResponse, exception) -> {
            if (exception != null) {
                client.onError(new LHApiException(Status.UNAVAILABLE, "Failed recording task claim to Kafka"));
            }
            PollTaskResponse result = (PollTaskResponse) taskClaimResponse;
            client.getResponseObserver().onNext(result);
            return null;
        };

        CompletableFuture<PollTaskResponse> future = commandSender.doSend(
                taskClaim,
                PollTaskResponse.class,
                client.getPrincipalId(),
                client.getTenantId(),
                client.getRequestContext());

        future.handleAsync(completeTaskClaim, networkThreadpool);
    }

    public void onTaskScheduled(
            TaskId streamsTaskId, TaskDefIdModel taskDef, TaskRunIdModel scheduledTask, TenantIdModel tenantId) {
        taskQueueManager.onTaskScheduled(streamsTaskId, taskDef, scheduledTask, tenantId);
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

        CountDownLatch streamLatch = new CountDownLatch(2);
        new Thread(() -> {
                    log.info("Closing timer Kafka Streams");
                    timerStreams.close();
                    streamLatch.countDown();
                    log.info("Done closing timer Kafka Streams");
                })
                .start();

        new Thread(() -> {
                    log.info("Closing core Kafka Streams");
                    coreStreams.close();
                    streamLatch.countDown();
                    log.info("Done closing core Kafka Streams");
                })
                .start();
        try {
            streamLatch.await();
            log.info("Done shutting down all Kafka Stream threads");
        } catch (InterruptedException exn) {
            throw new IllegalStateException("Failed to shut down Kafka Stream threads", exn);
        }

        CountDownLatch latch = new CountDownLatch(2);

        new Thread(() -> {
                    log.info("Closing internalComms");
                    internalComms.close();
                    latch.countDown();
                    log.info("Done closing internalComms");
                })
                .start();

        new Thread(() -> {
                    log.info("Closing health service");
                    healthService.close();
                    latch.countDown();
                    log.info("Done closing health service");
                })
                .start();
        try {
            latch.await();
            log.info("Done closing internalComms and health service");
        } catch (InterruptedException exn) {
            throw new IllegalStateException("Failed to shut down internalComms and health service", exn);
        }

        for (LHServerListener listener : listeners) {
            log.info("Closing listener {}", listener);
            listener.close();
        }
        log.info("Done closing all listeners");
        LogManager.shutdown();
    }

    public Set<HostModel> getAllInternalHosts() {
        return internalComms.getAllInternalHosts();
    }

    public LHHostInfo getAdvertisedHost(
            HostModel host, String listenerName, InternalCallCredentials internalCredentials) {
        return internalComms.getAdvertisedHost(host, listenerName, internalCredentials);
    }

    public void onEventThrown(Collection<WorkflowEventModel> eventsToThrow, TenantIdModel tenantId) {
        internalComms.onWorkflowEventThrown(eventsToThrow, tenantId);
    }
}
