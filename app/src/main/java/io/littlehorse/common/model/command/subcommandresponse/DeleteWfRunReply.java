package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.proto.DeleteWfRunReplyPb;
import io.littlehorse.common.proto.DeleteWfRunReplyPbOrBuilder;

public class DeleteWfRunReply extends AbstractResponse<DeleteWfRunReplyPb> {

    public Class<DeleteWfRunReplyPb> getProtoBaseClass() {
        return DeleteWfRunReplyPb.class;
    }

    public DeleteWfRunReplyPb.Builder toProto() {
        DeleteWfRunReplyPb.Builder out = DeleteWfRunReplyPb.newBuilder();
        out.setCode(code);
        if (message != null) {
            out.setMessage(message);
        }
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        DeleteWfRunReplyPbOrBuilder p = (DeleteWfRunReplyPbOrBuilder) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
    }

    public static DeleteWfRunReply fromProto(DeleteWfRunReplyPbOrBuilder p) {
        DeleteWfRunReply out = new DeleteWfRunReply();
        out.initFrom(p);
        return out;
    }
}
