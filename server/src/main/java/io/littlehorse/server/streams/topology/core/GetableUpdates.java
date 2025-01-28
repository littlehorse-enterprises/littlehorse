package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskStatus;
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
            WfSpecIdModel wfSpecId, TenantIdModel tenantId, LHStatus previousStatus, LHStatus newStatus) {
        return new WfRunStatusUpdate(wfSpecId, tenantId, previousStatus, newStatus);
    }

    public static GetableStatusUpdate createEndEvent(
            WfSpecIdModel wfSpecId,
            TenantIdModel tenantId,
            LHStatus previousStatus,
            LHStatus newStatus,
            Date wfRunStartDate) {
        return new WfRunStatusUpdate(wfSpecId, tenantId, previousStatus, newStatus, wfRunStartDate);
    }

    public static TaskRunStatusUpdate create(
            TaskDefIdModel taskDefId, TenantIdModel tenantId, TaskStatus previousStatus, TaskStatus newStatus) {
        return new TaskRunStatusUpdate(taskDefId, tenantId, previousStatus, newStatus);
    }

    @Getter
    public static class GetableStatusUpdate {

        private final Date creationDate;
        private final TenantIdModel tenantId;
        private final long firstEventLatency;

        public GetableStatusUpdate(TenantIdModel tenantId) {
            this.creationDate = new Date();
            this.tenantId = tenantId;
            this.firstEventLatency = 0;
        }

        public GetableStatusUpdate(TenantIdModel tenantId, Date firstEventDate) {
            this.creationDate = new Date();
            this.tenantId = tenantId;
            this.firstEventLatency = System.currentTimeMillis() - firstEventDate.getTime();
        }
    }

    @Getter
    public static class WfRunStatusUpdate extends GetableStatusUpdate {
        private final LHStatus previousStatus;
        private final LHStatus newStatus;
        private final WfSpecIdModel wfSpecId;

        public WfRunStatusUpdate(
                WfSpecIdModel wfSpecId, TenantIdModel tenantId, LHStatus previousStatus, LHStatus newStatus) {
            super(tenantId);
            this.previousStatus = previousStatus;
            this.wfSpecId = wfSpecId;
            this.newStatus = Objects.requireNonNull(newStatus);
        }

        public WfRunStatusUpdate(
                WfSpecIdModel wfSpecId,
                TenantIdModel tenantId,
                LHStatus previousStatus,
                LHStatus newStatus,
                Date wfRunStartDate) {
            super(tenantId, wfRunStartDate);
            this.previousStatus = previousStatus;
            this.wfSpecId = wfSpecId;
            this.newStatus = Objects.requireNonNull(newStatus);
        }
    }

    @Getter
    public static class TaskRunStatusUpdate extends GetableStatusUpdate {
        private final TaskDefIdModel taskDefId;
        private final TaskStatus previousStatus;
        private final TaskStatus newStatus;

        public TaskRunStatusUpdate(
                TaskDefIdModel taskDefId, TenantIdModel tenantId, TaskStatus previousStatus, TaskStatus newStatus) {
            super(tenantId);
            this.taskDefId = taskDefId;
            this.previousStatus = previousStatus;
            this.newStatus = newStatus;
        }
    }

    public interface GetableStatusListener {
        void listen(GetableStatusUpdate event);
    }
}
