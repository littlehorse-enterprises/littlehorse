package io.littlehorse.test;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.OutputTopicConfig;
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
import java.util.stream.Collectors;
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
        TestContext testContext = getTestContext(context);
        maybeCreateTenantAndPrincipal(testContext);
        try {
            startWorkersFromDeclaredTaskMethods(testInstance, testContext, store);
            testContext.registerUserTaskSchemas(testInstance);
            registerExternalEventDefinitions(testInstance, testContext);
            registerWorkflowEventDefinitions(testInstance, testContext);
            startTestInstanceWorkers(
                    testInstance, testContext.getConfig(), store, scanWorkersSetup(testInstance.getClass()));
        } catch (IllegalAccessException e) {
            throw new LHTestInitializationException("Something went wrong registering task workers", e);
        }
        testContext.instrument(testInstance);
    }

    private static void registerWorkflowEventDefinitions(Object testInstance, TestContext testContext)
            throws IllegalAccessException {
        List<PutWorkflowEventDefRequest> workflowEvents = testContext.discoverWorkflowEvents(testInstance);
        workflowEvents.forEach(testContext::registerWorkflowEventDef);
    }

    private static void registerExternalEventDefinitions(Object testInstance, TestContext testContext) {
        List<ExternalEventDef> externalEventDefinitions = testContext.discoverExternalEventDefinitions(testInstance);
        externalEventDefinitions.forEach(testContext::registerExternalEventDef);
    }

    private static void startWorkersFromDeclaredTaskMethods(
            Object testInstance, TestContext testContext, ExtensionContext.Store store) {
        List<LHTaskWorker> workers = testContext.discoverTaskWorkers(testInstance);
        startWorkers(workers, store, testContext.getLhClient());
    }

    private static void startWorkers(
            List<LHTaskWorker> workers,
            ExtensionContext.Store store,
            LittleHorseGrpc.LittleHorseBlockingStub lhClient) {
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
                    .until(() -> lhClient.getTaskDef(taskDefId), Objects::nonNull);
            Awaitility.await().until(() -> {
                worker.start();
                return true;
            });
        }
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

    private void startTestInstanceWorkers(
            Object testInstance, LHConfig config, ExtensionContext.Store store, WithWorkers[] workersSetup)
            throws IllegalAccessException {
        List<LHTaskWorker> workers = discoverTaskWorkers(workersSetup, testInstance, config);
        startWorkers(workers, store, config.getBlockingStub());
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

    private List<LHTaskWorker> discoverTaskWorkers(WithWorkers[] workersSetup, Object testInstance, LHConfig config) {
        List<LHTaskWorker> taskWorkers = new ArrayList<>();
        for (WithWorkers workerMetadata : workersSetup) {
            String methodSourceName = workerMetadata.value();
            List<String> allowedMethods = List.of(workerMetadata.lhMethods());
            boolean startAllWorkers = allowedMethods.isEmpty();
            try {
                Object executable =
                        testInstance.getClass().getMethod(methodSourceName).invoke(testInstance);
                for (Method declaredMethod : executable.getClass().getDeclaredMethods()) {
                    if (declaredMethod.getAnnotation(LHTaskMethod.class) != null) {
                        String taskDefName =
                                declaredMethod.getAnnotation(LHTaskMethod.class).value();
                        if (startAllWorkers || allowedMethods.contains(taskDefName)) {
                            taskWorkers.add(new LHTaskWorker(executable, taskDefName, config));
                        }
                    }
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return taskWorkers;
    }

    private void maybeCreateTenantAndPrincipal(TestContext testContext) {
        if (testContext.getConfig().getTenantId() == null) {
            return;
        }

        LittleHorseBlockingStub client = testContext.getLhClient();

        Awaitility.await()
                .atMost(Duration.ofSeconds(25))
                .ignoreExceptionsMatching(exn -> RuntimeException.class.isAssignableFrom(exn.getClass()))
                .until(() -> {
                    try {
                        testContext
                                .getLhClient()
                                .withDeadlineAfter(2, TimeUnit.SECONDS)
                                .getTenant(testContext.getConfig().getTenantId());
                    } catch (StatusRuntimeException exn) {
                        if (exn.getStatus().getCode() == Code.NOT_FOUND) {
                            client.withDeadlineAfter(2, TimeUnit.SECONDS)
                                    .putTenant(PutTenantRequest.newBuilder()
                                            .setId(testContext
                                                    .getConfig()
                                                    .getTenantId()
                                                    .getId())
                                            .setOutputTopicConfig(OutputTopicConfig.newBuilder())
                                            .build());
                        } else {
                            throw exn;
                        }
                    }
                    return true;
                });
    }

    private TestContext getTestContext(ExtensionContext context) {
        ExtensionContext.Store store = getStore(context);
        return store.get(LH_TEST_CONTEXT, TestContext.class);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws IllegalAccessException {
        ExtensionContext.Store store = getStore(context);
        TestContext testContext = store.get(LH_TEST_CONTEXT, TestContext.class);
        WithWorkers[] withWorkers = scanWorkersSetup(context.getRequiredTestMethod());
        startTestInstanceWorkers(context.getRequiredTestInstance(), testContext.getConfig(), store, withWorkers);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ExtensionContext.Store store = getStore(context);
        TestContext testContext = store.get(LH_TEST_CONTEXT, TestContext.class);
        WithWorkers[] withWorkers = scanWorkersSetup(context.getRequiredTestMethod());
        List<String> taskDefNames =
                discoverTaskWorkers(withWorkers, context.getRequiredTestInstance(), testContext.getConfig()).stream()
                        .map(LHTaskWorker::getTaskDefName)
                        .collect(Collectors.toList());
        for (String taskDefName : taskDefNames) {
            LHTaskWorker taskWorker = (LHTaskWorker) store.get(taskDefName);
            taskWorker.close();
            store.remove(taskDefName);
        }
    }
}
