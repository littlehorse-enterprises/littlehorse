package io.littlehorse.common.model.getable.global.migrations;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.NodeMigrationPlan;
import io.littlehorse.sdk.common.proto.ThreadMigrationPlan;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreadMigrationPlanModel extends LHSerializable<ThreadMigrationPlan> {

    private String newThreadName;
    private Map<String, NodeMigrationPlanModel> nodeMigrations;
    private List<String> threadSpecDependencies;

    public ThreadMigrationPlanModel() {
        nodeMigrations = new HashMap<>();
        threadSpecDependencies = new ArrayList<>();
    }

    @Override
    public ThreadMigrationPlan.Builder toProto() {
        ThreadMigrationPlan.Builder out = ThreadMigrationPlan.newBuilder()
                .setNewThreadName(newThreadName)
                .addAllThreadSpecDependencies(threadSpecDependencies);

        for (Map.Entry<String, NodeMigrationPlanModel> entry : nodeMigrations.entrySet()) {
            out.putNodeMigrations(entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ThreadMigrationPlan p = (ThreadMigrationPlan) proto;
        newThreadName = p.getNewThreadName();
        threadSpecDependencies = new ArrayList<>(p.getThreadSpecDependenciesList());
        nodeMigrations = new HashMap<>();

        for (Map.Entry<String, NodeMigrationPlan> entry :
                p.getNodeMigrationsMap().entrySet()) {
            nodeMigrations.put(
                    entry.getKey(), LHSerializable.fromProto(entry.getValue(), NodeMigrationPlanModel.class, context));
        }
    }

    @Override
    public Class<ThreadMigrationPlan> getProtoBaseClass() {
        return ThreadMigrationPlan.class;
    }
}
