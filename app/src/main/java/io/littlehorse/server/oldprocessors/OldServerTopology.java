package io.littlehorse.server.oldprocessors;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.GlobalPOSTable;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.command.subcommand.ExternalEvent;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.server.IndexEntryAction;
import io.littlehorse.common.model.server.LHResponse;
import io.littlehorse.common.model.server.POSTableRequest;
import io.littlehorse.common.model.server.Tags;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.common.util.serde.LHDeserializer;
import io.littlehorse.common.util.serde.LHSerde;
import io.littlehorse.common.util.serde.LHSerializer;
import io.littlehorse.server.oldprocessors.util.GenericOutput;
import java.util.Arrays;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

public class OldServerTopology {

    public static String schedulerSource = "Scheduler Source";
    public static String schedulerProcessor = "SchedulerProcessor";
    public static String schedulerWfRunSink = "Scheduler WFRun Sink";
    public static String schedulerTaskSink = "Scheduled Tasks";
    public static String schedulerTickerSink = "Scheduler Ticks";

    public static String timerSource = "Timer Source";
    public static String timerProcessor = "Timer Processor";
    public static String newTimerSink = "New Timer Sink";
    public static String maturedTimerSink = "Matured Timer Sink";

    public static Topology initTimerTopology(LHConfig config) {
        Topology topo = new Topology();
        Serde<LHTimer> timerSerde = new LHSerde<>(LHTimer.class, config);

        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(() -> {
                    timerSerde.close();
                })
            );

        topo.addSource(
            timerSource,
            Serdes.String().deserializer(),
            timerSerde.deserializer(),
            LHConstants.TIMER_TOPIC_NAME
        );

        topo.addProcessor(
            timerProcessor,
            () -> {
                return new TimerProcessor();
            },
            timerSource
        );

        topo.addSink(
            maturedTimerSink,
            (key, lhTimer, ctx) -> {
                return ((LHTimer) lhTimer).topic;
            },
            Serdes.String().serializer(),
            (topic, lhTimer) -> {
                return ((LHTimer) lhTimer).getPayload(config);
            },
            timerProcessor
        );

