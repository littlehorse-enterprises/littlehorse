package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.meta.ExternalEventDefModel;
import io.littlehorse.sdk.common.proto.PutExternalEventDefResponse;

public class PutExternalEventDefResponseModel extends AbstractResponse<PutExternalEventDefResponse> {

    public ExternalEventDefModel result;

    public Class<PutExternalEventDefResponse> getProtoBaseClass() {
        return PutExternalEventDefResponse.class;
    }

    public PutExternalEventDefResponse.Builder toProto() {
        PutExternalEventDefResponse.Builder out = PutExternalEventDefResponse.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);
        if (result != null) out.setResult(result.toProto());
        return out;
    }

    public void initFrom(Message proto) {
        PutExternalEventDefResponse p = (PutExternalEventDefResponse) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasResult()) result = ExternalEventDefModel.fromProto(p.getResult());
    }
}
