package io.littlehorse.common.model.corecommand.failure;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.LHTaskException;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class LHTaskExceptionModel extends LHSerializable<LHTaskException> {

    private String name;
    private String message;
    private VariableValueModel content;

    @Override
    public LHTaskException.Builder toProto() {
        return LHTaskException.newBuilder().setMessage(message).setName(name).setContent(content.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        LHTaskException taskException = (LHTaskException) proto;
        name = taskException.getName();
        message = taskException.getMessage();
        content = VariableValueModel.fromProto(taskException.getContent(), context);
    }

    @Override
    public Class<LHTaskException> getProtoBaseClass() {
        return LHTaskException.class;
    }
}
