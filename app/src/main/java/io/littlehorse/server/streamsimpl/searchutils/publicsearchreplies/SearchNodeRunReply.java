package io.littlehorse.server.streamsimpl.searchutils.publicsearchreplies;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.jlib.common.proto.NodeRunIdPb;
import io.littlehorse.jlib.common.proto.SearchNodeRunReplyPb;
import io.littlehorse.jlib.common.proto.SearchNodeRunReplyPbOrBuilder;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearchReply;

public class SearchNodeRunReply extends LHPublicSearchReply<SearchNodeRunReplyPb> {

    public Class<SearchNodeRunReplyPb> getProtoBaseClass() {
        return SearchNodeRunReplyPb.class;
    }

    public SearchNodeRunReplyPb.Builder toProto() {
        SearchNodeRunReplyPb.Builder out = SearchNodeRunReplyPb
            .newBuilder()
            .setCode(code);
        if (message != null) out.setMessage(message);
        if (bookmark != null) out.setBookmark(ByteString.copyFrom(bookmark));

        for (String objectId : objectIds) {
            out.addIds(NodeRun.parseId(objectId));
        }

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        SearchNodeRunReplyPbOrBuilder p = (SearchNodeRunReplyPbOrBuilder) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasBookmark()) bookmark = p.getBookmark().toByteArray();

        for (NodeRunIdPb id : p.getIdsList()) {
            objectIds.add(NodeRun.getObjectId(id));
        }
    }
}
