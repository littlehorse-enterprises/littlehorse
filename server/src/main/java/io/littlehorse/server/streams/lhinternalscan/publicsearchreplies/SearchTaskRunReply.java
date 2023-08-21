package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.TaskRunIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchTaskRunReply extends PublicScanReply<TaskRunIdList, TaskRunId, TaskRunIdModel> {

    public Class<TaskRunIdList> getProtoBaseClass() {
        return TaskRunIdList.class;
    }

    public Class<TaskRunId> getResultProtoClass() {
        return TaskRunId.class;
    }

    public Class<TaskRunIdModel> getResultJavaClass() {
        return TaskRunIdModel.class;
    }
}
