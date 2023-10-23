package io.littlehorse.common.dao;

import com.google.protobuf.Message;
import io.littlehorse.common.ServerContext;
import io.littlehorse.common.ServerContextImpl;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.ServerSubCommand;
import io.littlehorse.common.model.SubCommand;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.LHStore;
import io.littlehorse.server.streams.topology.core.MetadataProcessorDAOImpl;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class ProcessorDAOFactory implements DAOFactory {

    private final MetadataCache metadataCache;
    private final ProcessorContext<String, Bytes> context;

    public ProcessorDAOFactory(final MetadataCache metadataCache, final ProcessorContext<String, Bytes> context) {
        this.metadataCache = metadataCache;
        this.context = context;
    }

    @Override
    public MetadataProcessorDAO getMetadataDao(AbstractCommand<? extends Message> command) {
        SubCommand<? extends Message> subCommand = command.getSubCommand();
        KeyValueStore<String, Bytes> nativeStore = context.getStateStore(ServerTopology.METADATA_STORE);
        LHStore store;
        if (subCommand instanceof ServerSubCommand) {
            store = LHStore.defaultStore(nativeStore);
        } else {
            store = LHStore.instanceFor(nativeStore, command.getTenantId());
        }
        return new MetadataProcessorDAOImpl(
                store, metadataCache, new ServerContextImpl(command.getTenantId(), ServerContext.Scope.PROCESSOR));
    }
}
