package io.littlehorse.server.streams.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.server.streams.BackendInternalComms;
import java.time.Duration;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class POSTStreamObserver<U extends Message> implements StreamObserver<WaitForCommandResponse> {

    private StreamObserver<U> ctx;
    private Class<U> responseCls;
    private boolean shouldComplete;
    private final BackendInternalComms internalComms;
    private final AbstractCommand<?> command;
    private final Date commandStartedAt;
    private final Date timeoutAt;
    // Wait until kafka streams rebalance finishes
    private final Duration successResponseTimeout;

    public POSTStreamObserver(
            StreamObserver<U> responseObserver,
            Class<U> responseCls,
            boolean shouldComplete,
            BackendInternalComms internalComms,
            AbstractCommand<?> command,
            Duration successResponseTimeout) {
        this.ctx = responseObserver;
        this.responseCls = responseCls;
        this.shouldComplete = shouldComplete;
        this.internalComms = internalComms;
        this.command = command;
        this.commandStartedAt = new Date();
        this.timeoutAt = new Date(commandStartedAt.getTime() + successResponseTimeout.toMillis());
        this.successResponseTimeout = successResponseTimeout;
    }

    @Override
    public void onError(Throwable t) {
        final boolean isRetryable = t instanceof StatusRuntimeException grpcRuntimeException
                && grpcRuntimeException.getStatus().getCode().equals(Status.UNAVAILABLE.getCode());
        final boolean retryOneMoreTime = timeoutAt.compareTo(new Date()) > 0;
        if (isRetryable && retryOneMoreTime) {
            internalComms.waitForCommand(
                    command,
                    new POSTStreamObserver<>(
                            ctx, responseCls, shouldComplete, internalComms, command, successResponseTimeout));
        } else {
            ctx.onError(t);
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
            ctx.onCompleted();
        }
    }

    @Override
    public void onNext(WaitForCommandResponse reply) {
        if (reply.hasResult()) {
            ctx.onNext(buildRespFromBytes(reply.getResult()));
        } else if (reply.hasPartitionMigratedResponse()) {
            internalComms.waitForCommand(command, this);
        }
    }
}
