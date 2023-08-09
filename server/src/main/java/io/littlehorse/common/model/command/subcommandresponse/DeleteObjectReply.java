package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.sdk.common.proto.DeleteObjectReplyPb;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;

public class DeleteObjectReply extends AbstractResponse<DeleteObjectReplyPb> {

    public DeleteObjectReply() {}

    public DeleteObjectReply(LHResponseCodePb code, String message) {
        this.code = code;
        this.message = message;
    }

    public Class<DeleteObjectReplyPb> getProtoBaseClass() {
        return DeleteObjectReplyPb.class;
    }

    public DeleteObjectReplyPb.Builder toProto() {
        DeleteObjectReplyPb.Builder out = DeleteObjectReplyPb.newBuilder();
        out.setCode(code);
        if (message != null) {
            out.setMessage(message);
        }
        return out;
    }

    public void initFrom(Message proto) {
        DeleteObjectReplyPb p = (DeleteObjectReplyPb) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
    }

    public static DeleteObjectReply fromProto(DeleteObjectReplyPb p) {
        DeleteObjectReply out = new DeleteObjectReply();
        out.initFrom(p);
        return out;
    }
}
