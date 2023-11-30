package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.LHSerializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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
}
