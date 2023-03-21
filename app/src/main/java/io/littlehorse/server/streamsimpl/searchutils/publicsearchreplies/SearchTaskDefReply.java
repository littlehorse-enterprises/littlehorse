package io.littlehorse.server.streamsimpl.searchutils.publicsearchreplies;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.jlib.common.proto.SearchTaskDefReplyPb;
import io.littlehorse.jlib.common.proto.SearchTaskDefReplyPbOrBuilder;
import io.littlehorse.jlib.common.proto.TaskDefIdPb;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearchReply;

public class SearchTaskDefReply extends LHPublicSearchReply<SearchTaskDefReplyPb> {

    public Class<SearchTaskDefReplyPb> getProtoBaseClass() {
        return SearchTaskDefReplyPb.class;
    }

    public SearchTaskDefReplyPb.Builder toProto() {
        SearchTaskDefReplyPb.Builder out = SearchTaskDefReplyPb
            .newBuilder()
            .setCode(code);
        if (message != null) out.setMessage(message);
        if (bookmark != null) out.setBookmark(ByteString.copyFrom(bookmark));

        for (String objectId : objectIds) {
            out.addIds(TaskDef.parseId(objectId));
        }

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        SearchTaskDefReplyPbOrBuilder p = (SearchTaskDefReplyPbOrBuilder) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasBookmark()) bookmark = p.getBookmark().toByteArray();

        for (TaskDefIdPb id : p.getIdsList()) {
            objectIds.add(TaskDef.getObjectId(id));
        }
    }
}
