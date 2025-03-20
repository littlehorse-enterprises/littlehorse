package io.littlehorse.server.streams.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.server.streams.BackendInternalComms;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class POSTStreamObserver<U extends Message> implements StreamObserver<WaitForCommandResponse> {

    private StreamObserver<U> ctx;
    private Class<U> responseCls;
    private boolean shouldComplete;
    private final BackendInternalComms internalComms;
    private final AbstractCommand<?> command;
    private final RequestExecutionContext requestContext;
    private final Date commandStartedAt;
    private final Date timeoutAt;
    // Wait until kafka streams rebalance finishes
    private final Duration successResponseTimeout;
    private final ExecutorService retryExecutor;

    public POSTStreamObserver(
            StreamObserver<U> responseObserver,
            Class<U> responseCls,
            boolean shouldComplete,
            BackendInternalComms internalComms,
            AbstractCommand<?> command,
            RequestExecutionContext requestContext,
            Duration successResponseTimeout,
            ExecutorService retryExecutor) {
        this.ctx = responseObserver;
        this.responseCls = responseCls;
        this.shouldComplete = shouldComplete;
        this.internalComms = internalComms;
        this.command = command;
        this.commandStartedAt = new Date();
        this.timeoutAt = new Date(commandStartedAt.getTime() + successResponseTimeout.toMillis());
        this.successResponseTimeout = successResponseTimeout;
        this.requestContext = requestContext;
        this.retryExecutor = retryExecutor;
    }

    @Override
    public void onError(Throwable t) {
        final boolean isRetryable = t instanceof StatusRuntimeException grpcRuntimeException
                && grpcRuntimeException.getStatus().getCode().equals(Status.UNAVAILABLE.getCode());
        final boolean retryOneMoreTime = timeoutAt.compareTo(new Date()) > 0;
        if (isRetryable && retryOneMoreTime) {
            retryExecutor.submit(() -> {
                internalComms.waitForCommand(
                        command,
                        new POSTStreamObserver<>(
                                ctx,
                                responseCls,
                                shouldComplete,
                                internalComms,
                                command,
                                requestContext,
                                successResponseTimeout,
                                retryExecutor),
                        requestContext);
            });
        } else {
            try {
                ctx.onError(t);
            } catch (IllegalStateException e) {
                log.debug("Call already closed");
            }
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

    @Override
    public void onCompleted() {
        if (shouldComplete) {
            try {
                ctx.onCompleted();
            } catch (IllegalStateException e) {
                log.debug("Call already closed");
            }
        }
    }

    @Override
    public void onNext(WaitForCommandResponse reply) {
        if (reply.hasResult()) {
            try {
                ctx.onNext(buildRespFromBytes(reply.getResult()));
            } catch (IllegalStateException e) {
                log.debug("Call already closed");
            }
        } else if (reply.hasPartitionMigratedResponse()) {
            internalComms.waitForCommand(command, this, requestContext);
        }
    }
}
