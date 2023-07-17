package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.UserTaskRunId;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.UserTaskRunIdPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchUserTaskRunReply
    extends PublicScanReply<SearchUserTaskRunReplyPb, UserTaskRunIdPb, UserTaskRunId> {

    public Class<SearchUserTaskRunReplyPb> getProtoBaseClass() {
        return SearchUserTaskRunReplyPb.class;
    }

    public Class<UserTaskRunIdPb> getResultProtoClass() {
        return UserTaskRunIdPb.class;
    }

    public Class<UserTaskRunId> getResultJavaClass() {
        return UserTaskRunId.class;
    }
}
