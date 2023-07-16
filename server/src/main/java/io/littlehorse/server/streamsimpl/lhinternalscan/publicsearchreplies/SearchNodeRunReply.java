package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.NodeRunId;
import io.littlehorse.sdk.common.proto.NodeRunIdPb;
import io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchNodeRunReply
    extends PublicScanReply<SearchNodeRunReplyPb, NodeRunIdPb, NodeRunId> {

    public Class<SearchNodeRunReplyPb> getProtoBaseClass() {
        return SearchNodeRunReplyPb.class;
    }

    public Class<NodeRunIdPb> getResultProtoClass() {
        return NodeRunIdPb.class;
    }

    public Class<NodeRunId> getResultJavaClass() {
        return NodeRunId.class;
    }
}
