package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.PutTaskDefResponseModel;
import io.littlehorse.common.model.meta.TaskDefModel;
import io.littlehorse.common.model.meta.VariableDefModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.VariableDef;
import java.util.ArrayList;
import java.util.List;

public class PutTaskDefRequestModel
        extends SubCommand<io.littlehorse.sdk.common.proto.PutTaskDefRequest> {

    public String name;
    public List<VariableDefModel> inputVars;

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public PutTaskDefRequestModel() {
        inputVars = new ArrayList<>();
    }

    public Class<io.littlehorse.sdk.common.proto.PutTaskDefRequest> getProtoBaseClass() {
        return io.littlehorse.sdk.common.proto.PutTaskDefRequest.class;
    }

    public io.littlehorse.sdk.common.proto.PutTaskDefRequest.Builder toProto() {
        io.littlehorse.sdk.common.proto.PutTaskDefRequest.Builder out =
                io.littlehorse.sdk.common.proto.PutTaskDefRequest.newBuilder();
        out.setName(name);

        for (VariableDefModel entry : inputVars) {
            out.addInputVars(entry.toProto());
        }

        return out;
    }

    public void initFrom(Message proto) {
        io.littlehorse.sdk.common.proto.PutTaskDefRequest p =
                (io.littlehorse.sdk.common.proto.PutTaskDefRequest) proto;
        name = p.getName();
        for (VariableDef entry : p.getInputVarsList()) {
            inputVars.add(VariableDefModel.fromProto(entry));
        }
    }

    public boolean hasResponse() {
        return true;
    }

    public PutTaskDefResponseModel process(LHDAO dao, LHConfig config) {
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
            dao.putTaskDef(spec);

            out.code = LHResponseCode.OK;
            out.result = spec;
        }

        return out;
    }

    public static PutTaskDefRequestModel fromProto(
            io.littlehorse.sdk.common.proto.PutTaskDefRequest p) {
        PutTaskDefRequestModel out = new PutTaskDefRequestModel();
        out.initFrom(p);
        return out;
    }
}
