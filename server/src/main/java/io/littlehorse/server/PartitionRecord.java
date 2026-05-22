package io.littlehorse.server;

import io.littlehorse.server.streams.topology.core.Forwardable;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.processor.api.Record;

public class PartitionRecord extends Record<PartitionNumber, byte[]> implements Forwardable {

    public PartitionRecord(int partition, byte[] value, long timestamp, Headers headers) {
        super(new PartitionNumber(partition), value, timestamp, headers);
    }

}
