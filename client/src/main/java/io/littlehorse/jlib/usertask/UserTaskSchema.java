package io.littlehorse.jlib.usertask;

import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.proto.PutUserTaskDefPb;
import io.littlehorse.jlib.common.proto.UserTaskFieldPb;
import io.littlehorse.jlib.common.proto.VariableTypePb;
import io.littlehorse.jlib.usertask.annotations.UserTaskField;
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
            VariableTypePb type = LHLibUtil.javaClassToLHVarType(field.getType());
            if (
                type == VariableTypePb.JSON_ARR ||
                type == VariableTypePb.JSON_OBJ ||
                type == VariableTypePb.BYTES
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
                .setName(utf.name())
                .setRequired(utf.required())
                .setType(type);

            if (!"".equals(utf.description())) {
                fieldBuilder.setDescription(utf.description());
            }

            out.addFields(fieldBuilder);
        }

        out.setName(userTaskDefName);

        compiled = out.build();
    }
}
