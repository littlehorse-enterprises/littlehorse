package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

/**
 * Hold information about new events related to WfRuns
 */
public class LHEventBus {

    private final List<Subscriber> subscribers = new ArrayList<>();

    LHEventBus() {}

    public void dispatch(LHEvent event) {
        subscribers.forEach(subscriber -> subscriber.listen(event));
    }

    public void subscribe(Subscriber subscriber) {
        this.subscribers.add(subscriber);
    }

    public static LHEvent newEvent(WfSpecIdModel wfSpecId, LHStatus previousStatus, LHStatus newStatus) {
        return new LHWfRunEvent(wfSpecId.getName(), wfSpecId.getMajorVersion(), previousStatus, newStatus);
    }

    @Getter
    public static class LHEvent {

        private final String wfSpecName;
        private final int wfSpecVersion;
        private final Date creationDate;

        public LHEvent(String wfSpecName, int wfSpecVersion) {
            this.creationDate = new Date();
            this.wfSpecName = Objects.requireNonNull(wfSpecName);
            this.wfSpecVersion = wfSpecVersion;
        }
    }

    @Getter
    public static class LHWfRunEvent extends LHEvent {
        private final LHStatus previousStatus;
        private final LHStatus newStatus;

        public LHWfRunEvent(String wfSpecName, int wfSpecVersion, LHStatus previousStatus, LHStatus newStatus) {
            super(wfSpecName, wfSpecVersion);
            this.previousStatus = previousStatus;
            this.newStatus = Objects.requireNonNull(newStatus);
        }
    }

    public interface Subscriber {
        void listen(LHEvent event);
    }
}
