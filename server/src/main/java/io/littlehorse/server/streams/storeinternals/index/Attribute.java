package io.littlehorse.server.streams.storeinternals.index;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.AttributePb;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.io.UnsupportedEncodingException;

public class Attribute extends LHSerializable<AttributePb> {

    private String key;
    private String val;

    public Attribute(String key, String val) {
        this.key = key;
        this.val = val;
    }

    public Attribute() {}

    public Class<AttributePb> getProtoBaseClass() {
        return AttributePb.class;
    }

    public AttributePb.Builder toProto() {
        AttributePb.Builder out = AttributePb.newBuilder().setKey(key).setVal(val);
        try {
            key.getBytes("UTF-8");
            val.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        AttributePb p = (AttributePb) proto;
        key = p.getKey();
        val = p.getVal();
    }

    public String getEscapedKey() {
        return escapeString(key);
    }

    public String getEscapedVal() {
        return escapeString(val);
    }

    public String escapeString(String str) {
        if (str == null) return null;
        return str.replace("_", "\\_");
    }

    public static Attribute fromProto(AttributePb p, ExecutionContext context) {
        Attribute out = new Attribute();
        out.initFrom(p, context);
        return out;
    }
}
