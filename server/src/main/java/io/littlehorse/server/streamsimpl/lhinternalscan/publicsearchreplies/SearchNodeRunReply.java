package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.NodeRunIdModel;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.SearchNodeRunResponse;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchNodeRunReply extends PublicScanReply<SearchNodeRunResponse, NodeRunId, NodeRunIdModel> {

    public Class<SearchNodeRunResponse> getProtoBaseClass() {
        return SearchNodeRunResponse.class;
    }

    public Class<NodeRunId> getResultProtoClass() {
        return NodeRunId.class;
    }

    public Class<NodeRunIdModel> getResultJavaClass() {
        return NodeRunIdModel.class;
    }
}
