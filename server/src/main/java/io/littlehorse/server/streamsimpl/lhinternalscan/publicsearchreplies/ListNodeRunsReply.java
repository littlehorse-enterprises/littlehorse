package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.wfrun.NodeRunModel;
import io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class ListNodeRunsReply
    extends PublicScanReply<ListNodeRunsReplyPb, NodeRun, NodeRunModel> {

    public Class<NodeRunModel> getResultJavaClass() {
        return NodeRunModel.class;
    }

    public Class<NodeRun> getResultProtoClass() {
        return NodeRun.class;
    }

    public Class<ListNodeRunsReplyPb> getProtoBaseClass() {
        return ListNodeRunsReplyPb.class;
    }
}
