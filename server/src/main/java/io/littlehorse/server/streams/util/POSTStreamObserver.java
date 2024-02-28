package io.littlehorse.server.streams.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.proto.WaitForCommandResponse;
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

    @Override
    public void onError(Throwable t) {
        ctx.onError(t);
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

    @Override
    public void onCompleted() {
        if (shouldComplete) {
            ctx.onCompleted();
        }
    }

    @Override
    public void onNext(WaitForCommandResponse reply) {
        ctx.onNext(buildRespFromBytes(reply.getResult()));
    }
}
