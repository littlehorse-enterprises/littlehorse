package io.littlehorse.common.model;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
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
    private TenantIdModel tenantId;
    private PrincipalIdModel principalId;

    public LHTimer() {}

    public LHTimer(CommandModel command) {
        maturationTime = command.getTime();
        if (maturationTime == null) {
            throw new IllegalArgumentException("Command's time was null!");
        }
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

        if (p.hasPrincipalId()) {
            principalId = LHSerializable.fromProto(p.getPrincipalId(), PrincipalIdModel.class, context);
        } else {
            // TODO: We need to introduce some internal principal which says "this is done by the
            // internal system" which is DIFFERENT FROM the `anonymous` principal
            principalId = new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL);
        }

        if (p.hasTenantId()) {
            tenantId = LHSerializable.fromProto(p.getTenantId(), TenantIdModel.class, context);
        } else {
            // TODO: not all timers will belong to a tenant. This logic should change to
            tenantId = new TenantIdModel(LHConstants.DEFAULT_TENANT);
        }
    }

    public LHTimerPb.Builder toProto() {
        LHTimerPb.Builder out = LHTimerPb.newBuilder()
                .setMaturationTime(LHUtil.fromDate(maturationTime))
                .setKey(key)
                .setTopic(topic)
                .setPayload(ByteString.copyFrom(payload))
                .setPrincipalId(principalId.toProto()) // TODO: allow nulls
                .setTenantId(tenantId.toProto()); // TODO: Allow nulls

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
