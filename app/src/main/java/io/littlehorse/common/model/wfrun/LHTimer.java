package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.LHTimerPb;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

public class LHTimer extends LHSerializable<LHTimerPb> {

    public Date maturationTime;
    public String topic;
    public String key;
    public byte[] payload;

    public LHTimer() {}

    public void initFrom(MessageOrBuilder proto) {
        LHTimerPb p = (LHTimerPb) proto;
        maturationTime = LHUtil.fromProtoTs(p.getMaturationTime());
        topic = p.getTopic();
        key = p.getKey();
        payload = p.getPayload().toByteArray();
    }

    public LHTimerPb.Builder toProto() {
        LHTimerPb.Builder out = LHTimerPb
            .newBuilder()
            .setMaturationTime(LHUtil.fromDate(maturationTime))
            .setKey(key)
            .setTopic(topic)
            .setPayload(ByteString.copyFrom(payload));

        return out;
    }

    public String getStoreKey() {
        return LHUtil.toLhDbFormat(maturationTime) + "_" + topic + "_" + key;
    }

    @JsonIgnore
    public Class<LHTimerPb> getProtoBaseClass() {
        return LHTimerPb.class;
    }

    @JsonIgnore
    public byte[] getPayload(LHConfig config) {
        return payload;
    }
}
