package io.littlehorse.common.model.meta;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.TaskDefPb.KafkaTaskQueueDetailsPb;
import io.littlehorse.common.proto.TaskDefPb.KafkaTaskQueueDetailsPbOrBuilder;

public class KafkaTaskQueueDetails extends LHSerializable<KafkaTaskQueueDetailsPb> {

    public String topic;
    public String consumerGroupId;

    public Class<KafkaTaskQueueDetailsPb> getProtoBaseClass() {
        return KafkaTaskQueueDetailsPb.class;
    }

    public KafkaTaskQueueDetailsPb.Builder toProto() {
        KafkaTaskQueueDetailsPb.Builder out = KafkaTaskQueueDetailsPb.newBuilder();
        out.setConsumerGroupId(consumerGroupId);
        out.setTopic(topic);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        KafkaTaskQueueDetailsPbOrBuilder p = (KafkaTaskQueueDetailsPbOrBuilder) proto;
        topic = p.getTopic();
        consumerGroupId = p.getConsumerGroupId();
    }

    public static KafkaTaskQueueDetails fromProto(
        KafkaTaskQueueDetailsPbOrBuilder p
    ) {
        KafkaTaskQueueDetails out = new KafkaTaskQueueDetails();
        out.initFrom(p);
        return out;
    }

    public KafkaTaskQueueDetails() {}

    public KafkaTaskQueueDetails(TaskDef taskDef, LHConfig config) {
        topic = config.getKafkaTopicPrefix() + "task-queue-" + taskDef.name;
        consumerGroupId =
            config.getKafkaTopicPrefix() + "task-worker-" + taskDef.name;
    }
}
