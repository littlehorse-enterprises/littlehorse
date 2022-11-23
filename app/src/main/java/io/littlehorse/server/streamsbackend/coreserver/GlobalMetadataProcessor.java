package io.littlehorse.server.streamsbackend.coreserver;

import io.littlehorse.common.model.command.MetadataCmd;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsbackend.KafkaStreamsBackend;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

public class GlobalMetadataProcessor
    implements Processor<String, MetadataCmd, Void, Void> {

    private boolean isHotPartition;
    private KafkaStreamsBackend backend;

    public static String WF_SPEC_PREFIX = "WF_SPEC";
    public static String TASK_DEF_PREFIX = "TASK_DEF";
    public static String EXTERNAL_EVENT_DEF_PREFIX = "EXTERNAL_EVENT_DEF";

    public void init(final ProcessorContext<Void, Void> ctx) {
        // TODO: Maybe do something here...?
    }

    public void process(final Record<String, MetadataCmd> record) {
        String key = record.key();
        String prefix = key.substring(0, key.indexOf("/"));

        if (prefix.equals(WF_SPEC_PREFIX)) {
            processWfSpec(record.value());
        } else if (prefix.equals(TASK_DEF_PREFIX)) {
            processTaskDef(record.value());
        } else if (prefix.equals(EXTERNAL_EVENT_DEF_PREFIX)) {
            processExternalEventDef(record.value());
        } else {
            LHUtil.log("Invalid key: " + key);
        }
    }

    private void processWfSpec(MetadataCmd cmd) {}

    private void processExternalEventDef(MetadataCmd cmd) {}

    private void processTaskDef(MetadataCmd cmd) {}
}
