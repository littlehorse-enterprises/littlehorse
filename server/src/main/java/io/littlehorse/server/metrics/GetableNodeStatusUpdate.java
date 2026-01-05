package io.littlehorse.server.metrics;

import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import lombok.Getter;

@Getter
public abstract class GetableNodeStatusUpdate extends GetableStatusUpdate {

    private final NodeRunIdModel nodeRunId;

    public GetableNodeStatusUpdate(NodeRunIdModel nodeRunId) {
        this.nodeRunId = nodeRunId;
    }
}
