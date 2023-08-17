package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.ExternalEventIdModel;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchExternalEventReply
    extends PublicScanReply<SearchExternalEventReplyPb, ExternalEventId, ExternalEventIdModel> {

    public Class<SearchExternalEventReplyPb> getProtoBaseClass() {
        return SearchExternalEventReplyPb.class;
    }

    public Class<ExternalEventId> getResultProtoClass() {
        return ExternalEventId.class;
    }

    public Class<ExternalEventIdModel> getResultJavaClass() {
        return ExternalEventIdModel.class;
    }
    // public SearchExternalEventReplyPb.Builder toProto() {
    //     SearchExternalEventReplyPb.Builder out = SearchExternalEventReplyPb
    //         .newBuilder()
    //         .setCode(code);
    //     if (message != null) out.setMessage(message);
    //     if (bookmark != null) out.setBookmark(bookmark);

    //     for (ExternalEventId id : results) {
    //         out.addIds(id.toProto());
    //     }

    //     return out;
    // }

    // public void initFrom(Message proto) {
    //     SearchExternalEventReplyPb p = (SearchExternalEventReplyPb) proto;
    //     code = p.getCode();
    //     if (p.hasMessage()) message = p.getMessage();
    //     if (p.hasBookmark()) bookmark = p.getBookmark();

    //     for (ExternalEventId id : p.getIdsList()) {
    //         results.add(LHSerializable.fromProto(id, ExternalEventId.class));
    //     }
    // }
}
