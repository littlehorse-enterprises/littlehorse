package io.littlehorse.test;

import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import io.littlehorse.test.exception.LHTestInitializationException;
import io.littlehorse.test.internal.StandaloneTestBootstrapper;
import io.littlehorse.test.internal.TestContext;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;

public class LHExtension implements BeforeAllCallback, TestInstancePostProcessor, TestInstancePreDestroyCallback {

    private static final ExtensionContext.Namespace LH_TEST_NAMESPACE =
            ExtensionContext.Namespace.create(LHExtension.class);
    private static final String LH_TEST_CONTEXT = "LH-test-context";

    @Override
    public void beforeAll(ExtensionContext context) {
        Awaitility.setDefaultPollInterval(Duration.of(25, ChronoUnit.MILLIS));
        Awaitility.setDefaultTimeout(Duration.of(2000, ChronoUnit.MILLIS));
        getStore(context)
                .getOrComputeIfAbsent(
                        LH_TEST_CONTEXT, s -> new TestContext(new StandaloneTestBootstrapper()), TestContext.class);
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
            worker.close();
            store.remove(taskDef);
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
                                    .setId(testContext.getConfig().getTenantId())
                                    .build());
                    return true;
                });
    }
}
