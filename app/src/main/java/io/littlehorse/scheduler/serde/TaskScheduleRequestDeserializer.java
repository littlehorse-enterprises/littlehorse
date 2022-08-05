package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.proto.scheduler.TaskScheduleRequestPb;

public class TaskScheduleRequestDeserializer
implements Deserializer<TaskScheduleRequest> {

    public TaskScheduleRequest deserialize(String topic, byte[] bytes) {
        try {
            TaskScheduleRequestPb proto = TaskScheduleRequestPb.parseFrom(bytes);
            return TaskScheduleRequest.fromProto(proto);
        } catch(InvalidProtocolBufferException exn) {
            throw new RuntimeException(exn);
        }
    }

}
