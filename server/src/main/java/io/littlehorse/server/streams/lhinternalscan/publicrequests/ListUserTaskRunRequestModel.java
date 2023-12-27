package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ListUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListUserTaskRunReply;
import io.littlehorse.server.streams.lhinternalscan.util.BoundedObjectIdScanModel;
import io.littlehorse.server.streams.lhinternalscan.util.ScanBoundary;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;

public class ListUserTaskRunRequestModel
        extends PublicScanRequest<
                ListUserTaskRunRequest, UserTaskRunList, UserTaskRun, UserTaskRunModel, ListUserTaskRunReply> {

    private WfRunIdModel wfRunId;

    @Override
    public GeneratedMessageV3.Builder<?> toProto() {
        return ListUserTaskRunRequest.newBuilder().setWfRunId(wfRunId.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        ListUserTaskRunRequest p = (ListUserTaskRunRequest) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
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
    public LHStoreType getStoreType() {
        return LHStoreType.CORE;
    }

    @Override
    public ScanBoundary<?, TaskRunIdModel> getScanBoundary(RequestExecutionContext ctx) throws LHApiException {
        return new BoundedObjectIdScanModel<>(GetableClassEnum.USER_TASK_RUN, wfRunId);
    }
}
