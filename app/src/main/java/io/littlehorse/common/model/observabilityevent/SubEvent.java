package io.littlehorse.common.model.observabilityevent;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;

public abstract class SubEvent<U extends MessageOrBuilder>
    extends LHSerializable<U> {}
