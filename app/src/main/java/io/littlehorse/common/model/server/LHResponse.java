package io.littlehorse.common.model.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.Any;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.LHResponsePb;
import io.littlehorse.common.proto.LHResponsePbOrBuilder;

public class LHResponse extends LHSerializable<LHResponsePb> {

    public LHResponseCodePb code;
    public String message;
    public String id;
    public LHSerializable<?> result;

    @JsonIgnore
    private LHConfig config;

    public LHResponse(LHConfig config) {
        this.config = config;
    }

    @JsonIgnore
    public Class<LHResponsePb> getProtoBaseClass() {
        return LHResponsePb.class;
    }

    @JsonIgnore
    public int getStatus() {
        switch (code) {
            case OK:
                return 200;
            case NOT_FOUND_ERROR:
                return 404;
            case VALIDATION_ERROR:
            case BAD_REQUEST_ERROR:
                return 400;
            case CONNECTION_ERROR:
            case UNRECOGNIZED:
            default:
                return 500;
        }
    }

    public LHResponsePb.Builder toProto() {
        LHResponsePb.Builder out = LHResponsePb.newBuilder();

        out.setCode(code);
        if (message != null) out.setMessage(message);
        if (id != null) out.setId(id);
        if (result != null) {
            // This is jank i know
            out.setResult(
                Any
                    .newBuilder()
                    .setValue(result.toProto().build().toByteString())
                    .setTypeUrl("unknownoops")
            );
            out.setResultClass(result.getClass().getCanonicalName());
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public void initFrom(MessageOrBuilder p) {
        LHResponsePbOrBuilder proto = (LHResponsePbOrBuilder) p;
        code = proto.getCode();
        if (proto.hasMessage()) message = proto.getMessage();
        if (proto.hasId()) id = proto.getId();
        if (proto.hasResult()) {
            try {
                result =
                    LHSerializable.fromBytes(
                        proto.getResult().getValue().toByteArray(),
                        (Class<LHSerializable<?>>) Class.forName(
                            proto.getResultClass()
                        ),
                        config
                    );
            } catch (LHSerdeError | ClassNotFoundException exn) {
                // Should be impossible
                throw new RuntimeException(exn);
            }
        }
    }
}
