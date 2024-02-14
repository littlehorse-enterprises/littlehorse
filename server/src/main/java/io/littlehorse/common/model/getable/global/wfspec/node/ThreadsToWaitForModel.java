package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode.ThreadToWaitFor;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode.ThreadsToWaitFor;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class ThreadsToWaitForModel extends LHSerializable<ThreadsToWaitFor> {

    private List<ThreadToWaitForModel> threads;

    public ThreadsToWaitForModel() {
        threads = new ArrayList<>();
    }

    public Class<ThreadsToWaitFor> getProtoBaseClass() {
        return ThreadsToWaitFor.class;
    }

    public ThreadsToWaitFor.Builder toProto() {
        ThreadsToWaitFor.Builder out = ThreadsToWaitFor.newBuilder();
        for (ThreadToWaitForModel thread : threads) {
            out.addThreads(thread.toProto());
        }
        return out;
    }

    public void initFrom(Message proto, ExecutionContext ctx) {
        ThreadsToWaitFor p = (ThreadsToWaitFor) proto;
        for (ThreadToWaitFor thread : p.getThreadsList()) {
            threads.add(LHSerializable.fromProto(thread, ThreadToWaitForModel.class, ctx));
        }
    }
}
