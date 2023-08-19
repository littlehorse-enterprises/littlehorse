package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.sdk.common.proto.DeleteObjectResponse;
import io.littlehorse.sdk.common.proto.LHResponseCode;

public class DeleteObjectReply extends AbstractResponse<DeleteObjectResponse> {

    public DeleteObjectReply() {}

    public DeleteObjectReply(LHResponseCode code, String message) {
        this.code = code;
        this.message = message;
    }

    public Class<DeleteObjectResponse> getProtoBaseClass() {
        return DeleteObjectResponse.class;
    }

    public DeleteObjectResponse.Builder toProto() {
        DeleteObjectResponse.Builder out = DeleteObjectResponse.newBuilder();
        out.setCode(code);
        if (message != null) {
            out.setMessage(message);
        }
        return out;
    }

    public void initFrom(Message proto) {
        DeleteObjectResponse p = (DeleteObjectResponse) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
    }

    public static DeleteObjectReply fromProto(DeleteObjectResponse p) {
        DeleteObjectReply out = new DeleteObjectReply();
        out.initFrom(p);
        return out;
    }
}
