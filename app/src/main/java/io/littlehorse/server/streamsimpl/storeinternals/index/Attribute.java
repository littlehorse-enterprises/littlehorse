package io.littlehorse.server.streamsimpl.storeinternals.index;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.AttributePb;
import io.littlehorse.common.proto.AttributePbOrBuilder;

public class Attribute extends LHSerializable<AttributePb> {

    private String key;
    private String val;

    public Class<AttributePb> getProtoBaseClass() {
        return AttributePb.class;
    }

    public AttributePb.Builder toProto() {
        AttributePb.Builder out = AttributePb.newBuilder().setKey(key).setVal(val);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        AttributePbOrBuilder p = (AttributePbOrBuilder) proto;
        key = p.getKey();
        val = p.getVal();
    }

    // TODO: determine what escaping is necessary
    public String getEscapedKey() {
        return key;
    }

    // TODO: determine what escaping is necessary
    public String getEscapedVal() {
        return val;
    }

    public Attribute(String key, String val) {
        this.key = key;
        this.val = val;
    }

    public Attribute() {}

    public static Attribute fromProto(AttributePbOrBuilder p) {
        Attribute out = new Attribute();
        out.initFrom(p);
        return out;
    }
}
