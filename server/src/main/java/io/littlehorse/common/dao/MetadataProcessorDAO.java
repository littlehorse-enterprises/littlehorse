package io.littlehorse.common.dao;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;

public interface MetadataProcessorDAO extends ReadOnlyMetadataDAO {

    void initCommand(MetadataCommandModel command);

    MetadataCommandModel getCommand();

    <U extends Message, T extends GlobalGetable<U>> void put(T getable);

    <U extends Message, T extends GlobalGetable<U>> void delete(ObjectIdModel<?, U, T> id);
}
