package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StartChildWfNodeRun;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

import java.util.Date;
import java.util.Optional;

public class StartChildWfNodeRunModel extends SubNodeRun<StartChildWfNodeRun> {

    public StartChildWfNodeRunModel() {}

    @Override
    public void arrive(Date time, CoreProcessorContext processorContext) throws NodeFailureException {

    }

    @Override
    public Optional<VariableValueModel> getOutput(CoreProcessorContext processorContext) {
        return Optional.empty();
    }

    @Override
    public boolean checkIfProcessingCompleted(CoreProcessorContext processorContext) throws NodeFailureException {
        return false;
    }

    @Override
    public GeneratedMessageV3.Builder<?> toProto() {
        return null;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {

    }

    @Override
    public Class<? extends GeneratedMessageV3> getProtoBaseClass() {
        return null;
    }
}
