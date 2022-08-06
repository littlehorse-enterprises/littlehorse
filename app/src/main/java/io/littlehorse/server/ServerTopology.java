package io.littlehorse.server;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.wfspec.TaskDefPbOrBuilder;
import io.littlehorse.common.proto.wfspec.WFSpecPbOrBuilder;
import io.littlehorse.common.util.serde.LHDeserializer;
import io.littlehorse.common.util.serde.LHSerde;
import io.littlehorse.server.model.internal.POSTableRequest;
import io.littlehorse.server.model.internal.LHResponse;
import io.littlehorse.server.processors.POSTableProcessor;
import io.littlehorse.server.processors.POSTableProcessorOutput;

public class ServerTopology {
    public static String wfSpecSource = "wfSpecSource";
    public static String wfSpecProcessor = "wfSpecProcessor";
    public static String wfSpecSink = "wfSpecSink";

    public static String taskDefSource = "taskDefSource";
    public static String taskDefProcessor = "taskDefProcessor";
    public static String taskDefSink = "taskDefSink";

    public static String indexStoreSink = "indexStoreSink";

    public static String wfRunSource = "wfRunSource";

    public static Topology initTopology(LHConfig config) {
        Topology topo = new Topology();

        Serde<LHResponse> respSerde = new LHSerde<LHResponse>(
            LHResponse.class
        );

        Runtime.getRuntime().addShutdownHook(new Thread(respSerde::close));

        topo.addSource(
            wfSpecSource,
            Serdes.String().deserializer(),
            new LHDeserializer<POSTableRequest>(POSTableRequest.class),
            POSTable.getRequestTopicName(WfSpec.class)
        );

        topo.addProcessor(
            wfSpecProcessor,
            () -> {return new POSTableProcessor<WFSpecPbOrBuilder, WfSpec>(
                WfSpec.class, config, wfSpecSink
            );},
            wfSpecSource
        );

        topo.addSink(
            wfSpecSink,
            POSTable.getEntitytTopicName(WfSpec.class),
            Serdes.String().serializer(),
            (topic, output) -> {
                return ((POSTableProcessorOutput)output).t.toBytes();
            },
            wfSpecProcessor
        );

        // TaskDef 
        topo.addSource(
            taskDefSource,
            Serdes.String().deserializer(),
            new LHDeserializer<POSTableRequest>(POSTableRequest.class),
            POSTable.getRequestTopicName(TaskDef.class)
        );

        topo.addProcessor(
            taskDefProcessor,
            () -> {return new POSTableProcessor<TaskDefPbOrBuilder, TaskDef>(
                TaskDef.class, config, taskDefSink
            );},
            taskDefSource
        );

        topo.addSink(
            taskDefSink,
            POSTable.getEntitytTopicName(TaskDef.class),
            Serdes.String().serializer(),
            (topic, output) -> {
                return ((POSTableProcessorOutput)output).t.toBytes();
            },
            taskDefProcessor
        );

        topo.addSink(
            indexStoreSink,
            LHConstants.INDEX_TOPIC_NAME,
            Serdes.String().serializer(),
            (topic, output) -> {
                return ((POSTableProcessorOutput)output).action.toBytes();
            },
            wfSpecProcessor, taskDefProcessor
        );

        StoreBuilder<KeyValueStore<String, LHResponse>> responseStoreBuilder = 
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(LHConstants.RESPONSE_STORE_NAME),
                Serdes.String(),
                new LHSerde<LHResponse>(LHResponse.class)
            );

        topo.addStateStore(responseStoreBuilder, taskDefProcessor, wfSpecProcessor);

        StoreBuilder<KeyValueStore<String, WfSpec>> wfSpecStoreBuilder =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(
                    POSTable.getBaseStoreName(WfSpec.class)
                ),
                Serdes.String(),
                new LHSerde<>(WfSpec.class)
            );
        topo.addStateStore(wfSpecStoreBuilder, wfSpecProcessor);

        StoreBuilder<KeyValueStore<String, TaskDef>> taskDefStoreBuilder =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(POSTable.getBaseStoreName(
                    TaskDef.class
                )),
                Serdes.String(),
                new LHSerde<>(TaskDef.class)
            );
        topo.addStateStore(taskDefStoreBuilder, taskDefProcessor);

        return topo;
    }
}
