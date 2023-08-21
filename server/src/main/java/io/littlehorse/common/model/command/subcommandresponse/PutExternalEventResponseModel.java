package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.sdk.common.proto.PutExternalEventResponse;

public class PutExternalEventResponseModel extends AbstractResponse<PutExternalEventResponse> {

    public ExternalEventModel result;

    public Class<PutExternalEventResponse> getProtoBaseClass() {
        return PutExternalEventResponse.class;
    }

    public PutExternalEventResponse.Builder toProto() {
        PutExternalEventResponse.Builder out =
                PutExternalEventResponse.newBuilder().setCode(code);
        if (message != null) out.setMessage(message);
        if (result != null) out.setResult(result.toProto());

        return out;
    }

    public void initFrom(Message proto) {
        PutExternalEventResponse p = (PutExternalEventResponse) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasResult()) result = ExternalEventModel.fromProto(p.getResult());
    }
}
