package io.littlehorse.common.model.getable.core.wfrun.failure;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.Failure;

public class FailureModel extends LHSerializable<Failure> {

    public String failureName;
    public String message;
    public VariableValueModel content;

    public FailureModel() {}

    public FailureModel(String message, String failureName) {
        this.message = message;
        this.failureName = failureName;
    }

    public FailureModel(String message, String failureName, VariableValueModel content) {
        this.message = message;
        this.failureName = failureName;
        this.content = new VariableValueModel();
    }

    public static FailureModel fromProto(Failure p) {
        FailureModel out = new FailureModel();
        out.initFrom(p);
        return out;
    }

    public Class<Failure> getProtoBaseClass() {
        return Failure.class;
    }

    public Failure.Builder toProto() {
        Failure.Builder out = Failure.newBuilder().setMessage(message).setFailureName(failureName);

        if (content != null) out.setContent(content.toProto());

        return out;
    }

    public void initFrom(Message proto) {
        Failure p = (Failure) proto;
        failureName = p.getFailureName();
        message = p.getMessage();

        if (p.hasContent()) {
            content = VariableValueModel.fromProto(p.getContent());
        }
    }

    public boolean isUserDefinedFailure() {
        return !LHConstants.RESERVED_EXCEPTION_NAMES.contains(failureName);
    }
}
