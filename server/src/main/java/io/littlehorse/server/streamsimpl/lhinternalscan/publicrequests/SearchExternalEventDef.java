package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.objectId.ExternalEventDefId;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.ExternalEventDefIdPb;
import io.littlehorse.sdk.common.proto.SearchExternalEventDefPb;
import io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchExternalEventDefReply;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchExternalEventDef
    extends PublicScanRequest<SearchExternalEventDefPb, SearchExternalEventDefReplyPb, ExternalEventDefIdPb, ExternalEventDefId, SearchExternalEventDefReply> {

    public String prefix;

    public Class<SearchExternalEventDefPb> getProtoBaseClass() {
        return SearchExternalEventDefPb.class;
    }

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.EXTERNAL_EVENT_DEF;
    }

    public void initFrom(Message proto) {
        SearchExternalEventDefPb p = (SearchExternalEventDefPb) proto;
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

    public SearchExternalEventDefPb.Builder toProto() {
        SearchExternalEventDefPb.Builder out = SearchExternalEventDefPb.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        if (prefix != null) out.setPrefix(prefix);

        return out;
    }

    public static SearchExternalEventDef fromProto(SearchExternalEventDefPb proto) {
        SearchExternalEventDef out = new SearchExternalEventDef();
        out.initFrom(proto);
        return out;
    }

    @Override
    public TagStorageTypePb indexTypeForSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        return TagStorageTypePb.LOCAL;
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
