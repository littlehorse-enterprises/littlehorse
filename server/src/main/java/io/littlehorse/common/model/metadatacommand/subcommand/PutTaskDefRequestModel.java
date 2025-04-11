package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.common.util.TaskDefUtil;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class PutTaskDefRequestModel extends MetadataSubCommand<PutTaskDefRequest> {

    public String name;
    public List<VariableDefModel> inputVars;

    public ReturnTypeModel returnType;

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
        PutTaskDefRequest.Builder out =
                PutTaskDefRequest.newBuilder().setName(name).setReturnType(returnType.toProto());

        for (VariableDefModel entry : inputVars) {
            out.addInputVars(entry.toProto());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PutTaskDefRequest p = (PutTaskDefRequest) proto;
        name = p.getName();
        returnType = LHSerializable.fromProto(p.getReturnType(), ReturnTypeModel.class, context);
        for (VariableDef entry : p.getInputVarsList()) {
            inputVars.add(VariableDefModel.fromProto(entry, context));
        }
    }

    public boolean hasResponse() {
        return true;
    }

    public TaskDef process(MetadataCommandExecution context) {
        MetadataManager metadataManager = context.metadataManager();
        if (!LHUtil.isValidLHName(name)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "TaskDefName must be a valid hostname");
        }

        TaskDefModel spec = new TaskDefModel();
        spec.setId(new TaskDefIdModel(name));
        spec.inputVars = inputVars;
        spec.setReturnType(returnType);

        TaskDefModel oldVersion = metadataManager.get(new TaskDefIdModel(name));
        if (oldVersion != null) {
            if (TaskDefUtil.equals(spec, oldVersion))
                return oldVersion.toProto().build();
            throw new LHApiException(
                    Status.ALREADY_EXISTS,
                    MessageFormat.format("TaskDef [{0}] already exists and is immutable.", name));
        }

        metadataManager.put(spec);
        return spec.toProto().build();
    }

    public static PutTaskDefRequestModel fromProto(PutTaskDefRequest p, ExecutionContext context) {
        PutTaskDefRequestModel out = new PutTaskDefRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
