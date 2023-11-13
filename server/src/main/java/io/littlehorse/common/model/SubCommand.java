package io.littlehorse.common.model;

import com.google.protobuf.Message;

public interface SubCommand<T extends Message> {

    boolean hasResponse();
}
