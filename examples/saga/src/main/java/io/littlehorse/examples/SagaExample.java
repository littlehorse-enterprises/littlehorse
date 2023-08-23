package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * LH support advanced microservices patterns like Sagas.
 * Here you are going to be able to see how to define a workflow for a saga transaction.
 * */
public class SagaExample {

    private static final Logger log = LoggerFactory.getLogger(SagaExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "example-saga",
            thread -> {
                WfRunVariable flightConfirmationNumber = thread.addVariable(
                    "flight-confirmation-number",
                    VariableType.STR
                );
                WfRunVariable hotelConfirmationNumber = thread.addVariable(
                    "hotel-confirmation-number",
                    VariableType.STR
                );

                SpawnedThread sagaThread = thread.spawnThread(
                    bookFlightAndHotel(
                        flightConfirmationNumber,
                        hotelConfirmationNumber
                    ),
                    "example-saga",
                    null
                );

                // If there is a failure, we abort it.
                NodeOutput waitForThread = thread.waitForThreads(sagaThread);
                thread.handleException(
                    waitForThread,
                    null,
                    abortFlight(flightConfirmationNumber)
                );
            }
        );
    }

    private static ThreadFunc abortFlight(WfRunVariable flightConfirmationNumber) {
        return abortThread ->
            abortThread.execute("cancel-flight", flightConfirmationNumber);
    }

    private static ThreadFunc bookFlightAndHotel(
        WfRunVariable flightConfirmationNumber,
        WfRunVariable hotelConfirmationNumber
    ) {
        return bookThread -> {
            NodeOutput bookFlightOutput = bookThread.execute("book-flight");
            bookThread.mutate(
                flightConfirmationNumber,
                VariableMutationType.ASSIGN,
                bookFlightOutput
            );

            NodeOutput bookHotelOutput = bookThread.execute("book-hotel");
            bookThread.mutate(
                hotelConfirmationNumber,
                VariableMutationType.ASSIGN,
                bookHotelOutput
            );
        };
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        Path configPath = Path.of(
            System.getProperty("user.home"),
            ".config/littlehorse.config"
        );
        props.load(new FileInputStream(configPath.toFile()));
        return props;
    }

    public static List<LHTaskWorker> getTaskWorkers(LHWorkerConfig config) throws IOException {
        ReservationBooker executable = new ReservationBooker();
        List<LHTaskWorker> workers = List.of(
            new LHTaskWorker(executable, "book-flight", config),
            new LHTaskWorker(executable, "cancel-flight", config),
            new LHTaskWorker(executable, "book-hotel", config)
        );

        // Gracefully shutdown
        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(() ->
                    workers.forEach(worker -> {
                        log.debug("Closing {}", worker.getTaskDefName());
                        worker.close();
                    })
                )
            );
        return workers;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHWorkerConfig config = new LHWorkerConfig(props);
        LHPublicApiGrpc.LHPublicApiBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New workers
        List<LHTaskWorker> workers = getTaskWorkers(config);

        // Register tasks if they don't exist
        for (LHTaskWorker worker : workers) {
            if (worker.doesTaskDefExist()) {
                log.debug(
                    "Task {} already exists, skipping creation",
                    worker.getTaskDefName()
                );
            } else {
                log.debug(
                    "Task {} does not exist, registering it",
                    worker.getTaskDefName()
                );
                worker.registerTaskDef();
            }
        }

        // Register a workflow if it does not exist
        if (workflow.doesWfSpecExist(client)) {
            log.debug(
                "Workflow {} already exists, skipping creation",
                workflow.getName()
            );
        } else {
            log.debug(
                "Workflow {} does not exist, registering it",
                workflow.getName()
            );
            workflow.registerWfSpec(client);
        }

        // Run the workers
        for (LHTaskWorker worker : workers) {
            log.debug("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }
}
