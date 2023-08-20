package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.FailureHandlerDef;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FailureHandlerDefModel extends LHSerializable<FailureHandlerDef> {

    public String specificFailure;
    public String handlerSpecName;

    public NodeModel node;

    public FailureHandlerDefModel() {
    }

    public Class<FailureHandlerDef> getProtoBaseClass() {
        return FailureHandlerDef.class;
    }

    public FailureHandlerDef.Builder toProto() {
        FailureHandlerDef.Builder out = FailureHandlerDef.newBuilder().setHandlerSpecName(handlerSpecName);

        if (specificFailure != null)
            out.setSpecificFailure(specificFailure);

        return out;
    }

    public void initFrom(Message proto) {
        FailureHandlerDef p = (FailureHandlerDef) proto;
        if (p.hasSpecificFailure())
            specificFailure = p.getSpecificFailure();
        handlerSpecName = p.getHandlerSpecName();
    }

    public static FailureHandlerDefModel fromProto(FailureHandlerDef p) {
        FailureHandlerDefModel out = new FailureHandlerDefModel();
        out.initFrom(p);
        return out;
    }

    public boolean doesHandle(String failureName) {
        if (specificFailure == null) {
            // Then it's a wildcard, which means match all exceptions.
            log.debug("Wildcard exception handler...accepting.");
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
