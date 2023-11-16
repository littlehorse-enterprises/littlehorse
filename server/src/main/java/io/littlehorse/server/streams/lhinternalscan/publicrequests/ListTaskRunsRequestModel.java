package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.dao.ReadOnlyMetadataDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ListTaskRunsRequest;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListTaskRunsReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class ListTaskRunsRequestModel
        extends PublicScanRequest<ListTaskRunsRequest, TaskRunList, TaskRun, TaskRunModel, ListTaskRunsReply> {

    private String wfRunId;

    @Override
    public GeneratedMessageV3.Builder<?> toProto() {
        return ListTaskRunsRequest.newBuilder().setWfRunId(wfRunId);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        ListTaskRunsRequest p = (ListTaskRunsRequest) proto;
        wfRunId = p.getWfRunId();
    }

    @Override
    public Class<ListTaskRunsRequest> getProtoBaseClass() {
        return ListTaskRunsRequest.class;
    }

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.TASK_RUN;
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
    public TagStorageType indexTypeForSearch(ReadOnlyMetadataDAO readOnlyDao) throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) throws LHApiException {
        return new ObjectIdScanBoundaryStrategy(wfRunId);
    }
}
