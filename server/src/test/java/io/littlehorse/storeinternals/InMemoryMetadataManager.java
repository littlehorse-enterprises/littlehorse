package io.littlehorse.storeinternals;

import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.util.MetadataCache;
import org.mockito.Mockito;

public class InMemoryMetadataManager extends MetadataManager {
    public InMemoryMetadataManager() {
        super(Mockito.mock(), Mockito.mock(), new MetadataCache());
    }
}
