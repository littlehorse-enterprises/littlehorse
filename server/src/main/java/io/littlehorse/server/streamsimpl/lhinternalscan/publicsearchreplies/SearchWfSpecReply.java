package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb;
import io.littlehorse.sdk.common.proto.WfSpecIdPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchWfSpecReply
    extends PublicScanReply<SearchWfSpecReplyPb, WfSpecIdPb, WfSpecId> {

    public Class<SearchWfSpecReplyPb> getProtoBaseClass() {
        return SearchWfSpecReplyPb.class;
    }

    public Class<WfSpecId> getResultJavaClass() {
        return WfSpecId.class;
    }

    public Class<WfSpecIdPb> getResultProtoClass() {
        return WfSpecIdPb.class;
    }
    // public SearchWfSpecReplyPb.Builder toProto() {
    //     SearchWfSpecReplyPb.Builder out = SearchWfSpecReplyPb
    //         .newBuilder()
    //         .setCode(code);
    //     if (message != null) out.setMessage(message);
    //     if (bookmark != null) out.setBookmark(ByteString.copyFrom(bookmark));

    //     for (String objectId : objectIds) {
    //         out.addIds(WfSpec.parseId(objectId));
    //     }

    //     return out;
    // }

    // public void initFrom(Message proto) {
    //     SearchWfSpecReplyPb p = (SearchWfSpecReplyPb) proto;
    //     code = p.getCode();
    //     if (p.hasMessage()) message = p.getMessage();
    //     if (p.hasBookmark()) bookmark = p.getBookmark().toByteArray();

    //     for (WfSpecIdPb id : p.getIdsList()) {
    //         objectIds.add(WfSpec.getObjectId(id));
    //     }
    // }
}
