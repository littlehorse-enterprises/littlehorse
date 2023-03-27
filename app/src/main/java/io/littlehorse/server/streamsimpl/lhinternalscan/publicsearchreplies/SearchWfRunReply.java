package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.WfRunId;
import io.littlehorse.jlib.common.proto.SearchWfRunReplyPb;
import io.littlehorse.jlib.common.proto.WfRunIdPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchWfRunReply
    extends PublicScanReply<SearchWfRunReplyPb, WfRunIdPb, WfRunId> {

    public Class<SearchWfRunReplyPb> getProtoBaseClass() {
        return SearchWfRunReplyPb.class;
    }

    public Class<WfRunId> getResultJavaClass() {
        return WfRunId.class;
    }

    public Class<WfRunIdPb> getResultProtoClass() {
        return WfRunIdPb.class;
    }
    // public SearchWfRunReplyPb.Builder toProto() {
    //     SearchWfRunReplyPb.Builder out = SearchWfRunReplyPb
    //         .newBuilder()
    //         .setCode(code);
    //     if (message != null) out.setMessage(message);
    //     if (bookmark != null) out.setBookmark(ByteString.copyFrom(bookmark));

    //     for (String objectId : objectIds) {
    //         out.addIds(WfRun.parseId(objectId));
    //     }

    //     return out;
    // }

    // public void initFrom(Message proto) {
    //     SearchWfRunReplyPb p = (SearchWfRunReplyPb) proto;
    //     code = p.getCode();
    //     if (p.hasMessage()) message = p.getMessage();
    //     if (p.hasBookmark()) bookmark = p.getBookmark().toByteArray();

    //     for (WfRunIdPb id : p.getIdsList()) {
    //         objectIds.add(WfRun.getObjectId(id));
    //     }
    // }
}
