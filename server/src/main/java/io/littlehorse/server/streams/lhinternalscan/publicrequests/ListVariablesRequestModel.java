package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.sdk.common.proto.ListVariablesRequest;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListVariablesReply;
import io.littlehorse.server.streams.lhinternalscan.util.BoundedObjectIdScanModel;
import io.littlehorse.server.streams.lhinternalscan.util.ScanBoundary;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;

public class ListVariablesRequestModel
        extends PublicScanRequest<ListVariablesRequest, VariableList, Variable, VariableModel, ListVariablesReply> {

    private WfRunIdModel wfRunId;

    @Override
    public Class<ListVariablesRequest> getProtoBaseClass() {
        return ListVariablesRequest.class;
    }

    @Override
    public ListVariablesRequest.Builder toProto() {
        return ListVariablesRequest.newBuilder().setWfRunId(wfRunId.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ListVariablesRequest p = (ListVariablesRequest) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
    }

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.VARIABLE;
    }

    @Override
    public ScanResultTypePb getResultType() {
        return ScanResultTypePb.OBJECT;
    }

    @Override
    public ScanBoundary<?, VariableIdModel> getScanBoundary(RequestExecutionContext ctx) {
        return new BoundedObjectIdScanModel<>(GetableClassEnum.VARIABLE, wfRunId);
    }

    @Override
    public LHStoreType getStoreType() {
        return LHStoreType.CORE;
    }
}
