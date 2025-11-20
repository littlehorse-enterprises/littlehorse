package io.littlehorse.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class LHUtilTest {

    @Test
    void shouldCalculateNextDateFromCronExpression() {
        String cronExpression = "5 0 * 8 *";
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

    @Test
    void shouldConvertGroupedFormatToLegacyFormat() {
        String groupedKey = "tenant1/5/wrg/wf-run-123/5/3/0/1";
        String legacyKey = LHUtil.toLegacyFormat(groupedKey);
        String expected = "tenant1/5/3/wf-run-123/0/1";
        Assertions.assertThat(legacyKey).isEqualTo(expected);
    }

    @Test
    void shouldConvertGroupedFormatWithoutRestOfKey() {
        String groupedKey = "tenant2/5/wrg/wf-run-456/5/1";
        String legacyKey = LHUtil.toLegacyFormat(groupedKey);
        String expected = "tenant2/5/1/wf-run-456";
        Assertions.assertThat(legacyKey).isEqualTo(expected);
    }

    @Test
    void shouldReturnNullForOldFormatKey() {
        String input = "tenant/5/3/old-format-key";
        String result = LHUtil.toLegacyFormat(input);
        Assertions.assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullForLegacyFormatWithoutWrgPrefix() {
        String input = "tenant/5/legacy/wf-run/key";
        String result = LHUtil.toLegacyFormat(input);
        Assertions.assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullForTooFewParts() {
        String input = "tenant/5/wrg";
        String result = LHUtil.toLegacyFormat(input);
        Assertions.assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullForMissingStoreableAndGetableTypes() {
        String input = "tenant/5/wrg/wf-run";
        String result = LHUtil.toLegacyFormat(input);
        Assertions.assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullForMissingGetableType() {
        String input = "tenant/5/wrg/wf-run/5";
        String result = LHUtil.toLegacyFormat(input);
        Assertions.assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullForNonGroupedPrefix() {
        String nonGroupedKey = "tenant/5/other-prefix/wf-run/5/1";
        String result = LHUtil.toLegacyFormat(nonGroupedKey);
        Assertions.assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullForIncorrectPrefixInParts() {
        String incorrectKey = "tenant/5/wrong/wf-run/5/1";
        String result = LHUtil.toLegacyFormat(incorrectKey);
        Assertions.assertThat(result).isNull();
    }

    @Test
    void shouldHandleComplexRestOfKey() {
        String groupedKey = "default/5/wrg/complex-wf-run/5/2/thread/0/node/1/extra";
        String legacyKey = LHUtil.toLegacyFormat(groupedKey);
        String expected = "default/5/2/complex-wf-run/thread/0/node/1/extra";
        Assertions.assertThat(legacyKey).isEqualTo(expected);
    }

    @Test
    void shouldHandleEmptyTenantId() {
        String groupedKey = "/5/wrg/wf-run-789/5/4/simple";
        String legacyKey = LHUtil.toLegacyFormat(groupedKey);
        String expected = "/5/4/wf-run-789/simple";
        Assertions.assertThat(legacyKey).isEqualTo(expected);
    }

    @Test
    void strToJsonObjShouldDeserializeNulls() {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("key1", null);

        Map<String, Object> actualMap = LHUtil.strToJsonObj("{\"key1\": null}");

        Assertions.assertThat(actualMap).isEqualTo(expectedMap);
    }

    @Test
    void objToStringShouldSerializeNulls() {
        String expectedStr = "{\"key1\":null}";

        Map<String, Object> someMap = new HashMap<>();
        someMap.put("key1", null);
        String actualStr = LHUtil.objToString(someMap);

        Assertions.assertThat(expectedStr).isEqualTo(actualStr);
    }
}
