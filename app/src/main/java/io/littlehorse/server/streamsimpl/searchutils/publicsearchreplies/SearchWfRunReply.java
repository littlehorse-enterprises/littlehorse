package io.littlehorse.server.streamsimpl.searchutils.publicsearchreplies;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.jlib.common.proto.SearchWfRunReplyPb;
import io.littlehorse.jlib.common.proto.SearchWfRunReplyPbOrBuilder;
import io.littlehorse.jlib.common.proto.WfRunIdPb;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearchReply;

public class SearchWfRunReply extends LHPublicSearchReply<SearchWfRunReplyPb> {

    public Class<SearchWfRunReplyPb> getProtoBaseClass() {
        return SearchWfRunReplyPb.class;
    }

    public SearchWfRunReplyPb.Builder toProto() {
        SearchWfRunReplyPb.Builder out = SearchWfRunReplyPb
            .newBuilder()
            .setCode(code);
        if (message != null) out.setMessage(message);
        if (bookmark != null) out.setBookmark(ByteString.copyFrom(bookmark));

        for (String objectId : objectIds) {
            out.addIds(WfRun.parseId(objectId));
        }

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        SearchWfRunReplyPbOrBuilder p = (SearchWfRunReplyPbOrBuilder) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasBookmark()) bookmark = p.getBookmark().toByteArray();

        for (WfRunIdPb id : p.getIdsList()) {
            objectIds.add(WfRun.getObjectId(id));
        }
    }
}
