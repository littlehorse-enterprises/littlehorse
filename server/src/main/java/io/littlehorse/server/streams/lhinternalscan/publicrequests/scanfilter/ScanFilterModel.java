package io.littlehorse.server.streams.lhinternalscan.publicrequests.scanfilter;

import com.google.protobuf.Message;

import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.ScanFilter;
import io.littlehorse.common.proto.ScanFilter.CriteriaCase;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.VariableMatchModel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;


public class ScanFilterModel extends LHSerializable<ScanFilter> {

    private CriteriaCase type;
    private LHStatus wfRunStatus;
    private VariableMatchModel variableMatch;
    
    @Override
    public Class<ScanFilter> getProtoBaseClass() {
        return ScanFilter.class;
    }

    @Override
    public ScanFilter.Builder toProto() {
        ScanFilter.Builder out = ScanFilter.newBuilder();

        switch(type) {
            case VARIABLE_MATCH:
                out.setVariableMatch(variableMatch.toProto());
                break;
            case WF_RUN_STATUS:
                out.setWfRunStatus(wfRunStatus);
                break;
            case CRITERIA_NOT_SET:
                // not really possible
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        ScanFilter p = (ScanFilter) proto;
        type = p.getCriteriaCase();
        switch(type) {
            case VARIABLE_MATCH:
                variableMatch = LHSerializable.fromProto(p.getVariableMatch(), VariableMatchModel.class, ctx);
                break;
            case WF_RUN_STATUS:
                wfRunStatus = p.getWfRunStatus();
                break;
            case CRITERIA_NOT_SET:
                // not really possible
        }
    }

    public boolean matches(WfRunIdModel candidate, RequestExecutionContext ctx) {
        if (type == CriteriaCase.VARIABLE_MATCH) {
            return variableMatch.matchesCriteria(candidate, ctx);
        } else if (type == CriteriaCase.WF_RUN_STATUS) {
            WfRunModel wfRun = ctx.getableManager().get(candidate);
            return wfRun.getStatus() == wfRunStatus;
        }
        throw new LHApiException(Status.INVALID_ARGUMENT, "Encountered WfRunFilter without criteria");
    }
}
