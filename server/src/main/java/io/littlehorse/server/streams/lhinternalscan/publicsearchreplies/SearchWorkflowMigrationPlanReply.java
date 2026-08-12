package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.WorkflowMigrationPlanIdModel;
import io.littlehorse.sdk.common.proto.WorkflowMigrationPlanId;
import io.littlehorse.sdk.common.proto.WorkflowMigrationPlanIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchWorkflowMigrationPlanReply
        extends PublicScanReply<WorkflowMigrationPlanIdList, WorkflowMigrationPlanId, WorkflowMigrationPlanIdModel> {

    @Override
    public Class<WorkflowMigrationPlanId> getResultProtoClass() {
        return WorkflowMigrationPlanId.class;
    }

    @Override
    public Class<WorkflowMigrationPlanIdModel> getResultJavaClass() {
        return WorkflowMigrationPlanIdModel.class;
    }

    @Override
    public Class<WorkflowMigrationPlanIdList> getProtoBaseClass() {
        return WorkflowMigrationPlanIdList.class;
    }
}
