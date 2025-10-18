package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunIdList;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchUserTaskRunReply;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
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

    private String userId;
    private String userGroup;

    private Date latestStart;
    private Date earliestStart;
    private TagStorageType storageTypePbByStatus;

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.USER_TASK_RUN;
    }

    public Class<SearchUserTaskRunRequest> getProtoBaseClass() {
        return SearchUserTaskRunRequest.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SearchUserTaskRunRequest p = (SearchUserTaskRunRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        if (p.hasUserGroup()) userGroup = p.getUserGroup();
        if (p.hasUserId()) userId = p.getUserId();

        if (p.hasStatus()) status = p.getStatus();
        if (p.hasUserTaskDefName()) userTaskDefName = p.getUserTaskDefName();

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

        if (userGroup != null) out.setUserGroup(userGroup);
        if (userId != null) out.setUserId(userId);

        if (status != null) out.setStatus(status);
        if (userTaskDefName != null) out.setUserTaskDefName(userTaskDefName);

        if (latestStart != null) {
            out.setLatestStart(LHUtil.fromDate(latestStart));
        }
        if (earliestStart != null) {
            out.setEarliestStart(LHUtil.fromDate(earliestStart));
        }

        return out;
    }

    public static SearchUserTaskRunRequestModel fromProto(SearchUserTaskRunRequest proto, ExecutionContext context) {
        SearchUserTaskRunRequestModel out = new SearchUserTaskRunRequestModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public List<Attribute> getSearchAttributes() {
        // Ordering is important. See UserTaskRunModel#getIndexConfigurations()

        List<Attribute> attributes = new ArrayList<>();
        if (status != null) {
            attributes.add(new Attribute("status", this.getStatus().toString()));
        }
        if (userTaskDefName != null) {
            attributes.add(new Attribute("userTaskDefName", this.getUserTaskDefName()));
        }

        if (userId != null) {
            attributes.add(new Attribute("userId", this.userId));
        }

        if (userGroup != null) {
            attributes.add(new Attribute("userGroup", this.userGroup));
        }
        if (attributes.isEmpty()) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "Must provide at least one search criteria: status,user_id,user_group,user_task_def_name");
        }
        return attributes;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        // Everything is local.
        return TagStorageType.LOCAL;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new TagScanBoundaryStrategy(
                searchAttributeString, Optional.ofNullable(earliestStart), Optional.ofNullable(latestStart));
    }

    // private Optional<TagStorageType> getStorageTypeForSearchAttributes(List<String> attributes) {
    //     return new UserTaskRunModel()
    //             .getIndexConfigurations().stream()
    //                     .filter(getableIndex -> getableIndex.searchAttributesMatch(attributes))
    //                     .map(GetableIndex::getTagStorageType)
    //                     .filter(Optional::isPresent)
    //                     .map(Optional::get)
    //                     .findFirst();
    // }

    public LHStore getStoreType() {
        return indexTypeForSearch() == TagStorageType.LOCAL ? LHStore.CORE : LHStore.REPARTITION;
    }
}
