package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.ExternalEventDefIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchExternalEventDefReply
        extends PublicScanReply<ExternalEventDefIdList, ExternalEventDefId, ExternalEventDefIdModel> {

    public Class<ExternalEventDefIdList> getProtoBaseClass() {
        return ExternalEventDefIdList.class;
    }

    public Class<ExternalEventDefIdModel> getResultJavaClass() {
        return ExternalEventDefIdModel.class;
    }

    public Class<ExternalEventDefId> getResultProtoClass() {
        return ExternalEventDefId.class;
    }
}
