package io.littlehorse.common.model.observability;

import java.util.ArrayList;
import java.util.List;
import io.littlehorse.common.proto.observability.ObservabilityEventsPb;

public class ObservabilityEvents {
    public List<ObservabilityEvent> events;
    public String wfRunId;

    public ObservabilityEvents() {
        events = new ArrayList<>();
    }

    public void add(ObservabilityEvent event) {
        events.add(event);
    }

    public ObservabilityEventsPb.Builder toProtoBuilder() {
        ObservabilityEventsPb.Builder out = ObservabilityEventsPb.newBuilder()
            .setWfRunId(wfRunId);

        for (ObservabilityEvent e: events) {
            out.addEvents(e.toProto());
        }

        return out;
    }
}
