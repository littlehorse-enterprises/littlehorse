package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructDefExample {

    private static final Logger log = LoggerFactory.getLogger(StructDefExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("issue-parking-ticket", wf -> {
            WfRunVariable ticketReport =
                    wf.declareStruct("ticket-report", ParkingTicketReport.class).required();
            WfRunVariable carOwner = wf.declareStruct("car-owner", Person.class);

            carOwner.assign(wf.execute("get-car-owner", ticketReport));

            wf.execute("mail-ticket", carOwner);
        });
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
        MyWorker executable = new MyWorker();

        List<LHTaskWorker> workers = new ArrayList<>();

        for (Method method : executable.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(LHTaskMethod.class)) continue;

            LHTaskMethod lhTaskMethod = method.getAnnotation(LHTaskMethod.class);

            workers.add(new LHTaskWorker(executable, lhTaskMethod.value(), config));
        }

        // Gracefully shutdown
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> workers.forEach(worker -> {
                    log.debug("Closing {}", worker.getTaskDefName());
                    worker.close();
                })));
        return workers;
    }

    private static void registerStructDefs(LittleHorseBlockingStub client, Class<?>... structDefClasses) {
        for (Class<?> structDefClass : structDefClasses) {
            registerStructDef(client, structDefClass);
        }
    }

    private static void registerStructDef(LittleHorseBlockingStub client, Class<?> structDefClass) {
        StructDefCompatibilityType compatibilityType = StructDefCompatibilityType.NO_SCHEMA_UPDATES;

        LHStructDefType structDefType = new LHStructDefType(structDefClass, LHTypeAdapterRegistry.empty());

        PutStructDefRequest request = structDefType.toPutStructDefRequest().toBuilder()
                .setAllowedUpdates(compatibilityType)
                .build();

        client.putStructDef(request);
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            runWorkers();
        } else {
            runWf(args);
        }
    }

    public static void runWorkers() throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);

        Workflow workflow = getWorkflow();

        // New worker
        List<LHTaskWorker> workers = getTaskWorkers(config);

        registerStructDefs(config.getBlockingStub(), ParkingTicketReport.class, Person.class, Address.class);

        // Register tasks if they don't exist
        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        // Register WfSpec
        workflow.registerWfSpec(config.getBlockingStub());

        // Run the workers
        for (LHTaskWorker worker : workers) {
            log.debug("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }

    public static void runWf(String[] args) throws IOException {
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        System.out.println("Running the workflow...");

        String vehicleMake = args[0];
        String vehicleModel = args[1];
        String licensePlateNumber = args[2];

        ParkingTicketReport parkingTicketReport =
                new ParkingTicketReport(vehicleMake, vehicleModel, licensePlateNumber, new Date());

        System.out.println("Generated parking ticket report from arguments: \n" + parkingTicketReport);

        client.runWf(RunWfRequest.newBuilder()
                .setWfSpecName("issue-parking-ticket")
                .putVariables("ticket-report", LHLibUtil.objToVarVal(parkingTicketReport))
                .build());
    }
}
