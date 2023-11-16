package io.littlehorse.common.model.getable.global.taskdef;

import com.google.protobuf.Message;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDefModel extends GlobalGetable<TaskDef> {

    @Getter
    public String name;

    public Date createdAt;
    public List<VariableDefModel> inputVars;

    public TaskDefModel() {
        inputVars = new ArrayList<>();
    }

    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    public TaskDefIdModel getObjectId() {
        return new TaskDefIdModel(name);
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    public Class<TaskDef> getProtoBaseClass() {
        return TaskDef.class;
    }

    public TaskDef.Builder toProto() {
        TaskDef.Builder b = TaskDef.newBuilder().setName(name).setCreatedAt(LHUtil.fromDate(getCreatedAt()));
        for (VariableDefModel entry : inputVars) {
            b.addInputVars(entry.toProto());
        }

        return b;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {
        TaskDef proto = (TaskDef) p;
        name = proto.getName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());

        for (VariableDef entry : proto.getInputVarsList()) {
            inputVars.add(VariableDefModel.fromProto(entry, context));
        }
    }

    public static TaskDefModel fromProto(TaskDef p, ExecutionContext context) {
        TaskDefModel out = new TaskDefModel();
        out.initFrom(p, context);
        return out;
    }

    public static TaskDefId parseId(String fullId) {
        return TaskDefId.newBuilder().setName(fullId).build();
    }
}
