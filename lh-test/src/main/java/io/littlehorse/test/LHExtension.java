package io.littlehorse.test;

import io.littlehorse.test.internal.ExternalTestBootstrapper;
import io.littlehorse.test.internal.TestBootstrapper;
import io.littlehorse.test.internal.TestContext;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class LHExtension implements BeforeAllCallback, TestInstancePostProcessor {

    private static final ExtensionContext.Namespace LH_TEST_NAMESPACE = ExtensionContext.Namespace.create(
        LHExtension.class
    );
    private static final String LH_TEST_CONTEXT = "LH-test-context";

    private TestBootstrapper testBootstrapper = new ExternalTestBootstrapper();

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        getStore(context)
            .getOrComputeIfAbsent(
                LH_TEST_CONTEXT,
                s -> new TestContext(testBootstrapper),
                TestContext.class
            );
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(LH_TEST_NAMESPACE);
    }

    @Override
    public void postProcessTestInstance(
        Object testInstance,
        ExtensionContext context
    ) {
        getStore(context)
            .get(LH_TEST_CONTEXT, TestContext.class)
            .instrument(testInstance);
    }
}
