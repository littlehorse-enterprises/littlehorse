package io.littlehorse.common.model.getable.global.migrations;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ThreadMigrationPlan;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreadMigrationPlanModel extends LHSerializable<ThreadMigrationPlan> {

    private String newThreadName;
    private String oldNodeName;
    private String newNodeName;
    private List<String> requiredVariables;

    public ThreadMigrationPlanModel() {
        requiredVariables = new ArrayList<>();
    }

    @Override
    public ThreadMigrationPlan.Builder toProto() {
        return ThreadMigrationPlan.newBuilder()
                .setNewThreadName(newThreadName)
                .setFromNode(oldNodeName)
                .setToNode(newNodeName)
                .addAllRequiredVariables(requiredVariables);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ThreadMigrationPlan p = (ThreadMigrationPlan) proto;
        newThreadName = p.getNewThreadName();
        oldNodeName = p.getFromNode();
        newNodeName = p.getToNode();
        requiredVariables = new ArrayList<>(p.getRequiredVariablesList());
    }

    @Override
    public Class<ThreadMigrationPlan> getProtoBaseClass() {
        return ThreadMigrationPlan.class;
    }
}
