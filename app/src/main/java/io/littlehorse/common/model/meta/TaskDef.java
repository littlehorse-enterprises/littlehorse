package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.objectId.TaskDefId;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.TaskDefIdPb;
import io.littlehorse.jlib.common.proto.TaskDefPb;
import io.littlehorse.jlib.common.proto.VariableDefPb;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskDef extends GETable<TaskDefPb> {

    public String name;
    public int version;
    public Date createdAt;
    public OutputSchema outputSchema;
    public List<VariableDef> inputVars;

    public TaskDef() {
        inputVars = new ArrayList<>();
    }

    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public TaskDefId getObjectId() {
        return new TaskDefId(name, version);
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
            .setCreatedAt(LHUtil.fromDate(getCreatedAt()))
            .setVersion(version);

        if (outputSchema != null) {
            b.setOutputSchema(outputSchema.toProto());
        }
        for (VariableDef entry : inputVars) {
            b.addInputVars(entry.toProto());
        }

        return b;
    }

    public void initFrom(Message p) {
        TaskDefPb proto = (TaskDefPb) p;
        name = proto.getName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        if (proto.hasOutputSchema()) {
            outputSchema = OutputSchema.fromProto(proto.getOutputSchema());
        }
        version = proto.getVersion();

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
        String[] split = fullId.split("/");
        return TaskDefIdPb
            .newBuilder()
            .setName(split[0])
            .setVersion(Integer.valueOf(split[1]))
            .build();
    }
}
