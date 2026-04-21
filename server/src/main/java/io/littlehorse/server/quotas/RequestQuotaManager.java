package io.littlehorse.server.quotas;

import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.rpc.Code;
import com.google.rpc.RetryInfo;
import com.google.rpc.Status;
import io.grpc.protobuf.StatusProto;
import io.littlehorse.common.model.getable.global.acl.QuotaModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.QuotaIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.BackendInternalComms;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestQuotaManager {

    private final BackendInternalComms internalComms;
    private final Map<String, QuotaState> quotaStates = new ConcurrentHashMap<>();

    public RequestQuotaManager(BackendInternalComms internalComms) {
        this.internalComms = internalComms;
    }

    public void enforceOrThrow(RequestExecutionContext context) {
        TenantIdModel tenantId = context.authorization().tenantId();
        PrincipalIdModel principalId = context.authorization().principalId();

        QuotaModel tenantQuota = context.metadataManager().get(new QuotaIdModel(tenantId));
        QuotaModel principalQuota = context.metadataManager().get(new QuotaIdModel(tenantId, principalId));
        if (tenantQuota == null && principalQuota == null) return;

        int serverCount = Math.max(1, internalComms.getAllInternalHosts().size());

        long retryDelayMs = 0L;
        if (tenantQuota != null) {
            retryDelayMs = consumeAndGetDelay(tenantQuota, serverCount);
        }
        if (principalQuota != null) {
            retryDelayMs = Math.max(retryDelayMs, consumeAndGetDelay(principalQuota, serverCount));
        }

        if (retryDelayMs > 0) {
            throw StatusProto.toStatusRuntimeException(resourceExhaustedStatus(retryDelayMs));
        }
    }

    private long consumeAndGetDelay(QuotaModel quota, int serverCount) {
        String key = quota.getObjectId().toString();
        QuotaState state = quotaStates.computeIfAbsent(key, k -> new QuotaState());
        return state.recordRequestAndCalculateDelay(quota, serverCount);
    }

    private static Status resourceExhaustedStatus(long retryDelayMs) {
        return Status.newBuilder()
                .setCode(Code.RESOURCE_EXHAUSTED.getNumber())
                .setMessage("Quota exceeded. Retry after %dms.".formatted(retryDelayMs))
                .addDetails(Any.pack(RetryInfo.newBuilder()
                        .setRetryDelay(Duration.newBuilder()
                                .setSeconds(retryDelayMs / 1_000)
                                .setNanos((int) ((retryDelayMs % 1_000) * 1_000_000))
                                .build())
                        .build()))
                .build();
    }
}
