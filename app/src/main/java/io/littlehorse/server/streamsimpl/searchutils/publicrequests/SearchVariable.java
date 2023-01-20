package io.littlehorse.server.streamsimpl.searchutils.publicrequests;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.SearchVariablePb;
import io.littlehorse.common.proto.SearchVariablePb.VariableCriteriaCase;
import io.littlehorse.common.proto.SearchVariablePbOrBuilder;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsimpl.searchutils.LHInternalSubSearch;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearch;

public class SearchVariable extends LHPublicSearch<SearchVariablePb> {

    public VariableCriteriaCase type;
    public VariableValue value;
    public String wfRunId;

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.VARIABLE;
    }

    public Class<SearchVariablePb> getProtoBaseClass() {
        return SearchVariablePb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        SearchVariablePbOrBuilder p = (SearchVariablePbOrBuilder) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                LHUtil.log("Failed to load bookmark:");
                exn.printStackTrace();
            }
        }

        type = p.getVariableCriteriaCase();
        switch (type) {
            case VALUE:
                value = VariableValue.fromProto(p.getValueOrBuilder());
                break;
            case WF_RUN_ID:
                wfRunId = p.getWfRunId();
                break;
            case VARIABLECRITERIA_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }

    public SearchVariablePb.Builder toProto() {
        SearchVariablePb.Builder out = SearchVariablePb.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        switch (type) {
            case VALUE:
                out.setValue(value.toProto());
                break;
            case WF_RUN_ID:
                out.setWfRunId(wfRunId);
                break;
            case VARIABLECRITERIA_NOT_SET:
                throw new RuntimeException("not possible");
        }

        return out;
    }

    public static SearchVariable fromProto(SearchVariablePbOrBuilder proto) {
        SearchVariable out = new SearchVariable();
        out.initFrom(proto);
        return out;
    }

    public LHInternalSubSearch<?> getSubSearch(LHGlobalMetaStores stores) {
        throw new RuntimeException("not possible");
    }
}
