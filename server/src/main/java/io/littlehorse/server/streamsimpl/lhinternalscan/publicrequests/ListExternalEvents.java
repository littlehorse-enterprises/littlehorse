package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.ExternalEventPb;
import io.littlehorse.sdk.common.proto.ListExternalEventsPb;
import io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
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

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.EXTERNAL_EVENT;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores) {
        return null;
    }

    @Override
    public TagStorageTypePb indexTypeForSearch() throws LHValidationError {
        return TagStorageTypePb.LOCAL;
    }

    @Override
    public void validate() throws LHValidationError {}

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new ObjectIdScanBoundaryStrategy(wfRunId);
    }
}
