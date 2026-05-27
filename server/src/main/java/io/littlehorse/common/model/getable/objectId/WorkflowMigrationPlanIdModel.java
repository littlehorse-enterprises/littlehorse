package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;

import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.migrations.WorkflowMigrationPlanModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.WorkflowMigrationPlan;
import io.littlehorse.sdk.common.proto.WorkflowMigrationPlanId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowMigrationPlanIdModel
        extends MetadataId<WorkflowMigrationPlanId, WorkflowMigrationPlan, WorkflowMigrationPlanModel> {

    private String name;

    public WorkflowMigrationPlanIdModel() {}

    public WorkflowMigrationPlanIdModel(String name) {
        this.name = name;
    }

    @Override
    public WorkflowMigrationPlanId.Builder toProto() {
        return WorkflowMigrationPlanId.newBuilder().setName(name);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        WorkflowMigrationPlanId p = (WorkflowMigrationPlanId) proto;
        name = p.getName();
    }

    @Override
    public Class<WorkflowMigrationPlanId> getProtoBaseClass() {
        return WorkflowMigrationPlanId.class;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void initFromString(String storeKey) {
        name = storeKey;
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.WORKFLOW_MIGRATION_PLAN;
    }

}