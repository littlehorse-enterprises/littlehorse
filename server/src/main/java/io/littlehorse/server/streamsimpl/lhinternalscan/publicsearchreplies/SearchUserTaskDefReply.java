package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.UserTaskDefId;
import io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb;
import io.littlehorse.sdk.common.proto.UserTaskDefIdPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchUserTaskDefReply
    extends PublicScanReply<SearchUserTaskDefReplyPb, UserTaskDefIdPb, UserTaskDefId> {

    public Class<SearchUserTaskDefReplyPb> getProtoBaseClass() {
        return SearchUserTaskDefReplyPb.class;
    }

    public Class<UserTaskDefId> getResultJavaClass() {
        return UserTaskDefId.class;
    }

    public Class<UserTaskDefIdPb> getResultProtoClass() {
        return UserTaskDefIdPb.class;
    }
}
