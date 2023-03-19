package io.littlehorse.server.streamsimpl.searchutils.publicsearchreplies;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.jlib.common.proto.ExternalEventIdPb;
import io.littlehorse.jlib.common.proto.SearchExternalEventReplyPb;
import io.littlehorse.jlib.common.proto.SearchExternalEventReplyPbOrBuilder;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearchReply;

public class SearchExternalEventReply
    extends LHPublicSearchReply<SearchExternalEventReplyPb> {

    public Class<SearchExternalEventReplyPb> getProtoBaseClass() {
        return SearchExternalEventReplyPb.class;
    }

    public SearchExternalEventReplyPb.Builder toProto() {
        SearchExternalEventReplyPb.Builder out = SearchExternalEventReplyPb
            .newBuilder()
            .setCode(code);
        if (message != null) out.setMessage(message);
        if (bookmark != null) out.setBookmark(ByteString.copyFrom(bookmark));

        for (String objectId : objectIds) {
            out.addIds(ExternalEvent.parseId(objectId));
        }

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        SearchExternalEventReplyPbOrBuilder p = (SearchExternalEventReplyPbOrBuilder) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasBookmark()) bookmark = p.getBookmark().toByteArray();

        for (ExternalEventIdPb id : p.getIdsList()) {
            objectIds.add(ExternalEvent.getObjectId(id));
        }
    }
}
