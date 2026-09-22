package io.littlehorse.sdk.usertask;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.UserTaskField;
import io.littlehorse.sdk.common.proto.VariableType;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Represents the schema for a user task.
 */
public class UserTaskSchema {

    private PutUserTaskDefRequest compiled;
    private Object taskObject;
    private String userTaskDefName;
    private StructDefId resultStructDefId;

    /**
     * Constructs a UserTaskSchema with the specified task object and user task definition name.
     *
     * @param taskObject the task object
     * @param userTaskDefName the name of the user task definition
     */
    @Deprecated
    public UserTaskSchema(Object taskObject, String userTaskDefName) {
        this.taskObject = taskObject;
        this.userTaskDefName = userTaskDefName;
    }

    /**
     * Constructs a strongly typed UserTaskSchema backed by an exact StructDef version.
     *
     * @param resultStructDefId the StructDef defining the UserTaskRun output
     * @param userTaskDefName the name of the UserTaskDef
     */
    public UserTaskSchema(StructDefId resultStructDefId, String userTaskDefName) {
        this.resultStructDefId = Objects.requireNonNull(resultStructDefId);
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
        PutUserTaskDefRequest.Builder out = PutUserTaskDefRequest.newBuilder().setName(userTaskDefName);
        if (resultStructDefId != null) {
            compiled = out.setResultStructDefId(resultStructDefId).build();
            return;
        }
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

        compiled = out.build();
    }
}
