package io.littlehorse.server;

import java.util.Map;
import java.util.Set;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
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
import io.littlehorse.common.proto.server.RequestTypePb;
import io.littlehorse.common.util.LHApiClient;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.model.internal.IndexEntries;
import io.littlehorse.server.model.internal.IndexEntry;
import io.littlehorse.server.model.internal.LHResponse;
import io.littlehorse.server.model.internal.POSTableRequest;
import io.littlehorse.server.model.internal.RangeResponse;

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
        String storeName = cls.getSimpleName();
        KeyQueryMetadata metadata = streams.queryMetadataForKey(
            storeName, partitionKey, Serdes.String().serializer()
        );
        if (metadata.activeHost().equals(thisHost)) {
            return localGet(storeKey, cls);
        } else {
            byte[] serialized = queryRemoteBytes(
                storeName, metadata.activeHost(), storeKey, metadata.standbyHosts()
            );
            try {
                return LHSerializable.fromBytes(serialized, cls, config);
            } catch(LHSerdeError exn) {
                throw new LHConnectionError(
                    exn, "Got invalid protobuf over the wire: ");
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

    public RangeResponse keyedPrefixScan(String prefixKey, String token, int limit)
    throws LHConnectionError {
        String storeName = LHConstants.INDEX_STORE_NAME;
        KeyQueryMetadata metadata = streams.queryMetadataForKey(
            storeName, prefixKey, Serdes.String().serializer()
        );
        if (metadata.activeHost().equals(thisHost)) {
            return internalLocalKeyedPrefixScan(prefixKey, token, limit);
        } else {
            return remoteKeyedPrefixScan(
                prefixKey, token, limit, metadata.activeHost(), metadata.standbyHosts()
            );
        }
    }

    public RangeResponse internalLocalKeyedPrefixScan(
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
                QueryableStoreTypes.keyValueStore()
            )
        );
    }

    private <U extends MessageOrBuilder, T extends GETable<U>>
    ReadOnlyKeyValueStore<String, T> getGETableStore(Class<T> cls) {

        return streams.store(
            StoreQueryParameters.fromNameAndType(
                cls.getSimpleName(),
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

    private RangeResponse remoteKeyedPrefixScan(
        String prefixKey, String token, int limit,
        HostInfo host, Set<HostInfo> standbys
    ) throws LHConnectionError {
        LHConnectionError caught = null;
        String path = "/internal/localKeyedPrefixScan/" + prefixKey;
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
        String storeName, HostInfo host, String storeKey, Set<HostInfo> standbys
    ) throws LHConnectionError {
        LHConnectionError caught = null;
        String path = "/internal/storeBytes/" + storeName + "/" + storeKey;
        try {
            return client.getResponse(host, path);
        } catch(LHConnectionError exn) {
            caught = exn;
            for (HostInfo standby: standbys) {
                try {
                    LHUtil.log("Calling standby: ", standby);
                    return client.getResponse(standby, path);
                } catch(LHConnectionError other) {
                    LHUtil.log("Failed making standby call.");
                }
            }
        }
        throw caught;
    }
}
