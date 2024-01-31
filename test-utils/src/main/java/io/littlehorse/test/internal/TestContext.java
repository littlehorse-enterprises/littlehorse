package io.littlehorse.test.internal;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHUserTaskForm;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.awaitility.Awaitility;

public class TestContext {

    private final LHConfig config;
    private final LittleHorseBlockingStub lhClient;
    private final LittleHorseBlockingStub anonymousClient;

    private final Map<String, ExternalEventDef> externalEventDefMap = new HashMap<>();

    private final Map<String, UserTaskSchema> userTaskSchemasStore = new HashMap<>();

    private final Map<String, WfSpec> wfSpecStore = new HashMap<>();

    private final Lock wfSpecStoreLock;

    public TestContext(TestBootstrapper bootstrapper) {
        this.config = bootstrapper.getWorkerConfig();
        this.lhClient = bootstrapper.getLhClient();
        this.anonymousClient = bootstrapper.getAnonymousClient();
        this.wfSpecStoreLock = new ReentrantLock();
    }

    public List<LHTaskWorker> discoverTaskWorkers(Object testInstance) throws IOException {
        List<LHTaskWorker> workers = new ArrayList<>();
        List<LHTaskMethod> annotatedMethods =
                ReflectionUtil.findAnnotatedMethods(testInstance.getClass(), LHTaskMethod.class);
        for (LHTaskMethod annotatedMethod : annotatedMethods) {
            workers.add(new LHTaskWorker(testInstance, annotatedMethod.value(), config));
        }
        return workers;
    }

    public List<UserTaskSchema> discoverUserTaskSchemas(Object testInstance) throws IllegalAccessException {
        List<UserTaskSchema> schemas = new ArrayList<>();
        List<Field> annotatedFields = ReflectionUtil.findAnnotatedFields(testInstance.getClass(), LHUserTaskForm.class);
        for (Field annotatedField : annotatedFields) {
            annotatedField.setAccessible(true);
            Object taskForm = annotatedField.get(testInstance);
            LHUserTaskForm annotation = annotatedField.getAnnotation(LHUserTaskForm.class);
            schemas.add(new UserTaskSchema(taskForm, annotation.value()));
        }
        return schemas;
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
        if (!externalEventDefMap.containsKey(externalEventDef.getName())) {
            PutExternalEventDefRequest putExternalEventDefRequest = PutExternalEventDefRequest.newBuilder()
                    .setName(externalEventDef.getName())
                    .build();

            try {
                ExternalEventDef externalEventDefResult = lhClient.putExternalEventDef(putExternalEventDefRequest);
                externalEventDefMap.put(externalEventDefResult.getName(), externalEventDefResult);
            } catch (StatusRuntimeException exn) {
                if (exn.getStatus().getCode() != Code.ALREADY_EXISTS) {
                    throw exn;
                }
            }
        }
    }

    public void instrument(Object testInstance) {
        injectWorkflowExecutors(testInstance);
        WorkflowDefinitionDiscover workflowDefinitionDiscover = new WorkflowDefinitionDiscover(testInstance);
        List<DiscoveredWorkflowDefinition> discoveredWorkflowDefinitions = workflowDefinitionDiscover.scan();
        injectWorkflowDefinitions(testInstance, discoveredWorkflowDefinitions);
        injectLhClient(testInstance);
        injectLhConfig(testInstance);
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

    private void injectLhClient(Object testInstance) {
        new FieldDependencyInjector(
                        () -> lhClient, testInstance, field -> field.getType().isAssignableFrom(lhClient.getClass()))
                .inject();
    }

    private void injectLhConfig(Object testInstance) {
        new FieldDependencyInjector(
                        () -> config, testInstance, field -> field.getType().isAssignableFrom(config.getClass()))
                .inject();
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
        new FieldDependencyInjector(() -> new WorkflowVerifier(this), testInstance, field -> field.getType()
                        .isAssignableFrom(WorkflowVerifier.class))
                .inject();
    }

    public void registerUserTaskDef(PutUserTaskDefRequest taskDefRequest) {
        lhClient.putUserTaskDef(taskDefRequest);
    }

    public void registerUserTaskSchemas(Object testInstance) throws IllegalAccessException {
        List<UserTaskSchema> userTaskSchemas = discoverUserTaskSchemas(testInstance);
        for (UserTaskSchema userTaskSchema : userTaskSchemas) {
            PutUserTaskDefRequest taskDefRequest = userTaskSchema.compile();
            if (userTaskSchemasStore.get(taskDefRequest.getName()) != null) {
                continue;
            }
            userTaskSchemasStore.put(taskDefRequest.getName(), userTaskSchema);
            registerUserTaskDef(taskDefRequest);
            Awaitility.await()
                    .ignoreExceptionsMatching(exn -> LHTestExceptionUtil.isNotFoundException(exn))
                    .until(
                            () -> lhClient.getLatestUserTaskDef(GetLatestUserTaskDefRequest.newBuilder()
                                    .setName(taskDefRequest.getName())
                                    .build()),
                            Objects::nonNull);
        }
    }

    public WfSpec registerWfSpecIfNotPresent(Workflow workflow) {
        wfSpecStoreLock.lock();
        try {
            if (!wfSpecStore.containsKey(workflow.getName())) {
                GetLatestWfSpecRequest wfSpecRequest = GetLatestWfSpecRequest.newBuilder()
                        .setName(workflow.getName())
                        .build();
                workflow.registerWfSpec(lhClient);
                WfSpec result = Awaitility.await()
                        .ignoreExceptionsMatching(exn -> LHTestExceptionUtil.isNotFoundException(exn))
                        .until(() -> lhClient.getLatestWfSpec(wfSpecRequest), Objects::nonNull);
                wfSpecStore.put(workflow.getName(), result);
            }

            return wfSpecStore.get(workflow.getName());
        } finally {
            wfSpecStoreLock.unlock();
        }
    }

    public LittleHorseBlockingStub getLhClient() {
        return lhClient;
    }

    public LittleHorseBlockingStub getAnonymousClient() {
        return anonymousClient;
    }

    public LHConfig getConfig() {
        return config;
    }
}
