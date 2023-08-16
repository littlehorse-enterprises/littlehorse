package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.VariableValueModel;
import io.littlehorse.sdk.common.proto.EntrypointRunPb;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.Date;

public class EntrypointRun extends SubNodeRun<EntrypointRunPb> {

    public Class<EntrypointRunPb> getProtoBaseClass() {
        return EntrypointRunPb.class;
    }

    public void initFrom(Message p) {}

    public EntrypointRunPb.Builder toProto() {
        return EntrypointRunPb.newBuilder();
    }

    public static EntrypointRun fromProto(EntrypointRunPb p) {
        EntrypointRun out = new EntrypointRun();
        out.initFrom(p);
        return out;
    }

    public boolean advanceIfPossible(Date time) {
        // By definition something changed
        return true;
    }

    public void arrive(Date time) {
        nodeRunModel.setStatus(LHStatus.COMPLETED);
        VariableValueModel result = new VariableValueModel();
        result.setType(VariableType.NULL);
        nodeRunModel.complete(result, time);
    }
}
