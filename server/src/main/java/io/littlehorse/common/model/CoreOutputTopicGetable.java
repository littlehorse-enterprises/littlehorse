package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.sdk.common.proto.OutputTopicConfig;
import io.littlehorse.sdk.common.proto.OutputTopicConfig.OutputTopicRecordingLevel;
import io.littlehorse.server.streams.storeinternals.ReadOnlyGetableManager;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;

/**
 * Classes that extend this class can be sent to the output topic.
 */
public interface CoreOutputTopicGetable<T extends Message> {

    /**
     * Can be overriden to provide fine-grained control over whether this Getable should be sent to
     * the output topic.
     * @param previousValue must be a T, but generics are hard.
     */
    default boolean shouldProduceToOutputTopic(
            T previousValue,
            ReadOnlyMetadataManager metadataManager,
            ReadOnlyGetableManager getableManager,
            OutputTopicConfig.OutputTopicRecordingLevel outputTopicRecordingLevel) {
        return outputTopicRecordingLevel == OutputTopicRecordingLevel.ALL_ENTITY_EVENTS;
    }
}
