package io.littlehorse.common.dao;

import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.ServerContext;
import io.littlehorse.common.ServerContextImpl;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.ServerSubCommand;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreProcessorDAOImpl;
import io.littlehorse.server.streams.topology.core.MetadataProcessorDAOImpl;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class ProcessorDAOFactory implements DAOFactory {

    private final MetadataCache metadataCache;
    private final ProcessorContext<String, CommandProcessorOutput> coreContext;
    private final ProcessorContext<String, Bytes> metadataContext;
    private final KafkaStreamsServerImpl streamsServer;
    private final LHServerConfig config;

    public ProcessorDAOFactory(
            final MetadataCache metadataCache,
            final LHServerConfig config,
            final KafkaStreamsServerImpl streamsServer,
            final ProcessorContext<String, CommandProcessorOutput> coreContext,
            final ProcessorContext<String, Bytes> metadataContext) {
        this.metadataCache = metadataCache;
        this.coreContext = coreContext;
        this.config = config;
        this.streamsServer = streamsServer;
        this.metadataContext = metadataContext;
    }

    @Override
    public MetadataProcessorDAO getMetadataDao(AbstractCommand<? extends Message> command) {
        ModelStore metadataStore = metadataStoreFor(command);
        return new MetadataProcessorDAOImpl(
                metadataStore,
                metadataCache,
                new ServerContextImpl(command.getTenantId(), ServerContext.Scope.PROCESSOR));
    }

    @Override
    public CoreProcessorDAO getCoreDao(AbstractCommand<? extends Message> command) {
        return new CoreProcessorDAOImpl(
                coreContext,
                config,
                streamsServer,
                metadataCache,
                metadataStoreFor(command),
                coreStoreFor(command),
                contextFor(command));
    }

    @Override
    public CoreProcessorDAO getCoreDao() {
        ModelStore metadataStore =
                ModelStore.defaultStore(coreContext.getStateStore(ServerTopology.GLOBAL_METADATA_STORE));
        ModelStore coreStore = ModelStore.defaultStore(coreContext.getStateStore(ServerTopology.CORE_STORE));
        ServerContext serverContext = new ServerContextImpl(ModelStore.DEFAULT_TENANT, ServerContext.Scope.PROCESSOR);
        return new CoreProcessorDAOImpl(
                coreContext, config, streamsServer, metadataCache, metadataStore, coreStore, serverContext);
    }

    private ModelStore metadataStoreFor(AbstractCommand<? extends Message> command) {
        KeyValueStore<String, Bytes> nativeStore;
        if (metadataContext != null) {
            nativeStore = metadataContext.getStateStore(ServerTopology.METADATA_STORE);
        } else {
            nativeStore = coreContext.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
        }
        return storeFor(command, nativeStore);
    }

    private ModelStore coreStoreFor(AbstractCommand<? extends Message> command) {
        KeyValueStore<String, Bytes> nativeStore = coreContext.getStateStore(ServerTopology.CORE_STORE);
        return storeFor(command, nativeStore);
    }

    private ModelStore storeFor(AbstractCommand<? extends Message> command, KeyValueStore<String, Bytes> nativeStore) {
        ModelStore store;
        if (command.getSubCommand() instanceof ServerSubCommand) {
            store = ModelStore.defaultStore(nativeStore);
        } else {
            store = ModelStore.instanceFor(nativeStore, command.getTenantId());
        }
        return store;
    }

    private ServerContext contextFor(AbstractCommand<? extends Message> command) {
        return new ServerContextImpl(command.getTenantId(), ServerContext.Scope.PROCESSOR);
    }

    private ProcessorContext<String, ?> currentProcessorContext() {
        return coreContext != null ? coreContext : metadataContext;
    }
}
