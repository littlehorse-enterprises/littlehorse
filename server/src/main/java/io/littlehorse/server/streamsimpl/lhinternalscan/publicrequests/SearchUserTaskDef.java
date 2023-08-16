package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.objectId.UserTaskDefIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.SearchUserTaskDefPb;
import io.littlehorse.sdk.common.proto.SearchUserTaskDefPb.UserTaskDefCriteriaCase;
import io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb;
import io.littlehorse.sdk.common.proto.UserTaskDefIdPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchUserTaskDefReply;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

// TODO: there seems to be quite a bit of code duplication here, see SearchWfSpec.
@Slf4j
@Getter
@Setter
public class SearchUserTaskDef
    extends PublicScanRequest<SearchUserTaskDefPb, SearchUserTaskDefReplyPb, UserTaskDefIdPb, UserTaskDefIdModel, SearchUserTaskDefReply> {

    private UserTaskDefCriteriaCase type;
    private String name;
    private String prefix;

    public Class<SearchUserTaskDefPb> getProtoBaseClass() {
        return SearchUserTaskDefPb.class;
    }

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.USER_TASK_DEF;
    }

    public void initFrom(Message proto) {
        SearchUserTaskDefPb p = (SearchUserTaskDefPb) proto;
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

    public SearchUserTaskDefPb.Builder toProto() {
        SearchUserTaskDefPb.Builder out = SearchUserTaskDefPb.newBuilder();
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

    public static SearchUserTaskDef fromProto(SearchUserTaskDefPb proto) {
        SearchUserTaskDef out = new SearchUserTaskDef();
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
        } else if (name != null && !name.isEmpty()) {
            return new ObjectIdScanBoundaryStrategy(
                LHConstants.META_PARTITION_KEY,
                name,
                name + "/"
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
