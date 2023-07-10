package io.littlehorse.jlib.worker.internal.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.exception.InputVarSubstitutionError;
import io.littlehorse.jlib.common.exception.TaskSchemaMismatchError;
import io.littlehorse.jlib.common.proto.ScheduledTaskPb;
import io.littlehorse.jlib.common.proto.TaskDefPb;
import io.littlehorse.jlib.common.proto.VarNameAndValPb;
import io.littlehorse.jlib.common.proto.VariableDefPb;
import io.littlehorse.jlib.common.proto.VariableValuePb;
import io.littlehorse.jlib.worker.WorkerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VariableMapping {

    private String name;
    private Class<?> type;
    private int position;

    public VariableMapping(
        TaskDefPb taskDef,
        int position,
        Class<?> type,
        String javaParamName
    ) throws TaskSchemaMismatchError {
        this.type = type;

        if (type.equals(WorkerContext.class)) return;
        this.position = position;

        if (position >= taskDef.getInputVarsCount()) {
            throw new TaskSchemaMismatchError(
                "Provided Java function has more parameters than the TaskDef."
            );
        }
        this.name = javaParamName;
        VariableDefPb input = taskDef.getInputVars(position);

        String msg = null;

        switch (input.getType()) {
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
                log.info(
                    "Info: Will use Jackson to deserialize Json into {}",
                    type.getName()
                );
                break;
            case NULL:
            case UNRECOGNIZED:
                throw new RuntimeException("Not possible");
        }

        if (msg != null) {
            throw new TaskSchemaMismatchError(
                "Invalid assignment for var " + name + ": " + msg
            );
        }
    }

    public Object assign(ScheduledTaskPb taskInstance, WorkerContext context)
        throws InputVarSubstitutionError {
        if (type.equals(WorkerContext.class)) {
            return context;
        }

        VarNameAndValPb assignment = taskInstance.getVariables(position);
        String taskDefParamName = assignment.getVarName();
        VariableValuePb val = assignment.getValue();

        String jsonStr = null;

        // We've already done validation for the
        switch (val.getType()) {
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
            case NULL:
                return null;
            case JSON_OBJ:
                jsonStr = val.getJsonObj();
                break;
            case UNRECOGNIZED:
                throw new RuntimeException("Unrecognized variable value type");
        }

        try {
            return LHLibUtil.deserializeFromjson(jsonStr, type);
        } catch (JsonProcessingException exn) {
            throw new InputVarSubstitutionError(
                "Failed deserializing the Java object for variable " +
                taskDefParamName,
                exn
            );
        }
    }
}
