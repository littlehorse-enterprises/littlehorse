package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.wfspec.WfSpecPb;

public class WfSpecDeserializer implements Deserializer<WfSpec> {
    public WfSpec deserialize(String topic, byte[] data) {
        try {
            // long start = System.nanoTime();
            // WFSpecPb proto = WFSpecPb.parseFrom(data);
            // long protoTime = System.nanoTime();
            // WfSpec out = WfSpec.fromProto(proto);
            // long done = System.nanoTime();

            // System.out.println("proto: " + (protoTime - start));
            // System.out.println("pojo : " + (done - protoTime));

            // return out;
            return WfSpec.fromProto(WfSpecPb.parseFrom(data));
        } catch(InvalidProtocolBufferException exn) {
            throw new RuntimeException(exn);
        }
    }
}
