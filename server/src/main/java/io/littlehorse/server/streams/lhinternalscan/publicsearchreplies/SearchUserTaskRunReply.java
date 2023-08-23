package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchUserTaskRunReply extends PublicScanReply<UserTaskRunIdList, UserTaskRunId, UserTaskRunIdModel> {

    public Class<UserTaskRunIdList> getProtoBaseClass() {
        return UserTaskRunIdList.class;
    }

    public Class<UserTaskRunId> getResultProtoClass() {
        return UserTaskRunId.class;
    }

    public Class<UserTaskRunIdModel> getResultJavaClass() {
        return UserTaskRunIdModel.class;
    }
}
