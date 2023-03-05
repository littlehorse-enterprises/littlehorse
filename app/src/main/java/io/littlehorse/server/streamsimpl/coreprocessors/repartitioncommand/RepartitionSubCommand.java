package io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;

public abstract class RepartitionSubCommand<T extends MessageOrBuilder>
    extends LHSerializable<T> {

    public abstract void process();
}
