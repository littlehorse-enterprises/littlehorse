package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHClassType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.StructDefUtil;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import io.littlehorse.sdk.worker.WorkerContext;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LHTaskSignature {

    @Getter
    List<VariableDef> variableDefs;

    LinkedHashSet<Class<?>> structDefClasses;

    Method taskMethod;
    boolean hasWorkerContextAtEnd;
    String taskDefName;
    String lhTaskMethodAnnotationValue;
    Object executable;
    ReturnType outputSchema;

    public LHTaskSignature(String taskDefName, Object executable, String lhTaskMethodAnnotationValue)
            throws TaskSchemaMismatchError {
        variableDefs = new ArrayList<>();
        hasWorkerContextAtEnd = false;
        this.taskDefName = taskDefName;
        this.executable = executable;
        this.lhTaskMethodAnnotationValue = lhTaskMethodAnnotationValue;
        this.structDefClasses = new LinkedHashSet<>();

        for (Method method : executable.getClass().getMethods()) {
            if (method.isAnnotationPresent(LHTaskMethod.class)) {
                String taskDefForThisMethod = method.getAnnotation(LHTaskMethod.class).value();

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

            variableDefs.add(buildVariableDef(param));
        }
        outputSchema = buildReturnType(taskMethod.getReturnType());
    }

    private VariableDef buildVariableDef(Parameter param) {
        VariableDef.Builder varDef = VariableDef.newBuilder();
        TypeDefinition.Builder typeDef = TypeDefinition.newBuilder();
        LHClassType lhClassType = new LHClassType(param.getType());

        // If param has `LHType` annotation...
        if (param.isAnnotationPresent(LHType.class)) {
            LHType lhTypeAnnotation = param.getAnnotation(LHType.class);

            varDef.setName(lhTypeAnnotation.name());
            typeDef.setMasked(lhTypeAnnotation.masked());
        } else {
            varDef.setName(varNameFromParameterName(param));
        }

        if (lhClassType.isStructDef()) {
            typeDef.setStructDefId(StructDefId.newBuilder().setName(lhClassType.getStructDefName()));
            structDefClasses.addAll(lhClassType.getDependencyClasses());
        } else {
            typeDef.setPrimitiveType(lhClassType.getPrimitiveType());
        }

        varDef.setTypeDef(typeDef);
        return varDef.build();
    }

    private ReturnType buildReturnType(Class<?> classReturnType) {
        if (void.class.isAssignableFrom(classReturnType)) {
            // Empty `type` field signifies that it's void.
            return ReturnType.newBuilder().build();
        } else {
            TypeDefinition.Builder typeDef = TypeDefinition.newBuilder();
            LHClassType lhClassType = new LHClassType(classReturnType);

            if (taskMethod.isAnnotationPresent(LHType.class)) {
                LHType type = taskMethod.getAnnotation(LHType.class);
                typeDef.setMasked(type.masked());
            }

            if (lhClassType.isStructDef()) {
                typeDef.setStructDefId(StructDefId.newBuilder().setName(lhClassType.getStructDefName()));
                structDefClasses.addAll(lhClassType.getDependencyClasses());
            } else {
                typeDef.setPrimitiveType(lhClassType.getPrimitiveType());
            }

            return ReturnType.newBuilder().setReturnType(typeDef).build();
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

    public String getTaskDefName() {
        return taskDefName;
    }

    public Object getExecutable() {
        return executable;
    }

    public Method getTaskMethod() {
        return taskMethod;
    }

    public ReturnType getReturnType() {
        return outputSchema;
    }

    public List<StructDef> getStructDefDependencies() {
        if (structDefClasses.isEmpty()) return List.of();

        List<StructDef> structDefs = new ArrayList<>();

        for (Class<?> structDefClass : structDefClasses) {
            LHStructDef lhStructDef = structDefClass.getAnnotation(LHStructDef.class);

            StructDef.Builder structDef = StructDef.newBuilder();
            structDef.setId(StructDefId.newBuilder().setName(lhStructDef.name()));
            structDef.setDescription(lhStructDef.description());
            structDef.setStructDef(StructDefUtil.buildInlineStructDef(structDefClass));

            structDefs.add(structDef.build());
        }

        return Collections.unmodifiableList(structDefs);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LHTaskSignature))
            return false;
        LHTaskSignature o = (LHTaskSignature) other;

        // List<VariableType> otherTypes = o.getParamTypes();
        // if (otherTypes.size() != paramTypes.size()) return false;

        // for (int i = 0; i < otherTypes.size(); i++) {
        // if (!otherTypes.get(i).equals(paramTypes.get(i))) return false;
        // }

        return true;
    }
}
