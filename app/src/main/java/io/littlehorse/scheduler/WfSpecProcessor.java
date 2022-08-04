package io.littlehorse.scheduler;

import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.processor.api.Processor;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.meta.WfSpec;


public class WfSpecProcessor implements Processor<String, WfSpec, Void, Void> {
    private KeyValueStore<String, WfSpec> specStore;

    @Override
    public void init(final ProcessorContext<Void, Void> context) {
        specStore = context.getStateStore(LHConstants.SCHED_WF_SPEC_STORE_NAME);
    }

    @Override
    public void process(final Record<String, WfSpec> r) {
        String k = r.key();
        WfSpec val = r.value();

        if (val == null) {
            specStore.delete(k);
        } else {
            System.out.println("Saved wfspec");
            specStore.put(k, val);
        }
    }
}
