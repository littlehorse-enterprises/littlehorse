package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class ListTaskRunsReply extends PublicScanReply<TaskRunList, TaskRun, TaskRunModel> {

    public Class<TaskRunModel> getResultJavaClass() {
        return TaskRunModel.class;
    }

    public Class<TaskRun> getResultProtoClass() {
        return TaskRun.class;
    }

    public Class<TaskRunList> getProtoBaseClass() {
        return TaskRunList.class;
    }
}
