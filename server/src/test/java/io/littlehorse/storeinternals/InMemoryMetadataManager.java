package io.littlehorse.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.ClusterMetadataGetable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.ClusterMetadataId;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.mockito.Mockito;

/**
 * The InMemoryMetadataManager class provides an in-memory implementation of the MetadataManager.
 * This class is designed to manage and metadata objects in an internal, thread-safe memory buffer,
 * rather than relying on Kafka Streams KeyValue stores. It is particularly useful for testing or scenarios
 * where persistence is not required.
 *
 * Example Usage:
 * <pre>{@code
 *
 * // Instantiate the InMemoryGetableManager
 * InMemoryMetadataManager metadataManager = new InMemoryMetadataManager(executionContext);
 *
 * // Store a Getable object in the buffer
 * metadataManager.put(myPrincipalInstance);
 *
 * // Retrieve the object later
 * PrincipalModel result = metadataManager.get(new PrincipalIdModel(id));
 * }</pre>
 *
 * `TestMetadataManager` is an alternative option for this class where a InMemory Kafka Streams store is configured.
 */
public class InMemoryMetadataManager extends MetadataManager {
    private final Map<ObjectIdModel<?, ?, ?>, AbstractGetable<?>> buffer = new ConcurrentHashMap<>();

    public InMemoryMetadataManager() {
        super(Mockito.mock(), Mockito.mock(), new MetadataCache());
    }

    @Override
    public <U extends Message, T extends ClusterMetadataGetable<U>> void put(T getable) {
        buffer.put(getable.getObjectId(), getable);
    }

    @Override
    public <U extends Message, T extends MetadataGetable<U>> void put(T getable) {
        buffer.put(getable.getObjectId(), getable);
    }

    @Override
    public <U extends Message, T extends MetadataGetable<U>> T get(MetadataId<?, U, T> id) {
        return (T) buffer.get(id);
    }

    @Override
    public <U extends Message, T extends ClusterMetadataGetable<U>> T get(ClusterMetadataId<?, U, T> id) {
        return (T) buffer.get(id);
    }
}
