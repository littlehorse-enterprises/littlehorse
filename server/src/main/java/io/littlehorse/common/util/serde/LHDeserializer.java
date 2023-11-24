package io.littlehorse.common.util.serde;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.binarylog.v1.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.ByteBuffer;

@Slf4j
public class LHDeserializer<T extends LHSerializable<?>> implements Deserializer<T> {

    private Class<T> cls;

    // When we do encryption, we'll need to inject the LHConfig object for
    // access to the encryption keys.
    public LHDeserializer(Class<T> cls) {
        this.cls = cls;
    }

    public T deserialize(String topic, byte[] b) {
        if (b == null) return null;
        try {
            return LHSerializable.fromBytes(b, cls, null); // TODO eduwer
        } catch (LHSerdeError exn) {
            log.error("Caught and re-throwing exception from deserializer.", exn);
            throw new RuntimeException(exn);
        }
    }
}
