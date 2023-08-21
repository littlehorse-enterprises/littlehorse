package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.SearchNodeRunRequest;
import io.littlehorse.sdk.common.proto.SearchNodeRunRequest.NoderunCriteriaCase;
import io.littlehorse.sdk.common.proto.SearchNodeRunResponse;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchNodeRunReply;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchNodeRunRequestModel
        extends PublicScanRequest<
                SearchNodeRunRequest, SearchNodeRunResponse, NodeRunId, NodeRunIdModel, SearchNodeRunReply> {

    public NoderunCriteriaCase type;
    public String wfRunId;

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.NODE_RUN;
    }

    public Class<SearchNodeRunRequest> getProtoBaseClass() {
        return SearchNodeRunRequest.class;
    }

    public void initFrom(Message proto) {
        SearchNodeRunRequest p = (SearchNodeRunRequest) proto;
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

    public SearchNodeRunRequest.Builder toProto() {
        SearchNodeRunRequest.Builder out = SearchNodeRunRequest.newBuilder();
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

    public static SearchNodeRunRequestModel fromProto(SearchNodeRunRequest proto) {
        SearchNodeRunRequestModel out = new SearchNodeRunRequestModel();
        out.initFrom(proto);
        return out;
    }

    @Override
    public TagStorageType indexTypeForSearch(ReadOnlyMetadataStore stores) throws LHValidationError {
        return TagStorageType.LOCAL;
    }

    @Override
    public void validate() throws LHValidationError {}

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) throws LHValidationError {
        if (type == NoderunCriteriaCase.WF_RUN_ID) {
            return new ObjectIdScanBoundaryStrategy(wfRunId);
        } else {
            throw new LHValidationError("Unimplemented type: " + type);
        }
    }
}
