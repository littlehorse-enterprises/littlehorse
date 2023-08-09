package io.littlehorse.common.model.meta;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaskWorkerUserGroupTest {

    private TaskWorkerGroup taskWorkerGroup;

    @BeforeEach
    public void initialize() {
        taskWorkerGroup = new TaskWorkerGroup();
    }

    @Test
    public void whenCreatedAtDoesntHaveValueThenReturnNotNull() {
        assertNotNull(taskWorkerGroup.getCreatedAt());
    }
}
