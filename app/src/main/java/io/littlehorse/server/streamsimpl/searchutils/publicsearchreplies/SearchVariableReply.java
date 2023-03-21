package io.littlehorse.server.streamsimpl.searchutils.publicsearchreplies;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.jlib.common.proto.SearchVariableReplyPb;
import io.littlehorse.jlib.common.proto.SearchVariableReplyPbOrBuilder;
import io.littlehorse.jlib.common.proto.VariableIdPb;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearchReply;

public class SearchVariableReply extends LHPublicSearchReply<SearchVariableReplyPb> {

    public Class<SearchVariableReplyPb> getProtoBaseClass() {
        return SearchVariableReplyPb.class;
    }

    public SearchVariableReplyPb.Builder toProto() {
        SearchVariableReplyPb.Builder out = SearchVariableReplyPb
            .newBuilder()
            .setCode(code);
        if (message != null) out.setMessage(message);
        if (bookmark != null) out.setBookmark(ByteString.copyFrom(bookmark));

        for (String objectId : objectIds) {
            out.addIds(Variable.parseId(objectId));
        }

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        SearchVariableReplyPbOrBuilder p = (SearchVariableReplyPbOrBuilder) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasBookmark()) bookmark = p.getBookmark().toByteArray();

        for (VariableIdPb id : p.getIdsList()) {
            objectIds.add(Variable.getObjectId(id));
        }
    }
}
