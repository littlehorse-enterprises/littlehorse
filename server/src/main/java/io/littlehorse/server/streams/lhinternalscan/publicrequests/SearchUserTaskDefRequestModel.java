package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.dao.ReadOnlyMetadataDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest.UserTaskDefCriteriaCase;
import io.littlehorse.sdk.common.proto.UserTaskDefId;
import io.littlehorse.sdk.common.proto.UserTaskDefIdList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchUserTaskDefReply;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

// TODO: there seems to be quite a bit of code duplication here, see SearchWfSpec.
@Slf4j
@Getter
@Setter
public class SearchUserTaskDefRequestModel
        extends PublicScanRequest<
                SearchUserTaskDefRequest,
                UserTaskDefIdList,
                UserTaskDefId,
                UserTaskDefIdModel,
                SearchUserTaskDefReply> {

    private UserTaskDefCriteriaCase type;
    private String name;
    private String prefix;

    public Class<SearchUserTaskDefRequest> getProtoBaseClass() {
        return SearchUserTaskDefRequest.class;
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.GLOBAL_METADATA;
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.USER_TASK_DEF;
    }

    public void initFrom(Message proto) {
        SearchUserTaskDefRequest p = (SearchUserTaskDefRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        type = p.getUserTaskDefCriteriaCase();
        switch (type) {
            case NAME:
                name = p.getName();
                break;
            case PREFIX:
                prefix = p.getPrefix();
                break;
            case USERTASKDEFCRITERIA_NOT_SET:
                // nothing to do, we just return all the UserTaskDef's.
        }
    }

    public SearchUserTaskDefRequest.Builder toProto() {
        SearchUserTaskDefRequest.Builder out = SearchUserTaskDefRequest.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        switch (type) {
            case NAME:
                out.setName(name);
                break;
            case PREFIX:
                out.setPrefix(prefix);
                break;
            case USERTASKDEFCRITERIA_NOT_SET:
                // nothing to do, we just return all the UserTaskDef's.
        }
        return out;
    }

    public static SearchUserTaskDefRequestModel fromProto(SearchUserTaskDefRequest proto) {
        SearchUserTaskDefRequestModel out = new SearchUserTaskDefRequestModel();
        out.initFrom(proto);
        return out;
    }

    @Override
    public TagStorageType indexTypeForSearch(ReadOnlyMetadataDAO readOnlyDao) throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        if (prefix != null && !prefix.equals("")) {
            return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, prefix, prefix + "~");
        } else if (name != null && !name.isEmpty()) {
            return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, name, name + "/");
        } else {
            return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, "", "~");
        }
    }
}
