package io.littlehorse.common.model.wfrun;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class WfRunModelTest {

    @Test
    void getThreadRunReturnsNullForInvalidThreadRunNumber() {
        WfRunModel wfRunModel = new WfRunModel();
        ThreadRunModel thread = new ThreadRunModel();
        thread.setNumber(0);
        wfRunModel.setThreadRunsUseMeCarefully(new ArrayList<>(List.of(thread)));
        wfRunModel.setGreatestThreadRunNumber(1);

        assertThat(wfRunModel.getThreadRun(0)).isSameAs(thread);
        assertThat(wfRunModel.getThreadRun(-1)).isNull();
    }
}
