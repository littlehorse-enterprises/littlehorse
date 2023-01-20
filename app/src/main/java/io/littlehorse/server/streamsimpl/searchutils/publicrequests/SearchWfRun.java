package io.littlehorse.server.streamsimpl.searchutils.publicrequests;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.SearchWfRunPb;
import io.littlehorse.common.proto.SearchWfRunPb.StatusAndSpecPb;
import io.littlehorse.common.proto.SearchWfRunPb.WfrunCriteriaCase;
import io.littlehorse.common.proto.SearchWfRunPbOrBuilder;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsimpl.searchutils.LHInternalSubSearch;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearch;

public class SearchWfRun extends LHPublicSearch<SearchWfRunPb> {

    public WfrunCriteriaCase type;
    public StatusAndSpecPb statusAndSpec;

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.WF_RUN;
    }

    public Class<SearchWfRunPb> getProtoBaseClass() {
        return SearchWfRunPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        SearchWfRunPbOrBuilder p = (SearchWfRunPbOrBuilder) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                LHUtil.log("Failed to load bookmark:");
                exn.printStackTrace();
            }
        }

        type = p.getWfrunCriteriaCase();
        switch (type) {
            case STATUS_AND_SPEC:
                statusAndSpec = p.getStatusAndSpec();
                break;
            case WFRUNCRITERIA_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }

    public SearchWfRunPb.Builder toProto() {
        SearchWfRunPb.Builder out = SearchWfRunPb.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        switch (type) {
            case STATUS_AND_SPEC:
                out.setStatusAndSpec(statusAndSpec);
                break;
            case WFRUNCRITERIA_NOT_SET:
                throw new RuntimeException("not possible");
        }

        return out;
    }

    public static SearchWfRun fromProto(SearchWfRunPbOrBuilder proto) {
        SearchWfRun out = new SearchWfRun();
        out.initFrom(proto);
        return out;
    }

    public LHInternalSubSearch<?> getSubSearch(LHGlobalMetaStores stores) {
        if (type == WfrunCriteriaCase.STATUS_AND_SPEC) {}
        throw new RuntimeException("not possible");
    }
}
