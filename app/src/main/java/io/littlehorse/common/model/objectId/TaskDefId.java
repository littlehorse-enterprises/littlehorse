package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.TaskDefIdPb;
import io.littlehorse.jlib.common.proto.TaskDefPb;

// Used by TaskDef, TaskDef, and ExternalEventDef
public class TaskDefId extends ObjectId<TaskDefIdPb, TaskDefPb, TaskDef> {

    public String name;
    public int version;

    public TaskDefId() {}

    public TaskDefId(String name, int version) {
        this.name = name;
        this.version = version;
    }

    public Class<TaskDefIdPb> getProtoBaseClass() {
        return TaskDefIdPb.class;
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public void initFrom(Message proto) {
        TaskDefIdPb p = (TaskDefIdPb) proto;
        version = p.getVersion();
        name = p.getName();
    }

    public TaskDefIdPb.Builder toProto() {
        TaskDefIdPb.Builder out = TaskDefIdPb
            .newBuilder()
            .setVersion(version)
            .setName(name);
        return out;
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(name, LHUtil.toLHDbVersionFormat(version));
    }

    public void initFrom(String storeKey) {
        String[] split = storeKey.split("/");
        name = split[0];
        version = Integer.valueOf(split[1]);
    }

    public GETableClassEnumPb getType() {
        return GETableClassEnumPb.WF_SPEC;
    }
}
