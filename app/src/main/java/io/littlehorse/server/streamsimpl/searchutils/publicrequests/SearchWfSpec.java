package io.littlehorse.server.streamsimpl.searchutils.publicrequests;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.BookmarkPb;
import io.littlehorse.jlib.common.proto.GETableClassEnumPb;
import io.littlehorse.jlib.common.proto.LHInternalSearchPb.PrefixCase;
import io.littlehorse.jlib.common.proto.SearchWfSpecPb;
import io.littlehorse.jlib.common.proto.SearchWfSpecPbOrBuilder;
import io.littlehorse.server.streamsimpl.searchutils.LHInternalSearch;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearch;

public class SearchWfSpec extends LHPublicSearch<SearchWfSpecPb> {

    public String name;

    public Class<SearchWfSpecPb> getProtoBaseClass() {
        return SearchWfSpecPb.class;
    }

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.WF_SPEC;
    }

    public void initFrom(MessageOrBuilder proto) {
        SearchWfSpecPbOrBuilder p = (SearchWfSpecPbOrBuilder) proto;
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

    public SearchWfSpecPb.Builder toProto() {
        SearchWfSpecPb.Builder out = SearchWfSpecPb.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        out.setName(name);

        return out;
    }

    public static SearchWfSpec fromProto(SearchWfSpecPbOrBuilder proto) {
        SearchWfSpec out = new SearchWfSpec();
        out.initFrom(proto);
        return out;
    }

    public LHInternalSearch startInternalSearch(LHGlobalMetaStores stores) {
        LHInternalSearch out = new LHInternalSearch();
        out.prefixType = PrefixCase.OBJECT_ID_PREFIX;
        out.partitionKey = LHConstants.META_PARTITION_KEY;
        System.out.print(name);
        if (name.equals("")) {
            // that means we want to search all wfSpecs
            out.objectIdPrefix = "";
        } else {
            out.objectIdPrefix = name + "/";
        }
        return out;
    }
}
