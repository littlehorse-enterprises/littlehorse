package io.littlehorse.server;

import java.util.Map;
import java.util.Set;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsMetadata;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.proto.server.LHResponseCodePb;
import io.littlehorse.common.proto.server.RemoteStoreQueryStatusPb;
import io.littlehorse.common.proto.server.RequestTypePb;
import io.littlehorse.common.util.LHApiClient;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.model.internal.IndexEntries;
import io.littlehorse.server.model.internal.IndexEntry;
import io.littlehorse.server.model.internal.LHResponse;
import io.littlehorse.server.model.internal.POSTableRequest;
import io.littlehorse.server.model.internal.RangeResponse;
import io.littlehorse.server.model.internal.RemoteStoreQueryResponse;

public class ApiStreamsContext {
    private KafkaStreams streams;
    private HostInfo thisHost;
    private LHApiClient client;
    private LHProducer producer;
    private LHConfig config;

    public ApiStreamsContext(LHConfig config, KafkaStreams streams) {
        this.streams = streams;
        this.thisHost = config.getHostInfo();
        this.client = config.getApiClient();
        this.producer = config.getProducer();
        this.config = config;
    }

    public <U extends MessageOrBuilder, T extends GETable<U>>
        T get(String storeKey, String partitionKey, Class<T> cls)
        throws LHConnectionError
    {
        String storeName = GETable.getBaseStoreName(cls);
        KeyQueryMetadata metadata = streams.queryMetadataForKey(
            storeName, partitionKey, Serdes.String().serializer()
        );

        if (metadata.activeHost().equals(thisHost)) {
            return localGet(storeKey, cls);
        } else {
            byte[] serialized = queryRemoteBytes(storeName, metadata, storeKey);
            if (serialized == null) return null;
            try {
                return LHSerializable.fromBytes(serialized, cls, config);
            } catch(LHSerdeError exn) {
                throw new LHConnectionError(exn, "Got invalid protobuf over the wire: ");
            }
        }
    }

    public <U extends MessageOrBuilder, T extends GETable<U>>
    T localGet(String storeKey, Class<T> cls) {
        return getGETableStore(cls).get(storeKey);
    }

    public byte[] localGetBytes(String storeName, String storeKey) {
        GETable<?> obj = getStore(storeName).get(storeKey);
        return obj == null ? null : obj.toBytes(config);
    }

    public <U extends MessageOrBuilder, T extends POSTable<U>> byte[] post(
        T toSave, Class<T> cls
    ) throws LHConnectionError {
        // Step 1: send the POST to the kafka topic.
        String topic = POSTable.getRequestTopicName(cls);
        String partitionKey = toSave.getPartitionKey();

        POSTableRequest request = new POSTableRequest();
        request.type = RequestTypePb.POST;
        request.storeKey = toSave.getObjectId();
        String requestId = LHUtil.generateGuid();
        request.requestId = requestId;
        request.payload = toSave.toBytes(config);

        this.producer.send(
            partitionKey,
            request,
            topic,
            Map.of(LHConstants.OBJECT_ID_HEADER, toSave.getObjectId().getBytes())
        );

        boolean checkLocalResponseStoreOnly = false;
        return waitForResponse(
            partitionKey, cls, requestId, checkLocalResponseStoreOnly
        );
    }

    public byte[] waitForResponse(
        String partitionKey, Class<? extends POSTable<?>> cls,
        String requestId, boolean forceLocal
    ) throws LHConnectionError {
        String storeName = POSTable.getBaseStoreName(cls);
        KeyQueryMetadata metadata = streams.queryMetadataForKey(
            storeName, partitionKey, Serdes.String().serializer()
        );

        if (metadata.activeHost().equals(thisHost)) {
            return localWait(requestId, cls);
        } else {
            String path = "/internal/waitForResponse/" + requestId + "/" +
                cls.getCanonicalName();
            return client.getResponse(metadata.activeHost(), path);
        }
    }

    public RemoteStoreQueryResponse handleRemoteStoreQuery(
        String storeName, int partition, String storeKey, boolean activeHost
    ) {
        RemoteStoreQueryResponse resp = new RemoteStoreQueryResponse();
        try {
            StoreQueryParameters<ReadOnlyKeyValueStore<String, GETable<?>>> storeParams =
                StoreQueryParameters.fromNameAndType(
                    storeName,
                    QueryableStoreTypes.<String, GETable<?>>keyValueStore()
                ).withPartition(partition);

            if (!activeHost) {
                storeParams = storeParams.enableStaleStores();
            }
            ReadOnlyKeyValueStore<String, GETable<?>> store = streams.store(storeParams);
            GETable<?> obj = store.get(storeKey);

            resp.approximateLag = getApproximateLag(storeName, partition);
            if (obj != null) {
                resp.code = RemoteStoreQueryStatusPb.RSQ_OK;
                resp.result = obj.toBytes(config);
            } else {
                resp.code = RemoteStoreQueryStatusPb.RSQ_NOT_FOUND;
            }
        } catch(InvalidStateStoreException exn) {
            exn.printStackTrace();
            resp.code = RemoteStoreQueryStatusPb.RSQ_NOT_AVAILABLE;
        }
        return resp;
    }

