package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.TaskDefOutputSchema;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import io.littlehorse.sdk.worker.WorkerContext;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LHTaskSignature {

    List<VariableType> paramTypes;
    List<String> varNames;
    List<Boolean> maskedParams;
    Method taskMethod;
    boolean hasWorkerContextAtEnd;
    String taskDefName;
    String lhTaskMethodAnnotationValue;
    Object executable;
    TaskDefOutputSchema outputSchema;

    public LHTaskSignature(String taskDefName, Object executable, String lhTaskMethodAnnotationValue)
            throws TaskSchemaMismatchError {
        paramTypes = new ArrayList<>();
        varNames = new ArrayList<>();
        maskedParams = new ArrayList<>();
        hasWorkerContextAtEnd = false;
        this.taskDefName = taskDefName;
        this.executable = executable;
        this.lhTaskMethodAnnotationValue = lhTaskMethodAnnotationValue;

        for (Method method : executable.getClass().getMethods()) {
            if (method.isAnnotationPresent(LHTaskMethod.class)) {
                String taskDefForThisMethod =
                        method.getAnnotation(LHTaskMethod.class).value();

                if (!taskDefForThisMethod.equals(lhTaskMethodAnnotationValue)) {
                    continue;
                }

                if (taskMethod != null) {
                    throw new TaskSchemaMismatchError("Found two annotated task methods!");
                }

                taskMethod = method;
            }
        }

        if (taskMethod == null) {
            throw new TaskSchemaMismatchError("Couldn't find annotated @LHTaskMethod for taskDef "
                    + taskDefName
                    + " on "
                    + executable.getClass());
        }
        VariableType returnType = LHLibUtil.javaClassToLHVarType(taskMethod.getReturnType());
        boolean maskedValue = false;
        String outputSchemaVarName = "output";
        if (taskMethod.isAnnotationPresent(LHType.class)) {
            LHType type = taskMethod.getAnnotation(LHType.class);
            maskedValue = type.masked();
            if (!type.name().isEmpty() || !type.name().isBlank()) {
                outputSchemaVarName = type.name();
            }
        }
        outputSchema = TaskDefOutputSchema.newBuilder()
                .setValueDef(VariableDef.newBuilder()
                        .setType(returnType)
                        .setName(outputSchemaVarName)
                        .setMaskedValue(maskedValue)
                        .build())
                .build();

        for (int i = 0; i < taskMethod.getParameterCount(); i++) {
            Parameter param = taskMethod.getParameters()[i];
            if (param.getType().equals(WorkerContext.class)) {
                if (i + 1 != taskMethod.getParameterCount()) {
                    throw new TaskSchemaMismatchError("Can only have WorkerContext as the last parameter.");
                } else {
                    hasWorkerContextAtEnd = true;
                    continue; // could also be `break;`
                }
            }
            VariableType paramLHType = LHLibUtil.javaClassToLHVarType(param.getType());
            paramTypes.add(paramLHType);
            if (param.isAnnotationPresent(LHType.class)) {
                LHType type = param.getAnnotation(LHType.class);
                maskedParams.add(type.masked());
                if (!type.name().isEmpty() && !type.name().isBlank()) {
                    varNames.add(type.name());
                } else {
                    varNames.add(varNameFromParameterName(param));
                }
            } else {
                maskedParams.add(false);
                varNames.add(varNameFromParameterName(param));
            }
        }
    }

    private String varNameFromParameterName(Parameter param) {
        if (!param.isNamePresent()) {
            log.warn(
                    "Unable to inspect parameter names using reflection; please either compile with"
                            + " `javac -parameters` to enable this, or specify a name via the LHType annotation. "
                            + "Using the parameter position as its name, which makes the resulting TaskDef harder to understand.");
        }
        return param.getName();
    }

    public boolean getHasWorkerContextAtEnd() {
        return hasWorkerContextAtEnd;
    }

    public List<VariableType> getParamTypes() {
        return paramTypes;
    }

    public List<Boolean> getMaskedParams() {
        return maskedParams;
    }

    public String getTaskDefName() {
        return taskDefName;
    }

    public Object getExecutable() {
        return executable;
    }

    public Method getTaskMethod() {
        return taskMethod;
    }

    public List<String> getVarNames() {
        return varNames;
    }

    public TaskDefOutputSchema getOutputSchema() {
        return outputSchema;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LHTaskSignature)) return false;
        LHTaskSignature o = (LHTaskSignature) other;

        List<VariableType> otherTypes = o.getParamTypes();
        if (otherTypes.size() != paramTypes.size()) return false;

        for (int i = 0; i < otherTypes.size(); i++) {
            if (!otherTypes.get(i).equals(paramTypes.get(i))) return false;
        }

        return true;
    }
}
