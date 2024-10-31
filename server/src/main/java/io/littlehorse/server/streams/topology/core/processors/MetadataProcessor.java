package io.littlehorse.server.streams.topology.core.processors;

import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.core.init.InitializationLogModel;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.InitializationLog;
import io.littlehorse.common.proto.MetadataCommand;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Principal;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.Version;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.LHProcessingExceptionHandler;
import io.littlehorse.server.streams.topology.core.MetadataCommandException;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

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

    public MetadataProcessor(LHServerConfig config, LHServer server, MetadataCache metadataCache) {
        this.config = config;
        this.server = server;
        this.metadataCache = metadataCache;
        this.exceptionHandler = new LHProcessingExceptionHandler(server);
    }

    public void init(final ProcessorContext<String, CommandProcessorOutput> ctx) {
        this.ctx = ctx;

        maybeInitializeStartupResources();
    }

    private void maybeInitializeStartupResources() {
        BackgroundContext context = new BackgroundContext();

        ClusterScopedStore clusterStore =
                ClusterScopedStore.newInstance(ctx.getStateStore(ServerTopology.METADATA_STORE), context);

        InitializationLogModel storedInitializationLogModel =
                clusterStore.get(InitializationLogModel.SERVER_INITIALIZED_KEY, InitializationLogModel.class);

        // If server has not been initialized..
        if (storedInitializationLogModel == null) {
            log.info("Initializing Cluster...");
            InitializationLog.Builder initializationLogBuilder = InitializationLog.newBuilder()
                    .setInitVersion(Version.getServerVersion())
                    .setInitTime(LHUtil.fromDate(new Date()));

            Tenant initialDefaultTenant = getDefaultTenant(context, clusterStore);
            initializationLogBuilder.setInitDefaultTenant(initialDefaultTenant);

            Principal initialAnonymousPrincipal = getAnonymousPrincipal(context, clusterStore);
            initializationLogBuilder.setInitAnonymousPrincipal(initialAnonymousPrincipal);

            InitializationLogModel initializationLogModel = InitializationLogModel.fromProto(
                    initializationLogBuilder.build(), InitializationLogModel.class, context);
            clusterStore.put(initializationLogModel);

            log.info("Initialization Log put to store!");
            log.info(initializationLogModel.toString());
        }
    }

    // Gets the anonymous Principal configuration. If one does not exist, create a new one.
    public Tenant getDefaultTenant(BackgroundContext context, ClusterScopedStore clusterStore) {
        TenantModel defaultTenantModel = InitializationLogModel.getDefaultTenantModel(context);

        // Check if it exists already
        @SuppressWarnings("unchecked")
        StoredGetable<Tenant, TenantModel> storedDefaultTenantModel =
                clusterStore.get(defaultTenantModel.getObjectId().getStoreableKey(), StoredGetable.class);

        // If so, return the existing default Tenant
        if (storedDefaultTenantModel != null) {
            return storedDefaultTenantModel.getStoredObject().toProto().build();
        }

        clusterStore.put(new StoredGetable<>(defaultTenantModel));
        log.info("Default Tenant put to store!");
        return defaultTenantModel.toProto().build();
    }

    // Gets the anonymous Principal configuration. If one does not exist, create a new one.
    public Principal getAnonymousPrincipal(BackgroundContext context, ClusterScopedStore clusterStore) {
        // Get the default implementation of the anonymous Principal
        PrincipalModel anonymousPrincipalModel = InitializationLogModel.getAnonymousPrincipalModel(context);

        // Check if it exists already
        @SuppressWarnings("unchecked")
        StoredGetable<Principal, PrincipalModel> storedAnonymousPrincipalStoreable =
                clusterStore.get(anonymousPrincipalModel.getObjectId().getStoreableKey(), StoredGetable.class);

        // If so, return the existing anonymous Principal
        if (storedAnonymousPrincipalStoreable != null) {
            return storedAnonymousPrincipalStoreable.getStoredObject().toProto().build();
        }

        // Otherwise, put the default implementation to the store and return it
        clusterStore.put(new StoredGetable<>(anonymousPrincipalModel));
        for (Tag tag : anonymousPrincipalModel.getIndexEntries()) {
            clusterStore.put(tag);
        }
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
                WaitForCommandResponse cmdReply = WaitForCommandResponse.newBuilder()
                        .setCommandId(command.getCommandId())
                        .setResultTime(LHUtil.fromDate(new Date()))
                        .setResult(response.toByteString())
                        .build();

                server.onResponseReceived(command.getCommandId(), cmdReply);

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
