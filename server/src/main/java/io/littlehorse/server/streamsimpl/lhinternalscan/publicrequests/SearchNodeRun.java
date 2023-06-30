package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.objectId.NodeRunId;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.InternalScanPb.TagScanPb;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.NodeRunIdPb;
import io.littlehorse.jlib.common.proto.SearchNodeRunPb;
import io.littlehorse.jlib.common.proto.SearchNodeRunPb.ByTaskDefPb;
import io.littlehorse.jlib.common.proto.SearchNodeRunPb.NoderunCriteriaCase;
import io.littlehorse.jlib.common.proto.SearchNodeRunPb.StatusAndTaskDefPb;
import io.littlehorse.jlib.common.proto.SearchNodeRunPb.UserTaskRunSearchPb;
import io.littlehorse.jlib.common.proto.SearchNodeRunReplyPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchNodeRunReply;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchNodeRun
    extends PublicScanRequest<SearchNodeRunPb, SearchNodeRunReplyPb, NodeRunIdPb, NodeRunId, SearchNodeRunReply> {

    public NoderunCriteriaCase type;
    public StatusAndTaskDefPb statusAndTaskDef;
    private ByTaskDefPb taskDef;
    public String wfRunId;
    public UserTaskRunSearchPb userTaskSearch;

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
            case STATUS_AND_TASKDEF:
                statusAndTaskDef = p.getStatusAndTaskdef();
                break;
            case WF_RUN_ID:
                wfRunId = p.getWfRunId();
                break;
            case USER_TASK_RUN:
                userTaskSearch = p.getUserTaskRun();
            case TASK_DEF:
                taskDef = p.getTaskDef();
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
            case STATUS_AND_TASKDEF:
                out.setStatusAndTaskdef(statusAndTaskDef);
                break;
            case WF_RUN_ID:
                out.setWfRunId(wfRunId);
                break;
            case USER_TASK_RUN:
                out.setUserTaskRun(userTaskSearch);
            case TASK_DEF:
                out.setTaskDef(taskDef);
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

    public InternalScan startInternalSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        InternalScan out = new InternalScan();
        out.storeName = ServerTopology.CORE_STORE;
        out.resultType = ScanResultTypePb.OBJECT_ID;

        if (type == NoderunCriteriaCase.STATUS_AND_TASKDEF) {
            out.type = ScanBoundaryCase.TAG_SCAN;
            TagScanPb.Builder prefixScanBuilder = TagScanPb
                .newBuilder()
                .addAttributes(
                    new Attribute("taskDefName", statusAndTaskDef.getTaskDefName())
                        .toProto()
                )
                .addAttributes(
                    new Attribute("status", statusAndTaskDef.getStatus().toString())
                        .toProto()
                );

            if (statusAndTaskDef.hasEarliestStart()) {
                prefixScanBuilder.setEarliestCreateTime(
                    statusAndTaskDef.getEarliestStart()
                );
            }
            if (statusAndTaskDef.hasLatestStart()) {
                prefixScanBuilder.setLatestCreateTime(
                    statusAndTaskDef.getLatestStart()
                );
            }
            out.tagScan = prefixScanBuilder.build();
        } else if (type == NoderunCriteriaCase.TASK_DEF) {
            out.type = ScanBoundaryCase.TAG_SCAN;
            TagScanPb.Builder prefixScanBuilder = TagScanPb
                .newBuilder()
                .addAttributes(
                    new Attribute("taskDefName", taskDef.getTaskDefName()).toProto()
                );

            if (taskDef.hasEarliestStart()) {
                prefixScanBuilder.setEarliestCreateTime(taskDef.getEarliestStart());
            }
            if (taskDef.hasLatestStart()) {
                prefixScanBuilder.setLatestCreateTime(taskDef.getLatestStart());
            }
            out.tagScan = prefixScanBuilder.build();
        } else if (type == NoderunCriteriaCase.WF_RUN_ID) {
            out.type = ScanBoundaryCase.BOUNDED_OBJECT_ID_SCAN;
            out.partitionKey = wfRunId;
            out.boundedObjectIdScan =
                BoundedObjectIdScanPb
                    .newBuilder()
                    .setStartObjectId(wfRunId + "/")
                    .setEndObjectId(wfRunId + "/~")
                    .build();
        } else if (type == NoderunCriteriaCase.USER_TASK_RUN) {
            // TODO: This will change after we implement remote tags.
            // For example, if request.hasUserId(), it will be REMOTE and the
            // partitionKey will be attribute string; otherwise, it will be LOCAL.
            out.type = ScanBoundaryCase.TAG_SCAN;
            TagScanPb.Builder prefixScanBuilder = TagScanPb.newBuilder();

            if (userTaskSearch.hasStatus()) {
                prefixScanBuilder.addAttributes(
                    new Attribute("status", userTaskSearch.getStatus().toString())
                        .toProto()
                );
            }

            if (userTaskSearch.hasUserTaskDef()) {
                prefixScanBuilder.addAttributes(
                    new Attribute("userTaskDefName", userTaskSearch.getUserTaskDef())
                        .toProto()
                );
            }

            if (userTaskSearch.hasUserId()) {
                if (userTaskSearch.hasUserGroup()) {
                    throw new LHValidationError(
                        null,
                        "Cannot specify UserID and User Group in same search!"
                    );
                }
                prefixScanBuilder.addAttributes(
                    new Attribute("userId", userTaskSearch.getUserId()).toProto()
                );
            }

            if (userTaskSearch.hasUserGroup()) {
                prefixScanBuilder.addAttributes(
                    new Attribute("userGroup", userTaskSearch.getUserGroup())
                        .toProto()
                );
            }

            // TODO: allow unfiltered search. Need to either search without time
            // constraints over object ids, or need to add an empty tag.
            if (prefixScanBuilder.getAttributesCount() == 0) {
                throw new LHValidationError(
                    null,
                    "Must specify at least one of: [status, userTaskDefName, userGroup, userId]"
                );
            }

            if (userTaskSearch.hasEarliestStart()) {
                prefixScanBuilder.setEarliestCreateTime(
                    userTaskSearch.getEarliestStart()
                );
            }
            if (userTaskSearch.hasLatestStart()) {
                prefixScanBuilder.setLatestCreateTime(
                    userTaskSearch.getLatestStart()
                );
            }
            out.tagScan = prefixScanBuilder.build();
        } else {
            throw new RuntimeException("Yikes, unimplemented type: " + type);
        }
        return out;
    }
}
