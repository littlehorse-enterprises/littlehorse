package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SearchTaskRunRequest;
import io.littlehorse.sdk.common.proto.SearchTaskRunRequest.ByTaskDefRequest;
import io.littlehorse.sdk.common.proto.SearchTaskRunRequest.StatusAndTaskDefRequest;
import io.littlehorse.sdk.common.proto.SearchTaskRunRequest.TaskRunCriteriaCase;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.TaskRunIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchTaskRunReply;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class SearchTaskRunRequestModel
        extends PublicScanRequest<SearchTaskRunRequest, TaskRunIdList, TaskRunId, TaskRunIdModel, SearchTaskRunReply> {

    private TaskRunCriteriaCase type;
    private ByTaskDefRequest taskDef;
    private StatusAndTaskDefRequest statusAndTaskDef;

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.TASK_RUN;
    }

    public Class<SearchTaskRunRequest> getProtoBaseClass() {
        return SearchTaskRunRequest.class;
    }

    public void initFrom(Message proto) {
        SearchTaskRunRequest p = (SearchTaskRunRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        type = p.getTaskRunCriteriaCase();
        switch (type) {
            case TASK_DEF:
                taskDef = p.getTaskDef();
                break;
            case STATUS_AND_TASK_DEF:
                statusAndTaskDef = p.getStatusAndTaskDef();
                break;
            case TASKRUNCRITERIA_NOT_SET:
                log.warn("Didn't set TaskDef or StatusAndTaskDef!");
        }
    }

    public SearchTaskRunRequest.Builder toProto() {
        SearchTaskRunRequest.Builder out = SearchTaskRunRequest.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        switch (type) {
            case TASK_DEF:
                out.setTaskDef(taskDef);
                break;
            case STATUS_AND_TASK_DEF:
                out.setStatusAndTaskDef(statusAndTaskDef);
            case TASKRUNCRITERIA_NOT_SET:
                log.warn("Didn't set TaskDef or StatusAndTaskDef!");
        }

        return out;
    }

    public static SearchTaskRunRequestModel fromProto(SearchTaskRunRequest proto) {
        SearchTaskRunRequestModel out = new SearchTaskRunRequestModel();
        out.initFrom(proto);
        return out;
    }

    private Timestamp getEarliestStart() {
        if (type == TaskRunCriteriaCase.TASK_DEF) {
            if (taskDef.hasEarliestStart()) {
                return taskDef.getEarliestStart();
            }
        } else if (type == TaskRunCriteriaCase.STATUS_AND_TASK_DEF) {
            if (statusAndTaskDef.hasEarliestStart()) {
                return statusAndTaskDef.getEarliestStart();
            }
        }
        return null;
    }

    private Timestamp getLatestStart() {
        if (type == TaskRunCriteriaCase.TASK_DEF) {
            if (taskDef.hasLatestStart()) {
                return taskDef.getLatestStart();
            }
        } else if (type == TaskRunCriteriaCase.STATUS_AND_TASK_DEF) {
            if (statusAndTaskDef.hasLatestStart()) {
                return statusAndTaskDef.getLatestStart();
            }
        }
        return null;
    }

    @Override
    public List<Attribute> getSearchAttributes() {
        if (type == TaskRunCriteriaCase.TASK_DEF) {
            return List.of(new Attribute("taskDefName", statusAndTaskDef.getTaskDefName()));
        } else {
            return List.of(
                    new Attribute("taskDefName", statusAndTaskDef.getTaskDefName()),
                    new Attribute("status", statusAndTaskDef.getStatus().toString()));
        }
    }

    @Override
    public TagStorageType indexTypeForSearch(ReadOnlyMetadataStore stores) throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public LHStore getStore(ReadOnlyMetadataStore metaStore) {
        return LHStore.REPARTITION;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) throws LHApiException {
        if (type == TaskRunCriteriaCase.TASK_DEF || type == TaskRunCriteriaCase.STATUS_AND_TASK_DEF) {
            return new TagScanBoundaryStrategy(
                    searchAttributeString,
                    Optional.ofNullable(LHUtil.fromProtoTs(getEarliestStart())),
                    Optional.ofNullable(LHUtil.fromProtoTs(getLatestStart())));
        } else {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Unimplemented search type: " + type);
        }
    }
}
