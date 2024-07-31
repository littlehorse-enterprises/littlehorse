package io.littlehorse.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class TaskDefUtilTest {
    @Test
    void testshouldBeTrueWhenTwoTaskDefAreEqual() {
        TaskDefId.Builder taskId = TaskDefId.newBuilder().setName("task-name");
        VariableDef.Builder variable =
                VariableDef.newBuilder().setName("variable").setType(VariableType.STR);
        TaskDef.Builder originalDef =
                TaskDef.newBuilder().setId(taskId).addInputVars(variable).setCreatedAt(LHUtil.fromDate(new Date()));
        TaskDef.Builder copyDef = TaskDef.newBuilder().setId(taskId).addInputVars(variable);

        TaskDefModel original = TaskDefModel.fromProto(originalDef.build(), null);
        TaskDefModel copy = TaskDefModel.fromProto(copyDef.build(), null);

        assertThat(TaskDefUtil.equals(original, copy)).isTrue();
    }

    @Test
    void testshouldBeFalseWhenTwoTaskDefAreDifferent() {
        TaskDefId.Builder taskId = TaskDefId.newBuilder().setName("task-name");
        VariableDef.Builder variable =
                VariableDef.newBuilder().setName("variable").setType(VariableType.STR);
        TaskDef.Builder originalDef =
                TaskDef.newBuilder().setId(taskId).addInputVars(variable).setCreatedAt(LHUtil.fromDate(new Date()));
        VariableDef.Builder newVariable =
                VariableDef.newBuilder().setName("new-variable").setType(VariableType.BOOL);
        TaskDef.Builder copyDef =
                TaskDef.newBuilder().setId(taskId).addInputVars(variable).addInputVars(newVariable);

        TaskDefModel original = TaskDefModel.fromProto(originalDef.build(), null);
        TaskDefModel copy = TaskDefModel.fromProto(copyDef.build(), null);

        assertThat(TaskDefUtil.equals(original, copy)).isFalse();
    }
}
