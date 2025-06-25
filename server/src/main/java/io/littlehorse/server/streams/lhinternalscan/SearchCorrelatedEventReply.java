package io.littlehorse.server.streams.lhinternalscan;

import io.littlehorse.common.model.getable.objectId.CorrelatedEventIdModel;
import io.littlehorse.sdk.common.proto.CorrelatedEventId;
import io.littlehorse.sdk.common.proto.CorrelatedEventIdList;

public class SearchCorrelatedEventReply
        extends PublicScanReply<CorrelatedEventIdList, CorrelatedEventId, CorrelatedEventIdModel> {
    @Override
    public Class<CorrelatedEventIdList> getProtoBaseClass() {
        return CorrelatedEventIdList.class;
    }

    @Override
    public Class<CorrelatedEventIdModel> getResultJavaClass() {
        return CorrelatedEventIdModel.class;
    }

    @Override
    public Class<CorrelatedEventId> getResultProtoClass() {
        return CorrelatedEventId.class;
    }
}
