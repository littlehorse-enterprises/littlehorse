package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.model.objectId.WfRunId;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.InternalScanPb.TagPrefixScanPb;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.SearchWfRunPb;
import io.littlehorse.jlib.common.proto.SearchWfRunPb.StatusAndSpecPb;
import io.littlehorse.jlib.common.proto.SearchWfRunPb.WfrunCriteriaCase;
import io.littlehorse.jlib.common.proto.SearchWfRunReplyPb;
import io.littlehorse.jlib.common.proto.WfRunIdPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchWfRunReply;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;

public class SearchWfRun
    extends PublicScanRequest<SearchWfRunPb, SearchWfRunReplyPb, WfRunIdPb, WfRunId, SearchWfRunReply> {

    public WfrunCriteriaCase type;
    public StatusAndSpecPb statusAndSpec;

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.WF_RUN;
    }

    public Class<SearchWfRunPb> getProtoBaseClass() {
        return SearchWfRunPb.class;
    }

    public void initFrom(Message proto) {
        SearchWfRunPb p = (SearchWfRunPb) proto;
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

    public static SearchWfRun fromProto(SearchWfRunPb proto) {
        SearchWfRun out = new SearchWfRun();
        out.initFrom(proto);
        return out;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores) {
        InternalScan out = new InternalScan();
        out.storeName = ServerTopology.CORE_STORE;
        out.resultType = ScanResultTypePb.OBJECT_ID;

        if (type == WfrunCriteriaCase.STATUS_AND_SPEC) {
            out.type = ScanBoundaryCase.LOCAL_TAG_PREFIX_SCAN;
            out.localTagPrefixScan =
                TagPrefixScanPb
                    .newBuilder()
                    .addAttributes(
                        new Attribute("wfSpecName", statusAndSpec.getWfSpecName())
                            .toProto()
                    )
                    .addAttributes(
                        new Attribute(
                            "wfSpecVersion",
                            LHUtil.toLHDbVersionFormat(
                                statusAndSpec.getWfSpecVersion()
                            )
                        )
                            .toProto()
                    )
                    .addAttributes(
                        new Attribute("status", statusAndSpec.getStatus().toString())
                            .toProto()
                    )
                    .build();
        } else {
            throw new RuntimeException("Not possible or unimplemented");
        }
        return out;
    }
}
