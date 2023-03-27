package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.ExternalEventDefId;
import io.littlehorse.jlib.common.proto.ExternalEventDefIdPb;
import io.littlehorse.jlib.common.proto.SearchExternalEventDefReplyPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchExternalEventDefReply
    extends PublicScanReply<SearchExternalEventDefReplyPb, ExternalEventDefIdPb, ExternalEventDefId> {

    public Class<SearchExternalEventDefReplyPb> getProtoBaseClass() {
        return SearchExternalEventDefReplyPb.class;
    }

    public Class<ExternalEventDefId> getResultJavaClass() {
        return ExternalEventDefId.class;
    }

    public Class<ExternalEventDefIdPb> getResultProtoClass() {
        return ExternalEventDefIdPb.class;
    }
}
