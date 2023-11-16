package io.littlehorse.common.model;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.proto.LHTimerPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LHTimer extends LHSerializable<LHTimerPb> {

    public Date maturationTime;
    public String topic;
    public String key;
    public byte[] payload;
    private String tenantId;
    private String principalId;

    public LHTimer() {}

    public LHTimer(CommandModel command) {
        maturationTime = command.getTime();
        payload = command.toProto().build().toByteArray();
        key = command.getPartitionKey();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        LHTimerPb p = (LHTimerPb) proto;
        maturationTime = LHUtil.fromProtoTs(p.getMaturationTime());
        topic = p.getTopic();
        key = p.getKey();
        payload = p.getPayload().toByteArray();
        principalId = p.hasPrincipalId() ? p.getPrincipalId() : LHConstants.ANONYMOUS_PRINCIPAL;
        tenantId = p.hasTenantId() ? p.getTenantId() : LHConstants.DEFAULT_TENANT;
    }

    public LHTimerPb.Builder toProto() {
        LHTimerPb.Builder out = LHTimerPb.newBuilder()
                .setMaturationTime(LHUtil.fromDate(maturationTime))
                .setKey(key)
                .setTopic(topic)
                .setPayload(ByteString.copyFrom(payload))
                .setPrincipalId(principalId)
                .setTenantId(tenantId);

        return out;
    }

    public String getStoreKey() {
        return LHUtil.toLhDbFormat(maturationTime) + "_" + topic + "_" + key + "/tenant_" + tenantId;
    }

    @Override
    public Class<LHTimerPb> getProtoBaseClass() {
        return LHTimerPb.class;
    }

    public byte[] getPayload(LHServerConfig config) {
        return payload;
    }
}
