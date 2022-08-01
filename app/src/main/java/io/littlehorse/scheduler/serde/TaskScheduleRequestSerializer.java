package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.event.TaskScheduleRequest;

public class TaskScheduleRequestSerializer implements Serializer<TaskScheduleRequest> {
    public byte[] serialize(String topic, TaskScheduleRequest evt) {
        return evt.toProtoBuilder().build().toByteArray();
    }
}
