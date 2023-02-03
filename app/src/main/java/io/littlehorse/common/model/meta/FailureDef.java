package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.FailureDefPb;
import io.littlehorse.common.proto.FailureDefPbOrBuilder;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.VariableTypePb;
import java.util.HashSet;
import java.util.Set;

public class FailureDef extends LHSerializable<FailureDefPb> {

    public String failureName;
    public TaskResultCodePb failureCode;
    public String message;
    public VariableAssignment content;

    public Class<FailureDefPb> getProtoBaseClass() {
        return FailureDefPb.class;
    }

    public FailureDefPb.Builder toProto() {
        FailureDefPb.Builder out = FailureDefPb
            .newBuilder()
            .setFailureCode(failureCode)
            .setFailureName(failureName)
            .setMessage(message);

        if (content != null) {
            out.setContent(content.toProto());
        }
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        FailureDefPbOrBuilder p = (FailureDefPbOrBuilder) proto;
        failureName = p.getFailureName();
        message = p.getMessage();
        failureCode = p.getFailureCode();

        if (p.hasContent()) {
            content = VariableAssignment.fromProto(p.getContentOrBuilder());
        }
    }

    public static FailureDef fromProto(FailureDefPbOrBuilder proto) {
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

    @JsonIgnore
    public Failure getFailure(ThreadRun thread) {
        Failure out = new Failure();
        out.failureName = failureName;
        out.failureCode = failureCode;
        out.message = message;

        try {
            out.content = thread.assignVariable(this.content);
        } catch (LHVarSubError exn) {
            out.content = new VariableValue();
            out.content.type = VariableTypePb.NULL;
            out.message +=
                "\n\nWARNING: Unable to assign output content: " + exn.getMessage();
        }
        return out;
    }
}
