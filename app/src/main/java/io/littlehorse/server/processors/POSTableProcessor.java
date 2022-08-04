package io.littlehorse.server.processors;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.server.model.internal.POSTableRequest;
import io.littlehorse.server.model.internal.POSTableResponse;

public class POSTableProcessor<U extends MessageOrBuilder, T extends POSTable<U>>
    implements Processor<String, POSTableRequest, String, T>
{
    private KeyValueStore<String, T> store;
    private KeyValueStore<String, POSTableResponse> responseStore;
    private Class<T> cls;
    private ProcessorContext<String, T> context;

    public POSTableProcessor(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public void init(final ProcessorContext<String, T> context) {
        this.context = context;
        this.store = context.getStateStore(
            POSTable.getBaseStoreName(cls)
        );
        this.responseStore = context.getStateStore(LHConstants.RESPONSE_STORE_NAME);
    }

    @Override
    public void process(final Record<String, POSTableRequest> record) {
        POSTableRequest req = record.value();
        String key = record.key();
        T newT;

        try {
            newT = LHSerializable.fromBytes(req.payload, cls);
        } catch(LHSerdeError exn) {
            // TODO: throw an error in response
            return;
        }

        T oldT = store.get(key);

        try {
            newT.handlePost(oldT);
        } catch(LHConnectionError exn) {
            return;
        } catch(LHValidationError exn) {
            return;
        }

        POSTableResponse resp = new POSTableResponse();
        resp.status = 200;
        resp.id = key;
        resp.payload = newT.toBytes();

        responseStore.put(req.requestId, resp);
        store.put(key, newT);

        context.forward(new Record<String, T>(
            key, newT, record.timestamp()
        ));
    }
}

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