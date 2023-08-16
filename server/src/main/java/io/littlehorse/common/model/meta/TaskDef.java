package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.objectId.TaskDefId;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.TaskDefIdPb;
import io.littlehorse.sdk.common.proto.TaskDefPb;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDef extends Getable<TaskDefPb> {

    @Getter
    public String name;

    public Date createdAt;
    public List<VariableDefModel> inputVars;

    public TaskDef() {
        inputVars = new ArrayList<>();
    }

    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends Getable<?>>> getIndexConfigurations() {
        return List.of();
    }

    public TaskDefId getObjectId() {
        return new TaskDefId(name);
    }

    @Override
    public List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageTypePb> tagStorageTypePb
    ) {
        return List.of();
    }

    public Class<TaskDefPb> getProtoBaseClass() {
        return TaskDefPb.class;
    }

    public TaskDefPb.Builder toProto() {
        TaskDefPb.Builder b = TaskDefPb
            .newBuilder()
            .setName(name)
            .setCreatedAt(LHUtil.fromDate(getCreatedAt()));
        for (VariableDefModel entry : inputVars) {
            b.addInputVars(entry.toProto());
        }

        return b;
    }

    public void initFrom(Message p) {
        TaskDefPb proto = (TaskDefPb) p;
        name = proto.getName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());

        for (VariableDef entry : proto.getInputVarsList()) {
            inputVars.add(VariableDefModel.fromProto(entry));
        }
    }

    public static TaskDef fromProto(TaskDefPb p) {
        TaskDef out = new TaskDef();
        out.initFrom(p);
        return out;
    }

    public static TaskDefIdPb parseId(String fullId) {
        return TaskDefIdPb.newBuilder().setName(fullId).build();
    }
}
