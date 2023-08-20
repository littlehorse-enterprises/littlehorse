package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunResponse;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchUserTaskRunReply
        extends PublicScanReply<SearchUserTaskRunResponse, UserTaskRunId, UserTaskRunIdModel> {

    public Class<SearchUserTaskRunResponse> getProtoBaseClass() {
        return SearchUserTaskRunResponse.class;
    }

    public Class<UserTaskRunId> getResultProtoClass() {
        return UserTaskRunId.class;
    }

    public Class<UserTaskRunIdModel> getResultJavaClass() {
        return UserTaskRunIdModel.class;
    }
}
