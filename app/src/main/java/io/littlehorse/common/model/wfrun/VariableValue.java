package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
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
    public Integer intVal;

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
        throw new RuntimeException("implement me");
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
}
