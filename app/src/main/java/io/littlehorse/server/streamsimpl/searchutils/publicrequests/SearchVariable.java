package io.littlehorse.server.streamsimpl.searchutils.publicrequests;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.LHInternalSearchPb.PrefixCase;
import io.littlehorse.common.proto.SearchVariablePb;
import io.littlehorse.common.proto.SearchVariablePb.NameAndValuePb;
import io.littlehorse.common.proto.SearchVariablePb.VariableCriteriaCase;
import io.littlehorse.common.proto.SearchVariablePbOrBuilder;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsimpl.searchutils.LHInternalSearch;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearch;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import org.apache.commons.lang3.tuple.Pair;

public class SearchVariable extends LHPublicSearch<SearchVariablePb> {

    public VariableCriteriaCase type;
    public NameAndValuePb value;
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
                value = p.getValue();
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
                out.setValue(value);
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

    public LHInternalSearch startInternalSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        LHInternalSearch out = new LHInternalSearch();

        if (type == VariableCriteriaCase.WF_RUN_ID) {
            out.prefixType = PrefixCase.OBJECT_ID_PREFIX;
            out.partitionKey = wfRunId;
            out.objectIdPrefix = wfRunId + "/";
        } else if (type == VariableCriteriaCase.VALUE) {
            out.prefixType = PrefixCase.TAG_PREFIX;

            // This may get more tricky once we add variable schemas...
            VariableValue varval = VariableValue.fromProto(value.getValueOrBuilder());

            Pair<String, String> valuePair = varval.getValueTagPair();

            // This may change depending on the type of the tag. For example,
            // sparse strings (such as emails) may be REMOTE_HASH_UNCOUNTED; whereas
            // hot boolean variables may be LOCAL_UNCOUNTED
            out.partitionKey = null;
            out.tagPrefix.add(
                new Attribute(valuePair.getLeft(), valuePair.getRight())
            );
            out.tagPrefix.add(new Attribute("name", value.getVarName()));
        }

        return out;
    }
}
