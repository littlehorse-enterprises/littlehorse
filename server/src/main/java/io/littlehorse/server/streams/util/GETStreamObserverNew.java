package io.littlehorse.server.streams.util;

import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.proto.GetObjectResponse;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.store.StoredGetable;

@Deprecated(forRemoval = true)
public class GETStreamObserverNew<U extends Message, T extends AbstractGetable<U>, V extends Message>
        implements StreamObserver<GetObjectResponse> {

    private StreamObserver<V> ctx;

    private IntermediateGETResp<U, T, V> out;

    public GETStreamObserverNew(StreamObserver<V> responseObserver, Class<T> getableCls, Class<V> responseCls) {
        this.ctx = responseObserver;
        this.out = new IntermediateGETResp<U, T, V>(responseCls);
    }

    public void onError(Throwable t) {
        ctx.onError(t);
    }

    public void onCompleted() {}

    public void onNext(CentralStoreQueryResponse reply) {
        if (reply.hasResult()) {
            out.code = LHResponseCode.OK;
            try {
                @SuppressWarnings("unchecked")
                StoredGetable<U, T> entity = (StoredGetable<U, T>)
                        LHSerializable.fromBytes(reply.getResult().toByteArray(), StoredGetable.class);
                out.result = entity.getStoredObject();
            } catch (LHSerdeError exn) {
                out.code = LHResponseCode.CONNECTION_ERROR;
                out.message = "Impossible: got unreadable response from backend: " + exn.getMessage();
            }
        } else {
            out.code = LHResponseCode.NOT_FOUND_ERROR;
        }

        ctx.onNext(out.toProto());
        ctx.onCompleted();
    }
}
