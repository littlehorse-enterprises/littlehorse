package io.littlehorse.common.model.server;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.RangeResponsePb;
import io.littlehorse.common.proto.RangeResponsePbOrBuilder;
import java.util.ArrayList;
import java.util.List;

public class RangeResponse extends LHSerializable<RangeResponsePb> {

    public String token;
    public List<String> ids;

    public RangeResponse() {
        ids = new ArrayList<>();
    }

    public Class<RangeResponsePb> getProtoBaseClass() {
        return RangeResponsePb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        RangeResponsePbOrBuilder p = (RangeResponsePbOrBuilder) proto;
        if (p.hasToken()) token = p.getToken();
        for (String theId : p.getIdsList()) {
            ids.add(theId);
        }
    }

    public RangeResponsePb.Builder toProto() {
        RangeResponsePb.Builder out = RangeResponsePb.newBuilder();
        for (String id : ids) {
            out.addIds(id);
        }
        if (token != null) out.setToken(token);
        return out;
    }
}
