package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.sdk.common.proto.ExternalEventPb;
import io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class ListExternalEventsReply
    extends PublicScanReply<ListExternalEventsReplyPb, ExternalEventPb, ExternalEvent> {

    public Class<ExternalEvent> getResultJavaClass() {
        return ExternalEvent.class;
    }

    public Class<ExternalEventPb> getResultProtoClass() {
        return ExternalEventPb.class;
    }

    public Class<ListExternalEventsReplyPb> getProtoBaseClass() {
        return ListExternalEventsReplyPb.class;
    }
}
