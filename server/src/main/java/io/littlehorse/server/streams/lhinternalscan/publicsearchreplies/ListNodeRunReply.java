package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class ListNodeRunReply extends PublicScanReply<NodeRunIdList, NodeRun, NodeRunModel> {

    public Class<NodeRunModel> getResultJavaClass() {
        return NodeRunModel.class;
    }

    public Class<NodeRun> getResultProtoClass() {
        return NodeRun.class;
    }

    public Class<NodeRunIdList> getProtoBaseClass() {
        return NodeRunIdList.class;
    }
}
