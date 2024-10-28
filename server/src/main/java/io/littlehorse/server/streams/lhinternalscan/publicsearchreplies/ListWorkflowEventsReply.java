package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.sdk.common.proto.WorkflowEventList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class ListWorkflowEventsReply extends PublicScanReply<WorkflowEventList, WorkflowEvent, WorkflowEventModel> {

    public Class<WorkflowEventModel> getResultJavaClass() {
        return WorkflowEventModel.class;
    }

    public Class<WorkflowEvent> getResultProtoClass() {
        return WorkflowEvent.class;
    }

    public Class<WorkflowEventList> getProtoBaseClass() {
        return WorkflowEventList.class;
    }
}
