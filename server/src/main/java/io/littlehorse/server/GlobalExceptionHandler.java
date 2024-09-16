package io.littlehorse.server;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.exceptions.LHApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.errors.InvalidStateStoreException;

/**
 * This class defines a centralized error handler for public RPC methods, translating exceptions into appropriate gRPC {@code Status} codes.
 * This class is designed to be closer to the network layer which means, that it should be executed before any other interceptor in the call.
 */
@Slf4j
public class GlobalExceptionHandler implements ServerInterceptor {

    public static final String INTERNAL_ERROR_MESSAGE =
            "An unexpected internal error occurred. Please contact support for assistance.";

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        return new InternalCallListener<>(call, next, headers);
    }

    private static class InternalCallListener<ReqT, RespT>
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
            } catch (LHApiException apiException) {
                call.close(apiException.getStatus(), metadata);
            } catch (InvalidStateStoreException ex) {
                call.close(Status.UNAVAILABLE.withDescription(ex.getMessage()).withCause(ex), metadata);
            } catch (StatusRuntimeException ex) {
                log.error("Internal exception: {}", ex.getMessage(), ex);
                call.close(Status.fromThrowable(ex), metadata);
            } catch (Throwable ex) {
                log.error("Unexpected exception: {}", ex.getMessage(), ex);
                call.close(Status.INTERNAL.withDescription(INTERNAL_ERROR_MESSAGE), metadata);
            }
        }
    }
}
