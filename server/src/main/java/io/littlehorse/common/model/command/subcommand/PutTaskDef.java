package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.PutTaskDefReply;
import io.littlehorse.common.model.meta.TaskDefModel;
import io.littlehorse.common.model.meta.VariableDefModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.PutTaskDefPb;
import io.littlehorse.sdk.common.proto.VariableDef;
import java.util.ArrayList;
import java.util.List;

public class PutTaskDef extends SubCommand<PutTaskDefPb> {

    public String name;
    public List<VariableDefModel> inputVars;

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

        for (VariableDefModel entry : inputVars) {
            out.addInputVars(entry.toProto());
        }

        return out;
    }

    public void initFrom(Message proto) {
        PutTaskDefPb p = (PutTaskDefPb) proto;
        name = p.getName();
        for (VariableDef entry : p.getInputVarsList()) {
            inputVars.add(VariableDefModel.fromProto(entry));
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

        TaskDefModel oldVersion = dao.getTaskDef(name);
        if (oldVersion != null) {
            out.code = LHResponseCodePb.ALREADY_EXISTS_ERROR;
            out.message = "TaskDef already exists and is immutable.";
            out.result = oldVersion;
        } else {
            TaskDefModel spec = new TaskDefModel();
            spec.name = name;
            spec.inputVars = inputVars;
            dao.putTaskDef(spec);

            out.code = LHResponseCodePb.OK;
            out.result = spec;
        }

        return out;
    }

    public static PutTaskDef fromProto(PutTaskDefPb p) {
        PutTaskDef out = new PutTaskDef();
        out.initFrom(p);
        return out;
    }
}
