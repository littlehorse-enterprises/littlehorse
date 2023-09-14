package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class ListUserTaskRunReply extends PublicScanReply<UserTaskRunList, UserTaskRun, UserTaskRunModel> {

    public Class<UserTaskRunModel> getResultJavaClass() {
        return UserTaskRunModel.class;
    }

    public Class<UserTaskRun> getResultProtoClass() {
        return UserTaskRun.class;
    }

    public Class<UserTaskRunList> getProtoBaseClass() {
        return UserTaskRunList.class;
    }
}
