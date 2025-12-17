package io.littlehorse.common.model.getable.global.wfspec.migration;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.ThreadMigrationPlan;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreadMigrationPlanModel extends LHSerializable<ThreadMigrationPlan> {

    private String newThreadName;
    private Map<String, NodeMigrationPlanModel> nodeMigrations;

    public ThreadMigrationPlanModel() {
        nodeMigrations = new HashMap<>();
    }

    public ThreadMigrationPlanModel(String newThreadName) {
        this();
        this.newThreadName = newThreadName;
    }

    @Override
    public Class<ThreadMigrationPlan> getProtoBaseClass() {
        return ThreadMigrationPlan.class;
    }

    @Override
    public ThreadMigrationPlan.Builder toProto() {
        ThreadMigrationPlan.Builder builder = ThreadMigrationPlan.newBuilder()
                .setNewThreadName(newThreadName);

        for (Map.Entry<String, NodeMigrationPlanModel> entry : nodeMigrations.entrySet()) {
            builder.putNodeMigrations(entry.getKey(), entry.getValue().toProto().build());
        }

        return builder;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ThreadMigrationPlan p = (ThreadMigrationPlan) proto;
        newThreadName = p.getNewThreadName();
        
        for (Map.Entry<String, io.littlehorse.sdk.common.proto.NodeMigrationPlan> entry : 
                p.getNodeMigrationsMap().entrySet()) {
            nodeMigrations.put(
                entry.getKey(),
                LHSerializable.fromProto(entry.getValue(), NodeMigrationPlanModel.class, context)
            );
        }
    }

    public static ThreadMigrationPlanModel fromProto(ThreadMigrationPlan p, ExecutionContext context) {
        ThreadMigrationPlanModel out = new ThreadMigrationPlanModel();
        out.initFrom(p, context);
        return out;
    }
}

