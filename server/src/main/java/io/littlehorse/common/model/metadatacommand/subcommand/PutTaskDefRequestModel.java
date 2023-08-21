package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.command.subcommandresponse.PutTaskDefResponseModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.VariableDef;
import java.util.ArrayList;
import java.util.List;

public class PutTaskDefRequestModel extends MetadataSubCommand<PutTaskDefRequest> {

    public String name;
    public List<VariableDefModel> inputVars;

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public PutTaskDefRequestModel() {
        inputVars = new ArrayList<>();
    }

    public Class<PutTaskDefRequest> getProtoBaseClass() {
        return PutTaskDefRequest.class;
    }

    public PutTaskDefRequest.Builder toProto() {
        PutTaskDefRequest.Builder out = PutTaskDefRequest.newBuilder();
        out.setName(name);

        for (VariableDefModel entry : inputVars) {
            out.addInputVars(entry.toProto());
        }

        return out;
    }

    public void initFrom(Message proto) {
        PutTaskDefRequest p = (PutTaskDefRequest) proto;
        name = p.getName();
        for (VariableDef entry : p.getInputVarsList()) {
            inputVars.add(VariableDefModel.fromProto(entry));
        }
    }

    public boolean hasResponse() {
        return true;
    }

    public PutTaskDefResponseModel process(MetadataProcessorDAO dao, LHConfig config) {
        PutTaskDefResponseModel out = new PutTaskDefResponseModel();

        if (!LHUtil.isValidLHName(name)) {
            out.code = LHResponseCode.VALIDATION_ERROR;
            out.message = "TaskDef name must be a valid hostname";
            return out;
        }

        TaskDefModel oldVersion = dao.getTaskDef(name);
        if (oldVersion != null) {
            out.code = LHResponseCode.ALREADY_EXISTS_ERROR;
            out.message = "TaskDef already exists and is immutable.";
            out.result = oldVersion;
        } else {
            TaskDefModel spec = new TaskDefModel();
            spec.name = name;
            spec.inputVars = inputVars;
            dao.put(spec);

            out.code = LHResponseCode.OK;
            out.result = spec;
        }

        return out;
    }

    public static PutTaskDefRequestModel fromProto(PutTaskDefRequest p) {
        PutTaskDefRequestModel out = new PutTaskDefRequestModel();
        out.initFrom(p);
        return out;
    }
}
