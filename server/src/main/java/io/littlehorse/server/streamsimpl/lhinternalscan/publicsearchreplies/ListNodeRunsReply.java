package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.wfrun.NodeRunModel;
import io.littlehorse.sdk.common.proto.ListNodeRunsResponse;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class ListNodeRunsReply
    extends PublicScanReply<ListNodeRunsResponse, NodeRun, NodeRunModel> {

    public Class<NodeRunModel> getResultJavaClass() {
        return NodeRunModel.class;
    }

    public Class<NodeRun> getResultProtoClass() {
        return NodeRun.class;
    }

    public Class<ListNodeRunsResponse> getProtoBaseClass() {
        return ListNodeRunsResponse.class;
    }
}
