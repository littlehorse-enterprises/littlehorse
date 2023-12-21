package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunIdList;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchUserTaskRunReply;
import io.littlehorse.server.streams.lhinternalscan.util.TagScanModel;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.Date;
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
    public TagScanModel getScanBoundary(RequestExecutionContext ctx) {
        // Ordering is important. See UserTaskRunModel#getIndexConfigurations()
        TagScanModel attributes = new TagScanModel(GetableClassEnum.USER_TASK_RUN);
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

        attributes.setEarliestCreateTime(earliestStart);
        attributes.setLatestCreateTime(latestStart);
        return attributes;
    }

    @Override
    public LHStoreType getStoreType() {
        return LHStoreType.CORE;
    }
}
