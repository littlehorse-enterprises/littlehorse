package io.littlehorse.server.quotas;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.grpc.StatusRuntimeException;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.model.getable.global.acl.QuotaModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.QuotaIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.BackendInternalComms;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RequestQuotaManagerTest {

    private BackendInternalComms internalComms;
    private RequestQuotaManager manager;
    private RequestExecutionContext context;
    private ReadOnlyMetadataManager metadataManager;
    private AuthorizationContext authContext;

    private final TenantIdModel tenantId = new TenantIdModel("test-tenant");
    private final PrincipalIdModel principalId = new PrincipalIdModel("test-principal");

    @BeforeEach
    void setup() {
        internalComms = Mockito.mock(BackendInternalComms.class);
        context = Mockito.mock(RequestExecutionContext.class);
        metadataManager = Mockito.mock(ReadOnlyMetadataManager.class);
        authContext = Mockito.mock(AuthorizationContext.class);

        when(context.authorization()).thenReturn(authContext);
        when(context.metadataManager()).thenReturn(metadataManager);
        when(authContext.tenantId()).thenReturn(tenantId);
        when(authContext.principalId()).thenReturn(principalId);
        when(internalComms.getAllInternalHosts()).thenReturn(Set.of(new HostModel("localhost", 2023)));

        manager = new RequestQuotaManager(internalComms);
    }

    @Test
    void noQuotasConfiguredShouldNotThrow() {
        when(metadataManager.get(any(QuotaIdModel.class))).thenReturn(null);

        assertThatCode(() -> manager.enforceOrThrow(context)).doesNotThrowAnyException();
    }

    @Test
    void requestsWithinTenantQuotaShouldNotThrow() {
        QuotaModel tenantQuota = quotaWithRate(100);
        stubQuotas(tenantQuota, null);

        assertThatCode(() -> manager.enforceOrThrow(context)).doesNotThrowAnyException();
    }

    @Test
    void exceedingTenantQuotaShouldThrowResourceExhausted() {
        QuotaModel tenantQuota = quotaWithRate(10); // 5 permits per 500ms window
        stubQuotas(tenantQuota, null);

        // Exhaust permits
        for (int i = 0; i < 5; i++) {
            manager.enforceOrThrow(context);
        }

        assertThatThrownBy(() -> manager.enforceOrThrow(context))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("RESOURCE_EXHAUSTED");
    }

    @Test
    void principalQuotaIsEnforcedIndependently() {
        QuotaModel principalQuota = quotaWithRate(new QuotaIdModel(tenantId, principalId), 10);
        stubQuotas(null, principalQuota);

        for (int i = 0; i < 5; i++) {
            manager.enforceOrThrow(context);
        }

        assertThatThrownBy(() -> manager.enforceOrThrow(context))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("RESOURCE_EXHAUSTED");
    }

    @Test
    void bothQuotasAppliedAndStricterOneWins() {
        // Tenant allows 100 rps, principal allows only 2 rps (1 permit per window)
        QuotaModel tenantQuota = quotaWithRate(100);
        QuotaModel principalQuota = quotaWithRate(new QuotaIdModel(tenantId, principalId), 2);
        stubQuotas(tenantQuota, principalQuota);

        // 1 permit for principal quota
        manager.enforceOrThrow(context);

        // Should be throttled by principal quota even though tenant has budget left
        assertThatThrownBy(() -> manager.enforceOrThrow(context))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("RESOURCE_EXHAUSTED");
    }

    @Test
    void serverCountDividesQuotaAcrossInstances() {
        // 2 servers, 10 rps => 5 rps per server => ~3 permits per window
        when(internalComms.getAllInternalHosts())
                .thenReturn(Set.of(new HostModel("host1", 2023), new HostModel("host2", 2023)));

        QuotaModel tenantQuota = quotaWithRate(10);
        stubQuotas(tenantQuota, null);

        for (int i = 0; i < 3; i++) {
            manager.enforceOrThrow(context);
        }

        assertThatThrownBy(() -> manager.enforceOrThrow(context))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("RESOURCE_EXHAUSTED");
    }

    @Test
    void retryInfoIncludedInException() {
        QuotaModel tenantQuota = quotaWithRate(10);
        stubQuotas(tenantQuota, null);

        for (int i = 0; i < 5; i++) {
            manager.enforceOrThrow(context);
        }

        assertThatThrownBy(() -> manager.enforceOrThrow(context))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("Retry after");
    }

    private void stubQuotas(QuotaModel tenantQuota, QuotaModel principalQuota) {
        String tenantKey = new QuotaIdModel(tenantId).toString();
        String principalKey = new QuotaIdModel(tenantId, principalId).toString();
        when(metadataManager.get(any(QuotaIdModel.class))).thenAnswer(invocation -> {
            QuotaIdModel id = invocation.getArgument(0);
            if (tenantKey.equals(id.toString())) return tenantQuota;
            if (principalKey.equals(id.toString())) return principalQuota;
            return null;
        });
    }

    private QuotaModel quotaWithRate(int requestsPerSecond) {
        return quotaWithRate(new QuotaIdModel(tenantId), requestsPerSecond);
    }

    private QuotaModel quotaWithRate(QuotaIdModel id, int requestsPerSecond) {
        QuotaModel quota = new QuotaModel(id);
        quota.setWriteRequestsPerSecond(requestsPerSecond);
        return quota;
    }
}
