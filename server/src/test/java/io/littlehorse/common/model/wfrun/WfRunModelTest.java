package io.littlehorse.common.model.wfrun;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class WfRunModelTest {

    @Test
    void getThreadRunReturnsNullForInvalidThreadRunNumber() {
        WfRunModel wfRunModel = new WfRunModel();
        ThreadRunModel thread = new ThreadRunModel();
        wfRunModel.threadRunModels.add(thread);
        assertThat(wfRunModel.getThreadRun(0) == thread);
        assertThat(wfRunModel.getThreadRun(-1) == null);
        assertThat(wfRunModel.getThreadRun(1) == null);
    }
}
