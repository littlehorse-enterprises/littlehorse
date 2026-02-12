package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ListUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListUserTaskRunReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class ListUserTaskRunRequestModel
        extends PublicScanRequest<
                ListUserTaskRunRequest, UserTaskRunList, UserTaskRun, UserTaskRunModel, ListUserTaskRunReply> {

    private WfRunIdModel wfRunId;

    @Override
    public GeneratedMessage.Builder<?> toProto() {
        ListUserTaskRunRequest.Builder out = ListUserTaskRunRequest.newBuilder().setWfRunId(wfRunId.toProto());
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ListUserTaskRunRequest p = (ListUserTaskRunRequest) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
        if (!p.getBookmark().isEmpty()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }
        if (p.hasLimit()) {
            limit = p.getLimit();
        }
    }

    @Override
    public Class<ListUserTaskRunRequest> getProtoBaseClass() {
        return ListUserTaskRunRequest.class;
    }

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.USER_TASK_RUN;
    }

    @Override
    public ScanResultTypePb getResultType() {
        return ScanResultTypePb.OBJECT;
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.CORE;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) throws LHApiException {
        return ObjectIdScanBoundaryStrategy.from(wfRunId);
    }
}
