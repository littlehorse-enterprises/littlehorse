package io.littlehorse.server.streamsimpl.searchutils.publicrequests;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.SearchNodeRunPb;
import io.littlehorse.common.proto.SearchNodeRunPb.NoderunCriteriaCase;
import io.littlehorse.common.proto.SearchNodeRunPb.StatusAndTaskDefPb;
import io.littlehorse.common.proto.SearchNodeRunPbOrBuilder;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsimpl.searchutils.LHInternalSubSearch;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearch;

public class SearchNodeRun extends LHPublicSearch<SearchNodeRunPb> {

    public NoderunCriteriaCase type;
    public StatusAndTaskDefPb statusAndTaskDef;
    public String wfRunId;

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.NODE_RUN;
    }

    public Class<SearchNodeRunPb> getProtoBaseClass() {
        return SearchNodeRunPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        SearchNodeRunPbOrBuilder p = (SearchNodeRunPbOrBuilder) proto;
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
            case NODERUNCRITERIA_NOT_SET:
                throw new RuntimeException("not possible");
        }

        return out;
    }

    public static SearchNodeRun fromProto(SearchNodeRunPbOrBuilder proto) {
        SearchNodeRun out = new SearchNodeRun();
        out.initFrom(proto);
        return out;
    }

    public LHInternalSubSearch<?> getSubSearch(LHGlobalMetaStores stores) {
        throw new RuntimeException("not possible");
    }
}
