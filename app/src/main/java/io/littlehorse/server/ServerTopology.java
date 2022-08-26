package io.littlehorse.server;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.observability.ObservabilityEvents;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.common.util.serde.LHDeserializer;
import io.littlehorse.common.util.serde.LHSerde;
import io.littlehorse.common.util.serde.LHSerializer;
import io.littlehorse.server.model.internal.IndexEntries;
import io.littlehorse.server.model.internal.IndexEntryAction;
import io.littlehorse.server.model.internal.LHResponse;
import io.littlehorse.server.model.internal.POSTableRequest;
import io.littlehorse.server.model.scheduler.SchedulerTimer;
import io.littlehorse.server.model.scheduler.WfRunState;
import io.littlehorse.server.model.scheduler.util.SchedulerOutputTsrSer;
import io.littlehorse.server.model.scheduler.util.SchedulerOutputWFRunSer;
import io.littlehorse.server.model.wfrun.TaskRun;
import io.littlehorse.server.model.wfrun.WfRun;
import io.littlehorse.server.processors.IndexFanoutProcessor;
import io.littlehorse.server.processors.IndexProcessor;
import io.littlehorse.server.processors.POSTableProcessor;
import io.littlehorse.server.processors.SchedulerProcessor;
import io.littlehorse.server.processors.WfRunProcessor;


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

public class ServerTopology {
    private static final String WfRunIdxStore = "WF_RUN_INDEX_TMP_STORE";

    public static String schedulerSource = "Scheduler Source";
    public static String schedulerProcessor = "SchedulerProcessor";
    public static String schedulerWfRunSink = "Scheduler WFRun Sink";
    public static String schedulerTaskSink = "Scheduled Tasks";


    public static Topology initTopology(LHConfig config) {
        Topology topo = new Topology();

        addPOSTableSubTopology(topo, WfSpec.class, config);
        addPOSTableSubTopology(topo, TaskDef.class, config);
        addGlobalMetaStore(topo, WfSpec.class, config);
        addGlobalMetaStore(topo, TaskDef.class, config);

        addIdxSubTopology(topo, config);

        addWfRunSubTopology(topo, config);

        addSchedulerTopology(topo, config);

        return topo;
    }

