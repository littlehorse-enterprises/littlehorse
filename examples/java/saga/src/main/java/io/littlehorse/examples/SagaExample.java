package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
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
        return new WorkflowImpl("example-saga", wf -> {
            WfRunVariable flightConfirmationNumber = wf.declareStr("flight-confirmation-number");
            WfRunVariable hotelConfirmationNumber = wf.declareStr("hotel-confirmation-number");

            SpawnedThread sagaThread = wf.spawnThread(
                    bookFlightAndHotel(flightConfirmationNumber, hotelConfirmationNumber), "example-saga", null);

            // If there is a failure, we abort it.
            NodeOutput waitForThread = wf.waitForThreads(SpawnedThreads.of(sagaThread));

            wf.handleException(waitForThread, null, abortFlight(flightConfirmationNumber));
        });
    }

    private static ThreadFunc abortFlight(WfRunVariable flightConfirmationNumber) {
        return abortThread -> abortThread.execute("cancel-flight", flightConfirmationNumber);
    }

    private static ThreadFunc bookFlightAndHotel(
            WfRunVariable flightConfirmationNumber, WfRunVariable hotelConfirmationNumber) {
        return bookThread -> {
            NodeOutput bookFlightOutput = bookThread.execute("book-flight");
            flightConfirmationNumber.assign(bookFlightOutput);

            NodeOutput bookHotelOutput = bookThread.execute("book-hotel");
            hotelConfirmationNumber.assign(bookHotelOutput);
        };
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(System.getProperty("user.home"), ".config/littlehorse.config")
                .toFile();
        if (configPath.exists()) {
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) {
        ReservationBooker executable = new ReservationBooker();
        List<LHTaskWorker> workers = List.of(
                new LHTaskWorker(executable, "book-flight", config),
                new LHTaskWorker(executable, "cancel-flight", config),
                new LHTaskWorker(executable, "book-hotel", config));

        // Gracefully shutdown
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> workers.forEach(worker -> {
                    log.debug("Closing {}", worker.getTaskDefName());
                    worker.close();
                })));
        return workers;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseGrpc.LittleHorseBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New workers
        List<LHTaskWorker> workers = getTaskWorkers(config);

        // Register tasks
        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        // Register a workflow
        workflow.registerWfSpec(client);

        // Run the workers
        for (LHTaskWorker worker : workers) {
            log.debug("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }
}
