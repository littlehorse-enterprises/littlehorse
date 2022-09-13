package io.littlehorse.common.model.observability;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.ObservabilityEventPb;
import io.littlehorse.common.proto.ObservabilityEventsPb;
import java.util.ArrayList;
import java.util.List;

public class ObservabilityEvents extends LHSerializable<ObservabilityEventsPb> {

    public List<ObservabilityEvent> events;
    public String wfRunId;

    public ObservabilityEvents() {
        events = new ArrayList<>();
    }

    public void add(ObservabilityEvent event) {
        events.add(event);
    }

    public ObservabilityEventsPb.Builder toProto() {
        ObservabilityEventsPb.Builder out = ObservabilityEventsPb
            .newBuilder()
            .setWfRunId(wfRunId);

        for (ObservabilityEvent e : events) {
            out.addEvents(e.toProto());
        }

        return out;
    }

    public Class<ObservabilityEventsPb> getProtoBaseClass() {
        return ObservabilityEventsPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        ObservabilityEventsPb p = (ObservabilityEventsPb) proto;
        wfRunId = p.getWfRunId();
        for (ObservabilityEventPb oepb : p.getEventsList()) {
            ObservabilityEvent oe = new ObservabilityEvent();
            oe.initFrom(oepb);
            events.add(oe);
        }
    }
}
