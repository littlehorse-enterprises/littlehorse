package io.littlehorse.sdk.worker.internal.util;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.InputVarSubstitutionException;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHClassType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.WorkerContext;
import java.util.Optional;
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
        TypeDefinition inputType = taskDef.getInputVars(position).getTypeDef();

        Optional<String> msg = null;

        switch (inputType.getDefinedTypeCase()) {
            case DEFINEDTYPE_NOT_SET:
                break;
            case PRIMITIVE_TYPE:
                msg = validatePrimitiveType(inputType.getPrimitiveType(), type);
                break;
            case STRUCT_DEF_ID:
                msg = validateStructDefType(inputType.getStructDefId(), type);
                break;
            default:
                break;
        }

        if (msg.isPresent()) {
            throw new TaskSchemaMismatchError("Invalid assignment for var " + name + ": " + msg.get());
        }
    }

    private Optional<String> validateStructDefType(StructDefId input, Class<?> type) {
        String msg = null;

        LHClassType lhClassType = LHClassType.fromJavaClass(type);

        if (!(lhClassType instanceof LHStructDefType)) {
            msg = "TaskDef provides StructDef, func accepts non-StructDef type " + type;
        }

        LHStructDefType lhStructDefType = (LHStructDefType) lhClassType;

        if (!input.equals(lhStructDefType.getStructDefId())) {
            msg = String.format(
                    "TaskDef provides StructDef <%s>, func accepts StructDef <%s>",
                    input, lhStructDefType.getStructDefId());
        }

        return Optional.ofNullable(msg);
    }

    private Optional<String> validatePrimitiveType(VariableType input, Class<?> type) {
        String msg = null;
        switch (input) {
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
            case WF_RUN_ID:
                if (!LHLibUtil.isWfRunId(type)) {
                    msg = "TaskDef provides WF_RUN_ID, func accepts " + type.getName();
                }
                break;
            case TIMESTAMP:
                if (!LHLibUtil.isTIMESTAMP(type)) {
                    msg = "TaskDef provides a TIMESTAMP, func accepts " + type.getName();
                }
                break;
            case UNRECOGNIZED:
                throw new RuntimeException("Not possible");
            default:
                break;
        }
        return Optional.ofNullable(msg);
    }

    public Object assign(ScheduledTask taskInstance, WorkerContext context) throws InputVarSubstitutionException {
        if (type.equals(WorkerContext.class)) {
            return context;
        }

        VarNameAndVal assignment = taskInstance.getVariables(position);
        String taskDefParamName = assignment.getVarName();

        VariableValue val = assignment.getValue();

        try {
            return LHLibUtil.varValToObj(val, this.type);
        } catch (LHSerdeException e) {
            throw new InputVarSubstitutionException(
                    "Failed serializing Java object for variable: " + taskDefParamName, e);
        }
    }
}
