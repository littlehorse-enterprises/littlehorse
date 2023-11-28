package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.sdk.common.proto.EntrypointRun;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.Date;

public class EntrypointRunModel extends SubNodeRun<EntrypointRun> {

    public Class<EntrypointRun> getProtoBaseClass() {
        return EntrypointRun.class;
    }

    public void initFrom(Message p) {}

    public EntrypointRun.Builder toProto() {
        return EntrypointRun.newBuilder();
    }

    public static EntrypointRunModel fromProto(EntrypointRun p) {
        EntrypointRunModel out = new EntrypointRunModel();
        out.initFrom(p);
        return out;
    }

    public boolean advanceIfPossible(Date time) {
        // By definition something changed
        return true;
    }

    public void arrive(Date time) {
        nodeRun.setStatus(LHStatus.COMPLETED);
        VariableValueModel result = new VariableValueModel();
        result.setType(VariableType.NULL);
        nodeRun.complete(result, time);
    }
}
