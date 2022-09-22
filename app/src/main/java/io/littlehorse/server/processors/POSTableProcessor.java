package io.littlehorse.server.processors;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.server.LHResponse;
import io.littlehorse.common.model.server.POSTableRequest;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.server.processors.util.GenericOutput;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

public class POSTableProcessor<U extends MessageOrBuilder, T extends POSTable<U>>
    implements Processor<String, POSTableRequest, String, GenericOutput> {

    private KeyValueStore<String, T> store;
    private KeyValueStore<String, LHResponse> responseStore;
    private Class<T> cls;
    private ProcessorContext<String, GenericOutput> context;
    private LHGlobalMetaStores dbClient;
    private LHConfig config;

    public POSTableProcessor(Class<T> cls, LHConfig config) {
        this.cls = cls;
        this.config = config;
    }

    @Override
    public void init(final ProcessorContext<String, GenericOutput> context) {
        this.context = context;
        this.store = context.getStateStore(POSTable.getBaseStoreName(cls));
        this.responseStore =
            context.getStateStore(POSTable.getResponseStoreName(cls));
        this.dbClient = new LHGlobalMetaStores(context);
    }

    @Override
    public void process(final Record<String, POSTableRequest> record) {
        POSTableRequest req = record.value();
        String key = record.key();
        T newT;

        try {
            newT = LHSerializable.fromBytes(req.payload, cls, config);
        } catch (LHSerdeError exn) {
            // TODO: throw an error in response
            return;
        }

        T oldT = store.get(key);
        LHResponse resp = new LHResponse(config);
        resp.id = key;

        try {
            newT.handlePost(oldT, dbClient, config);
            resp.result = newT;
            resp.code = LHResponseCodePb.OK;
            GenericOutput out = new GenericOutput();
            out.thingToTag = newT;
            Record<String, GenericOutput> newRec = new Record<>(
                key,
                out,
                record.timestamp()
            );
            newRec
                .headers()
                .add(LHConstants.OBJECT_ID_HEADER, newT.getObjectId().getBytes());

            context.forward(newRec);

            store.put(key, newT);
        } catch (LHValidationError exn) {
            resp.message = exn.getMessage();
            resp.code = LHResponseCodePb.VALIDATION_ERROR;
        }

        responseStore.put(req.requestId, resp);
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