    private static void addSchedulerTopology(Topology topo, LHConfig config) {
        Serde<WfRunEvent> evtSerde = new LHSerde<>(WfRunEvent.class, config);
        Serde<WfRunState> runSerde = new LHSerde<>(WfRunState.class, config);
        Serde<WfSpec> specSerde = new LHSerde<>(WfSpec.class, config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            evtSerde.close();
            runSerde.close();
            specSerde.close();
        }));

        topo.addSource(
            schedulerSource,
            Serdes.String().deserializer(),
            evtSerde.deserializer(),
            LHConstants.WF_RUN_EVENT_TOPIC
        );

        topo.addProcessor(
            schedulerProcessor,
            () -> {
                return new SchedulerProcessor(config);
            },
            schedulerSource
        );

        // Add sink for WFRun
        topo.addSink(
            schedulerWfRunSink,
            LHConstants.WF_RUN_OBSERVABILITY_TOPIC,
            Serdes.String().serializer(),
            new SchedulerOutputWFRunSer(config),
            schedulerProcessor
        );

        // Add sink for Task Schedule
        topo.addSink(
            schedulerTaskSink,
            (k, v, ctx) -> {
                // TODO: Eventually, kafka topics may not exactly match with
                // task def name; or task def name may not match task def id.
                return v.request.taskDefName;
            },
            Serdes.String().serializer(),
            new SchedulerOutputTsrSer(config),
            schedulerProcessor
        );

        // Add state store
        StoreBuilder<KeyValueStore<String, WfRunState>> wfRunStoreBuilder =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(LHConstants.SCHED_WF_RUN_STORE_NAME),
                Serdes.String(),
                runSerde
            );
        topo.addStateStore(wfRunStoreBuilder, schedulerProcessor);

        StoreBuilder<KeyValueStore<String, SchedulerTimer>> timerStoreBuilder =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(LHConstants.SCHED_TIMER_STORE_NAME),
                Serdes.String(),
                new LHSerde<>(SchedulerTimer.class, config)
            );
        topo.addStateStore(timerStoreBuilder, schedulerProcessor);

    }

    private static <U extends MessageOrBuilder, T extends POSTable<U>>
    void addGlobalMetaStore(Topology topology, Class<T> cls, LHConfig config) {
        String sourceName = POSTable.getGlobalStoreSourceName(cls);
        String inputTopic = POSTable.getEntityTopicName(cls);
        String processorName = POSTable.getGlobalStoreProcessorName(cls);

        topology.addGlobalStore(
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(POSTable.getGlobalStoreName(cls)),
                Serdes.String(),
                new LHSerde<>(cls, config)
            ).withLoggingDisabled(),
            sourceName,
            Serdes.String().deserializer(),
            new LHDeserializer<T>(cls, config),
            inputTopic,
            processorName,
            () -> {return new GlobalMetaStoreProcessor<T>(cls);}
        );
    }

    private static void addWfRunSubTopology(Topology topo, LHConfig config) {
        String wfRunProcessor = "WfRun Processor";
        String wfRunSource = "WfRun Source";
        String idxFanoutProcessor = "WfRun Index Fanout Processor";
        String idxSink = "WfRun Index sink";

        topo.addSource(
            wfRunSource,
            Serdes.String().deserializer(),
            new LHDeserializer<>(ObservabilityEvents.class, config),
            LHConstants.WF_RUN_OBSERVABILITY_TOPIC
        );

        topo.addProcessor(
            wfRunProcessor,
            () -> {return new WfRunProcessor(config);},
            wfRunSource
        );

        topo.addProcessor(
            idxFanoutProcessor,
            () -> {return new IndexFanoutProcessor<>(GETable.class, WfRunIdxStore);},
            wfRunProcessor
        );

        topo.addSink(
            idxSink,
            LHConstants.INDEX_TOPIC_NAME,
            Serdes.String().serializer(),
            new LHSerializer<IndexEntryAction>(config),
            idxFanoutProcessor
        );

        StoreBuilder<KeyValueStore<String, WfRun>> wfRunStoreBuilder =
        Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(
                    GETable.getBaseStoreName(WfRun.class)
                ),
                Serdes.String(),
                new LHSerde<>(WfRun.class, config)
            );
        topo.addStateStore(wfRunStoreBuilder, wfRunProcessor);

        StoreBuilder<KeyValueStore<String, TaskRun>> taskRunStoreBuilder =
        Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(
                    GETable.getBaseStoreName(TaskRun.class)
                ),
                Serdes.String(),
                new LHSerde<>(TaskRun.class, config)
            );
        topo.addStateStore(taskRunStoreBuilder, wfRunProcessor);

        StoreBuilder<KeyValueStore<String, IndexEntries>> idxStateStoreBuilder =
        Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(WfRunIdxStore),
                Serdes.String(),
                new LHSerde<>(IndexEntries.class, config)
            );
        topo.addStateStore(idxStateStoreBuilder, idxFanoutProcessor);
    }

    private static void addIdxSubTopology(Topology topo, LHConfig config) {
        topo.addSource(
            "Index Source",
            Serdes.String().deserializer(),
            new LHDeserializer<>(IndexEntryAction.class, config),
            LHConstants.INDEX_TOPIC_NAME
        );

        topo.addProcessor(
            "Index Processor",
            () -> {return new IndexProcessor();},
            "Index Source"
        );

        StoreBuilder<KeyValueStore<String, IndexEntries>> idxStateStoreBuilder =
        Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(LHConstants.INDEX_STORE_NAME),
                Serdes.String(),
                new LHSerde<>(IndexEntries.class, config)
            );
        topo.addStateStore(idxStateStoreBuilder, "Index Processor");

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
            new LHDeserializer<POSTableRequest>(POSTableRequest.class, config),
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
            POSTable.getEntityTopicName(cls),
            Serdes.String().serializer(),
            new LHSerializer<T>(config),
            baseProcessorName
        );

        topo.addProcessor(
            idxFanoutProcessorName,
            () -> {
                return new IndexFanoutProcessor<>(
                    cls, GETable.getIndexStoreName(cls)
                );
            },
            baseProcessorName
        );

        topo.addSink(
            idxSink,
            LHConstants.INDEX_TOPIC_NAME,
            Serdes.String().serializer(),
            new LHSerializer<IndexEntryAction>(config),
            idxFanoutProcessorName
        );

        StoreBuilder<KeyValueStore<String, T>> baseStoreBuilder =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(POSTable.getBaseStoreName(cls)),
                Serdes.String(),
                new LHSerde<>(cls, config)
            );
        topo.addStateStore(baseStoreBuilder, baseProcessorName);

        StoreBuilder<KeyValueStore<String, IndexEntries>> idxStateStoreBuilder =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(POSTable.getIndexStoreName(cls)),
                Serdes.String(),
                new LHSerde<>(IndexEntries.class, config)
            );
        topo.addStateStore(idxStateStoreBuilder, idxFanoutProcessorName);

        StoreBuilder<KeyValueStore<String, LHResponse>> responseStoreBuilder = 
        Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(POSTable.getResponseStoreName(cls)),
            Serdes.String(),
            new LHSerde<LHResponse>(LHResponse.class, config)
        );
        topo.addStateStore(responseStoreBuilder, baseProcessorName);
    }
}

// TODO: This results in duplicate storage and processing. Could be merged/replaced with the
// POSTableProcessor to save space.
class GlobalMetaStoreProcessor<T extends POSTable<?>>
implements Processor<String, T, Void, Void> {
    private KeyValueStore<String, T> store;
    private Class<T> cls;

    public GlobalMetaStoreProcessor(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public void init(final ProcessorContext<Void, Void> ctx) {
        this.store = ctx.getStateStore(POSTable.getGlobalStoreName(cls));
    }

    @Override
    public void process(final Record<String, T> record) {
        T v = record.value();
        String k = record.key();
        if (v == null) {
            LHUtil.log("delete");
            store.delete(k);
        } else {
            LHUtil.log("put");
            store.put(k, v);
        }
    }
}