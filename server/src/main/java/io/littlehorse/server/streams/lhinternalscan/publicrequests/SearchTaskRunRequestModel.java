package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SearchTaskRunRequest;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.TaskRunIdList;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchTaskRunReply;
import io.littlehorse.server.streams.lhinternalscan.util.TagScanModel;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class SearchTaskRunRequestModel
        extends PublicScanRequest<SearchTaskRunRequest, TaskRunIdList, TaskRunId, TaskRunIdModel, SearchTaskRunReply> {

    private TaskStatus status;
    private String taskDefName;
    private Date earliestStart;
    private Date latestStart;

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.TASK_RUN;
    }

    @Override
    public Class<SearchTaskRunRequest> getProtoBaseClass() {
        return SearchTaskRunRequest.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SearchTaskRunRequest p = (SearchTaskRunRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        taskDefName = p.getTaskDefName();
        if (p.hasStatus()) status = p.getStatus();

        if (p.hasEarliestStart()) earliestStart = LHUtil.fromProtoTs(p.getEarliestStart());
        if (p.hasLatestStart()) latestStart = LHUtil.fromProtoTs(p.getLatestStart());
    }

    @Override
    public SearchTaskRunRequest.Builder toProto() {
        SearchTaskRunRequest.Builder out = SearchTaskRunRequest.newBuilder().setTaskDefName(taskDefName);

        if (bookmark != null) out.setBookmark(bookmark.toByteString());
        if (limit != null) out.setLimit(limit);

        if (status != null) out.setStatus(status);
        if (earliestStart != null) out.setEarliestStart(LHUtil.fromDate(earliestStart));
        if (latestStart != null) out.setLatestStart(LHUtil.fromDate(latestStart));

        return out;
    }

    public static SearchTaskRunRequestModel fromProto(SearchTaskRunRequest proto, ExecutionContext context) {
        SearchTaskRunRequestModel out = new SearchTaskRunRequestModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public TagScanModel getScanBoundary(RequestExecutionContext ctx) throws LHApiException {
        // Return error to client if the TaskDef doesn't exist
        TaskDefModel taskDef = ctx.service().getTaskDef(taskDefName);
        if (taskDef == null)
            throw new LHApiException(Status.INVALID_ARGUMENT, "No such taskDef %s".formatted(taskDefName));

        TagScanModel out = new TagScanModel(GetableClassEnum.TASK_RUN, earliestStart, latestStart);
        out.add(new Attribute("taskDefName", taskDefName));

        if (status != null) {
            out.add(new Attribute("status", status.toString()));
        }

        return out;
    }

    @Override
    public LHStoreType getStoreType() {
        return LHStoreType.CORE;
    }
}
