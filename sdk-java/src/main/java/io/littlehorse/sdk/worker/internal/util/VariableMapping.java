package io.littlehorse.sdk.worker.internal.util;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.InputVarSubstitutionException;
import io.littlehorse.sdk.common.exception.LHJsonProcessingException;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.WorkerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VariableMapping {

    private String name;
    private Class<?> type;
    private int position;

    public VariableMapping(TaskDef taskDef, int position, Class<?> type, String javaParamName)
            throws TaskSchemaMismatchError {
        this.type = type;

        if (type.equals(WorkerContext.class)) return;
        this.position = position;

        if (position >= taskDef.getInputVarsCount()) {
            throw new TaskSchemaMismatchError("Provided Java function has more parameters than the TaskDef.");
        }
        this.name = javaParamName;
        VariableDef input = taskDef.getInputVars(position);

        String msg = null;

        switch (input.getTypeDef().getPrimitiveType()) {
            case INT:
                if (!LHLibUtil.isINT(type)) {
                    msg = "TaskDef provides INT, func accepts " + type.getName();
                }
                break;
            case DOUBLE:
                if (!LHLibUtil.isDOUBLE(type)) {
                    msg = "TaskDef provides a DOUBLE, func accepts " + type.getName();
                }
                break;
            case STR:
                if (!LHLibUtil.isSTR(type)) {
                    msg = "TaskDef provides a STRING, func accepts " + type.getName();
                }
                break;
            case BOOL:
                if (!LHLibUtil.isBOOL(type)) {
                    msg = "TaskDef provides a BOOL, func accepts " + type.getName();
                }
                break;
            case BYTES:
                if (!LHLibUtil.isBYTES(type)) {
                    msg = "TaskDef provides BYTES, func accepts " + type.getName();
                }
                break;
            case JSON_ARR:
            case JSON_OBJ:
                log.info("Info: Will use Gson to deserialize Json into {}", type.getName());
                break;
            case UNRECOGNIZED:
                throw new RuntimeException("Not possible");
        }

        if (msg != null) {
            throw new TaskSchemaMismatchError("Invalid assignment for var " + name + ": " + msg);
        }
    }

    public Object assign(ScheduledTask taskInstance, WorkerContext context) throws InputVarSubstitutionException {
        if (type.equals(WorkerContext.class)) {
            return context;
        }

        VarNameAndVal assignment = taskInstance.getVariables(position);
        String taskDefParamName = assignment.getVarName();
        VariableValue val = assignment.getValue();

        String jsonStr = null;

        // We've already done validation for the
        switch (val.getValueCase()) {
            case INT:
                if (type == Long.class || type == long.class) {
                    return val.getInt();
                } else {
                    return (int) val.getInt();
                }
            case DOUBLE:
                if (type == Double.class || type == double.class) {
                    return val.getDouble();
                } else {
                    return (float) val.getDouble();
                }
            case STR:
                return val.getStr();
            case BYTES:
                return val.getBytes().toByteArray();
            case BOOL:
                return val.getBool();
            case JSON_ARR:
                jsonStr = val.getJsonArr();
                break;
            case JSON_OBJ:
                jsonStr = val.getJsonObj();
                break;
            case VALUE_NOT_SET:
                return null;
        }

        try {
            return LHLibUtil.deserializeFromjson(jsonStr, type);
        } catch (LHJsonProcessingException exn) {
            throw new InputVarSubstitutionException(
                    "Failed deserializing the Java object for variable " + taskDefParamName, exn);
        }
    }
}
