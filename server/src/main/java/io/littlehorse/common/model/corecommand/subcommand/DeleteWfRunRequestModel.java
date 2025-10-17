package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ExternalEventNodeRunModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.List;

public class DeleteWfRunRequestModel extends CoreSubCommand<DeleteWfRunRequest> {

    public WfRunIdModel wfRunId;

    public Class<DeleteWfRunRequest> getProtoBaseClass() {
        return DeleteWfRunRequest.class;
    }

    public DeleteWfRunRequest.Builder toProto() {
        DeleteWfRunRequest.Builder out = DeleteWfRunRequest.newBuilder().setId(wfRunId.toProto());
        return out;
    }

    public void initFrom(Message proto, ExecutionContext context) {
        DeleteWfRunRequest p = (DeleteWfRunRequest) proto;
        wfRunId = LHSerializable.fromProto(p.getId(), WfRunIdModel.class, context);
    }

    public String getPartitionKey() {
        return wfRunId.getPartitionKey().get();
    }

    @Override
    public Empty process(CoreProcessorContext executionContext, LHServerConfig config) {
        GetableManager manager = executionContext.getableManager();
        WfRunModel wfRun = manager.get(wfRunId);
        if (wfRun == null) return Empty.getDefaultInstance();

        // We use our brains here to delete things we know are there rather than using a range scan.
        // Better to rely on our brainpower rather than RocksDB, since RocksDB gets grumpy when we ask
        // it to do work.
        for (ThreadRunModel thread : wfRun.getThreadRunsUseMeCarefully()) {
            for (int i = 0; i <= thread.getCurrentNodePosition(); i++) {
                NodeRunModel nodeRun = thread.getNodeRun(i);

                // Delete things created by the NodeRun, eg TaskRun / UserTaskRun / WorkflowEvent
                List<? extends CoreObjectId<?, ?, ?>> createdGetables =
                        nodeRun.getSubNodeRun().getCreatedSubGetableIds(executionContext);

                for (CoreObjectId<?, ?, ?> createdGetable : createdGetables) {
                    manager.delete((CoreObjectId<?, ?, ?>) createdGetable);
                }

                // ExternalEventNodeRun's can create correlation markers. Deleting those are tricky.
                if (nodeRun.getExternalEventRun() != null) {
                    ExternalEventNodeRunModel extEvtNr = nodeRun.getExternalEventRun();
                    if (extEvtNr.getCorrelationKey() != null) {
                        // If we delete the WfRun, we should remove the WfRun from the correlation marker.
                        extEvtNr.sendRemoveCorrelationMarkerCommand(executionContext);
                    }
                }

                // Delete the NodeRun
                manager.delete(nodeRun.getObjectId());
            }

            // Delete the variables belonging to that ThreadRun
            ThreadSpecModel threadSpec = thread.getThreadSpec();
            for (ThreadVarDefModel varDef : threadSpec.getVariableDefs()) {
                if (varDef.getAccessLevel() == WfRunVariableAccessLevel.INHERITED_VAR) continue;

                VariableIdModel id = new VariableIdModel(
                        wfRunId, thread.getNumber(), varDef.getVarDef().getName());
                manager.delete(id);
            }
        }

        // Delete the ExternalEvents, which can be done by the GetableManager itself
        manager.deleteAllExternalEventsFor(wfRunId);

        // Now we delete the WfRun itself
        executionContext.getableManager().delete(wfRunId);
        return Empty.getDefaultInstance();
    }

    public static DeleteWfRunRequestModel fromProto(DeleteWfRunRequest p, ExecutionContext context) {
        DeleteWfRunRequestModel out = new DeleteWfRunRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
