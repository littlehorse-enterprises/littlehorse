package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.meta.usertasks.UserTaskDefModel;
import io.littlehorse.sdk.common.proto.PutUserTaskDefResponse;

public class PutUserTaskDefResponseModel extends AbstractResponse<PutUserTaskDefResponse> {

    public UserTaskDefModel result;

    public Class<PutUserTaskDefResponse> getProtoBaseClass() {
        return PutUserTaskDefResponse.class;
    }

    public PutUserTaskDefResponse.Builder toProto() {
        PutUserTaskDefResponse.Builder out = PutUserTaskDefResponse.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);
        if (result != null) out.setResult(result.toProto());
        return out;
    }

    public void initFrom(Message proto) {
        PutUserTaskDefResponse p = (PutUserTaskDefResponse) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasResult()) result = LHSerializable.fromProto(p.getResult(), UserTaskDefModel.class);
    }
}
