package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.LHResponseCode;

public class CancelUserTaskRunReply
    extends AbstractResponse<CancelUserTaskRunReplyPb> {

    private String message;
    private LHResponseCode lhResponseCodePb;

    public CancelUserTaskRunReply(String message, LHResponseCode lhResponseCodePb) {
        this.message = message;
        this.lhResponseCodePb = lhResponseCodePb;
    }

    @Override
    public CancelUserTaskRunReplyPb.Builder toProto() {
        return CancelUserTaskRunReplyPb
            .newBuilder()
            .setCode(lhResponseCodePb)
            .setMessage(message);
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        CancelUserTaskRunReplyPb cancelUserTaskRunReplyPb = (CancelUserTaskRunReplyPb) proto;
        message = cancelUserTaskRunReplyPb.getMessage();
        lhResponseCodePb = cancelUserTaskRunReplyPb.getCode();
    }

    @Override
    public Class<CancelUserTaskRunReplyPb> getProtoBaseClass() {
        return CancelUserTaskRunReplyPb.class;
    }
}
