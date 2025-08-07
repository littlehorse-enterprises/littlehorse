package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.StructDefCircularDependencyException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import io.littlehorse.sdk.worker.LHStructIgnore;
import java.lang.reflect.Field;

public class StructDefUtil {

    public static Class<?> getCoreType(Class<?> clazz) {
        Class<?> coreType = clazz;
        
        while (coreType.isArray()) {
            coreType = coreType.getComponentType();
        }

        return coreType;
    }

    /**
     * Builds an InlineStructDef based on a given Java class
     * @param structClass The Java class you want to convert to an InlineStructDef
     * @return an InlineStructDef
     */
    public static InlineStructDef buildInlineStructDef(Class<?> structClass) {
        InlineStructDef.Builder inlineStructDef = InlineStructDef.newBuilder();

        Field[] fields = structClass.getFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(LHStructIgnore.class)) {
                continue;
            }

            StructFieldDef.Builder fieldDef = StructFieldDef.newBuilder();

            // Assemble Field's TypeDef
            TypeDefinition.Builder typeDef = TypeDefinition.newBuilder();
            String fieldName = field.getName();

            if (field.getType().isAnnotationPresent(LHStructDef.class)) {
                LHStructDef lhStructDefAnnotation = field.getType().getAnnotation(LHStructDef.class);
                typeDef.setStructDefId(StructDefId.newBuilder().setName(lhStructDefAnnotation.name()));
            } else {
                VariableType fieldPrimitiveType = LHLibUtil.javaClassToLHVarType(field.getType());
                typeDef.setPrimitiveType(fieldPrimitiveType);
            }

            if (field.isAnnotationPresent(LHStructField.class)) {
                LHStructField fieldAnnotation = field.getAnnotation(LHStructField.class);

                if (!fieldAnnotation.name().isBlank()) {
                    fieldName = fieldAnnotation.name();
                }

                typeDef.setMasked(fieldAnnotation.masked());
            }

            fieldDef.setFieldType(typeDef);

            // TODO: Process default value, if possible

            inlineStructDef.putFields(fieldName, fieldDef.build());
        }

        return inlineStructDef.build();
    }
}
