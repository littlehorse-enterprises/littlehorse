package io.littlehorse.common.model.getable.global.migrations;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.NodeMigrationPlan;
import io.littlehorse.sdk.common.proto.ThreadMigrationPlanRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreadMigrationPlanRequestModel extends LHSerializable<ThreadMigrationPlanRequest> {

    private String newThreadName;
    private Map<String, NodeMigrationPlanModel> nodeMigrations;

    public ThreadMigrationPlanRequestModel() {
        nodeMigrations = new HashMap<>();
    }

    @Override
    public ThreadMigrationPlanRequest.Builder toProto() {
        ThreadMigrationPlanRequest.Builder out =
                ThreadMigrationPlanRequest.newBuilder().setNewThreadName(newThreadName);

        for (Map.Entry<String, NodeMigrationPlanModel> entry : nodeMigrations.entrySet()) {
            out.putNodeMigrations(entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ThreadMigrationPlanRequest p = (ThreadMigrationPlanRequest) proto;
        newThreadName = p.getNewThreadName();
        nodeMigrations = new HashMap<>();

        for (Map.Entry<String, NodeMigrationPlan> entry :
                p.getNodeMigrationsMap().entrySet()) {
            nodeMigrations.put(
                    entry.getKey(), LHSerializable.fromProto(entry.getValue(), NodeMigrationPlanModel.class, context));
        }
    }

    /**
     * Builds the internal {@link ThreadMigrationPlanModel} from this request. The
     * required variables and dependencies are intentionally left empty here; they are
     * computed internally by the server.
     */
    public ThreadMigrationPlanModel toThreadMigrationPlan() {
        ThreadMigrationPlanModel out = new ThreadMigrationPlanModel();
        out.setNewThreadName(newThreadName);
        out.setNodeMigrations(new HashMap<>(nodeMigrations));
        return out;
    }

    @Override
    public Class<ThreadMigrationPlanRequest> getProtoBaseClass() {
        return ThreadMigrationPlanRequest.class;
    }
}
