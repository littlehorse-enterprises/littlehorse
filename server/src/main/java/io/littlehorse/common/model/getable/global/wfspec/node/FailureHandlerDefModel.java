package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.FailureHandlerDef;
import io.littlehorse.sdk.common.proto.FailureHandlerDef.LHFailureType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class FailureHandlerDefModel extends LHSerializable<FailureHandlerDef> {

    public String specificFailure;
    public String handlerSpecName;

    public NodeModel node;

    private LHFailureType type;

    public FailureHandlerDefModel() {}

    public Class<FailureHandlerDef> getProtoBaseClass() {
        return FailureHandlerDef.class;
    }

    public FailureHandlerDef.Builder toProto() {
        FailureHandlerDef.Builder out = FailureHandlerDef.newBuilder().setHandlerSpecName(handlerSpecName);

        if (specificFailure != null) out.setSpecificFailure(specificFailure);
        if (type != null) out.setAnyFailureOfType(type);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        FailureHandlerDef p = (FailureHandlerDef) proto;
        if (p.hasSpecificFailure()) specificFailure = p.getSpecificFailure();
        if (p.hasAnyFailureOfType()) type = p.getAnyFailureOfType();
        handlerSpecName = p.getHandlerSpecName();
    }

    public static FailureHandlerDefModel fromProto(FailureHandlerDef p, ExecutionContext context) {
        FailureHandlerDefModel out = new FailureHandlerDefModel();
        out.initFrom(p, context);
        return out;
    }

    public boolean doesHandle(String failureName) {
        if (specificFailure == null) {
            if (type == LHFailureType.FAILURE_TYPE_ERROR) {
                return LHConstants.RESERVED_EXCEPTION_NAMES.contains(failureName);
            } else if (type == LHFailureType.FAILURE_TYPE_EXCEPTION) {
                return !LHConstants.RESERVED_EXCEPTION_NAMES.contains(failureName);
            }
            // Then it's a wildcard, which means match all failures.
            log.debug("Wildcard failure handler...accepting.");
            return true;
        }

        if (specificFailure.equals(failureName)) {
            log.debug("Exact match exception handler");
            return true;
        }

        log.debug("Specific: {} handling: {}", this.specificFailure, failureName);

        if (specificFailure.equals(LHConstants.VAR_ERROR)) {
            return (failureName.equals(LHConstants.VAR_MUTATION_ERROR)
                    || failureName.equals(LHConstants.VAR_SUB_ERROR));
        }

        if (specificFailure.equals(LHConstants.TASK_ERROR)) {
            return (failureName.equals(LHConstants.TASK_FAILURE) || failureName.equals(LHConstants.TIMEOUT));
        }

        return false;
    }
}
