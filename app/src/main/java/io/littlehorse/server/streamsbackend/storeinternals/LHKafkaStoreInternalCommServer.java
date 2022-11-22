package io.littlehorse.server.streamsbackend.storeinternals;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.command.CommandResult;
import io.littlehorse.common.proto.CentralStoreQueryPb;
import io.littlehorse.common.proto.CentralStoreQueryReplyPb;
import io.littlehorse.common.proto.LHInternalsGrpc;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsBlockingStub;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsImplBase;
import io.littlehorse.common.proto.StoreQueryStatusPb;
import io.littlehorse.common.proto.WaitForCommandResultPb;
import io.littlehorse.common.proto.WaitForCommandResultReplyPb;
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

    private LHPartitionedReadOnlyStore getLocalStore(
        Integer specificPartition,
        boolean enableStaleStores
    ) {
        ReadOnlyKeyValueStore<String, Bytes> rawStore = getRawStore(
            specificPartition,
            enableStaleStores
        );
        return new LHPartitionedReadOnlyStore(rawStore, config);
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
            LHConstants.CORE_DATA_STORE_NAME,
            partitionKey,
            Serdes.String().serializer()
        );

        if (meta.activeHost().equals(thisHost)) {
            return getRawStore(null, false).get(fullStoreKey);
        } else {
            return queryRemoteBytes(meta, fullStoreKey, partitionKey);
        }
    }

    private Bytes queryRemoteBytes(
        KeyQueryMetadata meta,
        String fullStoreKey,
        String partitionKey
    ) throws LHConnectionError {
        LHInternalsBlockingStub client = getInternalClient(meta.activeHost());
        Exception caught = null;

        try {
            CentralStoreQueryReplyPb activeHostReply = client.centralStoreQuery(
                CentralStoreQueryPb
                    .newBuilder()
                    .setEnableStaleStores(false)
                    .setSpecificPartition(meta.partition())
                    .setFullKey(partitionKey)
                    .build()
            );

            if (activeHostReply.getCode() == StoreQueryStatusPb.RSQ_OK) {
                return new Bytes(activeHostReply.getResult().toByteArray());
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
                        .setFullKey(partitionKey)
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

            Bytes result = rawStore.get(req.getFullKey());

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
            LHPartitionedReadOnlyStore store;
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
            out.setCode(StoreQueryStatusPb.RSQ_OK);
            if (result != null) {
                out.setResult(result.toProto());
            }

            ctx.onNext(out.build());
            ctx.onCompleted();
        }
    }
}
