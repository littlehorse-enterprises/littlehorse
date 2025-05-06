package io.littlehorse.server.streams.topology.core.processors;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.core.init.InitializationLogModel;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.InitializationLog;
import io.littlehorse.common.proto.MetadataCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Principal;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.Version;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.LHProcessingExceptionHandler;
import io.littlehorse.server.streams.topology.core.MetadataCommandException;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

/*
 * This is the processor that validates and processes commands to update metadata,
 * such as WfSpec/TaskDef/ExternalEventDef/UserTaskDef.
 */
@Slf4j
public class MetadataProcessor implements Processor<String, MetadataCommand, String, CommandProcessorOutput> {

    private final LHServerConfig config;
    private final LHServer server;
    private final MetadataCache metadataCache;
    private final LHProcessingExceptionHandler exceptionHandler;

    private ProcessorContext<String, CommandProcessorOutput> ctx;
    private KeyValueStore<String, Bytes> metadataStore;
    private final AsyncWaiters asyncWaiters;

    public MetadataProcessor(
            LHServerConfig config, LHServer server, MetadataCache metadataCache, AsyncWaiters asyncWaiters) {
        this.config = config;
        this.server = server;
        this.metadataCache = metadataCache;
        this.exceptionHandler = new LHProcessingExceptionHandler(server, asyncWaiters);
        this.asyncWaiters = asyncWaiters;
    }

    public void init(final ProcessorContext<String, CommandProcessorOutput> ctx) {
        this.ctx = ctx;
        this.metadataStore = ctx.getStateStore(ServerTopology.METADATA_STORE);

        maybeInitializeStartupResources();
    }

    private void maybeInitializeStartupResources() {
        BackgroundContext context = new BackgroundContext();

        ClusterScopedStore clusterStore = ClusterScopedStore.newInstance(metadataStore, context);

        InitializationLogModel storedInitializationLogModel =
                clusterStore.get(InitializationLogModel.SERVER_INITIALIZED_KEY, InitializationLogModel.class);

        TenantScopedStore tenantScopedStore =
                TenantScopedStore.newInstance(metadataStore, new TenantIdModel(LHConstants.DEFAULT_TENANT), context);

        MetadataManager metadataManager = new MetadataManager(clusterStore, tenantScopedStore, metadataCache);

        // If server has not been initialized..
        if (storedInitializationLogModel == null) {
            log.info("Initializing Cluster...");
            InitializationLog.Builder initializationLogBuilder = InitializationLog.newBuilder()
                    .setInitVersion(Version.getCurrentServerVersion())
                    .setInitTime(LHUtil.fromDate(new Date()));

            Tenant initialDefaultTenant = getDefaultTenant(context, metadataManager);
            initializationLogBuilder.setInitDefaultTenant(initialDefaultTenant);

            Principal initialAnonymousPrincipal = getAnonymousPrincipal(context, metadataManager);
            initializationLogBuilder.setInitAnonymousPrincipal(initialAnonymousPrincipal);

            InitializationLogModel initializationLogModel = InitializationLogModel.fromProto(
                    initializationLogBuilder.build(), InitializationLogModel.class, context);
            clusterStore.put(initializationLogModel);

            log.info("Initialization Log put to store!");
            log.info(initializationLogModel.toString());
        }
    }

    // Gets the anonymous Principal configuration. If one does not exist, create a new one.
    public Tenant getDefaultTenant(BackgroundContext context, MetadataManager metadataManager) {
        TenantModel defaultTenantModel = InitializationLogModel.getDefaultTenantModel(context);

        // Check if it exists already
        TenantModel storedDefaultTenantModel = metadataManager.get(defaultTenantModel.getObjectId());

        // If so, return the existing default Tenant
        if (storedDefaultTenantModel != null) {
            return storedDefaultTenantModel.toProto().build();
        }

        // Otherwise, put the default implementation to the store and return it
        metadataManager.put(defaultTenantModel);
        log.info("Default Tenant put to store!");

        return defaultTenantModel.toProto().build();
    }

    // Gets the anonymous Principal configuration. If one does not exist, create a new one.
    public Principal getAnonymousPrincipal(BackgroundContext context, MetadataManager metadataManager) {
        // Get the default implementation of the anonymous Principal
        PrincipalModel anonymousPrincipalModel = InitializationLogModel.getAnonymousPrincipalModel(context);

        // Check if it exists already
        PrincipalModel storedAnonymousPrincipalModel = metadataManager.get(new PrincipalIdModel("anonymous"));

        // If so, return the existing anonymous Principal
        if (storedAnonymousPrincipalModel != null) {
            return storedAnonymousPrincipalModel.toProto().build();
        }

        // Otherwise, put the default implementation to the store and return it
        metadataManager.put(anonymousPrincipalModel);
        log.info("Anonymous Principal put to store!");

        return anonymousPrincipalModel.toProto().build();
    }

    @Override
    public void process(final Record<String, MetadataCommand> commandRecord) {
        exceptionHandler.tryRun(() -> processHelper(commandRecord));
    }

    public void processHelper(final Record<String, MetadataCommand> record) {
        MetadataCommandExecution metadataContext = buildContext(record);
        MetadataCommandModel command = metadataContext.currentCommand();
        log.trace(
                "{} Processing command of type {} with commandId {}",
                config.getLHInstanceName(),
                command.getType(),
                command.getCommandId());

        try {
            Message response = command.process(metadataContext);
            if (command.hasResponse() && command.getCommandId() != null) {
                CompletableFuture<Message> completable = asyncWaiters.getOrRegisterFuture(
                        command.getCommandId(), Message.class, new CompletableFuture<>());
                completable.complete(response);
                // This allows us to set a larger commit interval for the Core Topology
                // without affecting latency of updates to the metadata global store.
                //
                // Messages coming through this processor are very low-throughput (updating
                // WfSpec is much less common than running a WfRun), so calling a premature
                // commit will not impact performance of the rest of the application very
                // often.
                ctx.commit();
            }
        } catch (Exception exn) {
            throw new MetadataCommandException(exn, command);
        }
    }

    public MetadataCommandExecution buildContext(final Record<String, MetadataCommand> record) {
        Headers recordMetadata = record.headers();
        return new MetadataCommandExecution(recordMetadata, ctx, metadataCache, config, record.value());
    }
}
