package io.littlehorse.server.metrics;

import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * Hold information about new events related to WfRuns
 */
public class GetableUpdates {

    private final Map<NodeRunIdModel, ArrayDeque<GetableStatusUpdate>> getableUpdatesByNodeRun = new HashMap<>();
    private final Map<WfRunIdAndThreadRunNumber, ArrayDeque<GetableStatusUpdate>> getableUpdatesByThreadRunNumber =
            new HashMap<>();
    private final Map<WfRunIdModel, ArrayDeque<GetableStatusUpdate>> getableUpdatesByWfRunId = new HashMap<>();

    public GetableUpdates() {}

    public void append(NodeRunIdModel nodeRunId, GetableStatusUpdate statusUpdate) {
        getableUpdatesByNodeRun
                .computeIfAbsent(nodeRunId, k -> new ArrayDeque<>())
                .add(statusUpdate);
    }

    public void append(WfRunIdModel wfRunId, int threadRunNumber, GetableStatusUpdate statusUpdate) {
        getableUpdatesByThreadRunNumber
                .computeIfAbsent(new WfRunIdAndThreadRunNumber(wfRunId, threadRunNumber), k -> new ArrayDeque<>())
                .add(statusUpdate);
    }

    public void append(WfRunIdModel wfRunId, GetableStatusUpdate statusUpdate) {
        getableUpdatesByWfRunId
                .computeIfAbsent(wfRunId, k -> new ArrayDeque<>())
                .add(statusUpdate);
    }

    private record WfRunIdAndThreadRunNumber(WfRunIdModel wfRunId, int threadRunNumber) {}

    public ArrayDeque<GetableStatusUpdate> getUpdatesForNodeRun(NodeRunIdModel nodeRunId) {
        return getableUpdatesByNodeRun.getOrDefault(nodeRunId, new ArrayDeque<>());
    }

    public ArrayDeque<GetableStatusUpdate> getUpdatesForWfRunId(WfRunIdModel wfRunId) {
        return getableUpdatesByWfRunId.getOrDefault(wfRunId, new ArrayDeque<>());
    }

    public ArrayDeque<GetableStatusUpdate> getUpdatesForThreadRunNumber(WfRunIdModel wfRunId, int threadRunNumber) {
        return getableUpdatesByThreadRunNumber.getOrDefault(
                new WfRunIdAndThreadRunNumber(wfRunId, threadRunNumber), new ArrayDeque<>());
    }

    public static GetableStatusUpdate create(
            WfSpecIdModel wfSpecId, TenantIdModel tenantId, LHStatus previousStatus, LHStatus newStatus) {
        return new WfRunStatusUpdate(wfSpecId, tenantId, previousStatus, newStatus);
    }

    public static GetableStatusUpdate createEndEvent(
            WfSpecIdModel wfSpecId, TenantIdModel tenantId, LHStatus previousStatus, LHStatus newStatus) {
        return new WfRunStatusUpdate(wfSpecId, tenantId, previousStatus, newStatus);
    }
}
