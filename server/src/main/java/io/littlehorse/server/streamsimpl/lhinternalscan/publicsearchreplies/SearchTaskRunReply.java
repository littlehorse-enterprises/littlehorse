package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.TaskRunId;
import io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.TaskRunIdPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchTaskRunReply
    extends PublicScanReply<SearchTaskRunReplyPb, TaskRunIdPb, TaskRunId> {

    public Class<SearchTaskRunReplyPb> getProtoBaseClass() {
        return SearchTaskRunReplyPb.class;
    }

    public Class<TaskRunIdPb> getResultProtoClass() {
        return TaskRunIdPb.class;
    }

    public Class<TaskRunId> getResultJavaClass() {
        return TaskRunId.class;
    }
}
