package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.objectId.TaskDefId;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.SearchTaskDefPb;
import io.littlehorse.jlib.common.proto.SearchTaskDefReplyPb;
import io.littlehorse.jlib.common.proto.TaskDefIdPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchTaskDefReply;

public class SearchTaskDef
    extends PublicScanRequest<SearchTaskDefPb, SearchTaskDefReplyPb, TaskDefIdPb, TaskDefId, SearchTaskDefReply> {

    public String name;

    public Class<SearchTaskDefPb> getProtoBaseClass() {
        return SearchTaskDefPb.class;
    }

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.TASK_DEF;
    }

    public void initFrom(Message proto) {
        SearchTaskDefPb p = (SearchTaskDefPb) proto;
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

    public static SearchTaskDef fromProto(SearchTaskDefPb proto) {
        SearchTaskDef out = new SearchTaskDef();
        out.initFrom(proto);
        return out;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores) {
        InternalScan out = new InternalScan();
        out.type = ScanBoundaryCase.OBJECT_ID_PREFIX;
        out.storeName = ServerTopology.CORE_STORE;
        out.resultType = ScanResultTypePb.OBJECT_ID;
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
