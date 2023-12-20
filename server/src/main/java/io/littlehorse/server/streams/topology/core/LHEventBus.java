package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.littlehorse.sdk.common.proto.TaskDef;
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

    public static LHEvent newEvent(WfSpecIdModel wfSpecId, String tenantId, LHStatus previousStatus, LHStatus newStatus) {
        return new LHWfRunEvent(wfSpecId, tenantId, previousStatus, newStatus);
    }

    @Getter
    public static class LHEvent {

        private final WfSpecIdModel wfSPecId;
        private final Date creationDate;
        private final String tenantId;

        public LHEvent(WfSpecIdModel wfSPecId, String tenantId) {
            this.creationDate = new Date();
            this.wfSPecId = wfSPecId;
            this.tenantId = tenantId;
        }
    }

    @Getter
    public static class LHWfRunEvent extends LHEvent {
        private final LHStatus previousStatus;
        private final LHStatus newStatus;

        public LHWfRunEvent(WfSpecIdModel wfSPecId, String tenantId, LHStatus previousStatus, LHStatus newStatus) {
            super(wfSPecId, tenantId);
            this.previousStatus = previousStatus;
            this.newStatus = Objects.requireNonNull(newStatus);
        }
    }

    public interface Subscriber {
        void listen(LHEvent event);
    }
}
