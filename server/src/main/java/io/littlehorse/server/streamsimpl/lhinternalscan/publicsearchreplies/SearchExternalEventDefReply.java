package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.ExternalEventDefIdModel;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchExternalEventDefReply
    extends PublicScanReply<SearchExternalEventDefReplyPb, ExternalEventDefId, ExternalEventDefIdModel> {

    public Class<SearchExternalEventDefReplyPb> getProtoBaseClass() {
        return SearchExternalEventDefReplyPb.class;
    }

    public Class<ExternalEventDefIdModel> getResultJavaClass() {
        return ExternalEventDefIdModel.class;
    }

    public Class<ExternalEventDefId> getResultProtoClass() {
        return ExternalEventDefId.class;
    }
}
