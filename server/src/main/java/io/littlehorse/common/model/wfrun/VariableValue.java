package io.littlehorse.common.model.wfrun;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.jayway.jsonpath.JsonPath;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.VariableMutationTypePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.common.proto.VariableValuePb;
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
public class VariableValue extends LHSerializable<VariableValuePb> {

    public VariableTypePb type;
    public Map<String, Object> jsonObjVal;
    public List<Object> jsonArrVal;
    public Double doubleVal;
    public Boolean boolVal;
    public String strVal;
    public Long intVal;
    public byte[] bytesVal;

    public static VariableValue fromProto(VariableValuePb proto) {
        VariableValue out = new VariableValue();
        out.initFrom(proto);
        return out;
    }

    public Class<VariableValuePb> getProtoBaseClass() {
        return VariableValuePb.class;
    }

    public void initFrom(Message proto) {
        VariableValuePb p = (VariableValuePb) proto;
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
        if (type == VariableTypePb.JSON_ARR) {
            return toProto().getJsonArr();
        } else if (type == VariableTypePb.JSON_OBJ) {
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

        if (type == VariableTypePb.JSON_ARR) {
            jsonArrVal = LHUtil.strToJsonArr(newJsonString);
        } else if (type == VariableTypePb.JSON_OBJ) {
            jsonObjVal = LHUtil.strToJsonObj(newJsonString);
        }
    }

    public VariableValuePb.Builder toProto() {
        VariableValuePb.Builder out = VariableValuePb.newBuilder();
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

    public VariableValue getCopy() {
        VariableValue out = new VariableValue();
        out.initFrom(toProto().build());
        return out;
    }

    public VariableValue operate(
        VariableMutationTypePb operation,
        VariableValue rhs,
        VariableTypePb typeToCoerceTo
    ) throws LHVarSubError {
        if (type != VariableTypePb.NULL) {
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
            if (type == VariableTypePb.NULL) return rhs.coerceToType(typeToCoerceTo);

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
    public VariableValue jsonPath(String path) throws LHVarSubError {
        Object val;
        String jsonStr;
        if (type == VariableTypePb.JSON_ARR) {
            jsonStr = LHUtil.objToString(jsonArrVal);
        } else if (type == VariableTypePb.JSON_OBJ) {
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
            return new VariableValue();
        }

        if (Long.class.isAssignableFrom(val.getClass())) {
            return new VariableValue((long) val);
        } else if (Integer.class.isAssignableFrom(val.getClass())) {
            return new VariableValue(Long.valueOf((long) ((Integer) val)));
        } else if (String.class.isAssignableFrom(val.getClass())) {
            return new VariableValue((String) val);
        } else if (Boolean.class.isAssignableFrom(val.getClass())) {
            return new VariableValue((Boolean) val);
        } else if (Double.class.isAssignableFrom(val.getClass())) {
            return new VariableValue((Double) val);
        } else if (Map.class.isAssignableFrom(val.getClass())) {
            return new VariableValue((Map<String, Object>) val);
        } else if (List.class.isAssignableFrom(val.getClass())) {
            return new VariableValue((List<Object>) val);
        } else {
            log.error(
                "Not possible to get this from jsonpath {}={}",
                val,
                val.getClass()
            );
            throw new RuntimeException("Not possible to get this from jsonpath");
        }
    }

    public VariableValue add(VariableValue rhs) throws LHVarSubError {
        if (type == VariableTypePb.INT) {
            return new VariableValue(asInt().intVal + rhs.asInt().intVal);
        } else if (type == VariableTypePb.DOUBLE) {
            return new VariableValue(asDouble().doubleVal + rhs.asDouble().doubleVal);
        } else if (type == VariableTypePb.STR) {
            return new VariableValue(asStr().strVal + rhs.asStr().strVal);
        }
        throw new LHVarSubError(null, "Cannot add to var of type " + type);
    }

    public VariableValue subtract(VariableValue rhs) throws LHVarSubError {
        if (type == VariableTypePb.INT) {
            return new VariableValue(asInt().intVal - rhs.asInt().intVal);
        } else if (type == VariableTypePb.DOUBLE) {
            return new VariableValue(asDouble().doubleVal - rhs.asDouble().doubleVal);
        }
        throw new LHVarSubError(null, "Cannot subtract from var of type " + type);
    }

    public VariableValue multiply(VariableValue rhs) throws LHVarSubError {
        if (type == VariableTypePb.INT) {
            return new VariableValue(
                (long) (asInt().intVal * rhs.asDouble().doubleVal)
            );
        } else if (type == VariableTypePb.DOUBLE) {
            return new VariableValue(
                (double) (asDouble().doubleVal * rhs.asDouble().doubleVal)
            );
        }
        throw new LHVarSubError(null, "Cannot multiply var of type " + type);
    }

    public VariableValue divide(VariableValue rhs) throws LHVarSubError {
        if (type == VariableTypePb.INT) {
            if (rhs.type == VariableTypePb.DOUBLE) {
                return new VariableValue(
                    (long) (asDouble().doubleVal / rhs.asDouble().doubleVal)
                );
            } else {
                return new VariableValue((long) (intVal / rhs.asInt().intVal));
            }
        } else if (type == VariableTypePb.DOUBLE) {
            return new VariableValue(
                (double) (asDouble().doubleVal / rhs.asDouble().doubleVal)
            );
        }
        throw new LHVarSubError(null, "Cannot divide var of type " + type);
    }

    public VariableValue extend(VariableValue rhs) throws LHVarSubError {
        if (type == VariableTypePb.JSON_ARR) {
            List<Object> newList = new ArrayList<>();
            newList.addAll(asArr().jsonArrVal);
            newList.addAll(rhs.asArr().jsonArrVal);
            return new VariableValue(newList);
        } else if (type == VariableTypePb.BYTES) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                if (bytesVal != null) baos.write(bytesVal);
                rhs = rhs.asBytes();
                if (rhs.bytesVal != null) baos.write(rhs.bytesVal);
            } catch (IOException exn) {
                throw new LHVarSubError(exn, "Failed concatenating bytes");
            }
            return new VariableValue(baos.toByteArray());
        }
        throw new LHVarSubError(null, "Cannot extend var of type " + type);
    }

    public VariableValue removeIfPresent(VariableValue other) throws LHVarSubError {
        List<Object> lhsList = asArr().jsonArrVal;
        Object o = other.getVal();
        lhsList.removeIf(i -> {
            return isEqual(i, o);
        });
        return new VariableValue(lhsList);
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

    public VariableValue removeIndex(VariableValue other) throws LHVarSubError {
        List<Object> lhsList = asArr().jsonArrVal;
        Long longIdx = other.asInt().intVal;
        if (longIdx == null) {
            throw new LHVarSubError(null, "Tried to remove null index");
        }
        int idx = longIdx.intValue();
        lhsList.remove(idx);
        return new VariableValue(lhsList);
    }

    public VariableValue removeKey(VariableValue other) throws LHVarSubError {
        Map<String, Object> m = asObj().jsonObjVal;
        m.remove(other.asStr().strVal);
        return new VariableValue(m);
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

    public VariableValue coerceToType(VariableTypePb otherType) throws LHVarSubError {
        if (type == VariableTypePb.NULL || otherType == VariableTypePb.NULL) {
            throw new LHVarSubError(null, "Coercing to or from NULL not supported.");
        }

        if (otherType == VariableTypePb.INT) {
            return asInt();
        } else if (otherType == VariableTypePb.DOUBLE) {
            return asDouble();
        } else if (otherType == VariableTypePb.BOOL) {
            return asBool();
        } else if (otherType == VariableTypePb.STR) {
            return asStr();
        } else if (otherType == VariableTypePb.JSON_ARR) {
            return asArr();
        } else if (otherType == VariableTypePb.JSON_OBJ) {
            return asObj();
        } else if (otherType == VariableTypePb.BYTES) {
            return asBytes();
        } else {
            throw new LHVarSubError(
                null,
                "Unsupported type for coersion: " + otherType
            );
        }
    }

    public VariableValue asInt() throws LHVarSubError {
        Long out = null;

        if (type == VariableTypePb.INT) {
            out = intVal;
        } else if (type == VariableTypePb.DOUBLE) {
            out = doubleVal == null ? null : doubleVal.longValue();
        } else if (type == VariableTypePb.STR) {
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

        VariableValue result = new VariableValue();
        result.type = VariableTypePb.INT;
        result.intVal = out;
        return result;
    }

    public VariableValue asDouble() throws LHVarSubError {
        Double out = null;

        if (type == VariableTypePb.INT) {
            out = intVal == null ? null : Double.valueOf(intVal);
        } else if (type == VariableTypePb.DOUBLE) {
            out = doubleVal;
        } else if (type == VariableTypePb.STR) {
            try {
                out = strVal == null ? null : Double.valueOf(strVal);
            } catch (Exception exn) {
                throw new LHVarSubError(exn, "Couldn't convert STR to DOUBLE");
            }
        } else {
            throw new LHVarSubError(null, "Cant convert " + type + " to DOUBLE");
        }

        VariableValue result = new VariableValue();
        result.type = VariableTypePb.DOUBLE;
        result.doubleVal = out;
        return result;
    }

    public VariableValue asBool() throws LHVarSubError {
        if (type != VariableTypePb.BOOL) {
            throw new LHVarSubError(null, "Unsupported converting to bool");
        }
        return getCopy();
    }

    public VariableValue asStr() throws LHVarSubError {
        String s = getVal() == null ? null : getVal().toString();
        VariableValue out = new VariableValue();
        out.type = VariableTypePb.STR;
        out.strVal = s;
        return out;
    }

    public VariableValue asArr() throws LHVarSubError {
        if (type != VariableTypePb.JSON_ARR) {
            throw new LHVarSubError(null, "Converting to JSON_ARR not supported.");
        }
        return getCopy();
    }

    public VariableValue asObj() throws LHVarSubError {
        if (type != VariableTypePb.JSON_OBJ) {
            throw new LHVarSubError(null, "Converting to JSON_OBJ not supported.");
        }
        return getCopy();
    }

    public VariableValue asBytes() throws LHVarSubError {
        byte[] b;
        if (type == VariableTypePb.BYTES) {
            b = bytesVal;
        } else {
            b = LHUtil.objToBytes(getVal());
        }
        return new VariableValue(b);
    }

    public VariableValue() {
        type = VariableTypePb.NULL;
    }

    public VariableValue(long val) {
        intVal = val;
        type = VariableTypePb.INT;
    }

    public VariableValue(double val) {
        doubleVal = val;
        type = VariableTypePb.DOUBLE;
    }

    public VariableValue(String val) {
        strVal = val;
        type = VariableTypePb.STR;
    }

    public VariableValue(byte[] bytes) {
        bytesVal = bytes;
        type = VariableTypePb.BYTES;
    }

    public VariableValue(List<Object> val) {
        jsonArrVal = val;
        type = VariableTypePb.JSON_ARR;
    }

    public VariableValue(Map<String, Object> val) {
        jsonObjVal = val;
        type = VariableTypePb.JSON_OBJ;
    }

    public VariableValue(boolean val) {
        type = VariableTypePb.BOOL;
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
