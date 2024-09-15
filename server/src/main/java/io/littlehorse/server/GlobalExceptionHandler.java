package io.littlehorse.server;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.littlehorse.common.exceptions.LHApiException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalExceptionHandler implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        return new InternalCallListener<>(call, next, headers);
    }

    private class InternalCallListener<ReqT, RespT>
            extends ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT> {
        private final ServerCall<ReqT, RespT> call;
        private final Metadata metadata;

        public InternalCallListener(
                ServerCall<ReqT, RespT> call, ServerCallHandler<ReqT, RespT> next, Metadata metadata) {
            super(next.startCall(call, metadata));
            this.call = call;
            this.metadata = metadata;
        }

        @Override
        public void onHalfClose() {
            try {
                // Proceed with the call
                super.onHalfClose();
            } catch (Exception e) {
                handleException(call, e, metadata);
            }
        }
    }

    public void handleException(ServerCall<?, ?> call, Throwable throwable, Metadata metadata) {
        log.trace("Unrecognized exception: ", throwable);
        Status status = Status.INTERNAL
                .withDescription("Server Error: " + throwable.getMessage())
                .withCause(throwable);
        call.close(status, metadata);
        if (throwable instanceof LHApiException myEx) {
            call.close(myEx.getStatus(), metadata);
        }
    }
}
