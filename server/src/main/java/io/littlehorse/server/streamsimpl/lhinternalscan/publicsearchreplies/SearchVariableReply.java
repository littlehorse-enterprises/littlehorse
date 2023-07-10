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
}
