package io.littlehorse.test.internal;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TestContext {

    private final LHWorkerConfig lhWorkerConfig;
    private final LHClient lhClient;

    public TestContext(TestBootstrapper bootstrapper) {
        this.lhWorkerConfig = bootstrapper.getWorkerConfig();
        this.lhClient = bootstrapper.getLhClient();
    }

    public List<LHTaskWorker> discoverTaskWorkers(Object testInstance) {
        List<LHTaskWorker> workers = new ArrayList<>();
        List<LHTaskMethod> annotatedMethods =
                ReflectionUtil.findAnnotatedMethods(testInstance.getClass(), LHTaskMethod.class);
        for (LHTaskMethod annotatedMethod : annotatedMethods) {
            workers.add(new LHTaskWorker(testInstance, annotatedMethod.value(), lhWorkerConfig));
        }
        return workers;
    }

    /*public void registerWorkers() {
        for (LHTaskWorker worker : workers) {
            try {
                worker.registerTaskDef(true);
            } catch (LHApiError e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void runTaskWorkers() {
        for (LHTaskWorker worker : workers) {
            try {
                worker.start();
            } catch (LHApiError e) {
                throw new RuntimeException(e);
            }
        }
    }*/

    public void instrument(Object testInstance) {
        injectWorkflowExecutors(testInstance);
        WorkflowDefinitionDiscover workflowDefinitionDiscover = new WorkflowDefinitionDiscover(testInstance);
        List<DiscoveredWorkflowDefinition> discoveredWorkflowDefinitions = workflowDefinitionDiscover.scan();
        injectWorkflowDefinitions(testInstance, discoveredWorkflowDefinitions);
    }

    private void injectWorkflowDefinitions(
            Object testInstance, List<DiscoveredWorkflowDefinition> discoveredWorkflowDefinitions) {
        discoveredWorkflowDefinitions.stream()
                .map(discoveredWorkflowDefinition -> new FieldDependencyInjector(
                        discoveredWorkflowDefinition::getWorkflow,
                        testInstance,
                        field -> isWorkflowDefinitionField(discoveredWorkflowDefinition, field)))
                .forEach(FieldDependencyInjector::inject);
    }

    private boolean isWorkflowDefinitionField(DiscoveredWorkflowDefinition discoveredWorkflowDefinition, Field field) {
        if (field.isAnnotationPresent(LHWorkflow.class)) {
            LHWorkflow annotation = field.getAnnotation(LHWorkflow.class);
            String definedName = annotation.value();
            return discoveredWorkflowDefinition.getName().equals(definedName);
        }
        return false;
    }

    private void injectWorkflowExecutors(Object testInstance) {
        new FieldDependencyInjector(() -> new WorkflowVerifier(lhClient), testInstance, field -> field.getType()
                        .isAssignableFrom(WorkflowVerifier.class))
                .inject();
    }
}
