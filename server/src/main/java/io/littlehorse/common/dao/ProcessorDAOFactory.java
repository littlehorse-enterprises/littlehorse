package io.littlehorse.common.dao;

import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public class ProcessorDAOFactory implements DAOFactory {

    private final MetadataCache metadataCache;
    private final ProcessorContext<String, Bytes> context;

    public ProcessorDAOFactory(final MetadataCache metadataCache, final ProcessorContext<String, Bytes> context) {
        this.metadataCache = metadataCache;
        this.context = context;
    }
}
