package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.sdk.common.proto.PutWfSpecResponse;

public class PutWfSpecResponseModel extends AbstractResponse<PutWfSpecResponse> {

    public WfSpecModel result;

    public Class<PutWfSpecResponse> getProtoBaseClass() {
        return PutWfSpecResponse.class;
    }

    public void initFrom(Message proto) {
        PutWfSpecResponse p = (PutWfSpecResponse) proto;

        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasResult()) result = WfSpecModel.fromProto(p.getResult());
    }

    public PutWfSpecResponse.Builder toProto() {
        PutWfSpecResponse.Builder out = PutWfSpecResponse.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);
        if (result != null) out.setResult(result.toProto());
        return out;
    }
}
