package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.SearchWfRunReplyPb;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchWfRunReply
    extends PublicScanReply<SearchWfRunReplyPb, WfRunId, WfRunIdModel> {

    public Class<SearchWfRunReplyPb> getProtoBaseClass() {
        return SearchWfRunReplyPb.class;
    }

    public Class<WfRunIdModel> getResultJavaClass() {
        return WfRunIdModel.class;
    }

    public Class<WfRunId> getResultProtoClass() {
        return WfRunId.class;
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

    //     for (WfRunId id : p.getIdsList()) {
    //         objectIds.add(WfRun.getObjectId(id));
    //     }
    // }
}
