package io.littlehorse.common.model.getable.core.variable;

import com.google.gson.JsonSyntaxException;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.PathNotFoundException;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.util.LHUtil;
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

    private ValueCase type;
    private Map<String, Object> jsonObjVal;
    private List<Object> jsonArrVal;
    private Double doubleVal;
    private Boolean boolVal;
    private String strVal;
    private Long intVal;
    private byte[] bytesVal;
    private WfRunIdModel wfRunId;

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
        type = p.getValueCase();
        switch (type) {
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
            case VALUE_NOT_SET:
                // it's a null variable! Nothing to do.
                break;
        }
    }

    public VariableType getType() {
        return fromValueCase(type);
    }

    private String getJsonString() throws LHVarSubError {
        if (type == ValueCase.JSON_ARR) {
            return toProto().getJsonArr();
        } else if (type == ValueCase.JSON_OBJ) {
            return toProto().getJsonObj();
        } else {
            throw new IllegalStateException(
                    "This is a bug: Variable is of type " + type + " but asked for json str from that variable.");
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

        if (getType() == VariableType.JSON_ARR) {
            jsonArrVal = LHUtil.strToJsonArr(newJsonString);
        } else if (getType() == VariableType.JSON_OBJ) {
            jsonObjVal = LHUtil.strToJsonObj(newJsonString);
        }
    }

    public VariableValue.Builder toProto() {
        VariableValue.Builder out = VariableValue.newBuilder();
        switch (type) {
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
        return type == ValueCase.VALUE_NOT_SET;
    }

    public VariableValueModel operate(
            VariableMutationType operation, VariableValueModel rhs, VariableType typeToCoerceTo) throws LHVarSubError {

        if (operation == VariableMutationType.ASSIGN) {
            if (rhs.isNull()) {
                return new VariableValueModel();
            } else {
                return rhs.coerceToType(typeToCoerceTo);
            }
        }

        if (operation == VariableMutationType.ADD) {
            return typeToCoerceTo == VariableType.DOUBLE ? asDouble().add(rhs) : add(rhs);
        } else if (operation == VariableMutationType.SUBTRACT) {
            return typeToCoerceTo == VariableType.DOUBLE ? asDouble().subtract(rhs) : subtract(rhs);
        } else if (operation == VariableMutationType.MULTIPLY) {
            return typeToCoerceTo == VariableType.DOUBLE ? asDouble().multiply(rhs) : multiply(rhs);
        } else if (operation == VariableMutationType.DIVIDE) {
            return typeToCoerceTo == VariableType.DOUBLE ? asDouble().divide(rhs) : divide(rhs);
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
        return type == ValueCase.VALUE_NOT_SET;
    }

    public VariableValueModel jsonPath(String path) throws LHVarSubError {
        Object val;
        String jsonStr;
        if (getType() == VariableType.JSON_ARR) {
            jsonStr = LHUtil.objToString(jsonArrVal);
        } else if (getType() == VariableType.JSON_OBJ) {
            jsonStr = LHUtil.objToString(jsonObjVal);
        } else {
            throw new LHVarSubError(null, "Cannot jsonpath on " + type);
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
        if (rhs.getType() == null) {
            throw new LHVarSubError(null, "Cannot add by null");
        }
        if (getType() == VariableType.INT) {
            if (rhs.getType() == VariableType.DOUBLE) {
                return new VariableValueModel(asDouble().doubleVal + rhs.asDouble().doubleVal);
            }
            return new VariableValueModel(asInt().intVal + rhs.asInt().intVal);
        } else if (getType() == VariableType.DOUBLE) {
            return new VariableValueModel(asDouble().doubleVal + rhs.asDouble().doubleVal);
        } else if (getType() == VariableType.STR) {
            return new VariableValueModel(asStr().strVal + rhs.asStr().strVal);
        } else if (getType() == VariableType.JSON_ARR) {
            List<Object> jsonList = getJsonArrVal();
            List<Object> newList = new ArrayList<>();
            newList.addAll(jsonList);
            newList.add(rhs.getVal());
            return new VariableValueModel(newList);
        }
        throw new LHVarSubError(null, "Cannot add to var of type " + type);
    }

    public VariableValueModel subtract(VariableValueModel rhs) throws LHVarSubError {
        if (rhs.getType() == null) {
            throw new LHVarSubError(null, "Cannot subtract null");
        }
        if (getType() == VariableType.INT) {
            if (rhs.getType() == VariableType.DOUBLE) {
                return new VariableValueModel((long) (asDouble().doubleVal - rhs.asDouble().doubleVal));
            }
            return new VariableValueModel(asInt().intVal - rhs.asInt().intVal);
        } else if (getType() == VariableType.DOUBLE) {
            return new VariableValueModel(asDouble().doubleVal - rhs.asDouble().doubleVal);
        }
        throw new LHVarSubError(null, "Cannot subtract from var of type " + type);
    }

    public VariableValueModel multiply(VariableValueModel rhs) throws LHVarSubError {
        if (rhs.getType() == null) {
            throw new LHVarSubError(null, "Cannot multiply by null");
        }
        if (getType() == VariableType.INT) {
            if (rhs.getType() == VariableType.DOUBLE) {
                return new VariableValueModel((long) (asDouble().doubleVal * rhs.asDouble().doubleVal));
            }
            return new VariableValueModel((long) (intVal * rhs.asInt().intVal));
        } else if (getType() == VariableType.DOUBLE) {
            return new VariableValueModel((double) (asDouble().doubleVal * rhs.asDouble().doubleVal));
        }
        throw new LHVarSubError(null, "Cannot multiply value of type " + type);
    }

    public VariableValueModel divide(VariableValueModel rhs) throws LHVarSubError {
        if (rhs.getType() == null) {
            throw new LHVarSubError(null, "Cannot divide by null");
        }

        if (rhs.asDouble().doubleVal == 0) {
            throw new LHVarSubError(null, "Cannot divide by zero");
        }

        if (getType() == VariableType.INT) {
            if (rhs.getType() == VariableType.DOUBLE) {
                return new VariableValueModel((long) (asDouble().doubleVal / rhs.asDouble().doubleVal));
            } else {
                return new VariableValueModel((long) (intVal / rhs.asInt().intVal));
            }
        } else if (getType() == VariableType.DOUBLE) {
            return new VariableValueModel((double) (asDouble().doubleVal / rhs.asDouble().doubleVal));
        }
        throw new LHVarSubError(null, "Cannot divide var of type " + type);
    }

    public VariableValueModel extend(VariableValueModel rhs) throws LHVarSubError {
        if (getType() == VariableType.JSON_ARR) {
            List<Object> newList = new ArrayList<>();
            newList.addAll(asArr().jsonArrVal);
            newList.addAll(rhs.asArr().jsonArrVal);
            return new VariableValueModel(newList);
        } else if (getType() == VariableType.BYTES) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                if (bytesVal != null) baos.write(bytesVal);
                rhs = rhs.asBytes();
                if (rhs.bytesVal != null) baos.write(rhs.bytesVal);
            } catch (IOException exn) {
                throw new LHVarSubError(exn, "Failed concatenating bytes");
            }
            return new VariableValueModel(baos.toByteArray());
        } else if (getType() == VariableType.STR) {
            return new VariableValueModel(this.strVal + rhs.asStr().strVal);
        }
        throw new LHVarSubError(null, "Cannot extend var of type " + type);
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
        switch (type) {
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
            case VALUE_NOT_SET:
        }
        return null;
    }

    public VariableValueModel coerceToType(VariableType otherType) throws LHVarSubError {
        if (getType() == null || otherType == null) {
            throw new LHVarSubError(null, "Coercing to or from NULL not supported.");
        }

        if (otherType == VariableType.INT) {
            return asInt();
        } else if (otherType == VariableType.DOUBLE) {
            return asDouble();
        } else if (otherType == VariableType.BOOL) {
            return asBool();
        } else if (otherType == VariableType.STR) {
            return asStr();
        } else if (otherType == VariableType.JSON_ARR) {
            return asArr();
        } else if (otherType == VariableType.JSON_OBJ) {
            return asObj();
        } else if (otherType == VariableType.BYTES) {
            return asBytes();
        } else if (otherType == VariableType.WF_RUN_ID) {
            return asWfRunId();
        } else {
            throw new LHVarSubError(null, "Unsupported type for coersion: " + otherType);
        }
    }

    public VariableValueModel asInt() throws LHVarSubError {
        Long out = null;

        if (getType() == VariableType.INT) {
            out = intVal;
        } else if (getType() == VariableType.DOUBLE) {
            out = doubleVal == null ? null : doubleVal.longValue();
        } else if (getType() == VariableType.STR) {
            try {
                out = strVal == null ? null : Long.valueOf(strVal);
            } catch (Exception exn) {
                throw new LHVarSubError(exn, "Couldn't convert strVal to INT");
            }
        } else {
            String typeDescription = type == ValueCase.VALUE_NOT_SET ? "NULL" : type.toString();
            throw new LHVarSubError(null, "Cant convert " + typeDescription + " to INT");
        }

        if (out == null) {
            // If this happens, then there is a seriously impossible bug.
            throw new IllegalStateException("Should be impossible for out to be null");
        }

        return new VariableValueModel(out);
    }

    public VariableValueModel asDouble() throws LHVarSubError {
        if (getType() == VariableType.INT) {
            return new VariableValueModel(Double.valueOf(intVal));
        } else if (getType() == VariableType.DOUBLE) {
            return new VariableValueModel(doubleVal);
        } else if (getType() == VariableType.STR) {
            try {
                return new VariableValueModel(Double.parseDouble(strVal));
            } catch (Exception exn) {
                throw new LHVarSubError(exn, "Couldn't convert STR to DOUBLE");
            }
        } else {
            throw new LHVarSubError(null, "Cant convert " + type + " to DOUBLE");
        }
    }

    public VariableValueModel asWfRunId() throws LHVarSubError {
        if (getType() == VariableType.WF_RUN_ID) {
            return new VariableValueModel(wfRunId);
        } else if (getType() == VariableType.DOUBLE) {
            return new VariableValueModel(new WfRunIdModel(String.valueOf(doubleVal)));
        } else if (getType() == VariableType.INT) {
            return new VariableValueModel(new WfRunIdModel(String.valueOf(intVal)));
        } else if (getType() == VariableType.STR) {
            return new VariableValueModel((WfRunIdModel) WfRunIdModel.fromString(strVal, WfRunIdModel.class));
        } else {
            throw new LHVarSubError(null, "Cant convert " + type + " to WF_RUN_ID");
        }
    }

    public VariableValueModel asBool() throws LHVarSubError {
        if (getType() != VariableType.BOOL) {
            throw new LHVarSubError(null, "Unsupported converting to bool");
        }
        return getCopy();
    }

    public VariableValueModel asStr() throws LHVarSubError {
        if (type == ValueCase.VALUE_NOT_SET) return new VariableValueModel();

        return new VariableValueModel(getVal().toString());
    }

    public VariableValueModel asArr() throws LHVarSubError {
        if (getType() != VariableType.JSON_ARR) {
            throw new LHVarSubError(null, "Converting to JSON_ARR not supported.");
        }
        return getCopy();
    }

    public VariableValueModel asObj() throws LHVarSubError {
        if (getType() != VariableType.JSON_OBJ) {
            throw new LHVarSubError(null, "Converting to JSON_OBJ not supported.");
        }
        return getCopy();
    }

    public VariableValueModel asBytes() throws LHVarSubError {
        byte[] b;
        if (getType() == VariableType.BYTES) {
            b = bytesVal;
        } else {
            b = LHUtil.objToBytes(getVal());
        }
        return new VariableValueModel(b);
    }

    public VariableValueModel() {
        type = ValueCase.VALUE_NOT_SET;
    }

    public VariableValueModel(long val) {
        intVal = val;
        type = ValueCase.INT;
    }

    public VariableValueModel(double val) {
        doubleVal = val;
        type = ValueCase.DOUBLE;
    }

    public VariableValueModel(String val) {
        strVal = val;
        type = ValueCase.STR;
    }

    public VariableValueModel(byte[] bytes) {
        bytesVal = bytes;
        type = ValueCase.BYTES;
    }

    public VariableValueModel(List<Object> val) {
        jsonArrVal = val;
        type = ValueCase.JSON_ARR;
    }

    public VariableValueModel(Map<String, Object> val) {
        jsonObjVal = val;
        type = ValueCase.JSON_OBJ;
    }

    public VariableValueModel(boolean val) {
        type = ValueCase.BOOL;
        boolVal = val;
    }

    public VariableValueModel(WfRunIdModel wfRunId) {
        type = ValueCase.WF_RUN_ID;
        this.wfRunId = wfRunId;
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
        switch (type) {
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
            case VALUE_NOT_SET:
                valuePair = null;
        }
        return valuePair;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof VariableValueModel)) return false;
        VariableValueModel o = (VariableValueModel) other;

        if (o.getType() != getType()) return false;

        // TODO: Support json path.
        return (o.getVal().equals(getVal()));
    }
}
