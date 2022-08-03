package io.littlehorse.server.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.server.model.internal.MetadataEntity;

public class MetadataEntitySerializer implements Serializer<MetadataEntity> {
    public byte[] serialize(String topic, MetadataEntity thing) {
        if (topic.equals(POSTable.getEntitytTopicName(WfSpec.class))) {
            return thing.wfSpec.toBytes();
        } else if (topic.equals(POSTable.getEntitytTopicName(TaskDef.class))) {
            return thing.taskDef.toBytes();
        }

        throw new RuntimeException("Unrecognized topic");
    }
    
}
