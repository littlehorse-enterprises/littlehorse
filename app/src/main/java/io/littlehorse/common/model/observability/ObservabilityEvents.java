package io.littlehorse.common.model.observability;

import java.util.ArrayList;
import java.util.List;
import io.littlehorse.common.proto.ObservabilityEventsPb;

public class ObservabilityEvents {
    public List<ObservabilityEvent> events;
    public String wfRunId;
    
    public ObservabilityEvents() {
        events = new ArrayList<>();
    }

    public ObservabilityEventsPb.Builder toProtoBuilder() {
        ObservabilityEventsPb.Builder out = ObservabilityEventsPb.newBuilder()
            .setWfRunId(wfRunId);

        for (ObservabilityEvent e: events) {
            out.addEvents(e.toProtoBuilder());
        }

        return out;
    }
}
