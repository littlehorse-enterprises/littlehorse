package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.storeinternals.ReadOnlyGetableManager;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.List;

public class MetadataCommandExecution implements ExecutionContext{

    private final ModelStore metadataStore;
    private final ProcessorContext<String, Bytes> processorContext;
    private final MetadataCache metadataCache;
    private final GetableManager getableManager;
    private final AuthorizationContext authContext;

    public MetadataCommandExecution(Headers recordMetadata, ProcessorContext<String, Bytes> processorContext, MetadataCache metadataCache, GetableManager getableManager){
        this.processorContext = processorContext;
        this.metadataCache = metadataCache;
        this.getableManager = getableManager;
        this.metadataStore = storeFor(HeadersUtil.tenantIdFromMetadata(recordMetadata), nativeMetadataStore());
        this.authContext = this.authContextFor(HeadersUtil.tenantIdFromMetadata(recordMetadata), HeadersUtil.principalIdFromMetadata(recordMetadata));
    }

    @Override
    public AuthorizationContext authorization() {
        return authContext;
    }

    @Override
    public WfService service() {
        return new WfService(null, metadataStore, metadataCache, readOnlyGetableManager);
    }

    public GetableManager getableManager(){
        return getableManager;
    }

    private ModelStore storeFor(String tenantId, KeyValueStore<String, Bytes> nativeStore) {
        return ModelStore.instanceFor(nativeStore, tenantId, this);
    }

    private KeyValueStore<String, Bytes> nativeMetadataStore(){
        return processorContext.getStateStore(ServerTopology.METADATA_STORE);
    }


    private AuthorizationContext authContextFor(String tenantId, String principalId){
        // We will need to pass list of acls and isAdmin argument in order to implement
        // fine-grained authorization
        return new AuthorizationContextImpl(principalId, tenantId, List.of(), false);
    }
}
