package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.sdk.common.proto.WorkflowEventId;
import io.littlehorse.sdk.common.proto.WorkflowEventIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchWorkflowEventReply
        extends PublicScanReply<WorkflowEventIdList, WorkflowEventId, WorkflowEventIdModel> {

    public Class<WorkflowEventIdList> getProtoBaseClass() {
        return WorkflowEventIdList.class;
    }

    public Class<WorkflowEventIdModel> getResultJavaClass() {
        return WorkflowEventIdModel.class;
    }

    public Class<WorkflowEventId> getResultProtoClass() {
        return WorkflowEventId.class;
    }
}
