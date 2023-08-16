package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.UserTaskDefIdModel;
import io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb;
import io.littlehorse.sdk.common.proto.UserTaskDefId;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchUserTaskDefReply
    extends PublicScanReply<SearchUserTaskDefReplyPb, UserTaskDefId, UserTaskDefIdModel> {

    public Class<SearchUserTaskDefReplyPb> getProtoBaseClass() {
        return SearchUserTaskDefReplyPb.class;
    }

    public Class<UserTaskDefIdModel> getResultJavaClass() {
        return UserTaskDefIdModel.class;
    }

    public Class<UserTaskDefId> getResultProtoClass() {
        return UserTaskDefId.class;
    }
}
