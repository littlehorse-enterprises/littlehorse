package io.littlehorse.scheduler;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.event.WFRunEvent;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.serde.SchedulerOutputTsrSer;
import io.littlehorse.common.serde.SchedulerOutputWFRunSer;
import io.littlehorse.common.serde.WFRunEventSerde;
import io.littlehorse.common.serde.WFRunSerde;
import io.littlehorse.common.serde.WfSpecSerde;
import io.littlehorse.scheduler.model.WfRunState;

public class SchedulerTopology {
    public static String topoSource = "WFRunEvent Source";
    public static String runtimeProcessor = "WFRuntime";
    public static String wfRunSink = "WFRun Sink";
    public static String taskSchedulerSink = "Scheduled Tasks";
    
    public static Topology initTopology(LHConfig config) {
        Topology topo = new Topology();

        Serde<WFRunEvent> evtSerde = new WFRunEventSerde();
        Serde<WfRunState> runSerde = new WFRunSerde();
        Serde<WfSpec> specSerde = new WfSpecSerde();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            evtSerde.close();
            runSerde.close();
            specSerde.close();
        }));

        topo.addSource(
            topoSource,
            Serdes.String().deserializer(),
            evtSerde.deserializer(),
            LHConstants.WF_RUN_EVENT_TOPIC
        );

        topo.addProcessor(
            runtimeProcessor,
            () -> {
                return new SchedulerProcessor(config);
            },
            topoSource
        );

        // Add sink for WFRun
        topo.addSink(
            wfRunSink,
            LHConstants.WF_RUN_ENTITY_TOPIC,
            Serdes.String().serializer(),
            new SchedulerOutputWFRunSer(),
            runtimeProcessor
        );

        // Add sink for Task Schedule
        topo.addSink(
            taskSchedulerSink,
            (k, v, ctx) -> {
                return v.request.taskDefName;
            },
            Serdes.String().serializer(),
            new SchedulerOutputTsrSer(),
            runtimeProcessor
        );

        // Add state store
        StoreBuilder<KeyValueStore<String, WfRunState>> wfRunStoreBuilder =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(LHConstants.WF_RUN_STORE_NAME),
                Serdes.String(),
                runSerde
            );
        topo.addStateStore(wfRunStoreBuilder, runtimeProcessor);

        // Add global store for WFSpec lookup
        StoreBuilder<KeyValueStore<String, WfSpec>> wfSpecStoreBuilder =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(LHConstants.WF_SPEC_STORE_NAME),
                Serdes.String(),
                specSerde
            ).withLoggingDisabled();

        topo.addGlobalStore(
            wfSpecStoreBuilder,
            "wfSpecViewNode",
            Serdes.String().deserializer(),
            specSerde.deserializer(),
            LHConstants.WF_SPEC_ENTITY_TOPIC,
            "wfSpecViewProcessor",
            () -> {return new WfSpecProcessor();}
        );

        return topo;
    }

    public static void doMain(LHConfig config) {
        Topology topology = initTopology(config);
        KafkaStreams scheduler = new KafkaStreams(topology, config.getStreamsConfig());

        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::close));
        scheduler.start();
    }
}
