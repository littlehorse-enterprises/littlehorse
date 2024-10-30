package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.sdk.common.proto.WorkflowEventDefId;
import io.littlehorse.sdk.common.proto.WorkflowEventDefIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchWorkflowEventDefReply
        extends PublicScanReply<WorkflowEventDefIdList, WorkflowEventDefId, WorkflowEventDefIdModel> {

    public Class<WorkflowEventDefIdList> getProtoBaseClass() {
        return WorkflowEventDefIdList.class;
    }

    public Class<WorkflowEventDefIdModel> getResultJavaClass() {
        return WorkflowEventDefIdModel.class;
    }

    public Class<WorkflowEventDefId> getResultProtoClass() {
        return WorkflowEventDefId.class;
    }
}
