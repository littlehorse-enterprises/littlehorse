package io.littlehorse.common.model.command;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractResponse<T extends Message> extends LHSerializable<T> {

    public LHResponseCode code;
    public String message;
}
