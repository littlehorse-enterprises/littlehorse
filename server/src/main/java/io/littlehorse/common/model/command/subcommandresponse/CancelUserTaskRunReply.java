package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunResponse;
import io.littlehorse.sdk.common.proto.LHResponseCode;

public class CancelUserTaskRunReply extends AbstractResponse<CancelUserTaskRunResponse> {

    private String message;
    private LHResponseCode lhResponseCodePb;

    public CancelUserTaskRunReply(String message, LHResponseCode lhResponseCodePb) {
        this.message = message;
        this.lhResponseCodePb = lhResponseCodePb;
    }

    @Override
    public CancelUserTaskRunResponse.Builder toProto() {
        return CancelUserTaskRunResponse.newBuilder().setCode(lhResponseCodePb).setMessage(message);
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        CancelUserTaskRunResponse cancelUserTaskRunResponse = (CancelUserTaskRunResponse) proto;
        message = cancelUserTaskRunResponse.getMessage();
        lhResponseCodePb = cancelUserTaskRunResponse.getCode();
    }

    @Override
    public Class<CancelUserTaskRunResponse> getProtoBaseClass() {
        return CancelUserTaskRunResponse.class;
    }
}
