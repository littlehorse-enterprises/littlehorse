package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerGroupModel;
import io.littlehorse.sdk.common.proto.ListTaskWorkerGroupResponse;
import io.littlehorse.sdk.common.proto.TaskWorkerGroup;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class ListTaskWorkerGroupReply
        extends PublicScanReply<ListTaskWorkerGroupResponse, TaskWorkerGroup, TaskWorkerGroupModel> {

    @Override
    public Class<ListTaskWorkerGroupResponse> getProtoBaseClass() {
        return ListTaskWorkerGroupResponse.class;
    }

    @Override
    public Class<TaskWorkerGroup> getResultProtoClass() {
        return TaskWorkerGroup.class;
    }

    @Override
    public Class<TaskWorkerGroupModel> getResultJavaClass() {
        return TaskWorkerGroupModel.class;
    }
}
