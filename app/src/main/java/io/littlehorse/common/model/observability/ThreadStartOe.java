package io.littlehorse.common.model.observability;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.observability.ThreadStartOePb;

public class ThreadStartOe extends LHSerializable<ThreadStartOePb> {
    public int number;
    public String threadSpecName;

    public ThreadStartOePb.Builder toProto() {
        ThreadStartOePb.Builder out = ThreadStartOePb.newBuilder()
            .setNumber(number)
            .setThreadSpecName(threadSpecName);

        return out;
    }

    public Class<ThreadStartOePb> getProtoBaseClass() {
        return ThreadStartOePb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        ThreadStartOePb p = (ThreadStartOePb) proto;
        number = p.getNumber();
        threadSpecName = p.getThreadSpecName();
    }

    public ThreadStartOe() {}

    public static ThreadStartOe fromProto(ThreadStartOePb proto) {
        ThreadStartOe out = new ThreadStartOe();
        out.initFrom(proto);
        return out;
    }

    public ThreadStartOe(int number, String threadSpecName) {
        this.number = number;
        this.threadSpecName = threadSpecName;
    }
}
