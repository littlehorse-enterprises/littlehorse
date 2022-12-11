package io.littlehorse.common.model.meta;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.TaskDefPb;
import io.littlehorse.common.proto.TaskDefPb.QueueDetailsCase;
import io.littlehorse.common.proto.TaskDefPbOrBuilder;
import io.littlehorse.common.proto.VariableDefPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsbackend.storeinternals.utils.StoreUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TaskDef extends GETable<TaskDefPbOrBuilder> {

    public String name;
    public int version;
    public Date createdAt;
    public OutputSchema outputSchema;
    public Map<String, VariableDef> inputVars;

    public QueueDetailsCase type;
    public KafkaTaskQueueDetails kafkaTaskQueueDetails;

    public TaskDef() {
        inputVars = new HashMap<>();
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
        return LHUtil.getCompositeId(name, String.valueOf(version));
    }

    public static String getPrefixByName(String name) {
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

        switch (type) {
            case KAFKA:
                b.setKafka(kafkaTaskQueueDetails.toProto());
                break;
            case RPC:
                b.setRpc(true);
                break;
            case QUEUEDETAILS_NOT_SET:
                throw new RuntimeException("Not possible");
        }

        if (outputSchema != null) {
            b.setOutputSchema(outputSchema.toProto());
        }
        for (Map.Entry<String, VariableDef> entry : inputVars.entrySet()) {
            b.putInputVars(entry.getKey(), entry.getValue().toProto().build());
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

        type = proto.getQueueDetailsCase();
        if (type == QueueDetailsCase.KAFKA) {
            kafkaTaskQueueDetails =
                KafkaTaskQueueDetails.fromProto(proto.getKafkaOrBuilder());
        }
        for (Map.Entry<String, VariableDefPb> entry : proto
            .getInputVarsMap()
            .entrySet()) {
            inputVars.put(entry.getKey(), VariableDef.fromProto(entry.getValue()));
        }
    }

    public static TaskDef fromProto(TaskDefPbOrBuilder p) {
        TaskDef out = new TaskDef();
        out.initFrom(p);
        return out;
    }
}
