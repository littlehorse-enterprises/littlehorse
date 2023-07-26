package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.objectId.NodeRunId;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.NodeRunIdPb;
import io.littlehorse.sdk.common.proto.SearchNodeRunPb;
import io.littlehorse.sdk.common.proto.SearchNodeRunPb.NoderunCriteriaCase;
import io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchNodeRunReply;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchNodeRun
    extends PublicScanRequest<SearchNodeRunPb, SearchNodeRunReplyPb, NodeRunIdPb, NodeRunId, SearchNodeRunReply> {

    public NoderunCriteriaCase type;
    public String wfRunId;

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.NODE_RUN;
    }

    public Class<SearchNodeRunPb> getProtoBaseClass() {
        return SearchNodeRunPb.class;
    }

    public void initFrom(Message proto) {
        SearchNodeRunPb p = (SearchNodeRunPb) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        type = p.getNoderunCriteriaCase();
        switch (type) {
            case WF_RUN_ID:
                wfRunId = p.getWfRunId();
                break;
            case NODERUNCRITERIA_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }

    public SearchNodeRunPb.Builder toProto() {
        SearchNodeRunPb.Builder out = SearchNodeRunPb.newBuilder();
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
            case NODERUNCRITERIA_NOT_SET:
                throw new RuntimeException("not possible");
        }

        return out;
    }

    public static SearchNodeRun fromProto(SearchNodeRunPb proto) {
        SearchNodeRun out = new SearchNodeRun();
        out.initFrom(proto);
        return out;
    }

    @Override
    public TagStorageTypePb indexTypeForSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        return TagStorageTypePb.LOCAL;
    }

    @Override
    public void validate() throws LHValidationError {}

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString)
        throws LHValidationError {
        if (type == NoderunCriteriaCase.WF_RUN_ID) {
            return new ObjectIdScanBoundaryStrategy(wfRunId);
        } else {
            throw new LHValidationError("Yikes, unimplemented type: " + type);
        }
    }
}
