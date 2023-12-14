package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.sdk.common.proto.EntrypointRun;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class EntrypointRunModel extends SubNodeRun<EntrypointRun> {

    public Class<EntrypointRun> getProtoBaseClass() {
        return EntrypointRun.class;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {}

    public EntrypointRun.Builder toProto() {
        return EntrypointRun.newBuilder();
    }

    public static EntrypointRunModel fromProto(EntrypointRun p, ExecutionContext context) {
        EntrypointRunModel out = new EntrypointRunModel();
        out.initFrom(p, context);
        return out;
    }

    public boolean advanceIfPossible(Date time) {
        // By definition something changed
        return true;
    }

    public void arrive(Date time) {
        nodeRun.setStatus(LHStatus.COMPLETED);
        VariableValueModel result = new VariableValueModel();
        nodeRun.complete(result, time);
    }
}
