package io.littlehorse.server.streamsimpl.searchutils.publicrequests;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.BookmarkPb;
import io.littlehorse.jlib.common.proto.GETableClassEnumPb;
import io.littlehorse.jlib.common.proto.LHInternalSearchPb.PrefixCase;
import io.littlehorse.jlib.common.proto.SearchWfRunPb;
import io.littlehorse.jlib.common.proto.SearchWfRunPb.StatusAndSpecPb;
import io.littlehorse.jlib.common.proto.SearchWfRunPb.WfrunCriteriaCase;
import io.littlehorse.jlib.common.proto.SearchWfRunPbOrBuilder;
import io.littlehorse.server.streamsimpl.searchutils.LHInternalSearch;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearch;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;

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

    public LHInternalSearch startInternalSearch(LHGlobalMetaStores stores) {
        LHInternalSearch out = new LHInternalSearch();
        if (type == WfrunCriteriaCase.STATUS_AND_SPEC) {
            out.prefixType = PrefixCase.TAG_PREFIX;
            out.tagPrefix.add(
                new Attribute("wfSpecName", statusAndSpec.getWfSpecName())
            );
            out.tagPrefix.add(
                new Attribute("status", statusAndSpec.getStatus().toString())
            );
        } else {
            throw new RuntimeException("Not possible or unimplemented");
        }
        return out;
    }
}
