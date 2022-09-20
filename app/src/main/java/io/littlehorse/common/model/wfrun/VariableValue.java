package io.littlehorse.common.model.wfrun;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.VariableMutationTypePb;
import io.littlehorse.common.proto.VariableTypePb;
import io.littlehorse.common.proto.VariableValuePb;
import io.littlehorse.common.proto.VariableValuePbOrBuilder;

public class VariableValue extends LHSerializable<VariableValuePb> {

    public VariableTypePb type;
    public String jsonObjVal;
    public String jsonArrVal;
    public Double doubleVal;
    public Boolean boolVal;
    public String strVal;
    public Integer intVal;
    public byte[] bytesVal;

    public static VariableValue fromProto(VariableValuePbOrBuilder proto) {
        VariableValue out = new VariableValue();
        out.initFrom(proto);
        return out;
    }

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
            case UNRECOGNIZED:
                throw new RuntimeException("Panic: impossible type");
        }

        return out;
    }

    public VariableValue getCopy() {
        VariableValue out = new VariableValue();
        out.initFrom(toProto());
        return out;
    }

    public VariableValue operate(VariableMutationTypePb operation, VariableValue rhs) {
        throw new RuntimeException("implement me");
    }
}
