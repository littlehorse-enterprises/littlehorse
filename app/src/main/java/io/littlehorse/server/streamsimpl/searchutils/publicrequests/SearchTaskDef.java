package io.littlehorse.server.streamsimpl.searchutils.publicrequests;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.BookmarkPb;
import io.littlehorse.jlib.common.proto.GETableClassEnumPb;
import io.littlehorse.jlib.common.proto.LHInternalSearchPb.PrefixCase;
import io.littlehorse.jlib.common.proto.SearchTaskDefPb;
import io.littlehorse.jlib.common.proto.SearchTaskDefPbOrBuilder;
import io.littlehorse.jlib.common.proto.SearchTaskDefReplyPb;
import io.littlehorse.server.streamsimpl.searchutils.LHInternalSearch;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearch;
import io.littlehorse.server.streamsimpl.searchutils.publicsearchreplies.SearchTaskDefReply;

public class SearchTaskDef
    extends LHPublicSearch<SearchTaskDefPb, SearchTaskDefReplyPb, SearchTaskDefReply> {

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

    public LHInternalSearch startInternalSearch(LHGlobalMetaStores stores) {
        LHInternalSearch out = new LHInternalSearch();
        out.prefixType = PrefixCase.OBJECT_ID_PREFIX;
        out.partitionKey = LHConstants.META_PARTITION_KEY;
        if (name.equals("")) {
            // Because we want to search all
            out.objectIdPrefix = "";
        } else {
            // Want to make sure we only search if name matches.
            out.objectIdPrefix = name + "/";
        }
        return out;
    }
}
