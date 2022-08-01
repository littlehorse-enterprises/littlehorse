package io.littlehorse.server.processors;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.model.internal.MetadataEntity;
import io.littlehorse.server.model.internal.MetadataEvent;
import io.littlehorse.server.model.internal.PutTaskDef;
import io.littlehorse.server.model.internal.PutWfSpec;

class MetadataStore {
    private KeyValueStore<String, TaskDef> taskDefStore;
    // private KeyValueStore<String, WfSpec> wfSpecStore;

    public MetadataStore(
        KeyValueStore<String, TaskDef> taskDefStore,
        KeyValueStore<String, WfSpec> wfSpecStore
    ) {
        this.taskDefStore = taskDefStore;
        // this.wfSpecStore = wfSpecStore;
    }

    public TaskDef lookupTaskDef(String name) {
        return taskDefStore.get(name);
    }
}

public class MetadataProcessor
    implements Processor<String, MetadataEvent, String, MetadataEntity>
{
    private KeyValueStore<String, TaskDef> taskDefStore;
    private KeyValueStore<String, WfSpec> wfSpecStore;
    private ProcessorContext<String, MetadataEntity> context;
    private MetadataStore ctxStore;

    @Override
    public void init(final ProcessorContext<String, MetadataEntity> context) {
        this.context = context;
        this.taskDefStore = context.getStateStore(LHConstants.TASK_DEF_STORE_NAME);
        this.wfSpecStore = context.getStateStore(LHConstants.WF_SPEC_STORE_NAME);
        this.ctxStore = new MetadataStore(this.taskDefStore, this.wfSpecStore);
    }

    @Override
    public void process(final Record<String, MetadataEvent> record) {
        MetadataEvent me = record.value();

        switch (me.type) {
        case PUT_TASK_DEF:
            handlePutTaskDef(record, me);
            break;
        case PUT_WF_SPEC:
            handlePutWfSpec(record, me);
            break;
        case DELETE_TASK_DEF:
            handleDeleteTaskDef(record, me);
            break;
        case DELETE_WF_SPEC:
            handleDeleteWfSpec(record, me);
            break;
        case EVENT_NOT_SET:
            LHUtil.log("uh oh, unset event. Not possible.");
            break;
        }
    }

    private void handlePutTaskDef(
        final Record<String, MetadataEvent> record, MetadataEvent me
    ) {
        PutTaskDef ptd = me.putTaskDef;
        TaskDef updater = ptd.spec;
        TaskDef taskDef = taskDefStore.get(updater.getStoreKey());

        if (taskDef != null) {
            // For now, taskdefs are immutable. Just update the offset.
            taskDef.processChange(updater);
        } else {
            taskDef = updater;
            taskDef.validate();
        }
        taskDef.setLastUpdatedOffset(context.recordMetadata().get().offset());
        taskDefStore.put(taskDef.getStoreKey(), taskDef);
    }

    private void handlePutWfSpec(
        final Record<String, MetadataEvent> record, MetadataEvent me
    ) {
        PutWfSpec pws = me.putWfSpec;
        WfSpec updater = pws.spec;
        WfSpec wfSpec = wfSpecStore.get(updater.getStoreKey());

        if (wfSpec == null) {
            wfSpec = updater;
            updater.validate(ctxStore);
        } else {
            wfSpec.update(updater, ctxStore);
        }
        wfSpec.setLastUpdatedOffset(context.recordMetadata().get().offset());
        wfSpecStore.put(wfSpec.getStoreKey(), wfSpec);
    }

    private void handleDeleteTaskDef(
        final Record<String, MetadataEvent> record, MetadataEvent me
    ) {
        
    }

    private void handleDeleteWfSpec(
        final Record<String, MetadataEvent> record, MetadataEvent me
    ) {
        
    }
}
