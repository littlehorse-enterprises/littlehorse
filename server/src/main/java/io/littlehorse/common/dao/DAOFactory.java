package io.littlehorse.common.dao;

import com.google.protobuf.Message;
import io.littlehorse.common.model.AbstractCommand;

public interface DAOFactory {

    default ReadOnlyMetadataProcessorDAO getMetadataDao(String tenantId) {
        throw new UnsupportedOperationException();
    }

    default MetadataProcessorDAO getMetadataDao(AbstractCommand<? extends Message> command) {
        throw new UnsupportedOperationException();
    }

    default CoreProcessorDAO getCoreDao(AbstractCommand<? extends Message> command) {
        throw new UnsupportedOperationException();
    }

    default CoreProcessorDAO getCoreDao() {
        throw new UnsupportedOperationException();
    }
}
