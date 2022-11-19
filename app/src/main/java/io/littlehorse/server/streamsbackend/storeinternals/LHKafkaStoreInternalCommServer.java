package io.littlehorse.server.streamsbackend.storeinternals;

import com.google.protobuf.ByteString;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.command.CommandResult;
import io.littlehorse.common.proto.CentralStoreQueryPb;
import io.littlehorse.common.proto.CentralStoreQueryReplyPb;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsImplBase;
import io.littlehorse.common.proto.StoreQueryStatusPb;
import io.littlehorse.common.proto.WaitForCommandResultPb;
import io.littlehorse.common.proto.WaitForCommandResultReplyPb;
import io.littlehorse.server.streamsbackend.storeinternals.utils.LHLocalReadOnlyStore;
import java.io.Closeable;
import java.io.IOException;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class LHKafkaStoreInternalCommServer
    extends LHInternalsImplBase
    implements Closeable {

    private LHConfig config;
    private Server internalGrpcServer;
    private KafkaStreams coreStreams;

    public LHKafkaStoreInternalCommServer(LHConfig config, KafkaStreams coreStreams) {
        this.config = config;
        this.coreStreams = coreStreams;

        this.internalGrpcServer =
            ServerBuilder
                .forPort(this.config.getInternalBindPort())
                .addService(this)
                .build();
    }

    /*
     * Need to investigate:
     * - Behavior when not owner of partition
     * - What exception is thrown when the store is unavailable (eg. REBALANCING)?
     */
    @Override
    public void centralStoreQuery(
        CentralStoreQueryPb req,
        StreamObserver<CentralStoreQueryReplyPb> ctx
    ) {
        Integer specificPartition = null;
        if (req.hasSpecificPartition()) {
            specificPartition = req.getSpecificPartition();
        }

        CentralStoreQueryReplyPb.Builder out = CentralStoreQueryReplyPb.newBuilder();

        ReadOnlyKeyValueStore<String, Bytes> rawStore;
        try {
            rawStore = getRawStore(specificPartition, req.getEnableStaleStores());
        } catch (Exception exn) {
            exn.printStackTrace();
            System.out.println("\n\nTODO: Figure out and handle the runtime error");
            return;
        }

        Bytes result = rawStore.get(req.getKey());

        if (result == null) {
            out.setCode(StoreQueryStatusPb.RSQ_NOT_FOUND);
        } else {
            out.setCode(StoreQueryStatusPb.RSQ_OK);
            out.setResult(ByteString.copyFrom(result.get()));
        }

        ctx.onNext(out.build());
        ctx.onCompleted();
    }

    @Override
    public void waitForCommandResult(
        WaitForCommandResultPb req,
        StreamObserver<WaitForCommandResultReplyPb> ctx
    ) {
        LHLocalReadOnlyStore store;
        try {
            store = getLocalStore(req.getSpecificPartition(), false);
        } catch (Exception exn) {
            exn.printStackTrace();
            System.out.println("\n\nTODO: Figure out and handle the runtime error");
            return;
        }

        int iterations = 0;
        CommandResult result = null;
        while (iterations++ < 500) {
            result = store.get(req.getCommandId(), CommandResult.class);
            if (result == null) {
                try {
                    Thread.sleep(30);
                } catch (Exception ignored) {}
            } else {
                break;
            }
        }

        WaitForCommandResultReplyPb.Builder out = WaitForCommandResultReplyPb.newBuilder();
        if (result == null) {
            out.setCode(StoreQueryStatusPb.RSQ_NOT_FOUND);
        } else {
            out.setCode(StoreQueryStatusPb.RSQ_OK);
            out.setResult(result.toProto());
        }

        ctx.onNext(out.build());
        ctx.onCompleted();
    }

    private ReadOnlyKeyValueStore<String, Bytes> getRawStore(
        Integer specificPartition,
        boolean enableStaleStores
    ) {
        StoreQueryParameters<ReadOnlyKeyValueStore<String, Bytes>> params = StoreQueryParameters.fromNameAndType(
            LHConstants.CORE_DATA_STORE_NAME,
            QueryableStoreTypes.keyValueStore()
        );

        if (enableStaleStores) {
            params = params.enableStaleStores();
        }

        if (specificPartition != null) {
            params = params.withPartition(specificPartition);
        }

        return coreStreams.store(params);
    }

    private LHLocalReadOnlyStore getLocalStore(
        Integer specificPartition,
        boolean enableStaleStores
    ) {
        ReadOnlyKeyValueStore<String, Bytes> rawStore = getRawStore(
            specificPartition,
            enableStaleStores
        );
        return new LHLocalReadOnlyStore(rawStore, config);
    }

    public void start() throws IOException {
        internalGrpcServer.start();
    }

    public void close() {
        internalGrpcServer.shutdown();
    }
}
