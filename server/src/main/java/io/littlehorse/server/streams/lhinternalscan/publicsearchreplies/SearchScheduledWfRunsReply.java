package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.ScheduledWfRunIdModel;
import io.littlehorse.sdk.common.proto.ScheduledWfRunId;
import io.littlehorse.sdk.common.proto.ScheduledWfRunIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchScheduledWfRunsReply
        extends PublicScanReply<ScheduledWfRunIdList, ScheduledWfRunId, ScheduledWfRunIdModel> {

    public Class<ScheduledWfRunIdModel> getResultJavaClass() {
        return ScheduledWfRunIdModel.class;
    }

    public Class<ScheduledWfRunId> getResultProtoClass() {
        return ScheduledWfRunId.class;
    }

    public Class<ScheduledWfRunIdList> getProtoBaseClass() {
        return ScheduledWfRunIdList.class;
    }
}
