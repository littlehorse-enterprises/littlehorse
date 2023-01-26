package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.PutTaskDefReply;
import io.littlehorse.common.model.meta.OutputSchema;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.PutTaskDefPb;
import io.littlehorse.common.proto.PutTaskDefPbOrBuilder;
import io.littlehorse.common.proto.VariableDefPb;
import io.littlehorse.common.util.LHUtil;
import java.util.ArrayList;
import java.util.List;

public class PutTaskDef extends SubCommand<PutTaskDefPb> {

    public String name;
    public OutputSchema outputSchema;
    public List<VariableDef> inputVars;

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public PutTaskDef() {
        inputVars = new ArrayList<>();
    }

    public Class<PutTaskDefPb> getProtoBaseClass() {
        return PutTaskDefPb.class;
    }

    public PutTaskDefPb.Builder toProto() {
        PutTaskDefPb.Builder out = PutTaskDefPb.newBuilder();
        out.setName(name);
        if (outputSchema != null) out.setOutputSchema(outputSchema.toProto());

        for (VariableDef entry : inputVars) {
            out.addInputVars(entry.toProto());
        }

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        PutTaskDefPbOrBuilder p = (PutTaskDefPbOrBuilder) proto;
        name = p.getName();
        for (VariableDefPb entry : p.getInputVarsList()) {
            inputVars.add(VariableDef.fromProto(entry));
        }
        if (p.hasOutputSchema()) {
            outputSchema = OutputSchema.fromProto(p.getOutputSchema());
        }
    }

    public boolean hasResponse() {
        return true;
    }

    public PutTaskDefReply process(LHDAO dao, LHConfig config) {
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
