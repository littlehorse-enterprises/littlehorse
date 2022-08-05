package io.littlehorse.server.processors;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDatabaseClient;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.server.model.internal.POSTableRequest;
import io.littlehorse.server.model.internal.LHResponse;

public class POSTableProcessor<U extends MessageOrBuilder, T extends POSTable<U>>
    implements Processor<String, POSTableRequest, String, T>
{
    private KeyValueStore<String, T> store;
    private KeyValueStore<String, LHResponse> responseStore;
    private Class<T> cls;
    private ProcessorContext<String, T> context;
    private LHDatabaseClient dbClient;

    public POSTableProcessor(Class<T> cls, LHConfig config) {
        this.dbClient = new LHDatabaseClient(config);
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
        LHResponse resp = new LHResponse();
        resp.id = key;

        try {
            newT.handlePost(oldT, dbClient);
            resp.result = newT;
            resp.code = LHResponseCodePb.OK;
            store.put(key, newT);
            context.forward(new Record<String, T>(
                key, newT, record.timestamp()
            ));

        } catch(LHConnectionError exn) {
            resp.message = exn.getMessage();
            resp.code = LHResponseCodePb.CONNECTION_ERROR;

        } catch(LHValidationError exn) {
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