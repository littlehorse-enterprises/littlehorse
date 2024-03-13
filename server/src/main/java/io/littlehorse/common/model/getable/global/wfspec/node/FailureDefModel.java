package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.FailureDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashSet;
import java.util.Set;

public class FailureDefModel extends LHSerializable<FailureDef> {

    public String failureName;
    public String message;
    public VariableAssignmentModel content;

    public Class<FailureDef> getProtoBaseClass() {
        return FailureDef.class;
    }

    public FailureDef.Builder toProto() {
        FailureDef.Builder out =
                FailureDef.newBuilder().setFailureName(failureName).setMessage(message);

        if (content != null) {
            out.setContent(content.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        FailureDef p = (FailureDef) proto;
        failureName = p.getFailureName();
        message = p.getMessage();

        if (p.hasContent()) {
            content = VariableAssignmentModel.fromProto(p.getContent(), context);
        }
    }

    public static FailureDefModel fromProto(FailureDef proto, ExecutionContext context) {
        FailureDefModel out = new FailureDefModel();
        out.initFrom(proto, context);
        return out;
    }

    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();
        if (content != null) {
            out.addAll(content.getRequiredWfRunVarNames());
        }
        return out;
    }

    public void validate() throws LHApiException {
        if (LHConstants.RESERVED_EXCEPTION_NAMES.contains(failureName)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Failure name " + failureName + " is reserved!");
        } else if (!LHUtil.isValidLHName(failureName)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Invalid name for exception: " + failureName);
        }
    }

    public FailureModel getFailure(ThreadRunModel thread) {
        FailureModel out = new FailureModel();
        out.failureName = failureName;
        out.message = message;

        if (this.content != null) {
            try {
                out.content = thread.assignVariable(this.content);
            } catch (LHVarSubError exn) {
                out.content = new VariableValueModel();
                out.message += "\n\nWARNING: Unable to assign output content: " + exn.getMessage();
            }
        } else {
            out.content = new VariableValueModel();
        }
        return out;
    }
}
