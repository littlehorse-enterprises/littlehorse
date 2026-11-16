package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

import io.littlehorse.sdk.common.proto.SearchWfMetricWindowRequest;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SearchWfMetricWindowRequestModelTest {
    @Test
    void fixedSleepCanMoveLatestOnlyPastThePopulatedWindow() {
        Instant populatedWindow = Instant.parse("2026-11-10T12:00:00Z");
        Instant beforeSleep = Instant.parse("2026-11-10T12:00:59Z");
        Instant afterSleep = beforeSleep.plusSeconds(65);
        // Identical 65-second waits can select different buckets depending on
        // where the workflow completed within its minute.
        assertWindowAt(Instant.parse("2026-11-10T12:01:15Z"), populatedWindow);
        assertWindowAt(afterSleep, Instant.parse("2026-11-10T12:01:00Z"));
    }

    @Test
    void latestOnlySwitchesBucketsExactlyAtTheMinuteBoundary() {
        assertWindowAt(Instant.parse("2026-11-10T12:01:59.999Z"), Instant.parse("2026-11-10T12:00:00Z"));
        assertWindowAt(Instant.parse("2026-11-10T12:02:00Z"), Instant.parse("2026-11-10T12:01:00Z"));
    }

    private void assertWindowAt(Instant now, Instant expectedStart) {
        try (var clock = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
            clock.when(Instant::now).thenReturn(now);
            var request = SearchWfMetricWindowRequestModel.fromProto(
                    SearchWfMetricWindowRequest.newBuilder()
                            .setWfSpecName("example")
                            .setLatestOnly(true)
                            .build(),
                    null);
            var scan = (io.littlehorse.common.proto.InternalScanPb.TagScanPb)
                    request.getScanBoundary("example").buildScanProto();
            assertThat(scan.getEarliestCreateTime().getSeconds()).isEqualTo(expectedStart.getEpochSecond());
            assertThat(scan.getLatestCreateTime()).isEqualTo(scan.getEarliestCreateTime());
        }
    }
}
