package io.littlehorse.jtests;

import io.littlehorse.jlib.client.LHClient;
import io.littlehorse.jlib.common.config.LHWorkerConfig;
import io.littlehorse.jtests.test.Test;
import io.littlehorse.jtests.test.lifecycletests.AATaskDefDependency;
import io.littlehorse.jtests.test.lifecycletests.ABSimpleDeleteDeadTaskWorker;
import io.littlehorse.jtests.test.lifecycletests.ACSimpleTaskRebalancing;
import io.littlehorse.jtests.test.workflowtests.AASequential;
import io.littlehorse.jtests.test.workflowtests.ABIntInputVars;
import io.littlehorse.jtests.test.workflowtests.ACVarMutationsJsonObj;
import io.littlehorse.jtests.test.workflowtests.ADVarMutationsNumbers;
import io.littlehorse.jtests.test.workflowtests.AEVarMutationsRemoveKey;
import io.littlehorse.jtests.test.workflowtests.AFVarMutationsRemoveFromList;
import io.littlehorse.jtests.test.workflowtests.AGConditionalsEquals;
import io.littlehorse.jtests.test.workflowtests.AHConditionalsNotEquals;
import io.littlehorse.jtests.test.workflowtests.AIConditionalsLessThan;
import io.littlehorse.jtests.test.workflowtests.AJConditionalsLessThanEq;
import io.littlehorse.jtests.test.workflowtests.AKConditionalsGreaterThan;
import io.littlehorse.jtests.test.workflowtests.ALConditionalsGreaterThanEq;
import io.littlehorse.jtests.test.workflowtests.AMConditionalsIn;
import io.littlehorse.jtests.test.workflowtests.ANConditionalsNotIn;
import io.littlehorse.jtests.test.workflowtests.AOExternalEventBasic;
import io.littlehorse.jtests.test.workflowtests.APConditionalLogicComplex;
import io.littlehorse.jtests.test.workflowtests.AQConditionalLogicComplex2;
import io.littlehorse.jtests.test.workflowtests.ARChildThreadSimple;
import io.littlehorse.jtests.test.workflowtests.ASChildThreadFails;
import io.littlehorse.jtests.test.workflowtests.ATInterruptsBasic;
import io.littlehorse.jtests.test.workflowtests.AUExceptionHandlerTask;
import io.littlehorse.jtests.test.workflowtests.AVChildThreadInterrupt;
import io.littlehorse.jtests.test.workflowtests.AWChildThreadExceptionHandler;
import io.littlehorse.jtests.test.workflowtests.AXConditionalWhileLogic;
import io.littlehorse.jtests.test.workflowtests.AYExtEvtFollowedByChildThread;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDriver {

    private static Logger log = LoggerFactory.getLogger(TestDriver.class);

    public static List<Class<? extends Test>> TEST_CLASSES = Arrays.asList(
        AASequential.class,
        ABIntInputVars.class,
        ACVarMutationsJsonObj.class,
        ADVarMutationsNumbers.class,
        AEVarMutationsRemoveKey.class,
        AFVarMutationsRemoveFromList.class,
        AGConditionalsEquals.class,
        AHConditionalsNotEquals.class,
        AIConditionalsLessThan.class,
        AJConditionalsLessThanEq.class,
        AKConditionalsGreaterThan.class,
        ALConditionalsGreaterThanEq.class,
        AMConditionalsIn.class,
        ANConditionalsNotIn.class,
        AOExternalEventBasic.class,
        APConditionalLogicComplex.class,
        AQConditionalLogicComplex2.class,
        ARChildThreadSimple.class,
        ASChildThreadFails.class,
        ATInterruptsBasic.class,
        AUExceptionHandlerTask.class,
        AVChildThreadInterrupt.class,
        AWChildThreadExceptionHandler.class,
        AXConditionalWhileLogic.class,
        AYExtEvtFollowedByChildThread.class,
        AATaskDefDependency.class,
        ABSimpleDeleteDeadTaskWorker.class,
        ACSimpleTaskRebalancing.class
    );

    public static Properties getConfigProps() {
        Properties props = new Properties();
        props.put("LHC_API_HOST", "localhost");
        props.put("LHC_API_PORT", 5000);
        props.put("LHW_TASK_WORKER_VERSION", "lh.integration-tests.local");

        Path configPath = Path.of(
            System.getProperty("user.home"),
            ".config/littlehorse.config"
        );
        if (Files.exists(configPath)) {
            log.info("Loading config from properties file.");
            try {
                props.load(
                    new InputStreamReader(new FileInputStream(configPath.toFile()))
                );
            } catch (IOException exn) {
                exn.printStackTrace();
                log.info("Failed to load config file, using defaults");
            }
        } else {
            log.info("No config found. Using defaults.");
        }
        return props;
    }

    public static void main(String[] args) throws Exception {
        Properties props = getConfigProps();
        LHWorkerConfig workerConfig = new LHWorkerConfig(props);
        LHClient client = new LHClient(workerConfig);

        for (Class<? extends Test> testClass : TEST_CLASSES) {
            Test test = testClass
                .getDeclaredConstructor(LHClient.class, LHWorkerConfig.class)
                .newInstance(client, workerConfig);

            try {
                test.test();
                test.cleanup();
            } catch (Exception exn) {
                // Raise a big stink and make the CI/CD fail.
                String exnMessage = exn.getMessage();
                exn.printStackTrace();
                if (exn.getCause() != null) {
                    exn.getCause().printStackTrace();
                    exnMessage += " / " + exn.getCause().getMessage();
                }

                System.err.println(
                    "Test " +
                    testClass.getName() +
                    " failed: " +
                    test.getDescription() +
                    ": " +
                    exnMessage
                );
                try {
                    test.cleanup();
                } catch (Exception exn2) {
                    log.warn("Failed cleaning up: " + exn2.getMessage());
                }
                System.exit(1);
            }
        }
        System.out.println("Tests passed!");
        System.exit(0);
    }
}
