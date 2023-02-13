package io.littlehorse.common.model.meta;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.TaskDefPb;
import io.littlehorse.jlib.common.proto.TaskDefPbOrBuilder;
import io.littlehorse.jlib.common.proto.VariableDefPb;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskDef extends GETable<TaskDefPbOrBuilder> {

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

    public String getObjectId() {
        return TaskDef.getSubKey(name, version);
    }

    public static String getSubKey(String name, int version) {
        return LHUtil.getCompositeId(name, LHUtil.toLHDbVersionFormat(version));
    }

    public static String getFullPrefixByName(String name) {
        return StoreUtils.getFullStoreKey(name + "/", TaskDef.class);
    }

    public static String getFullKey(String name, int version) {
        return StoreUtils.getFullStoreKey(getSubKey(name, version), TaskDef.class);
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
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

    public void initFrom(MessageOrBuilder p) {
        TaskDefPbOrBuilder proto = (TaskDefPbOrBuilder) p;
        name = proto.getName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        if (proto.hasOutputSchema()) {
            outputSchema = OutputSchema.fromProto(proto.getOutputSchemaOrBuilder());
        }
        version = proto.getVersion();

        for (VariableDefPb entry : proto.getInputVarsList()) {
            inputVars.add(VariableDef.fromProto(entry));
        }
    }

    public static TaskDef fromProto(TaskDefPbOrBuilder p) {
        TaskDef out = new TaskDef();
        out.initFrom(p);
        return out;
    }
}
