package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.jlib.common.proto.FailureDefPb;
import io.littlehorse.jlib.common.proto.VariableTypePb;
import java.util.HashSet;
import java.util.Set;

public class FailureDef extends LHSerializable<FailureDefPb> {

    public String failureName;
    public String message;
    public VariableAssignment content;

    public Class<FailureDefPb> getProtoBaseClass() {
        return FailureDefPb.class;
    }

    public FailureDefPb.Builder toProto() {
        FailureDefPb.Builder out = FailureDefPb
            .newBuilder()
            .setFailureName(failureName)
            .setMessage(message);

        if (content != null) {
            out.setContent(content.toProto());
        }
        return out;
    }

    public void initFrom(Message proto) {
        FailureDefPb p = (FailureDefPb) proto;
        failureName = p.getFailureName();
        message = p.getMessage();

        if (p.hasContent()) {
            content = VariableAssignment.fromProto(p.getContent());
        }
    }

    public static FailureDef fromProto(FailureDefPb proto) {
        FailureDef out = new FailureDef();
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
            throw new LHValidationError(
                null,
                "Failure name " + failureName + " is reserved!"
            );
        }
    }

    public Failure getFailure(ThreadRun thread) {
        Failure out = new Failure();
        out.failureName = failureName;
        out.message = message;

        if (this.content != null) {
            try {
                out.content = thread.assignVariable(this.content);
            } catch (LHVarSubError exn) {
                out.content = new VariableValue();
                out.content.type = VariableTypePb.NULL;
                out.message +=
                    "\n\nWARNING: Unable to assign output content: " +
                    exn.getMessage();
            }
        } else {
            out.content = new VariableValue();
            out.content.type = VariableTypePb.NULL;
        }
        return out;
    }
}
