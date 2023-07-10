package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.sdk.common.proto.HostInfoPb;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb.Builder;
import java.util.ArrayList;
import java.util.List;

public class RegisterTaskWorkerReply
    extends AbstractResponse<RegisterTaskWorkerReplyPb> {

    public List<HostInfoPb> yourHosts = new ArrayList<>();

    @Override
    public Builder toProto() {
        Builder builder = RegisterTaskWorkerReplyPb.newBuilder();
        builder.setCode(code);
        if (message != null) {
            builder.setMessage(message);
        }
        if (yourHosts != null) {
            builder.addAllYourHosts(yourHosts);
        }
        return builder;
    }

    @Override
    public void initFrom(Message proto) {
        RegisterTaskWorkerReplyPb p = (RegisterTaskWorkerReplyPb) proto;

        code = p.getCode();

        if (p.hasMessage()) {
            message = p.getMessage();
        }

        if (p.getYourHostsList() != null) {
            yourHosts = p.getYourHostsList();
        } else {
            yourHosts = new ArrayList<>();
        }
    }

    @Override
    public Class<RegisterTaskWorkerReplyPb> getProtoBaseClass() {
        return RegisterTaskWorkerReplyPb.class;
    }
}
