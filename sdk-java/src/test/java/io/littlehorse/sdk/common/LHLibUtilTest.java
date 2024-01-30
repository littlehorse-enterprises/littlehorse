package io.littlehorse.sdk.common;

import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.WfRunId;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class LHLibUtilTest {

    @Test
    void shouldToStringWfRunIdWhenNoParent() {
        String idStr = "asdfasdf";
        WfRunId id = WfRunId.newBuilder().setId(idStr).build();
        Assertions.assertThat(LHLibUtil.wfRunIdToString(id)).isEqualTo(idStr);

        String taskGuid = "task-guid";
        TaskRunId taskRunid =
                TaskRunId.newBuilder().setWfRunId(id).setTaskGuid(taskGuid).build();
        Assertions.assertThat(LHLibUtil.taskRunIdToString(taskRunid)).isEqualTo(idStr + "/" + taskGuid);
    }

    @Test
    void shouldIncludeParentWfRunidOnToString() {
        String parentId = "parent";
        String childId = "child";
        WfRunId id = WfRunId.newBuilder()
                .setId(childId)
                .setParentWfRunId(WfRunId.newBuilder().setId(parentId))
                .build();

        Assertions.assertThat(LHLibUtil.wfRunIdToString(id)).isEqualTo(parentId + "_" + childId);
    }
}
