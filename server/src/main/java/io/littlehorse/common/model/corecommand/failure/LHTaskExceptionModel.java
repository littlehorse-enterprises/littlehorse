package io.littlehorse.common.model.corecommand.failure;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.LHTaskException;
import lombok.Getter;

@Getter
public class LHTaskExceptionModel extends LHSerializable<LHTaskException> {

    private String name;
    private String message;

    @Override
    public LHTaskException.Builder toProto() {
        return LHTaskException.newBuilder().setMessage(message).setName(name);
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        LHTaskException taskException = (LHTaskException) proto;
        name = taskException.getName();
        message = taskException.getMessage();
    }

    @Override
    public Class<LHTaskException> getProtoBaseClass() {
        return LHTaskException.class;
    }
}
