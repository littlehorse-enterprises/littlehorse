package io.littlehorse.common.model.getable.global.wfspec.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.NodeMigrationPlan;
import io.littlehorse.sdk.common.proto.ThreadMigrationPlan;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreadMigrationPlanModel extends LHSerializable<ThreadMigrationPlan> {

    private String newThreadName;
    private Map<String, NodeMigrationPlanModel> nodeMigrations;
    private List<String> requiredMigrationVars;

    public ThreadMigrationPlanModel() {
        nodeMigrations = new HashMap<>();
        requiredMigrationVars = new ArrayList<>();
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

        for(String varName: requiredMigrationVars){
            builder.addRequiredMigrationVars(varName);
        }
        
        return builder;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ThreadMigrationPlan p = (ThreadMigrationPlan) proto;
        newThreadName = p.getNewThreadName();
        
        for (Map.Entry<String, NodeMigrationPlan> entry : 
                p.getNodeMigrationsMap().entrySet()) {
            nodeMigrations.put(
                entry.getKey(),
                LHSerializable.fromProto(entry.getValue(), NodeMigrationPlanModel.class, context)
            );
        }

        for (String var : p.getRequiredMigrationVarsList()) {
            requiredMigrationVars.add(var);
        }
    }

    public static ThreadMigrationPlanModel fromProto(ThreadMigrationPlan p, ExecutionContext context) {
        ThreadMigrationPlanModel out = new ThreadMigrationPlanModel();
        out.initFrom(p, context);
        return out;
    }
}

