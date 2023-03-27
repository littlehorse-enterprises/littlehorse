package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.ExternalEventPb;
import io.littlehorse.jlib.common.proto.ListExternalEventsPb;
import io.littlehorse.jlib.common.proto.ListExternalEventsReplyPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListExternalEventsReply;

public class ListExternalEvents
    extends PublicScanRequest<ListExternalEventsPb, ListExternalEventsReplyPb, ExternalEventPb, ExternalEvent, ListExternalEventsReply> {

    public String wfRunId;

    public Class<ListExternalEventsPb> getProtoBaseClass() {
        return ListExternalEventsPb.class;
    }

    public ListExternalEventsPb.Builder toProto() {
        return ListExternalEventsPb.newBuilder().setWfRunId(wfRunId);
    }

    public void initFrom(Message proto) {
        ListExternalEventsPb p = (ListExternalEventsPb) proto;
        wfRunId = p.getWfRunId();
    }

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.EXTERNAL_EVENT;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores) {
        InternalScan out = new InternalScan();
        out.storeName = ServerTopology.CORE_STORE;
        out.resultType = ScanResultTypePb.OBJECT;
        out.type = ScanBoundaryCase.OBJECT_ID_PREFIX;
        out.partitionKey = wfRunId;
        out.objectIdPrefix = wfRunId + "/";
        return out;
    }
}
