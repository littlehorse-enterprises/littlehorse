package io.littlehorse.test;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.test.exception.LHTestInitializationException;
import io.littlehorse.test.internal.ExternalTestBootstrapper;
import io.littlehorse.test.internal.StandaloneTestBootstrapper;
import io.littlehorse.test.internal.TestContext;

import java.io.IOException;
import java.util.List;

import org.awaitility.Awaitility;
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
                        LH_TEST_CONTEXT, s -> new TestContext(new StandaloneTestBootstrapper()), TestContext.class);
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(LH_TEST_NAMESPACE);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        ExtensionContext.Store store = getStore(context);
        TestContext testContext = store.get(LH_TEST_CONTEXT, TestContext.class);
        try {
        List<LHTaskWorker> workers = testContext.discoverTaskWorkers(testInstance);
        for (LHTaskWorker worker : workers) {
            if(store.get(worker.getTaskDefName()) != null){
                continue;
            }
            store.put(worker.getTaskDefName(), worker);
            worker.registerTaskDef(true);
            Awaitility.await().ignoreException(StatusRuntimeException.class).ignoreException(LHMisconfigurationException.class).until(() -> {
                try {
                    worker.start();
                    return true;
                }catch (IOException e){
                    throw new IllegalStateException(e);
                }
            });
        }
        } catch (IOException e) {
            throw new LHTestInitializationException(
                    "Something went wrong registering task workers", e);
        }
        testContext.instrument(testInstance);
    }
}
