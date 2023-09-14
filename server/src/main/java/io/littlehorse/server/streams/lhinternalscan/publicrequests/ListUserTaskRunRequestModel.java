package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ListUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListUserTaskRunReply;

public class ListUserTaskRunRequestModel
        extends PublicScanRequest<
                ListUserTaskRunRequest, UserTaskRunList, UserTaskRun, UserTaskRunModel, ListUserTaskRunReply> {

    private String wfRunId;

    @Override
    public GeneratedMessageV3.Builder<?> toProto() {
        return ListUserTaskRunRequest.newBuilder().setWfRunId(wfRunId);
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        ListUserTaskRunRequest p = (ListUserTaskRunRequest) proto;
        wfRunId = p.getWfRunId();
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
    public LHStore getStore(ReadOnlyMetadataStore metaStore) {
        return LHStore.CORE;
    }

    @Override
    public TagStorageType indexTypeForSearch(ReadOnlyMetadataStore stores) throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) throws LHApiException {
        return new ObjectIdScanBoundaryStrategy(wfRunId);
    }
}
