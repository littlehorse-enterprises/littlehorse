package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.TaskRunIdModel;
import io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchTaskRunReply
    extends PublicScanReply<SearchTaskRunReplyPb, TaskRunId, TaskRunIdModel> {

    public Class<SearchTaskRunReplyPb> getProtoBaseClass() {
        return SearchTaskRunReplyPb.class;
    }

    public Class<TaskRunId> getResultProtoClass() {
        return TaskRunId.class;
    }

    public Class<TaskRunIdModel> getResultJavaClass() {
        return TaskRunIdModel.class;
    }
}
