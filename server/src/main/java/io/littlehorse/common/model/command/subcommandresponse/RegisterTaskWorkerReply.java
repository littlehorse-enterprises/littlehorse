package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.sdk.common.proto.HostInfo;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse.Builder;
import java.util.ArrayList;
import java.util.List;

public class RegisterTaskWorkerReply extends AbstractResponse<RegisterTaskWorkerResponse> {

    public List<HostInfo> yourHosts = new ArrayList<>();

    @Override
    public Builder toProto() {
        Builder builder = RegisterTaskWorkerResponse.newBuilder();
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
        RegisterTaskWorkerResponse p = (RegisterTaskWorkerResponse) proto;

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
    public Class<RegisterTaskWorkerResponse> getProtoBaseClass() {
        return RegisterTaskWorkerResponse.class;
    }
}
