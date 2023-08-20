package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.UserTaskDefIdModel;
import io.littlehorse.sdk.common.proto.SearchUserTaskDefResponse;
import io.littlehorse.sdk.common.proto.UserTaskDefId;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchUserTaskDefReply
        extends PublicScanReply<SearchUserTaskDefResponse, UserTaskDefId, UserTaskDefIdModel> {

    public Class<SearchUserTaskDefResponse> getProtoBaseClass() {
        return SearchUserTaskDefResponse.class;
    }

    public Class<UserTaskDefIdModel> getResultJavaClass() {
        return UserTaskDefIdModel.class;
    }

    public Class<UserTaskDefId> getResultProtoClass() {
        return UserTaskDefId.class;
    }
}
