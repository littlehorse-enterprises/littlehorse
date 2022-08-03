package io.littlehorse.server;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Topology;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.POSTableRequestPb;
import io.littlehorse.common.proto.POSTableRequestPbOrBuilder;
import io.littlehorse.common.proto.POSTableResponsePb;
import io.littlehorse.common.proto.TaskDefPbOrBuilder;
import io.littlehorse.common.proto.WFSpecPb;
import io.littlehorse.common.proto.WFSpecPbOrBuilder;
import io.littlehorse.common.util.serde.LHSerde;
import io.littlehorse.server.model.internal.POSTableRequest;
import io.littlehorse.server.processors.POSTableProcessor;
import io.littlehorse.server.serde.MetadataEntitySerializer;
import io.littlehorse.server.serde.POSTableRequestSerde;
import io.littlehorse.server.serde.POSTableResponsePbSerde;

public class ServerTopology {
    public static String wfSpecSource = "wfSpecSource";
    public static String wfSpecProcessor = "wfSpecProcessor";
    public static String wfSpecSink = "wfSpecSink";

    public static String taskDefSource = "taskDefSource";
    public static String taskDefProcessor = "taskDefProcessor";
    public static String taskDefSink = "taskDefSink";

    public static String wfRunSource = "wfRunSource";

    public static Topology initTopology(LHConfig config) {
        Topology topo = new Topology();

        Serde<POSTableRequest> reqSerde = new LHSerde<
            POSTableRequestPb, POSTableRequest
        >(POSTableRequest.class);

        Serde<POSTableResponsePb> respSerde = new LHSerde<
            POSTableResponsePb, POSTableResponse
        >(POSTableResponse.class);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            reqSerde.close();
            respSerde.close();
        }));

        topo.addSource(
            wfSpecSource,
            Serdes.String().deserializer(),
            reqSerde.deserializer(),
            POSTable.getRequestTopicName(WfSpec.class)
        );

        topo.addProcessor(
            wfSpecProcessor,
            () -> {return new POSTableProcessor<WFSpecPbOrBuilder, WfSpec>();},
            wfSpecSource
        );

        topo.addSink(
            wfSpecSink,
            POSTable.getEntitytTopicName(WfSpec.class),
            Serdes.String().serializer(),
            new MetadataEntitySerializer(),
            wfSpecProcessor
        );

        // TaskDef 
        topo.addSource(
            taskDefSource,
            Serdes.String().deserializer(),
            reqSerde.deserializer(),
            POSTable.getRequestTopicName(TaskDef.class)
        );

        topo.addProcessor(
            taskDefProcessor,
            () -> {return new POSTableProcessor<TaskDefPbOrBuilder, TaskDef>();},
            taskDefSource
        );

        topo.addSink(
            taskDefSink,
            POSTable.getEntitytTopicName(TaskDef.class),
            Serdes.String().serializer(),
            new TaskDefSerializer(),
            taskDefProcessor
        );


        return topo;
    }
}
