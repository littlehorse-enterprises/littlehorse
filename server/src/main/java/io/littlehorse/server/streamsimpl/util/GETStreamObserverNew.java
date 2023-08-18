package io.littlehorse.server.streamsimpl.util;

import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.CentralStoreQueryResponse;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoredGetable;

public class GETStreamObserverNew<U extends Message, T extends Getable<U>, V extends Message>
        implements StreamObserver<CentralStoreQueryResponse> {

    private StreamObserver<V> ctx;
    private LHConfig config;
    private Class<T> getableCls;

    private IntermediateGETResp<U, T, V> out;

    public GETStreamObserverNew(
            StreamObserver<V> responseObserver,
            Class<T> getableCls,
            Class<V> responseCls,
            LHConfig config) {
        this.ctx = responseObserver;
        this.getableCls = getableCls;
        this.config = config;

        this.out = new IntermediateGETResp<U, T, V>(responseCls);
    }

    public void onError(Throwable t) {
        Throwable cause = t.getCause() != null ? t.getCause() : t;

        out.code = LHResponseCode.CONNECTION_ERROR;
        out.message = "Failed connecting to backend: " + cause.getMessage();
        ctx.onNext(out.toProto());
        ctx.onCompleted();
    }

    public void onCompleted() {}

    public void onNext(CentralStoreQueryResponse reply) {
        if (reply.hasResult()) {
            out.code = LHResponseCode.OK;
            try {
                StoredGetable<U, T> entity =
                        (StoredGetable<U, T>)
                                LHSerializable.fromBytes(
                                        reply.getResult().toByteArray(),
                                        StoredGetable.class,
                                        config);
                out.result = entity.getStoredObject();
            } catch (LHSerdeError exn) {
                out.code = LHResponseCode.CONNECTION_ERROR;
                out.message =
                        "Impossible: got unreadable response from backend: " + exn.getMessage();
            }
        } else {
            out.code = LHResponseCode.NOT_FOUND_ERROR;
        }

        ctx.onNext(out.toProto());
        ctx.onCompleted();
    }
}
