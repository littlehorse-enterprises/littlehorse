package io.littlehorse.common.model.getable.global.wfspec.migration;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.MigrationPlan;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MigrationPlanModel extends LHSerializable<MigrationPlan> {

    private Map<String, ThreadMigrationPlanModel> threadMigrations;

    public MigrationPlanModel() {
        threadMigrations = new HashMap<>();
    }

    @Override
    public Class<MigrationPlan> getProtoBaseClass() {
        return MigrationPlan.class;
    }

    @Override
    public MigrationPlan.Builder toProto() {
        MigrationPlan.Builder builder = MigrationPlan.newBuilder();

        for (Map.Entry<String, ThreadMigrationPlanModel> entry : threadMigrations.entrySet()) {
            builder.putThreadMigrations(entry.getKey(), entry.getValue().toProto().build());
        }

        return builder;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        MigrationPlan p = (MigrationPlan) proto;
        
        for (Map.Entry<String, io.littlehorse.sdk.common.proto.ThreadMigrationPlan> entry : 
                p.getThreadMigrationsMap().entrySet()) {
            threadMigrations.put(
                entry.getKey(),
                LHSerializable.fromProto(entry.getValue(), ThreadMigrationPlanModel.class, context)
            );
        }
    }

    public static MigrationPlanModel fromProto(MigrationPlan p, ExecutionContext context) {
        MigrationPlanModel out = new MigrationPlanModel();
        out.initFrom(p, context);
        return out;
    }
}

