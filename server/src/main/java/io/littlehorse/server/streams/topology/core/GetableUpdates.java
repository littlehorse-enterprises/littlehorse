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
public class GetableUpdates {

    private final List<GetableStatusListener> getableStatusListeners = new ArrayList<>();

    GetableUpdates() {}

    public void dispatch(GetableStatusUpdate statusUpdate) {
        getableStatusListeners.forEach(getableStatusListener -> getableStatusListener.listen(statusUpdate));
    }

    public void subscribe(GetableStatusListener getableStatusListener) {
        this.getableStatusListeners.add(getableStatusListener);
    }

    public static GetableStatusUpdate create(
            WfSpecIdModel wfSpecId, String tenantId, LHStatus previousStatus, LHStatus newStatus) {
        return new WfRunStatusUpdate(wfSpecId, tenantId, previousStatus, newStatus);
    }

    @Getter
    public static class GetableStatusUpdate {

        private final WfSpecIdModel wfSPecId;
        private final Date creationDate;
        private final String tenantId;

        public GetableStatusUpdate(WfSpecIdModel wfSPecId, String tenantId) {
            this.creationDate = new Date();
            this.wfSPecId = wfSPecId;
            this.tenantId = tenantId;
        }
    }

    @Getter
    public static class WfRunStatusUpdate extends GetableStatusUpdate {
        private final LHStatus previousStatus;
        private final LHStatus newStatus;

        public WfRunStatusUpdate(WfSpecIdModel wfSPecId, String tenantId, LHStatus previousStatus, LHStatus newStatus) {
            super(wfSPecId, tenantId);
            this.previousStatus = previousStatus;
            this.newStatus = Objects.requireNonNull(newStatus);
        }
    }

    public interface GetableStatusListener {
        void listen(GetableStatusUpdate event);
    }
}
