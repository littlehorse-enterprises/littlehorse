package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.getable.core.usertaskrun.UserGroupModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest.TaskOwnerCase;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunIdList;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchUserTaskRunReply;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
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
public class SearchUserTaskRunRequestModel
        extends PublicScanRequest<
                SearchUserTaskRunRequest,
                UserTaskRunIdList,
                UserTaskRunId,
                UserTaskRunIdModel,
                SearchUserTaskRunReply> {

    private UserTaskRunStatus status;
    private String userTaskDefName;

    private TaskOwnerCase ownerCase;
    private UserModel user;
    private UserGroupModel userGroup;

    private Date latestStart;
    private Date earliestStart;
    private TagStorageType storageTypePbByStatus;

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.USER_TASK_RUN;
    }

    public Class<SearchUserTaskRunRequest> getProtoBaseClass() {
        return SearchUserTaskRunRequest.class;
    }

    public void initFrom(Message proto) {
        SearchUserTaskRunRequest p = (SearchUserTaskRunRequest) proto;
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
        if (p.hasUserGroup()) userGroup = LHSerializable.fromProto(p.getUserGroup(), UserGroupModel.class);
        if (p.hasUser()) user = LHSerializable.fromProto(p.getUser(), UserModel.class);
        if (p.hasLatestStart()) {
            latestStart = LHUtil.fromProtoTs(p.getLatestStart());
        }
        if (p.hasEarliestStart()) {
            earliestStart = LHUtil.fromProtoTs(p.getEarliestStart());
        }
    }

    public SearchUserTaskRunRequest.Builder toProto() {
        SearchUserTaskRunRequest.Builder out = SearchUserTaskRunRequest.newBuilder();
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
                out.setUserGroup(userGroup.toProto());
                break;
            case USER:
                out.setUser(user.toProto());
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

    public static SearchUserTaskRunRequestModel fromProto(SearchUserTaskRunRequest proto) {
        SearchUserTaskRunRequestModel out = new SearchUserTaskRunRequestModel();
        out.initFrom(proto);
        return out;
    }

    private void validateUserGroupAndUserId() throws LHApiException {
        if (userGroup != null && user != null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Cannot specify UserID and User Group in same search!");
        }
    }

    private Optional<TagStorageType> tagStorageTypePbByStatus() {
        return Optional.ofNullable(status).map(userTaskRunStatusPb -> {
            if (UserTaskRunModel.isRemote(userTaskRunStatusPb)) {
                return TagStorageType.REMOTE;
            } else {
                return TagStorageType.LOCAL;
            }
        });
    }

    private Optional<TagStorageType> tagStorageTypePbByUserId() {
        return Optional.ofNullable(user).map(userId -> TagStorageType.REMOTE);
    }

    @Override
    public List<Attribute> getSearchAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        if (status != null) {
            attributes.add(new Attribute("status", this.getStatus().toString()));
        }
        if (userTaskDefName != null) {
            attributes.add(new Attribute("userTaskDefName", this.getUserTaskDefName()));
        }

        if (user != null) {
            attributes.add(new Attribute("userId", this.getUser().getId()));
            if (this.getUser().getUserGroup() != null) {
                attributes.add(
                        new Attribute("userGroup", this.getUser().getUserGroup().getId()));
            }
        }

        if (userGroup != null) {
            attributes.add(new Attribute("userGroup", this.getUserGroup().getId()));
        }
        return attributes;
    }

    @Override
    public TagStorageType indexTypeForSearch(ReadOnlyMetadataStore stores) throws LHValidationError {
        TagStorageType tagStorageType = tagStorageTypePbByUserId()
                .orElseGet(() -> tagStorageTypePbByStatus().orElse(null));
        if (tagStorageType == null) {
            List<String> searchAttributes =
                    getSearchAttributes().stream().map(Attribute::getEscapedKey).toList();
            Optional<TagStorageType> tagStorageTypePbOptional = getStorageTypeForSearchAttributes(searchAttributes);
            if (tagStorageTypePbOptional.isEmpty()) {
                throw new LHValidationError("There is no index configuration for this search");
            }
            tagStorageType = tagStorageTypePbOptional.get();
        }
        return tagStorageType;
    }

    @Override
    public void validate() throws LHValidationError {
        this.validateUserGroupAndUserId();
        if (getSearchAttributes().isEmpty()) {
            throw new LHValidationError(
                    null, "Must specify at least one of: [status, userTaskDefName, userGroup, userId]");
        }
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new TagScanBoundaryStrategy(
                searchAttributeString, Optional.ofNullable(earliestStart), Optional.ofNullable(latestStart));
    }

    private Optional<TagStorageType> getStorageTypeForSearchAttributes(List<String> attributes) {
        return new UserTaskRunModel()
                .getIndexConfigurations().stream()
                        .filter(getableIndex -> getableIndex.searchAttributesMatch(attributes))
                        .map(GetableIndex::getTagStorageType)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst();
    }
}
