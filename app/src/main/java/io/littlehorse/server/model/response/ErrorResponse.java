package io.littlehorse.server.model.response;

import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.ErrorCodePb;
import io.littlehorse.common.proto.ErrorResponsePb;

public class ErrorResponse extends LHSerializable<ErrorResponsePb> {
    public ErrorCodePb code;
    public String message;

    public Class<ErrorResponsePb> getProtoBaseClass() {
        return ErrorResponsePb.class;
    }

    public ErrorResponse() {}

    public ErrorResponse(ErrorCodePb code, String message) {
        this.code = code;
        this.message = message;
    }

    public void initFrom(ErrorResponsePb proto) {
        this.code = proto.getCode();
        this.message = proto.getMessage();
    }

    public ErrorResponsePb.Builder toProto() {
        ErrorResponsePb.Builder out = ErrorResponsePb.newBuilder()
            .setCode(code)
            .setMessage(message);

        return out;
    }
}
