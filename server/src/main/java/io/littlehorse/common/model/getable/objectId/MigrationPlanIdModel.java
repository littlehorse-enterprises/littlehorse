package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.wfspec.migration.WfRunMigrationPlanModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.MigrationPlanId;
import io.littlehorse.sdk.common.proto.WfRunMigrationPlan;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MigrationPlanIdModel extends MetadataId<MigrationPlanId, WfRunMigrationPlan, WfRunMigrationPlanModel> {

    private String name;

    public MigrationPlanIdModel() {}

    public MigrationPlanIdModel(String name) {
        this.name = name;
    }

    @Override
    public Class<MigrationPlanId> getProtoBaseClass() {
        return MigrationPlanId.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        MigrationPlanId p = (MigrationPlanId) proto;
        name = p.getName();
    }

    @Override
    public MigrationPlanId.Builder toProto() {
        return MigrationPlanId.newBuilder()
                .setName(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void initFromString(String storeKey) {
        // For MigrationPlan, the store key is just the name
        this.name = storeKey;
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.MIGRATION_PLAN;
    }
}