        // Add state store
        StoreBuilder<KeyValueStore<String, LHTimer>> timerStoreBuilder = Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(LHConstants.TIMER_STORE_NAME),
            Serdes.String(),
            timerSerde
        );
        topo.addStateStore(timerStoreBuilder, timerProcessor);

        return topo;
    }

    public static Topology initMainTopology(LHConfig config) {
        Topology topo = new Topology();

        addPOSTableSubTopology(topo, WfSpec.class, config);
        addPOSTableSubTopology(topo, TaskDef.class, config);
        addPOSTableSubTopology(topo, ExternalEventDef.class, config);

        addGlobalMetaStore(topo, WfSpec.class, config);
        addGlobalMetaStore(topo, TaskDef.class, config);
        addGlobalMetaStore(topo, ExternalEventDef.class, config);

        addIdxSubTopology(topo, config);

        addSchedulerTopology(topo, config);

        return topo;
    }

    private static void addSchedulerTopology(Topology topo, LHConfig config) {
        Serde<WfRunEvent> evtSerde = new LHSerde<>(WfRunEvent.class, config);
        Serde<WfRun> runSerde = new LHSerde<>(WfRun.class, config);
        Serde<WfSpec> specSerde = new LHSerde<>(WfSpec.class, config);
        Serde<ExternalEvent> extEvtSerde = new LHSerde<>(ExternalEvent.class, config);

        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(() -> {
                    evtSerde.close();
                    runSerde.close();
                    specSerde.close();
                    extEvtSerde.close();
                })
            );

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

        // Add sink for Task Schedule
        topo.addSink(
            schedulerTaskSink,
            (k, v, ctx) -> {
                // TODO: Eventually, kafka topics may not exactly match with
                // task def name; or task def name may not match task def id.
                // May need to look up from task store.
                return ((GenericOutput) v).request.taskDefName;
            },
            Serdes.String().serializer(),
            // Serializer
            (topic, schedulerOutput) -> {
                return ((GenericOutput) schedulerOutput).request.toBytes(config);
            },
            schedulerProcessor
        );

        // Sink for new Timers
        topo.addSink(
            newTimerSink,
            LHConstants.TIMER_TOPIC_NAME,
            Serdes.String().serializer(),
            (topic, schedulerOutput) -> {
                return ((GenericOutput) schedulerOutput).timer.toBytes(config);
            },
            schedulerProcessor
        );

        // Add WfRun state store
        StoreBuilder<KeyValueStore<String, WfRun>> wfRunStoreBuilder = Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(GETable.getBaseStoreName(WfRun.class)),
            Serdes.String(),
            runSerde
        );
        topo.addStateStore(wfRunStoreBuilder, schedulerProcessor);

        // NodeRun State Store
        StoreBuilder<KeyValueStore<String, NodeRun>> nodeRunStoreBuilder = Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(GETable.getBaseStoreName(NodeRun.class)),
            Serdes.String(),
            new LHSerde<>(NodeRun.class, config)
        );
        topo.addStateStore(nodeRunStoreBuilder, schedulerProcessor);

        // Variable Value Store
        StoreBuilder<KeyValueStore<String, Variable>> varValStoreBuilder = Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(GETable.getBaseStoreName(Variable.class)),
            Serdes.String(),
            new LHSerde<>(Variable.class, config)
        );
        topo.addStateStore(varValStoreBuilder, schedulerProcessor);

        // External Event Payload store
        StoreBuilder<KeyValueStore<String, ExternalEvent>> extEvtStoreBuilder = Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(
                GETable.getBaseStoreName(ExternalEvent.class)
            ),
            Serdes.String(),
            extEvtSerde
        );
        topo.addStateStore(extEvtStoreBuilder, schedulerProcessor);

        for (Class<? extends GETable<?>> cls : Arrays.asList(
            Variable.class,
            NodeRun.class,
            WfRun.class,
            ExternalEvent.class
        )) {
            String storeName = GETable.getTagStoreName(cls);
            String processorName = GETable.getTaggingProcessorName(cls);

            topo.addProcessor(
                processorName,
                () -> {
                    return new TaggingProcessor(cls);
                },
                schedulerProcessor
            );

            topo.addSink(
                processorName + "_Sink",
                LHConstants.TAG_TOPIC_NAME,
                Serdes.String().serializer(),
                new LHSerializer<IndexEntryAction>(config),
                processorName
            );

            // Tag Cache State Store
            StoreBuilder<KeyValueStore<String, Tags>> tagCacheStoreBuilder = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(storeName),
                Serdes.String(),
                new LHSerde<>(Tags.class, config)
            );
            topo.addStateStore(tagCacheStoreBuilder, processorName);
        }
    }

    private static <
        U extends MessageOrBuilder, T extends GlobalPOSTable<U>
    > void addGlobalMetaStore(Topology topology, Class<T> cls, LHConfig config) {
        String sourceName = GlobalPOSTable.getGlobalStoreSourceName(cls);
        String inputTopic = GlobalPOSTable.getEntityTopicName(cls);
        String processorName = GlobalPOSTable.getGlobalStoreProcessorName(cls);

        topology.addGlobalStore(
            Stores
                .keyValueStoreBuilder(
                    Stores.persistentKeyValueStore(
                        GlobalPOSTable.getGlobalStoreName(cls)
                    ),
                    Serdes.String(),
                    new LHSerde<>(cls, config)
                )
                .withLoggingDisabled(),
            sourceName,
            Serdes.String().deserializer(),
            new LHDeserializer<T>(cls, config),
            inputTopic,
            processorName,
            () -> {
                return new GlobalMetaStoreProcessor<T>(cls);
            }
        );
    }

    /**
     * This subtopology processes all of the Tag events sent to the
     */
    private static void addIdxSubTopology(Topology topo, LHConfig config) {
        topo.addSource(
            "Index Source",
            Serdes.String().deserializer(),
            new LHDeserializer<>(IndexEntryAction.class, config),
            LHConstants.TAG_TOPIC_NAME
        );

        topo.addProcessor(
            "Index Processor",
            () -> {
                return new IndexProcessor();
            },
            "Index Source"
        );

        StoreBuilder<KeyValueStore<String, Tags>> idxStateStoreBuilder = Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(LHConstants.INDEX_STORE_NAME),
            Serdes.String(),
            new LHSerde<>(Tags.class, config)
        );
        topo.addStateStore(idxStateStoreBuilder, "Index Processor");
    }

    private static <
        U extends MessageOrBuilder, T extends POSTable<U>
    > void addPOSTableSubTopology(Topology topo, Class<T> cls, LHConfig config) {
        String sourceName = POSTable.getTopoSourceName(cls);
        String baseProcessorName = POSTable.getTopoProcessorName(cls);
        String taggingProcessorName = POSTable.getTaggingProcessorName(cls);
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
            (topic, genericOutput) -> {
                return ((GenericOutput) genericOutput).thingToTag.toBytes(config);
            },
            baseProcessorName
        );

        topo.addProcessor(
            taggingProcessorName,
            () -> {
                return new TaggingProcessor(cls);
            },
            baseProcessorName
        );

        topo.addSink(
            idxSink,
            LHConstants.TAG_TOPIC_NAME,
            Serdes.String().serializer(),
            new LHSerializer<IndexEntryAction>(config),
            taggingProcessorName
        );

        StoreBuilder<KeyValueStore<String, T>> baseStoreBuilder = Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(POSTable.getBaseStoreName(cls)),
            Serdes.String(),
            new LHSerde<>(cls, config)
        );
        topo.addStateStore(baseStoreBuilder, baseProcessorName);

        StoreBuilder<KeyValueStore<String, Tags>> idxStateStoreBuilder = Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(POSTable.getTagStoreName(cls)),
            Serdes.String(),
            new LHSerde<>(Tags.class, config)
        );
        topo.addStateStore(idxStateStoreBuilder, taggingProcessorName);

        StoreBuilder<KeyValueStore<String, LHResponse>> responseStoreBuilder = Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(POSTable.getResponseStoreName(cls)),
            Serdes.String(),
            new LHSerde<LHResponse>(LHResponse.class, config)
        );
        topo.addStateStore(responseStoreBuilder, baseProcessorName);
    }
}

// TODO: This results in duplicate storage and processing. Could be merged/replaced with the
// POSTableProcessor to save space.
class GlobalMetaStoreProcessor<T extends GlobalPOSTable<?>>
    implements Processor<String, T, Void, Void> {

    private KeyValueStore<String, T> store;
    private Class<T> cls;

    public GlobalMetaStoreProcessor(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public void init(final ProcessorContext<Void, Void> ctx) {
        this.store = ctx.getStateStore(GlobalPOSTable.getGlobalStoreName(cls));
    }

    @Override
    public void process(final Record<String, T> record) {
        T v = record.value();
        String k = record.key();
        if (v == null) {
            LHUtil.log("delete");
            store.delete(k);
        } else {
            store.put(k, v);
            // TODO: Need to ensure somehow that there's never conflict between ID
            // and name.
            store.put(v.getName(), v);
        }
    }
}
