package io.littlehorse.server.streams.taskqueue;

import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import java.util.ArrayList;

public class ClusterHealthRequestObserver implements StreamObserver<RegisterTaskWorkerResponse> {

    public final StreamObserver<RegisterTaskWorkerResponse> responseObserver;

    public ClusterHealthRequestObserver(StreamObserver<RegisterTaskWorkerResponse> responseObserver) {
        this.responseObserver = responseObserver;
    }

    @Override
    public void onNext(RegisterTaskWorkerResponse response) {
        responseObserver.onNext(response);
    }

    @Override
    public void onError(Throwable t) {
        RegisterTaskWorkerResponse out = RegisterTaskWorkerResponse.newBuilder()
                .setIsClusterHealthy(false)
                .addAllYourHosts(new ArrayList<>())
                .build();
        responseObserver.onNext(out);
    }

    @Override
    public void onCompleted() {
        responseObserver.onCompleted();
    }
}
