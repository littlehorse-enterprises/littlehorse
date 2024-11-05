package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest;
import io.littlehorse.sdk.common.proto.WorkflowEventDefId;
import io.littlehorse.sdk.common.proto.WorkflowEventDefIdList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWorkflowEventDefReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchWorkflowEventDefRequestModel
        extends PublicScanRequest<
                SearchWorkflowEventDefRequest,
                WorkflowEventDefIdList,
                WorkflowEventDefId,
                WorkflowEventDefIdModel,
                SearchWorkflowEventDefReply> {

    public String prefix;

    public Class<SearchWorkflowEventDefRequest> getProtoBaseClass() {
        return SearchWorkflowEventDefRequest.class;
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.GLOBAL_METADATA;
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.WORKFLOW_EVENT_DEF;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SearchWorkflowEventDefRequest p = (SearchWorkflowEventDefRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }
        if (p.hasPrefix()) prefix = p.getPrefix();
    }

    public SearchWorkflowEventDefRequest.Builder toProto() {
        SearchWorkflowEventDefRequest.Builder out = SearchWorkflowEventDefRequest.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        if (prefix != null) out.setPrefix(prefix);

        return out;
    }

    public static SearchWorkflowEventDefRequestModel fromProto(
            SearchWorkflowEventDefRequest proto, ExecutionContext context) {
        SearchWorkflowEventDefRequestModel out = new SearchWorkflowEventDefRequestModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        if (prefix != null && !prefix.equals("")) {
            return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, prefix, prefix + "~");
        } else {
            return ObjectIdScanBoundaryStrategy.prefixMetadataScan();
        }
    }
}
