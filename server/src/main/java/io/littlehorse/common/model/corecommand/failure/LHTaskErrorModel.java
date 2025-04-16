package io.littlehorse.common.model.corecommand.failure;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHTaskError;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class LHTaskErrorModel extends LHSerializable<LHTaskError> {

    private String message;
    private LHErrorType type;

    @Override
    public LHTaskError.Builder toProto() {
        return LHTaskError.newBuilder().setMessage(message).setType(type);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        LHTaskError taskError = (LHTaskError) proto;
        message = taskError.getMessage();
        type = taskError.getType();
    }

    @Override
    public Class<LHTaskError> getProtoBaseClass() {
        return LHTaskError.class;
    }
}
