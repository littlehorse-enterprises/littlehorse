package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.ExternalEventDefIdModel;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.SearchExternalEventDefResponse;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchExternalEventDefReply
        extends PublicScanReply<
                SearchExternalEventDefResponse, ExternalEventDefId, ExternalEventDefIdModel> {

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
