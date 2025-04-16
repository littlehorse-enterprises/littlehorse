package io.littlehorse.sdk.usertask;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.UserTaskField;
import io.littlehorse.sdk.common.proto.VariableType;
import java.lang.reflect.Field;

/**
 * Represents the schema for a user task.
 */
public class UserTaskSchema {

    private PutUserTaskDefRequest compiled;
    private Object taskObject;
    private String userTaskDefName;

    /**
     * Constructs a UserTaskSchema with the specified task object and user task definition name.
     *
     * @param taskObject the task object
     * @param userTaskDefName the name of the user task definition
     */
    public UserTaskSchema(Object taskObject, String userTaskDefName) {
        this.taskObject = taskObject;
        this.userTaskDefName = userTaskDefName;
    }

    /**
     * Compiles the user task schema into a PutUserTaskDefRequest.
     * - Fields in task object should be primitive types.
     * - If field has not set a `DisplayName`, it will assign the field name.
     * - Fields in user task form are `required` by default.
     * @return the compiled PutUserTaskDefRequest
     */
    public PutUserTaskDefRequest compile() {
        if (compiled == null) compileHelper();

        return compiled;
    }

    private void compileHelper() {
        PutUserTaskDefRequest.Builder out = PutUserTaskDefRequest.newBuilder();
        // todo
        Class<?> cls = taskObject.getClass();
        for (Field field : cls.getFields()) {
            if (!field.isAnnotationPresent(io.littlehorse.sdk.usertask.annotations.UserTaskField.class)) continue;

            io.littlehorse.sdk.usertask.annotations.UserTaskField utf =
                    field.getAnnotation(io.littlehorse.sdk.usertask.annotations.UserTaskField.class);
            VariableType type = LHLibUtil.javaClassToLHVarType(field.getType());
            if (type == VariableType.JSON_ARR || type == VariableType.JSON_OBJ || type == VariableType.BYTES) {
                throw new IllegalArgumentException("Only primitive types supported for UserTaskField. Field "
                        + field.getName()
                        + " is of type "
                        + type);
            }

            UserTaskField.Builder fieldBuilder =
                    UserTaskField.newBuilder().setName(field.getName()).setType(type);

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
