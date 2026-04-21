package io.littlehorse.sdk.common.config.retryinterceptor;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

final class RetryingUnaryClientCall<ReqT, RespT> extends ClientCall<ReqT, RespT> {

    private final Object lock = new Object();
    private final MethodDescriptor<ReqT, RespT> method;
    private final CallOptions callOptions;
    private final Channel next;
    private final ScheduledExecutorService retryExecutor;

    private ClientCall<ReqT, RespT> delegate;
    private Listener<RespT> listener;
    private Metadata headers;
    private ReqT request;
    private int requestedMessages;
    private boolean halfClosed;
    private boolean cancelled;
    private boolean closed;
    private ScheduledFuture<?> scheduledRetry;

    RetryingUnaryClientCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next,
            ScheduledExecutorService retryExecutor) {
        this.method = method;
        this.callOptions = callOptions;
        this.next = next;
        this.retryExecutor = retryExecutor;
    }

    @Override
    public void start(Listener<RespT> listener, Metadata headers) {
        synchronized (lock) {
            this.listener = listener;
            this.headers = copyMetadata(headers);
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
    public void cancel(@Nullable String message, @Nullable Throwable cause) {
        ClientCall<ReqT, RespT> currentDelegate;
        Listener<RespT> currentListener = null;
        synchronized (lock) {
            if (cancelled || closed) {
                return;
            }

            cancelled = true;

            currentDelegate = delegate;
            if (scheduledRetry != null && scheduledRetry.cancel(false) && currentDelegate == null) {
                closed = true;
                currentListener = listener;
            }
        }

        if (currentDelegate != null) {
            currentDelegate.cancel(message, cause);
        } else if (currentListener != null) {
            currentListener.onClose(Status.CANCELLED.withDescription(message).withCause(cause), new Metadata());
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

    void startAttempt() {
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
            currentHeaders = copyMetadata(headers);
            currentRequest = request;
            currentRequestedMessages = requestedMessages;
            currentHalfClosed = halfClosed;
        }

        newDelegate.start(new AttemptListener<>(this, newDelegate), currentHeaders);
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

    void scheduleRetry(long delayMillis) {
        scheduledRetry = retryExecutor.schedule(this::startAttempt, delayMillis, TimeUnit.MILLISECONDS);
    }

    Object getLock() {
        return lock;
    }

    ClientCall<ReqT, RespT> getDelegate() {
        return delegate;
    }

    void setDelegate(ClientCall<ReqT, RespT> delegate) {
        this.delegate = delegate;
    }

    boolean isClosed() {
        return closed;
    }

    void setClosed(boolean closed) {
        this.closed = closed;
    }

    boolean isCancelled() {
        return cancelled;
    }

    Listener<RespT> getListener() {
        return listener;
    }

    static Metadata copyMetadata(Metadata source) {
        Metadata copy = new Metadata();
        if (source != null) {
            copy.merge(source);
        }
        return copy;
    }
}
