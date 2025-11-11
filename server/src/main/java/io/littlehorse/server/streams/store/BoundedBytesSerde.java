package io.littlehorse.server.streams.store;

import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;

/**
 * A specialized Serde implementation for handling byte arrays with a size constraint.
 */
public class BoundedBytesSerde extends Serdes.WrapperSerde<Bytes> {

    /**
     * Creates a new BoundedBytesSerde with a specified maximum length constraint.
     *
     * @param maxLength The maximum allowed length in bytes for serialized data.
     *                  If the serialized data exceeds this limit, a RecordTooLargeException will be thrown.
     */
    public BoundedBytesSerde(final int maxLength) {
        super(new BoundedBytesSerializer(maxLength), new BytesDeserializer());
    }

    private static class BoundedBytesSerializer extends BytesSerializer {

        private final int maxLength;

        private BoundedBytesSerializer(final int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public byte[] serialize(String topic, Bytes data) {
            byte[] result = super.serialize(topic, data);
            if (result.length > maxLength) {
                throw new RecordTooLargeException("Data too large");
            }
            return result;
        }
    }
}
