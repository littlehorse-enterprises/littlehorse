package io.littlehorse.common.model.command;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.LHResponseCodePb;

public abstract class AbstractResponse<T extends MessageOrBuilder>
    extends LHSerializable<T> {

    public LHResponseCodePb code;
    public String message;
}
