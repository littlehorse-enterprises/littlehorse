package io.littlehorse.server.streamsbackend.coreserver;

import io.littlehorse.common.model.LHSerializable;

public class CommandProcessorOutput {

    public String topic;
    public LHSerializable<?> payload;
    public String partitionKey;

    public CommandProcessorOutput() {}

    public CommandProcessorOutput(
        String topic,
        LHSerializable<?> payload,
        String partitionKey
    ) {
        this.topic = topic;
        this.payload = payload;
        this.partitionKey = partitionKey;
    }
}
