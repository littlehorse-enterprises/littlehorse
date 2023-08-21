package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.sdk.common.proto.ExternalEventIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchExternalEventReply
        extends PublicScanReply<ExternalEventIdList, ExternalEventId, ExternalEventIdModel> {

    public Class<ExternalEventIdList> getProtoBaseClass() {
        return ExternalEventIdList.class;
    }

    public Class<ExternalEventId> getResultProtoClass() {
        return ExternalEventId.class;
    }

    public Class<ExternalEventIdModel> getResultJavaClass() {
        return ExternalEventIdModel.class;
    }
}
