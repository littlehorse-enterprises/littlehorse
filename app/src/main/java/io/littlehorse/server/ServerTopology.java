package io.littlehorse.server;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.util.serde.LHDeserializer;
import io.littlehorse.common.util.serde.LHSerde;
import io.littlehorse.common.util.serde.LHSerializer;
import io.littlehorse.server.model.internal.IndexEntries;
import io.littlehorse.server.model.internal.IndexEntryAction;
import io.littlehorse.server.model.internal.LHResponse;
import io.littlehorse.server.model.internal.POSTableRequest;
import io.littlehorse.server.processors.IndexFanoutProcessor;
import io.littlehorse.server.processors.POSTableProcessor;

public class ServerTopology {
    public static String wfSpecSource = "wfSpecSource";
    public static String wfSpecProcessor = "wfSpecProcessor";
    public static String wfSpecSink = "wfSpecSink";

    public static String taskDefSource = "taskDefSource";
    public static String taskDefProcessor = "taskDefProcessor";
    public static String taskDefSink = "taskDefSink";

    public static String indexFanoutProcessor = "indexFanoutProcessor";
    public static String indexStoreSink = "indexStoreSink";
    public static String indexStoreProcessor = "indexStoreProcessor";

    public static String wfRunSource = "wfRunSource";

    public static Topology initTopology(LHConfig config) {
        Topology topo = new Topology();

        addPOSTableSubTopology(topo, WfSpec.class, config);
        addPOSTableSubTopology(topo, TaskDef.class, config);

        return topo;
    }

    private static <U extends MessageOrBuilder, T extends POSTable<U>>
    void addPOSTableSubTopology(Topology topo, Class<T> cls, LHConfig config) {

        String sourceName = POSTable.getTopoSourceName(cls);
        String baseProcessorName = POSTable.getTopoProcessorName(cls);
        String idxFanoutProcessorName = POSTable.getIdxFanoutProcessorName(cls);
        String idxSink = POSTable.getIdxSinkName(cls);
        String entitySink = POSTable.getEntitySinkName(cls);

        topo.addSource(
            sourceName,
            Serdes.String().deserializer(),
            new LHDeserializer<POSTableRequest>(POSTableRequest.class),
            POSTable.getRequestTopicName(cls)
        );

        topo.addProcessor(
            baseProcessorName,
            () -> {
                return new POSTableProcessor<U, T>(cls, config);
            },
            sourceName
        );

        topo.addSink(
            entitySink,
            POSTable.getEntitytTopicName(cls),
            Serdes.String().serializer(),
            new LHSerializer<T>(),
            baseProcessorName
        );

        topo.addProcessor(
            idxFanoutProcessorName,
            () -> {
                return new IndexFanoutProcessor<>(cls);
            },
            baseProcessorName
        );

        topo.addSink(
            idxSink,
            LHConstants.INDEX_TOPIC_NAME,
            Serdes.String().serializer(),
            new LHSerializer<IndexEntryAction>(),
            idxFanoutProcessorName
        );

        StoreBuilder<KeyValueStore<String, T>> baseStoreBuilder =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(POSTable.getBaseStoreName(cls)),
                Serdes.String(),
                new LHSerde<>(cls)
            );
        topo.addStateStore(baseStoreBuilder, baseProcessorName);

        StoreBuilder<KeyValueStore<String, IndexEntries>> idxStateStoreBuilder =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(POSTable.getIndexStoreName(cls)),
                Serdes.String(),
                new LHSerde<>(IndexEntries.class)
            );
        topo.addStateStore(idxStateStoreBuilder, idxFanoutProcessorName);

        StoreBuilder<KeyValueStore<String, LHResponse>> responseStoreBuilder = 
        Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(POSTable.getResponseStoreName(cls)),
            Serdes.String(),
            new LHSerde<LHResponse>(LHResponse.class)
        );
        topo.addStateStore(responseStoreBuilder, baseProcessorName);
    }
}
