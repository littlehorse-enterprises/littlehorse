package io.littlehorse.server.streamsbackend.coreserver;

import io.littlehorse.common.model.LHSerializable;

public class CoreServerProcessorOutput {

    public String topic;
    public LHSerializable<?> payload;
    public String partitionKey;

    public CoreServerProcessorOutput() {}

    public CoreServerProcessorOutput(
        String topic,
        LHSerializable<?> payload,
        String partitionKey
    ) {
        this.topic = topic;
        this.payload = payload;
        this.partitionKey = partitionKey;
    }
}
