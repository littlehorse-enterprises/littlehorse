package io.littlehorse.sdk.worker.internal.util;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHTypeAdapter;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.InputVarSubstitutionException;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.InlineStruct;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VariableMapping {

    private String name;
    private Class<?> type;
    private int position;
    private LHTypeAdapterRegistry typeAdapterRegistry;
    private String inlineStructDefName;

    public VariableMapping(
            TaskDef taskDef, int position, Class<?> type, String javaParamName, List<LHTypeAdapter<?>> typeAdapters)
            throws TaskSchemaMismatchError {
        this(taskDef, position, type, javaParamName, null, LHTypeAdapterRegistry.from(typeAdapters));
    }

    public VariableMapping(
            TaskDef taskDef,
            int position,
            Class<?> type,
            String javaParamName,
            LHTypeAdapterRegistry typeAdapterRegistry)
            throws TaskSchemaMismatchError {
        this(taskDef, position, type, javaParamName, null, typeAdapterRegistry);
    }

    public VariableMapping(
            TaskDef taskDef,
            int position,
            Class<?> type,
            String javaParamName,
            String inlineStructDefName,
            LHTypeAdapterRegistry typeAdapterRegistry)
            throws TaskSchemaMismatchError {
        this.type = type;
        this.typeAdapterRegistry = Objects.requireNonNull(typeAdapterRegistry, "Type adapter registry cannot be null");
        this.inlineStructDefName = inlineStructDefName;

        if (type.equals(WorkerContext.class)) return;
        this.position = position;

        if (position >= taskDef.getInputVarsCount()) {
            throw new TaskSchemaMismatchError("Provided Java function has more parameters than the TaskDef.");
        }
        this.name = javaParamName;
        TypeDefinition inputType = taskDef.getInputVars(position).getTypeDef();

        try {
            validateParamAgainstTaskDef(inputType);
        } catch (TaskSchemaMismatchError taskSchemaMismatchError) {
            throw new TaskSchemaMismatchError(
                    "Invalid assignment for var " + name + ": " + taskSchemaMismatchError.getMessage());
        }
    }

    private void validateParamAgainstTaskDef(TypeDefinition paramType) {

        switch (paramType.getDefinedTypeCase()) {
            case PRIMITIVE_TYPE:
                validatePrimitiveType(paramType.getPrimitiveType(), type);
                break;
            case STRUCT_DEF_ID:
                validateStructDefType(paramType.getStructDefId(), type);
                break;
            case DEFINEDTYPE_NOT_SET:
            default:
                break;
        }
    }

    private void validateStructDefType(StructDefId input, Class<?> type) {
        if (InlineStruct.class.equals(type)) {
            if (inlineStructDefName == null || inlineStructDefName.isBlank()) {
                throw new TaskSchemaMismatchError(
                        "TaskDef provides StructDef, func accepts InlineStruct without @LHType(structDefName = ...)");
            } else if (!input.getName().equals(inlineStructDefName)) {
                throw new TaskSchemaMismatchError(String.format(
                        "TaskDef provides StructDef <%s>, func expects InlineStruct StructDef <%s>",
                        input.getName(), inlineStructDefName));
            }
        }

        LHClassType lhClassType = LHClassType.fromJavaClass(type, typeAdapterRegistry);

        if (!(lhClassType instanceof LHStructDefType)) {
            throw new TaskSchemaMismatchError("TaskDef provides StructDef, func accepts non-StructDef type " + type);
        }

        LHStructDefType lhStructDefType = (LHStructDefType) lhClassType;

        if (!input.equals(lhStructDefType.getStructDefId())) {
            throw new TaskSchemaMismatchError(String.format(
                    "TaskDef provides StructDef <%s>, func accepts StructDef <%s>",
                    input, lhStructDefType.getStructDefId()));
        }
    }

    private void validatePrimitiveType(VariableType input, Class<?> type) {
        Optional<LHTypeAdapter<?>> maybeAdapter = LHLibUtil.getTypeAdapterForClass(type, typeAdapterRegistry);
        if (maybeAdapter.isPresent()) {
            LHTypeAdapter<?> adapter = maybeAdapter.get();
            if (adapter.getVariableType() != input) {
                throw new TaskSchemaMismatchError("TaskDef provides " + input + ", but adapter for " + type.getName()
                        + " maps to " + adapter.getVariableType());
            }
            return;
        }

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
            default:
                msg = "TaskDef primitive input type unrecognized. Client versiom might be older than server.";
                break;
        }

        if (msg != null) {
            throw new TaskSchemaMismatchError(msg);
        }
    }

    public Object assign(ScheduledTask taskInstance, WorkerContext context) throws InputVarSubstitutionException {
        if (type.equals(WorkerContext.class)) {
            return context;
        }

        VarNameAndVal assignment = taskInstance.getVariables(position);
        String taskDefParamName = assignment.getVarName();

        VariableValue val = assignment.getValue();

        try {
            return LHLibUtil.varValToObj(val, this.type, this.typeAdapterRegistry);
        } catch (LHSerdeException e) {
            throw new InputVarSubstitutionException(
                    "Failed serializing Java object for variable: " + taskDefParamName, e);
        }
    }
}
