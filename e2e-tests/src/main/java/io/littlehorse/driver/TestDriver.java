package io.littlehorse.driver;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
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
    protected LHClient client;

    public TestDriver(Set<Class<?>> tests, int threads) {
        this.tests = tests;
        this.threads = threads;
    }

    public abstract void arrange() throws Exception;

    public abstract void teardown() throws Exception;

    public void run() throws Exception {
        ForkJoinPool customThreadPool = new ForkJoinPool(threads);

        customThreadPool
            .submit(() ->
                tests
                    .parallelStream()
                    .forEach(testClass -> {
                        execTest(workerConfig, client, testClass);
                    })
            )
            .get();
        customThreadPool.shutdown();
    }

    private static void execTest(
        LHWorkerConfig workerConfig,
        LHClient client,
        Class<?> testClass
    ) {
        try {
            Test test = (Test) testClass
                .getDeclaredConstructor(LHClient.class, LHWorkerConfig.class)
                .newInstance(client, workerConfig);
            log.info(
                "Starting test {}: {}",
                testClass.getName(),
                test.getDescription()
            );
            test.test();
            test.cleanup();
        } catch (Exception exn) {
            String exnMessage = exn.getMessage();

            if (exn.getCause() != null) {
                exnMessage += " / " + exn.getCause().getMessage();
            }

            log.error("Test {} failed: {}", testClass.getName(), exnMessage, exn);
            System.exit(1);
        }
    }
}
