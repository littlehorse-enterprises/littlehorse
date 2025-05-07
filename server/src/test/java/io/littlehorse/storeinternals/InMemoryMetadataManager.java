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
import java.util.HashMap;
import java.util.Map;
import org.mockito.Mockito;

public class InMemoryMetadataManager extends MetadataManager {
    private final Map<ObjectIdModel<?, ?, ?>, AbstractGetable<?>> buffer = new HashMap<>();

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
