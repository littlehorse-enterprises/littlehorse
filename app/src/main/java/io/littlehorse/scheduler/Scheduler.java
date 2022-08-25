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
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.util.kstreamlisteners.KStreamsStateListener;
import io.littlehorse.common.util.serde.LHSerde;
import io.littlehorse.scheduler.model.SchedulerTimer;
import io.littlehorse.scheduler.model.WfRunState;
import io.littlehorse.scheduler.serde.SchedulerOutputTsrSer;
import io.littlehorse.scheduler.serde.SchedulerOutputWFRunSer;

public class Scheduler {
    public static String topoSource = "WFRunEvent Source";
    public static String runtimeProcessor = "WFRuntime";
    public static String wfRunSink = "WFRun Sink";
    public static String taskSchedulerSink = "Scheduled Tasks";

    public static Topology initTopology(LHConfig config) {
        Topology topo = new Topology();

        Serde<WfRunEvent> evtSerde = new LHSerde<>(WfRunEvent.class, config);
        Serde<WfRunState> runSerde = new LHSerde<>(WfRunState.class, config);
        Serde<WfSpec> specSerde = new LHSerde<>(WfSpec.class, config);

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
            LHConstants.WF_RUN_OBSERVABILITY_TOPIC,
            Serdes.String().serializer(),
            new SchedulerOutputWFRunSer(config),
            runtimeProcessor
        );

        // Add sink for Task Schedule
        topo.addSink(
            taskSchedulerSink,
            (k, v, ctx) -> {
                // TODO: Eventually, kafka topics may not exactly match with
                // task def name; or task def name may not match task def id.
                return v.request.taskDefName;
            },
            Serdes.String().serializer(),
            new SchedulerOutputTsrSer(config),
            runtimeProcessor
        );

        // Add state store
        StoreBuilder<KeyValueStore<String, WfRunState>> wfRunStoreBuilder =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(LHConstants.SCHED_WF_RUN_STORE_NAME),
                Serdes.String(),
                runSerde
            );
        topo.addStateStore(wfRunStoreBuilder, runtimeProcessor);

        StoreBuilder<KeyValueStore<String, SchedulerTimer>> timerStoreBuilder =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(LHConstants.SCHED_TIMER_STORE_NAME),
                Serdes.String(),
                new LHSerde<>(SchedulerTimer.class, config)
            );
        topo.addStateStore(timerStoreBuilder, runtimeProcessor);

        return topo;
    }

    public static void doMain(LHConfig config) {
        Topology topology = initTopology(config);
        KafkaStreams scheduler = new KafkaStreams(topology, config.getStreamsConfig());
        scheduler.setStateListener(new KStreamsStateListener());

        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::close));
        scheduler.start();
    }
}
