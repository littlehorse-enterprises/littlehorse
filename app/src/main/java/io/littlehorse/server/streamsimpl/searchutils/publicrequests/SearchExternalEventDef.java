package io.littlehorse.server.streamsimpl.searchutils.publicrequests;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.LHInternalSearchPb.PrefixCase;
import io.littlehorse.common.proto.SearchExternalEventDefPb;
import io.littlehorse.common.proto.SearchExternalEventDefPbOrBuilder;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsimpl.searchutils.LHInternalSearch;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearch;

public class SearchExternalEventDef extends LHPublicSearch<SearchExternalEventDefPb> {

    public String name;

    public Class<SearchExternalEventDefPb> getProtoBaseClass() {
        return SearchExternalEventDefPb.class;
    }

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.EXTERNAL_EVENT_DEF;
    }

    public void initFrom(MessageOrBuilder proto) {
        SearchExternalEventDefPbOrBuilder p = (SearchExternalEventDefPbOrBuilder) proto;
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

    public static SearchExternalEventDef fromProto(
        SearchExternalEventDefPbOrBuilder proto
    ) {
        SearchExternalEventDef out = new SearchExternalEventDef();
        out.initFrom(proto);
        return out;
    }

    public LHInternalSearch startInternalSearch(LHGlobalMetaStores stores) {
        LHInternalSearch out = new LHInternalSearch();
        out.prefixType = PrefixCase.OBJECT_ID_PREFIX;
        out.partitionKey = LHConstants.META_PARTITION_KEY;
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
