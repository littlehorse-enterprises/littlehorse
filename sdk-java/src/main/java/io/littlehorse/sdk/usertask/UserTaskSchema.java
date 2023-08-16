package io.littlehorse.sdk.usertask;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.PutUserTaskDefPb;
import io.littlehorse.sdk.common.proto.UserTaskFieldPb;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import java.lang.reflect.Field;

public class UserTaskSchema {

    private PutUserTaskDefPb compiled;
    private Object taskObject;
    private String userTaskDefName;

    public UserTaskSchema(Object taskObject, String userTaskDefName) {
        this.taskObject = taskObject;
        this.userTaskDefName = userTaskDefName;
    }

    public PutUserTaskDefPb compile() {
        if (compiled == null) compileHelper();

        return compiled;
    }

    private void compileHelper() {
        PutUserTaskDefPb.Builder out = PutUserTaskDefPb.newBuilder();
        // todo
        Class<?> cls = taskObject.getClass();
        for (Field field : cls.getFields()) {
            if (!field.isAnnotationPresent(UserTaskField.class)) continue;

            UserTaskField utf = field.getAnnotation(UserTaskField.class);
            VariableType type = LHLibUtil.javaClassToLHVarType(field.getType());
            if (
                type == VariableType.JSON_ARR ||
                type == VariableType.JSON_OBJ ||
                type == VariableType.BYTES
            ) {
                throw new IllegalArgumentException(
                    "Only primitive types supported for UserTaskField. Field " +
                    field.getName() +
                    " is of type " +
                    type
                );
            }

            UserTaskFieldPb.Builder fieldBuilder = UserTaskFieldPb
                .newBuilder()
                .setName(field.getName())
                .setType(type);

            if (utf.description() != null && !utf.description().isEmpty()) {
                fieldBuilder.setDescription(utf.description());
            }

            fieldBuilder.setRequired(utf.required());

            if (utf.displayName() != null && !utf.displayName().isEmpty()) {
                fieldBuilder.setDisplayName(utf.displayName());
            } else {
                fieldBuilder.setDisplayName(field.getName());
            }

            out.addFields(fieldBuilder);
        }

        out.setName(userTaskDefName);

        compiled = out.build();
    }
}
