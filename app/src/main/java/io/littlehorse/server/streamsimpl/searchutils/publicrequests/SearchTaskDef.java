package io.littlehorse.server.streamsimpl.searchutils.publicrequests;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.SearchTaskDefPb;
import io.littlehorse.common.proto.SearchTaskDefPbOrBuilder;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsimpl.searchutils.LHInternalSubSearch;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearch;

public class SearchTaskDef extends LHPublicSearch<SearchTaskDefPb> {

    public String name;

    public Class<SearchTaskDefPb> getProtoBaseClass() {
        return SearchTaskDefPb.class;
    }

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.TASK_DEF;
    }

    public void initFrom(MessageOrBuilder proto) {
        SearchTaskDefPbOrBuilder p = (SearchTaskDefPbOrBuilder) proto;
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

    public SearchTaskDefPb.Builder toProto() {
        SearchTaskDefPb.Builder out = SearchTaskDefPb.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        out.setName(name);

        return out;
    }

    public static SearchTaskDef fromProto(SearchTaskDefPbOrBuilder proto) {
        SearchTaskDef out = new SearchTaskDef();
        out.initFrom(proto);
        return out;
    }

    public LHInternalSubSearch<?> getSubSearch(LHGlobalMetaStores stores) {
        throw new RuntimeException("not possible");
    }
}
