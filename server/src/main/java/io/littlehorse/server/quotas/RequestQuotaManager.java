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
    private final QuotaUsageReporter usageReporter;
    private final Map<String, QuotaState> quotaStates = new ConcurrentHashMap<>();

    public RequestQuotaManager(BackendInternalComms internalComms) {
        this(internalComms, null);
    }

    public RequestQuotaManager(BackendInternalComms internalComms, QuotaUsageReporter usageReporter) {
        this.internalComms = internalComms;
        this.usageReporter = usageReporter;
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
            long delay = consumeAndGetDelay(tenantQuota, serverCount);
            recordUsage(tenantQuota, delay);
            retryDelayMs = delay;
        }
        if (principalQuota != null) {
            long delay = consumeAndGetDelay(principalQuota, serverCount);
            recordUsage(principalQuota, delay);
            retryDelayMs = Math.max(retryDelayMs, delay);
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

    private void recordUsage(QuotaModel quota, long delayMs) {
        if (usageReporter != null) {
            usageReporter.record(quota.getObjectId(), delayMs > 0, delayMs);
        }
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
