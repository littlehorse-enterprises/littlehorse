package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;

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
    public Empty process(ProcessorExecutionContext executionContext, LHServerConfig config) {
        executionContext.getableManager().delete(wfRunId);
        executionContext.getableManager().deleteAllByPrefix(getPartitionKey(), TaskRunModel.class);
        executionContext.getableManager().deleteAllByPrefix(getPartitionKey(), VariableModel.class);
        executionContext.getableManager().deleteAllByPrefix(getPartitionKey(), ExternalEventModel.class);
        executionContext.getableManager().deleteAllByPrefix(getPartitionKey(), UserTaskRunModel.class);
        executionContext.getableManager().deleteAllByPrefix(getPartitionKey(), WorkflowEventModel.class);
        executionContext.getableManager().deleteAllByPrefix(getPartitionKey(), NodeRunModel.class);

        return Empty.getDefaultInstance();
    }

    public boolean hasResponse() {
        return true;
    }

    public static DeleteWfRunRequestModel fromProto(DeleteWfRunRequest p, ExecutionContext context) {
        DeleteWfRunRequestModel out = new DeleteWfRunRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
