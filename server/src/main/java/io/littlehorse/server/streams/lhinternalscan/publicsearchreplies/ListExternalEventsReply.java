package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.sdk.common.proto.ListExternalEventsResponse;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class ListExternalEventsReply
        extends PublicScanReply<ListExternalEventsResponse, ExternalEvent, ExternalEventModel> {

    public Class<ExternalEventModel> getResultJavaClass() {
        return ExternalEventModel.class;
    }

    public Class<ExternalEvent> getResultProtoClass() {
        return ExternalEvent.class;
    }

    public Class<ListExternalEventsResponse> getProtoBaseClass() {
        return ListExternalEventsResponse.class;
    }
}
