package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.PutTaskDefReply;
import io.littlehorse.common.model.meta.KafkaTaskQueueDetails;
import io.littlehorse.common.model.meta.OutputSchema;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.PutTaskDefPb;
import io.littlehorse.common.proto.PutTaskDefPb.QueueTypeCase;
import io.littlehorse.common.proto.PutTaskDefPbOrBuilder;
import io.littlehorse.common.proto.TaskDefPb.QueueDetailsCase;
import io.littlehorse.common.proto.VariableDefPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.CommandProcessorDao;
import java.util.HashMap;
import java.util.Map;

public class PutTaskDef extends SubCommand<PutTaskDefPb> {

    public String name;
    public OutputSchema outputSchema;
    public Map<String, VariableDef> inputVars;
    public QueueTypeCase type;

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public PutTaskDef() {
        inputVars = new HashMap<>();
    }

    public Class<PutTaskDefPb> getProtoBaseClass() {
        return PutTaskDefPb.class;
    }

    public PutTaskDefPb.Builder toProto() {
        PutTaskDefPb.Builder out = PutTaskDefPb.newBuilder();
        out.setName(name);
        if (outputSchema != null) out.setOutputSchema(outputSchema.toProto());

        for (Map.Entry<String, VariableDef> e : inputVars.entrySet()) {
            out.putInputVars(e.getKey(), e.getValue().toProto().build());
        }
        switch (type) {
            case KAFKA:
                out.setKafka(true);
                break;
            case RPC:
                out.setRpc(true);
                break;
            case QUEUETYPE_NOT_SET:
                // we want default to be RPC queue.
                out.setRpc(true);
                break;
        }

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        PutTaskDefPbOrBuilder p = (PutTaskDefPbOrBuilder) proto;
        name = p.getName();
        for (Map.Entry<String, VariableDefPb> e : p.getInputVarsMap().entrySet()) {
            inputVars.put(e.getKey(), VariableDef.fromProto(e.getValue()));
        }
        if (p.hasOutputSchema()) {
            outputSchema = OutputSchema.fromProto(p.getOutputSchema());
        }
        type = p.getQueueTypeCase();
    }

    public boolean hasResponse() {
        return true;
    }

    public PutTaskDefReply process(CommandProcessorDao dao, LHConfig config) {
        PutTaskDefReply out = new PutTaskDefReply();

        if (!LHUtil.isValidLHName(name)) {
            out.code = LHResponseCodePb.VALIDATION_ERROR;
            out.message = "TaskDef name must be a valid hostname";
            return out;
        }

        TaskDef spec = new TaskDef();
        spec.name = name;
        spec.outputSchema = outputSchema;
        spec.inputVars = inputVars;

        TaskDef oldVersion = dao.getTaskDef(name, null);
        if (oldVersion != null) {
            spec.version = oldVersion.version + 1;
        } else {
            spec.version = 0;
        }
        switch (type) {
            case KAFKA:
                spec.type = QueueDetailsCase.KAFKA;
                spec.kafkaTaskQueueDetails = new KafkaTaskQueueDetails(spec, config);
                break;
            case RPC:
                spec.type = QueueDetailsCase.RPC;
                break;
            case QUEUETYPE_NOT_SET:
                throw new RuntimeException("not possible");
        }

        // TODO: Check for schema evolution here
        out.code = LHResponseCodePb.OK;
        out.result = spec;

        dao.putTaskDef(spec);
        return out;
    }

    public static PutTaskDef fromProto(PutTaskDefPbOrBuilder p) {
        PutTaskDef out = new PutTaskDef();
        out.initFrom(p);
        return out;
    }
}
