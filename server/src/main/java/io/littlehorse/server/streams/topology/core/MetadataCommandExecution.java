package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHServerConfig;
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
import java.util.List;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class MetadataCommandExecution implements ExecutionContext {

    private final ProcessorContext<String, Bytes> processorContext;
    private final MetadataCache metadataCache;
    private final AuthorizationContext authContext;
    private MetadataManager metadataManager;
    private LHServerConfig lhConfig;
    private final MetadataCommandModel currentCommand;

    public MetadataCommandExecution(
            Headers recordMetadata,
            ProcessorContext<String, Bytes> processorContext,
            MetadataCache metadataCache,
            LHServerConfig lhConfig,
            MetadataCommand currentCommand) {
        this.processorContext = processorContext;
        KeyValueStore<String, Bytes> nativeMetadataStore = nativeMetadataStore();
        this.metadataManager = new MetadataManager(
                ClusterScopedStore.newInstance(nativeMetadataStore, this),
                TenantScopedStore.newInstance(
                        nativeMetadataStore, HeadersUtil.tenantIdFromMetadata(recordMetadata), this));
        this.currentCommand = MetadataCommandModel.fromProto(currentCommand, MetadataCommandModel.class, this);
        this.metadataCache = metadataCache;
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

    private KeyValueStore<String, Bytes> nativeMetadataStore() {
        return processorContext.getStateStore(ServerTopology.METADATA_STORE);
    }

    private AuthorizationContext authContextFor(TenantIdModel tenantId, PrincipalIdModel principalId) {
        // We will need to pass list of acls and isAdmin argument in order to implement
        // fine-grained authorization
        return new AuthorizationContextImpl(principalId, tenantId, List.of(), false);
    }
}
