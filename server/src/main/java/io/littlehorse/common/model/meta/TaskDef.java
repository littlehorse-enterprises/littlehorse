package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.objectId.TaskDefId;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.TaskDefIdPb;
import io.littlehorse.jlib.common.proto.TaskDefPb;
import io.littlehorse.jlib.common.proto.VariableDefPb;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskDef extends Getable<TaskDefPb> {

    public String name;
    public Date createdAt;
    public List<VariableDef> inputVars;

    public TaskDef() {
        inputVars = new ArrayList<>();
    }

    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    @Override
    public List<GetableIndex> getIndexes() {
        return new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public TaskDefId getObjectId() {
        return new TaskDefId(name);
    }

    public static String getFullPrefixByName(String name) {
        return StoreUtils.getFullStoreKey(name + "/", TaskDef.class);
    }

    public Class<TaskDefPb> getProtoBaseClass() {
        return TaskDefPb.class;
    }

    public TaskDefPb.Builder toProto() {
        TaskDefPb.Builder b = TaskDefPb
            .newBuilder()
            .setName(name)
            .setCreatedAt(LHUtil.fromDate(getCreatedAt()));
        for (VariableDef entry : inputVars) {
            b.addInputVars(entry.toProto());
        }

        return b;
    }

    public void initFrom(Message p) {
        TaskDefPb proto = (TaskDefPb) p;
        name = proto.getName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());

        for (VariableDefPb entry : proto.getInputVarsList()) {
            inputVars.add(VariableDef.fromProto(entry));
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