    public long getApproximateLag(String storeName, int partition) {
        // TODO
        return 0;
    }

    public byte[] localWait(String requestId, Class<? extends POSTable<?>> cls) {
        ReadOnlyKeyValueStore<String, LHResponse> respStore = getResponseStore(cls);

        int iterations = 0;
        while (iterations++ < 500) {
            LHResponse out = respStore.get(requestId);
            if (out == null) {
                try {
                    Thread.sleep(30);
                } catch(Exception exn) {}
                continue;
            }
            return out.toBytes(config);
        }

        // Timed out waiting for response.
        LHResponse timeOut = new LHResponse(config);
        timeOut.code = LHResponseCodePb.CONNECTION_ERROR;
        timeOut.message = "Timed out waiting for request of ID: " + requestId;
        return timeOut.toBytes(config);
    }

    public RangeResponse keyedPrefixIdxScan(String prefixKey, String token, int limit)
    throws LHConnectionError {
        String storeName = LHConstants.INDEX_STORE_NAME;
        KeyQueryMetadata metadata = streams.queryMetadataForKey(
            storeName, prefixKey, Serdes.String().serializer()
        );
        if (metadata.activeHost().equals(thisHost)) {
            return internalLocalKeyedIdxPrefixScan(prefixKey, token, limit);
        } else {
            return remoteKeyedPrefixIdxScan(
                prefixKey, token, limit, metadata.activeHost(), metadata.standbyHosts()
            );
        }
    }

    public RangeResponse internalLocalKeyedIdxPrefixScan(
        String prefixKey, String token, int limit
    ) {
        RangeResponse out = new RangeResponse();

        try (KeyValueIterator<String, IndexEntries> iter = getIdxStore().prefixScan(
            prefixKey, Serdes.String().serializer()
        )) {
            while (iter.hasNext()) {
                KeyValue<String, IndexEntries> kvp = iter.next();
                IndexEntries entries = kvp.value;
                for (IndexEntry entry: entries.entries) {
                    out.ids.add(entry.resultObjectId);
                }
            }
        }

        return out;
    }

    public RangeResponse keyedPrefixObjScan(
        String storeName,
        String partitionKey,
        String storeKeyPrefix,
        String token,
        int limit
    ) throws LHConnectionError {
        KeyQueryMetadata metadata = streams.queryMetadataForKey(
            storeName, partitionKey, Serdes.String().serializer()
        );
        if (metadata.activeHost().equals(thisHost)) {
            return internalLocalKeyedObjPrefixScan(storeName, storeKeyPrefix, token, limit);
        } else {
            return remoteKeyedPrefixObjScan(
                storeName, storeKeyPrefix, token, limit,
                metadata.activeHost(), metadata.standbyHosts()
            );
        }
    }

    public RangeResponse internalLocalKeyedObjPrefixScan(
        String storeName,
        String prefix,
        String token,
        int limit
    ) {
        RangeResponse out = new RangeResponse();
        ReadOnlyKeyValueStore<String, GETable<?>> store = getStore(storeName);
        try (KeyValueIterator<String, GETable<?>> iter = store.prefixScan(
            prefix, Serdes.String().serializer()
        )) {
            while (iter.hasNext()) {
                KeyValue<String, GETable<?>> kvp = iter.next();
                out.ids.add(kvp.key);
            }
        }
        return out;
    }

    private ReadOnlyKeyValueStore<String, IndexEntries> getIdxStore() {
        return streams.store(
            StoreQueryParameters.fromNameAndType(
                LHConstants.INDEX_STORE_NAME,
                QueryableStoreTypes.keyValueStore()
            )
        );
    }

    private ReadOnlyKeyValueStore<String, GETable<?>> getStore(String storeName) {
        return streams.store(
            StoreQueryParameters.fromNameAndType(
                storeName,
                QueryableStoreTypes.<String, GETable<?>>keyValueStore()
            ).enableStaleStores()
        );
    }

    private <U extends MessageOrBuilder, T extends GETable<U>>
    ReadOnlyKeyValueStore<String, T> getGETableStore(Class<T> cls) {

        return streams.store(
            StoreQueryParameters.fromNameAndType(
                GETable.getBaseStoreName(cls),
                QueryableStoreTypes.keyValueStore()
            )
        );
    }

    private ReadOnlyKeyValueStore<String, LHResponse> getResponseStore(
        Class<? extends POSTable<?>> cls
    ) {

        return streams.store(
            StoreQueryParameters.fromNameAndType(
                POSTable.getResponseStoreName(cls),
                QueryableStoreTypes.keyValueStore()
            )
        );
    }

