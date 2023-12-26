package io.littlehorse.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.UserTaskField;
import org.junit.jupiter.api.Test;

public class UserTaskUtilTest {
    @Test
    void testshouldBeTrueWhenTwoUserTaskModelsAreEqual() {
        UserTaskField.Builder field =
                UserTaskField.newBuilder().setName("field").setDescription("description");
        String name = "user-task";
        UserTaskDef originalTask = UserTaskDef.newBuilder()
                .addFields(field)
                .setName(name)
                .setVersion(1)
                .build();
        UserTaskDef copyTask = UserTaskDef.newBuilder()
                .addFields(field)
                .setName(name)
                .setVersion(0)
                .build();
        UserTaskDefModel original = UserTaskDefModel.fromProto(originalTask, UserTaskDefModel.class, null);
        UserTaskDefModel copy = UserTaskDefModel.fromProto(copyTask, UserTaskDefModel.class, null);

        assertThat(UserTaskUtil.equals(original, copy)).isTrue();
    }

    @Test
    void testshouldBeFalseWhenTwoUserTaskModelsAreDifferent() {
        UserTaskField.Builder field =
                UserTaskField.newBuilder().setName("field").setDescription("description");
        String name = "user-task";
        UserTaskDef originalTask = UserTaskDef.newBuilder()
                .addFields(field)
                .setName(name)
                .setVersion(1)
                .build();
        UserTaskField.Builder newField =
                UserTaskField.newBuilder().setName("new-field").setDescription("new-description");
        UserTaskDef copyTask = UserTaskDef.newBuilder()
                .addFields(field)
                .addFields(newField)
                .setName(name)
                .setVersion(0)
                .build();
        UserTaskDefModel original = UserTaskDefModel.fromProto(originalTask, UserTaskDefModel.class, null);
        UserTaskDefModel copy = UserTaskDefModel.fromProto(copyTask, UserTaskDefModel.class, null);

        assertThat(UserTaskUtil.equals(original, copy)).isFalse();
    }
}
