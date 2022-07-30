package io.littlehorse.server;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.HostInfo;
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
import io.littlehorse.common.model.server.IndexEntry;
import io.littlehorse.common.model.server.RangeResponse;
import io.littlehorse.common.util.LHApiClient;

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
    T get(String id, Class<T> cls) throws LHConnectionError {

        String storeName = cls.getSimpleName();
        KeyQueryMetadata metadata = streams.queryMetadataForKey(
            storeName, id, Serdes.String().serializer()
        );
        if (metadata.activeHost().equals(thisHost)) {
            return localGet(id, cls);
        } else {
            byte[] serialized = queryRemoteBytes(
                storeName, metadata.activeHost(), id
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
    T localGet(String id, Class<T> cls) {
        return getGETableStore(cls).get(id);
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

    private <U extends MessageOrBuilder, T extends GETable<U>>
    ReadOnlyKeyValueStore<String, T> getGETableStore(Class<T> cls) {

        return streams.store(
            StoreQueryParameters.fromNameAndType(
                cls.getName(),
                QueryableStoreTypes.keyValueStore()
            )
        );
    }

    public ReadOnlyKeyValueStore<String, IndexEntry> getIndexStore() {
        return streams.store(
            StoreQueryParameters.fromNameAndType(
                LHConstants.INDEX_STORE_NAME,
                QueryableStoreTypes.keyValueStore()
            )
        );
    }

    private byte[] queryRemoteBytes(
        String storeName, HostInfo host, String storeKey
    ) throws LHConnectionError {
        return client.getResponse(host, "/storeBytes/" + storeName + "/" + storeKey);
    }
}
