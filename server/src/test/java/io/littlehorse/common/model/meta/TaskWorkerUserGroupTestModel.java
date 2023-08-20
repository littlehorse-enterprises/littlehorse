package io.littlehorse.common.model.meta;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerGroupModel;

public class TaskWorkerUserGroupTestModel {

    private TaskWorkerGroupModel taskWorkerGroup;

    @BeforeEach
    public void initialize() {
        taskWorkerGroup = new TaskWorkerGroupModel();
    }

    @Test
    public void whenCreatedAtDoesntHaveValueThenReturnNotNull() {
        assertNotNull(taskWorkerGroup.getCreatedAt());
    }
}
