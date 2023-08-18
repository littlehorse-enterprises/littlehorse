package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.worker.LHTaskMethod;
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
    Method taskMethod;
    boolean hasWorkerContextAtEnd;
    String taskDefName;

    Object executable;

    public LHTaskSignature(String taskDefName, Object executable) throws TaskSchemaMismatchError {
        paramTypes = new ArrayList<>();
        varNames = new ArrayList<>();
        hasWorkerContextAtEnd = false;
        this.taskDefName = taskDefName;
        this.executable = executable;

        for (Method method : executable.getClass().getMethods()) {
            if (method.isAnnotationPresent(LHTaskMethod.class)) {
                String taskDefForThisMethod =
                        method.getAnnotation(LHTaskMethod.class).value();

                if (!taskDefForThisMethod.equals(taskDefName)) {
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

            if (!param.isNamePresent()) {
                log.warn("Was unable to inspect parameter names usingreflection; please compile with"
                        + " `javac -Parameters` to enable that.Will use param position as its"
                        + " name, which makes resulting TaskDefharder to understand.");
            }
            paramTypes.add(paramLHType);
            varNames.add(param.getName());
        }
    }

    public boolean getHasWorkerContextAtEnd() {
        return hasWorkerContextAtEnd;
    }

    public List<VariableType> getParamTypes() {
        return paramTypes;
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
