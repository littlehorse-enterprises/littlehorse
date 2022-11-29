package io.littlehorse.server.streamsbackend.storeinternals;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.CommandResult;
import io.littlehorse.common.proto.CentralStoreQueryPb;
import io.littlehorse.common.proto.CentralStoreQueryPb.CentralStoreSubQueryPb;
import io.littlehorse.common.proto.CentralStoreQueryReplyPb;
import io.littlehorse.common.proto.LHInternalsGrpc;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsBlockingStub;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsImplBase;
import io.littlehorse.common.proto.StoreQueryStatusPb;
import io.littlehorse.common.proto.WaitForCommandResultPb;
import io.littlehorse.common.proto.WaitForCommandResultReplyPb;
import io.littlehorse.server.ServerTopology;
import java.io.Closeable;
import java.io.IOException;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class LHKafkaStoreInternalCommServer implements Closeable {

    private LHConfig config;
    private Server internalGrpcServer;
    private KafkaStreams coreStreams;
    private HostInfo thisHost;

    public LHKafkaStoreInternalCommServer(LHConfig config, KafkaStreams coreStreams) {
        this.config = config;
        this.coreStreams = coreStreams;

        this.internalGrpcServer =
            ServerBuilder
                .forPort(this.config.getInternalBindPort())
                .addService(new InterBrokerCommServer())
                .build();

        thisHost =
            new HostInfo(
                config.getAdvertisedHost(),
                config.getInternalAdvertisedPort()
            );
    }

    public void start() throws IOException {
        internalGrpcServer.start();
    }

    public void close() {
        internalGrpcServer.shutdown();
    }

    public Bytes getBytes(String fullStoreKey, String partitionKey)
        throws LHConnectionError {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
            ServerTopology.CORE_STORE,
            partitionKey,
            Serdes.String().serializer()
        );

        if (meta.activeHost().equals(thisHost)) {
            return getRawStore(null, false).get(fullStoreKey);
        } else {
            return queryRemote(
                meta,
                CentralStoreSubQueryPb.newBuilder().setKey(fullStoreKey).build()
            );
        }
    }

    public Bytes getLastFromPrefix(String prefix, String partitionKey)
        throws LHConnectionError {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
            ServerTopology.CORE_STORE,
            partitionKey,
            Serdes.String().serializer()
        );

        if (meta.activeHost().equals(thisHost)) {
            return new LHROStoreWrapper(getRawStore(null, false), config)
                .getLastBytesFromFullPrefix(prefix);
        } else {
            return queryRemote(
                meta,
                CentralStoreSubQueryPb.newBuilder().setLastFromPrefix(prefix).build()
            );
        }
    }

    public Bytes waitForProcessing(Command command) throws LHConnectionError {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
            ServerTopology.CORE_STORE,
            command.getPartitionKey(),
            Serdes.String().serializer()
        );

        LHInternalsBlockingStub client = getInternalClient(meta.activeHost());

        WaitForCommandResultReplyPb resp;
        try {
            resp =
                client.waitForCommandResult(
                    WaitForCommandResultPb
                        .newBuilder()
                        .setCommandId(command.commandId)
                        .setSpecificPartition(meta.partition())
                        .build()
                );
        } catch (Exception exn) {
            throw new LHConnectionError(
                exn,
                "Could not connect to required LH broker."
            );
        }

        switch (resp.getCode()) {
            case RSQ_OK:
                if (resp.hasResult()) {
                    // lol why did I design the CommandResultPb this way
                    return new Bytes(resp.getResult().getResult().toByteArray());
                } else {
                    return null;
                }
            case RSQ_NOT_AVAILABLE:
                throw new LHConnectionError(
                    null,
                    "Network error: " + resp.getMessage()
                );
            case UNRECOGNIZED:
            default:
                throw new RuntimeException("Not possible.");
        }
    }

    private ReadOnlyKeyValueStore<String, Bytes> getRawStore(
        Integer specificPartition,
        boolean enableStaleStores
    ) {
        StoreQueryParameters<ReadOnlyKeyValueStore<String, Bytes>> params = StoreQueryParameters.fromNameAndType(
            ServerTopology.CORE_STORE,
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

    // // Unclear if this is necessary yet.
    // private LHROStoreWrapper getGlobalStore() {
    //     StoreQueryParameters<ReadOnlyKeyValueStore<String, Bytes>> params = StoreQueryParameters.fromNameAndType(
    //         ServerTopology.globalStore,
    //         QueryableStoreTypes.keyValueStore()
    //     );
    //     ReadOnlyKeyValueStore<String, Bytes> rawGStore = coreStreams.store(params);
    //     return new LHROStoreWrapper(rawGStore, config);
    // }

    private LHROStoreWrapper getLocalStore(
        Integer specificPartition,
        boolean enableStaleStores
    ) {
        ReadOnlyKeyValueStore<String, Bytes> rawStore = getRawStore(
            specificPartition,
            enableStaleStores
        );
        return new LHROStoreWrapper(rawStore, config);
    }

    private Bytes queryRemote(KeyQueryMetadata meta, CentralStoreSubQueryPb subQuery)
        throws LHConnectionError {
        LHInternalsBlockingStub client = getInternalClient(meta.activeHost());
        Exception caught = null;

        try {
            CentralStoreQueryReplyPb resp = client.centralStoreQuery(
                CentralStoreQueryPb
                    .newBuilder()
                    .setEnableStaleStores(false)
                    .setSpecificPartition(meta.partition())
                    .setQuery(subQuery)
                    .build()
            );

            if (resp.getCode() == StoreQueryStatusPb.RSQ_OK) {
                return new Bytes(resp.getResult().toByteArray());
            } else if (resp.getCode() == StoreQueryStatusPb.RSQ_NOT_AVAILABLE) {
                caught = new LHConnectionError(null, "Could not access store.");
            }
        } catch (Exception exn) {
            // It's probably a runtime exception. TODO: investigate grpc error
            // throwing, cuz it's not cool dawg
            caught = exn;
        }

        CentralStoreQueryReplyPb resp = null;
        for (HostInfo standbyHost : meta.standbyHosts()) {
            client = getInternalClient(standbyHost);
            try {
                CentralStoreQueryReplyPb standbyCandidate = client.centralStoreQuery(
                    CentralStoreQueryPb
                        .newBuilder()
                        .setEnableStaleStores(true)
                        .setSpecificPartition(meta.partition())
                        .setQuery(subQuery)
                        .build()
                );

                if (standbyCandidate.getCode() == StoreQueryStatusPb.RSQ_OK) {
                    if (
                        resp == null ||
                        standbyCandidate.getApproximateLag() <
                        resp.getApproximateLag()
                    ) {
                        resp = standbyCandidate;
                    }
                }
            } catch (Exception exn) {
                // If we fail to contact a standby host, just ignore it and
                // proceed to the next standby. If all standby's failed, we still
                // have saved the caught Exception from calling the active host.
                // We will return that original error wrapped in an
                // LHConnectionError.
            }
        }

        if (resp != null) {
            return new Bytes(resp.getResult().toByteArray());
        } else {
            throw new LHConnectionError(
                caught,
                "Failed to look up desired data from active or standby replicas."
            );
        }
    }

    // TODO: We need to keep and re-use channels so that we don't open a bazillion
    // connections.
    private LHInternalsBlockingStub getInternalClient(HostInfo host) {
        return LHInternalsGrpc.newBlockingStub(
            ManagedChannelBuilder
                .forAddress(host.host(), host.port())
                .usePlaintext()
                .build()
        );
    }

    /*
     * Implements the internal_server.proto service, which is used
     * for communication between the LH servers to do distributed lookups etc.
     */
    private class InterBrokerCommServer extends LHInternalsImplBase {

        /*
         * Need to investigate:
         * - Behavior when not owner of partition
         * - What exception is thrown when the store is unavailable (eg. REBALANCING)?
         *
         * TODO for first hire: Figure out how to set the "approximateLag" field on
         * the response.
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
                System.out.println(
                    "\n\nTODO: Figure out and handle the runtime error"
                );
                return;
            }
            Bytes result = null;

            switch (req.getQuery().getQueryCase()) {
                case KEY:
                    result = rawStore.get(req.getQuery().getKey());
                    break;
                case LAST_FROM_PREFIX:
                    result =
                        new LHROStoreWrapper(rawStore, config)
                            .getLastBytesFromFullPrefix(
                                req.getQuery().getLastFromPrefix()
                            );
                    break;
                case QUERY_NOT_SET:
                default:
                    throw new RuntimeException("Not possible");
            }

            out.setCode(StoreQueryStatusPb.RSQ_OK);
            if (result != null) {
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
            LHROStoreWrapper store;
            try {
                store = getLocalStore(req.getSpecificPartition(), false);
            } catch (Exception exn) {
                exn.printStackTrace();
                System.out.println(
                    "\n\nTODO: Figure out and handle the runtime error"
                );
                return;
            }

            int iterations = 0;
            CommandResult result = null;
            while (iterations++ < 500) { // lol
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
            out.setCode(StoreQueryStatusPb.RSQ_OK);
            if (result != null) {
                out.setResult(result.toProto());
            }

            ctx.onNext(out.build());
            ctx.onCompleted();
        }
    }
}
