package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.objectId.ExternalEventDefId;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.ExternalEventDefIdPb;
import io.littlehorse.jlib.common.proto.SearchExternalEventDefPb;
import io.littlehorse.jlib.common.proto.SearchExternalEventDefReplyPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchExternalEventDefReply;

public class SearchExternalEventDef
    extends PublicScanRequest<SearchExternalEventDefPb, SearchExternalEventDefReplyPb, ExternalEventDefIdPb, ExternalEventDefId, SearchExternalEventDefReply> {

    public String name;

    public Class<SearchExternalEventDefPb> getProtoBaseClass() {
        return SearchExternalEventDefPb.class;
    }

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.EXTERNAL_EVENT_DEF;
    }

    public void initFrom(Message proto) {
        SearchExternalEventDefPb p = (SearchExternalEventDefPb) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                LHUtil.log("Failed to load bookmark:");
                exn.printStackTrace();
            }
        }

        name = p.getName();
    }

    public SearchExternalEventDefPb.Builder toProto() {
        SearchExternalEventDefPb.Builder out = SearchExternalEventDefPb.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        out.setName(name);

        return out;
    }

    public static SearchExternalEventDef fromProto(SearchExternalEventDefPb proto) {
        SearchExternalEventDef out = new SearchExternalEventDef();
        out.initFrom(proto);
        return out;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores) {
        InternalScan out = new InternalScan();
        out.type = ScanBoundaryCase.OBJECT_ID_PREFIX;
        out.partitionKey = LHConstants.META_PARTITION_KEY;

        out.storeName = ServerTopology.CORE_STORE;
        out.resultType = ScanResultTypePb.OBJECT_ID;

        System.out.print(name);
        if (name.equals("")) {
            // that means we want to search all ExternalEventDefs
            out.objectIdPrefix = "";
        } else {
            out.objectIdPrefix = name + "/";
        }
        return out;
    }
}
