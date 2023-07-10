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
    // public SearchNodeRunReplyPb.Builder toProto() {
    //     SearchNodeRunReplyPb.Builder out = SearchNodeRunReplyPb
    //         .newBuilder()
    //         .setCode(code);
    //     if (message != null) out.setMessage(message);
    //     if (bookmark != null) out.setBookmark(ByteString.copyFrom(bookmark));

    //     for (NodeRunId) {
    //         out.addIds(NodeRun.parseId(objectId));
    //     }

    //     return out;
    // }

    // public void initFrom(Message proto) {
    //     SearchNodeRunReplyPb p = (SearchNodeRunReplyPb) proto;
    //     code = p.getCode();
    //     if (p.hasMessage()) message = p.getMessage();
    //     if (p.hasBookmark()) bookmark = p.getBookmark().toByteArray();

    //     for (NodeRunIdPb id : p.getIdsList()) {
    //         objectIds.add(NodeRun.getObjectId(id));
    //     }
    // }
}
