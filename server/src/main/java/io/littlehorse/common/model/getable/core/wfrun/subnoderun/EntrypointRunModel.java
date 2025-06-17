package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.sdk.common.proto.EntrypointRun;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;

@Getter
public class EntrypointRunModel extends SubNodeRun<EntrypointRun> {

    private CoreProcessorContext context;

    @Override
    public Class<EntrypointRun> getProtoBaseClass() {
        return EntrypointRun.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {}

    @Override
    public EntrypointRun.Builder toProto() {
        return EntrypointRun.newBuilder();
    }

    @Override
    public Optional<VariableValueModel> getOutput(CoreProcessorContext processorContext) {
        return Optional.empty();
    }

    @Override
    public boolean checkIfProcessingCompleted(CoreProcessorContext processorContext) throws NodeFailureException {
        return true;
    }

    @Override
    public void arrive(Date time, CoreProcessorContext processorContext) throws NodeFailureException {}

    public static EntrypointRunModel fromProto(EntrypointRun p, ExecutionContext context) {
        EntrypointRunModel out = new EntrypointRunModel();
        out.initFrom(p, context);
        return out;
    }
}
