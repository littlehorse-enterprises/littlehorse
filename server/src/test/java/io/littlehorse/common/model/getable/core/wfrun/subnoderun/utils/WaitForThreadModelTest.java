package io.littlehorse.common.model.getable.core.wfrun.subnoderun.utils;

import static io.littlehorse.sdk.common.proto.LHStatus.*;
import static org.assertj.core.api.Assertions.*;

import io.littlehorse.sdk.common.proto.LHStatus;
import org.junit.jupiter.api.Test;

public class WaitForThreadModelTest {

    private final WaitForThreadModel waitForThreadModel = new WaitForThreadModel();

    @Test
    void shouldBeFailedIfHasErrorStatus() {
        waitForThreadModel.setThreadStatus(ERROR);
        assertThat(waitForThreadModel.isFailed()).isTrue();
    }

    @Test
    void shouldBeFailedIfHasExceptionStatus() {
        waitForThreadModel.setThreadStatus(EXCEPTION);
        assertThat(waitForThreadModel.isFailed()).isTrue();
    }

    @Test
    void shouldNotBeFailedIfHasNonFailureStatus() {
        for (LHStatus statusToTest : LHStatus.values()) {
            if (statusToTest == EXCEPTION || statusToTest == ERROR) {
                continue;
            }
            waitForThreadModel.setThreadStatus(statusToTest);
            assertThat(waitForThreadModel.isFailed()).isFalse();
        }
    }
}
