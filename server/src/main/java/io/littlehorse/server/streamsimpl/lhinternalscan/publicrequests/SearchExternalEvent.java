package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.model.objectId.ExternalEventId;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.ExternalEventIdPb;
import io.littlehorse.jlib.common.proto.SearchExternalEventPb;
import io.littlehorse.jlib.common.proto.SearchExternalEventPb.ExtEvtCriteriaCase;
import io.littlehorse.jlib.common.proto.SearchExternalEventReplyPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchExternalEventReply;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchExternalEvent
    extends PublicScanRequest<SearchExternalEventPb, SearchExternalEventReplyPb, ExternalEventIdPb, ExternalEventId, SearchExternalEventReply> {

    public ExtEvtCriteriaCase type;
    public String wfRunId;

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.EXTERNAL_EVENT;
    }

    public Class<SearchExternalEventPb> getProtoBaseClass() {
        return SearchExternalEventPb.class;
    }

    public void initFrom(Message proto) {
        SearchExternalEventPb p = (SearchExternalEventPb) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        type = p.getExtEvtCriteriaCase();
        switch (type) {
            case WF_RUN_ID:
                wfRunId = p.getWfRunId();
                break;
            case EXTEVTCRITERIA_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }

    public SearchExternalEventPb.Builder toProto() {
        SearchExternalEventPb.Builder out = SearchExternalEventPb.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        switch (type) {
            case WF_RUN_ID:
                out.setWfRunId(wfRunId);
                break;
            case EXTEVTCRITERIA_NOT_SET:
                throw new RuntimeException("not possible");
        }

        return out;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores) {
        InternalScan out = new InternalScan();

        out.storeName = ServerTopology.CORE_STORE;
        out.resultType = ScanResultTypePb.OBJECT_ID;

        if (type == ExtEvtCriteriaCase.WF_RUN_ID) {
            out.type = ScanBoundaryCase.BOUNDED_OBJECT_ID_SCAN;
            out.partitionKey = wfRunId;
            out.boundedObjectIdScan =
                BoundedObjectIdScanPb
                    .newBuilder()
                    .setStartObjectId(wfRunId + "/")
                    .build();
        } else {
            throw new RuntimeException("Not possible or unimplemented");
        }
        return out;
    }
}
