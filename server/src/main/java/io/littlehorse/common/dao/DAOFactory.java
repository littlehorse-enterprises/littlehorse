package io.littlehorse.common.dao;

public interface DAOFactory {

    default ReadOnlyMetadataProcessorDAO getMetadataDao(int specificPartition, String tenantId) {
        throw new UnsupportedOperationException();
    }

    default ReadOnlyMetadataProcessorDAO getMetadataDao(String tenantId) {
        throw new UnsupportedOperationException();
    }

    default MetadataProcessorDAO getMetadataDao() {
        throw new UnsupportedOperationException();
    }
}
