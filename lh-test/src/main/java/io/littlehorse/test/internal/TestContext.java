package io.littlehorse.test.internal;

import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TestContext {

    private final LHConfig LHConfig;
    private final LHPublicApiBlockingStub lhClient;

    private final Map<String, ExternalEventDef> externalEventDefMap = new HashMap<>();

    public TestContext(TestBootstrapper bootstrapper) {
        this.LHConfig = bootstrapper.getWorkerConfig();
        this.lhClient = bootstrapper.getLhClient();
    }

    public List<LHTaskWorker> discoverTaskWorkers(Object testInstance) throws IOException {
        List<LHTaskWorker> workers = new ArrayList<>();
        List<LHTaskMethod> annotatedMethods =
                ReflectionUtil.findAnnotatedMethods(testInstance.getClass(), LHTaskMethod.class);
        for (LHTaskMethod annotatedMethod : annotatedMethods) {
            workers.add(new LHTaskWorker(testInstance, annotatedMethod.value(), LHConfig));
        }
        return workers;
    }

    public List<ExternalEventDef> discoverExternalEventDefinitions(Object testInstance) {
        if (testInstance.getClass().isAnnotationPresent(LHTest.class)) {
            LHTest lhTestAnnotation = testInstance.getClass().getAnnotation(LHTest.class);
            return Stream.of(lhTestAnnotation.externalEventNames())
                    .map(externalEventName -> ExternalEventDef.newBuilder()
                            .setName(externalEventName)
                            .build())
                    .toList();
        }
        return List.of();
    }

    public void registerExternalEventDef(ExternalEventDef externalEventDef) {
        PutExternalEventDefRequest putExternalEventDefRequest = PutExternalEventDefRequest.newBuilder()
                .setName(externalEventDef.getName())
                .build();
        ExternalEventDef externalEventDefResult = lhClient.putExternalEventDef(putExternalEventDefRequest);
        externalEventDefMap.put(externalEventDefResult.getName(), externalEventDefResult);
    }

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
