package io.littlehorse.common.model.getable.global.migrations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
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
    private List<String> requiredVariables;
    private List<String> dependencies;

    public ThreadMigrationPlanModel() {
        nodeMigrations = new HashMap<>();
        requiredVariables = new ArrayList<>();
        dependencies = new ArrayList<>();
    }

    @Override
    public ThreadMigrationPlan.Builder toProto() {
        ThreadMigrationPlan.Builder out = ThreadMigrationPlan.newBuilder()
                .setNewThreadName(newThreadName)
                .addAllRequiredVariables(requiredVariables)
                .addAllDependencies(dependencies);

        for (Map.Entry<String, NodeMigrationPlanModel> entry : nodeMigrations.entrySet()) {
            out.putNodeMigrations(entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ThreadMigrationPlan p = (ThreadMigrationPlan) proto;
        newThreadName = p.getNewThreadName();
        requiredVariables = new ArrayList<>(p.getRequiredVariablesList());
        dependencies = new ArrayList<>(p.getDependenciesList());
        nodeMigrations = new HashMap<>();

        for (Map.Entry<String, NodeMigrationPlan> entry : p.getNodeMigrationsMap().entrySet()) {
            nodeMigrations.put(
                    entry.getKey(),
                    LHSerializable.fromProto(entry.getValue(), NodeMigrationPlanModel.class, context));
        }
    }

    @Override
    public Class<ThreadMigrationPlan> getProtoBaseClass() {
        return ThreadMigrationPlan.class;
    }
}
