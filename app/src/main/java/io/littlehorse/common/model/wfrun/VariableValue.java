package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import com.jayway.jsonpath.JsonPath;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.VariableMutationTypePb;
import io.littlehorse.common.proto.VariableTypePb;
import io.littlehorse.common.proto.VariableValuePb;
import io.littlehorse.common.proto.VariableValuePbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VariableValue extends LHSerializable<VariableValuePb> {

    public VariableTypePb type;

    @JsonIgnore
    public Map<String, Object> jsonObjVal;

    @JsonIgnore
    public List<Object> jsonArrVal;

    @JsonIgnore
    public Double doubleVal;

    @JsonIgnore
    public Boolean boolVal;

    @JsonIgnore
    public String strVal;

    @JsonIgnore
    public Long intVal;

    @JsonIgnore
    public byte[] bytesVal;

    public static VariableValue fromProto(VariableValuePbOrBuilder proto) {
        VariableValue out = new VariableValue();
        out.initFrom(proto);
        return out;
    }

    @JsonIgnore
    public Class<VariableValuePb> getProtoBaseClass() {
        return VariableValuePb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        VariableValuePbOrBuilder p = (VariableValuePbOrBuilder) proto;
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
            case VOID:
                // nothing to do
                break;
            case UNRECOGNIZED:
                throw new RuntimeException("Panic: impossible type");
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
            case VOID:
                // nothing to do
                break;
            case UNRECOGNIZED:
                throw new RuntimeException("Panic: impossible type");
        }

        return out;
    }

    @JsonIgnore
    public VariableValue getCopy() {
        VariableValue out = new VariableValue();
        out.initFrom(toProto());
        return out;
    }

    public VariableValue operate(
        VariableMutationTypePb operation,
        VariableValue rhs
    ) throws LHVarSubError {
        if (operation == VariableMutationTypePb.ASSIGN) {
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

        if (!(val instanceof List)) {
            throw new RuntimeException("Unexpected result from jsonpath");
        }

        val = ((List<Object>) val).get(0);

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
            LHUtil.log(val, val.getClass());
            throw new RuntimeException(
                "Not possible to get this from jsonpath"
            );
        }
    }

    public VariableValue add(VariableValue rhs) throws LHVarSubError {
        if (type == VariableTypePb.INT) {
            return new VariableValue(asInt().intVal + rhs.asInt().intVal);
        } else if (type == VariableTypePb.DOUBLE) {
            return new VariableValue(
                asDouble().doubleVal + rhs.asDouble().doubleVal
            );
        } else if (type == VariableTypePb.STR) {
            return new VariableValue(asStr().strVal + rhs.asStr().strVal);
        }
        throw new LHVarSubError(null, "Cannot add to var of type " + type);
    }

    public VariableValue subtract(VariableValue rhs) throws LHVarSubError {
        if (type == VariableTypePb.INT) {
            return new VariableValue(asInt().intVal - rhs.asInt().intVal);
        } else if (type == VariableTypePb.DOUBLE) {
            return new VariableValue(
                asDouble().doubleVal - rhs.asDouble().doubleVal
            );
        }
        throw new LHVarSubError(
            null,
            "Cannot subtract from var of type " + type
        );
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

    public VariableValue removeIfPresent(VariableValue other)
        throws LHVarSubError {
        List<Object> lhsList = asArr().jsonArrVal;
        Object o = other.getVal();
        lhsList.removeIf(i -> Objects.equals(i, o));
        return new VariableValue(lhsList);
    }

    public VariableValue removeIndex(VariableValue other) throws LHVarSubError {
        List<Object> lhsList = asArr().jsonArrVal;
        Long idx = other.asInt().intVal;
        if (idx == null) {
            throw new LHVarSubError(null, "Tried to remove null index");
        }
        lhsList.remove(idx);
        return new VariableValue(lhsList);
    }

    public VariableValue removeKey(VariableValue other) throws LHVarSubError {
        Map<String, Object> m = asObj().jsonObjVal;
        m.remove(other.asStr().strVal);
        return new VariableValue(m);
    }

    // Intended for use only by Jackson to pretty-print the Json.
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
                return LHUtil.b64Encode(this.bytesVal);
            case UNRECOGNIZED:
            default:
                return null;
        }
    }

    public VariableValue coerceToType(VariableTypePb otherType)
        throws LHVarSubError {
        if (type == VariableTypePb.VOID || otherType == VariableTypePb.VOID) {
            throw new LHVarSubError(
                null,
                "Coercing to or from VOID not supported."
            );
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
            LHUtil.log(LHUtil.objToString(jsonArrVal));
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
            throw new LHVarSubError(
                null,
                "Cant convert " + type + " to DOUBLE"
            );
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
            throw new LHVarSubError(
                null,
                "Converting to JSON_ARR not supported."
            );
        }
        return getCopy();
    }

    public VariableValue asObj() throws LHVarSubError {
        if (type != VariableTypePb.JSON_OBJ) {
            throw new LHVarSubError(
                null,
                "Converting to JSON_OBJ not supported."
            );
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
        type = VariableTypePb.VOID;
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
}