    private RangeResponse remoteKeyedPrefixObjScan(
        String storeName, String prefixKey, String token, int limit,
        HostInfo host, Set<HostInfo> standbys
    ) throws LHConnectionError {
        LHConnectionError caught = null;
        String path = "/internal/localKeyedPrefixObjScan/" + storeName + "/" + prefixKey;
        path += "?limit=" + limit;
        byte[] resp = null;
        if (token != null) {
            path += "&token=" + token;
        }
        try {
            resp = client.getResponse(host, path);
            return LHSerializable.fromBytes(resp, RangeResponse.class, config);
        } catch(LHConnectionError exn) {
            for (HostInfo standby: standbys) {
                caught = exn;
                try {
                    LHUtil.log("Calling standby: ", standby);
                    return LHSerializable.fromBytes(
                        client.getResponse(standby, path), RangeResponse.class, config
                    );
                } catch(LHConnectionError other) {
                    LHUtil.log("Failed making standby call.");
                } catch(LHSerdeError impossibleError) {
                    throw new RuntimeException(impossibleError);
                }
            }
        } catch(LHSerdeError exn) {
            String respstr = "";
            try {
                respstr = new String(resp);
            } catch(Exception exn2) {}

            throw new LHConnectionError(exn, "Yikes, got the exn: " + exn.getMessage() + respstr);
        }
        throw caught;
    }

    private RangeResponse remoteKeyedPrefixIdxScan(
        String prefixKey, String token, int limit,
        HostInfo host, Set<HostInfo> standbys
    ) throws LHConnectionError {
        LHConnectionError caught = null;
        String path = "/internal/localKeyedPrefixIdxScan/" + prefixKey;
        path += "?limit=" + limit;
        if (token != null) {
            path += "&token=" + token;
        }
        try {
            return LHSerializable.fromBytes(
                client.getResponse(host, path), RangeResponse.class, config
            );
        } catch(LHConnectionError exn) {
            for (HostInfo standby: standbys) {
                caught = exn;
                try {
                    LHUtil.log("Calling standby: ", standby);
                    return LHSerializable.fromBytes(
                        client.getResponse(standby, path), RangeResponse.class, config
                    );
                } catch(LHConnectionError other) {
                    LHUtil.log("Failed making standby call.");
                } catch(LHSerdeError impossibleError) {
                    throw new RuntimeException(impossibleError);
                }
            }
        } catch(LHSerdeError exn) {/* not possible */}

        throw caught;
    }

    private byte[] queryRemoteBytes(
        String storeName, KeyQueryMetadata metadata, String storeKey
    ) throws LHConnectionError {
        Exception caught = null;
        String path = (
            "/internal/storeBytes/" +
            storeName + "/" +
            metadata.partition() + "/" +
            storeKey
        );

        // First, query the active host. If we get it, then return that.
        RemoteStoreQueryResponse resp = new RemoteStoreQueryResponse();
        try {
            byte[] out = client.getResponse(metadata.activeHost(), path + "/true");
            resp = LHSerializable.fromBytes(out, RemoteStoreQueryResponse.class, config);
        } catch(LHConnectionError|LHSerdeError exn) {
            exn.printStackTrace();
            caught = exn;
        }

        // if the request to primary succeeds, woohoo!
        if (resp.isValid()) return resp.result;

        // If we got this far, it means that the Active Host is unavailable.
        resp = null;

        for (HostInfo standbyHost: metadata.standbyHosts()) {
            try {
                byte[] out = client.getResponse(metadata.activeHost(), path + "/false");
                RemoteStoreQueryResponse candidate = LHSerializable.fromBytes(
                    out, RemoteStoreQueryResponse.class, config
                );
                // Check if the thing is valid
                if (candidate.isValid()) {
                    if (resp == null || candidate.approximateLag < resp.approximateLag) {
                        // Then this is the best valid response we've received so far.
                        resp = candidate;
                    }
                }
            } catch(LHConnectionError|LHSerdeError exn) {
                LHUtil.log("Could not contact standby", standbyHost, exn.getMessage());
            }
        }

        if (resp != null) {
            if (!resp.isValid()) throw new RuntimeException("Impossible, see above.");
            return resp.result;
        } else {
            throw new LHConnectionError(
                caught,
                "Failed to look up desired data from active or standby replicas."
            );
        }
    }

    public Long count(String storeName) throws LHConnectionError {
        long result = 0;
        for (StreamsMetadata meta: streams.streamsMetadataForStore(storeName)) {
            byte[] resp = client.getResponse(meta.hostInfo(), "/internal/countLocal/" + storeName);
            result += Long.valueOf(new String(resp));
        }
        return result;
    }

    public Long countLocal(String storeName) {
        ReadOnlyKeyValueStore<?, ?> store = streams.store(
            StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore())
        );
        return store.approximateNumEntries();
    }
}
