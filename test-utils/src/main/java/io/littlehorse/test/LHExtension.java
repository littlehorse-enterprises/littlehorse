package io.littlehorse.test;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import io.littlehorse.test.exception.LHTestInitializationException;
import io.littlehorse.test.internal.TestBootstrapper;
import io.littlehorse.test.internal.TestContext;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;

public class LHExtension
        implements BeforeAllCallback,
                TestInstancePostProcessor,
                TestInstancePreDestroyCallback,
                BeforeEachCallback,
                AfterEachCallback {

    public static final String BOOTSTRAPPER_CLASS_PROPERTY = "bootstrapper.class";
    public static final String BOOTSTRAPPER_CLASS_ENV =
            BOOTSTRAPPER_CLASS_PROPERTY.toUpperCase().replace(".", "_");
    public static final String TEST_PROPERTIES_FILE = "test.properties";
    private static final ExtensionContext.Namespace LH_TEST_NAMESPACE =
            ExtensionContext.Namespace.create(LHExtension.class);
    private static final String LH_TEST_CONTEXT = "LH-test-context";
    private static final Properties testConfig;
    private static final TestBootstrapper testBootstrapper;

    static {
        testConfig = loadProperties();
        testBootstrapper = loadBootstrap();
    }

    private static TestBootstrapper loadBootstrap() {
        try {
            Object bootstrapName = testConfig.get(BOOTSTRAPPER_CLASS_PROPERTY);

            if (bootstrapName == null) {
                throw new IllegalStateException("bootstrapper.class property not provided");
            }

            return (TestBootstrapper) Class.forName(bootstrapName.toString())
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Properties loadProperties() {
        Properties testConfig = new Properties();

        InputStream configStream = LHExtension.class.getClassLoader().getResourceAsStream(TEST_PROPERTIES_FILE);
        if (configStream != null) {
            try {
                testConfig.load(configStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // system properties overwrite the file
        if (System.getProperty(BOOTSTRAPPER_CLASS_PROPERTY) != null) {
            testConfig.put(BOOTSTRAPPER_CLASS_PROPERTY, System.getProperty(BOOTSTRAPPER_CLASS_PROPERTY));
        }

        // environment variables overwrite system properties
        if (System.getenv(BOOTSTRAPPER_CLASS_ENV) != null) {
            testConfig.put(BOOTSTRAPPER_CLASS_PROPERTY, System.getenv(BOOTSTRAPPER_CLASS_ENV));
        }

        return testConfig;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        Awaitility.setDefaultPollInterval(Duration.of(40, ChronoUnit.MILLIS));
        Awaitility.setDefaultTimeout(Duration.of(3500, ChronoUnit.MILLIS));
        getStore(context)
                .getOrComputeIfAbsent(LH_TEST_CONTEXT, s -> new TestContext(testBootstrapper), TestContext.class);
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(LH_TEST_NAMESPACE);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        ExtensionContext.Store store = getStore(context);
        TestContext testContext = store.get(LH_TEST_CONTEXT, TestContext.class);
        maybeCreateTenantAndPrincipal(testContext);
        try {
            List<LHTaskWorker> workers = testContext.discoverTaskWorkers(testInstance);
            for (LHTaskWorker worker : workers) {
                if (store.get(worker.getTaskDefName()) != null) {
                    continue;
                }
                store.put(worker.getTaskDefName(), worker);
                worker.registerTaskDef();
                TaskDefId taskDefId =
                        TaskDefId.newBuilder().setName(worker.getTaskDefName()).build();
                Awaitility.await()
                        .ignoreExceptionsMatching(LHTestExceptionUtil::isNotFoundException)
                        .until(() -> testContext.getLhClient().getTaskDef(taskDefId), Objects::nonNull);
                Awaitility.await().until(() -> {
                    worker.start();
                    return true;
                });
            }
            testContext.registerUserTaskSchemas(testInstance);
            List<ExternalEventDef> externalEventDefinitions =
                    testContext.discoverExternalEventDefinitions(testInstance);
            externalEventDefinitions.forEach(testContext::registerExternalEventDef);

            List<PutWorkflowEventDefRequest> workflowEvents = testContext.discoverWorkflowEvents(testInstance);
            workflowEvents.forEach(testContext::registerWorkflowEventDef);
            startTestInstanceWorkers(testInstance, testContext.getConfig(), scanWorkersSetup(testInstance.getClass()));
        } catch (IllegalAccessException e) {
            throw new LHTestInitializationException("Something went wrong registering task workers", e);
        }
        testContext.instrument(testInstance);
    }

    private WithWorkers[] scanWorkersSetup(AnnotatedElement instanceClass) {
        if (instanceClass.isAnnotationPresent(WithWorkers.class)) {
            return new WithWorkers[] {instanceClass.getAnnotation(WithWorkers.class)};
        } else if (instanceClass.isAnnotationPresent(RepeatableWithWorkers.class)) {
            return instanceClass.getAnnotation(RepeatableWithWorkers.class).value();
        } else {
            return new WithWorkers[] {};
        }
    }

    private void startTestInstanceWorkers(Object testInstance, LHConfig config, WithWorkers[] workersSetup)
            throws IllegalAccessException {
        for (WithWorkers workerMetadata : workersSetup) {
            String methodSourceName = workerMetadata.value();
            List<String> allowedMethods = List.of(workerMetadata.lhMethods());
            boolean startAllWorkers = allowedMethods.isEmpty();
            try {
                Object executable =
                        testInstance.getClass().getMethod(methodSourceName).invoke(testInstance);
                List<LHTaskWorker> workers = new ArrayList<>();
                for (Method declaredMethod : executable.getClass().getDeclaredMethods()) {
                    if (declaredMethod.getAnnotation(LHTaskMethod.class) != null) {
                        String taskDefName =
                                declaredMethod.getAnnotation(LHTaskMethod.class).value();
                        if (startAllWorkers || allowedMethods.contains(taskDefName)) {
                            LHTaskWorker worker = new LHTaskWorker(executable, taskDefName, config);
                            workers.add(worker);
                        }
                    }
                }
                for (LHTaskWorker worker : workers) {
                    worker.registerTaskDef();
                    worker.start();
                }
            } catch (NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void preDestroyTestInstance(ExtensionContext context) {
        ExtensionContext.Store store = getStore(context);
        Object testInstance = context.getTestInstance().get();
        TestContext testContext = store.get(LH_TEST_CONTEXT, TestContext.class);

        for (String taskDef : testContext.discoverTaskDefNames(testInstance)) {
            LHTaskWorker worker = (LHTaskWorker) store.get(taskDef);
            if (worker != null) {
                worker.close();
                store.remove(taskDef);
            }
        }
    }

    private void maybeCreateTenantAndPrincipal(TestContext testContext) {
        if (testContext.getConfig().getTenantId() == null) {
            return;
        }
        Awaitility.await()
                .atMost(Duration.ofSeconds(25))
                .ignoreExceptionsMatching(exn -> RuntimeException.class.isAssignableFrom(exn.getClass()))
                .until(() -> {
                    testContext
                            .getLhClient()
                            .withDeadlineAfter(2, TimeUnit.SECONDS)
                            .putTenant(PutTenantRequest.newBuilder()
                                    .setId(testContext.getConfig().getTenantId().getId())
                                    .build());
                    return true;
                });
    }

    @Override
    public void beforeEach(ExtensionContext context) throws IllegalAccessException {
        ExtensionContext.Store store = getStore(context);
        TestContext testContext = store.get(LH_TEST_CONTEXT, TestContext.class);
        WithWorkers[] withWorkers = scanWorkersSetup(context.getRequiredTestMethod());
        startTestInstanceWorkers(context.getRequiredTestInstance(), testContext.getConfig(), withWorkers);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ExtensionContext.Store store = getStore(context);
        TestContext testContext = store.get(LH_TEST_CONTEXT, TestContext.class);
        WithWorkers[] withWorkers = scanWorkersSetup(context.getRequiredTestMethod());
    }
}
