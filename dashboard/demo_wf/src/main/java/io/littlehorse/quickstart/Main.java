package io.littlehorse.quickstart;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import io.littlehorse.sdk.worker.LHTaskWorker;

class FavoritePlayerForm {

    @UserTaskField(displayName = "Favorite Team", required = true)
    public String favoriteTeam;

    @UserTaskField(displayName = "Favorite Player's Number", required = true)
    public int favoritePlayerNumber;
}

public class Main {
    static KnowYourCustomerTasks tasks = new KnowYourCustomerTasks();

    static LHConfig config = new LHConfig();

    public static void main(String[] args) {
        config.getBlockingStub()
                .putExternalEventDef(PutExternalEventDefRequest.newBuilder()
                        .setName("demo-dashboard-external-event")
                        .build());

        UserTaskSchema schema = new UserTaskSchema(new FavoritePlayerForm(), "demo-dashboard-user-task");
        config.getBlockingStub().putUserTaskDef(schema.compile());

        LHTaskWorker demoDashboardTaskWorker = new LHTaskWorker(tasks, "demo-dashboard-task", config);
        demoDashboardTaskWorker.registerTaskDef();
        Runtime.getRuntime().addShutdownHook(new Thread(demoDashboardTaskWorker::close));
        System.out.println("Starting task worker!");
        demoDashboardTaskWorker.start();

        DemoDashboardWorkflow demoDashboardWorkflow = new DemoDashboardWorkflow();
        demoDashboardWorkflow.getWorkflow().registerWfSpec(config.getBlockingStub());
    }
}
