package io.littlehorse.common.model.observability;

import io.littlehorse.common.proto.observability.ThreadStartOePb;

public class ThreadStartOe {
    public int number;
    public String threadSpecName;

    public ThreadStartOePb.Builder toProtoBuilder() {
        ThreadStartOePb.Builder out = ThreadStartOePb.newBuilder()
            .setNumber(number)
            .setThreadSpecName(threadSpecName);

        return out;
    }

    public ThreadStartOe(int number, String threadSpecName) {
        this.number = number;
        this.threadSpecName = threadSpecName;
    }
}
