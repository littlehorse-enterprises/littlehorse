package io.littlehorse.server.quotas;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.common.model.getable.global.acl.QuotaModel;
import io.littlehorse.common.model.getable.objectId.QuotaIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import org.junit.jupiter.api.Test;

class QuotaStateTest {

    private QuotaModel quotaWithRate(int requestsPerSecond) {
        QuotaModel quota = new QuotaModel(new QuotaIdModel(new TenantIdModel("test-tenant")));
        quota.setWriteRequestsPerSecond(requestsPerSecond);
        return quota;
    }

    @Test
    void firstRequestShouldNotBeThrottled() {
        QuotaState state = new QuotaState();
        QuotaModel quota = quotaWithRate(10);

        long delay = state.recordRequestAndCalculateDelay(quota, 1);

        assertThat(delay).isZero();
    }

    @Test
    void requestsWithinBudgetShouldNotBeThrottled() {
        // 100 rps with 500ms window = 50 permits per window
        QuotaState state = new QuotaState();
        QuotaModel quota = quotaWithRate(100);

        for (int i = 0; i < 50; i++) {
            long delay = state.recordRequestAndCalculateDelay(quota, 1);
            assertThat(delay).isZero();
        }
    }

    @Test
    void exceedingBudgetShouldReturnPositiveDelay() {
        // 10 rps with 500ms window = 5 permits per window
        QuotaState state = new QuotaState();
        QuotaModel quota = quotaWithRate(10);

        // Exhaust permits
        for (int i = 0; i < 5; i++) {
            state.recordRequestAndCalculateDelay(quota, 1);
        }

        // Next request should be throttled
        long delay = state.recordRequestAndCalculateDelay(quota, 1);
        assertThat(delay).isGreaterThan(0);
    }

    @Test
    void delayIncreasesWithMoreExcessRequests() {
        QuotaState state = new QuotaState();
        QuotaModel quota = quotaWithRate(10);

        // Exhaust permits
        for (int i = 0; i < 5; i++) {
            state.recordRequestAndCalculateDelay(quota, 1);
        }

        long delay1 = state.recordRequestAndCalculateDelay(quota, 1);
        long delay2 = state.recordRequestAndCalculateDelay(quota, 1);
        assertThat(delay2).isGreaterThan(delay1);
    }

    @Test
    void serverCountDividesQuota() {
        // 10 rps across 2 servers = 5 rps per server = ~3 permits per 500ms window (ceil(2.5))
        QuotaState state = new QuotaState();
        QuotaModel quota = quotaWithRate(10);

        for (int i = 0; i < 3; i++) {
            long delay = state.recordRequestAndCalculateDelay(quota, 2);
            assertThat(delay).isZero();
        }

        long delay = state.recordRequestAndCalculateDelay(quota, 2);
        assertThat(delay).isGreaterThan(0);
    }

    @Test
    void windowRefreshResetsPermits() throws InterruptedException {
        QuotaState state = new QuotaState();
        QuotaModel quota = quotaWithRate(10);

        // Exhaust permits
        for (int i = 0; i < 5; i++) {
            state.recordRequestAndCalculateDelay(quota, 1);
        }

        // Wait for window to pass
        Thread.sleep(600);

        // Should have fresh permits
        long delay = state.recordRequestAndCalculateDelay(quota, 1);
        assertThat(delay).isZero();
    }

    @Test
    void windowRefreshReducesDebt() throws InterruptedException {
        QuotaState state = new QuotaState();
        QuotaModel quota = quotaWithRate(10);

        // Exhaust permits and accumulate some debt
        for (int i = 0; i < 8; i++) {
            state.recordRequestAndCalculateDelay(quota, 1);
        }

        // Wait for window to pass (debt should be reduced by permitsPerWindow)
        Thread.sleep(600);

        // First request after refill uses a permit, no debt if debt was cleared
        long delay = state.recordRequestAndCalculateDelay(quota, 1);
        assertThat(delay).isZero();
    }

    @Test
    void minimumQuotaIsOneRequestPerSecond() {
        // Even with 0 rps configured, the floor is 1 rps => 1 permit per window
        QuotaState state = new QuotaState();
        QuotaModel quota = quotaWithRate(0);

        long delay = state.recordRequestAndCalculateDelay(quota, 1);
        assertThat(delay).isZero();

        // Second request should be throttled
        delay = state.recordRequestAndCalculateDelay(quota, 1);
        assertThat(delay).isGreaterThan(0);
    }
}
