package io.littlehorse.common.dao;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;

public interface MetadataProcessorDAO extends ReadOnlyMetadataStore {

    /*
     * Lifecycle for processing a Command
     */

    public void initCommand(MetadataCommandModel command);

    public MetadataCommandModel getCommand();

    /*
     * Read/Write processing.
     */

    public <U extends Message, T extends GlobalGetable<U>> T get(ObjectIdModel<?, U, T> id);

    public <U extends Message, T extends GlobalGetable<U>> void put(T getable);

    public <U extends Message, T extends GlobalGetable<U>> DeleteObjectReply delete(ObjectIdModel<?, U, T> id);
}
