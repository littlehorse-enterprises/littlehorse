package io.littlehorse.test.internal;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowExecutor;
import java.lang.reflect.Field;
import java.util.List;

public class TestContext {

    private final LHWorkerConfig lhWorkerConfig;
    private final LHClient lhClient;

    public TestContext(TestBootstrapper bootstrapper) {
        this.lhWorkerConfig = bootstrapper.getWorkerConfig();
        this.lhClient = bootstrapper.getLhClient();
    }

    public void instrument(Object testInstance) {
        injectWorkflowExecutors(testInstance);
        WorkflowDefinitionDiscover workflowDefinitionDiscover = new WorkflowDefinitionDiscover(
            testInstance
        );
        List<DiscoveredWorkflowDefinition> discoveredWorkflowDefinitions = workflowDefinitionDiscover.scan();
        injectWorkflowDefinitions(testInstance, discoveredWorkflowDefinitions);
    }

    private void injectWorkflowDefinitions(
        Object testInstance,
        List<DiscoveredWorkflowDefinition> discoveredWorkflowDefinitions
    ) {
        discoveredWorkflowDefinitions
            .stream()
            .map(discoveredWorkflowDefinition ->
                new FieldDependencyInjector(
                    discoveredWorkflowDefinition::getWorkflow,
                    testInstance,
                    field ->
                        isWorkflowDefinitionField(discoveredWorkflowDefinition, field)
                )
            )
            .forEach(FieldDependencyInjector::inject);
    }

    private boolean isWorkflowDefinitionField(
        DiscoveredWorkflowDefinition discoveredWorkflowDefinition,
        Field field
    ) {
        if (field.isAnnotationPresent(LHWorkflow.class)) {
            LHWorkflow annotation = field.getAnnotation(LHWorkflow.class);
            String definedName = annotation.value();
            return discoveredWorkflowDefinition.getName().equals(definedName);
        }
        return false;
    }

    private void injectWorkflowExecutors(Object testInstance) {
        new FieldDependencyInjector(
            () -> new WorkflowExecutor(lhClient),
            testInstance,
            field -> field.getType().isAssignableFrom(WorkflowExecutor.class)
        )
            .inject();
    }
}
