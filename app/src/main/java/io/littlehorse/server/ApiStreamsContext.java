package io.littlehorse.server;

import java.util.Set;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.util.LHApiClient;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.model.internal.RangeResponse;

public class ApiStreamsContext {
    private KafkaStreams streams;
    private HostInfo thisHost;
    private LHApiClient client;

    public ApiStreamsContext(LHConfig config, KafkaStreams streams) {
        this.streams = streams;
        this.thisHost = config.getHostInfo();
        this.client = config.getApiClient();
    }

    public <U extends MessageOrBuilder, T extends GETable<U>>
    T get(String storeKey, String partitionKey, Class<T> cls)
    throws LHConnectionError {
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
                return LHSerializable.fromBytes(serialized, cls);
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
        return obj == null ? null : obj.toBytes();
    }

    public <U extends MessageOrBuilder, T extends POSTable<U>>T post(T toSave, Class<T> cls)
    throws LHConnectionError {
        // Step 1: send the POST to the kafka topic.

        // Step 2: call waitForProcessing() on the record metadata.
        return toSave;
    }

    public RangeResponse prefixSearch(String storeName, byte[] prefix)
    throws LHConnectionError {
        return null;
    }

    public RangeResponse prefixRange(String storeName, byte[] start, byte[] end)
    throws LHConnectionError {
        return null;
    }

    public RangeResponse localPrefixSearch(String storeName, byte[] prefix) {
        return null;
    }

    public RangeResponse localPrefixRange(String storeName, byte[] start, byte[] end) {
        return null;
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
                cls.getName(),
                QueryableStoreTypes.keyValueStore()
            )
        );
    }

    private byte[] queryRemoteBytes(
        String storeName, HostInfo host, String storeKey, Set<HostInfo> standbys
    ) throws LHConnectionError {
        LHConnectionError caught = null;
        String path = "/storeBytes/" + storeName + "/" + storeKey;
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
