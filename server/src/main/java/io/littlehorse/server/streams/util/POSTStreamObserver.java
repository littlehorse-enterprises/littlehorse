package io.littlehorse.server.streams.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.proto.StoreQueryStatusPb;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class POSTStreamObserver<U extends Message> implements StreamObserver<WaitForCommandResponse> {

    private StreamObserver<U> ctx;
    private Class<U> responseCls;
    private boolean shouldComplete;

    public POSTStreamObserver(StreamObserver<U> responseObserver, Class<U> responseCls, boolean shouldComplete) {
        this.ctx = responseObserver;
        this.responseCls = responseCls;
        this.shouldComplete = shouldComplete;
    }

    public void onError(Throwable t) {
        log.error(
                "Got onError() from postObserver. Returning RECORDED_NOT_PROCESSED: {} {}",
                responseCls,
                t.getMessage(),
                t);

        U response = buildErrorResponse(
                LHResponseCode.REPORTED_BUT_NOT_PROCESSED,
                "Recorded request but processing not verified: " + t.getMessage());

        ctx.onNext(response);
        if (shouldComplete) ctx.onCompleted();
    }

    private U buildErrorResponse(LHResponseCode code, String msg) {
        try {
            GeneratedMessageV3.Builder<?> b = (GeneratedMessageV3.Builder<?>)
                    responseCls.getMethod("newBuilder").invoke(null);
            b.getClass().getMethod("setCode", LHResponseCode.class).invoke(b, code);

            b.getClass().getMethod("setMessage", String.class).invoke(b, msg);

            @SuppressWarnings("unchecked")
            U response = (U) b.getClass().getMethod("build").invoke(b);

            return response;
        } catch (Exception exn) {
            log.error(exn.getMessage(), exn);
            throw new RuntimeException("Yikerz, not possible");
        }
    }

    @SuppressWarnings("unchecked")
    private U buildRespFromBytes(ByteString bytes) {
        try {
            return (U) responseCls.getMethod("parseFrom", ByteString.class).invoke(null, bytes);
        } catch (Exception exn) {
            log.error(exn.getMessage(), exn);
            throw new RuntimeException("Not possible");
        }
    }

    public void onCompleted() {
        // Nothing to do
    }

    public void onNext(WaitForCommandResponse reply) {
        U response;

        if (reply.getCode() == StoreQueryStatusPb.RSQ_OK) {
            if (!reply.hasResult()) {
                throw new RuntimeException("This should be impossible");
            }
            response = buildRespFromBytes(reply.getResult().getResult());
        } else if (reply.getCode() == StoreQueryStatusPb.RSQ_NOT_AVAILABLE) {
            response = buildErrorResponse(
                    LHResponseCode.CONNECTION_ERROR,
                    "Failed connecting to backend: " + (reply.hasMessage() ? reply.getMessage() : ""));
        } else {
            throw new RuntimeException("Unexpected RSQ code");
        }

        ctx.onNext(response);
        if (shouldComplete) ctx.onCompleted();
    }
}
