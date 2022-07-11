package io.littlehorse.scheduler;

import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.event.WFRunEvent;
import io.littlehorse.common.model.run.WFRun;

public class SchedulerProcessor
    implements Processor<String, WFRunEvent, String, SchedulerOutput>
{
    private KeyValueStore<String, WFRun> wfRunStore;
    private ProcessorContext<String, SchedulerOutput> context;
    private LHConfig config;

    public SchedulerProcessor(LHConfig config) {
        this.config = config;
    }

    @Override
    public void init(final ProcessorContext<String, SchedulerOutput> context) {
        wfRunStore = context.getStateStore(LHConstants.WF_RUN_STORE_NAME);
        this.context = context;
    }

    @Override
    public void process(final Record<String, WFRunEvent> record) {
        System.out.println("Got a record.");
    }
}
