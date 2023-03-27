package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.jlib.common.proto.ListNodeRunsReplyPb;
import io.littlehorse.jlib.common.proto.NodeRunPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class ListNodeRunsReply
    extends PublicScanReply<ListNodeRunsReplyPb, NodeRunPb, NodeRun> {

    public Class<NodeRun> getResultJavaClass() {
        return NodeRun.class;
    }

    public Class<NodeRunPb> getResultProtoClass() {
        return NodeRunPb.class;
    }

    public Class<ListNodeRunsReplyPb> getProtoBaseClass() {
        return ListNodeRunsReplyPb.class;
    }
}
