package io.littlehorse.tests;

import static org.reflections.scanners.Scanners.SubTypes;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDriver {

    private static final Logger log = LoggerFactory.getLogger(TestDriver.class);

    public static Properties getConfigProps() {
        Properties props = new Properties();
        props.put("LHC_API_HOST", "localhost");
        props.put("LHC_API_PORT", 2023);
        props.put("LHW_TASK_WORKER_VERSION", "lh.integration-tests.local");

        Path configPath = Path.of(
            System.getProperty("user.home"),
            ".config/littlehorse.config"
        );

        if (Files.exists(configPath)) {
            log.info("Loading config from properties file.");
            try {
                props.load(new FileInputStream(configPath.toFile()));
            } catch (IOException exn) {
                exn.printStackTrace();
                log.info("Failed to load config file, using defaults");
            }
        } else {
            log.info("No config found. Using defaults.");
        }

        return props;
    }

    public static Set<Class<?>> getAllTestClasses() {
        return getAllTestClasses(null);
    }

    public static Set<Class<?>> getAllTestClasses(Set<String> filter) {
        Reflections reflections = new Reflections("io.littlehorse.tests");

        return reflections
            .get(SubTypes.of(Test.class).asClass())
            .stream()
            .filter(aClass -> !Modifier.isAbstract(aClass.getModifiers()))
            .filter(aClass ->
                filter == null ||
                filter.isEmpty() ||
                filter.contains(aClass.getSimpleName())
            )
            .collect(Collectors.toUnmodifiableSet());
    }

    public static void main(String[] args) throws Exception {
        CommandLine cmd = getCommandLine(args);

        Properties props = getConfigProps();
        LHWorkerConfig workerConfig = new LHWorkerConfig(props);
        LHClient client = new LHClient(workerConfig);

        Set<String> testInput = new HashSet<>(cmd.getArgList());
        Set<Class<?>> casesFound = getAllTestClasses(testInput);
        List<Class<?>> remainingTests = new ArrayList<>(casesFound);

        testInput.removeAll(
            casesFound.stream().map(Class::getSimpleName).collect(Collectors.toSet())
        );
        if (!testInput.isEmpty()) {
            log.error("Tests not found: {}", testInput);
            System.exit(1);
        }

        int threads = cmd.hasOption("t")
            ? Integer.parseInt(cmd.getOptionValue("t"))
            : 4;

        ForkJoinPool customThreadPool = new ForkJoinPool(threads);

        customThreadPool
            .submit(() ->
                casesFound
                    .parallelStream()
                    .forEach(testClass -> {
                        execTest(workerConfig, client, testClass, remainingTests);
                    })
            )
            .get();
        customThreadPool.shutdown();

        if (remainingTests.isEmpty()) {
            System.out.println("Tests passed!.");
        } else {
            System.out.println("Remaining tests: " + remainingTests);
        }

        System.exit(0);
    }

    private static void execTest(
        LHWorkerConfig workerConfig,
        LHClient client,
        Class<?> testClass,
        List<Class<?>> remainingTests
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
            remainingTests.remove(testClass);
        } catch (Exception exn) {
            // Raise a big stink and make the CI/CD fail.
            String exnMessage = exn.getMessage();
            exn.printStackTrace();
            if (exn.getCause() != null) {
                exn.getCause().printStackTrace();
                exnMessage += " / " + exn.getCause().getMessage();
            }
            System.err.println(
                "Test " + testClass.getName() + " failed: " + exnMessage
            );
            System.out.println("Remaining tests: " + remainingTests);
            // Don't want to clean up the test, leave it for debugging
            // purposes.
            System.exit(1);
        }
    }

    private static CommandLine getCommandLine(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption(
            Option
                .builder("t")
                .longOpt("threads")
                .hasArg(true)
                .desc("number of threads, default 8")
                .required(false)
                .build()
        );
        options.addOption(
            Option
                .builder("h")
                .longOpt("help")
                .hasArg(false)
                .desc("shows this help message")
                .required(false)
                .build()
        );

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("e2e-tests [OPTIONS] [TESTS]", options);
            System.exit(0);
        }

        return cmd;
    }
}
