package io.littlehorse.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class LHUtilTest {

    @Test
    void shouldCalculateNextDateFromCronExpression() {
        String cronExpression = "5 0 * 8 *"; // At 00:05 in August.
        Date dateAt = timeToDate(LocalDateTime.of(2300, 1, 2, 1, 0, 0));
        Date expectedDate = timeToDate(LocalDateTime.of(2300, 8, 1, 0, 5, 0));
        Optional<Date> nextDate = LHUtil.nextDate(cronExpression, dateAt);
        Assertions.assertThat(nextDate.get()).isEqualTo(expectedDate);
    }

    @Test
    void shouldHandleNonValidCronExpression() {
        String cronExpression = "dasdasd"; // At 00:05 in August.
        Date dateAt = timeToDate(LocalDateTime.of(2300, 1, 2, 1, 0, 0));
        Throwable thrown = Assertions.catchThrowable(() -> LHUtil.nextDate(cronExpression, dateAt));
        Assertions.assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cron expression contains 1 parts but we expect one of [5]");
    }

    @Test
    void shouldReturnEmptyNextIfNoUpcomingCron() {
        String cronExpression = "5 0 * 8 *"; // At 00:05 in August.
        Date dateAt = timeToDate(LocalDateTime.of(2300, 1, 2, 1, 0, 0));
        Date expectedDate = timeToDate(LocalDateTime.of(2300, 8, 1, 0, 5, 0));
        Optional<Date> nextDate = LHUtil.nextDate(cronExpression, dateAt);
        Assertions.assertThat(nextDate.get()).isEqualTo(expectedDate);
    }

    private Date timeToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
