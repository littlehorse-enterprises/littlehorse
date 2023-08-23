package io.littlehorse.test;

import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.test.exception.LHTestInitializationException;
import io.littlehorse.test.internal.ExternalTestBootstrapper;
import io.littlehorse.test.internal.TestContext;
import java.util.List;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class LHExtension implements BeforeAllCallback, TestInstancePostProcessor {

    private static final ExtensionContext.Namespace LH_TEST_NAMESPACE =
            ExtensionContext.Namespace.create(LHExtension.class);
    private static final String LH_TEST_CONTEXT = "LH-test-context";

    @Override
    public void beforeAll(ExtensionContext context) {
        getStore(context)
                .getOrComputeIfAbsent(
                        LH_TEST_CONTEXT, s -> new TestContext(new ExternalTestBootstrapper()), TestContext.class);
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(LH_TEST_NAMESPACE);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        ExtensionContext.Store store = getStore(context);
        TestContext testContext = store.get(LH_TEST_CONTEXT, TestContext.class);
        List<LHTaskWorker> workers = testContext.discoverTaskWorkers(testInstance);
        for (LHTaskWorker worker : workers) {
            store.put(worker.getTaskDefName(), worker);
            try {
                worker.registerTaskDef(true);
                worker.start();
            } catch (LHApiError e) {
                throw new LHTestInitializationException(
                        "Something went wrong registering task worker " + worker.getTaskDefName(), e);
            }
        }
        testContext.instrument(testInstance);
    }
}
