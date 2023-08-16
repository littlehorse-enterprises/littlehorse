package io.littlehorse.common.model.wfrun;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.jayway.jsonpath.JsonPath;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.VariableMutationTypePb;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class VariableValueModel extends LHSerializable<VariableValue> {

    public VariableType type;
    public Map<String, Object> jsonObjVal;
    public List<Object> jsonArrVal;
    public Double doubleVal;
    public Boolean boolVal;
    public String strVal;
    public Long intVal;
    public byte[] bytesVal;

    public static VariableValueModel fromProto(VariableValue proto) {
        VariableValueModel out = new VariableValueModel();
        out.initFrom(proto);
        return out;
    }

    public Class<VariableValue> getProtoBaseClass() {
        return VariableValue.class;
    }

    public void initFrom(Message proto) {
        VariableValue p = (VariableValue) proto;
        type = p.getType();
        switch (type) {
            case JSON_ARR:
                jsonArrVal = LHUtil.strToJsonArr(p.getJsonArr());
                break;
            case JSON_OBJ:
                jsonObjVal = LHUtil.strToJsonObj(p.getJsonObj());
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
            case NULL:
                // nothing to do
                break;
            case UNRECOGNIZED:
                throw new RuntimeException("Panic: impossible type");
        }
    }

    private String getJsonString() throws LHVarSubError {
        if (type == VariableType.JSON_ARR) {
            return toProto().getJsonArr();
        } else if (type == VariableType.JSON_OBJ) {
            return toProto().getJsonObj();
        } else {
            throw new RuntimeException(
                "This is a bug: Variable is of type " +
                type +
                " but asked for json str from that variable."
            );
        }
    }

    public void updateJsonViaJsonPath(String jsonPath, Object toPut)
        throws LHVarSubError {
        String jsonString = getJsonString();
        String newJsonString;
        try {
            newJsonString =
                JsonPath.parse(jsonString).set(jsonPath, toPut).jsonString();
        } catch (Exception jsonExn) {
            throw new LHVarSubError(
                jsonExn,
                "Failed updating jsonPath " +
                jsonPath +
                " on object " +
                jsonString +
                ": " +
                jsonExn.getMessage()
            );
        }

        if (type == VariableType.JSON_ARR) {
            jsonArrVal = LHUtil.strToJsonArr(newJsonString);
        } else if (type == VariableType.JSON_OBJ) {
            jsonObjVal = LHUtil.strToJsonObj(newJsonString);
        }
    }

    public VariableValue.Builder toProto() {
        VariableValue.Builder out = VariableValue.newBuilder();
        out.setType(type);
        switch (type) {
            case JSON_ARR:
                if (jsonArrVal != null) {
                    out.setJsonArr(LHUtil.objToString(jsonArrVal));
                }
                break;
            case JSON_OBJ:
                if (jsonObjVal != null) {
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
            case NULL:
                // nothing to do
                break;
            case UNRECOGNIZED:
                throw new RuntimeException("Panic: impossible type");
        }

        return out;
    }

    public VariableValueModel getCopy() {
        VariableValueModel out = new VariableValueModel();
        out.initFrom(toProto().build());
        return out;
    }

    public VariableValueModel operate(
        VariableMutationTypePb operation,
        VariableValueModel rhs,
        VariableType typeToCoerceTo
    ) throws LHVarSubError {
        if (type != VariableType.NULL) {
            if (type != typeToCoerceTo) {
                throw new LHVarSubError(
                    null,
                    "got unexpected variable type. Thought it was a " +
                    typeToCoerceTo +
                    " but is a " +
                    type
                );
            }
        }

        if (operation == VariableMutationTypePb.ASSIGN) {
            if (type == VariableType.NULL) return rhs.coerceToType(typeToCoerceTo);

            return rhs.coerceToType(type);
        } else if (operation == VariableMutationTypePb.ADD) {
            return add(rhs);
        } else if (operation == VariableMutationTypePb.SUBTRACT) {
            return subtract(rhs);
        } else if (operation == VariableMutationTypePb.MULTIPLY) {
            return multiply(rhs);
        } else if (operation == VariableMutationTypePb.DIVIDE) {
            return divide(rhs);
        } else if (operation == VariableMutationTypePb.EXTEND) {
            return extend(rhs);
        } else if (operation == VariableMutationTypePb.REMOVE_IF_PRESENT) {
            return removeIfPresent(rhs);
        } else if (operation == VariableMutationTypePb.REMOVE_INDEX) {
            return removeIndex(rhs);
        } else if (operation == VariableMutationTypePb.REMOVE_KEY) {
            return removeKey(rhs);
        }
        throw new RuntimeException("Unsupported operation: " + operation);
    }

    @SuppressWarnings("unchecked")
    public VariableValueModel jsonPath(String path) throws LHVarSubError {
        Object val;
        String jsonStr;
        if (type == VariableType.JSON_ARR) {
            jsonStr = LHUtil.objToString(jsonArrVal);
        } else if (type == VariableType.JSON_OBJ) {
            jsonStr = LHUtil.objToString(jsonObjVal);
        } else {
            throw new LHVarSubError(null, "Cannot jsonpath on " + type);
        }

        try {
            val = JsonPath.parse(jsonStr).read(path);
        } catch (Exception exn) {
            throw new LHVarSubError(
                exn,
                "Failed accessing path " +
                path +
                " on data " +
                jsonStr +
                "  :\n" +
                exn.getMessage()
            );
        }

        if (val == null) {
            // TODO: Need to handle this better.
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
            log.error(
                "Not possible to get this from jsonpath {}={}",
                val,
                val.getClass()
            );
            throw new RuntimeException("Not possible to get this from jsonpath");
        }
    }

    public VariableValueModel add(VariableValueModel rhs) throws LHVarSubError {
        if (type == VariableType.INT) {
            return new VariableValueModel(asInt().intVal + rhs.asInt().intVal);
        } else if (type == VariableType.DOUBLE) {
            return new VariableValueModel(
                asDouble().doubleVal + rhs.asDouble().doubleVal
            );
        } else if (type == VariableType.STR) {
            return new VariableValueModel(asStr().strVal + rhs.asStr().strVal);
        }
        throw new LHVarSubError(null, "Cannot add to var of type " + type);
    }

    public VariableValueModel subtract(VariableValueModel rhs) throws LHVarSubError {
        if (type == VariableType.INT) {
            return new VariableValueModel(asInt().intVal - rhs.asInt().intVal);
        } else if (type == VariableType.DOUBLE) {
            return new VariableValueModel(
                asDouble().doubleVal - rhs.asDouble().doubleVal
            );
        }
        throw new LHVarSubError(null, "Cannot subtract from var of type " + type);
    }

    public VariableValueModel multiply(VariableValueModel rhs) throws LHVarSubError {
        if (type == VariableType.INT) {
            return new VariableValueModel(
                (long) (asInt().intVal * rhs.asDouble().doubleVal)
            );
        } else if (type == VariableType.DOUBLE) {
            return new VariableValueModel(
                (double) (asDouble().doubleVal * rhs.asDouble().doubleVal)
            );
        }
        throw new LHVarSubError(null, "Cannot multiply var of type " + type);
    }

    public VariableValueModel divide(VariableValueModel rhs) throws LHVarSubError {
        if (type == VariableType.INT) {
            if (rhs.type == VariableType.DOUBLE) {
                return new VariableValueModel(
                    (long) (asDouble().doubleVal / rhs.asDouble().doubleVal)
                );
            } else {
                return new VariableValueModel((long) (intVal / rhs.asInt().intVal));
            }
        } else if (type == VariableType.DOUBLE) {
            return new VariableValueModel(
                (double) (asDouble().doubleVal / rhs.asDouble().doubleVal)
            );
        }
        throw new LHVarSubError(null, "Cannot divide var of type " + type);
    }

    public VariableValueModel extend(VariableValueModel rhs) throws LHVarSubError {
        if (type == VariableType.JSON_ARR) {
            List<Object> newList = new ArrayList<>();
            newList.addAll(asArr().jsonArrVal);
            newList.addAll(rhs.asArr().jsonArrVal);
            return new VariableValueModel(newList);
        } else if (type == VariableType.BYTES) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                if (bytesVal != null) baos.write(bytesVal);
                rhs = rhs.asBytes();
                if (rhs.bytesVal != null) baos.write(rhs.bytesVal);
            } catch (IOException exn) {
                throw new LHVarSubError(exn, "Failed concatenating bytes");
            }
            return new VariableValueModel(baos.toByteArray());
        }
        throw new LHVarSubError(null, "Cannot extend var of type " + type);
    }

    public VariableValueModel removeIfPresent(VariableValueModel other)
        throws LHVarSubError {
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

    public VariableValueModel removeIndex(VariableValueModel other)
        throws LHVarSubError {
        List<Object> lhsList = asArr().jsonArrVal;
        Long longIdx = other.asInt().intVal;
        if (longIdx == null) {
            throw new LHVarSubError(null, "Tried to remove null index");
        }
        int idx = longIdx.intValue();
        lhsList.remove(idx);
        return new VariableValueModel(lhsList);
    }

    public VariableValueModel removeKey(VariableValueModel other)
        throws LHVarSubError {
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
            case NULL:
                return null;
            case UNRECOGNIZED:
        }
        return null;
    }

    public VariableValueModel coerceToType(VariableType otherType)
        throws LHVarSubError {
        if (type == VariableType.NULL || otherType == VariableType.NULL) {
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
        } else {
            throw new LHVarSubError(
                null,
                "Unsupported type for coersion: " + otherType
            );
        }
    }

    public VariableValueModel asInt() throws LHVarSubError {
        Long out = null;

        if (type == VariableType.INT) {
            out = intVal;
        } else if (type == VariableType.DOUBLE) {
            out = doubleVal == null ? null : doubleVal.longValue();
        } else if (type == VariableType.STR) {
            try {
                out = strVal == null ? null : Long.valueOf(strVal);
            } catch (Exception exn) {
                throw new LHVarSubError(exn, "Couldn't convert strVal to INT");
            }
        } else {
            log.error(
                "Can't convert {} to INT ({})",
                type,
                LHUtil.objToString(jsonArrVal)
            );
            throw new LHVarSubError(null, "Cant convert " + type + " to INT");
        }

        VariableValueModel result = new VariableValueModel();
        result.type = VariableType.INT;
        result.intVal = out;
        return result;
    }

    public VariableValueModel asDouble() throws LHVarSubError {
        Double out = null;

        if (type == VariableType.INT) {
            out = intVal == null ? null : Double.valueOf(intVal);
        } else if (type == VariableType.DOUBLE) {
            out = doubleVal;
        } else if (type == VariableType.STR) {
            try {
                out = strVal == null ? null : Double.valueOf(strVal);
            } catch (Exception exn) {
                throw new LHVarSubError(exn, "Couldn't convert STR to DOUBLE");
            }
        } else {
            throw new LHVarSubError(null, "Cant convert " + type + " to DOUBLE");
        }

        VariableValueModel result = new VariableValueModel();
        result.type = VariableType.DOUBLE;
        result.doubleVal = out;
        return result;
    }

    public VariableValueModel asBool() throws LHVarSubError {
        if (type != VariableType.BOOL) {
            throw new LHVarSubError(null, "Unsupported converting to bool");
        }
        return getCopy();
    }

    public VariableValueModel asStr() throws LHVarSubError {
        String s = getVal() == null ? null : getVal().toString();
        VariableValueModel out = new VariableValueModel();
        out.type = VariableType.STR;
        out.strVal = s;
        return out;
    }

    public VariableValueModel asArr() throws LHVarSubError {
        if (type != VariableType.JSON_ARR) {
            throw new LHVarSubError(null, "Converting to JSON_ARR not supported.");
        }
        return getCopy();
    }

    public VariableValueModel asObj() throws LHVarSubError {
        if (type != VariableType.JSON_OBJ) {
            throw new LHVarSubError(null, "Converting to JSON_OBJ not supported.");
        }
        return getCopy();
    }

    public VariableValueModel asBytes() throws LHVarSubError {
        byte[] b;
        if (type == VariableType.BYTES) {
            b = bytesVal;
        } else {
            b = LHUtil.objToBytes(getVal());
        }
        return new VariableValueModel(b);
    }

    public VariableValueModel() {
        type = VariableType.NULL;
    }

    public VariableValueModel(long val) {
        intVal = val;
        type = VariableType.INT;
    }

    public VariableValueModel(double val) {
        doubleVal = val;
        type = VariableType.DOUBLE;
    }

    public VariableValueModel(String val) {
        strVal = val;
        type = VariableType.STR;
    }

    public VariableValueModel(byte[] bytes) {
        bytesVal = bytes;
        type = VariableType.BYTES;
    }

    public VariableValueModel(List<Object> val) {
        jsonArrVal = val;
        type = VariableType.JSON_ARR;
    }

    public VariableValueModel(Map<String, Object> val) {
        jsonObjVal = val;
        type = VariableType.JSON_OBJ;
    }

    public VariableValueModel(boolean val) {
        type = VariableType.BOOL;
        boolVal = val;
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
            case NULL:
            case UNRECOGNIZED:
                valuePair = null;
        }
        return valuePair;
    }
}
