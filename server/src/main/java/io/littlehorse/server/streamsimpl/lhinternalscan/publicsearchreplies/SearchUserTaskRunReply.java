package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchUserTaskRunReply
    extends PublicScanReply<SearchUserTaskRunReplyPb, UserTaskRunId, UserTaskRunIdModel> {

    public Class<SearchUserTaskRunReplyPb> getProtoBaseClass() {
        return SearchUserTaskRunReplyPb.class;
    }

    public Class<UserTaskRunId> getResultProtoClass() {
        return UserTaskRunId.class;
    }

    public Class<UserTaskRunIdModel> getResultJavaClass() {
        return UserTaskRunIdModel.class;
    }
}
