package io.littlehorse.sdk.common.config.retryinterceptor;

import com.google.protobuf.Any;
import com.google.protobuf.util.Durations;
import com.google.rpc.RetryInfo;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.protobuf.StatusProto;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class ResourceExhaustedRetryInterceptor implements ClientInterceptor {

    private static final ScheduledExecutorService RETRY_EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "lh-sdk-grpc-retry");
                thread.setDaemon(true);
                return thread;
            });

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        // Can't easily throttle on streaming, so the LH GRPC protocol does not throw RESOURCE_EXHAUSTED
        // on streaming rpc's (of which there currently is only `rpc PollTask`).
        if (method.getType() != MethodDescriptor.MethodType.UNARY) {
            return next.newCall(method, callOptions);
        }

        return new RetryingUnaryClientCall<>(method, callOptions, next, RETRY_EXECUTOR);
    }

    public static Long getRetryDelayMillis(Status status, Metadata trailers) {
        if (status.getCode() != Status.Code.RESOURCE_EXHAUSTED || trailers == null) {
            return null;
        }

        com.google.rpc.Status statusDetails;
        try {
            statusDetails = StatusProto.fromStatusAndTrailers(status, trailers);
        } catch (IllegalArgumentException ex) {
            return null;
        }

        for (Any detail : statusDetails.getDetailsList()) {
            if (!detail.is(RetryInfo.class)) {
                continue;
            }

            try {
                RetryInfo retryInfo = detail.unpack(RetryInfo.class);
                if (!retryInfo.hasRetryDelay()) {
                    return null;
                }

                long delayMillis = Durations.toMillis(retryInfo.getRetryDelay());
                return delayMillis > 0 ? delayMillis : null;
            } catch (Exception ex) {
                return null;
            }
        }

        return null;
    }
}
