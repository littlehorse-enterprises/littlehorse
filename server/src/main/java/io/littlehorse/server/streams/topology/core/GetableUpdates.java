package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskStatus;
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

    public static GetableStatusUpdate create(
            TaskDefId taskDefId, TenantIdModel tenantId, TaskStatus previousStatus, TaskStatus newStatus) {
        return new TaskStatusUpdate(taskDefId, tenantId, previousStatus, newStatus);
    }

    @Getter
    public static class GetableStatusUpdate {

        private final Date creationDate;
        private final TenantIdModel tenantId;

        public GetableStatusUpdate(TenantIdModel tenantId) {
            this.creationDate = new Date();
            this.tenantId = tenantId;
        }
    }

    @Getter
    public static class WfRunStatusUpdate extends GetableStatusUpdate {
        private final LHStatus previousStatus;
        private final LHStatus newStatus;
        private final WfSpecIdModel wfSpecId;


        public WfRunStatusUpdate(WfSpecIdModel wfSpecId, TenantIdModel tenantId, LHStatus previousStatus, LHStatus newStatus) {
            super(tenantId);
            this.previousStatus = previousStatus;
            this.wfSpecId = wfSpecId;
            this.newStatus = Objects.requireNonNull(newStatus);
        }
    }

    public static class TaskStatusUpdate extends GetableStatusUpdate {
        private final TaskStatus previousStatus;
        private final TaskStatus newStatus;
        private final TaskDefId taskDefId;
        public TaskStatusUpdate(TaskDefId taskDefId, TenantIdModel tenantId, TaskStatus previousStatus, TaskStatus newStatus) {
            super(tenantId);
            this.previousStatus = previousStatus;
            this.newStatus = newStatus;
            this.taskDefId = taskDefId;
        }
    }

    public interface GetableStatusListener {
        void listen(GetableStatusUpdate event);
    }
}
