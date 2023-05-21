package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.SearchWfSpecPb;
import io.littlehorse.jlib.common.proto.SearchWfSpecReplyPb;
import io.littlehorse.jlib.common.proto.WfSpecIdPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchWfSpecReply;

public class SearchWfSpec
    extends PublicScanRequest<SearchWfSpecPb, SearchWfSpecReplyPb, WfSpecIdPb, WfSpecId, SearchWfSpecReply> {

    public String name;
    public String prefix;

    public Class<SearchWfSpecPb> getProtoBaseClass() {
        return SearchWfSpecPb.class;
    }

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.WF_SPEC;
    }

    public void initFrom(Message proto) {
        SearchWfSpecPb p = (SearchWfSpecPb) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                LHUtil.log("Failed to load bookmark:");
                exn.printStackTrace();
            }
        }

        if (p.hasName()) name = p.getName();
        if (p.hasPrefix()) prefix = p.getPrefix();
    }

    public SearchWfSpecPb.Builder toProto() {
        SearchWfSpecPb.Builder out = SearchWfSpecPb.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        if (name != null) out.setName(name);
        if (prefix != null) out.setPrefix(prefix);

        return out;
    }

    public static SearchWfSpec fromProto(SearchWfSpecPb proto) {
        SearchWfSpec out = new SearchWfSpec();
        out.initFrom(proto);
        return out;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores) {
        InternalScan out = new InternalScan();
        out.storeName = ServerTopology.CORE_STORE;
        out.resultType = ScanResultTypePb.OBJECT_ID;
        out.type = ScanBoundaryCase.BOUNDED_OBJECT_ID_SCAN;
        out.partitionKey = LHConstants.META_PARTITION_KEY;

        if (name != null && !name.equals("")) {
            // exact match on name
            out.boundedObjectIdScan =
                BoundedObjectIdScanPb
                    .newBuilder()
                    .setStartObjectId(name + "/")
                    .setEndObjectId(name + "/~")
                    .build();
        } else if (prefix != null && !prefix.equals("")) {
            // Prefix scan on name
            out.boundedObjectIdScan =
                BoundedObjectIdScanPb
                    .newBuilder()
                    .setStartObjectId(prefix)
                    .setEndObjectId(prefix + "~")
                    .build();
        } else {
            // that means we want to search all wfSpecs
            out.boundedObjectIdScan =
                BoundedObjectIdScanPb.newBuilder().setStartObjectId("").build();
        }

        return out;
    }
}
