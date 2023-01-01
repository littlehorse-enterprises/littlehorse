package io.littlehorse.server.streamsimpl.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.MessageOrBuilder;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.StoreQueryStatusPb;
import io.littlehorse.common.proto.WaitForCommandReplyPb;
import io.littlehorse.common.util.LHUtil;

public class POSTStreamObserver<U extends MessageOrBuilder>
    implements StreamObserver<WaitForCommandReplyPb> {

    private StreamObserver<U> ctx;
    private Class<U> responseCls;
    private boolean shouldComplete;

    public POSTStreamObserver(
        StreamObserver<U> responseObserver,
        Class<U> responseCls,
        boolean shouldComplete
    ) {
        this.ctx = responseObserver;
        this.responseCls = responseCls;
        this.shouldComplete = shouldComplete;
    }

    public void onError(Throwable t) {
        LHUtil.log(
            "Got onError() from postObserver. Returning RECORDED_NOT_PROCESSED",
            t.getMessage(),
            responseCls
        );

        U response = buildErrorResponse(
            LHResponseCodePb.REPORTED_BUT_NOT_PROCESSED,
            "Recorded request but processing not verified: " + t.getMessage()
        );

        ctx.onNext(response);
        if (shouldComplete) ctx.onCompleted();
    }

    private U buildErrorResponse(LHResponseCodePb code, String msg) {
        try {
            GeneratedMessageV3.Builder<?> b = (GeneratedMessageV3.Builder<?>) responseCls
                .getMethod("newBuilder")
                .invoke(null);
            b.getClass().getMethod("setCode", LHResponseCodePb.class).invoke(b, code);

            b.getClass().getMethod("setMessage", String.class).invoke(b, msg);

            @SuppressWarnings("unchecked")
            U response = (U) b.getClass().getMethod("build").invoke(b);

            return response;
        } catch (Exception exn) {
            exn.printStackTrace();
            throw new RuntimeException("Yikerz, not possible");
        }
    }

    @SuppressWarnings("unchecked")
    private U buildRespFromBytes(ByteString bytes) {
        try {
            return (U) responseCls
                .getMethod("parseFrom", ByteString.class)
                .invoke(null, bytes);
        } catch (Exception exn) {
            exn.printStackTrace();
            throw new RuntimeException("Not possible");
        }
    }

    public void onCompleted() {
        // Nothing to do
    }

    public void onNext(WaitForCommandReplyPb reply) {
        U response;

        if (reply.getCode() == StoreQueryStatusPb.RSQ_OK) {
            if (!reply.hasResult()) {
                throw new RuntimeException("This should be impossible");
            }
            response = buildRespFromBytes(reply.getResult().getResult());
        } else if (reply.getCode() == StoreQueryStatusPb.RSQ_NOT_AVAILABLE) {
            response =
                buildErrorResponse(
                    LHResponseCodePb.CONNECTION_ERROR,
                    "Failed connecting to backend: " +
                    (reply.hasMessage() ? reply.getMessage() : "")
                );
        } else {
            throw new RuntimeException("Unexpected RSQ code");
        }

        ctx.onNext(response);
        if (shouldComplete) ctx.onCompleted();
    }
}
