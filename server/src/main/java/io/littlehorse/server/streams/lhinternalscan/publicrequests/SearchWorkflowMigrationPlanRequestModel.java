package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.WorkflowMigrationPlanIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.SearchWorkflowMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.WorkflowMigrationPlanId;
import io.littlehorse.sdk.common.proto.WorkflowMigrationPlanIdList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWorkflowMigrationPlanReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchWorkflowMigrationPlanRequestModel
        extends PublicScanRequest<
                SearchWorkflowMigrationPlanRequest,
                WorkflowMigrationPlanIdList,
                WorkflowMigrationPlanId,
                WorkflowMigrationPlanIdModel,
                SearchWorkflowMigrationPlanReply> {

    private String prefix;

    public Class<SearchWorkflowMigrationPlanRequest> getProtoBaseClass() {
        return SearchWorkflowMigrationPlanRequest.class;
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.GLOBAL_METADATA;
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.WORKFLOW_MIGRATION_PLAN;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SearchWorkflowMigrationPlanRequest p = (SearchWorkflowMigrationPlanRequest) proto;
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

    public SearchWorkflowMigrationPlanRequest.Builder toProto() {
        SearchWorkflowMigrationPlanRequest.Builder out = SearchWorkflowMigrationPlanRequest.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        if (prefix != null) out.setPrefix(prefix);

        return out;
    }

    public static SearchWorkflowMigrationPlanRequestModel fromProto(
            SearchWorkflowMigrationPlanRequest proto, ExecutionContext context) {
        SearchWorkflowMigrationPlanRequestModel out = new SearchWorkflowMigrationPlanRequestModel();
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
