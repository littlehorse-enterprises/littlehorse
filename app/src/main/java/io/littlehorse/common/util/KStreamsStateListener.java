package io.littlehorse.common.util;

import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.KafkaStreams.StateListener;

public class KStreamsStateListener implements StateListener {
    private State state;

    public KStreamsStateListener() {
        this.state = State.CREATED;
    }

    public void onChange(State newState, State oldState) {
        this.state = newState;
    }

    public State getState() {
        return this.state;
    }
}
