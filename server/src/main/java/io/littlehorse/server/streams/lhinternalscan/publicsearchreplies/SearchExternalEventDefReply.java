package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.SearchExternalEventDefResponse;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchExternalEventDefReply
        extends PublicScanReply<SearchExternalEventDefResponse, ExternalEventDefId, ExternalEventDefIdModel> {

    public Class<SearchExternalEventDefResponse> getProtoBaseClass() {
        return SearchExternalEventDefResponse.class;
    }

    public Class<ExternalEventDefIdModel> getResultJavaClass() {
        return ExternalEventDefIdModel.class;
    }

    public Class<ExternalEventDefId> getResultProtoClass() {
        return ExternalEventDefId.class;
    }
}
