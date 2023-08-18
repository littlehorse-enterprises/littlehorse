package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.objectId.TaskDefIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.SearchTaskDefRequest;
import io.littlehorse.sdk.common.proto.SearchTaskDefResponse;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchTaskDefReply;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchTaskDefRequestModel
        extends PublicScanRequest<
                SearchTaskDefRequest,
                SearchTaskDefResponse,
                TaskDefId,
                TaskDefIdModel,
                SearchTaskDefReply> {

    public String prefix;

    public Class<SearchTaskDefRequest> getProtoBaseClass() {
        return SearchTaskDefRequest.class;
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.TASK_DEF;
    }

    public void initFrom(Message proto) {
        SearchTaskDefRequest p = (SearchTaskDefRequest) proto;
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

    public SearchTaskDefRequest.Builder toProto() {
        SearchTaskDefRequest.Builder out = SearchTaskDefRequest.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        if (prefix != null) out.setPrefix(prefix);

        return out;
    }

    public static SearchTaskDefRequestModel fromProto(SearchTaskDefRequest proto) {
        SearchTaskDefRequestModel out = new SearchTaskDefRequestModel();
        out.initFrom(proto);
        return out;
    }

    @Override
    public TagStorageType indexTypeForSearch(LHGlobalMetaStores stores) throws LHValidationError {
        return TagStorageType.LOCAL;
    }

    @Override
    public void validate() throws LHValidationError {}

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        if (prefix != null && !prefix.equals("")) {
            return new ObjectIdScanBoundaryStrategy(
                    LHConstants.META_PARTITION_KEY, prefix, prefix + "~");
        } else {
            return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, "", "~");
        }
    }
}
