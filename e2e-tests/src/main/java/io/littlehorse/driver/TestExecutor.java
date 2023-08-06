package io.littlehorse.driver;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestExecutor {

    private static final Logger log = LoggerFactory.getLogger(TestExecutor.class);

    private TestDriver driver;

    public TestExecutor(TestConfig testConfig) {
        Set<Class<?>> tests = TestUtils.getAllTestClasses(testConfig.getTestToRun());

        // Set the strategy
        if (testConfig.isStandalone()) {
            log.info("Using test container");
            this.driver = new TestDriverStandalone(tests, testConfig.getThreads());
        } else {
            log.info("Conecting to external server");
            this.driver = new TestDriverExternal(tests, testConfig.getThreads());
        }
    }

    public void run() throws Exception {
        driver.setup();
        driver.run();
    }
}
