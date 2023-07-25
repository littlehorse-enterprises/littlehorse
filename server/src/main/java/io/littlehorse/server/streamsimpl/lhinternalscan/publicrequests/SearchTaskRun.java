package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.objectId.TaskRunId;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.InternalScanPb.TagScanPb;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.SearchTaskRunPb;
import io.littlehorse.sdk.common.proto.SearchTaskRunPb.ByTaskDefPb;
import io.littlehorse.sdk.common.proto.SearchTaskRunPb.StatusAndTaskDefPb;
import io.littlehorse.sdk.common.proto.SearchTaskRunPb.TaskRunCriteriaCase;
import io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.TaskRunIdPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchTaskRunReply;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class SearchTaskRun
    extends PublicScanRequest<SearchTaskRunPb, SearchTaskRunReplyPb, TaskRunIdPb, TaskRunId, SearchTaskRunReply> {

    private TaskRunCriteriaCase type;
    private ByTaskDefPb taskDef;
    private StatusAndTaskDefPb statusAndTaskDef;

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.TASK_RUN;
    }

    public Class<SearchTaskRunPb> getProtoBaseClass() {
        return SearchTaskRunPb.class;
    }

    public void initFrom(Message proto) {
        SearchTaskRunPb p = (SearchTaskRunPb) proto;
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

    public SearchTaskRunPb.Builder toProto() {
        SearchTaskRunPb.Builder out = SearchTaskRunPb.newBuilder();
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

    public static SearchTaskRun fromProto(SearchTaskRunPb proto) {
        SearchTaskRun out = new SearchTaskRun();
        out.initFrom(proto);
        return out;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        InternalScan out = new InternalScan();
        out.setResultType(ScanResultTypePb.OBJECT_ID);
        out.setObjectType(getObjectType());

        if (type == TaskRunCriteriaCase.TASK_DEF) {
            out.storeName = ServerTopology.CORE_STORE;
            out.type = ScanBoundaryCase.TAG_SCAN;

            // partiiton key should be null, since it's a LOCAL search.
            TagScanPb.Builder scanBuilder = TagScanPb
                .newBuilder()
                .setKeyPrefix(getSearchAttributeString());

            if (taskDef.hasEarliestStart()) {
                scanBuilder.setEarliestCreateTime(taskDef.getEarliestStart());
            }
            if (taskDef.hasLatestStart()) {
                scanBuilder.setLatestCreateTime(taskDef.getLatestStart());
            }
            out.setTagScan(scanBuilder.build());
        } else if (type == TaskRunCriteriaCase.STATUS_AND_TASK_DEF) {
            out.storeName = ServerTopology.CORE_STORE;
            out.type = ScanBoundaryCase.TAG_SCAN;

            TagScanPb.Builder scanBuilder = TagScanPb
                .newBuilder()
                .setKeyPrefix(getSearchAttributeString());

            if (statusAndTaskDef.hasEarliestStart()) {
                scanBuilder.setEarliestCreateTime(
                    statusAndTaskDef.getEarliestStart()
                );
            }
            if (statusAndTaskDef.hasLatestStart()) {
                scanBuilder.setLatestCreateTime(statusAndTaskDef.getLatestStart());
            }

            out.setTagScan(scanBuilder.build());
        } else {
            throw new LHValidationError("Unimplemented search type: " + type);
        }
        return out;
    }

    @Override
    public List<Attribute> getSearchAttributes() {
        if (type == TaskRunCriteriaCase.TASK_DEF) {
            return List.of(
                new Attribute("taskDefName", statusAndTaskDef.getTaskDefName())
            );
        } else {
            return List.of(
                new Attribute("taskDefName", statusAndTaskDef.getTaskDefName()),
                new Attribute("status", statusAndTaskDef.getStatus().toString())
            );
        }
    }
}
