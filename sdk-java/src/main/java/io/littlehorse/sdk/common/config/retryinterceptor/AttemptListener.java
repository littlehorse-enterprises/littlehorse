package io.littlehorse.sdk.common.config.retryinterceptor;

import static io.littlehorse.sdk.common.config.retryinterceptor.ResourceExhaustedRetryInterceptor.getRetryDelayMillis;

import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;

final class AttemptListener<ReqT, RespT> extends ClientCall.Listener<RespT> {

    private final RetryingUnaryClientCall<ReqT, RespT> owner;
    private final ClientCall<ReqT, RespT> attemptDelegate;
    private boolean receivedMessage;

    AttemptListener(RetryingUnaryClientCall<ReqT, RespT> owner, ClientCall<ReqT, RespT> attemptDelegate) {
        this.owner = owner;
        this.attemptDelegate = attemptDelegate;
    }

    @Override
    public void onHeaders(Metadata headers) {
        ClientCall.Listener<RespT> currentListener;
        synchronized (owner.getLock()) {
            if (owner.getDelegate() != attemptDelegate || owner.isClosed()) {
                return;
            }
            currentListener = owner.getListener();
        }
        currentListener.onHeaders(headers);
    }

    @Override
    public void onMessage(RespT message) {
        ClientCall.Listener<RespT> currentListener;
        synchronized (owner.getLock()) {
            if (owner.getDelegate() != attemptDelegate || owner.isClosed()) {
                return;
            }
            receivedMessage = true;
            currentListener = owner.getListener();
        }
        currentListener.onMessage(message);
    }

    @Override
    public void onClose(Status status, Metadata trailers) {
        Long retryDelayMillis = receivedMessage ? null : getRetryDelayMillis(status, trailers);
        if (retryDelayMillis != null && owner.canRetryWithinDeadline(retryDelayMillis)) {
            synchronized (owner.getLock()) {
                if (owner.getDelegate() != attemptDelegate || owner.isClosed() || owner.isCancelled()) {
                    return;
                }
                owner.setDelegate(null);
                owner.scheduleRetry(retryDelayMillis);
            }
            return;
        }

        ClientCall.Listener<RespT> currentListener;
        synchronized (owner.getLock()) {
            if (owner.getDelegate() != attemptDelegate || owner.isClosed()) {
                return;
            }
            owner.setClosed(true);
            currentListener = owner.getListener();
        }
        currentListener.onClose(status, trailers);
    }

    @Override
    public void onReady() {
        ClientCall.Listener<RespT> currentListener;
        synchronized (owner.getLock()) {
            if (owner.getDelegate() != attemptDelegate || owner.isClosed()) {
                return;
            }
            currentListener = owner.getListener();
        }
        currentListener.onReady();
    }
}
