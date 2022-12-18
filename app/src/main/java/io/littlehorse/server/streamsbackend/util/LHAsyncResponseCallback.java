package io.littlehorse.server.streamsbackend.util;

import com.google.protobuf.MessageOrBuilder;

public interface LHAsyncResponseCallback<T extends MessageOrBuilder> {
    public void onResponse(T resp);
}
