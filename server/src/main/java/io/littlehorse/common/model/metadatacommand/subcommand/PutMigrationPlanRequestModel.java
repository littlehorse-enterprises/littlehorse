package io.littlehorse.common.model.metadatacommand.subcommand;

import java.util.Map;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.migration.NodeMigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.migration.ThreadMigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.migration.WfRunMigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.PutMigrationPlanRequest;
import io.littlehorse.sdk.common.proto.ThreadMigrationPlan;
import io.littlehorse.sdk.common.proto.WfRunMigrationPlan;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutMigrationPlanRequestModel extends MetadataSubCommand<PutMigrationPlanRequest> {

    private String name;
    private Map<String,ThreadMigrationPlanModel> threadMigrationPlans;
    private WfSpecIdModel newWfSpecId;

    @Override
    public Class<PutMigrationPlanRequest> getProtoBaseClass() {
        return PutMigrationPlanRequest.class;
    }

    @Override
    public PutMigrationPlanRequest.Builder toProto() {
        PutMigrationPlanRequest.Builder builder = PutMigrationPlanRequest.newBuilder()
                .setName(name)
                .setNewWfSpec(newWfSpecId.toProto().build());
        for(Map.Entry<String,ThreadMigrationPlanModel> e: threadMigrationPlans.entrySet()){
            builder.putMigrationPlan(e.getKey(), e.getValue().toProto().build());
        }
        return builder;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PutMigrationPlanRequest p = (PutMigrationPlanRequest) proto;
        name = p.getName();
        newWfSpecId = LHSerializable.fromProto(p.getNewWfSpec(), WfSpecIdModel.class, context);
        for(Map.Entry<String,ThreadMigrationPlan> e: p.getMigrationPlanMap().entrySet()){
            threadMigrationPlans.put(e.getKey(), LHSerializable.fromProto(e.getValue(), ThreadMigrationPlanModel.class, context));
        }
    }

    @Override
    public WfRunMigrationPlan process(MetadataProcessorContext context) {
        MetadataManager metadataManager = context.metadataManager();

        WfSpecModel newWfSpec = context.service().getWfSpec(newWfSpecId);
        if (newWfSpec == null) {
            throw new LHApiException(
                    Status.NOT_FOUND, "Migration plan refers to nonexisting WfSpec %s".formatted(newWfSpecId));
        }
        validateThreadMigrations(newWfSpec);

        WfRunMigrationPlanModel spec = new WfRunMigrationPlanModel(name, threadMigrationPlans, newWfSpecId);
        metadataManager.put(spec);
        System.out.println(spec);
        return spec.toProto().build();
    }

    private void validateThreadMigrations(WfSpecModel newWfSpec) {
        if (threadMigrationPlans == null || threadMigrationPlans.isEmpty()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Migration plan must include thread migrations");
        }

        for (Map.Entry<String, ThreadMigrationPlanModel> entry : threadMigrationPlans.entrySet()) {
            String oldThreadName = entry.getKey();
            ThreadMigrationPlanModel threadPlan = entry.getValue();
            String newThreadName = threadPlan.getNewThreadName();

            if (!newWfSpec.getThreadSpecs().containsKey(newThreadName)) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Thread migration for %s refers to missing thread %s in new WfSpec".formatted(
                                oldThreadName, newThreadName));
            }

            ThreadSpecModel newThreadSpec = newWfSpec.getThreadSpecs().get(newThreadName);
            if (threadPlan.getNodeMigrations() == null || threadPlan.getNodeMigrations().isEmpty()) {
                continue;
            }

            for (Map.Entry<String, NodeMigrationPlanModel> nodeEntry :
                    threadPlan.getNodeMigrations().entrySet()) {
                String oldNodeName = nodeEntry.getKey();
                String newNodeName = nodeEntry.getValue().getNewNode();
                if (!newThreadSpec.getNodes().containsKey(newNodeName)) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            "Node migration for %s/%s refers to missing node %s in new thread %s".formatted(
                                    oldThreadName, oldNodeName, newNodeName, newThreadName));
                }
            }
        }
    }

    public static PutMigrationPlanRequestModel fromProto(PutMigrationPlanRequest p, ExecutionContext context) {
        PutMigrationPlanRequestModel out = new PutMigrationPlanRequestModel();
        out.initFrom(p, context);
        return out;
    }
}

