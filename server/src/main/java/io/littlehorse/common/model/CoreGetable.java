package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.model.metadatacommand.OutputTopicConfigModel;
import io.littlehorse.common.model.outputtopic.GenericOutputTopicRecordModel;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import java.util.Optional;

public abstract class CoreGetable<T extends Message> extends AbstractGetable<T> {

    /**
     * Can be overriden in the future to allow fine-grained control over when to send events to the output
     * topic based on the Tenant's output topic configuration and any overrides in the WfSpec/TaskDef/etc.
     */
    public boolean shouldUpdateToOutputTopic(
            OutputTopicConfigModel outputTopicConfig, ReadOnlyMetadataManager metadataManager) {
        return true;
    }

    public Optional<GenericOutputTopicRecordModel> getOutputTopicUpdate() {
        return Optional.empty();
    }
}
