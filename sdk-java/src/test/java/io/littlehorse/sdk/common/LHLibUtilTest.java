package io.littlehorse.sdk.common;

import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.WfRunId;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class LHLibUtilTest {

    @Test
    void shouldParseParentWfRunId() {
        String child = "anakin";
        String parent = "obi-wan";
        String grandparent = "qui-gon";

        WfRunId quiGon = WfRunId.newBuilder().setId(grandparent).build();
        WfRunId obiWan = WfRunId.newBuilder().setId(parent).setParentWfRunId(quiGon).build();
        WfRunId anakin = WfRunId.newBuilder().setId(child).setParentWfRunId(obiWan).build();

        String anakinStr = LHLibUtil.wfRunIdToString(anakin);
        Assertions.assertThat(anakinStr).isEqualTo(grandparent + "_" + parent + "_" + child);

        // Darth vader is Anakin but re-constructed
        WfRunId darthVader = LHLibUtil.wfRunId(anakinStr);
        Assertions.assertThat(darthVader.getId()).isEqualTo(child);
        Assertions.assertThat(darthVader.getParentWfRunId().getId()).isEqualTo(parent);
        Assertions.assertThat(darthVader.getParentWfRunId().getParentWfRunId().getId()).isEqualTo(grandparent);
    }

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
