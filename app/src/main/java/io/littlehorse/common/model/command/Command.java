package io.littlehorse.common.model.command;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.CommandPb;
import io.littlehorse.common.proto.CommandPbOrBuilder;

public class Command extends LHSerializable<CommandPb> {

    public Class<CommandPb> getProtoBaseClass() {
        return CommandPb.class;
    }

    public CommandPb.Builder toProto() {
        CommandPb.Builder out = CommandPb.newBuilder();
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        CommandPbOrBuilder p = (CommandPbOrBuilder) proto;
    }

    public SubCommand<?> getSubCommand() {
        return null;
    }
}
