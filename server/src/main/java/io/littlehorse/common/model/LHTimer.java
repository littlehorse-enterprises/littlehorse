package io.littlehorse.common.model;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.command.CommandModel;
import io.littlehorse.common.proto.LHTimerPb;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

public class LHTimer extends LHSerializable<LHTimerPb> {

    public Date maturationTime;
    public String topic;
    public String key;
    public byte[] payload;

    public LHTimer() {}

    public LHTimer(CommandModel command, CoreProcessorDAO dao) {
        maturationTime = command.getTime();
        payload = command.toProto().build().toByteArray();
        key = command.getPartitionKey();
        topic = dao.getCoreCmdTopic();
    }

    public void initFrom(Message proto) {
        LHTimerPb p = (LHTimerPb) proto;
        maturationTime = LHUtil.fromProtoTs(p.getMaturationTime());
        topic = p.getTopic();
        key = p.getKey();
        payload = p.getPayload().toByteArray();
    }

    public LHTimerPb.Builder toProto() {
        LHTimerPb.Builder out = LHTimerPb.newBuilder()
                .setMaturationTime(LHUtil.fromDate(maturationTime))
                .setKey(key)
                .setTopic(topic)
                .setPayload(ByteString.copyFrom(payload));

        return out;
    }

    public String getStoreKey() {
        return LHUtil.toLhDbFormat(maturationTime) + "_" + topic + "_" + key;
    }

    public Class<LHTimerPb> getProtoBaseClass() {
        return LHTimerPb.class;
    }

    public byte[] getPayload(LHConfig config) {
        return payload;
    }
}
