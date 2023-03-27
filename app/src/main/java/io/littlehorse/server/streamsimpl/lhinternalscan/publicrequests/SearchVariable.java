package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.objectId.VariableId;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.InternalScanPb.TagPrefixScanPb;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.SearchVariablePb;
import io.littlehorse.jlib.common.proto.SearchVariablePb.NameAndValuePb;
import io.littlehorse.jlib.common.proto.SearchVariablePb.VariableCriteriaCase;
import io.littlehorse.jlib.common.proto.SearchVariableReplyPb;
import io.littlehorse.jlib.common.proto.VariableIdPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchVariableReply;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import org.apache.commons.lang3.tuple.Pair;

public class SearchVariable
    extends PublicScanRequest<SearchVariablePb, SearchVariableReplyPb, VariableIdPb, VariableId, SearchVariableReply> {

    public VariableCriteriaCase type;
    public NameAndValuePb value;
    public String wfRunId;

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.VARIABLE;
    }

    public Class<SearchVariablePb> getProtoBaseClass() {
        return SearchVariablePb.class;
    }

    public void initFrom(Message proto) {
        SearchVariablePb p = (SearchVariablePb) proto;
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

    public static SearchVariable fromProto(SearchVariablePb proto) {
        SearchVariable out = new SearchVariable();
        out.initFrom(proto);
        return out;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        InternalScan out = new InternalScan();

        out.storeName = ServerTopology.CORE_STORE;
        out.resultType = ScanResultTypePb.OBJECT_ID;

        if (type == VariableCriteriaCase.WF_RUN_ID) {
            out.type = ScanBoundaryCase.OBJECT_ID_PREFIX;
            out.partitionKey = wfRunId;
            out.objectIdPrefix = wfRunId + "/";
        } else if (type == VariableCriteriaCase.VALUE) {
            out.type = ScanBoundaryCase.LOCAL_TAG_PREFIX_SCAN;

            // This may get more tricky once we add variable schemas...
            VariableValue varval = VariableValue.fromProto(value.getValue());

            Pair<String, String> valuePair = varval.getValueTagPair();

            // This may change depending on the type of the tag. For example,
            // sparse strings (such as emails) may be REMOTE_HASH_UNCOUNTED; whereas
            // hot boolean variables may be LOCAL_UNCOUNTED
            out.partitionKey = null;

            out.localTagPrefixScan =
                TagPrefixScanPb
                    .newBuilder()
                    .addAttributes(
                        new Attribute(valuePair.getLeft(), valuePair.getRight())
                            .toProto()
                    )
                    .addAttributes(
                        new Attribute("name", value.getVarName()).toProto()
                    )
                    .build();
        }

        return out;
    }
}
