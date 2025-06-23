package io.littlehorse.sdk.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.littlehorse.sdk.common.config.LHConfig;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class LHTaskWorkerTest {

    @Test
    public void getUnhealthyIfManagerIsNull() {
        LHTaskWorker task = new LHTaskWorker(new TaskWorker(), "greet", Map.of(), new LHConfig(), null);
        IllegalStateException exception = assertThrows(IllegalStateException.class, task::healthStatus);
        assertThat(exception.getMessage()).isEqualTo("Worker not started");
    }

    @Test
    public void shouldResolvePlaceHolder() {
        String taskDefName = "a-task-name-${CLUSTER_NAME}";
        Map<String, String> values = Map.of("CLUSTER_NAME", "pedro-cluster");

        LHTaskWorker task = new LHTaskWorker(new TaskWorker(), taskDefName, new LHConfig(), values);

        assertThat(task.getTaskDefName()).isEqualTo("a-task-name-pedro-cluster");
    }

    @Test
    public void shouldResolvePlaceHolderWhenItIsTheOnlyTextOnTheStringTemplate() {
        String taskDefName = "${CLUSTER_NAME}";
        Map<String, String> values = Map.of("CLUSTER_NAME", "pedro-cluster");

        LHTaskWorker task = new LHTaskWorker(new TaskWorker(), taskDefName, new LHConfig(), values);

        assertThat(task.getTaskDefName()).isEqualTo("pedro-cluster");
    }

    @Test
    public void shouldResolve2PlaceHolders() {
        String taskDefName = "a-task-name-${CLUSTER_NAME}-${CLOUD_NAME}";
        Map<String, String> values = Map.of("CLUSTER_NAME", "pedro-cluster", "CLOUD_NAME", "aws");

        LHTaskWorker task = new LHTaskWorker(new TaskWorker(), taskDefName, new LHConfig(), values);

        assertThat(task.getTaskDefName()).isEqualTo("a-task-name-pedro-cluster-aws");
    }

    @Test
    public void shouldResolve3PlaceHoldersWithOnePlaceholderAtTheBeginningOfTheTemplate() {
        String taskDefName = "${REGION}_a-task-name-${CLUSTER_NAME}-${CLOUD_NAME}";
        Map<String, String> values =
                Map.of("CLUSTER_NAME", "pedro-cluster", "CLOUD_NAME", "aws", "REGION", "us-west-2");

        LHTaskWorker task = new LHTaskWorker(new TaskWorker(), taskDefName, new LHConfig(), values);

        assertThat(task.getTaskDefName()).isEqualTo("us-west-2_a-task-name-pedro-cluster-aws");
    }

    @Test
    public void taskDefNameRemainsTheSameIfItHasNoPlaceholders() {
        String taskDefName = "greet";
        Map<String, String> values =
                Map.of("CLUSTER_NAME", "pedro-cluster", "CLOUD_NAME", "aws", "REGION", "us-west-2");

        LHTaskWorker task = new LHTaskWorker(new TaskWorker(), taskDefName, new LHConfig(), values);

        assertThat(task.getTaskDefName()).isEqualTo("greet");
    }
}

class TaskWorker {
    @LHTaskMethod("greet")
    public String greeting(String name) {
        return "hello there, " + name;
    }

    @LHTaskMethod("something-${INVALID_PLACEHOLDER}")
    public String withInvalidPlaceHolder(String name) {
        return "task with invalid placeholder " + name;
    }

    @LHTaskMethod("${REGION}_a-task-name-${CLUSTER_NAME}-${CLOUD_NAME}")
    public String withPlaceHolderAtTheBeginning(String name) {
        return "task with placeholder at the beginning " + name;
    }

    @LHTaskMethod("a-task-name-${CLUSTER_NAME}-${CLOUD_NAME}")
    public String with2PlaceHolderAtTheBeginning(String name) {
        return "task with 2 placeholders at the beginning " + name;
    }

    @LHTaskMethod("a-task-name-${CLUSTER_NAME}")
    public String with1PlaceHolderAtTheBeginning(String name) {
        return "task with 1 placeholders at the beginning " + name;
    }

    @LHTaskMethod("${CLUSTER_NAME}")
    public String onlyWithPlaceHolder(String name) {
        return "task only with placeholder " + name;
    }
}
