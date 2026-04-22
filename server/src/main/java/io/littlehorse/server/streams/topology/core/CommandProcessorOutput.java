package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.LHSerializable;

public class CommandProcessorOutput implements Forwardable {

    public String topic;
    public LHSerializable<?> payload;
    public String partitionKey;

    public CommandProcessorOutput() {}

    public CommandProcessorOutput(String topic, LHSerializable<?> payload, String partitionKey) {
        this.topic = topic;
        this.payload = payload;
        this.partitionKey = partitionKey;
    }

    public void setTopic(final String topic) {
        this.topic = topic;
    }

    public void setPayload(final LHSerializable<?> payload) {
        this.payload = payload;
    }

    public void setPartitionKey(final String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public String getTopic() {
        return this.topic;
    }

    public LHSerializable<?> getPayload() {
        return this.payload;
    }

    public String getPartitionKey() {
        return this.partitionKey;
    }
}
