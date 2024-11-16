package io.littlehorse.test;

import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import io.littlehorse.test.exception.LHTestInitializationException;
import io.littlehorse.test.internal.TestBootstrapper;
import io.littlehorse.test.internal.TestContext;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;

public class LHExtension implements BeforeAllCallback, TestInstancePostProcessor, TestInstancePreDestroyCallback {

    public static final String BOOTSTRAPPER_CLASS_PROPERTY = "bootstrapper.class";
    public static final String BOOTSTRAPPER_CLASS_ENV =
            BOOTSTRAPPER_CLASS_PROPERTY.toUpperCase().replace(".", "_");
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

        if (System.getenv(BOOTSTRAPPER_CLASS_ENV) != null) {
            testConfig.put(BOOTSTRAPPER_CLASS_PROPERTY, System.getenv(BOOTSTRAPPER_CLASS_ENV));
            return testConfig;
        }

        if (System.getProperty(BOOTSTRAPPER_CLASS_PROPERTY) != null) {
            testConfig.put(BOOTSTRAPPER_CLASS_PROPERTY, System.getProperty(BOOTSTRAPPER_CLASS_PROPERTY));
            return testConfig;
        }

        try {
            InputStream configStream = LHExtension.class.getClassLoader().getResourceAsStream("test.properties");
            if (configStream == null) {
                throw new FileNotFoundException("test.properties not found in the classpath");
            }
            testConfig.load(configStream);
            return testConfig;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        } catch (IllegalAccessException e) {
            throw new LHTestInitializationException("Something went wrong registering task workers", e);
        }
        testContext.instrument(testInstance);
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
}
