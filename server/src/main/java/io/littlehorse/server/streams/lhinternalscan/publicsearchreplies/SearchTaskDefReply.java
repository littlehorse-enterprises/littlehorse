package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskDefIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchTaskDefReply extends PublicScanReply<TaskDefIdList, TaskDefId, TaskDefIdModel> {

    public Class<TaskDefIdList> getProtoBaseClass() {
        return TaskDefIdList.class;
    }

    public Class<TaskDefIdModel> getResultJavaClass() {
        return TaskDefIdModel.class;
    }

    public Class<TaskDefId> getResultProtoClass() {
        return TaskDefId.class;
    }
}
