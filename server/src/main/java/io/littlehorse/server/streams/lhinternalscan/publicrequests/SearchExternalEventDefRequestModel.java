package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.dao.ReadOnlyMetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.ExternalEventDefIdList;
import io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchExternalEventDefReply;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchExternalEventDefRequestModel
        extends PublicScanRequest<
                SearchExternalEventDefRequest,
                ExternalEventDefIdList,
                ExternalEventDefId,
                ExternalEventDefIdModel,
                SearchExternalEventDefReply> {

    public String prefix;

    public Class<SearchExternalEventDefRequest> getProtoBaseClass() {
        return SearchExternalEventDefRequest.class;
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.GLOBAL_METADATA;
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.EXTERNAL_EVENT_DEF;
    }

    public void initFrom(Message proto) {
        SearchExternalEventDefRequest p = (SearchExternalEventDefRequest) proto;
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

    public SearchExternalEventDefRequest.Builder toProto() {
        SearchExternalEventDefRequest.Builder out = SearchExternalEventDefRequest.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        if (prefix != null) out.setPrefix(prefix);

        return out;
    }

    public static SearchExternalEventDefRequestModel fromProto(SearchExternalEventDefRequest proto) {
        SearchExternalEventDefRequestModel out = new SearchExternalEventDefRequestModel();
        out.initFrom(proto);
        return out;
    }

    @Override
    public TagStorageType indexTypeForSearch(ReadOnlyMetadataProcessorDAO readOnlyDao) throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        if (prefix != null && !prefix.equals("")) {
            return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, prefix, prefix + "~");
        } else {
            return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, "", "~");
        }
    }
}
