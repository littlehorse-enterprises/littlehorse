package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.VariableId;
import io.littlehorse.jlib.common.proto.SearchVariableReplyPb;
import io.littlehorse.jlib.common.proto.VariableIdPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchVariableReply
    extends PublicScanReply<SearchVariableReplyPb, VariableIdPb, VariableId> {

    public Class<SearchVariableReplyPb> getProtoBaseClass() {
        return SearchVariableReplyPb.class;
    }

    public Class<VariableId> getResultJavaClass() {
        return VariableId.class;
    }

    public Class<VariableIdPb> getResultProtoClass() {
        return VariableIdPb.class;
    }
    // public SearchVariableReplyPb.Builder toProto() {
    //     SearchVariableReplyPb.Builder out = SearchVariableReplyPb
    //         .newBuilder()
    //         .setCode(code);
    //     if (message != null) out.setMessage(message);
    //     if (bookmark != null) out.setBookmark(ByteString.copyFrom(bookmark));

    //     for (String objectId : objectIds) {
    //         out.addIds(Variable.parseId(objectId));
    //     }

    //     return out;
    // }

    // public void initFrom(Message proto) {
    //     SearchVariableReplyPb p = (SearchVariableReplyPb) proto;
    //     code = p.getCode();
    //     if (p.hasMessage()) message = p.getMessage();
    //     if (p.hasBookmark()) bookmark = p.getBookmark().toByteArray();

    //     for (VariableIdPb id : p.getIdsList()) {
    //         objectIds.add(Variable.getObjectId(id));
    //     }
    // }
}
