package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.jlib.common.proto.EntrypointRunPb;
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
        nodeRun.threadRun.advanceFrom(nodeRun.getNode());
        // By definition something changed
        return true;
    }

    public void arrive(Date time) {
        // Don't believe anything to do
    }
}
