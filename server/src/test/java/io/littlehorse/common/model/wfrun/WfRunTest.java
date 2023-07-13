package io.littlehorse.common.model.wfrun;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class WfRunTest {

    @Test
    void getThreadRunReturnsNullForInvalidThreadRunNumber() {
        WfRun wfRun = new WfRun();
        ThreadRun thread = new ThreadRun();
        wfRun.threadRuns.add(thread);
        assertThat(wfRun.getThreadRun(0) == thread);
        assertThat(wfRun.getThreadRun(-1) == null);
        assertThat(wfRun.getThreadRun(1) == null);
    }
}
