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
import io.littlehorse.server.streams.topology.core.Forwardable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LHTimer extends LHSerializable<LHTimerPb> implements Forwardable {

    public Date maturationTime;
    public String topic;
    public String partitionKey;
    public byte[] payload;
    private TenantIdModel tenantId;
    private PrincipalIdModel principalId;
    private String storeKeyInternal;

    public LHTimer() {}

    public LHTimer(CommandModel command) {
        maturationTime = command.getTime();
        if (maturationTime == null) {
            throw new IllegalArgumentException("Command's time was null!");
        }
        payload = command.toProto().build().toByteArray();
        partitionKey = command.getPartitionKey();
        if (command.hasResponse()) {
            throw new IllegalArgumentException("Timer commands cannot have a response");
        }
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        LHTimerPb p = (LHTimerPb) proto;
        maturationTime = LHUtil.fromProtoTs(p.getMaturationTime());
        topic = p.getTopic();
        partitionKey = p.getPartitionKey();
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

        if (p.hasStoreKey()) {
            this.storeKeyInternal = p.getStoreKey();
        } else {
            // This is what we used in the past. It is left here for backwards compatibility
            // for timers that were created before we optimized the key structure
            storeKeyInternal =
                    LHUtil.toLhDbFormat(maturationTime) + "_" + topic + "_" + partitionKey + "/tenant_" + tenantId;
        }
    }

    public LHTimerPb.Builder toProto() {
        LHTimerPb.Builder out = LHTimerPb.newBuilder()
                .setMaturationTime(LHUtil.fromDate(maturationTime))
                .setPartitionKey(partitionKey)
                .setTopic(topic)
                .setPayload(ByteString.copyFrom(payload))
                .setStoreKey(getStoreKey())
                .setPrincipalId(principalId.toProto()) // TODO: allow nulls
                .setTenantId(tenantId.toProto()); // TODO: Allow nulls
        return out;
    }

    public String getStoreKey() {
        if (storeKeyInternal == null) {
            storeKeyInternal = LHUtil.toLhDbFormat(maturationTime) + "_" + LHUtil.generateGuid();
        }
        return storeKeyInternal;
    }

    @Override
    public Class<LHTimerPb> getProtoBaseClass() {
        return LHTimerPb.class;
    }

    public byte[] getPayload(LHServerConfig config) {
        return payload;
    }
}
