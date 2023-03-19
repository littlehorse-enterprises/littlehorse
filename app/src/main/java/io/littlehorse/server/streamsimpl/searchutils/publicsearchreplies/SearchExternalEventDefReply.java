package io.littlehorse.server.streamsimpl.searchutils.publicsearchreplies;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.jlib.common.proto.ExternalEventDefIdPb;
import io.littlehorse.jlib.common.proto.SearchExternalEventDefReplyPb;
import io.littlehorse.jlib.common.proto.SearchExternalEventDefReplyPbOrBuilder;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearchReply;

public class SearchExternalEventDefReply
    extends LHPublicSearchReply<SearchExternalEventDefReplyPb> {

    public Class<SearchExternalEventDefReplyPb> getProtoBaseClass() {
        return SearchExternalEventDefReplyPb.class;
    }

    public SearchExternalEventDefReplyPb.Builder toProto() {
        SearchExternalEventDefReplyPb.Builder out = SearchExternalEventDefReplyPb
            .newBuilder()
            .setCode(code);
        if (message != null) out.setMessage(message);
        if (bookmark != null) out.setBookmark(ByteString.copyFrom(bookmark));

        for (String objectId : objectIds) {
            out.addIds(ExternalEventDef.parseId(objectId));
        }

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        SearchExternalEventDefReplyPbOrBuilder p = (SearchExternalEventDefReplyPbOrBuilder) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasBookmark()) bookmark = p.getBookmark().toByteArray();

        for (ExternalEventDefIdPb id : p.getIdsList()) {
            objectIds.add(ExternalEventDef.getObjectId(id));
        }
    }
}
