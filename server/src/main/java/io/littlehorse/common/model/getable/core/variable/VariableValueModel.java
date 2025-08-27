package io.littlehorse.common.model.getable.core.variable;

import com.google.gson.JsonSyntaxException;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.Type;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.PathNotFoundException;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.VariableValue.ValueCase;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@Getter
public class VariableValueModel extends LHSerializable<VariableValue> {

    private ValueCase valueType;
    private Map<String, Object> jsonObjVal;
    private List<Object> jsonArrVal;
    private Double doubleVal;
    private Boolean boolVal;
    private String strVal;
    private Long intVal;
    private byte[] bytesVal;
    private WfRunIdModel wfRunId;
    private StructModel struct;

    private ExecutionContext context;

    @Getter(AccessLevel.NONE)
    private String deserializationError;

    private String jsonStr;

    public static VariableValueModel fromProto(VariableValue proto, ExecutionContext context) {
        VariableValueModel out = new VariableValueModel();
        out.initFrom(proto, context);
        return out;
    }

    public Optional<String> getDeserializationError() {
        return Optional.ofNullable(deserializationError);
    }

    @Override
    public Class<VariableValue> getProtoBaseClass() {
        return VariableValue.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        this.context = context;
        VariableValue p = (VariableValue) proto;
        valueType = p.getValueCase();
        switch (valueType) {
            case JSON_ARR:
                try {
                    jsonArrVal = LHUtil.strToJsonArr(p.getJsonArr());
                } catch (JsonSyntaxException jsonSyntaxException) {
                    jsonStr = p.getJsonArr();
                    this.deserializationError = "Error deserializing JSON Arr";
                    jsonArrVal = new ArrayList<Object>();
                }
                break;
            case JSON_OBJ:
                try {
                    jsonObjVal = LHUtil.strToJsonObj(p.getJsonObj());
                } catch (JsonSyntaxException jsonSyntaxException) {
                    jsonStr = p.getJsonObj();
                    this.deserializationError = "Error deserializing JSON Obj";
                    jsonObjVal = new HashMap<String, Object>();
                }
                break;
            case DOUBLE:
                doubleVal = p.getDouble();
                break;
            case BOOL:
                boolVal = p.getBool();
                break;
            case STR:
                strVal = p.getStr();
                break;
            case INT:
                intVal = p.getInt();
                break;
            case BYTES:
                bytesVal = p.getBytes().toByteArray();
                break;
            case WF_RUN_ID:
                wfRunId = WfRunIdModel.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
                break;
            case STRUCT:
                struct = StructModel.fromProto(p.getStruct(), StructModel.class, context);
                break;
            case VALUE_NOT_SET:
                // it's a null variable! Nothing to do.
                break;
        }
    }

    public TypeDefinitionModel getTypeDefinition() {
        if (this.valueType == ValueCase.STRUCT) {
            return new TypeDefinitionModel(this.struct.getStructDefId());
        }

        VariableType primitiveType = fromValueCase(valueType);

        if (primitiveType == null) return new TypeDefinitionModel();

        return new TypeDefinitionModel(primitiveType);
    }

    private String getJsonString() throws LHVarSubError {
        if (valueType == ValueCase.JSON_ARR) {
            return toProto().getJsonArr();
        } else if (valueType == ValueCase.JSON_OBJ) {
            return toProto().getJsonObj();
        } else {
            throw new IllegalStateException(
                    "This is a bug: Variable is of type " + valueType + " but asked for json str from that variable.");
        }
    }

    private static VariableType fromValueCase(ValueCase valueCase) {
        switch (valueCase) {
            case STR:
                return VariableType.STR;
            case BYTES:
                return VariableType.BYTES;
            case INT:
                return VariableType.INT;
            case DOUBLE:
                return VariableType.DOUBLE;
            case JSON_ARR:
                return VariableType.JSON_ARR;
            case JSON_OBJ:
                return VariableType.JSON_OBJ;
            case BOOL:
                return VariableType.BOOL;
            case WF_RUN_ID:
                return VariableType.WF_RUN_ID;
            case VALUE_NOT_SET:
            default:
                return null;
        }
    }

    public void updateJsonViaJsonPath(String jsonPath, Object toPut) throws LHVarSubError {
        String jsonString = getJsonString();
        String newJsonString;
        try {
            ParseContext parser =
                    JsonPath.using(Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL));
            DocumentContext jsonDoc = parser.parse(jsonString);

            newJsonString = jsonDoc.set(jsonPath, toPut).jsonString();
        } catch (Exception jsonExn) {
            throw new LHVarSubError(
                    jsonExn,
                    "Failed updating jsonPath " + jsonPath + " on object " + jsonString + ": " + jsonExn.getMessage());
        }

