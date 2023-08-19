package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.meta.TaskDefModel;
import io.littlehorse.sdk.common.proto.PutTaskDefResponse;

public class PutTaskDefResponseModel extends AbstractResponse<PutTaskDefResponse> {

    public TaskDefModel result;

    public Class<PutTaskDefResponse> getProtoBaseClass() {
        return PutTaskDefResponse.class;
    }

    public PutTaskDefResponse.Builder toProto() {
        PutTaskDefResponse.Builder out = PutTaskDefResponse.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);
        if (result != null) out.setResult(result.toProto());
        return out;
    }

    public void initFrom(Message proto) {
        PutTaskDefResponse p = (PutTaskDefResponse) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasResult()) result = TaskDefModel.fromProto(p.getResult());
    }
}
