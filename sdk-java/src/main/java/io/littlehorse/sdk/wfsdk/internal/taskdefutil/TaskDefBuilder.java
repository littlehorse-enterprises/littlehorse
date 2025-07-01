package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskDefBuilder {

    public Object executable;
    public LHTaskSignature signature;
    public boolean shouldPutStructDefs;

    public TaskDefBuilder(Object executable, String taskDefName, String lhTaskMethodAnnotationValue)
            throws TaskSchemaMismatchError {
        signature = new LHTaskSignature(taskDefName, executable, lhTaskMethodAnnotationValue);
        this.executable = executable;
    }

    public PutTaskDefRequest toPutTaskDefRequest() {
        PutTaskDefRequest.Builder out = PutTaskDefRequest.newBuilder();

        out.addAllInputVars(signature.getVariableDefs());
        out.setName(this.signature.taskDefName);
        if (signature.getReturnType() != null) {
            out.setReturnType(signature.getReturnType());
        }

        return out.build();
    }

    public List<StructDef> buildStructDefsFromTaskSignature() {
        if (signature.getStructDefDependencies().isEmpty()) return List.of();

        List<StructDef> structDefs = new ArrayList<>();

        for (Class<?> structDefClass : signature.getStructDefDependencies()) {
            LHStructDef lhStructDef = structDefClass.getAnnotation(LHStructDef.class);

            StructDef.Builder structDef = StructDef.newBuilder();
            structDef.setId(StructDefId.newBuilder().setName(lhStructDef.name()));
            structDef.setDescription(lhStructDef.description());
            structDef.setStructDef(buildInlineStructDef(structDefClass));

            structDefs.add(structDef.build());
        }

        return structDefs;
    }

    public static InlineStructDef buildInlineStructDef(Class<?> structClass) {
        InlineStructDef.Builder inlineStructDef = InlineStructDef.newBuilder();

        Field[] fields = structClass.getFields();

        for (Field field : fields) {
            StructFieldDef.Builder structFieldDef = StructFieldDef.newBuilder();
            TypeDefinition.Builder structFieldTypeDef = TypeDefinition.newBuilder();

            VariableType fieldPrimitiveType = LHLibUtil.javaClassToLHVarType(field.getType());
            structFieldTypeDef.setPrimitiveType(fieldPrimitiveType);
            if (field.isAnnotationPresent(LHStructField.class)) {
                LHStructField fieldType = field.getAnnotation(LHStructField.class);
                structFieldTypeDef.setMasked(fieldType.masked());
            }
            structFieldDef.setFieldType(structFieldTypeDef);

            Object obj;

            try {
                obj = structClass.getDeclaredConstructor().newInstance();

                Object defaultValue = field.get(obj);
                if (defaultValue != null) {
                    structFieldDef.setDefaultValue(LHLibUtil.objToVarVal(defaultValue));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing defaultValue of field: " + field.getName());
            }

            inlineStructDef.putFields(field.getName(), structFieldDef.build());
        }

        return inlineStructDef.build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        if (!(o instanceof TaskDefBuilder)) return false;
        TaskDefBuilder other = (TaskDefBuilder) o;

        return (signature.equals(other.signature)
                && this.signature.getTaskDefName().equals(other.signature.getTaskDefName()));
    }

    @Override
    public int hashCode() {
        return this.signature.getTaskDefName().hashCode();
    }

    public String getTaskDefName() {
        return this.signature.getTaskDefName();
    }
}
