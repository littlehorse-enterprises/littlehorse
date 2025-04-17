package io.littlehorse.common.model.getable.global.taskdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.TaskDefOutputSchema;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

public class TaskDefOutputSchemaModel extends LHSerializable<TaskDefOutputSchema> {

    @Getter
    private VariableDefModel valueDef;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        TaskDefOutputSchema p = (TaskDefOutputSchema) proto;
        valueDef = LHSerializable.fromProto(p.getValueDef(), VariableDefModel.class, context);
    }

    @Override
    public TaskDefOutputSchema.Builder toProto() {
        return TaskDefOutputSchema.newBuilder().setValueDef(valueDef.toProto());
    }

    @Override
    public Class<TaskDefOutputSchema> getProtoBaseClass() {
        return TaskDefOutputSchema.class;
    }
}
