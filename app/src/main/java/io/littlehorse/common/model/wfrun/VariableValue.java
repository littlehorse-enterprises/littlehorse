package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.VariableMutationTypePb;
import io.littlehorse.common.proto.VariableTypePb;
import io.littlehorse.common.proto.VariableValuePb;
import io.littlehorse.common.proto.VariableValuePbOrBuilder;
import io.littlehorse.common.util.LHUtil;

public class VariableValue extends LHSerializable<VariableValuePb> {

    public VariableTypePb type;

    @JsonIgnore
    public String jsonObjVal;

    @JsonIgnore
    public String jsonArrVal;

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
                jsonArrVal = p.getJsonArr();
                break;
            case JSON_OBJ:
                jsonObjVal = p.getJsonObj();
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
                out.setJsonArr(jsonArrVal);
                break;
            case JSON_OBJ:
                out.setJsonObj(jsonObjVal);
                break;
            case DOUBLE:
                out.setDouble(doubleVal);
                break;
            case BOOL:
                out.setBool(boolVal);
                break;
            case STR:
                out.setStr(strVal);
                break;
            case INT:
                out.setInt(intVal);
                break;
            case BYTES:
                out.setBytes(ByteString.copyFrom(bytesVal));
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
    ) {
        if (operation == VariableMutationTypePb.ASSIGN) {
            return rhs;
        } else {
            throw new RuntimeException("Unsupported operation: " + operation);
        }
    }

    public VariableValue jsonPath(String path) throws LHVarSubError {
        throw new RuntimeException("JsonPath not implemented yet");
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
                return LHUtil.strToJsonArr(this.jsonArrVal);
            case JSON_OBJ:
                return LHUtil.strToJsonObj(this.jsonObjVal);
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

        VariableValue out = new VariableValue();
        out.type = VariableTypePb.BYTES;
        out.bytesVal = b;
        return out;
    }
}
