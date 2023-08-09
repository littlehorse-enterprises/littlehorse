package io.littlehorse.driver;

public class Main {

    public static void main(String[] args) throws Exception {
        TestConfig testConfig = new TestConfig(args);
        TestExecutor testExecutor = new TestExecutor(testConfig);
        testExecutor.run();
        System.exit(0);
    }
}
