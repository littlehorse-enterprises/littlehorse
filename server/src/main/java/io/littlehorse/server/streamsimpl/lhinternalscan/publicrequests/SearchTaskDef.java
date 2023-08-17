package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.objectId.TaskDefIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.SearchTaskDefPb;
import io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchTaskDefReply;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchTaskDef
    extends PublicScanRequest<SearchTaskDefPb, SearchTaskDefReplyPb, TaskDefId, TaskDefIdModel, SearchTaskDefReply> {

    public String prefix;

    public Class<SearchTaskDefPb> getProtoBaseClass() {
        return SearchTaskDefPb.class;
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.TASK_DEF;
    }

    public void initFrom(Message proto) {
        SearchTaskDefPb p = (SearchTaskDefPb) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }
        if (p.hasPrefix()) prefix = p.getPrefix();
    }

    public SearchTaskDefPb.Builder toProto() {
        SearchTaskDefPb.Builder out = SearchTaskDefPb.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        if (prefix != null) out.setPrefix(prefix);

        return out;
    }

    public static SearchTaskDef fromProto(SearchTaskDefPb proto) {
        SearchTaskDef out = new SearchTaskDef();
        out.initFrom(proto);
        return out;
    }

    @Override
    public TagStorageType indexTypeForSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        return TagStorageType.LOCAL;
    }

    @Override
    public void validate() throws LHValidationError {}

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        if (prefix != null && !prefix.equals("")) {
            return new ObjectIdScanBoundaryStrategy(
                LHConstants.META_PARTITION_KEY,
                prefix,
                prefix + "~"
            );
        } else {
            return new ObjectIdScanBoundaryStrategy(
                LHConstants.META_PARTITION_KEY,
                "",
                "~"
            );
        }
    }
}
