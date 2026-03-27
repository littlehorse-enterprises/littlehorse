package io.littlehorse.sdk.common.config;

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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

final class ResourceExhaustedRetryInterceptor implements ClientInterceptor {

    private static final ScheduledExecutorService RETRY_EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "lh-sdk-grpc-retry");
                thread.setDaemon(true);
                return thread;
            });

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        if (method.getType() != MethodDescriptor.MethodType.UNARY) {
            return next.newCall(method, callOptions);
        }

        return new RetryingUnaryClientCall<>(method, callOptions, next);
    }

    static Long getRetryDelayMillis(Status status, Metadata trailers) {
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

    private static final class RetryingUnaryClientCall<ReqT, RespT> extends ClientCall<ReqT, RespT> {

        private final Object lock = new Object();
        private final MethodDescriptor<ReqT, RespT> method;
        private final CallOptions callOptions;
        private final Channel next;

        private ClientCall<ReqT, RespT> delegate;
        private Listener<RespT> listener;
        private Metadata headers;
        private ReqT request;
        private int requestedMessages;
        private boolean halfClosed;
        private boolean cancelled;
        private boolean closed;
        private String cancelMessage;
        private Throwable cancelCause;
        private ScheduledFuture<?> scheduledRetry;

        private RetryingUnaryClientCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
            this.method = method;
            this.callOptions = callOptions;
            this.next = next;
        }

        @Override
        public void start(Listener<RespT> listener, Metadata headers) {
            synchronized (lock) {
                this.listener = listener;
                this.headers = copy(headers);
            }
            startAttempt();
        }

        @Override
        public void request(int numMessages) {
            ClientCall<ReqT, RespT> currentDelegate;
            synchronized (lock) {
                requestedMessages += numMessages;
                currentDelegate = delegate;
            }

            if (currentDelegate != null) {
                currentDelegate.request(numMessages);
            }
        }

        @Override
        public void cancel(String message, Throwable cause) {
            ClientCall<ReqT, RespT> currentDelegate;
            Listener<RespT> currentListener = null;
            synchronized (lock) {
                if (cancelled || closed) {
                    return;
                }

                cancelled = true;
                cancelMessage = message;
                cancelCause = cause;

                currentDelegate = delegate;
                if (scheduledRetry != null && scheduledRetry.cancel(false) && currentDelegate == null) {
                    closed = true;
                    currentListener = listener;
                }
            }

            if (currentDelegate != null) {
                currentDelegate.cancel(message, cause);
            } else if (currentListener != null) {
                currentListener.onClose(
                        Status.CANCELLED.withDescription(message).withCause(cause), new Metadata());
            }
        }

        @Override
        public void halfClose() {
            ClientCall<ReqT, RespT> currentDelegate;
            synchronized (lock) {
                halfClosed = true;
                currentDelegate = delegate;
            }

            if (currentDelegate != null) {
                currentDelegate.halfClose();
            }
        }

        @Override
        public void sendMessage(ReqT message) {
            ClientCall<ReqT, RespT> currentDelegate;
            synchronized (lock) {
                request = message;
                currentDelegate = delegate;
            }

            if (currentDelegate != null) {
                currentDelegate.sendMessage(message);
            }
        }

        @Override
        public boolean isReady() {
            synchronized (lock) {
                return delegate != null && delegate.isReady();
            }
        }

        private void startAttempt() {
            ClientCall<ReqT, RespT> newDelegate = next.newCall(method, callOptions);
            Metadata currentHeaders;
            ReqT currentRequest;
            int currentRequestedMessages;
            boolean currentHalfClosed;

            synchronized (lock) {
                if (cancelled || closed) {
                    return;
                }

                delegate = newDelegate;
                currentHeaders = copy(headers);
                currentRequest = request;
                currentRequestedMessages = requestedMessages;
                currentHalfClosed = halfClosed;
            }

            newDelegate.start(new AttemptListener(newDelegate), currentHeaders);
            if (currentRequestedMessages > 0) {
                newDelegate.request(currentRequestedMessages);
            }
            if (currentRequest != null) {
                newDelegate.sendMessage(currentRequest);
            }
            if (currentHalfClosed) {
                newDelegate.halfClose();
            }
        }

        private final class AttemptListener extends ClientCall.Listener<RespT> {

            private final ClientCall<ReqT, RespT> attemptDelegate;
            private boolean receivedMessage;

            private AttemptListener(ClientCall<ReqT, RespT> attemptDelegate) {
                this.attemptDelegate = attemptDelegate;
            }

            @Override
            public void onHeaders(Metadata headers) {
                Listener<RespT> currentListener;
                synchronized (lock) {
                    if (delegate != attemptDelegate || closed) {
                        return;
                    }
                    currentListener = listener;
                }
                currentListener.onHeaders(headers);
            }

            @Override
            public void onMessage(RespT message) {
                Listener<RespT> currentListener;
                synchronized (lock) {
                    if (delegate != attemptDelegate || closed) {
                        return;
                    }
                    receivedMessage = true;
                    currentListener = listener;
                }
                currentListener.onMessage(message);
            }

            @Override
            public void onClose(Status status, Metadata trailers) {
                Long retryDelayMillis = receivedMessage ? null : getRetryDelayMillis(status, trailers);
                if (retryDelayMillis != null) {
                    synchronized (lock) {
                        if (delegate != attemptDelegate || closed || cancelled) {
                            return;
                        }
                        delegate = null;
                        scheduledRetry = RETRY_EXECUTOR.schedule(
                                RetryingUnaryClientCall.this::startAttempt, retryDelayMillis, TimeUnit.MILLISECONDS);
                    }
                    return;
                }

                Listener<RespT> currentListener;
                synchronized (lock) {
                    if (delegate != attemptDelegate || closed) {
                        return;
                    }
                    closed = true;
                    currentListener = listener;
                }
                currentListener.onClose(status, trailers);
            }

            @Override
            public void onReady() {
                Listener<RespT> currentListener;
                synchronized (lock) {
                    if (delegate != attemptDelegate || closed) {
                        return;
                    }
                    currentListener = listener;
                }
                currentListener.onReady();
            }
        }

        private static Metadata copy(Metadata source) {
            Metadata copy = new Metadata();
            if (source != null) {
                copy.merge(source);
            }
            return copy;
        }
    }
}
