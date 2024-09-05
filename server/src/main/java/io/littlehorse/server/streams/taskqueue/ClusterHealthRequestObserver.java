package io.littlehorse.server.streams.taskqueue;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import java.util.ArrayList;

public class ClusterHealthRequestObserver implements StreamObserver<RegisterTaskWorkerResponse> {

    public final StreamObserver<RegisterTaskWorkerResponse> responseObserver;

    public ClusterHealthRequestObserver(StreamObserver<RegisterTaskWorkerResponse> responseObserver) {
        this.responseObserver = responseObserver;
        ((ServerCallStreamObserver<RegisterTaskWorkerResponse>) responseObserver).setOnCancelHandler(() -> {});
    }

    @Override
    public void onNext(RegisterTaskWorkerResponse response) {
        responseObserver.onNext(response);
    }

    @Override
    public void onError(Throwable throwable) {
        if (isUnavailableStatusException(throwable)) {
            RegisterTaskWorkerResponse out = RegisterTaskWorkerResponse.newBuilder()
                    .setIsClusterHealthy(false)
                    .addAllYourHosts(new ArrayList<>())
                    .build();
            responseObserver.onNext(out);
        } else {
            responseObserver.onError(throwable);
        }
    }

    public boolean isUnavailableStatusException(Throwable throwable) {
        return throwable instanceof StatusException
                && ((StatusException) throwable).getStatus().getCode().equals(Status.UNAVAILABLE.getCode());
    }

    @Override
    public void onCompleted() {
        responseObserver.onCompleted();
    }
}
