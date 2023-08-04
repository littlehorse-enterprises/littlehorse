package io.littlehorse.server.streamsimpl.util;

import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.proto.CentralStoreQueryReplyPb;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;

/**
 * @deprecated
 * Should not use this class because it's not using the StoredGetable class. This class will
 * be removed once all entities are migrated to use the StoredGetable class.
 */
@Deprecated(forRemoval = true)
public class GETStreamObserver<
    U extends Message, T extends Storeable<U>, V extends Message
>
    implements StreamObserver<CentralStoreQueryReplyPb> {

    private StreamObserver<V> ctx;
    private LHConfig config;
    private Class<T> getableCls;

    private IntermediateGETResp<U, T, V> out;

    public GETStreamObserver(
        StreamObserver<V> responseObserver,
        Class<T> getableCls,
        Class<V> responseCls,
        LHConfig config
    ) {
        this.ctx = responseObserver;
        this.getableCls = getableCls;
        this.config = config;

        this.out = new IntermediateGETResp<U, T, V>(responseCls);
    }

    public void onError(Throwable t) {
        Throwable cause = t.getCause() != null ? t.getCause() : t;

        out.code = LHResponseCodePb.CONNECTION_ERROR;
        out.message = "Failed connecting to backend: " + cause.getMessage();
        ctx.onNext(out.toProto());
        ctx.onCompleted();
    }

    public void onCompleted() {}

    public void onNext(CentralStoreQueryReplyPb reply) {
        // TODO
        if (reply.hasResult()) {
            out.code = LHResponseCodePb.OK;
            try {
                out.result =
                    LHSerializable.fromBytes(
                        reply.getResult().toByteArray(),
                        getableCls,
                        config
                    );
            } catch (LHSerdeError exn) {
                out.code = LHResponseCodePb.CONNECTION_ERROR;
                out.message =
                    "Impossible: got unreadable response from backend: " +
                    exn.getMessage();
            }
        } else {
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
        }

        ctx.onNext(out.toProto());
        ctx.onCompleted();
    }
}
