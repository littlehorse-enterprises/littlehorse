package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.jlib.common.proto.FailurePb;

public class Failure extends LHSerializable<FailurePb> {

    public String failureName;
    public String message;
    public VariableValue content;

    public Class<FailurePb> getProtoBaseClass() {
        return FailurePb.class;
    }

    public FailurePb.Builder toProto() {
        FailurePb.Builder out = FailurePb
            .newBuilder()
            .setMessage(message)
            .setFailureName(failureName);

        if (content != null) out.setContent(content.toProto());

        return out;
    }

    public void initFrom(Message proto) {
        FailurePb p = (FailurePb) proto;
        failureName = p.getFailureName();
        message = p.getMessage();

        if (p.hasContent()) {
            content = VariableValue.fromProto(p.getContent());
        }
    }

    public static Failure fromProto(FailurePb p) {
        Failure out = new Failure();
        out.initFrom(p);
        return out;
    }

    public Failure() {}

    public Failure(String message, String failureName) {
        this.message = message;
        this.failureName = failureName;
    }

    public Failure(String message, String failureName, VariableValue content) {
        this.message = message;
        this.failureName = failureName;
        this.content = new VariableValue();
    }
}
