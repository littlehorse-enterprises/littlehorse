package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.model.objectId.NodeRunId;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.InternalScanPb.TagPrefixScanPb;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.NodeRunIdPb;
import io.littlehorse.jlib.common.proto.SearchNodeRunPb;
import io.littlehorse.jlib.common.proto.SearchNodeRunPb.ByTaskDefPb;
import io.littlehorse.jlib.common.proto.SearchNodeRunPb.NoderunCriteriaCase;
import io.littlehorse.jlib.common.proto.SearchNodeRunPb.StatusAndTaskDefPb;
import io.littlehorse.jlib.common.proto.SearchNodeRunReplyPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchNodeRunReply;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;

public class SearchNodeRun
    extends PublicScanRequest<SearchNodeRunPb, SearchNodeRunReplyPb, NodeRunIdPb, NodeRunId, SearchNodeRunReply> {

    public NoderunCriteriaCase type;
    public StatusAndTaskDefPb statusAndTaskDef;
    private ByTaskDefPb taskDef;
    public String wfRunId;

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.NODE_RUN;
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
                LHUtil.log("Failed to load bookmark:");
                exn.printStackTrace();
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

    public InternalScan startInternalSearch(LHGlobalMetaStores stores) {
        InternalScan out = new InternalScan();
        out.storeName = ServerTopology.CORE_STORE;
        out.resultType = ScanResultTypePb.OBJECT_ID;

        if (type == NoderunCriteriaCase.STATUS_AND_TASKDEF) {
            out.type = ScanBoundaryCase.LOCAL_TAG_PREFIX_SCAN;
            TagPrefixScanPb.Builder prefixScanBuilder = TagPrefixScanPb
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
            out.localTagPrefixScan = prefixScanBuilder.build();
        } else if (type == NoderunCriteriaCase.TASK_DEF) {
            out.type = ScanBoundaryCase.LOCAL_TAG_PREFIX_SCAN;
            TagPrefixScanPb.Builder prefixScanBuilder = TagPrefixScanPb
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
            out.localTagPrefixScan = prefixScanBuilder.build();
        } else if (type == NoderunCriteriaCase.WF_RUN_ID) {
            out.type = ScanBoundaryCase.BOUNDED_OBJECT_ID_SCAN;
            out.partitionKey = wfRunId;
            out.boundedObjectIdScan =
                BoundedObjectIdScanPb
                    .newBuilder()
                    .setStartObjectId(wfRunId + "/")
                    .setEndObjectId(wfRunId + "/~")
                    .build();
        } else {
            throw new RuntimeException("Yikes, unimplemented type: " + type);
        }
        return out;
    }
}
