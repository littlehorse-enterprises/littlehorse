package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SearchWorkflowEventRequest;
import io.littlehorse.sdk.common.proto.WorkflowEventId;
import io.littlehorse.sdk.common.proto.WorkflowEventIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWorkflowEventReply;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class SearchWorkflowEventRequestModel
        extends PublicScanRequest<
                SearchWorkflowEventRequest,
                WorkflowEventIdList,
                WorkflowEventId,
                WorkflowEventIdModel,
                SearchWorkflowEventReply> {

    private Date earliestStart;
    private Date latestStart;
    private WorkflowEventDefIdModel workflowEventDefId;
    private Boolean isClaimed = null;
    private ExecutionContext context;

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.WORKFLOW_EVENT;
    }

    public Class<SearchWorkflowEventRequest> getProtoBaseClass() {
        return SearchWorkflowEventRequest.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SearchWorkflowEventRequest p = (SearchWorkflowEventRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        if (p.hasEarliestStart()) earliestStart = LHUtil.fromProtoTs(p.getEarliestStart());
        if (p.hasLatestStart()) latestStart = LHUtil.fromProtoTs(p.getLatestStart());

        workflowEventDefId =
                WorkflowEventDefIdModel.fromProto(p.getWorkflowEventDefId(), WorkflowEventDefIdModel.class, context);

        this.context = context;
    }

    public SearchWorkflowEventRequest.Builder toProto() {
        SearchWorkflowEventRequest.Builder builder = SearchWorkflowEventRequest.newBuilder();

        if (bookmark != null) builder.setBookmark(bookmark.toByteString());

        if (limit != null) builder.setLimit(limit);

        if (earliestStart != null) builder.setEarliestStart(LHUtil.fromDate(earliestStart));
        if (latestStart != null) builder.setLatestStart(LHUtil.fromDate(latestStart));

        builder.setWorkflowEventDefId(workflowEventDefId.toProto());

        return builder;
    }

    public List<Attribute> getSearchAttributes() {
        return List.of(new Attribute("wfEvtDefName", workflowEventDefId.toString()));
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        if (workflowEventDefId.getName().isBlank()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Missing required argument: WorkflowEventDefId.");
        }

        if (context.service().getWorkflowEventDef(workflowEventDefId.getName()) == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "WorkflowEventDef \"%s\" does not exist.".formatted(workflowEventDefId.getName()));
        }

        return TagStorageType.LOCAL;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new TagScanBoundaryStrategy(
                searchAttributeString, Optional.ofNullable(earliestStart), Optional.ofNullable(latestStart));
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.CORE;
    }
}
