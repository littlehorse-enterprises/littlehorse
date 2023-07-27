package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.objectId.UserTaskRunId;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunPb;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunPb.TaskOwnerCase;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.UserTaskRunIdPb;
import io.littlehorse.sdk.common.proto.UserTaskRunStatusPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchUserTaskRunReply;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class SearchUserTaskRun
    extends PublicScanRequest<SearchUserTaskRunPb, SearchUserTaskRunReplyPb, UserTaskRunIdPb, UserTaskRunId, SearchUserTaskRunReply> {

    private UserTaskRunStatusPb status;
    private String userTaskDefName;

    private TaskOwnerCase ownerCase;
    private String userId;
    private String userGroup;

    private Date latestStart;
    private Date earliestStart;
    private TagStorageTypePb storageTypePbByStatus;

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.USER_TASK_RUN;
    }

    public Class<SearchUserTaskRunPb> getProtoBaseClass() {
        return SearchUserTaskRunPb.class;
    }

    public void initFrom(Message proto) {
        SearchUserTaskRunPb p = (SearchUserTaskRunPb) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        if (p.hasStatus()) status = p.getStatus();
        if (p.hasUserTaskDefName()) userTaskDefName = p.getUserTaskDefName();

        ownerCase = p.getTaskOwnerCase();
        // Note: Typically, we would do as above. However, if a client (eg. the
        // grpc-gateway) sets both userId and userGroup, the way protobuf works
        // dictates that we would search by userGroup (since it has a higher
        // field number) and ignore userId silently. By using the way below,
        // we can throw an LHValidationError when processing the search.
        if (p.hasUserGroup()) userGroup = p.getUserGroup();
        if (p.hasUserId()) userId = p.getUserId();
        if (p.hasLatestStart()) {
            latestStart = LHUtil.fromProtoTs(p.getLatestStart());
        }
        if (p.hasEarliestStart()) {
            earliestStart = LHUtil.fromProtoTs(p.getEarliestStart());
        }
    }

    public SearchUserTaskRunPb.Builder toProto() {
        SearchUserTaskRunPb.Builder out = SearchUserTaskRunPb.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }

        if (status != null) out.setStatus(status);
        if (userTaskDefName != null) out.setUserTaskDefName(userTaskDefName);

        switch (ownerCase) {
            case USER_GROUP:
                out.setUserGroup(userGroup);
                break;
            case USER_ID:
                out.setUserId(userId);
                break;
            case TASKOWNER_NOT_SET:
            // nothing to do
        }

        if (latestStart != null) {
            out.setLatestStart(LHUtil.fromDate(latestStart));
        }
        if (earliestStart != null) {
            out.setEarliestStart(LHUtil.fromDate(earliestStart));
        }

        return out;
    }

    public static SearchUserTaskRun fromProto(SearchUserTaskRunPb proto) {
        SearchUserTaskRun out = new SearchUserTaskRun();
        out.initFrom(proto);
        return out;
    }

    private void validateUserGroupAndUserId() throws LHValidationError {
        if (userGroup != null && userId != null) {
            throw new LHValidationError(
                null,
                "Cannot specify UserID and User Group in same search!"
            );
        }
    }

    private Optional<TagStorageTypePb> tagStorageTypePbByStatus() {
        return Optional
            .ofNullable(status)
            .map(userTaskRunStatusPb -> {
                if (UserTaskRun.isRemote(userTaskRunStatusPb)) {
                    return TagStorageTypePb.REMOTE;
                } else {
                    return TagStorageTypePb.LOCAL;
                }
            });
    }

    private Optional<TagStorageTypePb> tagStorageTypePbByUserId() {
        return Optional.ofNullable(userId).map(userId -> TagStorageTypePb.REMOTE);
    }

    @Override
    public List<Attribute> getSearchAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        if (status != null) {
            attributes.add(new Attribute("status", this.getStatus().toString()));
        }
        if (userTaskDefName != null) {
            attributes.add(
                new Attribute("userTaskDefName", this.getUserTaskDefName())
            );
        }

        if (userId != null) {
            attributes.add(new Attribute("userId", this.getUserId()));
        }

        if (userGroup != null) {
            attributes.add(new Attribute("userGroup", this.getUserGroup()));
        }
        return attributes;
    }

    @Override
    public TagStorageTypePb indexTypeForSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        TagStorageTypePb tagStorageTypePb = tagStorageTypePbByUserId()
            .orElseGet(() -> tagStorageTypePbByStatus().orElse(null));
        if (tagStorageTypePb == null) {
            List<String> searchAttributes = getSearchAttributes()
                .stream()
                .map(Attribute::getEscapedKey)
                .toList();
            Optional<TagStorageTypePb> tagStorageTypePbOptional = getStorageTypeForSearchAttributes(
                searchAttributes
            );
            if (tagStorageTypePbOptional.isEmpty()) {
                throw new LHValidationError(
                    "There is no index configuration for this search"
                );
            }
            tagStorageTypePb = tagStorageTypePbOptional.get();
        }
        return tagStorageTypePb;
    }

    @Override
    public void validate() throws LHValidationError {
        this.validateUserGroupAndUserId();
        if (getSearchAttributes().isEmpty()) {
            throw new LHValidationError(
                null,
                "Must specify at least one of: [status, userTaskDefName, userGroup, userId]"
            );
        }
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new TagScanBoundaryStrategy(
            searchAttributeString,
            Optional.ofNullable(earliestStart),
            Optional.ofNullable(latestStart)
        );
    }

    private Optional<TagStorageTypePb> getStorageTypeForSearchAttributes(
        List<String> attributes
    ) {
        return new UserTaskRun()
            .getIndexConfigurations()
            .stream()
            .filter(getableIndex -> getableIndex.searchAttributesMatch(attributes))
            .map(GetableIndex::getTagStorageTypePb)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }
}
