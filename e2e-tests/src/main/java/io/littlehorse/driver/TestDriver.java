package io.littlehorse.driver;

import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.tests.Test;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TestDriver {

    private static final Logger log = LoggerFactory.getLogger(TestDriver.class);

    protected Set<Class<?>> tests;
    protected int threads;
    protected LHWorkerConfig workerConfig;
    protected LHPublicApiBlockingStub client;
    private int executedTest;

    public TestDriver(Set<Class<?>> tests, int threads) {
        this.tests = tests;
        this.threads = threads;
    }

    public abstract void setup() throws Exception;

    public void run() throws Exception {
        ForkJoinPool customThreadPool = new ForkJoinPool(threads);

        customThreadPool
                .submit(() -> tests.parallelStream().forEach(testClass -> {
                    execTest(workerConfig, client, testClass);
                }))
                .get();
        customThreadPool.shutdown();

        log.info("\u001B[32mPlanned tests: {}. Executed tests: {}.\u001B[0m", tests.size(), executedTest);
    }

    private void execTest(LHWorkerConfig workerConfig, LHPublicApiBlockingStub client, Class<?> testClass) {
        try {
            Test test = (Test) testClass
                    .getDeclaredConstructor(LHPublicApiBlockingStub.class, LHWorkerConfig.class)
                    .newInstance(client, workerConfig);
            log.info(
                    "\u001B[32mStarting test:\n\tName:        {}.\n\tDescription: {}.\u001B[0m",
                    testClass.getName(),
                    test.getDescription());
            test.test();
            test.cleanup();
            executedTest++;
        } catch (Exception exn) {
            String exnMessage = exn.getMessage();

            if (exn.getCause() != null) {
                exnMessage += " / " + exn.getCause().getMessage();
            }

            log.error("\u001B[31mTest {} failed: {}.\u001B[0m", testClass.getName(), exnMessage, exn);
            System.exit(1);
        }
    }
}
