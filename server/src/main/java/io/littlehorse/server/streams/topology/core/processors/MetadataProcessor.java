package io.littlehorse.server.streams.topology.core.processors;

import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.core.init.InitConfigModel;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.InitConfig;
import io.littlehorse.common.proto.MetadataCommand;
import io.littlehorse.common.proto.ServerVersion;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.Principal;
import io.littlehorse.sdk.common.proto.PrincipalId;
import io.littlehorse.sdk.common.proto.ServerACL;
import io.littlehorse.sdk.common.proto.ServerACLs;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.sdk.common.proto.TenantId;
import io.littlehorse.server.LHServer;
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

        ClusterScopedStore clusterStore = ClusterScopedStore.newInstance(
                ctx.getStateStore(ServerTopology.METADATA_STORE), new BackgroundContext());

        InitConfigModel oldInitConfigModel = clusterStore.get(InitConfigModel.STORE_KEY, InitConfigModel.class);
        if (oldInitConfigModel != null) {
            System.out.println("Exists!");
            System.out.println(oldInitConfigModel.toJson());
        } else {
            InitConfig.Builder initConfigBuilder = InitConfig.newBuilder()
                    .setInitVersion(ServerVersion.newBuilder()
                            .setMajorVersion(0)
                            .setMinorVersion(0)
                            .setPatchVersion(0))
                    .setInitTime(LHUtil.fromDate(new Date()))
                    .setPedro("pedro");

            Tenant defaultTenant = Tenant.newBuilder()
                    .setId(TenantId.newBuilder().setId("default"))
                    .build();
            TenantModel defaultTenantModel = TenantModel.fromProto(defaultTenant, TenantModel.class, null);

            clusterStore.put(new StoredGetable<>(defaultTenantModel));
            initConfigBuilder.setInitDefaultTenant(defaultTenant);
            System.out.println("Default Tenant put to store!");

            Principal anonymousPrincipal = Principal.newBuilder()
                    .setId(PrincipalId.newBuilder().setId("anonymous"))
                    .setGlobalAcls(ServerACLs.newBuilder()
                            .addAcls(ServerACL.newBuilder()
                                    .addResources(ACLResource.ACL_ALL_RESOURCES)
                                    .addAllowedActions(ACLAction.ALL_ACTIONS)))
                    .build();
            PrincipalModel anonymousPrincipalModel =
                    PrincipalModel.fromProto(anonymousPrincipal, PrincipalModel.class, null);
            clusterStore.put(new StoredGetable<>(anonymousPrincipalModel));
            for (Tag tag : anonymousPrincipalModel.getIndexEntries()) {
                clusterStore.put(tag);
            }
            initConfigBuilder.setInitAnonymousPrincipal(anonymousPrincipal);
            System.out.println("Anonymous Principal put to store!");

            InitConfigModel initConfigModel =
                    InitConfigModel.fromProto(initConfigBuilder.build(), InitConfigModel.class, null);
            clusterStore.put(initConfigModel);

            System.out.println("InitConfig put to store!");
        }
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
