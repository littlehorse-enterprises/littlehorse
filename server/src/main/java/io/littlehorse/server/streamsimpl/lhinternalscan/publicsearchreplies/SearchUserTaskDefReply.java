package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.UserTaskDefIdModel;
import io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb;
import io.littlehorse.sdk.common.proto.UserTaskDefIdPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchUserTaskDefReply
    extends PublicScanReply<SearchUserTaskDefReplyPb, UserTaskDefIdPb, UserTaskDefIdModel> {

    public Class<SearchUserTaskDefReplyPb> getProtoBaseClass() {
        return SearchUserTaskDefReplyPb.class;
    }

    public Class<UserTaskDefIdModel> getResultJavaClass() {
        return UserTaskDefIdModel.class;
    }

    public Class<UserTaskDefIdPb> getResultProtoClass() {
        return UserTaskDefIdPb.class;
    }
}
