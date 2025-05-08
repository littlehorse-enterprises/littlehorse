package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.MetadataCommand;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

public class MetadataCommandExecution implements ExecutionContext {

    private final ProcessorContext<String, CommandProcessorOutput> processorContext;
    private final MetadataCache metadataCache;
    private final AuthorizationContext authContext;
    private MetadataManager metadataManager;
    private LHServerConfig lhConfig;
    private final MetadataCommandModel currentCommand;

    public MetadataCommandExecution(
            Headers recordMetadata,
            ProcessorContext<String, CommandProcessorOutput> processorContext,
            MetadataCache metadataCache,
            LHServerConfig lhConfig,
            MetadataCommand currentCommand) {
        this.processorContext = processorContext;
        this.metadataCache = metadataCache;
        KeyValueStore<String, Bytes> nativeMetadataStore = nativeMetadataStore();
        this.metadataManager = new MetadataManager(
                ClusterScopedStore.newInstance(nativeMetadataStore, this),
                TenantScopedStore.newInstance(
                        nativeMetadataStore, HeadersUtil.tenantIdFromMetadata(recordMetadata), this),
                metadataCache);
        this.currentCommand = MetadataCommandModel.fromProto(currentCommand, MetadataCommandModel.class, this);
        this.authContext = this.authContextFor(
                HeadersUtil.tenantIdFromMetadata(recordMetadata), HeadersUtil.principalIdFromMetadata(recordMetadata));
        this.lhConfig = lhConfig;
    }

    @Override
    public AuthorizationContext authorization() {
        return authContext;
    }

    @Override
    public WfService service() {
        return new WfService(this.metadataManager, metadataCache, this);
    }

    @Override
    public MetadataManager metadataManager() {
        return metadataManager;
    }

    @Override
    public LHServerConfig serverConfig() {
        return lhConfig;
    }

    public MetadataCommandModel currentCommand() {
        return currentCommand;
    }

    public void forward(CoreSubCommand<?> coreCommand) {
        CommandModel commandModel = new CommandModel(coreCommand, new Date());
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = coreCommand.getPartitionKey();
        cpo.topic = this.lhConfig.getCoreCmdTopicName();
        cpo.payload = commandModel;
        TenantIdModel tenantId = authorization().tenantId();
        PrincipalIdModel principalId = authorization().principalId();

        Record<String, CommandProcessorOutput> out = new Record<>(
                cpo.partitionKey,
                cpo,
                System.currentTimeMillis(),
                HeadersUtil.metadataHeadersFor(tenantId, principalId));

        this.processorContext.forward(out);
    }

    public void maybeCreateOutputTopic(TenantModel tenant) {
        if (tenant.getOutputTopicConfig() == null) return;

        Pair<NewTopic, NewTopic> topics = lhConfig.getOutputTopicsFor(tenant);
        try {
            this.lhConfig.createKafkaTopic(topics.getLeft());
            this.lhConfig.createKafkaTopic(topics.getRight());
        } catch (Exception exn) {
            // Note that using automatic topic creation is not intended for production
            // clusters.
            exn.printStackTrace();
        }
    }

    private KeyValueStore<String, Bytes> nativeMetadataStore() {
        return processorContext.getStateStore(ServerTopology.METADATA_STORE);
    }

    private AuthorizationContext authContextFor(TenantIdModel tenantId, PrincipalIdModel principalId) {
        // We will need to pass list of acls and isAdmin argument in order to implement
        // fine-grained authorization
        return new AuthorizationContextImpl(principalId, tenantId, List.of(), false);
    }
}
