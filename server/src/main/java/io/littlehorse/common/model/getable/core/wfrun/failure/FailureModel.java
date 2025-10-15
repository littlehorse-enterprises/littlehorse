package io.littlehorse.common.model.getable.core.wfrun.failure;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FailureModel extends LHSerializable<Failure> {

    public String failureName;

    @Getter
    public String message;

    public VariableValueModel content;
    private boolean properlyHandled;
    private Integer failureHandlerThreadRunId;

    public FailureModel() {}

    public FailureModel(String message, String failureName) {
        this.message = message;
        this.failureName = failureName;
    }

    public FailureModel(String message, String failureName, VariableValueModel content) {
        this.message = message;
        this.failureName = failureName;
        this.content = content;
    }

    public static FailureModel fromProto(Failure p, ExecutionContext context) {
        FailureModel out = new FailureModel();
        out.initFrom(p, context);
        return out;
    }

    public Class<Failure> getProtoBaseClass() {
        return Failure.class;
    }

    public Failure.Builder toProto() {
        Failure.Builder out = Failure.newBuilder()
                .setMessage(message)
                .setFailureName(failureName)
                .setWasProperlyHandled(properlyHandled);

        if (content != null) out.setContent(content.toProto());
        if (failureHandlerThreadRunId != null) out.setFailureHandlerThreadrunId(failureHandlerThreadRunId);

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        Failure p = (Failure) proto;
        failureName = p.getFailureName();
        message = p.getMessage();
        properlyHandled = p.getWasProperlyHandled();

        if (p.hasContent()) {
            content = VariableValueModel.fromProto(p.getContent(), context);
        }

        if (p.hasFailureHandlerThreadrunId()) {
            failureHandlerThreadRunId = p.getFailureHandlerThreadrunId();
        }
    }

    public boolean isUserDefinedFailure() {
        return !LHConstants.RESERVED_EXCEPTION_NAMES.contains(failureName);
    }

    public LHStatus getStatus() {
        return isUserDefinedFailure() ? LHStatus.EXCEPTION : LHStatus.ERROR;
    }

    public FailureModel copyWithPrefix(String prefix) {
        return new FailureModel(prefix + message, failureName, content);
    }
}
