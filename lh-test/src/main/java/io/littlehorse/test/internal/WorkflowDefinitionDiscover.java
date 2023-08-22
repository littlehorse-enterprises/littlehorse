package io.littlehorse.test.internal;

import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.exception.LHTestInitializationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class WorkflowDefinitionDiscover {

    private final Object target;

    public WorkflowDefinitionDiscover(Object target) {
        this.target = target;
    }

    public List<DiscoveredWorkflowDefinition> scan() {
        List<DiscoveredWorkflowDefinition> discoveredWorkflowDefinitions = new ArrayList<>();
        for (Method declaredMethod : target.getClass().getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(LHWorkflow.class)) {
                String value = declaredMethod.getAnnotation(LHWorkflow.class).value();
                try {
                    Workflow workflow = (Workflow) declaredMethod.invoke(target);
                    discoveredWorkflowDefinitions.add(new DiscoveredWorkflowDefinition(value, workflow));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new LHTestInitializationException(
                            String.format("Not possible to read %s method", declaredMethod.getName()), e);
                }
            }
        }
        return discoveredWorkflowDefinitions;
    }
}
