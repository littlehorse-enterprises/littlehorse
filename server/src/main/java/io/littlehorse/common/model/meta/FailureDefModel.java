package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.FailureModel;
import io.littlehorse.common.model.wfrun.ThreadRunModel;
import io.littlehorse.common.model.wfrun.VariableValueModel;
import io.littlehorse.sdk.common.proto.FailureDef;
import io.littlehorse.sdk.common.proto.VariableType;
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

    public void initFrom(Message proto) {
        FailureDef p = (FailureDef) proto;
        failureName = p.getFailureName();
        message = p.getMessage();

        if (p.hasContent()) {
            content = VariableAssignmentModel.fromProto(p.getContent());
        }
    }

    public static FailureDefModel fromProto(FailureDef proto) {
        FailureDefModel out = new FailureDefModel();
        out.initFrom(proto);
        return out;
    }

    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();
        if (content != null) {
            out.addAll(content.getRequiredWfRunVarNames());
        }
        return out;
    }

    public void validate() throws LHValidationError {
        if (LHConstants.RESERVED_EXCEPTION_NAMES.contains(failureName)) {
            throw new LHValidationError(null, "Failure name " + failureName + " is reserved!");
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
                out.content.type = VariableType.NULL;
                out.message += "\n\nWARNING: Unable to assign output content: " + exn.getMessage();
            }
        } else {
            out.content = new VariableValueModel();
            out.content.type = VariableType.NULL;
        }
        return out;
    }
}
