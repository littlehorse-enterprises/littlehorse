package io.littlehorse.common.util.kstreamlisteners;

import java.util.Date;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.KafkaStreams.StateListener;
import io.littlehorse.common.util.LHUtil;

public class KStreamsStateListener implements StateListener {
    private State state;

    public KStreamsStateListener() {
        this.state = State.CREATED;
    }

    public void onChange(State newState, State oldState) {
        this.state = newState;
        LHUtil.log(new Date(), "New state: ", newState);
    }

    public State getState() {
        return this.state;
    }
}