        if (getTypeDefinition().getPrimitiveType() == VariableType.JSON_ARR) {
            jsonArrVal = LHUtil.strToJsonArr(newJsonString);
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.JSON_OBJ) {
            jsonObjVal = LHUtil.strToJsonObj(newJsonString);
        }
    }

    public VariableValue.Builder toProto() {
        VariableValue.Builder out = VariableValue.newBuilder();
        switch (valueType) {
            case JSON_ARR:
                if (this.deserializationError != null) {
                    out.setJsonArr((this.jsonStr));
                } else if (jsonArrVal != null) {
                    out.setJsonArr(LHUtil.objToString(jsonArrVal));
                }
                break;
            case JSON_OBJ:
                if (this.deserializationError != null) {
                    out.setJsonObj(this.jsonStr);
                } else if (jsonObjVal != null) {
                    out.setJsonObj(LHUtil.objToString(jsonObjVal));
                }
                break;
            case DOUBLE:
                if (doubleVal != null) out.setDouble(doubleVal);
                break;
            case BOOL:
                if (boolVal != null) out.setBool(boolVal);
                break;
            case STR:
                if (strVal != null) out.setStr(strVal);
                break;
            case INT:
                if (intVal != null) out.setInt(intVal);
                break;
            case BYTES:
                if (bytesVal != null) {
                    out.setBytes(ByteString.copyFrom(bytesVal));
                }
                break;
            case WF_RUN_ID:
                if (wfRunId != null) {
                    out.setWfRunId(wfRunId.toProto());
                }
                break;
            case STRUCT:
                if (struct != null) {
                    out.setStruct(struct.toProto());
                }
            case VALUE_NOT_SET:
                // nothing to do
                break;
        }

        return out;
    }

    public VariableValueModel getCopy() {
        VariableValueModel out = new VariableValueModel();
        out.initFrom(toProto().build(), context);
        return out;
    }

    /**
     * Returns true if the value is empty, which is the LittleHorse equivalent of `null`.
     */
    public boolean isEmpty() {
        return valueType == ValueCase.VALUE_NOT_SET;
    }

    public VariableValueModel operate(
            VariableMutationType operation, VariableValueModel rhs, TypeDefinitionModel typeToCoerceTo)
            throws LHVarSubError {

        if (operation == VariableMutationType.ASSIGN) {
            if (rhs.isNull()) {
                return new VariableValueModel();
            } else {
                return rhs.coerceToType(typeToCoerceTo);
            }
        }

        if (typeToCoerceTo.getDefinedTypeCase() != DefinedTypeCase.DEFINEDTYPE_NOT_SET && typeToCoerceTo.getDefinedTypeCase() != DefinedTypeCase.PRIMITIVE_TYPE) {
            throw new RuntimeException("Unsupported operation: " + operation);
        }

        if (operation == VariableMutationType.ADD) {
            return typeToCoerceTo.getPrimitiveType() == VariableType.DOUBLE
                    ? asDouble().add(rhs)
                    : add(rhs);
        } else if (operation == VariableMutationType.SUBTRACT) {
            return typeToCoerceTo.getPrimitiveType() == VariableType.DOUBLE
                    ? asDouble().subtract(rhs)
                    : subtract(rhs);
        } else if (operation == VariableMutationType.MULTIPLY) {
            return typeToCoerceTo.getPrimitiveType() == VariableType.DOUBLE
                    ? asDouble().multiply(rhs)
                    : multiply(rhs);
        } else if (operation == VariableMutationType.DIVIDE) {
            return typeToCoerceTo.getPrimitiveType() == VariableType.DOUBLE
                    ? asDouble().divide(rhs)
                    : divide(rhs);
        } else if (operation == VariableMutationType.EXTEND) {
            return extend(rhs);
        } else if (operation == VariableMutationType.REMOVE_IF_PRESENT) {
            return removeIfPresent(rhs);
        } else if (operation == VariableMutationType.REMOVE_INDEX) {
            return removeIndex(rhs);
        } else if (operation == VariableMutationType.REMOVE_KEY) {
            return removeKey(rhs);
        }
        throw new RuntimeException("Unsupported operation: " + operation);
    }

    public boolean isNull() {
        return valueType == ValueCase.VALUE_NOT_SET;
    }

    public VariableValueModel jsonPath(String path) throws LHVarSubError {
        Object val;
        String jsonStr;
        if (getTypeDefinition().getPrimitiveType() == VariableType.JSON_ARR) {
            jsonStr = LHUtil.objToString(jsonArrVal);
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.JSON_OBJ) {
            jsonStr = LHUtil.objToString(jsonObjVal);
        } else {
            throw new LHVarSubError(null, "Cannot jsonpath on " + valueType);
        }

        try {
            val = JsonPath.parse(jsonStr).read(path);
        } catch (PathNotFoundException exn) {
            return new VariableValueModel();
        } catch (Exception exn) {
            exn.printStackTrace();
            throw new LHVarSubError(
                    exn, "Failed accessing path " + path + " on data " + jsonStr + "  :\n" + exn.getMessage());
        }

        if (val == null) {
            // We do not differentiate between the key not being there and the key being explicitly
            // set to the value of null.
            return new VariableValueModel();
        }

        if (Long.class.isAssignableFrom(val.getClass())) {
            return new VariableValueModel((long) val);
        } else if (Integer.class.isAssignableFrom(val.getClass())) {
            return new VariableValueModel(Long.valueOf((long) ((Integer) val)));
        } else if (String.class.isAssignableFrom(val.getClass())) {
            return new VariableValueModel((String) val);
        } else if (Boolean.class.isAssignableFrom(val.getClass())) {
            return new VariableValueModel((Boolean) val);
        } else if (Double.class.isAssignableFrom(val.getClass())) {
            return new VariableValueModel((Double) val);
        } else if (Map.class.isAssignableFrom(val.getClass())) {
            return new VariableValueModel((Map<String, Object>) val);
        } else if (List.class.isAssignableFrom(val.getClass())) {
            return new VariableValueModel((List<Object>) val);
        } else {
            log.error("Not possible to get this from jsonpath {}={}", val, val.getClass());
            throw new RuntimeException("Not possible to get this from jsonpath");
        }
    }

    public VariableValueModel add(VariableValueModel rhs) throws LHVarSubError {
        if (rhs.getTypeDefinition().getPrimitiveType() == null) {
            throw new LHVarSubError(null, "Cannot add by null");
        }
        if (getTypeDefinition().getPrimitiveType() == VariableType.INT) {
            if (getTypeDefinition().getPrimitiveType() == VariableType.DOUBLE) {
                return new VariableValueModel(asDouble().doubleVal + rhs.asDouble().doubleVal);
            }
            return new VariableValueModel(asInt().intVal + rhs.asInt().intVal);
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.DOUBLE) {
            return new VariableValueModel(asDouble().doubleVal + rhs.asDouble().doubleVal);
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.STR) {
            return new VariableValueModel(asStr().strVal + rhs.asStr().strVal);
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.JSON_ARR) {
            List<Object> jsonList = getJsonArrVal();
            List<Object> newList = new ArrayList<>();
            newList.addAll(jsonList);
            newList.add(rhs.getVal());
            return new VariableValueModel(newList);
        }
        throw new LHVarSubError(null, "Cannot add to var of type " + valueType);
    }

    public VariableValueModel subtract(VariableValueModel rhs) throws LHVarSubError {
        if (rhs.getTypeDefinition().getPrimitiveType() == null) {
            throw new LHVarSubError(null, "Cannot subtract null");
        }
        if (getTypeDefinition().getPrimitiveType() == VariableType.INT) {
            if (rhs.getTypeDefinition().getPrimitiveType() == VariableType.DOUBLE) {
                return new VariableValueModel((long) (asDouble().doubleVal - rhs.asDouble().doubleVal));
            }
            return new VariableValueModel(asInt().intVal - rhs.asInt().intVal);
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.DOUBLE) {
            return new VariableValueModel(asDouble().doubleVal - rhs.asDouble().doubleVal);
        }
        throw new LHVarSubError(null, "Cannot subtract from var of type " + valueType);
    }

    public VariableValueModel multiply(VariableValueModel rhs) throws LHVarSubError {
        if (rhs.getTypeDefinition().getPrimitiveType() == null) {
            throw new LHVarSubError(null, "Cannot multiply by null");
        }
        if (getTypeDefinition().getPrimitiveType() == VariableType.INT) {
            if (rhs.getTypeDefinition().getPrimitiveType() == VariableType.DOUBLE) {
                return new VariableValueModel((long) (asDouble().doubleVal * rhs.asDouble().doubleVal));
            }
            return new VariableValueModel((long) (intVal * rhs.asInt().intVal));
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.DOUBLE) {
            return new VariableValueModel((double) (asDouble().doubleVal * rhs.asDouble().doubleVal));
        }
        throw new LHVarSubError(null, "Cannot multiply value of type " + valueType);
    }

    public VariableValueModel divide(VariableValueModel rhs) throws LHVarSubError {
        if (rhs.getTypeDefinition().getPrimitiveType() == null) {
            throw new LHVarSubError(null, "Cannot divide by null");
        }

        if (rhs.asDouble().doubleVal == 0) {
            throw new LHVarSubError(null, "Cannot divide by zero");
        }

        if (getTypeDefinition().getPrimitiveType() == VariableType.INT) {
            if (rhs.getTypeDefinition().getPrimitiveType() == VariableType.DOUBLE) {
                return new VariableValueModel((long) (asDouble().doubleVal / rhs.asDouble().doubleVal));
            } else {
                return new VariableValueModel((long) (intVal / rhs.asInt().intVal));
            }
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.DOUBLE) {
            return new VariableValueModel((double) (asDouble().doubleVal / rhs.asDouble().doubleVal));
        }
        throw new LHVarSubError(null, "Cannot divide var of type " + valueType);
    }

    public VariableValueModel extend(VariableValueModel rhs) throws LHVarSubError {
        if (getTypeDefinition().getPrimitiveType() == VariableType.JSON_ARR) {
            List<Object> newList = new ArrayList<>();
            newList.addAll(asArr().jsonArrVal);
            newList.addAll(rhs.asArr().jsonArrVal);
            return new VariableValueModel(newList);
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.BYTES) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                if (bytesVal != null) baos.write(bytesVal);
                rhs = rhs.asBytes();
                if (rhs.bytesVal != null) baos.write(rhs.bytesVal);
            } catch (IOException exn) {
                throw new LHVarSubError(exn, "Failed concatenating bytes");
            }
            return new VariableValueModel(baos.toByteArray());
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.STR) {
            return new VariableValueModel(this.strVal + rhs.asStr().strVal);
        }
        throw new LHVarSubError(null, "Cannot extend var of type " + valueType);
    }

    public VariableValueModel removeIfPresent(VariableValueModel other) throws LHVarSubError {
        List<Object> lhsList = asArr().jsonArrVal;
        Object o = other.getVal();
        lhsList.removeIf(i -> {
            return isEqual(i, o);
        });
        return new VariableValueModel(lhsList);
    }

    private boolean isEqual(Object a, Object b) {
        if (a instanceof Integer) {
            a = ((Integer) a).longValue();
        }
        if (b instanceof Integer) {
            b = ((Integer) b).longValue();
        }
        return Objects.equals(a, b);
    }

    public VariableValueModel removeIndex(VariableValueModel other) throws LHVarSubError {
        List<Object> lhsList = asArr().jsonArrVal;
        Long longIdx = other.asInt().intVal;
        if (longIdx == null) {
            throw new LHVarSubError(null, "Tried to remove null index");
        }
        int idx = longIdx.intValue();
        lhsList.remove(idx);
        return new VariableValueModel(lhsList);
    }

    public VariableValueModel removeKey(VariableValueModel other) throws LHVarSubError {
        Map<String, Object> m = asObj().jsonObjVal;
        m.remove(other.asStr().strVal);
        return new VariableValueModel(m);
    }

    public Object getVal() {
        switch (valueType) {
            case INT:
                return this.intVal;
            case DOUBLE:
                return this.doubleVal;
            case STR:
                return this.strVal;
            case BOOL:
                return this.boolVal;
            case JSON_ARR:
                return this.jsonArrVal;
            case JSON_OBJ:
                return this.jsonObjVal;
            case BYTES:
                return this.bytesVal;
            case WF_RUN_ID:
                return this.wfRunId;
            case STRUCT:
                return this.struct;
            case VALUE_NOT_SET:
            default:
                break;
        }
        return null;
    }

    public VariableValueModel coerceToType(TypeDefinitionModel otherType) throws LHVarSubError {
        if (getTypeDefinition().isNull()) {
            throw new LHVarSubError(null, "Coercing from NULL not supported.");
        } else if (otherType.isNull()) {
            throw new LHVarSubError(null, "Coercing to NULL not supported.");
        }

        if (getTypeDefinition().getDefinedTypeCase() != otherType.getDefinedTypeCase()) {
            throw new LHVarSubError(
                    null,
                    "Coercing from " + getTypeDefinition() + " to " + otherType + " or vice versa not supported.");
        }

        switch (getTypeDefinition().getDefinedTypeCase()) {
            case PRIMITIVE_TYPE:
                VariableType primitiveType = getTypeDefinition().getPrimitiveType();
                VariableType otherPrimitiveType = otherType.getPrimitiveType();
                if (primitiveType == null || otherPrimitiveType == null) {
                    throw new LHVarSubError(null, "Coercing to or from NULL not supported.");
                }

                if (otherPrimitiveType == VariableType.INT) {
                    return asInt();
                } else if (otherPrimitiveType == VariableType.DOUBLE) {
                    return asDouble();
                } else if (otherPrimitiveType == VariableType.BOOL) {
                    return asBool();
                } else if (otherPrimitiveType == VariableType.STR) {
                    return asStr();
                } else if (otherPrimitiveType == VariableType.JSON_ARR) {
                    return asArr();
                } else if (otherPrimitiveType == VariableType.JSON_OBJ) {
                    return asObj();
                } else if (otherPrimitiveType == VariableType.BYTES) {
                    return asBytes();
                } else if (otherPrimitiveType == VariableType.WF_RUN_ID) {
                    return asWfRunId();
                } else {
                    throw new LHVarSubError(null, "Unsupported type for coersion: " + otherType);
                }
            case STRUCT_DEF_ID:
                if (otherType.getStructDefId().equals(getTypeDefinition().getStructDefId())) {
                    return asStruct();
                }
                break;
            case DEFINEDTYPE_NOT_SET:
            case INLINE_ARRAY_DEF:
            default:
        }

        throw new LHVarSubError(
                null,
                "Coercing from " + getTypeDefinition().getDefinedTypeCase() + "to " + otherType + " not supported.");
    }

    public VariableValueModel asInt() throws LHVarSubError {
        Long out = null;

        if (getTypeDefinition().getPrimitiveType() == VariableType.INT) {
            out = intVal;
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.DOUBLE) {
            out = doubleVal == null ? null : doubleVal.longValue();
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.STR) {
            try {
                out = strVal == null ? null : Long.valueOf(strVal);
            } catch (Exception exn) {
                throw new LHVarSubError(exn, "Couldn't convert strVal to INT");
            }
        } else {
            String typeDescription = valueType == ValueCase.VALUE_NOT_SET ? "NULL" : valueType.toString();
            throw new LHVarSubError(null, "Cant convert " + typeDescription + " to INT");
        }

        if (out == null) {
            // If this happens, then there is a seriously impossible bug.
            throw new IllegalStateException("Should be impossible for out to be null");
        }

        return new VariableValueModel(out);
    }

    public VariableValueModel asDouble() throws LHVarSubError {
        if (getTypeDefinition().getPrimitiveType() == VariableType.INT) {
            return new VariableValueModel(Double.valueOf(intVal));
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.DOUBLE) {
            return new VariableValueModel(doubleVal);
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.STR) {
            try {
                return new VariableValueModel(Double.parseDouble(strVal));
            } catch (Exception exn) {
                throw new LHVarSubError(exn, "Couldn't convert STR to DOUBLE");
            }
        } else {
            throw new LHVarSubError(null, "Cant convert " + valueType + " to DOUBLE");
        }
    }

    public VariableValueModel asWfRunId() throws LHVarSubError {
        if (getTypeDefinition().getPrimitiveType() == VariableType.WF_RUN_ID) {
            return new VariableValueModel(wfRunId);
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.DOUBLE) {
            return new VariableValueModel(new WfRunIdModel(String.valueOf(doubleVal)));
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.INT) {
            return new VariableValueModel(new WfRunIdModel(String.valueOf(intVal)));
        } else if (getTypeDefinition().getPrimitiveType() == VariableType.STR) {
            return new VariableValueModel((WfRunIdModel) WfRunIdModel.fromString(strVal, WfRunIdModel.class));
        } else {
            throw new LHVarSubError(null, "Cant convert " + valueType + " to WF_RUN_ID");
        }
    }

    public VariableValueModel asStruct() throws LHVarSubError {
        if (getTypeDefinition().getDefinedTypeCase() == DefinedTypeCase.STRUCT_DEF_ID) {
            return new VariableValueModel(struct);
        } else {
            throw new LHVarSubError(null, "Cant convert " + this.getTypeDefinition() + " to STRUCT");
        }
    }

    public VariableValueModel asBool() throws LHVarSubError {
        if (getTypeDefinition().getPrimitiveType() != VariableType.BOOL) {
            throw new LHVarSubError(null, "Unsupported converting to bool");
        }
        return getCopy();
    }

    public VariableValueModel asStr() throws LHVarSubError {
        if (valueType == ValueCase.VALUE_NOT_SET) return new VariableValueModel();

        return new VariableValueModel(getVal().toString());
    }

    public VariableValueModel asArr() throws LHVarSubError {
        if (getTypeDefinition().getPrimitiveType() != VariableType.JSON_ARR) {
            throw new LHVarSubError(null, "Converting to JSON_ARR not supported.");
        }
        return getCopy();
    }

    public VariableValueModel asObj() throws LHVarSubError {
        if (getTypeDefinition().getPrimitiveType() != VariableType.JSON_OBJ) {
            throw new LHVarSubError(null, "Converting to JSON_OBJ not supported.");
        }
        return getCopy();
    }

    public VariableValueModel asBytes() throws LHVarSubError {
        byte[] b;
        if (getTypeDefinition().getPrimitiveType() == VariableType.BYTES) {
            b = bytesVal;
        } else {
            b = LHUtil.objToBytes(getVal());
        }
        return new VariableValueModel(b);
    }

    public VariableValueModel() {
        valueType = ValueCase.VALUE_NOT_SET;
    }

    public VariableValueModel(long val) {
        intVal = val;
        valueType = ValueCase.INT;
    }

    public VariableValueModel(double val) {
        doubleVal = val;
        valueType = ValueCase.DOUBLE;
    }

    public VariableValueModel(String val) {
        strVal = val;
        valueType = ValueCase.STR;
    }

    public VariableValueModel(byte[] bytes) {
        bytesVal = bytes;
        valueType = ValueCase.BYTES;
    }

    public VariableValueModel(List<Object> val) {
        jsonArrVal = val;
        valueType = ValueCase.JSON_ARR;
    }

    public VariableValueModel(Map<String, Object> val) {
        jsonObjVal = val;
        valueType = ValueCase.JSON_OBJ;
    }

    public VariableValueModel(boolean val) {
        valueType = ValueCase.BOOL;
        boolVal = val;
    }

    public VariableValueModel(WfRunIdModel wfRunId) {
        valueType = ValueCase.WF_RUN_ID;
        this.wfRunId = wfRunId;
    }

    public VariableValueModel(StructModel struct) {
        valueType = ValueCase.STRUCT;
        this.struct = struct;
    }

    /*
     * Returns a pair of String, String that can be used to find the Variable via
     * a Tag Search. If not supported, returns null.
     */
    public Pair<String, String> getValueTagPair() {
        // EMPLOYEE_TODO: provide a way in the WfSpec to distinguish between hot
        // variables and non-hot variables.
        // Non-hot variables should have special index entries which are partitioned
        // according to the tags; hot variables should be of type LOCAL_COUNTED or
        // LOCAL_UNCOUNTED.

        Pair<String, String> valuePair = null;
        switch (valueType) {
            case INT:
                valuePair = Pair.of("intVal", LHUtil.toLhDbFormat(intVal));
                break;
            case DOUBLE:
                valuePair = Pair.of("doubleVal", LHUtil.toLhDbFormat(doubleVal));
                break;
            case STR:
                // EMPLOYEE_TODO: probably want to hash the values here.
                valuePair = Pair.of("strVal", strVal);
                break;
            case BOOL:
                valuePair = Pair.of("boolVal", String.valueOf(boolVal));
                break;
            case JSON_OBJ:
                valuePair = Pair.of("jsonObj", String.valueOf(jsonObjVal.toString()));
                break;
            case BYTES:
            case JSON_ARR:
            case WF_RUN_ID:
            case VALUE_NOT_SET:
                valuePair = null;
        }
        return valuePair;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof VariableValueModel)) return false;
        VariableValueModel o = (VariableValueModel) other;

        if (o.getTypeDefinition().getPrimitiveType() != getTypeDefinition().getPrimitiveType()) return false;

        // TODO: Support json path.
        return (o.getVal().equals(getVal()));
    }
}
