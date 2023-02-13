package io.littlehorse.server.streamsimpl.searchutils.publicrequests;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.BookmarkPb;
import io.littlehorse.jlib.common.proto.GETableClassEnumPb;
import io.littlehorse.jlib.common.proto.LHInternalSearchPb.PrefixCase;
import io.littlehorse.jlib.common.proto.SearchExternalEventPb;
import io.littlehorse.jlib.common.proto.SearchExternalEventPb.ExtEvtCriteriaCase;
import io.littlehorse.jlib.common.proto.SearchExternalEventPbOrBuilder;
import io.littlehorse.server.streamsimpl.searchutils.LHInternalSearch;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearch;

public class SearchExternalEvent extends LHPublicSearch<SearchExternalEventPb> {

    public ExtEvtCriteriaCase type;
    public String wfRunId;

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.EXTERNAL_EVENT;
    }

    public Class<SearchExternalEventPb> getProtoBaseClass() {
        return SearchExternalEventPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        SearchExternalEventPbOrBuilder p = (SearchExternalEventPbOrBuilder) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                LHUtil.log("Failed to load bookmark:");
                exn.printStackTrace();
            }
        }

        type = p.getExtEvtCriteriaCase();
        switch (type) {
            case WF_RUN_ID:
                wfRunId = p.getWfRunId();
                break;
            case EXTEVTCRITERIA_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }

    public SearchExternalEventPb.Builder toProto() {
        SearchExternalEventPb.Builder out = SearchExternalEventPb.newBuilder();
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
            case EXTEVTCRITERIA_NOT_SET:
                throw new RuntimeException("not possible");
        }

        return out;
    }

    public LHInternalSearch startInternalSearch(LHGlobalMetaStores stores) {
        LHInternalSearch out = new LHInternalSearch();
        if (type == ExtEvtCriteriaCase.WF_RUN_ID) {
            out.prefixType = PrefixCase.OBJECT_ID_PREFIX;
            out.partitionKey = wfRunId;
            out.objectIdPrefix = wfRunId;
        } else {
            throw new RuntimeException("Not possible or unimplemented");
        }
        return out;
    }
}
