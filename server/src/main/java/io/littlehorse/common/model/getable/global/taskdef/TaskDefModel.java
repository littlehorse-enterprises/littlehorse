package io.littlehorse.common.model.getable.global.taskdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.MetadataGetable;
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
public class TaskDefModel extends MetadataGetable<TaskDef> {

    @Setter
    private TaskDefIdModel id;

    public Date createdAt;
    public List<VariableDefModel> inputVars;

    public TaskDefModel() {
        inputVars = new ArrayList<>();
    }

    @Override
    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    @Override
    public TaskDefIdModel getObjectId() {
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    @Override
    public Class<TaskDef> getProtoBaseClass() {
        return TaskDef.class;
    }

    @Override
    public TaskDef.Builder toProto() {
        TaskDef.Builder b = TaskDef.newBuilder().setId(id.toProto()).setCreatedAt(LHUtil.fromDate(getCreatedAt()));
        for (VariableDefModel entry : inputVars) {
            b.addInputVars(entry.toProto());
        }

        return b;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {
        TaskDef proto = (TaskDef) p;
        id = LHSerializable.fromProto(proto.getId(), TaskDefIdModel.class, context);
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());

        for (VariableDef entry : proto.getInputVarsList()) {
            inputVars.add(VariableDefModel.fromProto(entry, context));
        }
    }

    public String getName() {
        return id.getName();
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
